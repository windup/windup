package org.apache.log4j.chainsaw;

import org.apache.log4j.chainsaw.ExitAction;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import org.apache.log4j.Priority;
import java.awt.Component;
import javax.swing.JLabel;
import java.awt.LayoutManager;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import org.apache.log4j.chainsaw.MyTableModel;
import org.apache.log4j.Category;
import javax.swing.JPanel;

class ControlPanel extends JPanel{
    private static final Category LOG;
    static /* synthetic */ Class class$org$apache$log4j$chainsaw$ControlPanel;
    ControlPanel(final MyTableModel aModel){
        super();
        this.setBorder(BorderFactory.createTitledBorder("Controls: "));
        final GridBagLayout gridbag=new GridBagLayout();
        final GridBagConstraints c=new GridBagConstraints();
        this.setLayout(gridbag);
        c.ipadx=5;
        c.ipady=5;
        c.gridx=0;
        c.anchor=13;
        c.gridy=0;
        JLabel label=new JLabel("Filter Level:");
        gridbag.setConstraints(label,c);
        this.add(label);
        final GridBagConstraints gridBagConstraints=c;
        ++gridBagConstraints.gridy;
        label=new JLabel("Filter Thread:");
        gridbag.setConstraints(label,c);
        this.add(label);
        final GridBagConstraints gridBagConstraints2=c;
        ++gridBagConstraints2.gridy;
        label=new JLabel("Filter Category:");
        gridbag.setConstraints(label,c);
        this.add(label);
        final GridBagConstraints gridBagConstraints3=c;
        ++gridBagConstraints3.gridy;
        label=new JLabel("Filter NDC:");
        gridbag.setConstraints(label,c);
        this.add(label);
        final GridBagConstraints gridBagConstraints4=c;
        ++gridBagConstraints4.gridy;
        label=new JLabel("Filter Message:");
        gridbag.setConstraints(label,c);
        this.add(label);
        c.weightx=1.0;
        c.gridx=1;
        c.anchor=17;
        c.gridy=0;
        final Priority[] allPriorities=Priority.getAllPossiblePriorities();
        final JComboBox priorities=new JComboBox((E[])allPriorities);
        final Priority lowest=allPriorities[allPriorities.length-1];
        priorities.setSelectedItem(lowest);
        aModel.setPriorityFilter(lowest);
        gridbag.setConstraints(priorities,c);
        this.add(priorities);
        priorities.setEditable(false);
        priorities.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent aEvent){
                aModel.setPriorityFilter((Priority)priorities.getSelectedItem());
            }
        });
        c.fill=2;
        final GridBagConstraints gridBagConstraints5=c;
        ++gridBagConstraints5.gridy;
        final JTextField threadField=new JTextField("");
        threadField.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(final DocumentEvent aEvent){
                aModel.setThreadFilter(threadField.getText());
            }
            public void removeUpdate(final DocumentEvent aEvente){
                aModel.setThreadFilter(threadField.getText());
            }
            public void changedUpdate(final DocumentEvent aEvent){
                aModel.setThreadFilter(threadField.getText());
            }
        });
        gridbag.setConstraints(threadField,c);
        this.add(threadField);
        final GridBagConstraints gridBagConstraints6=c;
        ++gridBagConstraints6.gridy;
        final JTextField catField=new JTextField("");
        catField.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(final DocumentEvent aEvent){
                aModel.setCategoryFilter(catField.getText());
            }
            public void removeUpdate(final DocumentEvent aEvent){
                aModel.setCategoryFilter(catField.getText());
            }
            public void changedUpdate(final DocumentEvent aEvent){
                aModel.setCategoryFilter(catField.getText());
            }
        });
        gridbag.setConstraints(catField,c);
        this.add(catField);
        final GridBagConstraints gridBagConstraints7=c;
        ++gridBagConstraints7.gridy;
        final JTextField ndcField=new JTextField("");
        ndcField.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(final DocumentEvent aEvent){
                aModel.setNDCFilter(ndcField.getText());
            }
            public void removeUpdate(final DocumentEvent aEvent){
                aModel.setNDCFilter(ndcField.getText());
            }
            public void changedUpdate(final DocumentEvent aEvent){
                aModel.setNDCFilter(ndcField.getText());
            }
        });
        gridbag.setConstraints(ndcField,c);
        this.add(ndcField);
        final GridBagConstraints gridBagConstraints8=c;
        ++gridBagConstraints8.gridy;
        final JTextField msgField=new JTextField("");
        msgField.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(final DocumentEvent aEvent){
                aModel.setMessageFilter(msgField.getText());
            }
            public void removeUpdate(final DocumentEvent aEvent){
                aModel.setMessageFilter(msgField.getText());
            }
            public void changedUpdate(final DocumentEvent aEvent){
                aModel.setMessageFilter(msgField.getText());
            }
        });
        gridbag.setConstraints(msgField,c);
        this.add(msgField);
        c.weightx=0.0;
        c.fill=2;
        c.anchor=13;
        c.gridx=2;
        c.gridy=0;
        final JButton exitButton=new JButton("Exit");
        exitButton.setMnemonic('x');
        exitButton.addActionListener(ExitAction.INSTANCE);
        gridbag.setConstraints(exitButton,c);
        this.add(exitButton);
        final GridBagConstraints gridBagConstraints9=c;
        ++gridBagConstraints9.gridy;
        final JButton clearButton=new JButton("Clear");
        clearButton.setMnemonic('c');
        clearButton.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent aEvent){
                aModel.clear();
            }
        });
        gridbag.setConstraints(clearButton,c);
        this.add(clearButton);
        final GridBagConstraints gridBagConstraints10=c;
        ++gridBagConstraints10.gridy;
        final JButton toggleButton=new JButton("Pause");
        toggleButton.setMnemonic('p');
        toggleButton.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent aEvent){
                aModel.toggle();
                toggleButton.setText(aModel.isPaused()?"Resume":"Pause");
            }
        });
        gridbag.setConstraints(toggleButton,c);
        this.add(toggleButton);
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
        LOG=Category.getInstance((ControlPanel.class$org$apache$log4j$chainsaw$ControlPanel==null)?(ControlPanel.class$org$apache$log4j$chainsaw$ControlPanel=class$("org.apache.log4j.chainsaw.ControlPanel")):ControlPanel.class$org$apache$log4j$chainsaw$ControlPanel);
    }
}
