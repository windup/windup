package org.apache.wicket.response.filter;

import org.apache.wicket.util.string.*;

public interface IResponseFilter{
    AppendingStringBuffer filter(AppendingStringBuffer p0);
}
