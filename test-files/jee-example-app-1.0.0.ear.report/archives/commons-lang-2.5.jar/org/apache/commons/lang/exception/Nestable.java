package org.apache.commons.lang.exception;

import java.io.PrintStream;
import java.io.PrintWriter;

public interface Nestable{
    Throwable getCause();
    String getMessage();
    String getMessage(int p0);
    String[] getMessages();
    Throwable getThrowable(int p0);
    int getThrowableCount();
    Throwable[] getThrowables();
    int indexOfThrowable(Class p0);
    int indexOfThrowable(Class p0,int p1);
    void printStackTrace(PrintWriter p0);
    void printStackTrace(PrintStream p0);
    void printPartialStackTrace(PrintWriter p0);
}
