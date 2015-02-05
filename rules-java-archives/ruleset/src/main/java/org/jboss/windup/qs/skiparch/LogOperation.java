package org.jboss.windup.qs.skiparch;

import java.util.logging.Logger;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * Wrapper for logging to avoid boilerplate code.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public final class LogOperation {

    private static Logger getLog(){
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        Logger log = Logger.getLogger(stackTrace[3].getClassName());
        return log;
    }

    public static Operation info(final String msg)
    {
        return new Operation()
        {
            @Override
            public void perform(Rewrite event, EvaluationContext context)
            {
                getLog().info(msg);
            }
        };
    }

    public static Operation warning(final String msg)
    {
        return new Operation()
        {
            @Override
            public void perform(Rewrite event, EvaluationContext context)
            {
                getLog().info(msg);
            }
        };
    }

    public static Operation fine(final String msg)
    {
        return new Operation()
        {
            @Override
            public void perform(Rewrite event, EvaluationContext context)
            {
                getLog().info(msg);
            }
        };
    }

}// class
