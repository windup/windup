package org.apache.wicket.protocol.http.servlet;

import org.apache.wicket.*;
import java.io.*;

public class ResponseIOException extends WicketRuntimeException{
    private static final long serialVersionUID=1L;
    public ResponseIOException(final IOException cause){
        super(cause);
    }
}
