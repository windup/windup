package org.apache.log4j.chainsaw;

import org.apache.log4j.chainsaw.EventDetails;
import java.util.Date;
import javax.swing.event.ListSelectionEvent;
import javax.swing.ListSelectionModel;
import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import javax.swing.JTable;
import javax.swing.JEditorPane;
import org.apache.log4j.chainsaw.MyTableModel;
import java.text.MessageFormat;
import org.apache.log4j.Category;
import javax.swing.event.ListSelectionListener;
import javax.swing.JPanel;

class DetailPanel extends JPanel implements ListSelectionListener{
    private static final Category LOG;
    private static final MessageFormat FORMATTER;
    private final MyTableModel mModel;
    private final JEditorPane mDetails;
    static /* synthetic */ Class class$org$apache$log4j$chainsaw$DetailPanel;
    DetailPanel(final JTable aTable,final MyTableModel aModel){
        super();
        this.mModel=aModel;
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createTitledBorder("Details: "));
        (this.mDetails=new JEditorPane()).setEditable(false);
        this.mDetails.setContentType("text/html");
        this.add(new JScrollPane(this.mDetails),"Center");
        final ListSelectionModel rowSM=aTable.getSelectionModel();
        rowSM.addListSelectionListener(this);
    }
    public void valueChanged(final ListSelectionEvent aEvent){
        if(aEvent.getValueIsAdjusting()){
            return;
        }
        final ListSelectionModel lsm=(ListSelectionModel)aEvent.getSource();
        if(lsm.isSelectionEmpty()){
            this.mDetails.setText("Nothing selected");
        }
        else{
            final int selectedRow=lsm.getMinSelectionIndex();
            final EventDetails e=this.mModel.getEventDetails(selectedRow);
            final Object[] args= { new Date(e.getTimeStamp()),e.getPriority(),this.escape(e.getThreadName()),this.escape(e.getNDC()),this.escape(e.getCategoryName()),this.escape(e.getLocationDetails()),this.escape(e.getMessage()),this.escape(getThrowableStrRep(e)) };
            this.mDetails.setText(DetailPanel.FORMATTER.format(args));
            this.mDetails.setCaretPosition(0);
        }
    }
    private static String getThrowableStrRep(final EventDetails aEvent){
        final String[] strs=aEvent.getThrowableStrRep();
        if(strs==null){
            return null;
        }
        final StringBuffer sb=new StringBuffer();
        for(int i=0;i<strs.length;++i){
            sb.append(strs[i]).append("\n");
        }
        return sb.toString();
    }
    private String escape(final String aStr){
        if(aStr==null){
            return null;
        }
        final StringBuffer buf=new StringBuffer();
        for(int i=0;i<aStr.length();++i){
            final char c=aStr.charAt(i);
            switch(c){
                case '<':{
                    buf.append("&lt;");
                    break;
                }
                case '>':{
                    buf.append("&gt;");
                    break;
                }
                case '\"':{
                    buf.append("&quot;");
                    break;
                }
                case '&':{
                    buf.append("&amp;");
                    break;
                }
                default:{
                    buf.append(c);
                    break;
                }
            }
        }
        return buf.toString();
    }
    static /* synthetic */ Class class$(final String x0){
        try{
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x){
            throw new NoClassDefFoundError(x.getMessage());
        }
    }
    static{
        LOG=Category.getInstance((DetailPanel.class$org$apache$log4j$chainsaw$DetailPanel==null)?(DetailPanel.class$org$apache$log4j$chainsaw$DetailPanel=class$("org.apache.log4j.chainsaw.DetailPanel")):DetailPanel.class$org$apache$log4j$chainsaw$DetailPanel);
        FORMATTER=new MessageFormat("<b>Time:</b> <code>{0,time,medium}</code>&nbsp;&nbsp;<b>Priority:</b> <code>{1}</code>&nbsp;&nbsp;<b>Thread:</b> <code>{2}</code>&nbsp;&nbsp;<b>NDC:</b> <code>{3}</code><br><b>Category:</b> <code>{4}</code><br><b>Location:</b> <code>{5}</code><br><b>Message:</b><pre>{6}</pre><b>Throwable:</b><pre>{7}</pre>");
    }
}
