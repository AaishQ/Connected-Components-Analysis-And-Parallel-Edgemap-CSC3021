
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

// This class represents the adjacency matrix of a graph as a sparse matrix
// in coordinate format (COO)
public class SparseMatrixCOO extends SparseMatrix {
    // TODO: variable declarations
	
    // I have added source and destination variables so that we can traverse correctly, and map which vertices are connected to which specific edges.
	int[] edgeSource;
	int[] edgeDestination;
	
	
	
	
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph

    public SparseMatrixCOO( String file ) {
	try {
	    InputStreamReader is
		= new InputStreamReader( new FileInputStream( file ), "UTF-8" );
	    BufferedReader rd = new BufferedReader( is );
	    readFile( rd );
	} catch( FileNotFoundException e ) {
	    System.err.println( "File not found: " + e );
	    return;
	} catch( UnsupportedEncodingException e ) {
	    System.err.println( "Unsupported encoding exception: " + e );
	    return;
	} catch( Exception e ) {
	    System.err.println( "Exception: " + e );
	    return;
	}
    }

    int getNext( BufferedReader rd ) throws Exception {
	String line = rd.readLine();
	if( line == null )
	    throw new Exception( "premature end of file" );
	return Integer.parseInt( line );
    }

    void getNextPair( BufferedReader rd, int pair[] ) throws Exception {
	String line = rd.readLine();
	if( line == null )
	    throw new Exception( "premature end of file" );
	StringTokenizer st = new StringTokenizer( line );
	pair[0] = Integer.parseInt( st.nextToken() );
	pair[1] = Integer.parseInt( st.nextToken() );
    }

    void readFile( BufferedReader rd ) throws Exception {
	String line = rd.readLine();
	if( line == null )
	    throw new Exception( "premature end of file" );
	if( !line.equalsIgnoreCase( "COO" ) )
	    throw new Exception( "file format error -- header" );
	
	num_vertices = getNext(rd);
	num_edges = getNext(rd);

	// TODO: Allocate memory for the COO representation
	
	// I have allocated the memory below
	
	edgeSource = new int[num_edges];
	edgeDestination = new int[num_edges];

	int edge[] = new int[2];
	for( int i=0; i < num_edges; ++i ) {
	    getNextPair( rd, edge );
	    
	    edgeSource[i] = edge[0];
	    edgeDestination[i] = edge[1]; 
	    // TODO:
	    //    Insert edge with source edge[0] and destination edge[1]
	    // I have inserted the instructed source and destination correctly above within the loop

	    
	    
	}
    }

    // Return number of vertices in the graph
    public int getNumVertices() { return num_vertices; }

    // Return number of edges in the graph
    public int getNumEdges() { return num_edges; }

    // Auxiliary function for PageRank calculation
    public void calculateOutDegree( int outdeg[] ) {
	// TODO:
	//    Calculate the out-degree for every vertex, i.e., the number of edges where a vertex appears as a source vertex.
    	
    	
	// I have added two for-loops below. The first one to intialise the array 'outdeg' to zero
    	for(int i=0; i < num_vertices; ++i) {
    		outdeg[i] = 0;	
    	}
    	
    	// This is to count the outgoing edges for each vertex source
    	for(int i=0; i < num_edges; ++i) {
    		outdeg[edgeSource[i]]++;
    	}
    	
    }

    public void edgemap( Relax relax ) {
	// TODO:
	//    Iterate over all edges in the sparse matrix and calculate
	//    the contribution to the new PageRank value of a destination
	//    vertex made by the corresponding source vertex
    	
  
	// I have created a for loop to iterate through each edge and apply an update to the PageRank value by traversing each index and getting the source / destination
    	for ( int i=0; i < num_edges; ++i) {
    		relax.relax(edgeSource[i], edgeDestination[i]); 
    	}
    }

    public void ranged_edgemap( Relax relax, int from, int to ) {
	// Only implement for parallel/concurrent processing
	// if you find it useful. Not relevant for the first assignment.
    	
    	// I have made a for-loop to iterate over the edges to and from a certain range 
    	for (int i = from; i < to; ++i) {
    		relax.relax( edgeSource[i], edgeDestination[i]); 
    	}
    }
}
 