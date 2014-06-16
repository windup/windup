package org.jboss.windup.ext.groovy.blacklist;

import org.jboss.windup.rules.apps.java.scan.ast.event.JavaScannerASTEvent;


public interface GroovyBlackListSupport
{
    public abstract void evaluateBlackList(JavaScannerASTEvent event);
}
