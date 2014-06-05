package org.apache.wicket.protocol.http.servlet;

import javax.servlet.http.*;
import org.apache.wicket.util.lang.*;
import java.util.regex.*;
import java.util.*;
import org.apache.wicket.util.string.*;

public abstract class AbstractRequestWrapperFactory{
    private static final Pattern commaSeparatedValuesPattern;
    private boolean enabled;
    public AbstractRequestWrapperFactory(){
        super();
        this.enabled=true;
    }
    public final boolean isEnabled(){
        return this.enabled;
    }
    public final void setEnabled(final boolean enabled){
        this.enabled=enabled;
    }
    public HttpServletRequest getWrapper(final HttpServletRequest request){
        if(this.isEnabled()&&this.needsWrapper(request)){
            return this.newRequestWrapper(request);
        }
        return request;
    }
    abstract boolean needsWrapper(final HttpServletRequest p0);
    public abstract HttpServletRequest newRequestWrapper(final HttpServletRequest p0);
    public static final Pattern[] commaDelimitedListToPatternArray(final String commaDelimitedPatterns){
        final String[] patterns=commaDelimitedListToStringArray(commaDelimitedPatterns);
        final List<Pattern> patternsList=(List<Pattern>)Generics.newArrayList();
        for(final String pattern : patterns){
            try{
                patternsList.add(Pattern.compile(pattern));
            }
            catch(PatternSyntaxException e){
                throw new IllegalArgumentException("Illegal pattern syntax '"+pattern+"'",(Throwable)e);
            }
        }
        return (Pattern[])patternsList.toArray(new Pattern[patternsList.size()]);
    }
    public static final String[] commaDelimitedListToStringArray(final String commaDelimitedStrings){
        if(Strings.isEmpty((CharSequence)commaDelimitedStrings)){
            return new String[0];
        }
        return AbstractRequestWrapperFactory.commaSeparatedValuesPattern.split((CharSequence)commaDelimitedStrings);
    }
    public static final String listToCommaDelimitedString(final List<String> stringList){
        return Strings.join(", ",(List)stringList);
    }
    public static final boolean matchesOne(final String str,final Pattern... patterns){
        for(final Pattern pattern : patterns){
            if(pattern.matcher((CharSequence)str).matches()){
                return true;
            }
        }
        return false;
    }
    static{
        commaSeparatedValuesPattern=Pattern.compile("\\s*,\\s*");
    }
}
