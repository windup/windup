package org.apache.wicket.validation.validator;

public class EmailAddressValidator extends PatternValidator{
    private static final long serialVersionUID=1L;
    private static final EmailAddressValidator INSTANCE;
    public static EmailAddressValidator getInstance(){
        return EmailAddressValidator.INSTANCE;
    }
    protected EmailAddressValidator(){
        super("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z]{2,}){1}$)",2);
    }
    static{
        INSTANCE=new EmailAddressValidator();
    }
}
