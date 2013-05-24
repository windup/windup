package org.jboss.windup.decorator.ccpp.cpp;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.jboss.windup.decorator.ChainingDecorator;
import org.jboss.windup.decorator.ccpp.c.CASTDecorator;
import org.jboss.windup.decorator.ccpp.shared.CCPPLineResult;
import org.jboss.windup.decorator.ccpp.shared.CakeCDTLogServiceAdaptor;
import org.jboss.windup.decorator.ccpp.shared.CakeCodeReaderFactory;
import org.jboss.windup.decorator.ccpp.shared.Language;
import org.jboss.windup.decorator.ccpp.shared.PlatformIncludeHeuristics;
import org.jboss.windup.decorator.ccpp.shared.SourceType;
import org.jboss.windup.resource.type.CCPPMeta;

/**
 * C++ Decorator using Eclipse Foundation's CDT library.
 * 
 * @author Clark Hale
 *
 */
public class CPPASTDecorator extends ChainingDecorator<CCPPMeta> {

	private static final Log LOG = LogFactory.getLog(CASTDecorator.class);

	protected static final IParserLogService LOG_ADAPTOR = new CakeCDTLogServiceAdaptor();

	private Set<String> ignoredCDTErrors;
	private Set<String> blackListedIncludes;
	private Set<String> blackListedFunctions;
	private Set<String> blackListedTypes;
	private Set<String> keywords;
	
	@Override
	public void processMeta(CCPPMeta meta) {
		LOG.info("Parsing C++ file " +meta.getFilePointer().getName());
		// Build IASTTranslationUnit
		IASTTranslationUnit tu = buildIASTTranslationUnit(meta);
		
		
		/*
		 * Process #include directives separately, because they don't come out as
		 * clean in the AST
		 */
		IASTPreprocessorIncludeStatement[] statements = tu.getIncludeDirectives();		
		if (statements != null) {
			for (IASTPreprocessorIncludeStatement statement : statements) {
				processName(meta, statement.getName(), statement.getFileLocation().getStartingLineNumber());
			}
		}

		tu.accept(new CPPASTVisitor(tu, meta.getDecorations()));
	}

	private void processName(CCPPMeta meta, IASTName name, int position) {
		if (name == null) {
			return;
		}

		int sourcePosition = position;
		String sourceString = name.toString();

		for (String badInclude : blackListedIncludes) {
			if (sourceString.equals(badInclude)) {
				CCPPLineResult dr = new CCPPLineResult();
				dr.setDescription("Problem Include: '" + badInclude + "' at line " + sourcePosition);

				dr.setLineNumber(sourcePosition);
				dr.setSourceType(SourceType.INCLUDE);
				dr.setLanguage(Language.C);
				dr.setPattern("Problem Include: " + badInclude);
				
				meta.getDecorations().add(dr);
			}
		}
	}

	public IASTTranslationUnit buildIASTTranslationUnit(CCPPMeta meta) {

		/* Create Scanner */

		/* Bare minimum required for ScannerInfo */
		Map<String, String> scannerInfoMap = new HashMap<String, String>();
		scannerInfoMap.put("__SIZEOF_INT__", "4");
		scannerInfoMap.put("__SIZEOF_LONG__", "8");

		/* Create Scanner Info */
		IScannerInfo scannerInfo = new ScannerInfo(scannerInfoMap);

		IScanner scanner = new CPreprocessor(meta.getFileContent(), scannerInfo, ParserLanguage.CPP, LOG_ADAPTOR, GCCScannerExtensionConfiguration.getInstance(),
				IncludeFileContentProvider.adapt(new CakeCodeReaderFactory(new PlatformIncludeHeuristics())));

		/* Create Parser */
		AbstractGNUSourceCodeParser parser = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, LOG_ADAPTOR, new ANSICPPParserExtensionConfiguration(), null);

		IASTTranslationUnit translationUnit = parser.parse();

		if (parser.encounteredError()) {
			LOG.warn("CPP Parser Encountered Error.");
		}

		return translationUnit;

	}

	public Set<String> getIgnoredCDTErrors() {
		return ignoredCDTErrors;
	}

	public void setIgnoredCDTErrors(Set<String> ignoredCDTErrors) {
		this.ignoredCDTErrors = ignoredCDTErrors;
	}

	public Set<String> getBlackListedIncludes() {
		return blackListedIncludes;
	}

	public void setBlackListedIncludes(Set<String> blackListedIncludes) {
		this.blackListedIncludes = blackListedIncludes;
	}

	public Set<String> getBlackListedFunctions() {
		return blackListedFunctions;
	}

	public void setBlackListedFunctions(Set<String> blackListedFunctions) {
		this.blackListedFunctions = blackListedFunctions;
	}

	public Set<String> getBlackListedTypes() {
		return blackListedTypes;
	}

	public void setBlackListedTypes(Set<String> blackListedTypes) {
		this.blackListedTypes = blackListedTypes;
	}

	public Set<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(Set<String> keywords) {
		this.keywords = keywords;
	}

}
