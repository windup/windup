package org.jboss.windup.decorator.ccpp.shared;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.cdt.core.parser.IParserLogService;

/**
 * Very Simple wrapper around CDT's ParserLogService.
 * 
 * Sends all messages to debug mode.
 * 
 * @author chale
 *
 */
public class CakeCDTLogServiceAdaptor implements IParserLogService {
	private static final Log LOG = LogFactory.getLog(CakeCDTLogServiceAdaptor.class);

	@Override
	public void traceLog(String message) {
		LOG.debug(message);
	}

	@Override
	public boolean isTracing() {
		// Always true for the moment.
		return true;
	}

}