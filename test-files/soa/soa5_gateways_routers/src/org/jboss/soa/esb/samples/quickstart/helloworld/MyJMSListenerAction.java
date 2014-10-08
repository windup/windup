/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package org.jboss.soa.esb.samples.quickstart.helloworld;

import org.jboss.soa.esb.actions.AbstractActionLifecycle;
import org.jboss.soa.esb.helpers.ConfigTree;
import org.jboss.soa.esb.message.Body;
import org.jboss.soa.esb.message.Message;

public class MyJMSListenerAction extends AbstractActionLifecycle
{
    
    protected ConfigTree _config;
    public MyJMSListenerAction(ConfigTree config) { _config = config; } 
  
    public Message displayMessage(Message message) {
        logHeader();
        System.out.println("Body: " + message.getBody().get());
        logFooter();
        return message;
    }

    public Message playWithMessage(Message message) throws Exception {
           Body msgBody = message.getBody();
           String contents = msgBody.get().toString();
           StringBuffer sb = new StringBuffer();
           sb.append("[Changed] ");
           sb.append(contents);
	   sb.trimToSize();
           msgBody.add(sb.toString());
           return message;
    }

    private void logHeader() {
        System.out.println("\n&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
    }

    private void logFooter() {
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n");
    } 
}
