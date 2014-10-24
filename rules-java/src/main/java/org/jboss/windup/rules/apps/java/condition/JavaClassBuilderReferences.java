package org.jboss.windup.rules.apps.java.condition;

public class JavaClassBuilderReferences
{
    private String inputVarName;

    public JavaClassBuilderReferences(String inputVarName)
    {
        this.inputVarName = inputVarName;
    }

    public JavaClassBuilder references(String regex)
    {
        JavaClassBuilder javaClass = JavaClass.references(regex);
        ((JavaClass) javaClass).setInputVariablesName(this.inputVarName);
        return javaClass;
    }
}
