package org.apache.wicket.markup.html.debug;

import org.apache.wicket.markup.html.panel.*;
import org.apache.wicket.markup.html.basic.*;
import java.util.*;
import org.apache.wicket.markup.html.list.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.visit.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.*;

public final class PageView extends Panel{
    public static final MetaDataKey<Long> RENDER_KEY;
    private static final long serialVersionUID=1L;
    public PageView(final String id,final Page page){
        super(id);
        this.add(new Label("info",(page==null)?"[Stateless Page]":page.toString()));
        List<ComponentData> data=null;
        String pageRenderDuration="n/a";
        if(page!=null){
            final Long renderTime=page.getMetaData(PageView.RENDER_KEY);
            if(renderTime!=null){
                pageRenderDuration=renderTime.toString();
            }
            data=(List<ComponentData>)new ArrayList(this.getComponentData(page));
            Collections.sort(data,new Comparator<ComponentData>(){
                public int compare(final ComponentData o1,final ComponentData o2){
                    return o1.path.compareTo(o2.path);
                }
            });
        }
        else{
            data=(List<ComponentData>)Collections.emptyList();
        }
        this.add(new Label("pageRenderDuration",pageRenderDuration));
        this.add(new ListView<ComponentData>("components",data){
            private static final long serialVersionUID=1L;
            protected void populateItem(final ListItem<ComponentData> listItem){
                final ComponentData componentData=listItem.getModelObject();
                listItem.add(new Label("row",Integer.toString(listItem.getIndex()+1)));
                listItem.add(new Label("path",componentData.path));
                listItem.add(new Label("size",Bytes.bytes(componentData.size).toString()));
                listItem.add(new Label("type",componentData.type));
                listItem.add(new Label("model",componentData.value));
                listItem.add(new Label("renderDuration",(componentData.renderDuration!=null)?componentData.renderDuration.toString():"n/a"));
            }
        });
    }
    private List<ComponentData> getComponentData(final Page page){
        final List<ComponentData> data=(List<ComponentData>)new ArrayList();
        page.visitChildren((org.apache.wicket.util.visit.IVisitor<Component,Object>)new IVisitor<Component,Void>(){
            public void component(final Component component,final IVisit<Void> visit){
                if(!component.getPath().startsWith(PageView.this.getPath())){
                    String name=component.getClass().getName();
                    if(name.indexOf("$")>0){
                        name=component.getClass().getSuperclass().getName();
                    }
                    name=Strings.lastPathComponent(name,':');
                    final ComponentData componentData=new ComponentData(component.getPageRelativePath(),name,component.getSizeInBytes());
                    final Long renderDuration=component.getMetaData(PageView.RENDER_KEY);
                    if(renderDuration!=null){
                        componentData.renderDuration=renderDuration;
                    }
                    try{
                        componentData.value=component.getDefaultModelObjectAsString();
                    }
                    catch(Exception e){
                        componentData.value=e.getMessage();
                    }
                    data.add(componentData);
                }
            }
        });
        return data;
    }
    static{
        RENDER_KEY=new MetaDataKey<Long>() {};
    }
    private static class ComponentData implements IClusterable{
        private static final long serialVersionUID=1L;
        public final String path;
        public final String type;
        public String value;
        public final long size;
        private Long renderDuration;
        ComponentData(final String path,final String type,final long size){
            super();
            this.path=path;
            this.type=type;
            this.size=size;
        }
    }
}
