package org.jboss.windup.gui.components;

import org.jboss.windup.exec.configuration.WindupConfiguration;

/**
 * This interface provides a set of commmon methods for panels with a Wizard-like process.
 * 
 * @author jsightler
 */
public interface WizardStep
{
    /**
     * This should validate, display any error messages to the user, and then return true if the navigation should succeed, or false if the user needs
     * to take additional correction steps.
     */
    boolean validateStep();

    /**
     * Intialize the panel with the given {@link WindupConfiguration}.
     */
    void init(WindupConfiguration cfg);

    /**
     * Indicates that this step should be marked as complete, and it should finish any actions required, such as attaching information to the
     * {@link WindupConfiguration}.
     */
    void stepComplete();
}
