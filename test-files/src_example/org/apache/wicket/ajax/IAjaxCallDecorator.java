package org.apache.wicket.ajax;

import org.apache.wicket.*;

public interface IAjaxCallDecorator extends IClusterable{
    public static final String WICKET_CALL_RESULT_VAR="wcall";
    CharSequence decorateScript(Component p0,CharSequence p1);
    CharSequence decorateOnSuccessScript(Component p0,CharSequence p1);
    CharSequence decorateOnFailureScript(Component p0,CharSequence p1);
}
