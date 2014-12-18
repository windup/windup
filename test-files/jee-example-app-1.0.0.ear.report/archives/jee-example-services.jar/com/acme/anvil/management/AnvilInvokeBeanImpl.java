package com.acme.anvil.management;

import javax.management.NotCompliantMBeanException;
import com.acme.anvil.management.AnvilInvokeBean;
import javax.management.StandardMBean;

public class AnvilInvokeBeanImpl extends StandardMBean implements AnvilInvokeBean{
    private int invoked;
    public AnvilInvokeBeanImpl() throws NotCompliantMBeanException{
        super(AnvilInvokeBean.class);
        this.invoked=0;
    }
    public void addInvoked(){
        ++this.invoked;
    }
    public int getInvoked(){
        return this.invoked;
    }
}
