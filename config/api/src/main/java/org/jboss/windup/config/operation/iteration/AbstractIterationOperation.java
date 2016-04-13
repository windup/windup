package org.jboss.windup.config.operation.iteration;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.frames.Property;

/**
 * Simplified operation having method that already accepts the found payload.
 */
public abstract class AbstractIterationOperation<T extends WindupVertexFrame> extends GraphOperation
{

    /**
     * When the variable name is not specified, let the Iteration to set the current payload variable name.
     */
    public AbstractIterationOperation()
    {

    }

    public AbstractIterationOperation(String variableName)
    {
        this.variableName = variableName;
    }

    private String variableName;

    public String getVariableName()
    {
        if (variableName == null)
        {
            return null;
        }
        return new VariableNameIterator(variableName).next();
    }

    public void setVariableName(String variableName)
    {
        this.variableName = variableName;
    }

    public boolean hasVariableNameSet()
    {
        return getVariableName() != null;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        checkVariableName(event, context);
        WindupVertexFrame payload = resolveVariable(event, variableName);
        perform(event, context, resolvePayload(event, context, payload));
    }

    @SuppressWarnings("unchecked")
    public T resolvePayload(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload)
    {
        return (T) payload;
    }

    /**
     * Check the variable name and if not set, set it with the singleton variable name being on the top of the stack.
     */
    protected void checkVariableName(GraphRewrite event, EvaluationContext context)
    {
        if (variableName == null)
        {
            setVariableName(Iteration.getPayloadVariableName(event, context));
        }
    }

    /**
     * Resolves variable/property name expressions of the form `
     * <code>#{initialModelVar.customProperty.anotherProp}</code>`, where the `initialModelVar` has a {@link Property}
     * method of the form `<code>@Property public X getCustomProperty()</code>` and `X` has a {@link Property} method of
     * the form `<code>@Property public X getAnotherProp()</code>`
     */
    protected WindupVertexFrame resolveVariable(GraphRewrite event, String variableName)
    {
        Variables variables = Variables.instance(event);
        Iterator<String> tokenizer = new VariableNameIterator(variableName);

        WindupVertexFrame payload;
        String initialName = tokenizer.next();
        try
        {
            payload = Iteration.getCurrentPayload(variables, initialName);
        }
        catch (IllegalArgumentException e1)
        {
            payload = variables.findSingletonVariable(initialName);
        }

        while (tokenizer.hasNext())
        {
            String propertyName = tokenizer.next();
            propertyName = "get" + camelCase(propertyName, true);
            try
            {
                payload = (WindupVertexFrame) payload.getClass().getMethod(propertyName).invoke(payload);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e)
            {
                throw new IllegalArgumentException("Invalid variable expression: " + variableName, e);
            }
        }
        return payload;
    }

    public abstract void perform(GraphRewrite event, EvaluationContext context, T payload);

    // TODO Replace all this lame hacky junk with a real Variables resolving EL implementation.
    private static class VariableNameIterator implements Iterator<String>
    {
        final Queue<String> queue;

        public VariableNameIterator(String name)
        {
            String result = name;
            if (name.trim().startsWith("#{"))
            {
                result = result.replaceAll("\\s*#\\{\\s*([a-zA-Z0-9.]+)\\s*\\}\\s*", "$1");
                result = result.replaceAll("([a-zA-Z0-9]+\\..*)", "$1");
            }
            queue = new LinkedList<>(Arrays.asList(result.split("\\.")));
        }

        @Override
        public boolean hasNext()
        {
            return !queue.isEmpty();
        }

        @Override
        public String next()
        {
            return queue.remove();
        }

        @Override
        public void remove()
        {
            queue.poll();
        }

    }

    /**
     * By default, this method converts strings to UpperCamelCase. If the <code>uppercaseFirstLetter</code> argument to
     * false, then this method produces lowerCamelCase. This method will also use any extra delimiter characters to
     * identify word boundaries.
     * <p>
     * Examples:
     *
     * <pre>
     *   inflector.camelCase(&quot;active_record&quot;,false)    #=&gt; &quot;activeRecord&quot;
     *   inflector.camelCase(&quot;active_record&quot;,true)     #=&gt; &quot;ActiveRecord&quot;
     *   inflector.camelCase(&quot;first_name&quot;,false)       #=&gt; &quot;firstName&quot;
     *   inflector.camelCase(&quot;first_name&quot;,true)        #=&gt; &quot;FirstName&quot;
     *   inflector.camelCase(&quot;name&quot;,false)             #=&gt; &quot;name&quot;
     *   inflector.camelCase(&quot;name&quot;,true)              #=&gt; &quot;Name&quot;
     * </pre>
     *
     * </p>
     *
     * @param lowerCaseAndUnderscoredWord the word that is to be converted to camel case
     * @param uppercaseFirstLetter true if the first character is to be uppercased, or false if the first character is
     *            to be lowercased
     * @param delimiterChars optional characters that are used to delimit word boundaries
     * @return the camel case version of the word
     */
    public String camelCase(String lowerCaseAndUnderscoredWord,
                boolean uppercaseFirstLetter,
                char... delimiterChars)
    {
        if (lowerCaseAndUnderscoredWord == null)
            return null;
        lowerCaseAndUnderscoredWord = lowerCaseAndUnderscoredWord.trim();
        if (lowerCaseAndUnderscoredWord.length() == 0)
            return "";
        if (uppercaseFirstLetter)
        {
            String result = lowerCaseAndUnderscoredWord;
            // Replace any extra delimiters with underscores (before the underscores are converted in the next step)...
            if (delimiterChars != null)
            {
                for (char delimiterChar : delimiterChars)
                {
                    result = result.replace(delimiterChar, '_');
                }
            }

            // Change the case at the beginning at after each underscore ...
            return replaceAllWithUppercase(result, "(^|_)(.)", 2);
        }
        if (lowerCaseAndUnderscoredWord.length() < 2)
            return lowerCaseAndUnderscoredWord;
        return "" + Character.toLowerCase(lowerCaseAndUnderscoredWord.charAt(0))
                    + camelCase(lowerCaseAndUnderscoredWord, true, delimiterChars).substring(1);
    }

    /**
     * Utility method to replace all occurrences given by the specific backreference with its uppercased form, and
     * remove all other backreferences.
     * <p>
     * The Java {@link Pattern regular expression processing} does not use the preprocessing directives <code>\l</code>,
     * <code>&#92;u</code>, <code>\L</code>, and <code>\U</code>. If so, such directives could be used in the
     * replacement string to uppercase or lowercase the backreferences. For example, <code>\L1</code> would lowercase
     * the first backreference, and <code>&#92;u3</code> would uppercase the 3rd backreference.
     * </p>
     *
     * @param input
     * @param regex
     * @param groupNumberToUppercase
     * @return the input string with the appropriate characters converted to upper-case
     */
    String replaceAllWithUppercase(String input,
                String regex,
                int groupNumberToUppercase)
    {
        Pattern underscoreAndDotPattern = Pattern.compile(regex);
        Matcher matcher = underscoreAndDotPattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find())
        {
            matcher.appendReplacement(sb, matcher.group(groupNumberToUppercase).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " with var '" + variableName + "'";
    }
}
