package org.apache.wicket.markup.html.image.resource;

import java.util.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.*;
import org.apache.wicket.util.parse.metapattern.parsers.*;
import org.apache.wicket.util.parse.metapattern.*;

public class DefaultButtonImageResourceFactory implements IResourceFactory{
    public IResource newResource(final String specification,final Locale locale,final String style,final String variation){
        final Parser parser=new Parser((CharSequence)specification);
        if(parser.matches()){
            return new DefaultButtonImageResource(parser.getWidth(),parser.getHeight(),parser.getLabel());
        }
        throw new WicketRuntimeException("DefaultButtonImageResourceFactory does not recognized the specification "+specification);
    }
    private static final class Parser extends MetaPatternParser{
        private static final IntegerGroup width;
        private static final IntegerGroup height;
        private static final Group label;
        private static final MetaPattern pattern;
        public Parser(final CharSequence input){
            super(Parser.pattern,input);
        }
        public String getLabel(){
            return Parser.label.get(this.matcher());
        }
        public int getWidth(){
            return Parser.width.getInt(this.matcher(),-1);
        }
        public int getHeight(){
            return Parser.height.getInt(this.matcher(),-1);
        }
        static{
            width=new IntegerGroup();
            height=new IntegerGroup();
            label=new Group(MetaPattern.ANYTHING);
            pattern=new MetaPattern(new MetaPattern[] { new OptionalMetaPattern(new MetaPattern[] { Parser.width,MetaPattern.COMMA,Parser.height,MetaPattern.COLON }),Parser.label });
        }
    }
}
