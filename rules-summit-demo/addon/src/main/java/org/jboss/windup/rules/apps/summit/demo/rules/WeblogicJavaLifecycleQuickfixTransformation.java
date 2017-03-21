package org.jboss.windup.rules.apps.summit.demo.rules;

import java.io.File;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.reporting.quickfix.QuickfixLocationDTO;
import org.jboss.windup.reporting.quickfix.QuickfixTransformation;

public class WeblogicJavaLifecycleQuickfixTransformation implements QuickfixTransformation
{
	public static final String ID = WeblogicJavaLifecycleQuickfixTransformation.class.getSimpleName();
	
    private static Logger LOG = Logger.getLogger(WeblogicJavaLifecycleQuickfixTransformation.class.getName());
    
    @Inject
    private GraphContextFactory graphContextFactory;
	
	@Override
	public String getTransformationID() {
		return ID;
	}
	
	@Override
	public String transform(QuickfixLocationDTO locationDTO) {
		try 
		{
			StringBuilder builder = new StringBuilder();
			builder.append("<!--");
			getApplicationLifecycleListener(locationDTO);
			builder.append("-->");
			return builder.toString();
		}	
		catch (Exception e)
		{
			LOG.severe(e.getMessage());
		}
		return null;
	}
	
	private Class<?> getApplicationLifecycleListener(QuickfixLocationDTO locationDTO) throws Exception
	{
		File javaFile = locationDTO.getFile();
		String contents = FileUtils.readFileToString(javaFile, Charset.defaultCharset());
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(contents.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		AST ast = cu.getAST();

		ImportDeclaration scopeImport = ast.newImportDeclaration();
		scopeImport.setName(ast.newName(new String[] {"javax", "enterprise", "context", "ApplicationScoped"}));
		
		ASTRewrite rewriter = ASTRewrite.create(ast);
		
		
		return null;
	}
}
