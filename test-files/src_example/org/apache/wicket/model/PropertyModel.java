package org.apache.wicket.model;

public class PropertyModel<T> extends AbstractPropertyModel<T>{
    private static final long serialVersionUID=1L;
    private final String expression;
    public PropertyModel(final Object modelObject,final String expression){
        super(modelObject);
        this.expression=expression;
    }
    public String toString(){
        final StringBuilder sb=new StringBuilder(super.toString());
        sb.append(":expression=[").append(this.expression).append("]");
        return sb.toString();
    }
    protected String propertyExpression(){
        return this.expression;
    }
    public static <Z> PropertyModel<Z> of(final Object parent,final String property){
        return new PropertyModel<Z>(parent,property);
    }
}
