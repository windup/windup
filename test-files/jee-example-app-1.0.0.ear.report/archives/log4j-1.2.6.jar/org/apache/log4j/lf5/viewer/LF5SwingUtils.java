package org.apache.log4j.lf5.viewer;

import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import java.awt.event.AdjustmentListener;
import org.apache.log4j.lf5.viewer.TrackingAdjustmentListener;
import javax.swing.JComponent;
import java.awt.Adjustable;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class LF5SwingUtils{
    public static void selectRow(final int row,final JTable table,final JScrollPane pane){
        if(table==null||pane==null){
            return;
        }
        if(!contains(row,table.getModel())){
            return;
        }
        moveAdjustable(row*table.getRowHeight(),pane.getVerticalScrollBar());
        selectRow(row,table.getSelectionModel());
        repaintLater(table);
    }
    public static void makeScrollBarTrack(final Adjustable scrollBar){
        if(scrollBar==null){
            return;
        }
        scrollBar.addAdjustmentListener(new TrackingAdjustmentListener());
    }
    public static void makeVerticalScrollBarTrack(final JScrollPane pane){
        if(pane==null){
            return;
        }
        makeScrollBarTrack(pane.getVerticalScrollBar());
    }
    protected static boolean contains(final int row,final TableModel model){
        return model!=null&&row>=0&&row<model.getRowCount();
    }
    protected static void selectRow(final int row,final ListSelectionModel model){
        if(model==null){
            return;
        }
        model.setSelectionInterval(row,row);
    }
    protected static void moveAdjustable(final int location,final Adjustable scrollBar){
        if(scrollBar==null){
            return;
        }
        scrollBar.setValue(location);
    }
    protected static void repaintLater(final JComponent component){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                component.repaint();
            }
        });
    }
}
