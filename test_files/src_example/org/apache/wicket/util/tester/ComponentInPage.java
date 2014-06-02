package org.apache.wicket.util.tester;

import org.apache.wicket.*;

class ComponentInPage{
    static final String ID="testObject";
    Component component;
    boolean isInstantiated;
    ComponentInPage(){
        super();
        this.isInstantiated=false;
    }
}
