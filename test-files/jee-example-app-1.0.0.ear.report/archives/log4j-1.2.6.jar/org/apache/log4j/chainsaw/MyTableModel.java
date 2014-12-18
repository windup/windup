package org.apache.log4j.chainsaw;

import java.util.Iterator;
import java.util.Date;
import java.util.ArrayList;
import java.util.TreeSet;
import org.apache.log4j.Priority;
import java.util.List;
import java.util.SortedSet;
import java.text.DateFormat;
import org.apache.log4j.chainsaw.EventDetails;
import java.util.Comparator;
import org.apache.log4j.Category;
import javax.swing.table.AbstractTableModel;

class MyTableModel extends AbstractTableModel{
    private static final Category LOG;
    private static final Comparator MY_COMP;
    private static final String[] COL_NAMES;
    private static final EventDetails[] EMPTY_LIST;
    private static final DateFormat DATE_FORMATTER;
    private final Object mLock;
    private final SortedSet mAllEvents;
    private EventDetails[] mFilteredEvents;
    private final List mPendingEvents;
    private boolean mPaused;
    private String mThreadFilter;
    private String mMessageFilter;
    private String mNDCFilter;
    private String mCategoryFilter;
    private Priority mPriorityFilter;
    static /* synthetic */ Class class$org$apache$log4j$chainsaw$MyTableModel;
    static /* synthetic */ Class class$java$lang$Boolean;
    static /* synthetic */ Class class$java$lang$Object;
    MyTableModel(){
        super();
        this.mLock=new Object();
        this.mAllEvents=new TreeSet(MyTableModel.MY_COMP);
        this.mFilteredEvents=MyTableModel.EMPTY_LIST;
        this.mPendingEvents=new ArrayList();
        this.mPaused=false;
        this.mThreadFilter="";
        this.mMessageFilter="";
        this.mNDCFilter="";
        this.mCategoryFilter="";
        this.mPriorityFilter=Priority.DEBUG;
        final Thread t=new Thread(new Processor());
        t.setDaemon(true);
        t.start();
    }
    public int getRowCount(){
        synchronized(this.mLock){
            return this.mFilteredEvents.length;
        }
    }
    public int getColumnCount(){
        return MyTableModel.COL_NAMES.length;
    }
    public String getColumnName(final int aCol){
        return MyTableModel.COL_NAMES[aCol];
    }
    public Class getColumnClass(final int aCol){
        return (aCol==2)?((MyTableModel.class$java$lang$Boolean==null)?(MyTableModel.class$java$lang$Boolean=class$("java.lang.Boolean")):MyTableModel.class$java$lang$Boolean):((MyTableModel.class$java$lang$Object==null)?(MyTableModel.class$java$lang$Object=class$("java.lang.Object")):MyTableModel.class$java$lang$Object);
    }
    public Object getValueAt(final int aRow,final int aCol){
        synchronized(this.mLock){
            final EventDetails event=this.mFilteredEvents[aRow];
            if(aCol==0){
                return MyTableModel.DATE_FORMATTER.format(new Date(event.getTimeStamp()));
            }
            if(aCol==1){
                return event.getPriority();
            }
            if(aCol==2){
                return (event.getThrowableStrRep()==null)?Boolean.FALSE:Boolean.TRUE;
            }
            if(aCol==3){
                return event.getCategoryName();
            }
            if(aCol==4){
                return event.getNDC();
            }
            return event.getMessage();
        }
    }
    public void setPriorityFilter(final Priority aPriority){
        synchronized(this.mLock){
            this.mPriorityFilter=aPriority;
            this.updateFilteredEvents(false);
        }
    }
    public void setThreadFilter(final String aStr){
        synchronized(this.mLock){
            this.mThreadFilter=aStr.trim();
            this.updateFilteredEvents(false);
        }
    }
    public void setMessageFilter(final String aStr){
        synchronized(this.mLock){
            this.mMessageFilter=aStr.trim();
            this.updateFilteredEvents(false);
        }
    }
    public void setNDCFilter(final String aStr){
        synchronized(this.mLock){
            this.mNDCFilter=aStr.trim();
            this.updateFilteredEvents(false);
        }
    }
    public void setCategoryFilter(final String aStr){
        synchronized(this.mLock){
            this.mCategoryFilter=aStr.trim();
            this.updateFilteredEvents(false);
        }
    }
    public void addEvent(final EventDetails aEvent){
        synchronized(this.mLock){
            this.mPendingEvents.add(aEvent);
        }
    }
    public void clear(){
        synchronized(this.mLock){
            this.mAllEvents.clear();
            this.mFilteredEvents=new EventDetails[0];
            this.mPendingEvents.clear();
            this.fireTableDataChanged();
        }
    }
    public void toggle(){
        synchronized(this.mLock){
            this.mPaused=!this.mPaused;
        }
    }
    public boolean isPaused(){
        synchronized(this.mLock){
            return this.mPaused;
        }
    }
    public EventDetails getEventDetails(final int aRow){
        synchronized(this.mLock){
            return this.mFilteredEvents[aRow];
        }
    }
    private void updateFilteredEvents(final boolean aInsertedToFront){
        final long start=System.currentTimeMillis();
        final List filtered=new ArrayList();
        final int size=this.mAllEvents.size();
        for(final EventDetails event : this.mAllEvents){
            if(this.matchFilter(event)){
                filtered.add(event);
            }
        }
        final EventDetails lastFirst=(this.mFilteredEvents.length==0)?null:this.mFilteredEvents[0];
        this.mFilteredEvents=filtered.toArray(MyTableModel.EMPTY_LIST);
        if(aInsertedToFront&&lastFirst!=null){
            final int index=filtered.indexOf(lastFirst);
            if(index<1){
                MyTableModel.LOG.warn("In strange state");
                this.fireTableDataChanged();
            }
            else{
                this.fireTableRowsInserted(0,index-1);
            }
        }
        else{
            this.fireTableDataChanged();
        }
        final long end=System.currentTimeMillis();
        MyTableModel.LOG.debug("Total time [ms]: "+(end-start)+" in update, size: "+size);
    }
    private boolean matchFilter(final EventDetails aEvent){
        if(!aEvent.getPriority().isGreaterOrEqual(this.mPriorityFilter)||aEvent.getThreadName().indexOf(this.mThreadFilter)<0||aEvent.getCategoryName().indexOf(this.mCategoryFilter)<0||(this.mNDCFilter.length()!=0&&(aEvent.getNDC()==null||aEvent.getNDC().indexOf(this.mNDCFilter)<0))){
            return false;
        }
        final String rm=aEvent.getMessage();
        if(rm==null){
            return this.mMessageFilter.length()==0;
        }
        return rm.indexOf(this.mMessageFilter)>=0;
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
        LOG=Category.getInstance((MyTableModel.class$org$apache$log4j$chainsaw$MyTableModel==null)?(MyTableModel.class$org$apache$log4j$chainsaw$MyTableModel=class$("org.apache.log4j.chainsaw.MyTableModel")):MyTableModel.class$org$apache$log4j$chainsaw$MyTableModel);
        MY_COMP=new Comparator(){
            public int compare(final Object aObj1,final Object aObj2){
                if(aObj1==null&&aObj2==null){
                    return 0;
                }
                if(aObj1==null){
                    return -1;
                }
                if(aObj2==null){
                    return 1;
                }
                final EventDetails le1=(EventDetails)aObj1;
                final EventDetails le2=(EventDetails)aObj2;
                if(le1.getTimeStamp()<le2.getTimeStamp()){
                    return 1;
                }
                return -1;
            }
        };
        COL_NAMES=new String[] { "Time","Priority","Trace","Category","NDC","Message" };
        EMPTY_LIST=new EventDetails[0];
        DATE_FORMATTER=DateFormat.getDateTimeInstance(3,2);
    }
    private class Processor implements Runnable{
        public void run(){
            while(true){
                try{
                    Thread.sleep(1000L);
                }
                catch(InterruptedException ex){
                }
                synchronized(MyTableModel.this.mLock){
                    if(MyTableModel.this.mPaused){
                        continue;
                    }
                    boolean toHead=true;
                    boolean needUpdate=false;
                    for(final EventDetails event : MyTableModel.this.mPendingEvents){
                        MyTableModel.this.mAllEvents.add(event);
                        toHead=(toHead&&event==MyTableModel.this.mAllEvents.first());
                        needUpdate=(needUpdate||MyTableModel.this.matchFilter(event));
                    }
                    MyTableModel.this.mPendingEvents.clear();
                    if(!needUpdate){
                        continue;
                    }
                    MyTableModel.this.updateFilteredEvents(toHead);
                }
            }
        }
    }
}
