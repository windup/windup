package org.jboss.windup.xmlunit;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

/**
 * User: rsearls
 * Date: 5/29/13
 */
public class XmlDifferenceListener implements DifferenceListener {

    private boolean calledFlag = false;
    public boolean called() {
        return calledFlag;
    }

    public int differenceFound(Difference difference) {
        calledFlag = true;
        return RETURN_ACCEPT_DIFFERENCE;
    }

    public void skippedComparison(Node control, Node test) {
    }
}
