package weblogic.ejbgen;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivationConfigProperty{
    String propertyName();
    String propertyValue();
}
