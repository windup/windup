package org.apache.wicket.markup.html.list;

import org.apache.wicket.markup.repeater.*;
import org.apache.wicket.model.*;
import org.apache.wicket.markup.html.link.*;
import org.apache.wicket.*;
import java.util.*;
import org.apache.wicket.util.collections.*;
import org.apache.wicket.markup.*;

public abstract class ListView<T> extends AbstractRepeater{
    private static final long serialVersionUID=1L;
    private int firstIndex;
    private boolean reuseItems;
    private int viewSize;
    public ListView(final String id){
        super(id);
        this.firstIndex=0;
        this.reuseItems=false;
        this.viewSize=Integer.MAX_VALUE;
    }
    public ListView(final String id,final IModel<? extends List<? extends T>> model){
        super(id,model);
        this.firstIndex=0;
        this.reuseItems=false;
        this.viewSize=Integer.MAX_VALUE;
        if(model==null){
            throw new IllegalArgumentException("Null models are not allowed. If you have no model, you may prefer a Loop instead");
        }
    }
    public ListView(final String id,final List<? extends T> list){
        this(id,(IModel)Model.ofList((List<?>)list));
    }
    public final List<? extends T> getList(){
        final List<? extends T> list=(List<? extends T>)this.getDefaultModelObject();
        if(list==null){
            return (List<? extends T>)Collections.emptyList();
        }
        return list;
    }
    public boolean getReuseItems(){
        return this.reuseItems;
    }
    public final int getStartIndex(){
        return this.firstIndex;
    }
    public int getViewSize(){
        int size=this.viewSize;
        final Object modelObject=this.getDefaultModelObject();
        if(modelObject==null){
            return (size==Integer.MAX_VALUE)?0:size;
        }
        final int modelSize=this.getList().size();
        if(this.firstIndex>modelSize){
            return 0;
        }
        if(size==Integer.MAX_VALUE||this.firstIndex+size>modelSize){
            size=modelSize-this.firstIndex;
        }
        if(Integer.MAX_VALUE-size<this.firstIndex){
            throw new IllegalStateException("firstIndex + size must be smaller than Integer.MAX_VALUE");
        }
        return size;
    }
    public final Link<Void> moveDownLink(final String id,final ListItem<T> item){
        return new Link<Void>(id){
            private static final long serialVersionUID=1L;
            public void onClick(){
                final int index=item.getIndex();
                if(index!=-1){
                    this.addStateChange();
                    Collections.swap((List)ListView.this.getList(),index,index+1);
                    ListView.this.removeAll();
                }
            }
            public boolean isEnabled(){
                return item.getIndex()!=ListView.this.getList().size()-1;
            }
        };
    }
    public final Link<Void> moveUpLink(final String id,final ListItem<T> item){
        return new Link<Void>(id){
            private static final long serialVersionUID=1L;
            public void onClick(){
                final int index=item.getIndex();
                if(index!=-1){
                    this.addStateChange();
                    Collections.swap((List)ListView.this.getList(),index,index-1);
                    ListView.this.removeAll();
                }
            }
            public boolean isEnabled(){
                return item.getIndex()!=0;
            }
        };
    }
    public final Link<Void> removeLink(final String id,final ListItem<T> item){
        return new Link<Void>(id){
            private static final long serialVersionUID=1L;
            public void onClick(){
                this.addStateChange();
                item.modelChanging();
                ListView.this.getList().remove(item.getIndex());
                ListView.this.modelChanged();
                ListView.this.removeAll();
            }
        };
    }
    public ListView<T> setList(final List<? extends T> list){
        this.setDefaultModel(Model.ofList((List<?>)list));
        return this;
    }
    public ListView<T> setReuseItems(final boolean reuseItems){
        this.reuseItems=reuseItems;
        return this;
    }
    public ListView<T> setStartIndex(final int startIndex){
        this.firstIndex=startIndex;
        if(this.firstIndex<0){
            this.firstIndex=0;
        }
        else if(this.firstIndex>this.getList().size()){
            this.firstIndex=0;
        }
        return this;
    }
    public ListView<T> setViewSize(final int size){
        this.viewSize=size;
        if(this.viewSize<0){
            this.viewSize=Integer.MAX_VALUE;
        }
        return this;
    }
    protected IModel<T> getListItemModel(final IModel<? extends List<T>> listViewModel,final int index){
        return new ListItemModel<T>(this,index);
    }
    protected ListItem<T> newItem(final int index,final IModel<T> itemModel){
        return new ListItem<T>(index,itemModel);
    }
    protected final void onPopulate(){
        final int size=this.getViewSize();
        if(size>0){
            if(this.getReuseItems()){
                final int maxIndex=this.firstIndex+size;
                final Iterator<Component> iterator=this.iterator();
                while(iterator.hasNext()){
                    final ListItem<?> child=(ListItem<?>)iterator.next();
                    if(child!=null){
                        final int index=child.getIndex();
                        if(index>=this.firstIndex&&index<maxIndex){
                            continue;
                        }
                        iterator.remove();
                    }
                }
            }
            else{
                this.removeAll();
            }
            final boolean hasChildren=this.size()!=0;
            for(int i=0;i<size;++i){
                final int index2=this.firstIndex+i;
                ListItem<T> item=null;
                if(hasChildren){
                    item=(ListItem<T>)this.get(Integer.toString(index2));
                }
                if(item==null){
                    item=this.newItem(index2,this.getListItemModel(this.getModel(),index2));
                    this.add(item);
                    this.onBeginPopulateItem(item);
                    this.populateItem(item);
                }
            }
        }
        else{
            this.removeAll();
        }
    }
    protected void onBeginPopulateItem(final ListItem<T> item){
    }
    protected abstract void populateItem(final ListItem<T> p0);
    protected final void renderChild(final Component child){
        this.renderItem((ListItem<?>)child);
    }
    protected void renderItem(final ListItem<?> item){
        item.render();
    }
    protected Iterator<Component> renderIterator(){
        final int size=this.size();
        return (Iterator<Component>)new ReadOnlyIterator<Component>(){
            private int index=0;
            public boolean hasNext(){
                return this.index<size;
            }
            public Component next(){
                final String id=Integer.toString(ListView.this.firstIndex+this.index);
                ++this.index;
                return ListView.this.get(id);
            }
        };
    }
    public final IModel<? extends List<T>> getModel(){
        return (IModel<? extends List<T>>)this.getDefaultModel();
    }
    public final void setModel(final IModel<? extends List<T>> model){
        this.setDefaultModel(model);
    }
    public final List<T> getModelObject(){
        return (List<T>)this.getDefaultModelObject();
    }
    public final void setModelObject(final List<T> object){
        this.setDefaultModelObject(object);
    }
    public IMarkupFragment getMarkup(final Component child){
        return this.getMarkup();
    }
}
