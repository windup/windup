package org.jboss.windup.utils.el;

/**
 *  Evaluates expression like:
 * 
    String greet = new IExprLangEvaluator.SimpleEvaluator().evaluateEL(
        "Hello ${person.name} ${person.surname}, ${person.age}!", 
        new HashMap(){{
            put("person", new Person("Ondra"));
        }}
    );
 *  
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public interface IExprLangEvaluator {
    
    public String evaluateEL( String template ); // , Map<String, ? extends Object> properties
    
    
    
    public static interface IVariablesProvider<T> {
        T getVariable( String name );
    }
            
}// class
