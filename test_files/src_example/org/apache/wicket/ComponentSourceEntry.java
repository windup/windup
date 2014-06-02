package org.apache.wicket;

import org.apache.wicket.util.string.*;
import java.util.*;
import org.slf4j.*;

@Deprecated
abstract class ComponentSourceEntry implements IClusterable{
    private static final long serialVersionUID=1L;
    final String id;
    private final IComponentSource componentSource;
    private final String componentInfo;
    private static final Logger logger;
    private final void checkId(final String name,final String id){
        if(id.indexOf(40)!=-1||id.indexOf(40)!=-1||id.indexOf(32)!=-1||id.indexOf(44)!=-1){
            throw new IllegalStateException(name+"'"+id+"' is not valid, it may not contain any of the ' ', '(', ')', ',' characters");
        }
    }
    private final void appendComponent(final AppendingStringBuffer buffer,final Component component){
        this.checkId("Component id",component.getId());
        buffer.append(component.getId());
        buffer.append(' ');
        final Object markupId=component.getMarkupIdImpl();
        if(markupId!=null){
            if(markupId instanceof String){
                this.checkId("Component markup id",(String)markupId);
            }
            if(markupId instanceof Integer){
                buffer.append('*');
            }
            buffer.append(markupId);
            buffer.append(' ');
        }
        if(component instanceof MarkupContainer&&((MarkupContainer)component).iterator().hasNext()){
            buffer.append('(');
            final Iterator<? extends Component> i=((MarkupContainer)component).iterator();
            while(i.hasNext()){
                final Component child=(Component)i.next();
                this.appendComponent(buffer,child);
                if(i.hasNext()){
                    buffer.append(',');
                }
            }
            buffer.append(')');
        }
    }
    ComponentSourceEntry(final MarkupContainer container,final Component component,final IComponentSource componentSource){
        super();
        this.id=component.getId();
        this.componentSource=componentSource;
        final AppendingStringBuffer buffer=new AppendingStringBuffer();
        this.appendComponent(buffer,component);
        this.componentInfo=buffer.toString();
        System.out.println("Info: "+this.componentInfo);
    }
    protected abstract void setChild(final MarkupContainer p0,final int p1,final Component p2);
    Component reconstruct(final MarkupContainer parent,final int index){
        final Component component=this.componentSource.restoreComponent(this.id);
        if(parent!=null){
            component.setParent(parent);
        }
        component.beforeRender();
        parseComponentInfo(parent,this.componentInfo,component);
        return component;
    }
    private static String getComponentSubString(final String string){
        int len=string.length();
        int i=string.indexOf(44);
        if(i!=-1&&i<len){
            len=i;
        }
        i=string.indexOf(41);
        if(i!=-1&&i<len){
            len=i;
        }
        i=string.substring(0,len).indexOf(40);
        if(i!=-1&&i<len){
            len=i;
        }
        return string.substring(0,len);
    }
    private static MarkupContainer applyComponentInfo(final MarkupContainer parent,final String info,Component component){
        if(parent==null){
            return null;
        }
        final String[] parts=info.split(" ");
        final String id=parts[0];
        Object markupId;
        if(parts.length==2){
            markupId=null;
        }
        else{
            if(parts.length!=3){
                throw new IllegalArgumentException("Malformed component info string '"+info+"'.");
            }
            if(parts[1]!=null&&parts[1].startsWith("*")){
                markupId=Integer.valueOf(parts[1].substring(1));
            }
            else{
                markupId=parts[1];
            }
        }
        if(component==null){
            component=parent.get(id);
        }
        if(component==null){
            ComponentSourceEntry.logger.warn("Couldn't find component with id '"+id+"'. This means that the component was not properly reconstructed from ComponentSource.");
        }
        else if(markupId!=null){
            component.setMarkupIdImpl(markupId);
        }
        return (component instanceof MarkupContainer)?((MarkupContainer)component):null;
    }
    private static int parseComponentInfo(final MarkupContainer parent,final String info,final Component component){
        final String substring=getComponentSubString(info);
        int len=substring.length();
        boolean hasChildren=false;
        if(len<info.length()&&info.charAt(len)=='('){
            hasChildren=true;
            ++len;
        }
        final MarkupContainer child=applyComponentInfo(parent,substring,component);
        if(hasChildren){
            int i=0;
            final String children=info.substring(len);
            while(i<children.length()){
                if(children.charAt(i)==','){
                    ++i;
                }
                i+=parseComponentInfo(child,children.substring(i),null);
                if(children.charAt(i)==')'){
                    ++i;
                    break;
                }
            }
            return len+i;
        }
        return len;
    }
    static{
        logger=LoggerFactory.getLogger(ComponentSourceEntry.class);
    }
}
