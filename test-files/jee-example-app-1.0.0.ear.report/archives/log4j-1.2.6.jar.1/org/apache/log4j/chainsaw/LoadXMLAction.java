package org.apache.log4j.chainsaw;

import java.io.IOException;
import java.io.Reader;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.io.File;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import javax.xml.parsers.SAXParserFactory;
import org.apache.log4j.chainsaw.MyTableModel;
import org.apache.log4j.chainsaw.XMLFileHandler;
import org.xml.sax.XMLReader;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.apache.log4j.Category;
import javax.swing.AbstractAction;

class LoadXMLAction extends AbstractAction{
    private static final Category LOG;
    private final JFrame mParent;
    private final JFileChooser mChooser;
    private final XMLReader mParser;
    private final XMLFileHandler mHandler;
    static /* synthetic */ Class class$org$apache$log4j$chainsaw$LoadXMLAction;
    LoadXMLAction(final JFrame aParent,final MyTableModel aModel) throws SAXException,ParserConfigurationException{
        super();
        (this.mChooser=new JFileChooser()).setMultiSelectionEnabled(false);
        this.mChooser.setFileSelectionMode(0);
        this.mParent=aParent;
        this.mHandler=new XMLFileHandler(aModel);
        (this.mParser=SAXParserFactory.newInstance().newSAXParser().getXMLReader()).setContentHandler(this.mHandler);
    }
    public void actionPerformed(final ActionEvent aIgnore){
        LoadXMLAction.LOG.info("load file called");
        if(this.mChooser.showOpenDialog(this.mParent)==0){
            LoadXMLAction.LOG.info("Need to load a file");
            final File chosen=this.mChooser.getSelectedFile();
            LoadXMLAction.LOG.info("loading the contents of "+chosen.getAbsolutePath());
            try{
                final int num=this.loadFile(chosen.getAbsolutePath());
                JOptionPane.showMessageDialog(this.mParent,"Loaded "+num+" events.","CHAINSAW",1);
            }
            catch(Exception e){
                LoadXMLAction.LOG.warn("caught an exception loading the file",e);
                JOptionPane.showMessageDialog(this.mParent,"Error parsing file - "+e.getMessage(),"CHAINSAW",0);
            }
        }
    }
    private int loadFile(final String aFile) throws SAXException,IOException{
        synchronized(this.mParser){
            final StringBuffer buf=new StringBuffer();
            buf.append("<?xml version=\"1.0\" standalone=\"yes\"?>\n");
            buf.append("<!DOCTYPE log4j:eventSet ");
            buf.append("[<!ENTITY data SYSTEM \"file:///");
            buf.append(aFile);
            buf.append("\">]>\n");
            buf.append("<log4j:eventSet xmlns:log4j=\"Claira\">\n");
            buf.append("&data;\n");
            buf.append("</log4j:eventSet>\n");
            final InputSource is=new InputSource(new StringReader(buf.toString()));
            this.mParser.parse(is);
            return this.mHandler.getNumEvents();
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
    static{
        LOG=Category.getInstance((LoadXMLAction.class$org$apache$log4j$chainsaw$LoadXMLAction==null)?(LoadXMLAction.class$org$apache$log4j$chainsaw$LoadXMLAction=class$("org.apache.log4j.chainsaw.LoadXMLAction")):LoadXMLAction.class$org$apache$log4j$chainsaw$LoadXMLAction);
    }
}
