package org.jboss.windup.ext.groovy.blacklist;

import org.jboss.windup.rules.apps.javascanner.ast.event.JavaScannerASTEvent;

public interface GroovyBlackListSupport
{
    public abstract void evaluateBlackList(JavaScannerASTEvent event);
}
