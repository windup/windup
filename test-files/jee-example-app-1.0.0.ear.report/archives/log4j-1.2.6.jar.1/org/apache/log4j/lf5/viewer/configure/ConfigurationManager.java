package org.apache.log4j.lf5.viewer.configure;

import java.util.Iterator;
import java.io.IOException;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.List;
import org.apache.log4j.lf5.viewer.LogTableColumnFormatException;
import java.util.ArrayList;
import java.awt.Color;
import java.util.Map;
import org.apache.log4j.lf5.LogLevelFormatException;
import javax.swing.JCheckBoxMenuItem;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryExplorerTree;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryPath;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import javax.swing.tree.TreePath;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryNode;
import org.apache.log4j.lf5.viewer.categoryexplorer.CategoryExplorerModel;
import org.apache.log4j.lf5.viewer.LogTableColumn;
import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.viewer.LogTable;
import org.apache.log4j.lf5.viewer.LogBrokerMonitor;

public class ConfigurationManager{
    private static final String CONFIG_FILE_NAME="lf5_configuration.xml";
    private static final String NAME="name";
    private static final String PATH="path";
    private static final String SELECTED="selected";
    private static final String EXPANDED="expanded";
    private static final String CATEGORY="category";
    private static final String FIRST_CATEGORY_NAME="Categories";
    private static final String LEVEL="level";
    private static final String COLORLEVEL="colorlevel";
    private static final String COLOR="color";
    private static final String RED="red";
    private static final String GREEN="green";
    private static final String BLUE="blue";
    private static final String COLUMN="column";
    private static final String NDCTEXTFILTER="searchtext";
    private LogBrokerMonitor _monitor;
    private LogTable _table;
    public ConfigurationManager(final LogBrokerMonitor monitor,final LogTable table){
        super();
        this._monitor=null;
        this._table=null;
        this._monitor=monitor;
        this._table=table;
        this.load();
    }
    public void save(){
        final CategoryExplorerModel model=this._monitor.getCategoryExplorerTree().getExplorerModel();
        final CategoryNode root=model.getRootCategoryNode();
        final StringBuffer xml=new StringBuffer(2048);
        this.openXMLDocument(xml);
        this.openConfigurationXML(xml);
        this.processLogRecordFilter(this._monitor.getNDCTextFilter(),xml);
        this.processLogLevels(this._monitor.getLogLevelMenuItems(),xml);
        this.processLogLevelColors(this._monitor.getLogLevelMenuItems(),LogLevel.getLogLevelColorMap(),xml);
        this.processLogTableColumns(LogTableColumn.getLogTableColumns(),xml);
        this.processConfigurationNode(root,xml);
        this.closeConfigurationXML(xml);
        this.store(xml.toString());
    }
    public void reset(){
        this.deleteConfigurationFile();
        this.collapseTree();
        this.selectAllNodes();
    }
    public static String treePathToString(final TreePath path){
        final StringBuffer sb=new StringBuffer();
        CategoryNode n=null;
        final Object[] objects=path.getPath();
        for(int i=1;i<objects.length;++i){
            n=(CategoryNode)objects[i];
            if(i>1){
                sb.append(".");
            }
            sb.append(n.getTitle());
        }
        return sb.toString();
    }
    protected void load(){
        final File file=new File(this.getFilename());
        if(file.exists()){
            try{
                final DocumentBuilderFactory docBuilderFactory=DocumentBuilderFactory.newInstance();
                final DocumentBuilder docBuilder=docBuilderFactory.newDocumentBuilder();
                final Document doc=docBuilder.parse(file);
                this.processRecordFilter(doc);
                this.processCategories(doc);
                this.processLogLevels(doc);
                this.processLogLevelColors(doc);
                this.processLogTableColumns(doc);
            }
            catch(Exception e){
                System.err.println("Unable process configuration file at "+this.getFilename()+". Error Message="+e.getMessage());
            }
        }
    }
    protected void processRecordFilter(final Document doc){
        final NodeList nodeList=doc.getElementsByTagName("searchtext");
        final Node n=nodeList.item(0);
        if(n==null){
            return;
        }
        final NamedNodeMap map=n.getAttributes();
        final String text=this.getValue(map,"name");
        if(text==null||text.equals("")){
            return;
        }
        this._monitor.setNDCLogRecordFilter(text);
    }
    protected void processCategories(final Document doc){
        final CategoryExplorerTree tree=this._monitor.getCategoryExplorerTree();
        final CategoryExplorerModel model=tree.getExplorerModel();
        final NodeList nodeList=doc.getElementsByTagName("category");
        NamedNodeMap map=nodeList.item(0).getAttributes();
        for(int j=this.getValue(map,"name").equalsIgnoreCase("Categories")?1:0,i=nodeList.getLength()-1;i>=j;--i){
            final Node n=nodeList.item(i);
            map=n.getAttributes();
            final CategoryNode chnode=model.addCategory(new CategoryPath(this.getValue(map,"path")));
            chnode.setSelected(this.getValue(map,"selected").equalsIgnoreCase("true"));
            if(!this.getValue(map,"expanded").equalsIgnoreCase("true")){
            }
            tree.expandPath(model.getTreePathToRoot(chnode));
        }
    }
    protected void processLogLevels(final Document doc){
        final NodeList nodeList=doc.getElementsByTagName("level");
        final Map menuItems=this._monitor.getLogLevelMenuItems();
        for(int i=0;i<nodeList.getLength();++i){
            final Node n=nodeList.item(i);
            final NamedNodeMap map=n.getAttributes();
            final String name=this.getValue(map,"name");
            try{
                final JCheckBoxMenuItem item=menuItems.get(LogLevel.valueOf(name));
                item.setSelected(this.getValue(map,"selected").equalsIgnoreCase("true"));
            }
            catch(LogLevelFormatException ex){
            }
        }
    }
    protected void processLogLevelColors(final Document doc){
        final NodeList nodeList=doc.getElementsByTagName("colorlevel");
        final Map logLevelColors=LogLevel.getLogLevelColorMap();
        for(int i=0;i<nodeList.getLength();++i){
            final Node n=nodeList.item(i);
            if(n==null){
                return;
            }
            final NamedNodeMap map=n.getAttributes();
            final String name=this.getValue(map,"name");
            try{
                final LogLevel level=LogLevel.valueOf(name);
                final int red=Integer.parseInt(this.getValue(map,"red"));
                final int green=Integer.parseInt(this.getValue(map,"green"));
                final int blue=Integer.parseInt(this.getValue(map,"blue"));
                final Color c=new Color(red,green,blue);
                if(level!=null){
                    level.setLogLevelColorMap(level,c);
                }
            }
            catch(LogLevelFormatException ex){
            }
        }
    }
    protected void processLogTableColumns(final Document doc){
        final NodeList nodeList=doc.getElementsByTagName("column");
        final Map menuItems=this._monitor.getLogTableColumnMenuItems();
        final List selectedColumns=new ArrayList();
        for(int i=0;i<nodeList.getLength();++i){
            final Node n=nodeList.item(i);
            if(n==null){
                return;
            }
            final NamedNodeMap map=n.getAttributes();
            final String name=this.getValue(map,"name");
            try{
                final LogTableColumn column=LogTableColumn.valueOf(name);
                final JCheckBoxMenuItem item=menuItems.get(column);
                item.setSelected(this.getValue(map,"selected").equalsIgnoreCase("true"));
                if(item.isSelected()){
                    selectedColumns.add(column);
                }
            }
            catch(LogTableColumnFormatException ex){
            }
            if(selectedColumns.isEmpty()){
                this._table.setDetailedView();
            }
            else{
                this._table.setView(selectedColumns);
            }
        }
    }
    protected String getValue(final NamedNodeMap map,final String attr){
        final Node n=map.getNamedItem(attr);
        return n.getNodeValue();
    }
    protected void collapseTree(){
        final CategoryExplorerTree tree=this._monitor.getCategoryExplorerTree();
        for(int i=tree.getRowCount()-1;i>0;--i){
            tree.collapseRow(i);
        }
    }
    protected void selectAllNodes(){
        final CategoryExplorerModel model=this._monitor.getCategoryExplorerTree().getExplorerModel();
        final CategoryNode root=model.getRootCategoryNode();
        final Enumeration all=root.breadthFirstEnumeration();
        CategoryNode n=null;
        while(all.hasMoreElements()){
            n=all.nextElement();
            n.setSelected(true);
        }
    }
    protected void store(final String s){
        try{
            final PrintWriter writer=new PrintWriter(new FileWriter(this.getFilename()));
            writer.print(s);
            writer.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
    protected void deleteConfigurationFile(){
        try{
            final File f=new File(this.getFilename());
            if(f.exists()){
                f.delete();
            }
        }
        catch(SecurityException e){
            System.err.println("Cannot delete "+this.getFilename()+" because a security violation occured.");
        }
    }
    protected String getFilename(){
        final String home=System.getProperty("user.home");
        final String sep=System.getProperty("file.separator");
        return home+sep+"lf5"+sep+"lf5_configuration.xml";
    }
    private void processConfigurationNode(final CategoryNode node,final StringBuffer xml){
        final CategoryExplorerModel model=this._monitor.getCategoryExplorerTree().getExplorerModel();
        final Enumeration all=node.breadthFirstEnumeration();
        CategoryNode n=null;
        while(all.hasMoreElements()){
            n=all.nextElement();
            this.exportXMLElement(n,model.getTreePathToRoot(n),xml);
        }
    }
    private void processLogLevels(final Map logLevelMenuItems,final StringBuffer xml){
        xml.append("\t<loglevels>\r\n");
        for(final LogLevel level : logLevelMenuItems.keySet()){
            final JCheckBoxMenuItem item=logLevelMenuItems.get(level);
            this.exportLogLevelXMLElement(level.getLabel(),item.isSelected(),xml);
        }
        xml.append("\t</loglevels>\r\n");
    }
    private void processLogLevelColors(final Map logLevelMenuItems,final Map logLevelColors,final StringBuffer xml){
        xml.append("\t<loglevelcolors>\r\n");
        for(final LogLevel level : logLevelMenuItems.keySet()){
            final Color color=logLevelColors.get(level);
            this.exportLogLevelColorXMLElement(level.getLabel(),color,xml);
        }
        xml.append("\t</loglevelcolors>\r\n");
    }
    private void processLogTableColumns(final List logTableColumnMenuItems,final StringBuffer xml){
        xml.append("\t<logtablecolumns>\r\n");
        for(final LogTableColumn column : logTableColumnMenuItems){
            final JCheckBoxMenuItem item=this._monitor.getTableColumnMenuItem(column);
            this.exportLogTableColumnXMLElement(column.getLabel(),item.isSelected(),xml);
        }
        xml.append("\t</logtablecolumns>\r\n");
    }
    private void processLogRecordFilter(final String text,final StringBuffer xml){
        xml.append("\t<").append("searchtext").append(" ");
        xml.append("name").append("=\"").append(text).append("\"");
        xml.append("/>\r\n");
    }
    private void openXMLDocument(final StringBuffer xml){
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
    }
    private void openConfigurationXML(final StringBuffer xml){
        xml.append("<configuration>\r\n");
    }
    private void closeConfigurationXML(final StringBuffer xml){
        xml.append("</configuration>\r\n");
    }
    private void exportXMLElement(final CategoryNode node,final TreePath path,final StringBuffer xml){
        final CategoryExplorerTree tree=this._monitor.getCategoryExplorerTree();
        xml.append("\t<").append("category").append(" ");
        xml.append("name").append("=\"").append(node.getTitle()).append("\" ");
        xml.append("path").append("=\"").append(treePathToString(path)).append("\" ");
        xml.append("expanded").append("=\"").append(tree.isExpanded(path)).append("\" ");
        xml.append("selected").append("=\"").append(node.isSelected()).append("\"/>\r\n");
    }
    private void exportLogLevelXMLElement(final String label,final boolean selected,final StringBuffer xml){
        xml.append("\t\t<").append("level").append(" ").append("name");
        xml.append("=\"").append(label).append("\" ");
        xml.append("selected").append("=\"").append(selected);
        xml.append("\"/>\r\n");
    }
    private void exportLogLevelColorXMLElement(final String label,final Color color,final StringBuffer xml){
        xml.append("\t\t<").append("colorlevel").append(" ").append("name");
        xml.append("=\"").append(label).append("\" ");
        xml.append("red").append("=\"").append(color.getRed()).append("\" ");
        xml.append("green").append("=\"").append(color.getGreen()).append("\" ");
        xml.append("blue").append("=\"").append(color.getBlue());
        xml.append("\"/>\r\n");
    }
    private void exportLogTableColumnXMLElement(final String label,final boolean selected,final StringBuffer xml){
        xml.append("\t\t<").append("column").append(" ").append("name");
        xml.append("=\"").append(label).append("\" ");
        xml.append("selected").append("=\"").append(selected);
        xml.append("\"/>\r\n");
    }
}
