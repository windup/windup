package org.apache.wicket.util.resource.locator.caching;

import org.apache.wicket.util.resource.*;
import org.apache.wicket.util.file.*;

class FileResourceStreamReference extends AbstractResourceStreamReference{
    private final String fileName;
    FileResourceStreamReference(final FileResourceStream fileResourceStream){
        super();
        this.fileName=fileResourceStream.getFile().getAbsolutePath();
        this.saveResourceStream((IResourceStream)fileResourceStream);
    }
    public FileResourceStream getReference(){
        final FileResourceStream fileResourceStream=new FileResourceStream(new File(this.fileName));
        this.restoreResourceStream((IResourceStream)fileResourceStream);
        return fileResourceStream;
    }
}
