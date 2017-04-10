package org.jboss.windup.rules.apps.summit.demo.rules;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.Document;
import org.jboss.windup.reporting.quickfix.QuickfixLocationDTO;
import org.jboss.windup.reporting.quickfix.QuickfixTransformation;

@SuppressWarnings("unchecked")
public class WeblogicJavaLifecycleQuickfixTransformation implements QuickfixTransformation
{
	public static final String ID = WeblogicJavaLifecycleQuickfixTransformation.class.getSimpleName();
	
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
	
	@SuppressWarnings("deprecation")
	private String refactor(QuickfixLocationDTO locationDTO) throws Exception
	{
		File javaFile = locationDTO.getFile();
		String contents = FileUtils.readFileToString(javaFile, Charset.defaultCharset());
		
		Document doc = new Document(contents);
		
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(doc.get().toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		AST ast = cu.getAST();
		
		cu.recordModifications();
		
		cleanupImports(cu);

		addImports(cu, ast);
		
		TypeDeclaration typeDecl = (TypeDeclaration)cu.types().get(0);
		
		removeSuper(cu, typeDecl);
		
		addApplicationAnnotation(ast, typeDecl);
		
		refactorLifecycleMethods(ast, typeDecl);
		
		cu.rewrite(doc, null).apply(doc);
		
		return doc.get();
	}
	
	private void cleanupImports(CompilationUnit cu)
	{
		 for (Iterator<Object> importsIter = cu.imports().iterator(); importsIter.hasNext();)
		 {
			 ImportDeclaration imported = (ImportDeclaration)importsIter.next();
			 Name name = imported.getName();
			 if (name instanceof QualifiedName)
			 {
				 QualifiedName qualifiedName = (QualifiedName)name;
				 String simpleName = qualifiedName.getName().toString();
				 if (simpleName.equals("ApplicationLifecycleListener") || simpleName.equals("ApplicationLifecycleEvent") )
				 {
					 importsIter.remove();
				 }
			 }
		 }
	}
	
	private void addImports(CompilationUnit cu, AST ast)
	{
		ImportDeclaration importDeclaration = ast.newImportDeclaration();
		importDeclaration.setName(ast.newName(new String[] {"javax", "enterprise", "context", "ApplicationScoped"}));
		cu.imports().add(importDeclaration);
		
		importDeclaration = ast.newImportDeclaration();
		importDeclaration.setName(ast.newName(new String[] {"javax", "annotation", "PostConstruct"}));
		cu.imports().add(importDeclaration);
		
		importDeclaration = ast.newImportDeclaration();
		importDeclaration.setName(ast.newName(new String[] {"javax", "annotation", "PreDestroy"}));
		cu.imports().add(importDeclaration);
	}
	
	private void removeSuper(CompilationUnit cu, TypeDeclaration typeDecl)
	{
		Type superType = typeDecl.getSuperclassType();
		superType.delete();
	}
	
	private void addApplicationAnnotation(AST ast, TypeDeclaration typeDecl)
	{
		MarkerAnnotation annotation = ast.newMarkerAnnotation();
		annotation.setTypeName(ast.newName("ApplicationScoped"));
		typeDecl.modifiers().add(0, annotation);
	}
	
	private void refactorLifecycleMethods(AST ast, TypeDeclaration typeDecl)
	{
		MethodDeclaration[] methods = typeDecl.getMethods();
		for (MethodDeclaration method : methods)
		{
			if (method.getName().toString().equals("postStart"))
			{
				MarkerAnnotation postStartAnnotation = ast.newMarkerAnnotation();
				postStartAnnotation.setTypeName(ast.newName("PostConstruct"));
				method.modifiers().add(0, postStartAnnotation);
				method.setName(ast.newSimpleName("startup"));
				method.parameters().clear();
			}
			else if (method.getName().toString().equals("preStop"))
			{
				MarkerAnnotation preStopAnnotation = ast.newMarkerAnnotation();
				preStopAnnotation.setTypeName(ast.newName("PreDestroy"));
				method.modifiers().add(0, preStopAnnotation);
				method.setName(ast.newSimpleName("shutdown"));
				method.parameters().clear();
			}
		}
	}
}
