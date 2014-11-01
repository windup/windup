package org.jboss.windup.gui.components;

import java.awt.event.ActionEvent;

/**
 * Sent whenever one of the wizard buttons (next, previous, finish) is clicked.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class WizardActionEvent extends ActionEvent
{
    private static final long serialVersionUID = 1L;
    public static final String COMMAND_PREVIOUS = "previous";
    public static final String COMMAND_NEXT = "next";
    public static final String COMMAND_FINISH = "finish";

    /**
     * Creates an event with the given source, id, and command.
     */
    public WizardActionEvent(Object source, int id, String command)
    {
        super(source, id, command);
    }

}
