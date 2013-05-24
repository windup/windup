package org.jboss.windup.decorator.ccpp.cpp;

import java.util.Collection;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.jboss.windup.decorator.ccpp.shared.CCPPLineResult;
import org.jboss.windup.resource.decoration.AbstractDecoration;

/**
 * C++ AST Visitor 
 * 
 * @author Clark Hale
 *
 */
public class CPPASTVisitor extends ASTVisitor {

	private static final Log LOG = LogFactory.getLog(CPPASTVisitor.class);

	private IASTTranslationUnit translationUnit;
	private Collection<AbstractDecoration> results;

	public CPPASTVisitor(IASTTranslationUnit tu, Collection<AbstractDecoration> decorations) {
		super(true); /* Visit Everything */
		includeInactiveNodes = true;
		translationUnit = tu;
		results = decorations;
	}

	@Override
	public int visit(IASTName name) {
		LOG.info("Found name:  " + new String(name.getSimpleID()) + " at line: " + name.getFileLocation().getStartingLineNumber());
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTStatement statement) {
		CCPPLineResult result = new CCPPLineResult();
		result.setLineNumber(statement.getFileLocation().getStartingLineNumber());
		result.setDescription("CPP Statement at line:  " + statement.getFileLocation().getStartingLineNumber());
		result.setPattern("blah");
		LOG.info(result.getDescription());
		results.add(result);
		return PROCESS_CONTINUE;
	}

	@Override
	public int visit(IASTProblem problem) {
		CCPPLineResult result = new CCPPLineResult();		
		result.setLineNumber(problem.getFileLocation().getStartingLineNumber());
		result.setDescription("CPP Problem at line:  " + problem.getFileLocation().getStartingLineNumber() + ".  " + problem.getMessage());
		result.setPattern("blah");
		LOG.info(result.getDescription());
		try {
			LOG.info(problem.getSyntax());
		}
		catch (ExpansionOverlapsBoundaryException e) {
			LOG.warn("Exception.");
			LOG.warn(e.toString());
		}
		LOG.info(ReflectionToStringBuilder.toString(problem));
		results.add(result);
		return PROCESS_CONTINUE;
	}
}
