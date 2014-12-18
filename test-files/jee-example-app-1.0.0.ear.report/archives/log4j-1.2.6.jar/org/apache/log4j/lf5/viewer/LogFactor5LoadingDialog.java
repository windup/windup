package org.apache.log4j.lf5.viewer;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import org.apache.log4j.lf5.viewer.LogFactor5Dialog;

public class LogFactor5LoadingDialog extends LogFactor5Dialog{
    public LogFactor5LoadingDialog(final JFrame jframe,final String message){
        super(jframe,"LogFactor5",false);
        final JPanel bottom=new JPanel();
        bottom.setLayout(new FlowLayout());
        final JPanel main=new JPanel();
        main.setLayout(new GridBagLayout());
        this.wrapStringOnPanel(message,main);
        this.getContentPane().add(main,"Center");
        this.getContentPane().add(bottom,"South");
        this.show();
    }
}
