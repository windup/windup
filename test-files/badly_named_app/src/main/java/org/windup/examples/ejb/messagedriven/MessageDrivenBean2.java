package org.windup.examples.ejb.messagedriven;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

@MessageDriven(
        name="MyName",
        activationConfig = @ActivationConfigProperty(propertyName = "destination", propertyValue="jms/OtherQueue")
)
public class MessageDrivenBean2 {
	// stub... this is just here to test annotation scanning rulesActivationConfigProperty
}
