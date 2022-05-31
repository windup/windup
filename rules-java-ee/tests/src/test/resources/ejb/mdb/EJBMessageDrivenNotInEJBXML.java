import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;

public abstract class EJBMessageDrivenNotInEJBXML implements javax.ejb.MessageDrivenBean {

    public EpiMessageDrivenBean() {
    }

    public void ejbRemove() {
    }

    public void setMessageDrivenContext(javax.ejb.MessageDrivenContext context) {
    }

    public void onMessage(javax.jms.Message msg) {

    }
}