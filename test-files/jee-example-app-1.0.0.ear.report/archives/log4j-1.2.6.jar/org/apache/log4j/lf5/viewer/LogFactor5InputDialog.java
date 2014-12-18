package org.apache.log4j.lf5.viewer;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.Component;
import javax.swing.JLabel;
import java.awt.LayoutManager;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JTextField;
import org.apache.log4j.lf5.viewer.LogFactor5Dialog;

public class LogFactor5InputDialog extends LogFactor5Dialog{
    public static final int SIZE=30;
    private JTextField _textField;
    public LogFactor5InputDialog(final JFrame jframe,final String title,final String label){
        this(jframe,title,label,30);
    }
    public LogFactor5InputDialog(final JFrame jframe,final String title,final String label,final int size){
        super(jframe,title,true);
        final JPanel bottom=new JPanel();
        bottom.setLayout(new FlowLayout());
        final JPanel main=new JPanel();
        main.setLayout(new FlowLayout());
        main.add(new JLabel(label));
        main.add(this._textField=new JTextField(size));
        this.addKeyListener(new KeyAdapter(){
            public void keyPressed(final KeyEvent e){
                if(e.getKeyCode()==10){
                    LogFactor5InputDialog.this.hide();
                }
            }
        });
        final JButton ok=new JButton("Ok");
        ok.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogFactor5InputDialog.this.hide();
            }
        });
        final JButton cancel=new JButton("Cancel");
        cancel.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e){
                LogFactor5InputDialog.this.hide();
                LogFactor5InputDialog.this._textField.setText("");
            }
        });
        bottom.add(ok);
        bottom.add(cancel);
        this.getContentPane().add(main,"Center");
        this.getContentPane().add(bottom,"South");
        this.pack();
        this.centerWindow(this);
        this.show();
    }
    public String getText(){
        final String s=this._textField.getText();
        if(s!=null&&s.trim().length()==0){
            return null;
        }
        return s;
    }
}
