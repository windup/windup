package org.jboss.windup.graph.renderer;

import java.io.IOException;
import java.io.OutputStream;

public interface GraphWriter {
	
	public void writeGraph(OutputStream os) throws IOException;
	
}
