package org.apache.log4j.lf5.viewer;

import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import org.apache.log4j.lf5.viewer.LogFactor5Dialog;

public class LogFactor5ErrorDialog extends LogFactor5Dialog{
    public LogFactor5ErrorDialog(final JFrame jframe,final String message){
        super(jframe,"Error",true);
        final JButton ok=new JButton("Ok");
        ok.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogFactor5ErrorDialog.this.hide();
            }
        });
        final JPanel bottom=new JPanel();
        bottom.setLayout(new FlowLayout());
        bottom.add(ok);
        final JPanel main=new JPanel();
        main.setLayout(new GridBagLayout());
        this.wrapStringOnPanel(message,main);
        this.getContentPane().add(main,"Center");
        this.getContentPane().add(bottom,"South");
        this.show();
    }
}
