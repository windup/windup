package org.apache.wicket.markup.html.tree;

import org.apache.wicket.model.*;
import javax.swing.tree.*;
import org.apache.wicket.behavior.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.ajax.*;
import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.markup.html.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.*;
import org.apache.wicket.ajax.markup.html.*;
import org.apache.wicket.*;

public abstract class BaseTree extends AbstractTree{
    private static final ResourceReference CSS;
    private static final long serialVersionUID=1L;
    private static final String JUNCTION_LINK_ID="junctionLink";
    private static final String NODE_COMPONENT_ID="nodeComponent";
    private LinkType linkType;
    public BaseTree(final String id){
        this(id,null);
    }
    public BaseTree(final String id,final IModel<? extends TreeModel> model){
        super(id,model);
        this.linkType=LinkType.AJAX;
    }
    protected ResourceReference getCSS(){
        return BaseTree.CSS;
    }
    protected void populateTreeItem(final WebMarkupContainer item,final int level){
        final Object node=item.getDefaultModelObject();
        final Component junctionLink=this.newJunctionLink(item,"junctionLink",node);
        junctionLink.add(new JunctionBorder(node,level));
        item.add(junctionLink);
        final Component nodeComponent=this.newNodeComponent("nodeComponent",(IModel<Object>)item.getDefaultModel());
        item.add(nodeComponent);
        item.add(new Behavior(){
            private static final long serialVersionUID=1L;
            public void onComponentTag(final Component component,final ComponentTag tag){
                final Object node=component.getDefaultModelObject();
                final String klass=BaseTree.this.getItemClass(node);
                if(!Strings.isEmpty((CharSequence)klass)){
                    final CharSequence oldClass=(CharSequence)tag.getAttribute("class");
                    if(Strings.isEmpty(oldClass)){
                        tag.put("class",(CharSequence)klass);
                    }
                    else{
                        tag.put("class",(CharSequence)((Object)oldClass+" "+klass));
                    }
                }
            }
        });
    }
    protected String getItemClass(final Object node){
        if(this.getTreeState().isNodeSelected(node)){
            return this.getSelectedClass();
        }
        return null;
    }
    protected String getSelectedClass(){
        return "selected";
    }
    protected abstract Component newNodeComponent(final String p0,final IModel<Object> p1);
    private boolean isNodeLast(final Object node){
        final Object parent=this.getParentNode(node);
        return parent==null||this.getChildAt(parent,this.getChildCount(parent)-1).equals(node);
    }
    protected Component newJunctionLink(final MarkupContainer parent,final String id,final Object node){
        MarkupContainer junctionLink;
        if(!this.isLeaf(node)){
            junctionLink=this.newLink(id,new ILinkCallback(){
                private static final long serialVersionUID=1L;
                public void onClick(final AjaxRequestTarget target){
                    if(BaseTree.this.isNodeExpanded(node)){
                        BaseTree.this.getTreeState().collapseNode(node);
                    }
                    else{
                        BaseTree.this.getTreeState().expandNode(node);
                    }
                    BaseTree.this.onJunctionLinkClicked(target,node);
                    if(target!=null){
                        BaseTree.this.updateTree(target);
                    }
                }
            });
            junctionLink.add(new Behavior(){
                private static final long serialVersionUID=1L;
                public void onComponentTag(final Component component,final ComponentTag tag){
                    if(BaseTree.this.isNodeExpanded(node)){
                        tag.put("class",(CharSequence)"junction-open");
                    }
                    else{
                        tag.put("class",(CharSequence)"junction-closed");
                    }
                }
            });
        }
        else{
            junctionLink=new WebMarkupContainer(id){
                private static final long serialVersionUID=1L;
                protected void onComponentTag(final ComponentTag tag){
                    super.onComponentTag(tag);
                    tag.setName("span");
                    tag.put("class",(CharSequence)"junction-corner");
                }
            };
        }
        return junctionLink;
    }
    protected void onJunctionLinkClicked(final AjaxRequestTarget target,final Object node){
    }
    public MarkupContainer newLink(final String id,final ILinkCallback callback){
        if(this.getLinkType()==LinkType.REGULAR){
            return new Link<Void>(id){
                private static final long serialVersionUID=1L;
                public void onClick(){
                    callback.onClick(null);
                }
            };
        }
        if(this.getLinkType()==LinkType.AJAX){
            return new AjaxLink<Void>(id){
                private static final long serialVersionUID=1L;
                public void onClick(final AjaxRequestTarget target){
                    callback.onClick(target);
                }
            };
        }
        return new AjaxFallbackLink<Void>(id){
            private static final long serialVersionUID=1L;
            public void onClick(final AjaxRequestTarget target){
                callback.onClick(target);
            }
        };
    }
    public LinkType getLinkType(){
        return this.linkType;
    }
    public void setLinkType(final LinkType linkType){
        if(this.linkType!=linkType){
            this.linkType=linkType;
        }
    }
    protected boolean isForceRebuildOnSelectionChange(){
        return false;
    }
    public void renderHead(final IHeaderResponse response){
        super.renderHead(response);
        final ResourceReference css=this.getCSS();
        if(css!=null){
            response.renderCSSReference(css);
        }
    }
    static{
        CSS=new PackageResourceReference((Class<?>)BaseTree.class,"res/base-tree.css");
    }
    private class JunctionBorder extends Behavior{
        private static final long serialVersionUID=1L;
        private final Object node;
        private final int level;
        public JunctionBorder(final Object node,final int level){
            super();
            this.node=node;
            this.level=level;
        }
        public void afterRender(final Component component){
            component.getResponse().write((CharSequence)"</td>");
        }
        public void beforeRender(final Component component){
            final Response response=component.getResponse();
            Object parent=BaseTree.this.getParentNode(this.node);
            final CharSequence[] classes=new CharSequence[this.level];
            for(int i=0;i<this.level;++i){
                if(parent==null||BaseTree.this.isNodeLast(parent)){
                    classes[i]=(CharSequence)"spacer";
                }
                else{
                    classes[i]=(CharSequence)"line";
                }
                parent=BaseTree.this.getParentNode(parent);
            }
            for(int i=this.level-1;i>=0;--i){
                response.write((CharSequence)("<td class=\""+(Object)classes[i]+"\"><span></span></td>"));
            }
            if(BaseTree.this.isNodeLast(this.node)){
                response.write((CharSequence)"<td class=\"half-line\">");
            }
            else{
                response.write((CharSequence)"<td class=\"line\">");
            }
        }
    }
    public interface ILinkCallback extends IAjaxLink,IClusterable{
    }
}
