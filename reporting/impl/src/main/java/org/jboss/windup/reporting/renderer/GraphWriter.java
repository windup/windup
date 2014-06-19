package org.jboss.windup.reporting.renderer;

import java.io.IOException;
import java.io.OutputStream;

public interface GraphWriter {
	
	public void writeGraph(OutputStream os) throws IOException;
	
}
