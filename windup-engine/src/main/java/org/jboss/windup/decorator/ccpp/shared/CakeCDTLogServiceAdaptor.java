package org.jboss.windup.decorator.ccpp.shared;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.cdt.core.parser.IParserLogService;

public class CakeCDTLogServiceAdaptor implements IParserLogService {
	private static final Log LOG = LogFactory.getLog(CakeCDTLogServiceAdaptor.class);

	@Override
	public void traceLog(String message) {
		LOG.info(message);
	}

	@Override
	public boolean isTracing() {
		// Always true for the moment.
		return true;
	}

}