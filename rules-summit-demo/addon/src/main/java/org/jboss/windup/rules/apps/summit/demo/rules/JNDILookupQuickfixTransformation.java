package org.jboss.windup.rules.apps.summit.demo.rules;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.Document;
import org.jboss.windup.reporting.quickfix.QuickfixLocationDTO;
import org.jboss.windup.reporting.quickfix.QuickfixTransformation;

public class JNDILookupQuickfixTransformation implements QuickfixTransformation
{
	public static final String ID = JNDILookupQuickfixTransformation.class.getSimpleName();
	
    private static Logger LOG = Logger.getLogger(WeblogicJavaLifecycleQuickfixTransformation.class.getName());
    
	@Override
	public String getTransformationID() {
		return ID;
	}
	
	@Override
	public String transform(QuickfixLocationDTO locationDTO) {
		try 
		{
			return refactor(locationDTO);
		}	
		catch (Exception e)
		{
			LOG.severe(e.getMessage());
		}
		return null;
	}
	
	private String refactor(QuickfixLocationDTO locationDTO) throws Exception
	{
		File javaFile = locationDTO.getFile();
		String contents = FileUtils.readFileToString(javaFile, Charset.defaultCharset());
		
		Document doc = new Document(contents);
		
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(doc.get().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		
		AST ast = cu.getAST();
		
		cu.recordModifications();
		
		TypeDeclaration typeDecl = (TypeDeclaration)cu.types().get(0);
		
		MethodDeclaration[] methods = typeDecl.getMethods();
		for (MethodDeclaration method : methods)
		{
			if (method.getName().toString().equals("lookupService"))
			{
				for (Iterator<Object> iter = method.getBody().statements().iterator(); iter.hasNext();)
				{
					iter.next();
					iter.remove();
				}
				
				// Context context = new InitialContext();
				ClassInstanceCreation creation = ast.newClassInstanceCreation();
				creation.setType(ast.newSimpleType(ast.newSimpleName("InitialContext")));

			    Assignment assignment = ast.newAssignment();
			    
			    VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
			    fragment.setName(ast.newSimpleName("context"));
			    
			    VariableDeclarationExpression expression = ast.newVariableDeclarationExpression(fragment);
			    expression.setType(ast.newSimpleType(ast.newSimpleName("Context")));
			    
			    assignment.setLeftHandSide(expression);
			    
			    assignment.setOperator(Operator.ASSIGN);
			    assignment.setRightHandSide(creation);
				
				ExpressionStatement statement = ast.newExpressionStatement(assignment);
				
			    method.getBody().statements().add(statement);
			    
			    // return (Service)context.lookup("java:app/service/" + ServiceImpl.class.getSimpleName() );
			    MethodInvocation invocation = ast.newMethodInvocation();
			    invocation.setExpression(ast.newSimpleName("context"));
			    invocation.setName(ast.newSimpleName("lookup"));

			    StringLiteral literal = ast.newStringLiteral();
			    literal.setLiteralValue("java:app/service/");
			   
			    InfixExpression infixExpression = ast.newInfixExpression();
			    infixExpression.setLeftOperand(literal);
			    infixExpression.setOperator(InfixExpression.Operator.PLUS);
			    
			    TypeLiteral typeLiteral = ast.newTypeLiteral();
		    	typeLiteral.setType(ast.newSimpleType(ast.newSimpleName("ServiceImpl")));
		    	
			    MethodInvocation nameInvocation = ast.newMethodInvocation();
		    	nameInvocation.setExpression(typeLiteral);
		    	nameInvocation.setName(ast.newSimpleName("getSimpleName"));
		    	
			    infixExpression.setRightOperand(nameInvocation);
			    
			    invocation.arguments().add(infixExpression);
			    
			    CastExpression cast = ast.newCastExpression();
			    cast.setExpression(invocation);
			    cast.setType(ast.newSimpleType(ast.newSimpleName("Service")));
			    
			    ReturnStatement returnStatement = ast.newReturnStatement();
			    returnStatement.setExpression(cast);
			    
			    method.getBody().statements().add(returnStatement);
			    break;
			}
		}
		
		cu.rewrite(doc, null).apply(doc);
		return doc.get();
	}
}
