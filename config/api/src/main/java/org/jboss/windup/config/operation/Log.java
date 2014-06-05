package org.jboss.windup.config.operation;

import java.util.Set;

import org.ocpsoft.logging.Logger;
import org.ocpsoft.logging.Logger.Level;
import org.ocpsoft.rewrite.config.ConfigurationRuleParameterBuilder;
import org.ocpsoft.rewrite.config.DefaultOperationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.ParameterizedPattern;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternBuilder;

public class Log extends DefaultOperationBuilder implements Parameterized
{
    private static final Logger log = Logger.getLogger(Log.class);

    private final Level level;
    private final RegexParameterizedPatternBuilder messageBuilder;

    private Log(Level level, String message)
    {
        this.level = level;
        this.messageBuilder = new RegexParameterizedPatternBuilder(message);
    }

    /**
     * Log a message at the given {@link Level}.
     * <p>
     * The message may be parameterized:
     * <p>
     * For example, assuming a given {@link Condition} has been configured to use a parameter "p"}:
     * <p>
     * <code>
     *    Path.matches("/{p}")
     * </code>
     * <p>
     * The parameter "p" may be referenced in the log message:
     * <p>
     * <code>
     *    Log.message(Level.INFO, "Client requested path: {p}")
     * </code>
     * <p>
     * 
     * @param level the log {@link Level} to which the message be written
     * @param message {@link ParameterizedPattern} to be written to the log.
     * 
     * @see {@link ConfigurationRuleParameterBuilder#where(String)}
     */
    public static Log message(Level level, String message)
    {
        return new Log(level, message);
    }

    @Override
    public void perform(Rewrite event, EvaluationContext context)
    {
        final String message = messageBuilder.build(event, context);

        switch (level)
        {
        case TRACE:
            if (log.isTraceEnabled())
                log.trace(message);
            break;

        case DEBUG:
            if (log.isDebugEnabled())
                log.debug(message);
            break;

        case INFO:
            if (log.isInfoEnabled())
                log.info(message);
            break;

        case WARN:
            if (log.isWarnEnabled())
                log.warn(message);
            break;

        case ERROR:
            if (log.isErrorEnabled())
                log.error(message);
            break;

        default:
            break;
        }
    }

    @Override
    public Set<String> getRequiredParameterNames()
    {
        return messageBuilder.getRequiredParameterNames();
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
        messageBuilder.setParameterStore(store);
    }

}