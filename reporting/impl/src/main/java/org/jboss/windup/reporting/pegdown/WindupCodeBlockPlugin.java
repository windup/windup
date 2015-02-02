package org.jboss.windup.reporting.pegdown;

import org.parboiled.Rule;
import org.pegdown.Parser;
import org.pegdown.Printer;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;
import org.pegdown.plugins.BlockPluginParser;
import org.pegdown.plugins.PegDownPlugins;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

/**
 * Extends Pegdown with support for adding the language type after the code block indicator.<br/>
 * 
 * This will cause text such as the following:
 * 
 * <pre>
 *     ```java
 *     System.out.println()
 *     ```
 * </pre>
 * 
 * <p>
 * To be rendered as java source code (by appending data-code-syntax='java' to the code element in the resulting output).
 * </p>
 * 
 * <p>
 * Thanks to bradsdavis for the <a href="https://github.com/bradsdavis/pegdown/commit/70e1f598557d9d20164b4b05159400e6d62c73f6">original code</a> for
 * this (adapted here for Windup).
 * </p>
 * 
 * 
 * @author jsightler
 *
 */
public class WindupCodeBlockPlugin extends Parser implements BlockPluginParser, ToHtmlSerializerPlugin
{
    public WindupCodeBlockPlugin()
    {
        super(ALL, (long) 10001, DefaultParseRunnerProvider);
    }

    public WindupCodeBlockPlugin(Integer options, Long maxParsingTimeInMillis, ParseRunnerProvider parseRunnerProvider, PegDownPlugins plugins)
    {
        super(options, maxParsingTimeInMillis, parseRunnerProvider, plugins);
    }

    public WindupCodeBlockPlugin(Integer options, Long maxParsingTimeInMillis, ParseRunnerProvider parseRunnerProvider)
    {
        super(options, maxParsingTimeInMillis, parseRunnerProvider);
    }

    @Override
    public Rule[] blockPluginRules()
    {
        return new Rule[]
        {
                    Code(),
        };
    }

    /**
     * This Rule is used as part of our extension to the regular Code() rule
     */
    public Rule CodeSyntax(Rule ticks)
    {
        return Sequence(
                    ticks, Sequence(OneOrMore(Nonspacechar()), push(match())),
                    Sp(),
                    OneOrMore(
                    FirstOf(
                                Sequence(TestNot('`'),
                                            Nonspacechar()),
                                Sequence(TestNot(ticks),
                                            OneOrMore('`')),
                                Sequence(TestNot(Sp(), ticks),
                                            FirstOf(Spacechar(), Sequence(Newline(), TestNot(BlankLine()))))
                    )
                    ),
                    push(new WindupCodeNode(match(), popAsString())),
                    Sp(), ticks);
    }

    /**
     * Extends the Code rule to support specifying the language of the code block
     */
    @Override
    public Rule Code()
    {
        return NodeSequence(
                    Test('`'),
                    FirstOf(
                                CodeSyntax(Ticks(1)),
                                CodeSyntax(Ticks(2)),
                                CodeSyntax(Ticks(3)),
                                CodeSyntax(Ticks(4)),
                                CodeSyntax(Ticks(5)),
                                Code(Ticks(1)),
                                Code(Ticks(2)),
                                Code(Ticks(3)),
                                Code(Ticks(4)),
                                Code(Ticks(5))
                    ));
    }

    @Override
    public Rule Code(Rule ticks)
    {
        return Sequence(
                    ticks,
                    Sp(),
                    OneOrMore(
                    FirstOf(
                                Sequence(TestNot('`'),
                                            Nonspacechar()),
                                Sequence(TestNot(ticks),
                                            OneOrMore('`')),
                                Sequence(TestNot(Sp(), ticks),
                                            FirstOf(Spacechar(), Sequence(Newline(), TestNot(BlankLine()))))
                    )
                    ),
                    push(new WindupCodeNode(match())),
                    Sp(), ticks);
    }

    @Override
    public boolean visit(Node node, Visitor visitor, Printer printer)
    {
        if (node instanceof WindupCodeNode)
        {
            return visit((WindupCodeNode) node, printer);
        }
        else
        {
            return false;
        }
    }

    /**
     * Output our special {@link WindupCodeNode} with support for specifying the code's language.
     */
    public boolean visit(WindupCodeNode node, Printer printer)
    {
        printer.print("<pre><code");
        String syntax = node.getLanguage();
        if (syntax != null && syntax.length() > 0)
        {
            printer.print(" data-code-syntax='").print(syntax).print("'");
        }
        printer.print(">");
        printer.printEncoded(node.getText());
        printer.print("</code></pre>");
        return true;
    }
}
