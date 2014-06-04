package org.apache.wicket.validation.validator;

import org.apache.wicket.validation.*;
import java.util.*;
import java.text.*;

public abstract class DateValidator extends AbstractValidator<Date>{
    private static final long serialVersionUID=1L;
    public static DateValidator range(final Date minimum,final Date maximum){
        return new RangeValidator(minimum,maximum,(String)null);
    }
    public static DateValidator range(final Date minimum,final Date maximum,final String format){
        return new RangeValidator(minimum,maximum,format);
    }
    public static DateValidator minimum(final Date minimum){
        return new MinimumValidator(minimum,(String)null);
    }
    public static DateValidator minimum(final Date minimum,final String format){
        return new MinimumValidator(minimum,format);
    }
    public static DateValidator maximum(final Date maximum){
        return new MaximumValidator(maximum,(String)null);
    }
    public static DateValidator maximum(final Date maximum,final String format){
        return new MaximumValidator(maximum,format);
    }
    private static class RangeValidator extends DateValidator{
        private static final long serialVersionUID=1L;
        private final Date minimum;
        private final Date maximum;
        private final String format;
        private RangeValidator(final Date minimum,final Date maximum,final String format){
            super();
            this.minimum=minimum;
            this.maximum=maximum;
            this.format=format;
        }
        protected Map<String,Object> variablesMap(final IValidatable<Date> validatable){
            final Map<String,Object> map=super.variablesMap(validatable);
            if(this.format==null){
                map.put("minimum",this.minimum);
                map.put("maximum",this.maximum);
                map.put("inputdate",validatable.getValue());
            }
            else{
                final SimpleDateFormat sdf=new SimpleDateFormat(this.format);
                map.put("minimum",sdf.format(this.minimum));
                map.put("maximum",sdf.format(this.maximum));
                map.put("inputdate",sdf.format(validatable.getValue()));
            }
            return map;
        }
        protected String resourceKey(){
            return "DateValidator.range";
        }
        protected void onValidate(final IValidatable<Date> validatable){
            final Date value=validatable.getValue();
            if(value.before(this.minimum)||value.after(this.maximum)){
                this.error(validatable);
            }
        }
    }
    private static class MinimumValidator extends DateValidator{
        private static final long serialVersionUID=1L;
        private final Date minimum;
        private final String format;
        private MinimumValidator(final Date minimum,final String format){
            super();
            this.minimum=minimum;
            this.format=format;
        }
        protected Map<String,Object> variablesMap(final IValidatable<Date> validatable){
            final Map<String,Object> map=super.variablesMap(validatable);
            if(this.format==null){
                map.put("minimum",this.minimum);
                map.put("inputdate",validatable.getValue());
            }
            else{
                final SimpleDateFormat sdf=new SimpleDateFormat(this.format);
                map.put("minimum",sdf.format(this.minimum));
                map.put("inputdate",sdf.format(validatable.getValue()));
            }
            return map;
        }
        protected String resourceKey(){
            return "DateValidator.minimum";
        }
        protected void onValidate(final IValidatable<Date> validatable){
            final Date value=validatable.getValue();
            if(value.before(this.minimum)){
                this.error(validatable);
            }
        }
    }
    private static class MaximumValidator extends DateValidator{
        private static final long serialVersionUID=1L;
        private final Date maximum;
        private final String format;
        private MaximumValidator(final Date maximum,final String format){
            super();
            this.maximum=maximum;
            this.format=format;
        }
        protected Map<String,Object> variablesMap(final IValidatable<Date> validatable){
            final Map<String,Object> map=super.variablesMap(validatable);
            if(this.format==null){
                map.put("maximum",this.maximum);
                map.put("inputdate",validatable.getValue());
            }
            else{
                final SimpleDateFormat sdf=new SimpleDateFormat(this.format);
                map.put("maximum",sdf.format(this.maximum));
                map.put("inputdate",sdf.format(validatable.getValue()));
            }
            return map;
        }
        protected String resourceKey(){
            return "DateValidator.maximum";
        }
        protected void onValidate(final IValidatable<Date> validatable){
            final Date value=validatable.getValue();
            if(value.after(this.maximum)){
                this.error(validatable);
            }
        }
    }
}
