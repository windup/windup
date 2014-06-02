package org.apache.wicket.behavior;

import org.apache.wicket.*;

public class InvalidBehaviorIdException extends WicketRuntimeException{
    private static final long serialVersionUID=1L;
    private final Component component;
    private final int behaviorId;
    public InvalidBehaviorIdException(final Component component,final int behaviorId){
        super(String.format("Cannot find behavior with id '%d' on component '%s' in page '%s'. Perhaps the behavior did not properly implement getStatelessHint() and returned 'true' to indicate that it is stateless instead of returning 'false' to indicate that it is stateful.",new Object[] { behaviorId,component.getClassRelativePath(),component.getPage() }));
        this.component=component;
        this.behaviorId=behaviorId;
    }
    public Component getComponent(){
        return this.component;
    }
    public int getBehaviorId(){
        return this.behaviorId;
    }
}
