package org.apache.wicket.validation.validator;

import org.apache.wicket.validation.*;
import java.util.*;

public abstract class StringValidator extends AbstractValidator<String>{
    private static final long serialVersionUID=1L;
    public static StringValidator exactLength(final int length){
        return new ExactLengthValidator(length);
    }
    public static StringValidator lengthBetween(final int minimum,final int maximum){
        return new LengthBetweenValidator(minimum,maximum);
    }
    public static StringValidator maximumLength(final int maximum){
        return new MaximumLengthValidator(maximum);
    }
    public static StringValidator minimumLength(final int minimum){
        return new MinimumLengthValidator(minimum);
    }
    public static class ExactLengthValidator extends StringValidator{
        private static final long serialVersionUID=1L;
        private final int length;
        public ExactLengthValidator(final int length){
            super();
            this.length=length;
        }
        public final int getLength(){
            return this.length;
        }
        protected void onValidate(final IValidatable<String> validatable){
            if(validatable.getValue().length()!=this.length){
                this.error(validatable);
            }
        }
        protected String resourceKey(){
            return "StringValidator.exact";
        }
        protected Map<String,Object> variablesMap(final IValidatable<String> validatable){
            final Map<String,Object> map=super.variablesMap(validatable);
            map.put("length",(validatable.getValue()!=null)?validatable.getValue().length():0);
            map.put("exact",this.length);
            return map;
        }
    }
    public static class LengthBetweenValidator extends StringValidator{
        private static final long serialVersionUID=1L;
        private final int maximum;
        private final int minimum;
        public LengthBetweenValidator(final int minimum,final int maximum){
            super();
            this.minimum=minimum;
            this.maximum=maximum;
        }
        public final int getMaximum(){
            return this.maximum;
        }
        public final int getMinimum(){
            return this.minimum;
        }
        protected void onValidate(final IValidatable<String> validatable){
            final String value=validatable.getValue();
            if(value.length()<this.minimum||value.length()>this.maximum){
                this.error(validatable);
            }
        }
        protected String resourceKey(){
            return "StringValidator.range";
        }
        protected Map<String,Object> variablesMap(final IValidatable<String> validatable){
            final Map<String,Object> map=super.variablesMap(validatable);
            map.put("minimum",this.minimum);
            map.put("maximum",this.maximum);
            map.put("length",validatable.getValue().length());
            return map;
        }
    }
    public static class MaximumLengthValidator extends StringValidator{
        private static final long serialVersionUID=1L;
        private final int maximum;
        public MaximumLengthValidator(final int maximum){
            super();
            this.maximum=maximum;
        }
        public final int getMaximum(){
            return this.maximum;
        }
        protected void onValidate(final IValidatable<String> validatable){
            if(validatable.getValue().length()>this.maximum){
                this.error(validatable);
            }
        }
        protected String resourceKey(){
            return "StringValidator.maximum";
        }
        protected Map<String,Object> variablesMap(final IValidatable<String> validatable){
            final Map<String,Object> map=super.variablesMap(validatable);
            map.put("maximum",this.maximum);
            map.put("length",validatable.getValue().length());
            return map;
        }
    }
    public static class MinimumLengthValidator extends StringValidator{
        private static final long serialVersionUID=1L;
        private final int minimum;
        public MinimumLengthValidator(final int minimum){
            super();
            this.minimum=minimum;
        }
        public final int getMinimum(){
            return this.minimum;
        }
        protected void onValidate(final IValidatable<String> validatable){
            if(validatable.getValue().length()<this.minimum){
                this.error(validatable);
            }
        }
        protected String resourceKey(){
            return "StringValidator.minimum";
        }
        protected Map<String,Object> variablesMap(final IValidatable<String> validatable){
            final Map<String,Object> map=super.variablesMap(validatable);
            map.put("minimum",this.minimum);
            map.put("length",validatable.getValue().length());
            return map;
        }
    }
}
