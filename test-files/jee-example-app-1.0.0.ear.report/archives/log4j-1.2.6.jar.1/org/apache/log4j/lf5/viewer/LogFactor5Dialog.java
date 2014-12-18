package org.apache.log4j.lf5.viewer;

import java.awt.GridBagConstraints;
import java.awt.Label;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.Component;
import java.awt.Frame;
import javax.swing.JFrame;
import java.awt.Font;
import javax.swing.JDialog;

public abstract class LogFactor5Dialog extends JDialog{
    protected static final Font DISPLAY_FONT;
    protected LogFactor5Dialog(final JFrame jframe,final String message,final boolean modal){
        super(jframe,message,modal);
    }
    public void show(){
        this.pack();
        this.minimumSizeDialog(this,200,100);
        this.centerWindow(this);
        super.show();
    }
    protected void centerWindow(final Window win){
        final Dimension screenDim=Toolkit.getDefaultToolkit().getScreenSize();
        if(screenDim.width<win.getSize().width){
            win.setSize(screenDim.width,win.getSize().height);
        }
        if(screenDim.height<win.getSize().height){
            win.setSize(win.getSize().width,screenDim.height);
        }
        final int x=(screenDim.width-win.getSize().width)/2;
        final int y=(screenDim.height-win.getSize().height)/2;
        win.setLocation(x,y);
    }
    protected void wrapStringOnPanel(String message,final Container container){
        final GridBagConstraints c=this.getDefaultConstraints();
        c.gridwidth=0;
        c.insets=new Insets(0,0,0,0);
        final GridBagLayout gbLayout=(GridBagLayout)container.getLayout();
        while(message.length()>0){
            final int newLineIndex=message.indexOf(10);
            String line;
            if(newLineIndex>=0){
                line=message.substring(0,newLineIndex);
                message=message.substring(newLineIndex+1);
            }
            else{
                line=message;
                message="";
            }
            final Label label=new Label(line);
            label.setFont(LogFactor5Dialog.DISPLAY_FONT);
            gbLayout.setConstraints(label,c);
            container.add(label);
        }
    }
    protected GridBagConstraints getDefaultConstraints(){
        final GridBagConstraints constraints=new GridBagConstraints();
        constraints.weightx=1.0;
        constraints.weighty=1.0;
        constraints.gridheight=1;
        constraints.insets=new Insets(4,4,4,4);
        constraints.fill=0;
        constraints.anchor=17;
        return constraints;
    }
    protected void minimumSizeDialog(final Component component,final int minWidth,final int minHeight){
        if(component.getSize().width<minWidth){
            component.setSize(minWidth,component.getSize().height);
        }
        if(component.getSize().height<minHeight){
            component.setSize(component.getSize().width,minHeight);
        }
    }
    static{
        DISPLAY_FONT=new Font("Arial",1,12);
    }
}
