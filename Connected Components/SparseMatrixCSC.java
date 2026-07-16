

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
// in compressed sparse columns format (CSC). The incoming edges for each
// vertex are listed.
public class SparseMatrixCSC extends SparseMatrix {
    // TODO: variable declarations
	
    // Index and source variables defined below
	int[] index_pointer;
	int[] source;
	
	
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph

    public SparseMatrixCSC( String file ) {
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
	if( !line.equalsIgnoreCase( "CSC" ) && !line.equalsIgnoreCase( "CSC-CSR" ) )
	    throw new Exception( "file format error -- header" );
	
	num_vertices = getNext(rd);
	num_edges = getNext(rd);

	// TODO: allocate data structures
	
	// Defined a counter so that it counts all edges per destination
	int[] edgeCount = new int[num_vertices];
	ArrayList<String> lines = new ArrayList<>();
	
    
	for( int i=0; i < num_vertices; ++i ) {
	    line = rd.readLine();
	    if( line == null )
		throw new Exception( "premature end of file" );
	    //Storing line
	    lines.add(line);
	    String elm[] = line.split( " " );
	    assert Integer.parseInt( elm[0] ) == i : "Error in CSC file";
	    // edgeCount will be used now to count the edges for the destination
	    edgeCount[i] = elm.length - 1;
	
	}
	   // Building the index pointer array 
	   index_pointer = new int[num_vertices+1];
	   index_pointer[0] = 0;
	   
	   for (int index1=0; index1 < num_vertices; index1++) {
		   index_pointer[index1+1] = index_pointer[index1] + edgeCount[index1];
	   }
	    
	
	    // Building the destination array and assigning it
	   	source = new int[num_edges];
	   	int index = 0;
	   	for (int destination = 0; destination < num_vertices; destination++) {
	   		String elm[] = lines.get(destination).split(" ");
	   		for( int j=1; j < elm.length; j++ ) {
	   			int src = Integer.parseInt( elm[j] );
	   		// TODO:
	   		//    Record an edge from source src to destination i
	   			
	   			// Adding index counter to record the edge from the source to destination
	   			source[index++] = src;
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
    	
	// I have created a for-loop to initialise the out degree to zero before creating another to count each vertex in the source
    	for (int i=0; i < num_vertices; ++i ) {
    		outdeg[i] = 0;
    	}
    	
    	for (int i=0; i < num_edges; ++i) {
    		outdeg[source[i]]++; 
    	}
    }
    
    public void edgemap( Relax relax ) {
	// TODO:
	//    Iterate over all edges in the sparse matrix and call "relax"
	//    on each edge.
    	
	// The for-loop and nested for-loop allow me to iterate all edges from the initial source edge and call the function
    	for (int i=0; i < num_vertices; ++i) {
    		for (int j = index_pointer[i]; j < index_pointer[i+1]; j++) {
    			relax.relax (source[j],i);
    		}
    	}
    }

    public void ranged_edgemap( Relax relax, int from, int to ) {
	// Only implement for parallel/concurrent processing
	// if you find it useful. Not relevant for the first assignment.
	//    Iterate over partition indicated by from...to and calculate
	//    the contribution to the new PageRank value of a destination
	//    vertex made by the corresponding source vertex
    	
    	// A for-loop for parallel processing by splitting the destination vertices among the threads
    	for (int destination = from; destination < to; destination++) {
    		for (int j = index_pointer[destination]; j < index_pointer[destination + 1]; j++) {
    			relax.relax (source[j], destination); 
    		}
    	}
    }
    
}

