package org.jboss.windup.rules.files.condition.regex;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.streamflyer.regex.MatchProcessor;
import com.googlecode.streamflyer.regex.MatchProcessorResult;
import com.googlecode.streamflyer.regex.OnStreamMatcher;
import com.googlecode.streamflyer.regex.OnStreamStandardMatcher;
import com.googlecode.streamflyer.regex.RegexModifier;
import com.googlecode.streamflyer.regex.addons.util.DoNothingProcessor;
import com.googlecode.streamflyer.util.ModificationFactory;
import com.googlecode.streamflyer.util.statistics.LineColumnAwareModificationFactory;

/**
 * Extends {@link RegexModifier} to support tracking the line number and column, and to fire {@link StreamRegexMatchedEvent} events when content is
 * matched by the regular expression.
 * 
 * @author jsightler
 *
 */
public class StreamRegexMatcher extends RegexModifier
{
    private final StreamRegexMatchListener listener;

    public StreamRegexMatcher(String regex, StreamRegexMatchListener listener)
    {
        this.listener = listener;
        Matcher jdkMatcher = Pattern.compile(regex, 0).matcher("");
        jdkMatcher.useTransparentBounds(true);
        jdkMatcher.useAnchoringBounds(false);
        init(new OnStreamStandardMatcher(jdkMatcher), new StreamRegexMatcherProcessor(), 2048, 2048);
    }

    @SuppressWarnings("hiding")
    protected void init(OnStreamMatcher matcher, MatchProcessor matchProcessor, int minimumLengthOfLookBehind,
                int newNumberOfChars)
    {

        ModificationFactory modFactory = new ModificationFactory(minimumLengthOfLookBehind, newNumberOfChars);
        this.factory = new LineColumnAwareModificationFactory(modFactory);
        this.matchProcessor = matchProcessor;
        this.matcher = matcher;
        this.newNumberOfChars = newNumberOfChars;
    }

    private LineColumnAwareModificationFactory getMatchFactory()
    {
        return (LineColumnAwareModificationFactory) this.factory;
    }

    private class StreamRegexMatcherProcessor extends DoNothingProcessor implements MatchProcessor
    {
        @Override
        public MatchProcessorResult process(StringBuilder characterBuffer, int firstModifiableCharacterInBuffer, MatchResult matchResult)
        {
            LineColumnAwareModificationFactory matchFactory = getMatchFactory();
            // This is the line number of the start of the buffer, not necessarily the start of the match
            long lineNumber = matchFactory.getCurrentLine();
            // the start Column may be after the first char in a line, so use the LineColumnAwareModificationFactory to figure out the actual column
            // number
            int startColumn = (int) matchFactory.getCurrentColumn() + matchResult.start() - firstModifiableCharacterInBuffer;
            boolean calculateStartColumn = false;

            // now we calculate the actual line number based upon the start line number of the buffer and the index of the match in the buffer
            char lastChar = 0;
            for (int i = 0; i < matcher.start(); i++)
            {
                char ch = characterBuffer.charAt(i);
                if (ch == '\r')
                {
                    startColumn = 0;
                    calculateStartColumn = true;
                    lineNumber++;
                }
                else if (ch == '\n')
                {
                    if (lastChar != '\r')
                    {
                        startColumn = 0;
                        calculateStartColumn = true;
                        lineNumber++;
                    }
                }
                else if (calculateStartColumn)
                {
                    startColumn++;
                }
                lastChar = ch;
            }

            StreamRegexMatchedEvent event = new StreamRegexMatchedEvent(matchResult.group(), lineNumber, startColumn);
            listener.regexMatched(event);
            MatchProcessorResult result = super.process(characterBuffer, firstModifiableCharacterInBuffer, matchResult);
            return result;
        }
    }
}
