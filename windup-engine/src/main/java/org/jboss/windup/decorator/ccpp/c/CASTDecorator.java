package org.jboss.windup.decorator.ccpp.c;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.parser.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.parser.AbstractGNUSourceCodeParser;
import org.eclipse.cdt.internal.core.dom.parser.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;
import org.jboss.windup.decorator.ChainingDecorator;
import org.jboss.windup.decorator.ccpp.shared.CCPPLineResult;
import org.jboss.windup.decorator.ccpp.shared.CakeCDTLogServiceAdaptor;
import org.jboss.windup.decorator.ccpp.shared.CakeCodeReaderFactory;
import org.jboss.windup.decorator.ccpp.shared.Language;
import org.jboss.windup.decorator.ccpp.shared.PlatformIncludeHeuristics;
import org.jboss.windup.decorator.ccpp.shared.SourceType;
import org.jboss.windup.resource.type.CCPPMeta;


/**
 * CASTDecorator uses the Eclipse Project's CDT library to parse 
 * C source code and search for items.
 * 
 * @author Clark Hale
 *
 */
public class CASTDecorator extends ChainingDecorator<CCPPMeta> {

	private static final Log LOG = LogFactory.getLog(CASTDecorator.class);

	private Set<String> ignoredCDTErrors;
	private Set<String> blackListedIncludes;
	private Set<String> blackListedFunctions;
	private Set<String> blackListedTypes;
	private Set<String> keywords;

	protected static final IParserLogService LOG_ADAPTOR = new CakeCDTLogServiceAdaptor();

	@Override
	public void processMeta(CCPPMeta meta) {
		/*
		 * Build initial IASTTranslationUnit 
		 */
		IASTTranslationUnit tu = buildIASTTranslationUnit(meta);
		IASTPreprocessorIncludeStatement[] includeStatements = tu.getIncludeDirectives();

		/*
		 * Preprocessor directives cannot be parsed out easily with the AST, so we
		 * look at the bad ones here.
		 */
		if (includeStatements != null) {
			for (IASTPreprocessorIncludeStatement statement : includeStatements) {
				processIncludes(meta, statement.getName(), statement.getFileLocation().getStartingLineNumber());
			}
		}

		/*
		 * Parse out and visit source file.
		 */
		tu.accept(new CASTVisitor(tu, meta.getDecorations(), ignoredCDTErrors, blackListedFunctions, blackListedTypes, keywords));

		/*
		 * Continue on...
		 */
		super.chainDecorators(meta);
	}

	/**
	 * Process Include files searching for black-listed items.
	 * @param meta
	 * @param name
	 * @param decoratorPrefix
	 * @param position
	 */
	private void processIncludes(CCPPMeta meta, IASTName name, int position) {
		
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


	/**
	 * Build the Translation Unit with reasonable parameters.
	 * @param meta
	 * @return
	 */
	private IASTTranslationUnit buildIASTTranslationUnit(CCPPMeta meta) {

		/* Create Scanner */

		/* Bare minimum required for ScannerInfo */
		Map<String, String> scannerInfoMap = new HashMap<String, String>();
		scannerInfoMap.put("__SIZEOF_INT__", "4");
		scannerInfoMap.put("__SIZEOF_LONG__", "8");

		/* Create Scanner Info */
		IScannerInfo scannerInfo = new ScannerInfo(scannerInfoMap);

		IScanner scanner = new CPreprocessor(meta.getFileContent(), scannerInfo, ParserLanguage.C, LOG_ADAPTOR, GCCScannerExtensionConfiguration.getInstance(),
				IncludeFileContentProvider.adapt(new CakeCodeReaderFactory(new PlatformIncludeHeuristics())));

		/* Create Parser */
		AbstractGNUSourceCodeParser parser = new GNUCSourceParser(scanner, ParserMode.COMPLETE_PARSE, LOG_ADAPTOR, new ANSICParserExtensionConfiguration(), null);

		IASTTranslationUnit translationUnit = parser.parse();

		if (parser.encounteredError()) {
			LOG.warn("C Parser Encountered Error.");
		}

		return translationUnit;
	}

	public Set<String> getignoredCDTErrors() {
		return ignoredCDTErrors;
	}

	public void setignoredCDTErrors(Set<String> ignoredCDTErrors) {
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

	public Set<String> getkeywords() {
		return keywords;
	}

	public void setkeywords(Set<String> keywords) {
		this.keywords = keywords;
	}
}
