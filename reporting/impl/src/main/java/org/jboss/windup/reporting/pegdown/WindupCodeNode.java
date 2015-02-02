package org.jboss.windup.reporting.pegdown;

import java.util.Collections;
import java.util.List;

import org.pegdown.ast.AbstractNode;
import org.pegdown.ast.CodeNode;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;

/**
 * Provides an object that is similar to a {@link CodeNode}, but that also contains a code type attribute.<br/>
 * 
 * NOTE: It does not appear that the serializer will work if this actually subclasses {@link CodeNode}, so this is directly a subclass of
 * {@link AbstractNode}.
 *
 */
public class WindupCodeNode extends AbstractNode
{
    private String text;
    private String language;

    /**
     * Construct a {@link WindupCodeNode} instance with the specified text.
     */
    public WindupCodeNode(String text)
    {
        this.text = text;
    }

    /**
     * Construct a {@link WindupCodeNode} instance with the specified text and syntax.
     */
    public WindupCodeNode(String text, String language)
    {
        this(text);
        this.language = language;
    }

    /**
     * Contains the body of the Code block.
     */
    public String getText()
    {
        return text;
    }

    /**
     * Contains the body of the Code block.
     */
    public void setText(String text)
    {
        this.text = text;
    }

    /**
     * Contains the language used for the code block (this may provide a hint for syntax highlighting).
     */
    public String getLanguage()
    {
        return language;
    }

    /**
     * Contains the language used for the code block (this may provide a hint for syntax highlighting).
     */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    @Override
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public List<Node> getChildren()
    {
        return Collections.emptyList();
    }
}