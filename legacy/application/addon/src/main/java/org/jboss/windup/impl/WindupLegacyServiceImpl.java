package org.jboss.windup.impl;

import java.io.IOException;

import org.jboss.windup.WindupEnvironment;
import org.jboss.windup.WindupLegacyService;
import org.jboss.windup.WindupLegacyServiceException;
import org.jboss.windup.reporting.ReportEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class WindupLegacyServiceImpl implements WindupLegacyService
{
   private static final Logger LOG = LoggerFactory.getLogger(WindupLegacyServiceImpl.class);

   @Override
   public void execute(WindupEnvironment environment) throws WindupLegacyServiceException
   {
	   
	   ReportEngine engine = new ReportEngine(environment);
	   try {
		   engine.process();
	   } catch (IOException e) {
		   throw new WindupLegacyServiceException(e.getMessage(), e);
	   }
   }

}
