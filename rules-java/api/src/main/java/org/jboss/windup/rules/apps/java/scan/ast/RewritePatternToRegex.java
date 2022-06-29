package org.jboss.windup.rules.apps.java.scan.ast;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class RewritePatternToRegex {
    private final String rewritePattern;
    private final Pattern compiledRegex;

    public String getRewritePattern() {
        return rewritePattern;
    }

    public Pattern getCompiledRegex() {
        return compiledRegex;
    }

    public RewritePatternToRegex(String rewritePattern, Pattern compiledRegex) {
        this.rewritePattern = rewritePattern;
        this.compiledRegex = compiledRegex;
    }

}
