import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;

@MessageDriven(activationConfig = { @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue") })
public class JakartaEJBMessageDrivenAnnotated implements MessageListener {

    public JakartaEJBMessageDrivenAnnotated() {}

    public void JakartaEJBMessageDrivenAnnotated(Message message) {}

}