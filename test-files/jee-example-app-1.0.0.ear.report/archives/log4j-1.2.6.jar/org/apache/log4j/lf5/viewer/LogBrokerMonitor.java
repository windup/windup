package org.apache.log4j.lf5.viewer;

import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.lf5.util.LogFileParser;
import java.util.StringTokenizer;
import java.net.MalformedURLException;
import javax.swing.JFileChooser;
import java.awt.GraphicsEnvironment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.apache.log4j.lf5.viewer.LogFactor5ErrorDialog;
import org.apache.log4j.lf5.viewer.LogFactor5InputDialog;
import javax.swing.KeyStroke;
import java.util.ArrayList;
import java.awt.Color;
import javax.swing.JColorChooser;
import java.util.Iterator;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import java.awt.LayoutManager;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.apache.log4j.lf5.viewer.FilteredLogTableModel;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryPath;
import org.apache.log4j.lf5.LogRecordFilter;
import java.net.URL;
import javax.swing.JSplitPane;
import java.awt.event.AdjustmentListener;
import org.apache.log4j.lf5.viewer.TrackingAdjustmentListener;
import javax.swing.ImageIcon;
import java.awt.Font;
import javax.swing.JTextArea;
import javax.swing.JTable;
import org.apache.log4j.lf5.viewer.LF5SwingUtils;
import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.JCheckBoxMenuItem;
import java.awt.Toolkit;
import org.apache.log4j.lf5.LogRecord;
import org.apache.log4j.lf5.util.DateFormatManager;
import javax.swing.SwingUtilities;
import java.awt.event.WindowListener;
import org.apache.log4j.lf5.viewer.LogTableColumn;
import java.util.HashMap;
import java.util.Vector;
import java.io.File;
import org.apache.log4j.lf5.viewer.configure.MRUFileManager;
import org.apache.log4j.lf5.viewer.configure.ConfigurationManager;
import java.util.Map;
import java.util.List;
import java.awt.Dimension;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryExplorerTree;
import org.apache.log4j.lf5.viewer.LogTable;
import javax.swing.JFrame;

public class LogBrokerMonitor{
    public static final String DETAILED_VIEW="Detailed";
    protected JFrame _logMonitorFrame;
    protected int _logMonitorFrameWidth;
    protected int _logMonitorFrameHeight;
    protected LogTable _table;
    protected CategoryExplorerTree _categoryExplorerTree;
    protected String _searchText;
    protected String _NDCTextFilter;
    protected LogLevel _leastSevereDisplayedLogLevel;
    protected JScrollPane _logTableScrollPane;
    protected JLabel _statusLabel;
    protected Object _lock;
    protected JComboBox _fontSizeCombo;
    protected int _fontSize;
    protected String _fontName;
    protected String _currentView;
    protected boolean _loadSystemFonts;
    protected boolean _trackTableScrollPane;
    protected Dimension _lastTableViewportSize;
    protected boolean _callSystemExitOnClose;
    protected List _displayedLogBrokerProperties;
    protected Map _logLevelMenuItems;
    protected Map _logTableColumnMenuItems;
    protected List _levels;
    protected List _columns;
    protected boolean _isDisposed;
    protected ConfigurationManager _configurationManager;
    protected MRUFileManager _mruFileManager;
    protected File _fileLocation;
    public LogBrokerMonitor(final List logLevels){
        super();
        this._logMonitorFrameWidth=550;
        this._logMonitorFrameHeight=500;
        this._NDCTextFilter="";
        this._leastSevereDisplayedLogLevel=LogLevel.DEBUG;
        this._lock=new Object();
        this._fontSize=10;
        this._fontName="Dialog";
        this._currentView="Detailed";
        this._loadSystemFonts=false;
        this._trackTableScrollPane=true;
        this._callSystemExitOnClose=false;
        this._displayedLogBrokerProperties=new Vector();
        this._logLevelMenuItems=new HashMap();
        this._logTableColumnMenuItems=new HashMap();
        this._levels=null;
        this._columns=null;
        this._isDisposed=false;
        this._configurationManager=null;
        this._mruFileManager=null;
        this._fileLocation=null;
        this._levels=logLevels;
        this._columns=LogTableColumn.getLogTableColumns();
        String callSystemExitOnClose=System.getProperty("monitor.exit");
        if(callSystemExitOnClose==null){
            callSystemExitOnClose="false";
        }
        callSystemExitOnClose=callSystemExitOnClose.trim().toLowerCase();
        if(callSystemExitOnClose.equals("true")){
            this._callSystemExitOnClose=true;
        }
        this.initComponents();
        this._logMonitorFrame.addWindowListener(new LogBrokerMonitorWindowAdaptor(this));
    }
    public void show(final int delay){
        if(this._logMonitorFrame.isVisible()){
            return;
        }
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                Thread.yield();
                LogBrokerMonitor.this.pause(delay);
                LogBrokerMonitor.this._logMonitorFrame.setVisible(true);
            }
        });
    }
    public void show(){
        this.show(0);
    }
    public void dispose(){
        this._logMonitorFrame.dispose();
        this._isDisposed=true;
        if(this._callSystemExitOnClose){
            System.exit(0);
        }
    }
    public void hide(){
        this._logMonitorFrame.setVisible(false);
    }
    public DateFormatManager getDateFormatManager(){
        return this._table.getDateFormatManager();
    }
    public void setDateFormatManager(final DateFormatManager dfm){
        this._table.setDateFormatManager(dfm);
    }
    public boolean getCallSystemExitOnClose(){
        return this._callSystemExitOnClose;
    }
    public void setCallSystemExitOnClose(final boolean callSystemExitOnClose){
        this._callSystemExitOnClose=callSystemExitOnClose;
    }
    public void addMessage(final LogRecord lr){
        if(this._isDisposed){
            return;
        }
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                LogBrokerMonitor.this._categoryExplorerTree.getExplorerModel().addLogRecord(lr);
                LogBrokerMonitor.this._table.getFilteredLogTableModel().addLogRecord(lr);
                LogBrokerMonitor.this.updateStatusLabel();
            }
        });
    }
    public void setMaxNumberOfLogRecords(final int maxNumberOfLogRecords){
        this._table.getFilteredLogTableModel().setMaxNumberOfLogRecords(maxNumberOfLogRecords);
    }
    public JFrame getBaseFrame(){
        return this._logMonitorFrame;
    }
    public void setTitle(final String title){
        this._logMonitorFrame.setTitle(title+" - LogFactor5");
    }
    public void setFrameSize(final int width,final int height){
        final Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
        if(0<width&&width<screen.width){
            this._logMonitorFrameWidth=width;
        }
        if(0<height&&height<screen.height){
            this._logMonitorFrameHeight=height;
        }
        this.updateFrameSize();
    }
    public void setFontSize(final int fontSize){
        this.changeFontSizeCombo(this._fontSizeCombo,fontSize);
    }
    public void addDisplayedProperty(final Object messageLine){
        this._displayedLogBrokerProperties.add(messageLine);
    }
    public Map getLogLevelMenuItems(){
        return this._logLevelMenuItems;
    }
    public Map getLogTableColumnMenuItems(){
        return this._logTableColumnMenuItems;
    }
    public JCheckBoxMenuItem getTableColumnMenuItem(final LogTableColumn column){
        return this.getLogTableColumnMenuItem(column);
    }
    public CategoryExplorerTree getCategoryExplorerTree(){
        return this._categoryExplorerTree;
    }
    public String getNDCTextFilter(){
        return this._NDCTextFilter;
    }
    public void setNDCLogRecordFilter(final String textFilter){
        this._table.getFilteredLogTableModel().setLogRecordFilter(this.createNDCLogRecordFilter(textFilter));
    }
    protected void setSearchText(final String text){
        this._searchText=text;
    }
    protected void setNDCTextFilter(final String text){
        if(text==null){
            this._NDCTextFilter="";
        }
        else{
            this._NDCTextFilter=text;
        }
    }
    protected void sortByNDC(){
        final String text=this._NDCTextFilter;
        if(text==null||text.length()==0){
            return;
        }
        this._table.getFilteredLogTableModel().setLogRecordFilter(this.createNDCLogRecordFilter(text));
    }
    protected void findSearchText(){
        final String text=this._searchText;
        if(text==null||text.length()==0){
            return;
        }
        final int startRow=this.getFirstSelectedRow();
        final int foundRow=this.findRecord(startRow,text,this._table.getFilteredLogTableModel().getFilteredRecords());
        this.selectRow(foundRow);
    }
    protected int getFirstSelectedRow(){
        return this._table.getSelectionModel().getMinSelectionIndex();
    }
    protected void selectRow(final int foundRow){
        if(foundRow==-1){
            final String message=this._searchText+" not found.";
            JOptionPane.showMessageDialog(this._logMonitorFrame,message,"Text not found",1);
            return;
        }
        LF5SwingUtils.selectRow(foundRow,this._table,this._logTableScrollPane);
    }
    protected int findRecord(int startRow,final String searchText,final List records){
        if(startRow<0){
            startRow=0;
        }
        else{
            ++startRow;
        }
        for(int len=records.size(),i=startRow;i<len;++i){
            if(this.matches(records.get(i),searchText)){
                return i;
            }
        }
        for(int len=startRow,j=0;j<len;++j){
            if(this.matches(records.get(j),searchText)){
                return j;
            }
        }
        return -1;
    }
    protected boolean matches(final LogRecord record,final String text){
        final String message=record.getMessage();
        final String NDC=record.getNDC();
        return (message!=null||NDC!=null)&&text!=null&&(message.toLowerCase().indexOf(text.toLowerCase())!=-1||NDC.toLowerCase().indexOf(text.toLowerCase())!=-1);
    }
    protected void refresh(final JTextArea textArea){
        final String text=textArea.getText();
        textArea.setText("");
        textArea.setText(text);
    }
    protected void refreshDetailTextArea(){
        this.refresh(this._table._detailTextArea);
    }
    protected void clearDetailTextArea(){
        this._table._detailTextArea.setText("");
    }
    protected int changeFontSizeCombo(final JComboBox box,final int requestedSize){
        final int len=box.getItemCount();
        Object selectedObject=box.getItemAt(0);
        int selectedValue=Integer.parseInt(String.valueOf(selectedObject));
        for(int i=0;i<len;++i){
            final Object currentObject=box.getItemAt(i);
            final int currentValue=Integer.parseInt(String.valueOf(currentObject));
            if(selectedValue<currentValue&&currentValue<=requestedSize){
                selectedValue=currentValue;
                selectedObject=currentObject;
            }
        }
        box.setSelectedItem(selectedObject);
        return selectedValue;
    }
    protected void setFontSizeSilently(final int fontSize){
        this._fontSize=fontSize;
        this.setFontSize(this._table._detailTextArea,fontSize);
        this.selectRow(0);
        this.setFontSize(this._table,fontSize);
    }
    protected void setFontSize(final Component component,final int fontSize){
        final Font oldFont=component.getFont();
        final Font newFont=new Font(oldFont.getFontName(),oldFont.getStyle(),fontSize);
        component.setFont(newFont);
    }
    protected void updateFrameSize(){
        this._logMonitorFrame.setSize(this._logMonitorFrameWidth,this._logMonitorFrameHeight);
        this.centerFrame(this._logMonitorFrame);
    }
    protected void pause(final int millis){
        try{
            Thread.sleep(millis);
        }
        catch(InterruptedException ex){
        }
    }
    protected void initComponents(){
        (this._logMonitorFrame=new JFrame("LogFactor5")).setDefaultCloseOperation(0);
        final String resource="/org/apache/log4j/lf5/viewer/images/lf5_small_icon.gif";
        final URL lf5IconURL=this.getClass().getResource(resource);
        if(lf5IconURL!=null){
            this._logMonitorFrame.setIconImage(new ImageIcon(lf5IconURL).getImage());
        }
        this.updateFrameSize();
        final JTextArea detailTA=this.createDetailTextArea();
        final JScrollPane detailTAScrollPane=new JScrollPane(detailTA);
        this._table=new LogTable(detailTA);
        this.setView(this._currentView,this._table);
        this._table.setFont(new Font(this._fontName,0,this._fontSize));
        this._logTableScrollPane=new JScrollPane(this._table);
        if(this._trackTableScrollPane){
            this._logTableScrollPane.getVerticalScrollBar().addAdjustmentListener(new TrackingAdjustmentListener());
        }
        final JSplitPane tableViewerSplitPane=new JSplitPane();
        tableViewerSplitPane.setOneTouchExpandable(true);
        tableViewerSplitPane.setOrientation(0);
        tableViewerSplitPane.setLeftComponent(this._logTableScrollPane);
        tableViewerSplitPane.setRightComponent(detailTAScrollPane);
        tableViewerSplitPane.setDividerLocation(350);
        this._categoryExplorerTree=new CategoryExplorerTree();
        this._table.getFilteredLogTableModel().setLogRecordFilter(this.createLogRecordFilter());
        final JScrollPane categoryExplorerTreeScrollPane=new JScrollPane(this._categoryExplorerTree);
        categoryExplorerTreeScrollPane.setPreferredSize(new Dimension(130,400));
        this._mruFileManager=new MRUFileManager();
        final JSplitPane splitPane=new JSplitPane();
        splitPane.setOneTouchExpandable(true);
        splitPane.setRightComponent(tableViewerSplitPane);
        splitPane.setLeftComponent(categoryExplorerTreeScrollPane);
        splitPane.setDividerLocation(130);
        this._logMonitorFrame.getRootPane().setJMenuBar(this.createMenuBar());
        this._logMonitorFrame.getContentPane().add(splitPane,"Center");
        this._logMonitorFrame.getContentPane().add(this.createToolBar(),"North");
        this._logMonitorFrame.getContentPane().add(this.createStatusArea(),"South");
        this.makeLogTableListenToCategoryExplorer();
        this.addTableModelProperties();
        this._configurationManager=new ConfigurationManager(this,this._table);
    }
    protected LogRecordFilter createLogRecordFilter(){
        final LogRecordFilter result=new LogRecordFilter(){
            public boolean passes(final LogRecord record){
                final CategoryPath path=new CategoryPath(record.getCategory());
                return LogBrokerMonitor.this.getMenuItem(record.getLevel()).isSelected()&&LogBrokerMonitor.this._categoryExplorerTree.getExplorerModel().isCategoryPathActive(path);
            }
        };
        return result;
    }
    protected LogRecordFilter createNDCLogRecordFilter(final String text){
        this._NDCTextFilter=text;
        final LogRecordFilter result=new LogRecordFilter(){
            public boolean passes(final LogRecord record){
                final String NDC=record.getNDC();
                final CategoryPath path=new CategoryPath(record.getCategory());
                return NDC!=null&&LogBrokerMonitor.this._NDCTextFilter!=null&&NDC.toLowerCase().indexOf(LogBrokerMonitor.this._NDCTextFilter.toLowerCase())!=-1&&LogBrokerMonitor.this.getMenuItem(record.getLevel()).isSelected()&&LogBrokerMonitor.this._categoryExplorerTree.getExplorerModel().isCategoryPathActive(path);
            }
        };
        return result;
    }
    protected void updateStatusLabel(){
        this._statusLabel.setText(this.getRecordsDisplayedMessage());
    }
    protected String getRecordsDisplayedMessage(){
        final FilteredLogTableModel model=this._table.getFilteredLogTableModel();
        return this.getStatusText(model.getRowCount(),model.getTotalRowCount());
    }
    protected void addTableModelProperties(){
        final FilteredLogTableModel model=this._table.getFilteredLogTableModel();
        this.addDisplayedProperty(new Object(){
            public String toString(){
                return LogBrokerMonitor.this.getRecordsDisplayedMessage();
            }
        });
        this.addDisplayedProperty(new Object(){
            public String toString(){
                return "Maximum number of displayed LogRecords: "+model._maxNumberOfLogRecords;
            }
        });
    }
    protected String getStatusText(final int displayedRows,final int totalRows){
        final StringBuffer result=new StringBuffer();
        result.append("Displaying: ");
        result.append(displayedRows);
        result.append(" records out of a total of: ");
        result.append(totalRows);
        result.append(" records.");
        return result.toString();
    }
    protected void makeLogTableListenToCategoryExplorer(){
        final ActionListener listener=new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this._table.getFilteredLogTableModel().refresh();
                LogBrokerMonitor.this.updateStatusLabel();
            }
        };
        this._categoryExplorerTree.getExplorerModel().addActionListener(listener);
    }
    protected JPanel createStatusArea(){
        final JPanel statusArea=new JPanel();
        final JLabel status=new JLabel("No log records to display.");
        (this._statusLabel=status).setHorizontalAlignment(2);
        statusArea.setBorder(BorderFactory.createEtchedBorder());
        statusArea.setLayout(new FlowLayout(0,0,0));
        statusArea.add(status);
        return statusArea;
    }
    protected JTextArea createDetailTextArea(){
        final JTextArea detailTA=new JTextArea();
        detailTA.setFont(new Font("Monospaced",0,14));
        detailTA.setTabSize(3);
        detailTA.setLineWrap(true);
        detailTA.setWrapStyleWord(false);
        return detailTA;
    }
    protected JMenuBar createMenuBar(){
        final JMenuBar menuBar=new JMenuBar();
        menuBar.add(this.createFileMenu());
        menuBar.add(this.createEditMenu());
        menuBar.add(this.createLogLevelMenu());
        menuBar.add(this.createViewMenu());
        menuBar.add(this.createConfigureMenu());
        menuBar.add(this.createHelpMenu());
        return menuBar;
    }
    protected JMenu createLogLevelMenu(){
        final JMenu result=new JMenu("Log Level");
        result.setMnemonic('l');
        final Iterator levels=this.getLogLevels();
        while(levels.hasNext()){
            result.add(this.getMenuItem(levels.next()));
        }
        result.addSeparator();
        result.add(this.createAllLogLevelsMenuItem());
        result.add(this.createNoLogLevelsMenuItem());
        result.addSeparator();
        result.add(this.createLogLevelColorMenu());
        result.add(this.createResetLogLevelColorMenuItem());
        return result;
    }
    protected JMenuItem createAllLogLevelsMenuItem(){
        final JMenuItem result=new JMenuItem("Show all LogLevels");
        result.setMnemonic('s');
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.selectAllLogLevels(true);
                LogBrokerMonitor.this._table.getFilteredLogTableModel().refresh();
                LogBrokerMonitor.this.updateStatusLabel();
            }
        });
        return result;
    }
    protected JMenuItem createNoLogLevelsMenuItem(){
        final JMenuItem result=new JMenuItem("Hide all LogLevels");
        result.setMnemonic('h');
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.selectAllLogLevels(false);
                LogBrokerMonitor.this._table.getFilteredLogTableModel().refresh();
                LogBrokerMonitor.this.updateStatusLabel();
            }
        });
        return result;
    }
    protected JMenu createLogLevelColorMenu(){
        final JMenu colorMenu=new JMenu("Configure LogLevel Colors");
        colorMenu.setMnemonic('c');
        final Iterator levels=this.getLogLevels();
        while(levels.hasNext()){
            colorMenu.add(this.createSubMenuItem(levels.next()));
        }
        return colorMenu;
    }
    protected JMenuItem createResetLogLevelColorMenuItem(){
        final JMenuItem result=new JMenuItem("Reset LogLevel Colors");
        result.setMnemonic('r');
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogLevel.resetLogLevelColorMap();
                LogBrokerMonitor.this._table.getFilteredLogTableModel().refresh();
            }
        });
        return result;
    }
    protected void selectAllLogLevels(final boolean selected){
        final Iterator levels=this.getLogLevels();
        while(levels.hasNext()){
            this.getMenuItem(levels.next()).setSelected(selected);
        }
    }
    protected JCheckBoxMenuItem getMenuItem(final LogLevel level){
        JCheckBoxMenuItem result=this._logLevelMenuItems.get(level);
        if(result==null){
            result=this.createMenuItem(level);
            this._logLevelMenuItems.put(level,result);
        }
        return result;
    }
    protected JMenuItem createSubMenuItem(final LogLevel level){
        final JMenuItem result=new JMenuItem(level.toString());
        result.setMnemonic(level.toString().charAt(0));
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.showLogLevelColorChangeDialog(result,level);
            }
        });
        return result;
    }
    protected void showLogLevelColorChangeDialog(final JMenuItem result,final LogLevel level){
        final Color newColor=JColorChooser.showDialog(this._logMonitorFrame,"Choose LogLevel Color",result.getForeground());
        if(newColor!=null){
            level.setLogLevelColorMap(level,newColor);
            this._table.getFilteredLogTableModel().refresh();
        }
    }
    protected JCheckBoxMenuItem createMenuItem(final LogLevel level){
        final JCheckBoxMenuItem result=new JCheckBoxMenuItem(level.toString());
        result.setSelected(true);
        result.setMnemonic(level.toString().charAt(0));
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this._table.getFilteredLogTableModel().refresh();
                LogBrokerMonitor.this.updateStatusLabel();
            }
        });
        return result;
    }
    protected JMenu createViewMenu(){
        final JMenu result=new JMenu("View");
        result.setMnemonic('v');
        final Iterator columns=this.getLogTableColumns();
        while(columns.hasNext()){
            result.add(this.getLogTableColumnMenuItem(columns.next()));
        }
        result.addSeparator();
        result.add(this.createAllLogTableColumnsMenuItem());
        result.add(this.createNoLogTableColumnsMenuItem());
        return result;
    }
    protected JCheckBoxMenuItem getLogTableColumnMenuItem(final LogTableColumn column){
        JCheckBoxMenuItem result=this._logTableColumnMenuItems.get(column);
        if(result==null){
            result=this.createLogTableColumnMenuItem(column);
            this._logTableColumnMenuItems.put(column,result);
        }
        return result;
    }
    protected JCheckBoxMenuItem createLogTableColumnMenuItem(final LogTableColumn column){
        final JCheckBoxMenuItem result=new JCheckBoxMenuItem(column.toString());
        result.setSelected(true);
        result.setMnemonic(column.toString().charAt(0));
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                final List selectedColumns=LogBrokerMonitor.this.updateView();
                LogBrokerMonitor.this._table.setView(selectedColumns);
            }
        });
        return result;
    }
    protected List updateView(){
        final ArrayList updatedList=new ArrayList();
        for(final LogTableColumn column : this._columns){
            final JCheckBoxMenuItem result=this.getLogTableColumnMenuItem(column);
            if(result.isSelected()){
                updatedList.add(column);
            }
        }
        return updatedList;
    }
    protected JMenuItem createAllLogTableColumnsMenuItem(){
        final JMenuItem result=new JMenuItem("Show all Columns");
        result.setMnemonic('s');
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.selectAllLogTableColumns(true);
                final List selectedColumns=LogBrokerMonitor.this.updateView();
                LogBrokerMonitor.this._table.setView(selectedColumns);
            }
        });
        return result;
    }
    protected JMenuItem createNoLogTableColumnsMenuItem(){
        final JMenuItem result=new JMenuItem("Hide all Columns");
        result.setMnemonic('h');
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.selectAllLogTableColumns(false);
                final List selectedColumns=LogBrokerMonitor.this.updateView();
                LogBrokerMonitor.this._table.setView(selectedColumns);
            }
        });
        return result;
    }
    protected void selectAllLogTableColumns(final boolean selected){
        final Iterator columns=this.getLogTableColumns();
        while(columns.hasNext()){
            this.getLogTableColumnMenuItem(columns.next()).setSelected(selected);
        }
    }
    protected JMenu createFileMenu(){
        final JMenu fileMenu=new JMenu("File");
        fileMenu.setMnemonic('f');
        fileMenu.add(this.createOpenMI());
        fileMenu.add(this.createOpenURLMI());
        fileMenu.addSeparator();
        fileMenu.add(this.createCloseMI());
        this.createMRUFileListMI(fileMenu);
        fileMenu.addSeparator();
        fileMenu.add(this.createExitMI());
        return fileMenu;
    }
    protected JMenuItem createOpenMI(){
        final JMenuItem result=new JMenuItem("Open...");
        result.setMnemonic('o');
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.requestOpen();
            }
        });
        return result;
    }
    protected JMenuItem createOpenURLMI(){
        final JMenuItem result=new JMenuItem("Open URL...");
        result.setMnemonic('u');
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.requestOpenURL();
            }
        });
        return result;
    }
    protected JMenuItem createCloseMI(){
        final JMenuItem result=new JMenuItem("Close");
        result.setMnemonic('c');
        result.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.requestClose();
            }
        });
        return result;
    }
    protected void createMRUFileListMI(final JMenu menu){
        final String[] files=this._mruFileManager.getMRUFileList();
        if(files!=null){
            menu.addSeparator();
            for(int i=0;i<files.length;++i){
                final JMenuItem result=new JMenuItem(i+1+" "+files[i]);
                result.setMnemonic(i+1);
                result.addActionListener(new ActionListener(){
                    public void actionPerformed(final ActionEvent e){
                        LogBrokerMonitor.this.requestOpenMRU(e);
                    }
                });
                menu.add(result);
            }
        }
    }
    protected JMenuItem createExitMI(){
        final JMenuItem result=new JMenuItem("Exit");
        result.setMnemonic('x');
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.requestExit();
            }
        });
        return result;
    }
    protected JMenu createConfigureMenu(){
        final JMenu configureMenu=new JMenu("Configure");
        configureMenu.setMnemonic('c');
        configureMenu.add(this.createConfigureSave());
        configureMenu.add(this.createConfigureReset());
        configureMenu.add(this.createConfigureMaxRecords());
        return configureMenu;
    }
    protected JMenuItem createConfigureSave(){
        final JMenuItem result=new JMenuItem("Save");
        result.setMnemonic('s');
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.saveConfiguration();
            }
        });
        return result;
    }
    protected JMenuItem createConfigureReset(){
        final JMenuItem result=new JMenuItem("Reset");
        result.setMnemonic('r');
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.resetConfiguration();
            }
        });
        return result;
    }
    protected JMenuItem createConfigureMaxRecords(){
        final JMenuItem result=new JMenuItem("Set Max Number of Records");
        result.setMnemonic('m');
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.setMaxRecordConfiguration();
            }
        });
        return result;
    }
    protected void saveConfiguration(){
        this._configurationManager.save();
    }
    protected void resetConfiguration(){
        this._configurationManager.reset();
    }
    protected void setMaxRecordConfiguration(){
        final LogFactor5InputDialog inputDialog=new LogFactor5InputDialog(this.getBaseFrame(),"Set Max Number of Records","",10);
        final String temp=inputDialog.getText();
        if(temp!=null){
            try{
                this.setMaxNumberOfLogRecords(Integer.parseInt(temp));
            }
            catch(NumberFormatException e){
                final LogFactor5ErrorDialog error=new LogFactor5ErrorDialog(this.getBaseFrame(),"'"+temp+"' is an invalid parameter.\nPlease try again.");
                this.setMaxRecordConfiguration();
            }
        }
    }
    protected JMenu createHelpMenu(){
        final JMenu helpMenu=new JMenu("Help");
        helpMenu.setMnemonic('h');
        helpMenu.add(this.createHelpProperties());
        return helpMenu;
    }
    protected JMenuItem createHelpProperties(){
        final String title="LogFactor5 Properties";
        final JMenuItem result=new JMenuItem("LogFactor5 Properties");
        result.setMnemonic('l');
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.showPropertiesDialog("LogFactor5 Properties");
            }
        });
        return result;
    }
    protected void showPropertiesDialog(final String title){
        JOptionPane.showMessageDialog(this._logMonitorFrame,this._displayedLogBrokerProperties.toArray(),title,-1);
    }
    protected JMenu createEditMenu(){
        final JMenu editMenu=new JMenu("Edit");
        editMenu.setMnemonic('e');
        editMenu.add(this.createEditFindMI());
        editMenu.add(this.createEditFindNextMI());
        editMenu.addSeparator();
        editMenu.add(this.createEditSortNDCMI());
        editMenu.add(this.createEditRestoreAllNDCMI());
        return editMenu;
    }
    protected JMenuItem createEditFindNextMI(){
        final JMenuItem editFindNextMI=new JMenuItem("Find Next");
        editFindNextMI.setMnemonic('n');
        editFindNextMI.setAccelerator(KeyStroke.getKeyStroke("F3"));
        editFindNextMI.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this.findSearchText();
            }
        });
        return editFindNextMI;
    }
    protected JMenuItem createEditFindMI(){
        final JMenuItem editFindMI=new JMenuItem("Find");
        editFindMI.setMnemonic('f');
        editFindMI.setAccelerator(KeyStroke.getKeyStroke("control F"));
        editFindMI.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                final String inputValue=JOptionPane.showInputDialog(LogBrokerMonitor.this._logMonitorFrame,"Find text: ","Search Record Messages",3);
                LogBrokerMonitor.this.setSearchText(inputValue);
                LogBrokerMonitor.this.findSearchText();
            }
        });
        return editFindMI;
    }
    protected JMenuItem createEditSortNDCMI(){
        final JMenuItem editSortNDCMI=new JMenuItem("Sort by NDC");
        editSortNDCMI.setMnemonic('s');
        editSortNDCMI.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                final String inputValue=JOptionPane.showInputDialog(LogBrokerMonitor.this._logMonitorFrame,"Sort by this NDC: ","Sort Log Records by NDC",3);
                LogBrokerMonitor.this.setNDCTextFilter(inputValue);
                LogBrokerMonitor.this.sortByNDC();
                LogBrokerMonitor.this._table.getFilteredLogTableModel().refresh();
                LogBrokerMonitor.this.updateStatusLabel();
            }
        });
        return editSortNDCMI;
    }
    protected JMenuItem createEditRestoreAllNDCMI(){
        final JMenuItem editRestoreAllNDCMI=new JMenuItem("Restore all NDCs");
        editRestoreAllNDCMI.setMnemonic('r');
        editRestoreAllNDCMI.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this._table.getFilteredLogTableModel().setLogRecordFilter(LogBrokerMonitor.this.createLogRecordFilter());
                LogBrokerMonitor.this.setNDCTextFilter("");
                LogBrokerMonitor.this._table.getFilteredLogTableModel().refresh();
                LogBrokerMonitor.this.updateStatusLabel();
            }
        });
        return editRestoreAllNDCMI;
    }
    protected JToolBar createToolBar(){
        final JToolBar tb=new JToolBar();
        tb.putClientProperty("JToolBar.isRollover",Boolean.TRUE);
        final JComboBox fontCombo=new JComboBox();
        final JComboBox fontSizeCombo=new JComboBox();
        this._fontSizeCombo=fontSizeCombo;
        ClassLoader cl=this.getClass().getClassLoader();
        if(cl==null){
            cl=ClassLoader.getSystemClassLoader();
        }
        final URL newIconURL=cl.getResource("org/apache/log4j/lf5/viewer/images/channelexplorer_new.gif");
        ImageIcon newIcon=null;
        if(newIconURL!=null){
            newIcon=new ImageIcon(newIconURL);
        }
        final JButton newButton=new JButton("Clear Log Table");
        if(newIcon!=null){
            newButton.setIcon(newIcon);
        }
        newButton.setToolTipText("Clear Log Table.");
        newButton.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogBrokerMonitor.this._table.clearLogRecords();
                LogBrokerMonitor.this._categoryExplorerTree.getExplorerModel().resetAllNodeCounts();
                LogBrokerMonitor.this.updateStatusLabel();
                LogBrokerMonitor.this.clearDetailTextArea();
                LogRecord.resetSequenceNumber();
            }
        });
        final Toolkit tk=Toolkit.getDefaultToolkit();
        String[] fonts;
        if(this._loadSystemFonts){
            fonts=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        }
        else{
            fonts=tk.getFontList();
        }
        for(int j=0;j<fonts.length;++j){
            fontCombo.addItem(fonts[j]);
        }
        fontCombo.setSelectedItem(this._fontName);
        fontCombo.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                final JComboBox box=(JComboBox)e.getSource();
                final String font=(String)box.getSelectedItem();
                LogBrokerMonitor.this._table.setFont(new Font(font,0,LogBrokerMonitor.this._fontSize));
                LogBrokerMonitor.this._fontName=font;
            }
        });
        fontSizeCombo.addItem("8");
        fontSizeCombo.addItem("9");
        fontSizeCombo.addItem("10");
        fontSizeCombo.addItem("12");
        fontSizeCombo.addItem("14");
        fontSizeCombo.addItem("16");
        fontSizeCombo.addItem("18");
        fontSizeCombo.addItem("24");
        fontSizeCombo.setSelectedItem(String.valueOf(this._fontSize));
        fontSizeCombo.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                final JComboBox box=(JComboBox)e.getSource();
                final String size=(String)box.getSelectedItem();
                final int s=Integer.valueOf(size);
                LogBrokerMonitor.this.setFontSizeSilently(s);
                LogBrokerMonitor.this.refreshDetailTextArea();
                LogBrokerMonitor.this._fontSize=s;
            }
        });
        tb.add(new JLabel(" Font: "));
        tb.add(fontCombo);
        tb.add(fontSizeCombo);
        tb.addSeparator();
        tb.addSeparator();
        tb.add(newButton);
        newButton.setAlignmentY(0.5f);
        newButton.setAlignmentX(0.5f);
        fontCombo.setMaximumSize(fontCombo.getPreferredSize());
        fontSizeCombo.setMaximumSize(fontSizeCombo.getPreferredSize());
        return tb;
    }
    protected void setView(final String viewString,final LogTable table){
        if("Detailed".equals(viewString)){
            table.setDetailedView();
            this._currentView=viewString;
            return;
        }
        final String message=viewString+"does not match a supported view.";
        throw new IllegalArgumentException(message);
    }
    protected JComboBox createLogLevelCombo(){
        final JComboBox result=new JComboBox();
        final Iterator levels=this.getLogLevels();
        while(levels.hasNext()){
            result.addItem(levels.next());
        }
        result.setSelectedItem(this._leastSevereDisplayedLogLevel);
        result.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                final JComboBox box=(JComboBox)e.getSource();
                final LogLevel level=(LogLevel)box.getSelectedItem();
                LogBrokerMonitor.this.setLeastSevereDisplayedLogLevel(level);
            }
        });
        result.setMaximumSize(result.getPreferredSize());
        return result;
    }
    protected void setLeastSevereDisplayedLogLevel(final LogLevel level){
        if(level==null||this._leastSevereDisplayedLogLevel==level){
            return;
        }
        this._leastSevereDisplayedLogLevel=level;
        this._table.getFilteredLogTableModel().refresh();
        this.updateStatusLabel();
    }
    protected void trackTableScrollPane(){
    }
    protected void centerFrame(final JFrame frame){
        final Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension comp=frame.getSize();
        frame.setLocation((screen.width-comp.width)/2,(screen.height-comp.height)/2);
    }
    protected void requestOpen(){
        JFileChooser chooser;
        if(this._fileLocation==null){
            chooser=new JFileChooser();
        }
        else{
            chooser=new JFileChooser(this._fileLocation);
        }
        final int returnVal=chooser.showOpenDialog(this._logMonitorFrame);
        if(returnVal==0){
            final File f=chooser.getSelectedFile();
            if(this.loadLogFile(f)){
                this._fileLocation=chooser.getSelectedFile();
                this._mruFileManager.set(f);
                this.updateMRUList();
            }
        }
    }
    protected void requestOpenURL(){
        final LogFactor5InputDialog inputDialog=new LogFactor5InputDialog(this.getBaseFrame(),"Open URL","URL:");
        String temp=inputDialog.getText();
        if(temp!=null){
            if(temp.indexOf("://")==-1){
                temp="http://"+temp;
            }
            try{
                final URL url=new URL(temp);
                if(this.loadLogFile(url)){
                    this._mruFileManager.set(url);
                    this.updateMRUList();
                }
            }
            catch(MalformedURLException e){
                final LogFactor5ErrorDialog error=new LogFactor5ErrorDialog(this.getBaseFrame(),"Error reading URL.");
            }
        }
    }
    protected void updateMRUList(){
        final JMenu menu=this._logMonitorFrame.getJMenuBar().getMenu(0);
        menu.removeAll();
        menu.add(this.createOpenMI());
        menu.add(this.createOpenURLMI());
        menu.addSeparator();
        menu.add(this.createCloseMI());
        this.createMRUFileListMI(menu);
        menu.addSeparator();
        menu.add(this.createExitMI());
    }
    protected void requestClose(){
        this.setCallSystemExitOnClose(false);
        this.closeAfterConfirm();
    }
    protected void requestOpenMRU(final ActionEvent e){
        String file=e.getActionCommand();
        final StringTokenizer st=new StringTokenizer(file);
        final String num=st.nextToken().trim();
        file=st.nextToken("\n");
        try{
            final int index=Integer.parseInt(num)-1;
            final InputStream in=this._mruFileManager.getInputStream(index);
            final LogFileParser lfp=new LogFileParser(in);
            lfp.parse(this);
            this._mruFileManager.moveToTop(index);
            this.updateMRUList();
        }
        catch(Exception me){
            final LogFactor5ErrorDialog error=new LogFactor5ErrorDialog(this.getBaseFrame(),"Unable to load file "+file);
        }
    }
    protected void requestExit(){
        this._mruFileManager.save();
        this.setCallSystemExitOnClose(true);
        this.closeAfterConfirm();
    }
    protected void closeAfterConfirm(){
        final StringBuffer message=new StringBuffer();
        if(!this._callSystemExitOnClose){
            message.append("Are you sure you want to close the logging ");
            message.append("console?\n");
            message.append("(Note: This will not shut down the Virtual Machine,\n");
            message.append("or the Swing event thread.)");
        }
        else{
            message.append("Are you sure you want to exit?\n");
            message.append("This will shut down the Virtual Machine.\n");
        }
        String title="Are you sure you want to dispose of the Logging Console?";
        if(this._callSystemExitOnClose){
            title="Are you sure you want to exit?";
        }
        final int value=JOptionPane.showConfirmDialog(this._logMonitorFrame,message.toString(),title,2,3,null);
        if(value==0){
            this.dispose();
        }
    }
    protected Iterator getLogLevels(){
        return this._levels.iterator();
    }
    protected Iterator getLogTableColumns(){
        return this._columns.iterator();
    }
    protected boolean loadLogFile(final File file){
        boolean ok=false;
        try{
            final LogFileParser lfp=new LogFileParser(file);
            lfp.parse(this);
            ok=true;
        }
        catch(IOException e){
            final LogFactor5ErrorDialog error=new LogFactor5ErrorDialog(this.getBaseFrame(),"Error reading "+file.getName());
        }
        return ok;
    }
    protected boolean loadLogFile(final URL url){
        boolean ok=false;
        try{
            final LogFileParser lfp=new LogFileParser(url.openStream());
            lfp.parse(this);
            ok=true;
        }
        catch(IOException e){
            final LogFactor5ErrorDialog error=new LogFactor5ErrorDialog(this.getBaseFrame(),"Error reading URL:"+url.getFile());
        }
        return ok;
    }
    class LogBrokerMonitorWindowAdaptor extends WindowAdapter{
        protected LogBrokerMonitor _monitor;
        public LogBrokerMonitorWindowAdaptor(final LogBrokerMonitor monitor){
            super();
            this._monitor=monitor;
        }
        public void windowClosing(final WindowEvent ev){
            this._monitor.requestClose();
        }
    }
}
