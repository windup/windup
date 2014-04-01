package org.jboss.windup.impl;

import java.io.IOException;

import org.jboss.windup.WindupEnvironment;
import org.jboss.windup.WindupService;
import org.jboss.windup.WindupServiceException;
import org.jboss.windup.reporting.ReportEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class WindupServiceImpl implements WindupService
{
   private static final Logger LOG = LoggerFactory.getLogger(WindupServiceImpl.class);

   @Override
   public void execute(WindupEnvironment environment) throws WindupServiceException
   {
	   
	   ReportEngine engine = new ReportEngine(environment);
	   try {
		   engine.process();
	   } catch (IOException e) {
		   throw new WindupServiceException(e.getMessage(), e);
	   }
   }

}
