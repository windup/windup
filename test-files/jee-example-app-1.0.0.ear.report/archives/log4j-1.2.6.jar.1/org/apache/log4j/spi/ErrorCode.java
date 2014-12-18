package org.apache.log4j.spi;

public interface ErrorCode{
    public static final int GENERIC_FAILURE=0;
    public static final int WRITE_FAILURE=1;
    public static final int FLUSH_FAILURE=2;
    public static final int CLOSE_FAILURE=3;
    public static final int FILE_OPEN_FAILURE=4;
    public static final int MISSING_LAYOUT=5;
    public static final int ADDRESS_PARSE_FAILURE=6;
}
