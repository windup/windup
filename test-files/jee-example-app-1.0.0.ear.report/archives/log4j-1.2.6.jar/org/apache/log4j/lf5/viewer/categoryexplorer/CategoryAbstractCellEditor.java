package org.apache.log4j.lf5.viewer.categoryexplorer;

import javax.swing.JTable;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.tree.TreeCellEditor;
import javax.swing.table.TableCellEditor;

public class CategoryAbstractCellEditor implements TableCellEditor,TreeCellEditor{
    protected EventListenerList _listenerList;
    protected Object _value;
    protected ChangeEvent _changeEvent;
    protected int _clickCountToStart;
    static /* synthetic */ Class class$javax$swing$event$CellEditorListener;
    public CategoryAbstractCellEditor(){
        super();
        this._listenerList=new EventListenerList();
        this._changeEvent=null;
        this._clickCountToStart=1;
    }
    public Object getCellEditorValue(){
        return this._value;
    }
    public void setCellEditorValue(final Object value){
        this._value=value;
    }
    public void setClickCountToStart(final int count){
        this._clickCountToStart=count;
    }
    public int getClickCountToStart(){
        return this._clickCountToStart;
    }
    public boolean isCellEditable(final EventObject anEvent){
        return !(anEvent instanceof MouseEvent)||((MouseEvent)anEvent).getClickCount()>=this._clickCountToStart;
    }
    public boolean shouldSelectCell(final EventObject anEvent){
        return this.isCellEditable(anEvent)&&(anEvent==null||((MouseEvent)anEvent).getClickCount()>=this._clickCountToStart);
    }
    public boolean stopCellEditing(){
        this.fireEditingStopped();
        return true;
    }
    public void cancelCellEditing(){
        this.fireEditingCanceled();
    }
    public void addCellEditorListener(final CellEditorListener l){
        this._listenerList.add((CategoryAbstractCellEditor.class$javax$swing$event$CellEditorListener==null)?(CategoryAbstractCellEditor.class$javax$swing$event$CellEditorListener=class$("javax.swing.event.CellEditorListener")):CategoryAbstractCellEditor.class$javax$swing$event$CellEditorListener,l);
    }
    public void removeCellEditorListener(final CellEditorListener l){
        this._listenerList.remove((CategoryAbstractCellEditor.class$javax$swing$event$CellEditorListener==null)?(CategoryAbstractCellEditor.class$javax$swing$event$CellEditorListener=class$("javax.swing.event.CellEditorListener")):CategoryAbstractCellEditor.class$javax$swing$event$CellEditorListener,l);
    }
    public Component getTreeCellEditorComponent(final JTree tree,final Object value,final boolean isSelected,final boolean expanded,final boolean leaf,final int row){
        return null;
    }
    public Component getTableCellEditorComponent(final JTable table,final Object value,final boolean isSelected,final int row,final int column){
        return null;
    }
    protected void fireEditingStopped(){
        final Object[] listeners=this._listenerList.getListenerList();
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==((CategoryAbstractCellEditor.class$javax$swing$event$CellEditorListener==null)?(CategoryAbstractCellEditor.class$javax$swing$event$CellEditorListener=class$("javax.swing.event.CellEditorListener")):CategoryAbstractCellEditor.class$javax$swing$event$CellEditorListener)){
                if(this._changeEvent==null){
                    this._changeEvent=new ChangeEvent(this);
                }
                ((CellEditorListener)listeners[i+1]).editingStopped(this._changeEvent);
            }
        }
    }
    protected void fireEditingCanceled(){
        final Object[] listeners=this._listenerList.getListenerList();
        for(int i=listeners.length-2;i>=0;i-=2){
            if(listeners[i]==((CategoryAbstractCellEditor.class$javax$swing$event$CellEditorListener==null)?(CategoryAbstractCellEditor.class$javax$swing$event$CellEditorListener=class$("javax.swing.event.CellEditorListener")):CategoryAbstractCellEditor.class$javax$swing$event$CellEditorListener)){
                if(this._changeEvent==null){
                    this._changeEvent=new ChangeEvent(this);
                }
                ((CellEditorListener)listeners[i+1]).editingCanceled(this._changeEvent);
            }
        }
    }
    static /* synthetic */ Class class$(final String x0){
        try{
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x){
            throw new NoClassDefFoundError(x.getMessage());
        }
    }
}
