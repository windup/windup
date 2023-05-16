import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
public class JavaxEJBMessageDrivenAnnotated implements MessageListener {

    public JakartaEJBMessageDrivenAnnotated() {}

    public void JakartaEJBMessageDrivenAnnotated(Message message) {}

}