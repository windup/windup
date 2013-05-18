package org.jboss.windup.decorator.ccpp.c;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.parser.IToken;
import org.jboss.windup.decorator.ccpp.shared.CCPPLineResult;
import org.jboss.windup.resource.decoration.AbstractDecoration;
import org.jboss.windup.decorator.ccpp.shared.SourceType;
import org.jboss.windup.decorator.ccpp.shared.Language;


public class CASTVisitor extends ASTVisitor {

	private static final Log LOG = LogFactory.getLog(CASTVisitor.class);

	private IASTTranslationUnit translationUnit;

	private Collection<AbstractDecoration> results;

    private Set<String> ignoredCDTErrors;
	private Set<String> blackListedFunctions;
	private Set<String> blackListedTypes;
	private Set<String> keywords;

	public CASTVisitor(IASTTranslationUnit tu, Collection<AbstractDecoration> decorations) {
		super(true); /* Visit Everything */
		includeInactiveNodes = true;
		translationUnit = tu;
		results = decorations;
	}

	public CASTVisitor(IASTTranslationUnit tu, Collection<AbstractDecoration> decorations, Set<String> ignoredCDTErrors, Set<String> blackListedFunctions, Set<String> blackListedTypes, Set<String> keywords) {
		this(tu, decorations);
        this.ignoredCDTErrors = ignoredCDTErrors;
		this.blackListedFunctions = blackListedFunctions;
		this.blackListedTypes = blackListedTypes;
		this.keywords = keywords;
	}
    
	@Override
	public int visit(IASTDeclaration declaration) {
        LOG.debug("Found a declaration of type " + declaration.getClass().getName() + " at line " + declaration.getFileLocation().getStartingLineNumber());
        if (declaration instanceof IASTSimpleDeclaration) {
            processDeclaration(declaration);
        } 
		return PROCESS_CONTINUE;
	}
    
	@Override
	public int visit(IASTExpression expression) {
        LOG.debug("Found a expression of type " + expression.getClass().getName() + " at line " + expression.getFileLocation().getStartingLineNumber());
        if (expression instanceof IASTFunctionCallExpression) {
            processFunctionCallExpression(expression);
        } else if (expression instanceof IASTCastExpression) {
            processCastExpression(expression);
        } else if (expression instanceof IASTTypeIdExpression) {
            processTypeIdExpression(expression);
        } 
		return PROCESS_CONTINUE;
	}
    
	@Override
	public int visit(IASTName name) {
		/*if (name.getSimpleID().length != 0) {
			LOG.info("Found name: " + name.getSimpleID() + " at line: " + name.getFileLocation().getStartingLineNumber());
		} else {
			LOG.warn("Parse error!");
		}*/
		return PROCESS_CONTINUE;
	}
    
	@Override
	public int visit(IASTStatement statement) {
        LOG.debug("Found a statement of type " + statement.getClass().getName() + " at line " + statement.getFileLocation().getStartingLineNumber());
		return PROCESS_CONTINUE;
	}
    
	@Override
	public int visit(IASTProblem problem) {
        /* Some of these are just CDT being brain-dead */
        if (ignoredCDTErrors.toArray().length != 0) {
            for (String ignoredError : ignoredCDTErrors) {
                if (problem.getMessage().equals(ignoredError)) {
                    return PROCESS_CONTINUE;
                }
            }
        }
		CCPPLineResult result = new CCPPLineResult();
		result.setDescription("CDT Warning:  " + problem.getFileLocation().getStartingLineNumber() + ".  " + problem.getMessage());
		result.setLineNumber(new Integer(problem.getFileLocation().getStartingLineNumber()));
		if (problem.isWarning()) {
			result.setPattern(problem.getMessage());
		} else if (problem.isError()) {
			result.setPattern(problem.getMessage());
			/* Weird.  CDT Warnings return true for isError() */
		} else {
			result.setPattern("Unknown issue");
		}
		LOG.info(result.getDescription());
		try {
			LOG.info(problem.getSyntax());
		}
		catch (ExpansionOverlapsBoundaryException e) {
			LOG.warn("Overlap at line " + problem.getFileLocation().getStartingLineNumber());
			/* We actually get a CDT error when this happens, so it's okay to just log it. */
		}
		LOG.info(ReflectionToStringBuilder.toString(problem));
		results.add(result);
		return PROCESS_CONTINUE;
	}
    
    private void processDeclaration(IASTDeclaration declaration) {
        if (declaration instanceof  IASTSimpleDeclaration) {
            IASTDeclSpecifier specifier = ((IASTSimpleDeclaration)declaration).getDeclSpecifier();
            processSpecifier(specifier);
        }
    }
    
    private void processCastExpression(IASTExpression expression) {
        /* Sometimes we get empty expressions i.e. for (++s;*s != '\0';) */
        if ( expression == null ) {
            return;
        }
        if (expression instanceof IASTCastExpression) {
            IASTTypeId typeId = ((IASTCastExpression)expression).getTypeId();
            IASTDeclSpecifier specifier = typeId.getDeclSpecifier();
            processSpecifier(specifier);
        }
    }
    
    private void processTypeIdExpression(IASTExpression expression) {
        /* Sometimes we get empty expressions i.e. for (++s;*s != '\0';) */
        if ( expression == null ) {
            return;
        }
        if (expression instanceof IASTTypeIdExpression) {
            IASTTypeId typeId = ((IASTTypeIdExpression)expression).getTypeId();
            IASTDeclSpecifier specifier = typeId.getDeclSpecifier();
            processSpecifier(specifier);
        }
    }
    
    private void processSpecifier(IASTDeclSpecifier specifier) {
        String type = null;
            
        if (specifier instanceof IASTSimpleDeclSpecifier) {
            /* These are builtin types.  Ignore them for now */
        } else if (specifier instanceof IASTCompositeTypeSpecifier) {
            /* These are structure definitions.  The types in the 
               structure get their own SimpleDeclarations*/
        } else if (specifier instanceof IASTElaboratedTypeSpecifier) {
            /* These are structure types.  Check for bad ones */
            type = ((IASTElaboratedTypeSpecifier)specifier).getName().toString();
        } else if (specifier instanceof IASTNamedTypeSpecifier) {
            /* These are defined types.  Check for bad ones */
            type = ((IASTNamedTypeSpecifier)specifier).getName().toString();
        } 
        
        if ( type != null ) {
            LOG.info("Found type " + type + " at line " + " at line " + specifier.getFileLocation().getStartingLineNumber());
            for (String badType : blackListedTypes) {
                if (type.equals(badType)) {
                    CCPPLineResult dr = new CCPPLineResult();
                    dr.setDescription("Problem Type: " + type + " at line " + specifier.getFileLocation().getStartingLineNumber());
                    dr.setLineNumber(specifier.getFileLocation().getStartingLineNumber());
                    dr.setSourceType(SourceType.TYPE);
                    dr.setLanguage(Language.C);
                    dr.setPattern("Problem Type: " + type);
                    results.add(dr);
                }
            }
        }
    }
    
    private void processFunctionCallExpression(IASTExpression expression) {
        /* Sometimes we get empty expressions i.e. for (++s;*s != '\0';) */
        if ( expression == null ) {
            return;
        }
        
        if (expression instanceof IASTFunctionCallExpression) {
            IASTExpression nameExpression = ((IASTFunctionCallExpression)expression).getFunctionNameExpression();
        
            if (nameExpression instanceof IASTIdExpression) {
                String functionName = ((IASTIdExpression)nameExpression).getName().toString();
                LOG.info("Found function name " + functionName + " at line " + nameExpression.getFileLocation().getStartingLineNumber());
                /* Check this function against the blacklist */
                for (String badFunction : blackListedFunctions) {
                    if (functionName.equals(badFunction)) {
                        CCPPLineResult dr = new CCPPLineResult();
                        dr.setDescription("Problem Function: " + badFunction + " at line " + expression.getFileLocation().getStartingLineNumber());
                        dr.setLineNumber(expression.getFileLocation().getStartingLineNumber());
                        dr.setSourceType(SourceType.FUNCTION);
                        dr.setLanguage(Language.C);
                        dr.setPattern("Problem Function: " + badFunction);
                        results.add(dr);
                    }
                }
            } 
        }
    }
}
