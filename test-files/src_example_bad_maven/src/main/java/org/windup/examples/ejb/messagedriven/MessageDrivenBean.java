package org.windup.examples.ejb.messagedriven;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;

@MessageDriven(
	name="MyNameForMessageDrivenBean",
	activationConfig = {
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/MyQueue")
	}
)
public class MessageDrivenBean {
	// stub... this is just here to test annotation scanning rulesActivationConfigProperty
}
