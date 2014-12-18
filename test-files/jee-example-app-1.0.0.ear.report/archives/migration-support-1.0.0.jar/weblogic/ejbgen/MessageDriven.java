package weblogic.ejbgen;

import weblogic.ejbgen.ActivationConfigProperty;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageDriven{
    String ejbName() default "";
    String destinationType() default "";
    String runAsPrincipalName() default "";
    String runAs() default "";
    Class messageListenerInterface() default Object.class;
    ActivationConfigProperty[] activationConfig() default {};
    String destinationJndiName() default "";
    String description() default "";
}
