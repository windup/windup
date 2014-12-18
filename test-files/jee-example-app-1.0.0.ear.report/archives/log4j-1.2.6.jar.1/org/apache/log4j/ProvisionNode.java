package org.apache.log4j;

import org.apache.log4j.Logger;
import java.util.Vector;

class ProvisionNode extends Vector{
    ProvisionNode(final Logger logger){
        super();
        this.addElement(logger);
    }
}
