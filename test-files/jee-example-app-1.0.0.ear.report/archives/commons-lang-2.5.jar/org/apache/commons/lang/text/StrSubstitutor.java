package org.apache.commons.lang.text;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.text.StrBuilder;
import java.util.Map;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrMatcher;

public class StrSubstitutor{
    public static final char DEFAULT_ESCAPE='$';
    public static final StrMatcher DEFAULT_PREFIX;
    public static final StrMatcher DEFAULT_SUFFIX;
    private char escapeChar;
    private StrMatcher prefixMatcher;
    private StrMatcher suffixMatcher;
    private StrLookup variableResolver;
    public static String replace(final Object source,final Map valueMap){
        return new StrSubstitutor(valueMap).replace(source);
    }
    public static String replace(final Object source,final Map valueMap,final String prefix,final String suffix){
        return new StrSubstitutor(valueMap,prefix,suffix).replace(source);
    }
    public static String replaceSystemProperties(final Object source){
        return new StrSubstitutor(StrLookup.systemPropertiesLookup()).replace(source);
    }
    public StrSubstitutor(){
        this(null,StrSubstitutor.DEFAULT_PREFIX,StrSubstitutor.DEFAULT_SUFFIX,'$');
    }
    public StrSubstitutor(final Map valueMap){
        this(StrLookup.mapLookup(valueMap),StrSubstitutor.DEFAULT_PREFIX,StrSubstitutor.DEFAULT_SUFFIX,'$');
    }
    public StrSubstitutor(final Map valueMap,final String prefix,final String suffix){
        this(StrLookup.mapLookup(valueMap),prefix,suffix,'$');
    }
    public StrSubstitutor(final Map valueMap,final String prefix,final String suffix,final char escape){
        this(StrLookup.mapLookup(valueMap),prefix,suffix,escape);
    }
    public StrSubstitutor(final StrLookup variableResolver){
        this(variableResolver,StrSubstitutor.DEFAULT_PREFIX,StrSubstitutor.DEFAULT_SUFFIX,'$');
    }
    public StrSubstitutor(final StrLookup variableResolver,final String prefix,final String suffix,final char escape){
        super();
        this.setVariableResolver(variableResolver);
        this.setVariablePrefix(prefix);
        this.setVariableSuffix(suffix);
        this.setEscapeChar(escape);
    }
    public StrSubstitutor(final StrLookup variableResolver,final StrMatcher prefixMatcher,final StrMatcher suffixMatcher,final char escape){
        super();
        this.setVariableResolver(variableResolver);
        this.setVariablePrefixMatcher(prefixMatcher);
        this.setVariableSuffixMatcher(suffixMatcher);
        this.setEscapeChar(escape);
    }
    public String replace(final String source){
        if(source==null){
            return null;
        }
        final StrBuilder buf=new StrBuilder(source);
        if(!this.substitute(buf,0,source.length())){
            return source;
        }
        return buf.toString();
    }
    public String replace(final String source,final int offset,final int length){
        if(source==null){
            return null;
        }
        final StrBuilder buf=new StrBuilder(length).append(source,offset,length);
        if(!this.substitute(buf,0,length)){
            return source.substring(offset,offset+length);
        }
        return buf.toString();
    }
    public String replace(final char[] source){
        if(source==null){
            return null;
        }
        final StrBuilder buf=new StrBuilder(source.length).append(source);
        this.substitute(buf,0,source.length);
        return buf.toString();
    }
    public String replace(final char[] source,final int offset,final int length){
        if(source==null){
            return null;
        }
        final StrBuilder buf=new StrBuilder(length).append(source,offset,length);
        this.substitute(buf,0,length);
        return buf.toString();
    }
    public String replace(final StringBuffer source){
        if(source==null){
            return null;
        }
        final StrBuilder buf=new StrBuilder(source.length()).append(source);
        this.substitute(buf,0,buf.length());
        return buf.toString();
    }
    public String replace(final StringBuffer source,final int offset,final int length){
        if(source==null){
            return null;
        }
        final StrBuilder buf=new StrBuilder(length).append(source,offset,length);
        this.substitute(buf,0,length);
        return buf.toString();
    }
    public String replace(final StrBuilder source){
        if(source==null){
            return null;
        }
        final StrBuilder buf=new StrBuilder(source.length()).append(source);
        this.substitute(buf,0,buf.length());
        return buf.toString();
    }
    public String replace(final StrBuilder source,final int offset,final int length){
        if(source==null){
            return null;
        }
        final StrBuilder buf=new StrBuilder(length).append(source,offset,length);
        this.substitute(buf,0,length);
        return buf.toString();
    }
    public String replace(final Object source){
        if(source==null){
            return null;
        }
        final StrBuilder buf=new StrBuilder().append(source);
        this.substitute(buf,0,buf.length());
        return buf.toString();
    }
    public boolean replaceIn(final StringBuffer source){
        return source!=null&&this.replaceIn(source,0,source.length());
    }
    public boolean replaceIn(final StringBuffer source,final int offset,final int length){
        if(source==null){
            return false;
        }
        final StrBuilder buf=new StrBuilder(length).append(source,offset,length);
        if(!this.substitute(buf,0,length)){
            return false;
        }
        source.replace(offset,offset+length,buf.toString());
        return true;
    }
    public boolean replaceIn(final StrBuilder source){
        return source!=null&&this.substitute(source,0,source.length());
    }
    public boolean replaceIn(final StrBuilder source,final int offset,final int length){
        return source!=null&&this.substitute(source,offset,length);
    }
    protected boolean substitute(final StrBuilder buf,final int offset,final int length){
        return this.substitute(buf,offset,length,null)>0;
    }
    private int substitute(final StrBuilder buf,final int offset,final int length,List priorVariables){
        final StrMatcher prefixMatcher=this.getVariablePrefixMatcher();
        final StrMatcher suffixMatcher=this.getVariableSuffixMatcher();
        final char escape=this.getEscapeChar();
        final boolean top=priorVariables==null;
        boolean altered=false;
        int lengthChange=0;
        char[] chars=buf.buffer;
        int bufEnd=offset+length;
        int pos=offset;
        while(pos<bufEnd){
            final int startMatchLen=prefixMatcher.isMatch(chars,pos,offset,bufEnd);
            if(startMatchLen==0){
                ++pos;
            }
            else if(pos>offset&&chars[pos-1]==escape){
                buf.deleteCharAt(pos-1);
                chars=buf.buffer;
                --lengthChange;
                altered=true;
                --bufEnd;
            }
            else{
                final int startPos=pos;
                pos+=startMatchLen;
                int endMatchLen=0;
                while(pos<bufEnd){
                    endMatchLen=suffixMatcher.isMatch(chars,pos,offset,bufEnd);
                    if(endMatchLen!=0){
                        final String varName=new String(chars,startPos+startMatchLen,pos-startPos-startMatchLen);
                        final int endPos;
                        pos=(endPos=pos+endMatchLen);
                        if(priorVariables==null){
                            priorVariables=new ArrayList<Object>();
                            priorVariables.add(new String(chars,offset,length));
                        }
                        this.checkCyclicSubstitution(varName,priorVariables);
                        priorVariables.add(varName);
                        final String varValue=this.resolveVariable(varName,buf,startPos,endPos);
                        if(varValue!=null){
                            final int varLen=varValue.length();
                            buf.replace(startPos,endPos,varValue);
                            altered=true;
                            int change=this.substitute(buf,startPos,varLen,priorVariables);
                            change+=varLen-(endPos-startPos);
                            pos+=change;
                            bufEnd+=change;
                            lengthChange+=change;
                            chars=buf.buffer;
                        }
                        priorVariables.remove(priorVariables.size()-1);
                        break;
                    }
                    ++pos;
                }
            }
        }
        if(top){
            return altered?1:0;
        }
        return lengthChange;
    }
    private void checkCyclicSubstitution(final String varName,final List priorVariables){
        if(!priorVariables.contains(varName)){
            return;
        }
        final StrBuilder buf=new StrBuilder(256);
        buf.append("Infinite loop in property interpolation of ");
        buf.append(priorVariables.remove(0));
        buf.append(": ");
        buf.appendWithSeparators(priorVariables,"->");
        throw new IllegalStateException(buf.toString());
    }
    protected String resolveVariable(final String variableName,final StrBuilder buf,final int startPos,final int endPos){
        final StrLookup resolver=this.getVariableResolver();
        if(resolver==null){
            return null;
        }
        return resolver.lookup(variableName);
    }
    public char getEscapeChar(){
        return this.escapeChar;
    }
    public void setEscapeChar(final char escapeCharacter){
        this.escapeChar=escapeCharacter;
    }
    public StrMatcher getVariablePrefixMatcher(){
        return this.prefixMatcher;
    }
    public StrSubstitutor setVariablePrefixMatcher(final StrMatcher prefixMatcher){
        if(prefixMatcher==null){
            throw new IllegalArgumentException("Variable prefix matcher must not be null!");
        }
        this.prefixMatcher=prefixMatcher;
        return this;
    }
    public StrSubstitutor setVariablePrefix(final char prefix){
        return this.setVariablePrefixMatcher(StrMatcher.charMatcher(prefix));
    }
    public StrSubstitutor setVariablePrefix(final String prefix){
        if(prefix==null){
            throw new IllegalArgumentException("Variable prefix must not be null!");
        }
        return this.setVariablePrefixMatcher(StrMatcher.stringMatcher(prefix));
    }
    public StrMatcher getVariableSuffixMatcher(){
        return this.suffixMatcher;
    }
    public StrSubstitutor setVariableSuffixMatcher(final StrMatcher suffixMatcher){
        if(suffixMatcher==null){
            throw new IllegalArgumentException("Variable suffix matcher must not be null!");
        }
        this.suffixMatcher=suffixMatcher;
        return this;
    }
    public StrSubstitutor setVariableSuffix(final char suffix){
        return this.setVariableSuffixMatcher(StrMatcher.charMatcher(suffix));
    }
    public StrSubstitutor setVariableSuffix(final String suffix){
        if(suffix==null){
            throw new IllegalArgumentException("Variable suffix must not be null!");
        }
        return this.setVariableSuffixMatcher(StrMatcher.stringMatcher(suffix));
    }
    public StrLookup getVariableResolver(){
        return this.variableResolver;
    }
    public void setVariableResolver(final StrLookup variableResolver){
        this.variableResolver=variableResolver;
    }
    static{
        DEFAULT_PREFIX=StrMatcher.stringMatcher("${");
        DEFAULT_SUFFIX=StrMatcher.stringMatcher("}");
    }
}
