package org.jboss.windup.rules.files.condition.regex;

import com.github.rwitzel.streamflyer.core.AfterModification;
import com.github.rwitzel.streamflyer.regex.MatchProcessor;
import com.github.rwitzel.streamflyer.regex.MatchProcessorResult;
import com.github.rwitzel.streamflyer.regex.OnStreamMatcher;
import com.github.rwitzel.streamflyer.regex.RegexModifier;
import com.github.rwitzel.streamflyer.regex.addons.util.DoNothingProcessor;
import com.github.rwitzel.streamflyer.util.ModificationFactory;
import com.github.rwitzel.streamflyer.util.statistics.LineColumnAwareModificationFactory;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extends {@link RegexModifier} to support tracking the line number and column, and to fire {@link StreamRegexMatchedEvent} events when content is
 * matched by the regular expression.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class StreamRegexMatcher extends RegexModifier {
    private final StreamRegexMatcherProcessor streamRegexMatcherProcessor;

    private StreamRegexMatcher(String regex, int flags, StreamRegexMatcherProcessor streamRegexMatcherProcessor) {
        super(regex, flags, streamRegexMatcherProcessor, 2048, 2048);
        this.streamRegexMatcherProcessor = streamRegexMatcherProcessor;
    }

    public static StreamRegexMatcher create(String regex, StreamRegexMatchListener listener) {
        StreamRegexMatcherProcessor streamRegexMatcherProcessor = new StreamRegexMatcherProcessor(listener);
        return new StreamRegexMatcher(regex, 0, streamRegexMatcherProcessor);
    }

    @Override
    public AfterModification modify(StringBuilder characterBuffer, int firstModifiableCharacterInBuffer,
                                    boolean endOfStreamHit) {
        this.streamRegexMatcherProcessor.setFirstCharIndex(firstModifiableCharacterInBuffer);
        return super.modify(characterBuffer, firstModifiableCharacterInBuffer, endOfStreamHit);
    }

    @Override
    protected void init(OnStreamMatcher matcher, MatchProcessor matchProcessor, int minimumLengthOfLookBehind,
                        int newNumberOfChars) {
        super.init(matcher, matchProcessor, minimumLengthOfLookBehind, newNumberOfChars);
        ModificationFactory delegate = new ModificationFactory(minimumLengthOfLookBehind, newNumberOfChars);
        this.factory = new LineColumnAwareModificationFactory(delegate);
        this.matchProcessor = matchProcessor;
        this.matcher = matcher;
        this.newNumberOfChars = newNumberOfChars;

        ((StreamRegexMatcherProcessor) matchProcessor).setLineColumnAwareModificationFactory((LineColumnAwareModificationFactory) this.factory);
    }

    private static class StreamRegexMatcherProcessor extends DoNothingProcessor implements MatchProcessor {
        private final StreamRegexMatchListener listener;
        private int firstCharIndex;
        private LineColumnAwareModificationFactory lineColumnAwareModificationFactory;

        public StreamRegexMatcherProcessor(StreamRegexMatchListener listener) {
            this.listener = listener;
        }

        public void setLineColumnAwareModificationFactory(LineColumnAwareModificationFactory lineColumnAwareModificationFactory) {
            this.lineColumnAwareModificationFactory = lineColumnAwareModificationFactory;
        }

        public void setFirstCharIndex(int firstCharIndex) {
            this.firstCharIndex = firstCharIndex;
        }

        @Override
        public MatchProcessorResult process(StringBuilder characterBuffer, int firstModifiableCharacterInBuffer, MatchResult matchResult) {
            long unmatchedStartLine = lineColumnAwareModificationFactory.getCurrentLine();
            long unmatchedStartColumn = lineColumnAwareModificationFactory.getCurrentColumn();
            int unmatchedStart = firstCharIndex;
            int unmatchedEnd = matchResult.start();
            String unmatched = characterBuffer.substring(unmatchedStart, unmatchedEnd);

            Matcher matcher = Pattern.compile("\r\n|\r|\n").matcher(unmatched);
            int numLines = 0;
            int endOfLastLineBreak = 0;
            while (matcher.find()) {
                numLines++;
                endOfLastLineBreak = matcher.end();
            }
            long lineNumber = unmatchedStartLine + numLines;
            long columnNumber;
            if (numLines == 0) {
                columnNumber = unmatchedStartColumn + unmatched.length();
            } else {
                columnNumber = unmatched.length() - endOfLastLineBreak; // length of last line in 'unmatched'
            }

            String matchText = matchResult.group();
            StreamRegexMatchedEvent event = new StreamRegexMatchedEvent(matchText, lineNumber, columnNumber);
            listener.regexMatched(event);
            return super.process(characterBuffer, firstModifiableCharacterInBuffer, matchResult);
        }
    }
}
