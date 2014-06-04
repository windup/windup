package org.apache.wicket.settings;

import org.apache.wicket.util.lang.*;
import java.io.*;

public interface IStoreSettings{
    int getInmemoryCacheSize();
    void setInmemoryCacheSize(int p0);
    Bytes getMaxSizePerSession();
    void setMaxSizePerSession(Bytes p0);
    File getFileStoreFolder();
    void setFileStoreFolder(File p0);
    int getAsynchronousQueueCapacity();
    void setAsynchronousQueueCapacity(int p0);
    void setAsynchronous(boolean p0);
    boolean isAsynchronous();
}
