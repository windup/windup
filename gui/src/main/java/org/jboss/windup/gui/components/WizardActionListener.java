package org.jboss.windup.gui.components;

/**
 * Listens for events from the {@link WizardControlsPanel}.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public interface WizardActionListener
{
    /**
     * Called when the user has performed a wizard action.
     */
    void wizardActionPerformed(WizardActionEvent event);
}
