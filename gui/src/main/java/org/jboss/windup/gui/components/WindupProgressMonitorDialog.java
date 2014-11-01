package org.jboss.windup.gui.components;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.jboss.windup.exec.WindupProgressMonitor;
import org.jboss.windup.exec.configuration.WindupConfiguration;

/**
 * Displays the progress during the execution of Windup.
 * 
 * @author jsightler
 */
public class WindupProgressMonitorDialog extends JDialog implements WindupProgressMonitor
{
    private static final long serialVersionUID = 1L;
    private final MainWindupFrame parentFrame;
    private final JLabel statusLabel;
    private final JProgressBar progressBar;
    private final WindupConfiguration windupConfiguration;

    /**
     * Creates this dialog as a modal for the given parent.
     */
    public WindupProgressMonitorDialog(MainWindupFrame parent, WindupConfiguration windupConfiguration)
    {
        super(parent);
        this.parentFrame = parent;
        this.windupConfiguration = windupConfiguration;
        setTitle("Windup Progress");
        Dimension d = new Dimension(600, 100);
        setMinimumSize(d);
        setSize(d);
        setMaximumSize(d);

        statusLabel = new JLabel("Initializing...");

        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);

        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(statusLabel, BorderLayout.NORTH);
        container.add(progressBar, BorderLayout.CENTER);

        setVisible(true);
        setLocationRelativeTo(parent);
    }

    private void executeOnEventThread(Runnable r)
    {
        if (EventQueue.isDispatchThread())
        {
            r.run();
        }
        else
        {
            EventQueue.invokeLater(r);
        }
    }

    @Override
    public void beginTask(String name, final int totalWork)
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                progressBar.setMaximum(totalWork);
            }
        };
        executeOnEventThread(r);
    }

    @Override
    public void done()
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                JOptionPane.showMessageDialog(WindupProgressMonitorDialog.this,
                            "Windup Execution complete! Report is in: " + windupConfiguration.getOutputDirectory());
                parentFrame.dispose();
                dispose();
            }
        };
        executeOnEventThread(r);
    }

    @Override
    public boolean isCancelled()
    {
        return false;
    }

    @Override
    public void setCancelled(boolean value)
    {

    }

    @Override
    public void setTaskName(String name)
    {
    }

    @Override
    public void subTask(final String name)
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                statusLabel.setText("Executing (" + progressBar.getValue() + "/" + progressBar.getMaximum() + "): " + name);
            }
        };
        executeOnEventThread(r);
    }

    @Override
    public void worked(final int work)
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                progressBar.setValue(progressBar.getValue() + work);
            }
        };
        executeOnEventThread(r);

    }

}
