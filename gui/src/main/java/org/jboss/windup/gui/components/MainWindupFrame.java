package org.jboss.windup.gui.components;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.WindupPathUtil;

/**
 * The primary entrypoint for running Windup graphically.
 * 
 * @author jsightler
 */
public class MainWindupFrame extends JFrame implements WizardActionListener
{
    private static final long serialVersionUID = 1L;

    private static Logger LOG = Logging.get(MainWindupFrame.class);

    public static final String TITLE = "Windup Migration Tool";
    private static final AtomicInteger EXECUTION_COUNT = new AtomicInteger(1);
    private static final int INITIAL_WIDTH = 900;
    private static final int INITIAL_HEIGHT = 600;

    @Inject
    private GraphContextFactory graphContextFactory;

    @Inject
    private WindupProcessor processor;

    private JPanel mainPanel;
    private CardLayout mainPanelLayout;

    private WindupConfiguration windupConfiguration;

    private Container[] wizardSteps = new Container[] {
                new SelectInputOutputForm(),
                new SelectPackagesForm(),
                // new SelectArchivesForm(), // commented out until this functionality is ready in Windup
                new ReadyToRunForm(),
    };

    private WizardControlsPanel wizardControls;

    public MainWindupFrame()
    {
        initGui();
    }

    private void initGui()
    {
        setTitle(TITLE);
        setSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Container container = getContentPane();
        container.setLayout(new BorderLayout());

        this.mainPanel = new JPanel();
        this.mainPanelLayout = new CardLayout();
        this.mainPanel.setLayout(this.mainPanelLayout);

        this.windupConfiguration = new WindupConfiguration();

        for (Container step : this.wizardSteps)
        {
            ((WizardStep) step).init(this.windupConfiguration);
        }

        for (Container step : this.wizardSteps)
        {
            this.mainPanel.add(step);
        }
        container.add(this.mainPanel, BorderLayout.CENTER);

        this.wizardControls = new WizardControlsPanel();
        this.wizardControls.setPreviousEnabled(false);
        this.wizardControls.setFinishEnabled(false);
        this.wizardControls.addWizardActionListener(this);
        container.add(this.wizardControls, BorderLayout.SOUTH);
    }

    @Override
    public void wizardActionPerformed(WizardActionEvent event)
    {
        switch (event.getActionCommand())
        {
        case WizardActionEvent.COMMAND_PREVIOUS:
            previous();
            break;
        case WizardActionEvent.COMMAND_NEXT:
            next();
            break;
        case WizardActionEvent.COMMAND_FINISH:
            finish();
            break;
        }
    }

    private void previous()
    {
        this.mainPanelLayout.previous(this.mainPanel);
        this.wizardControls.setNextEnabled(true);
        this.wizardControls.setFinishEnabled(false);

        setWizardControlStates();
    }

    private void setWizardControlStates()
    {
        if (getCurrentStep() instanceof SelectInputOutputForm)
        {
            this.wizardControls.setPreviousEnabled(false);
            this.wizardControls.setNextEnabled(true);
            this.wizardControls.setFinishEnabled(false);
        }
        else if (getCurrentStep() instanceof ReadyToRunForm)
        {
            this.wizardControls.setPreviousEnabled(true);
            this.wizardControls.setNextEnabled(false);
            this.wizardControls.setFinishEnabled(true);
        }
        else
        {
            this.wizardControls.setPreviousEnabled(true);
            this.wizardControls.setNextEnabled(true);
            this.wizardControls.setFinishEnabled(false);
        }
    }

    private void next()
    {
        WizardStep step = getCurrentStep();
        if (!step.validateStep())
        {
            return;
        }
        step.stepComplete();
        this.wizardControls.setPreviousEnabled(true);
        this.mainPanelLayout.next(this.mainPanel);

        step = getCurrentStep();
        step.init(this.windupConfiguration);
        setWizardControlStates();
    }

    private void finish()
    {
        WizardStep step = getCurrentStep();
        if (!step.validateStep())
        {
            return;
        }
        this.wizardControls.setFinishEnabled(false);
        this.wizardControls.setPreviousEnabled(false);
        this.wizardControls.setNextEnabled(false);

        runWindup();
    }

    private void runWindup()
    {
        if (WindupPathUtil.pathNotEmpty(windupConfiguration.getOutputDirectory()))
        {
            String promptMsg = "Overwrite all contents of \"" + windupConfiguration.getOutputDirectory().toString()
                        + "\" (anything already in the directory will be deleted)?";
            if (JOptionPane.showConfirmDialog(this, promptMsg, "Overwrite?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            {
                setWizardControlStates();
                return;
            }
        }

        FileUtils.deleteQuietly(windupConfiguration.getOutputDirectory().toFile());
        final Path graphPath = windupConfiguration.getOutputDirectory().resolve("graph");

        final WindupProgressMonitorDialog progressMonitor = new WindupProgressMonitorDialog(MainWindupFrame.this, windupConfiguration);
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try (GraphContext graphContext = graphContextFactory.create(graphPath))
                {
                    windupConfiguration
                                .setProgressMonitor(progressMonitor)
                                .setGraphContext(graphContext);
                    processor.execute(windupConfiguration);
                }
                catch (Exception e)
                {
                    LOG.log(Level.SEVERE, "Error executing Windup due to: " + e.getMessage(), e);
                }
            }
        };
        t.setName("WindupExecution-From-Gui-" + MainWindupFrame.EXECUTION_COUNT.getAndIncrement());
        t.start();
    }

    private WizardStep getCurrentStep()
    {
        WizardStep step = null;
        for (Component component : this.mainPanel.getComponents())
        {
            if (component.isVisible())
            {
                step = (WizardStep) component;
            }
        }
        return step;
    }
}
