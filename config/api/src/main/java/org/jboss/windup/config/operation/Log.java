package org.jboss.windup.config.operation;

import java.util.Set;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.logging.Logger.Level;
import org.ocpsoft.rewrite.config.DefaultOperationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.ParameterizedPattern;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternBuilder;


/**
 * Logging utility class. Usage:
 * <code>
 *    Log.message(Level.INFO, "Client requested path: {p}")
 * </code>
 *
 * @see {@link Log#message(Level, String)}
 */
public class Log extends DefaultOperationBuilder implements Parameterized
{
    private final Logger log;
    private final Level level;
    private final RegexParameterizedPatternBuilder messageBuilder;

    private Log(Logger log, Level level, String message)
    {
        Class<?> caller = findClassCaller();
        if (caller == null)
            caller = Log.class;
        log = Logger.getLogger(caller);

        if (level == null)
            level = Level.INFO;

        if (message == null)
            message = "(null)";

        this.log = log;
        this.level = level;
        this.messageBuilder = new RegexParameterizedPatternBuilder(message);
    }

    private Log(Level level, String message)
    {
        this(null, level, message);
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

    public static Log message(Class<?> cls, Level level, String message)
    {
        return new Log(Logger.getLogger(cls), level, message);
    }

    @Override
    public void perform(Rewrite event, EvaluationContext context)
    {
        // Quite verbose. TODO: ... See https://github.com/ocpsoft/logging/issues/1
        switch (level)
        {
        case TRACE:
            if (log.isTraceEnabled())
                log.trace(messageBuilder.build(event, context));
            break;

        case DEBUG:
            if (log.isDebugEnabled())
                log.debug(messageBuilder.build(event, context));
            break;

        case INFO:
            if (log.isInfoEnabled())
                log.info(messageBuilder.build(event, context));
            break;

        case WARN:
            if (log.isWarnEnabled())
                log.warn(messageBuilder.build(event, context));
            break;

        case ERROR:
            if (log.isErrorEnabled())
                log.error(messageBuilder.build(event, context));
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

    @Override
    public String toString()
    {
        return "LOG[" + level + ", " + messageBuilder.toString() + "]";
    }


    /**
     * Finds the calling class - looks for most recent call outside of this class (and descendants).
     */
    private static Class<? extends StackTraceElement> findClassCaller()
    {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for( int i = 2; i < stackTrace.length; i++ )
        {
            StackTraceElement call = stackTrace[i];
            if (!Log.class.isAssignableFrom(call.getClass()))
                return call.getClass();
        }

        return null;
    }
}