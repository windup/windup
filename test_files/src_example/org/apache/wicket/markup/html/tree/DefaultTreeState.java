package org.apache.wicket.markup.html.tree;

import org.apache.wicket.*;
import org.apache.wicket.model.*;
import java.util.*;

public class DefaultTreeState implements ITreeState,IClusterable,IDetachable{
    private static final long serialVersionUID=1L;
    private boolean allowSelectMultiple;
    private final List<ITreeStateListener> listeners;
    private final Set<Object> nodes;
    private boolean nodesCollapsed;
    private final Set<Object> selectedNodes;
    public DefaultTreeState(){
        super();
        this.allowSelectMultiple=false;
        this.listeners=(List<ITreeStateListener>)new ArrayList(1);
        this.nodes=(Set<Object>)new HashSet();
        this.nodesCollapsed=false;
        this.selectedNodes=(Set<Object>)new HashSet();
    }
    public void addTreeStateListener(final ITreeStateListener l){
        if(!this.listeners.contains(l)){
            this.listeners.add(l);
        }
    }
    public void collapseAll(){
        if(!this.nodes.isEmpty()||this.nodesCollapsed){
            this.nodes.clear();
            this.nodesCollapsed=false;
            for(final ITreeStateListener listener : this.listeners){
                listener.allNodesCollapsed();
            }
        }
    }
    public void collapseNode(final Object node){
        if(this.nodesCollapsed){
            this.nodes.add(node);
        }
        else{
            this.nodes.remove(node);
        }
        for(final ITreeStateListener listener : this.listeners){
            listener.nodeCollapsed(node);
        }
    }
    public void expandAll(){
        if(!this.nodes.isEmpty()||!this.nodesCollapsed){
            this.nodes.clear();
            this.nodesCollapsed=true;
            for(final ITreeStateListener listener : this.listeners){
                listener.allNodesExpanded();
            }
        }
    }
    public void expandNode(final Object node){
        if(!this.nodesCollapsed){
            this.nodes.add(node);
        }
        else{
            this.nodes.remove(node);
        }
        for(final ITreeStateListener listener : this.listeners){
            listener.nodeExpanded(node);
        }
    }
    public Collection<Object> getSelectedNodes(){
        return (Collection<Object>)Collections.unmodifiableList(new ArrayList(this.selectedNodes));
    }
    protected void removeSelectedNodeSilent(final Object node){
        this.selectedNodes.remove(node);
    }
    public boolean isAllowSelectMultiple(){
        return this.allowSelectMultiple;
    }
    public boolean isNodeExpanded(final Object node){
        if(!this.nodesCollapsed){
            return this.nodes.contains(node);
        }
        return !this.nodes.contains(node);
    }
    public boolean isNodeSelected(final Object node){
        return this.selectedNodes.contains(node);
    }
    public void removeTreeStateListener(final ITreeStateListener l){
        this.listeners.remove(l);
    }
    private void deselectNode(final Object node){
        if(this.selectedNodes.remove(node)){
            for(final ITreeStateListener listener : (ITreeStateListener[])this.listeners.toArray(new ITreeStateListener[this.listeners.size()])){
                listener.nodeUnselected(node);
            }
        }
    }
    private void selectNode(final Object node){
        if(this.selectedNodes.size()>0&&!this.isAllowSelectMultiple()){
            for(final Object currentlySelectedNode : this.selectedNodes.toArray()){
                if(!currentlySelectedNode.equals(node)){
                    this.deselectNode(currentlySelectedNode);
                }
            }
        }
        if(!this.selectedNodes.contains(node)){
            this.selectedNodes.add(node);
            for(final ITreeStateListener listener : (ITreeStateListener[])this.listeners.toArray(new ITreeStateListener[this.listeners.size()])){
                listener.nodeSelected(node);
            }
        }
    }
    public void selectNode(final Object node,final boolean selected){
        if(selected){
            this.selectNode(node);
        }
        else{
            this.deselectNode(node);
        }
    }
    public void setAllowSelectMultiple(final boolean value){
        this.allowSelectMultiple=value;
    }
    public void detach(){
        for(final Object node : this.nodes){
            if(node instanceof IDetachable){
                ((IDetachable)node).detach();
            }
        }
        for(final Object node : this.selectedNodes){
            if(node instanceof IDetachable){
                ((IDetachable)node).detach();
            }
        }
    }
}
