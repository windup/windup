package org.apache.wicket.markup;

import org.apache.wicket.markup.parser.*;
import java.lang.ref.*;
import org.apache.wicket.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.value.*;
import org.apache.wicket.markup.parser.filter.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.request.*;
import java.util.*;
import org.slf4j.*;

public class ComponentTag extends MarkupElement{
    private static final Logger log;
    private static final int AUTOLINK=1;
    private static final int MODIFIED=2;
    private static final int IGNORE=4;
    private static final int AUTO_COMPONENT=8;
    private static final int NO_CLOSE_TAG=16;
    public static final int RENDER_RAW=32;
    private ComponentTag openTag;
    protected final XmlTag xmlTag;
    private int flags;
    private String id;
    private WeakReference<Class<? extends Component>> markupClassRef;
    private List<Behavior> behaviors;
    private Map<String,Object> userData;
    public ComponentTag(final String name,final XmlTag.TagType type){
        super();
        this.flags=0;
        this.markupClassRef=null;
        final XmlTag tag=new XmlTag();
        tag.setName(name);
        tag.setType(type);
        this.xmlTag=tag;
    }
    public ComponentTag(final XmlTag tag){
        super();
        this.flags=0;
        this.markupClassRef=null;
        this.xmlTag=tag;
    }
    public ComponentTag(final ComponentTag tag){
        this(tag.getXmlTag());
        tag.copyPropertiesTo(this);
    }
    public final void setFlag(final int flag,final boolean set){
        if(set){
            this.flags|=flag;
        }
        else{
            this.flags&=~flag;
        }
    }
    public final boolean getFlag(final int flag){
        return (this.flags&flag)!=0x0;
    }
    public final void addBehavior(final Behavior behavior){
        if(behavior==null){
            throw new IllegalArgumentException("Argument [behavior] cannot be null");
        }
        if(this.behaviors==null){
            this.behaviors=(List<Behavior>)Generics.newArrayList();
        }
        this.behaviors.add(behavior);
    }
    public final boolean hasBehaviors(){
        return this.behaviors!=null;
    }
    public final Iterator<? extends Behavior> getBehaviors(){
        if(this.behaviors==null){
            final List<Behavior> lst=(List<Behavior>)Collections.emptyList();
            return (Iterator<? extends Behavior>)lst.iterator();
        }
        return (Iterator<? extends Behavior>)Collections.unmodifiableCollection(this.behaviors).iterator();
    }
    public final boolean closes(final MarkupElement open){
        return open instanceof ComponentTag&&(this.openTag==open||this.getXmlTag().closes(((ComponentTag)open).getXmlTag()));
    }
    public final void enableAutolink(final boolean autolink){
        this.setFlag(1,autolink);
    }
    public final IValueMap getAttributes(){
        return this.xmlTag.getAttributes();
    }
    public final String getAttribute(final String name){
        return this.xmlTag.getAttributes().getString(name);
    }
    @Deprecated
    public final String getString(final String name){
        return this.getAttribute(name);
    }
    public final String getId(){
        return this.id;
    }
    public final int getLength(){
        return this.xmlTag.getLength();
    }
    public final String getName(){
        return this.xmlTag.getName();
    }
    public final String getNamespace(){
        return this.xmlTag.getNamespace();
    }
    public final ComponentTag getOpenTag(){
        return this.openTag;
    }
    public final int getPos(){
        return this.xmlTag.getPos();
    }
    public final XmlTag.TagType getType(){
        return this.xmlTag.getType();
    }
    public final boolean isAutolinkEnabled(){
        return this.getFlag(1);
    }
    public final boolean isClose(){
        return this.xmlTag.isClose();
    }
    public final boolean isOpen(){
        return this.xmlTag.isOpen();
    }
    public final boolean isOpen(final String id){
        return this.xmlTag.isOpen()&&this.id.equals(id);
    }
    public final boolean isOpenClose(){
        return this.xmlTag.isOpenClose();
    }
    public final boolean isOpenClose(final String id){
        return this.xmlTag.isOpenClose()&&this.id.equals(id);
    }
    public final void makeImmutable(){
        this.xmlTag.makeImmutable();
    }
    public ComponentTag mutable(){
        if(this.xmlTag.isMutable()){
            return this;
        }
        final ComponentTag tag=new ComponentTag(this.xmlTag.mutable());
        this.copyPropertiesTo(tag);
        return tag;
    }
    void copyPropertiesTo(final ComponentTag dest){
        dest.id=this.id;
        dest.flags=this.flags;
        if(this.markupClassRef!=null){
            dest.setMarkupClass((Class<Component>)this.markupClassRef.get());
        }
        if(this.behaviors!=null){
            (dest.behaviors=(List<Behavior>)new ArrayList(this.behaviors.size())).addAll(this.behaviors);
        }
    }
    public final void put(final String key,final boolean value){
        this.xmlTag.put(key,value);
        this.setModified(true);
    }
    public final void put(final String key,final int value){
        this.xmlTag.put(key,value);
        this.setModified(true);
    }
    public final void put(final String key,final CharSequence value){
        this.checkIdAttribute(key);
        this.putInternal(key,value);
    }
    public final void putInternal(final String key,final CharSequence value){
        this.xmlTag.put(key,value);
        this.setModified(true);
    }
    private void checkIdAttribute(final String key){
        if(key!=null&&key.equalsIgnoreCase("id")){
            ComponentTag.log.warn("Please use component.setMarkupId(String) to change the tag's 'id' attribute.");
        }
    }
    public final void append(final String key,final CharSequence value,final String separator){
        final String current=this.getAttribute(key);
        if(Strings.isEmpty((CharSequence)current)){
            this.xmlTag.put(key,value);
        }
        else{
            this.xmlTag.put(key,(CharSequence)(current+separator+(Object)value));
        }
        this.setModified(true);
    }
    public final void put(final String key,final StringValue value){
        this.xmlTag.put(key,value);
        this.setModified(true);
    }
    public final void putAll(final Map<String,Object> map){
        this.xmlTag.putAll(map);
        this.setModified(true);
    }
    public final void remove(final String key){
        this.xmlTag.remove(key);
        this.setModified(true);
    }
    public final boolean requiresCloseTag(){
        if(this.getNamespace()==null){
            return HtmlHandler.requiresCloseTag(this.getName());
        }
        return HtmlHandler.requiresCloseTag(this.getNamespace()+":"+this.getName());
    }
    public final void setId(final String id){
        this.id=id;
    }
    public final void setName(final String name){
        this.xmlTag.setName(name);
    }
    public final void setNamespace(final String namespace){
        this.xmlTag.setNamespace(namespace);
    }
    public final void setOpenTag(final ComponentTag tag){
        this.openTag=tag;
        this.getXmlTag().setOpenTag(tag.getXmlTag());
    }
    public final void setType(final XmlTag.TagType type){
        if(type!=this.xmlTag.getType()){
            this.xmlTag.setType(type);
            this.setModified(true);
        }
    }
    public final CharSequence syntheticCloseTagString(){
        final AppendingStringBuffer buf=new AppendingStringBuffer();
        buf.append("</");
        if(this.getNamespace()!=null){
            buf.append(this.getNamespace()).append(":");
        }
        buf.append(this.getName()).append(">");
        return (CharSequence)buf;
    }
    public CharSequence toCharSequence(){
        return this.xmlTag.toCharSequence();
    }
    public final String toString(){
        return this.toCharSequence().toString();
    }
    public final void writeOutput(final Response response,final boolean stripWicketAttributes,final String namespace){
        response.write((CharSequence)"<");
        if(this.getType()==XmlTag.TagType.CLOSE){
            response.write((CharSequence)"/");
        }
        if(this.getNamespace()!=null){
            response.write((CharSequence)this.getNamespace());
            response.write((CharSequence)":");
        }
        response.write((CharSequence)this.getName());
        String namespacePrefix=null;
        if(stripWicketAttributes){
            namespacePrefix=namespace+":";
        }
        if(this.getAttributes().size()>0){
            for(final String key : this.getAttributes().keySet()){
                if(key==null){
                    continue;
                }
                if(namespacePrefix!=null&&key.startsWith(namespacePrefix)){
                    continue;
                }
                response.write((CharSequence)" ");
                response.write((CharSequence)key);
                CharSequence value=(CharSequence)this.getAttribute(key);
                if(value==null){
                    continue;
                }
                response.write((CharSequence)"=\"");
                value=Strings.escapeMarkup(value);
                response.write(value);
                response.write((CharSequence)"\"");
            }
        }
        if(this.getType()==XmlTag.TagType.OPEN_CLOSE){
            response.write((CharSequence)"/");
        }
        response.write((CharSequence)">");
    }
    public final String toUserDebugString(){
        return this.xmlTag.toUserDebugString();
    }
    public final XmlTag getXmlTag(){
        return this.xmlTag;
    }
    public final void setModified(final boolean modified){
        this.setFlag(2,modified);
    }
    final boolean isModified(){
        return this.getFlag(2);
    }
    public boolean hasNoCloseTag(){
        return this.getFlag(16);
    }
    public void setHasNoCloseTag(final boolean hasNoCloseTag){
        this.setFlag(16,hasNoCloseTag);
    }
    public Class<? extends Component> getMarkupClass(){
        return (Class<? extends Component>)((this.markupClassRef==null)?null:((Class)this.markupClassRef.get()));
    }
    public <C extends Component> void setMarkupClass(final Class<C> wicketHeaderClass){
        if(wicketHeaderClass==null){
            this.markupClassRef=null;
        }
        else{
            this.markupClassRef=(WeakReference<Class<? extends Component>>)new WeakReference(wicketHeaderClass);
        }
    }
    public boolean equalTo(final MarkupElement element){
        if(element instanceof ComponentTag){
            final ComponentTag that=(ComponentTag)element;
            return this.getXmlTag().equalTo(that.getXmlTag());
        }
        return false;
    }
    public boolean isIgnore(){
        return this.getFlag(4);
    }
    public void setIgnore(final boolean ignore){
        this.setFlag(4,ignore);
    }
    public boolean isAutoComponentTag(){
        return this.getFlag(8);
    }
    public void setAutoComponentTag(final boolean auto){
        this.setFlag(8,auto);
    }
    public Object getUserData(final String key){
        if(this.userData==null){
            return null;
        }
        return this.userData.get(key);
    }
    public void setUserData(final String key,final Object value){
        if(this.userData==null){
            this.userData=(Map<String,Object>)new HashMap();
        }
        this.userData.put(key,value);
    }
    public void onBeforeRender(final Component component,final MarkupStream markupStream){
    }
    static{
        log=LoggerFactory.getLogger(ComponentTag.class);
    }
}
