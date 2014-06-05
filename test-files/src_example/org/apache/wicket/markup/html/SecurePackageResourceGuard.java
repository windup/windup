package org.apache.wicket.markup.html;

import org.apache.wicket.util.collections.*;
import java.util.*;
import org.slf4j.*;
import java.util.regex.*;
import org.apache.wicket.util.string.*;
import java.util.concurrent.*;

public class SecurePackageResourceGuard extends PackageResourceGuard{
    private static final Logger log;
    private static final char PATH_SEPARATOR='/';
    private List<SearchPattern> pattern;
    private final ConcurrentMap<String,Boolean> cache;
    public SecurePackageResourceGuard(){
        this(new SimpleCache(100));
    }
    public SecurePackageResourceGuard(final ConcurrentMap<String,Boolean> cache){
        super();
        this.pattern=(List<SearchPattern>)new ArrayList();
        this.cache=cache;
        this.addPattern("+*.js");
        this.addPattern("+*.css");
        this.addPattern("+*.png");
        this.addPattern("+*.jpg");
        this.addPattern("+*.jpeg");
        this.addPattern("+*.gif");
        this.addPattern("+*.ico");
        this.addPattern("+*.cur");
        this.addPattern("+*.html");
        this.addPattern("+*.txt");
        this.addPattern("+*.swf");
        this.addPattern("+*.bmp");
        this.addPattern("+*.svg");
    }
    @Deprecated
    public ConcurrentHashMap<String,Boolean> newCache(){
        return new SimpleCache(100);
    }
    public void clearCache(){
        if(this.cache!=null){
            this.cache.clear();
        }
    }
    protected boolean acceptAbsolutePath(final String path){
        if(this.cache!=null){
            final Boolean rtn=(Boolean)this.cache.get(path);
            if(rtn!=null){
                return rtn;
            }
        }
        if(!super.acceptAbsolutePath(path)){
            return false;
        }
        boolean hit=false;
        for(final SearchPattern pattern : new ReverseListIterator((List)this.pattern)){
            if(pattern!=null&&pattern.isActive()&&pattern.matches(path)){
                hit=pattern.isInclude();
                break;
            }
        }
        if(this.cache!=null){
            this.cache.put(path,hit?Boolean.TRUE:Boolean.FALSE);
        }
        if(!hit){
            SecurePackageResourceGuard.log.warn("Access denied to shared (static) resource: "+path);
        }
        return hit;
    }
    public List<SearchPattern> getPattern(){
        this.clearCache();
        return this.pattern;
    }
    public void setPattern(final List<SearchPattern> pattern){
        this.pattern=pattern;
        this.clearCache();
    }
    public void addPattern(final String pattern){
        this.pattern.add(new SearchPattern(pattern));
        this.clearCache();
    }
    static{
        log=LoggerFactory.getLogger(SecurePackageResourceGuard.class);
    }
    public static class SearchPattern{
        private String pattern;
        private Pattern regex;
        private boolean include;
        private boolean active;
        private boolean fileOnly;
        public SearchPattern(final String pattern){
            super();
            this.active=true;
            this.setPattern(pattern);
        }
        private Pattern convertToRegex(final String pattern){
            String regex=Strings.replaceAll((CharSequence)pattern,(CharSequence)".",(CharSequence)"#dot#").toString();
            regex=regex.replaceAll("^\\*/","[^/]+/");
            regex=regex.replaceAll("^[\\*]{2,}/","([^/].#star#/)?");
            regex=regex.replaceAll("/\\*/","/[^/]+/");
            regex=regex.replaceAll("/[\\*]{2,}/","(/|/.+/)");
            regex=regex.replaceAll("\\*+","[^/]*");
            regex=Strings.replaceAll((CharSequence)regex,(CharSequence)"#dot#",(CharSequence)"\\.").toString();
            regex=Strings.replaceAll((CharSequence)regex,(CharSequence)"#star#",(CharSequence)"*").toString();
            return Pattern.compile(regex);
        }
        public String getPattern(){
            return this.pattern;
        }
        public Pattern getRegex(){
            return this.regex;
        }
        public void setPattern(final String pattern){
            if(Strings.isEmpty((CharSequence)pattern)){
                throw new IllegalArgumentException("Parameter 'pattern' can not be null or an empty string");
            }
            if(pattern.charAt(0)=='+'){
                this.include=true;
            }
            else{
                if(pattern.charAt(0)!='-'){
                    throw new IllegalArgumentException("Parameter 'pattern' must start with either '+' or '-'. pattern='"+pattern+"'");
                }
                this.include=false;
            }
            this.pattern=pattern;
            this.regex=this.convertToRegex(pattern.substring(1));
            this.fileOnly=(pattern.indexOf(47)==-1);
        }
        public boolean matches(String path){
            if(this.fileOnly){
                path=Strings.lastPathComponent(path,'/');
            }
            return this.regex.matcher((CharSequence)path).matches();
        }
        public boolean isInclude(){
            return this.include;
        }
        public void setInclude(final boolean include){
            this.include=include;
        }
        public boolean isActive(){
            return this.active;
        }
        public void setActive(final boolean active){
            this.active=active;
        }
        public String toString(){
            return "Pattern: "+this.pattern+", Regex: "+this.regex+", include:"+this.include+", fileOnly:"+this.fileOnly+", active:"+this.active;
        }
    }
    public static class SimpleCache extends ConcurrentHashMap<String,Boolean>{
        private static final long serialVersionUID=1L;
        private final ConcurrentLinkedQueue<String> fifo;
        private final int maxSize;
        public SimpleCache(final int maxSize){
            super();
            this.fifo=new ConcurrentLinkedQueue<String>();
            this.maxSize=maxSize;
        }
        public Boolean put(final String key,final Boolean value){
            final Boolean rtn=super.putIfAbsent(key,value);
            if(rtn!=null){
                this.fifo.remove(key);
            }
            this.fifo.add(key);
            while(this.fifo.size()>this.maxSize){
                this.remove(this.fifo.poll());
            }
            return rtn;
        }
    }
}
