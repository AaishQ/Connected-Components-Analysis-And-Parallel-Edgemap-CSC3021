

public class ParallelContextSimple extends ParallelContext {
    private Thread threads[] = null;
    
    private class ThreadSimple extends Thread {
	private SparseMatrix matrix;
	private Relax relax;
	private int from;
	private int to;
	
	public ThreadSimple( SparseMatrix matrix_, Relax relax_, int from_, int to_ ) {
	    matrix = matrix_;
	    relax = relax_;
	    from = from_;
	    to = to_;
	}
	
	public void run() {
	    // Process the assigned range of vertices
	    matrix.ranged_edgemap( relax, from, to );
	}
    };
    
    public ParallelContextSimple( int num_threads_ ) {
	super( num_threads_ );
	threads = new Thread[num_threads_];
    }

    public void terminate() {
	// No additional cleanup needed as threads complete and are joined
    }

    // The edgemap method for Q4 distributes the work across multiple threads.
    // Each thread processes a subset of vertices by calling ranged_edgemap.
    public void edgemap( SparseMatrix matrix, Relax relax ) {
	int num_vertices = matrix.getNumVertices();
	int num_threads = getNumThreads();
	
	// Calculate the range for each thread
	// If vertices don't divide evenly, some threads get one extra vertex
	int vertices_per_thread = num_vertices / num_threads;
	int remainder = num_vertices % num_threads;
	
	int from = 0;
	// Create and start all threads
	for( int i = 0; i < num_threads; i++ ) {
	    // Calculate the end vertex for this thread
	    int to = from + vertices_per_thread;
	    // Distribute remainder vertices among first few threads
	    if( i < remainder ) {
		to++;
	    }
	    
	    threads[i] = new ThreadSimple( matrix, relax, from, to );
	    threads[i].start();
	    
	    from = to;
	}
	
	// Wait for all threads to complete
	try {
	    for( int i = 0; i < num_threads; i++ ) {
		threads[i].join();
	    }
	} catch( InterruptedException e ) {
	    System.err.println( "Thread interrupted: " + e );
	}
    }
}
