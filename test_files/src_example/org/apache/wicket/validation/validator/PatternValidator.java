package org.apache.wicket.validation.validator;

import java.util.regex.*;
import org.apache.wicket.util.parse.metapattern.*;
import org.apache.wicket.validation.*;
import java.util.*;

public class PatternValidator extends StringValidator{
    private static final long serialVersionUID=1L;
    private final Pattern pattern;
    private boolean reverse;
    public PatternValidator(final String pattern){
        this(Pattern.compile(pattern));
    }
    public PatternValidator(final String pattern,final int flags){
        this(Pattern.compile(pattern,flags));
    }
    public PatternValidator(final Pattern pattern){
        super();
        this.reverse=false;
        this.pattern=pattern;
    }
    public PatternValidator(final MetaPattern pattern){
        this(pattern.pattern());
    }
    public final Pattern getPattern(){
        return this.pattern;
    }
    public PatternValidator setReverse(final boolean reverse){
        this.reverse=reverse;
        return this;
    }
    protected Map<String,Object> variablesMap(final IValidatable<String> validatable){
        final Map<String,Object> map=super.variablesMap(validatable);
        map.put("pattern",this.pattern.pattern());
        return map;
    }
    public String toString(){
        return "[PatternValidator pattern = "+this.pattern+"]";
    }
    protected void onValidate(final IValidatable<String> validatable){
        if(this.pattern.matcher((CharSequence)validatable.getValue()).matches()==this.reverse){
            this.error(validatable);
        }
    }
}
