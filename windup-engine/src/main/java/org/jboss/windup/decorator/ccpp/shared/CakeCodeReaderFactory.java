package org.jboss.windup.decorator.ccpp.shared;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.ICodeReaderCache;
import org.eclipse.cdt.internal.core.dom.AbstractCodeReaderFactory;
import org.eclipse.cdt.internal.core.dom.IIncludeFileResolutionHeuristics;
import org.eclipse.core.runtime.CoreException;

public class CakeCodeReaderFactory extends AbstractCodeReaderFactory {

	public CakeCodeReaderFactory(IIncludeFileResolutionHeuristics heuristics) {
		super(heuristics);
	}

	private static final Log LOG = LogFactory.getLog(CakeCodeReaderFactory.class);

	@Override
	public int getUniqueIdentifier() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public CodeReader createCodeReaderForTranslationUnit(String path) {
		LOG.info("CRFTU PATH:  " + path);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CodeReader createCodeReaderForInclusion(String path) {
		LOG.info("CRFI PATH:  " + path);
		String[] split = path.split("/");
		String newPath = "/home/chale/ctest/" + split[split.length - 1];

		/*
		 * try {
		 * return new CodeReader(newPath);
		 * }
		 * catch (IOException e) {
		 * LOG.error("Cannot find #include:  " + newPath);
		 * LOG.error(e);
		 * return null;
		 * }
		 */
		return null;

	}

	@Override
	public ICodeReaderCache getCodeReaderCache() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CodeReader createCodeReaderForInclusion(IIndexFileLocation ifl, String astPath) throws CoreException, IOException {
		LOG.info("CRFI w/ IFL PATH:  " + astPath);
		// TODO Auto-generated method stub
		return null;
	}

}

