package org.apache.wicket.request.resource.caching;

import org.apache.wicket.request.resource.*;
import java.io.*;
import org.apache.wicket.util.resource.*;

public interface IStaticCacheableResource extends IResource{
    Serializable getCacheKey();
    IResourceStream getCacheableResourceStream();
}
