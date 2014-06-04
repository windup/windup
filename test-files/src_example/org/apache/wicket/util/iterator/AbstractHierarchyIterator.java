package org.apache.wicket.util.iterator;

import java.util.*;
import org.apache.wicket.util.collections.*;
import org.apache.wicket.util.lang.*;

public abstract class AbstractHierarchyIterator<N,I extends N> implements Iterator<I>,Iterable<I>{
    private ArrayListStack<LevelIterator<N>> stack;
    private LevelIterator<N> data;
    private boolean traverse;
    private boolean childFirst;
    private boolean skipRemainingSiblings;
    private boolean hasNextWasLast;
    public AbstractHierarchyIterator(final N root){
        super();
        this.stack=(ArrayListStack<LevelIterator<N>>)new ArrayListStack();
        Args.notNull((Object)root,"root");
        if(this.hasChildren(root)){
            this.data=new LevelIterator<N>(root,this.newIterator(root));
        }
    }
    public final void setChildFirst(final boolean childFirst){
        this.childFirst=childFirst;
    }
    protected abstract boolean hasChildren(final N p0);
    protected abstract Iterator<N> newIterator(final N p0);
    public boolean hasNext(){
        if(this.data==null){
            return false;
        }
        if(this.hasNextWasLast){
            return true;
        }
        this.hasNextWasLast=true;
        if(this.skipRemainingSiblings){
            this.skipRemainingSiblings=false;
            return this.moveUp();
        }
        if(!this.childFirst&&this.traverse){
            return this.moveDown(((LevelIterator<Object>)this.data).lastNode)&&(this.hasNextWasLast=true);
        }
        return this.nextNode();
    }
    private boolean moveDown(final N node){
        this.stack.push((Object)this.data);
        this.data=new LevelIterator<N>(node,this.newIterator(node));
        return this.nextNode();
    }
    protected boolean onFilter(final N node){
        return true;
    }
    protected boolean onTraversalFilter(final N node){
        return true;
    }
    private boolean nextNode(){
        while(this.data.hasNext()){
            ((LevelIterator<Object>)this.data).lastNode=this.data.next();
            this.traverse=this.hasChildren(((LevelIterator<Object>)this.data).lastNode);
            if(this.traverse){
                this.traverse=this.onTraversalFilter(((LevelIterator<Object>)this.data).lastNode);
            }
            if(this.childFirst&&this.traverse&&!this.moveDown(((LevelIterator<Object>)this.data).lastNode)){
                return false;
            }
            if(this.onFilter(((LevelIterator<Object>)this.data).lastNode)){
                return true;
            }
            if(!this.childFirst&&this.traverse){
                return this.moveDown(((LevelIterator<Object>)this.data).lastNode)&&this.data!=null&&(this.hasNextWasLast=true);
            }
            if(this.skipRemainingSiblings){
                this.skipRemainingSiblings=false;
                break;
            }
        }
        return this.moveUp();
    }
    private boolean moveUp(){
        if(this.data==null){
            return false;
        }
        if(this.stack.isEmpty()){
            this.data=null;
            return false;
        }
        this.data=(LevelIterator<N>)this.stack.pop();
        if(this.childFirst){
            this.hasNextWasLast=true;
            return this.onFilter(((LevelIterator<Object>)this.data).lastNode)||this.nextNode();
        }
        if(this.data.hasNext()){
            return this.nextNode();
        }
        return this.moveUp();
    }
    public I next(){
        if(this.data==null){
            return null;
        }
        if(!this.hasNextWasLast&&!this.hasNext()){
            return null;
        }
        this.hasNextWasLast=false;
        return (I)((LevelIterator<Object>)this.data).lastNode;
    }
    public void remove(){
        if(this.data==null){
            throw new IllegalStateException("Already reached the end of the iterator.");
        }
        this.data.remove();
    }
    public void skipRemainingSiblings(){
        this.skipRemainingSiblings=true;
        this.traverse=false;
    }
    public void dontGoDeeper(){
        this.traverse=false;
    }
    public final Iterator<I> iterator(){
        return this;
    }
    public String toString(){
        final StringBuilder msg=new StringBuilder(500);
        msg.append("traverse=").append(this.traverse).append("; childFirst=").append(this.childFirst).append("; hasNextWasLast=").append(this.hasNextWasLast).append("\n");
        msg.append("data.node=").append(((LevelIterator<Object>)this.data).node).append("\n").append("data.lastNode=").append(((LevelIterator<Object>)this.data).lastNode).append("\n");
        msg.append("stack.size=").append(this.stack.size());
        return msg.toString();
    }
    private static class LevelIterator<N> implements Iterator<N>{
        private final N node;
        private final Iterator<N> iter;
        private N lastNode;
        public LevelIterator(final N node,final Iterator<N> iter){
            super();
            Args.notNull((Object)iter,"iter");
            this.node=node;
            this.iter=iter;
        }
        public boolean hasNext(){
            return this.iter.hasNext();
        }
        public N next(){
            return this.lastNode=(N)this.iter.next();
        }
        public void remove(){
            this.iter.remove();
        }
        public String toString(){
            final StringBuilder msg=new StringBuilder(500);
            msg.append("node=").append(this.node).append("\n").append("lastNode=").append(this.lastNode).append("\n");
            return msg.toString();
        }
    }
}
