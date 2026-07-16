
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
import java.util.ArrayList;

// This class represents the adjacency matrix of a graph as a sparse matrix
// in compressed sparse rows format (CSR), where a row index corresponds to
// a source vertex and a column index corresponds to a destination
public class SparseMatrixCSR extends SparseMatrix {
    // TODO: variable declarations
	
    // index and destination variables below
	int[] row_pointer;
	int[] destination; 
	
	
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph

    public SparseMatrixCSR( String file ) {
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

    void readFile( BufferedReader rd ) throws Exception {
	String line = rd.readLine();
	if( line == null )
	    throw new Exception( "premature end of file" );
	if( !line.equalsIgnoreCase( "CSR" ) && !line.equalsIgnoreCase( "CSC-CSR" ) )
	    throw new Exception( "file format error -- header" );
	
	num_vertices = getNext(rd);
	num_edges = getNext(rd);

	// TODO: Allocate memory for the CSR representation
	
	// Edge Count is created and a lines array - similar to CSC
	int[] counterEdge = new int[num_vertices];
	ArrayList<String> cLines = new ArrayList<>();
	

	for( int i=0; i < num_vertices; ++i ) {
	    line = rd.readLine();
	    if( line == null )
		throw new Exception( "premature end of file" );
	    //Storing line
	    cLines.add(line);
	    String elm[] = line.split( " " );
	    assert Integer.parseInt( elm[0] ) == i : "Error in CSC file";
	    // edgeCount will be used now to count the edges for the destination
	    counterEdge[i] = elm.length - 1;
	
	}
	   // Building the index pointer array 
	   row_pointer = new int[num_vertices+1];
	   row_pointer[0] = 0;
	   
	   for (int i=0; i < num_vertices; i++) {
		   row_pointer[i+1] = row_pointer[i] + counterEdge[i];
	   }
	    
	
	    // Building the destination array and assigning it
	   	destination = new int[num_edges];
	   	int index = 0;
	   	for (int edgesrc = 0; edgesrc < num_vertices; edgesrc++) {
	   		String elm2[] = cLines.get(edgesrc).split(" ");
	   		for( int j=1; j < elm2.length; j++ ) {
	   			int edgesrc1 = Integer.parseInt( elm2[j] );
	   		// TODO:
	   		//    Record an edge from source src to destination i
	   			
	   			// Adding index counter to record the edge from the source to destination
	   			destination[index++] = edgesrc1;
	   		}
	   	}
    }

    // Return number of vertices in the graph
    public int getNumVertices() { return num_vertices; }

    // Return number of edges in the graph
    public int getNumEdges() { return num_edges; }


    // Auxiliary function for PageRank calculation
    public void calculateOutDegree( int outdeg[] ) {
	// TODO:
	//    Calculate the out-degree for every vertex, i.e., the
	//    number of edges where a vertex appears as a source vertex.
    	
	// Calculation for out degree of vertices below
    	for (int i=0; i <num_vertices; ++i) {
    		outdeg[i] = row_pointer[i+1] - row_pointer[i]; 
    	}
    }
    
    // Apply relax once to every edge in the graph
    public void edgemap( Relax relax ) {
	// TODO:
	//    Iterate over all edges in the sparse matrix and calculate
	//    the contribution to the new PageRank value of a destination
	//    vertex made by the corresponding source vertex
    	
	// iteration and added PageRank value is taken into account with the for-loop I've made below
    	for (int i=0; i < num_vertices; ++i) {
    		for ( int j = row_pointer[i]; j < row_pointer[i+1]; j++) {
    			relax.relax (i, destination[j]); 
    		}
    	}
    }

    public void ranged_edgemap( Relax relax, int from, int to ) {
	// Only implement for parallel/concurrent processing
	// if you find it useful. Not relevant for the first assignment.
    	
    	// I have made a nested for-loop to go from the starting source to ending source and iterate through all outgoing edges
    	for (int source=from; source < to; source++) {
    		for (int j = row_pointer[source]; j < row_pointer[source+1]; j++) {
    			relax.relax (source, destination[j]);
    		}
    	}
    }
}

