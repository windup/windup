package org.jboss.windup.reporting.freemarker;

import java.util.List;
import java.util.Map;

import org.pegdown.LinkRenderer;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.VerbatimSerializer;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

public class ToHtmlSerializerExtended extends ToHtmlSerializer {
    public ToHtmlSerializerExtended(LinkRenderer linkRenderer) {
        super(linkRenderer);
    }

    public ToHtmlSerializerExtended(LinkRenderer linkRenderer, List<ToHtmlSerializerPlugin> plugins) {
        super(linkRenderer, plugins);
    }

    public ToHtmlSerializerExtended(LinkRenderer linkRenderer, Map<String, VerbatimSerializer> verbatimSerializers,
                                    List<ToHtmlSerializerPlugin> plugins) {
        super(linkRenderer, verbatimSerializers, plugins);
    }

    public ToHtmlSerializerExtended(LinkRenderer linkRenderer, Map<String, VerbatimSerializer> verbatimSerializers) {
        super(linkRenderer, verbatimSerializers);
    }

    @Override
    protected void printLink(LinkRenderer.Rendering rendering) {
        printer.print('<').print('a');
        printAttribute("href", rendering.href);
        for (LinkRenderer.Attribute attr : rendering.attributes) {
            printAttribute(attr.name, attr.value);
        }
        printAttribute("target", "_blank");
        printer.print('>').print(rendering.text).print("</a>");
    }

    private void printAttribute(String name, String value) {
        printer.print(' ').print(name).print('=').print('"').print(value).print('"');
    }

}
