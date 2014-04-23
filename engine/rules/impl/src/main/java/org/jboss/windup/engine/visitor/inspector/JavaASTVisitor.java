/*
 * Copyright (c) 2013 Red Hat, Inc. and/or its affiliates.
 *  
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *      Brad Davis - bradsdavis@gmail.com - Initial API and implementation
*/
package org.jboss.windup.engine.visitor.inspector;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.model.resource.JavaClass;

/**
 * Walks the Java Source by creating an Abstract Syntax Tree and walking the results.
 * 
 * @author bradsdavis
 *
 */
public class JavaASTVisitor extends AbstractGraphVisitor {
    private static final Log LOG = LogFactory.getLog(JavaASTVisitor.class);

    @Inject
    private WindupContext windupContext;
    
    @Inject
    private JavaClassDao javaClassDao;
    
    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.MIGRATION_RULES;
    }
    
    @Override
    public void run() {
        for(JavaClass clz : javaClassDao.getAll()) {
            if(clz.getSource() != null) {
                visitJavaClass(clz);
            }
        }
    }
    
    
    @Override
    public void visitJavaClass(JavaClass entry) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setBindingsRecovery(true);
        parser.setResolveBindings(true);
        try {
            File sourceFile = entry.getSource().asFile(); 
            parser.setSource(FileUtils.readFileToString(sourceFile).toCharArray());
        }
        catch (IOException e) {
            LOG.error("Exception setting source for parser.", e);
            return;
        }
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        cu.accept(new JavaASTVariableResolvingVisitor(cu, javaClassDao, windupContext, entry));
    }
}
