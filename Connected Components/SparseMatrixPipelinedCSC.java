// producer-consumer implementation for the Connected Components algorithm.
// It extends SparseMatrixCSC to reuse the CSC structure...
public class SparseMatrixPipelinedCSC extends SparseMatrixCSC {
    // Pipeline parameters
    private static final int BLOCK_SIZE = 128;  // Number of edges per block
    private static final int BUFFER_SIZE = 2;   // Number of blocks in buffer
    
    // Edge block structure for grouping edges together
    private static class EdgeBlock {
        int[] src;      // Source vertices in this block
        int[] dst;      // Destination vertices in this block
        int count;      // Number of valid edges in this block
        
        EdgeBlock() {
            src = new int[BLOCK_SIZE];
            dst = new int[BLOCK_SIZE];
            count = 0;
        }
        
        // Returns true if this is a sentinel block (marks end of processing)
        boolean isSentinel() {
            return count == 0;
        }
        
        // Reset block for reuse
        void reset() {
            count = 0;
        }
    }
    
    // Bounded buffer for producer-consumer communication
    private EdgeBlock[] buffer;
    private int bufferHead;     // Index where consumer reads
    private int bufferTail;     // Index where producer writes
    private int bufferCount;    // Current number of blocks in buffer
    
    // Synchronization lock for the buffer
    private final Object bufferLock = new Object();
    
    public SparseMatrixPipelinedCSC(String file) {
        super(file);
        // Initialize buffer with empty blocks
        buffer = new EdgeBlock[BUFFER_SIZE];
        bufferHead = 0;
        bufferTail = 0;
        bufferCount = 0;
        for (int i = 0; i < BUFFER_SIZE; i++) {
            buffer[i] = new EdgeBlock();
        }
    }
    
    // Producer thread: reads all edges from the graph and groups them into blocks
    private class ProducerThread extends Thread {
        private final Relax relax;
        private int blocksProduced = 0;
        
        public ProducerThread(Relax relax_) {
            relax = relax_;
        }
        
        @Override
        public void run() {
            EdgeBlock currentBlock = new EdgeBlock();
            int totalEdgesProcessed = 0;
            
            // Iterate over all vertices (destinations in CSC format)
            int numVertices = getNumVertices();
            for (int dst = 0; dst < numVertices; dst++) {
                // For each destination, iterate over all incoming edges
                for (int j = index_pointer[dst]; j < index_pointer[dst + 1]; j++) {
                    int src = source[j];
                    
                    // Add edge to current block
                    currentBlock.src[currentBlock.count] = src;
                    currentBlock.dst[currentBlock.count] = dst;
                    currentBlock.count++;
                    totalEdgesProcessed++;
                    
                    // If block is full, send it to the buffer
                    if (currentBlock.count >= BLOCK_SIZE) {
                        sendBlock(currentBlock);
                        currentBlock = new EdgeBlock();
                        blocksProduced++;
                    }
                }
            }
            
            // Send any remaining edges in the last incomplete block
            if (currentBlock.count > 0) {
                sendBlock(currentBlock);
                blocksProduced++;
            }
            
            System.err.println("Producer: Processed " + totalEdgesProcessed + " edges in " + blocksProduced + " blocks");
            
            // Send sentinel block to signal completion to consumer
            EdgeBlock sentinel = new EdgeBlock(); // Empty block is sentinel
            sendBlock(sentinel);
        }
        
        private void sendBlock(EdgeBlock block) {
            synchronized (bufferLock) {
                // Wait if buffer is full
                while (bufferCount >= BUFFER_SIZE) {
                    try {
                        bufferLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                
                // Copy block to buffer
                EdgeBlock bufferSlot = buffer[bufferTail];
                bufferSlot.count = block.count;
                // Copy edge data
                for (int i = 0; i < block.count; i++) {
                    bufferSlot.src[i] = block.src[i];
                    bufferSlot.dst[i] = block.dst[i];
                }
                
                // Update buffer indices
                bufferTail = (bufferTail + 1) % BUFFER_SIZE;
                bufferCount++;
                
                // Notify consumer that a new block is available
                bufferLock.notify();
            }
        }
    }
    
    // Consumer thread: takes blocks from buffer and processes them
    private class ConsumerThread extends Thread {
        private final Relax relax;
        private int blocksConsumed = 0;
        private int edgesProcessed = 0;
        
        public ConsumerThread(Relax relax_) {
            relax = relax_;
        }
        
        @Override
        public void run() {
            while (true) {
                EdgeBlock block = receiveBlock();
                blocksConsumed++;
                
                // Check for sentinel (empty block means producer is done)
                if (block.isSentinel()) {
                    break;
                }
                
                // Process all edges in this block
                for (int i = 0; i < block.count; i++) {
                    relax.relax(block.src[i], block.dst[i]);
                    edgesProcessed++;
                }
            }
            
            System.err.println("Consumer: Processed " + edgesProcessed + " edges in " + 
                             (blocksConsumed - 1) + " blocks"); // -1 for sentinel
        }
        
        private EdgeBlock receiveBlock() {
            synchronized (bufferLock) {
                // Wait if buffer is empty
                while (bufferCount == 0) {
                    try {
                        bufferLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return new EdgeBlock(); // Return empty sentinel
                    }
                }
                
                // Get block from buffer
                EdgeBlock block = buffer[bufferHead];
                EdgeBlock result = new EdgeBlock();
                result.count = block.count;
                
                // Copy data to result
                for (int i = 0; i < block.count; i++) {
                    result.src[i] = block.src[i];
                    result.dst[i] = block.dst[i];
                }
                
                // Reset the buffer slot for reuse
                block.reset();
                
                // Update buffer indices
                bufferHead = (bufferHead + 1) % BUFFER_SIZE;
                bufferCount--;
                
                // Notify producer that space is available
                bufferLock.notify();
                
                return result;
            }
        }
    }
    
    // Override edgemap to use the producer-consumer pipeline
    @Override
    public void edgemap(Relax relax) {
        // Create and start producer and consumer threads
        ProducerThread producer = new ProducerThread(relax);
        ConsumerThread consumer = new ConsumerThread(relax);
        
        long startTime = System.nanoTime();
        
        producer.start();
        consumer.start();
        
        // Wait for both threads to complete
        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e);
            Thread.currentThread().interrupt();
        }
        
        double elapsedTime = (System.nanoTime() - startTime) * 1e-9;
        System.err.println("Pipeline total time: " + elapsedTime + " seconds");
    }
    
    // Override ranged_edgemap - required by abstract class but not used in pipelined version
    @Override
    public void ranged_edgemap(Relax relax, int from, int to) {
        // For simplicity, we just call the regular edgemap
        // In a more sophisticated implementation, we could process only edges
        // where destination is in the range [from, to)
        System.err.println("Note: ranged_edgemap not optimized for pipelined version");
        super.ranged_edgemap(relax, from, to);
    }
}