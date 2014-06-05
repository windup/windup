package org.apache.wicket.validation.validator;

import java.util.*;
import org.apache.wicket.validation.*;
import java.util.regex.*;

public class UrlValidator extends AbstractValidator<String>{
    private static final long serialVersionUID=1L;
    public static final int ALLOW_ALL_SCHEMES=1;
    public static final int ALLOW_2_SLASHES=2;
    public static final int NO_FRAGMENTS=4;
    private static final String ALPHA_CHARS="a-zA-Z";
    private static final String ALPHA_NUMERIC_CHARS="a-zA-Z\\d";
    private static final String SPECIAL_CHARS=";/@&=,.?:+$";
    private static final String VALID_CHARS="[^\\s;/@&=,.?:+$]";
    private static final String SCHEME_CHARS="a-zA-Z";
    private static final String AUTHORITY_CHARS="a-zA-Z\\d\\-\\.";
    private static final String ATOM="[^\\s;/@&=,.?:+$]+";
    private static final String URL_PATTERN="^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";
    private static final int PARSE_URL_SCHEME=2;
    private static final int PARSE_URL_AUTHORITY=4;
    private static final int PARSE_URL_PATH=5;
    private static final int PARSE_URL_QUERY=7;
    private static final int PARSE_URL_FRAGMENT=9;
    private static final String SCHEME_PATTERN="^[a-zA-Z].*$";
    private static final String AUTHORITY_PATTERN="^(.+(:.*)?@)?([a-zA-Z\\d\\-\\.]*)(:\\d*)?(.*)?";
    private static final int PARSE_AUTHORITY_HOST_IP=3;
    private static final int PARSE_AUTHORITY_PORT=4;
    private static final int PARSE_AUTHORITY_EXTRA=5;
    private static final String PATH_PATTERN="^(/[-\\w:@&?=+,.!/~*'%$_;]*)?$";
    private static final String QUERY_PATTERN="^(.*)$";
    private static final String LEGAL_ASCII_PATTERN="^[\\x00-\\x7F]+$";
    private static final String IP_V4_DOMAIN_PATTERN="^(\\d{1,3})[.](\\d{1,3})[.](\\d{1,3})[.](\\d{1,3})$";
    private static final String DOMAIN_PATTERN="^[^\\s;/@&=,.?:+$]+(\\.[^\\s;/@&=,.?:+$]+)*$";
    private static final String PORT_PATTERN="^:(\\d{1,5})$";
    private static final String ATOM_PATTERN="([^\\s;/@&=,.?:+$]+)";
    private static final String ALPHA_PATTERN="^[a-zA-Z]";
    private long options;
    private final Set<String> allowedSchemes;
    protected String[] defaultSchemes;
    public UrlValidator(){
        this(null);
    }
    public UrlValidator(final String[] schemes){
        this(schemes,0);
    }
    public UrlValidator(final int options){
        this(null,options);
    }
    public UrlValidator(String[] schemes,final int options){
        super();
        this.options=0L;
        this.allowedSchemes=(Set<String>)new HashSet();
        this.defaultSchemes=new String[] { "http","https","ftp" };
        this.options=options;
        if(this.isOn(1L)){
            return;
        }
        if(schemes==null){
            schemes=this.defaultSchemes;
        }
        this.allowedSchemes.addAll(Arrays.asList(schemes));
    }
    protected void onValidate(final IValidatable<String> validatable){
        final String url=validatable.getValue();
        if(url!=null&&!this.isValid(url)){
            this.error(validatable);
        }
    }
    public final boolean isValid(final String value){
        if(value==null){
            return false;
        }
        final Matcher matchAsciiPat=Pattern.compile("^[\\x00-\\x7F]+$").matcher((CharSequence)value);
        if(!matchAsciiPat.matches()){
            return false;
        }
        final Matcher matchUrlPat=Pattern.compile("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?").matcher((CharSequence)value);
        return matchUrlPat.matches()&&this.isValidScheme(matchUrlPat.group(2))&&this.isValidAuthority(matchUrlPat.group(4))&&this.isValidPath(matchUrlPat.group(5))&&this.isValidQuery(matchUrlPat.group(7))&&this.isValidFragment(matchUrlPat.group(9));
    }
    protected boolean isValidScheme(final String scheme){
        return scheme!=null&&Pattern.compile("^[a-zA-Z].*$").matcher((CharSequence)scheme).matches()&&(!this.isOff(1L)||this.allowedSchemes.contains(scheme));
    }
    protected boolean isValidAuthority(final String authority){
        if(authority==null){
            return false;
        }
        final Matcher authorityMatcher=Pattern.compile("^(.+(:.*)?@)?([a-zA-Z\\d\\-\\.]*)(:\\d*)?(.*)?").matcher((CharSequence)authority);
        if(!authorityMatcher.matches()){
            return false;
        }
        boolean ipV4Address=false;
        boolean hostname=false;
        String hostIP=authorityMatcher.group(3);
        final Matcher matchIPV4Pat=Pattern.compile("^(\\d{1,3})[.](\\d{1,3})[.](\\d{1,3})[.](\\d{1,3})$").matcher((CharSequence)hostIP);
        ipV4Address=matchIPV4Pat.matches();
        if(ipV4Address){
            for(int i=1;i<=4;++i){
                final String ipSegment=matchIPV4Pat.group(i);
                if(ipSegment==null||ipSegment.length()<=0){
                    return false;
                }
                try{
                    if(Integer.parseInt(ipSegment)>255){
                        return false;
                    }
                }
                catch(NumberFormatException e){
                    return false;
                }
            }
        }
        else{
            hostname=Pattern.compile("^[^\\s;/@&=,.?:+$]+(\\.[^\\s;/@&=,.?:+$]+)*$").matcher((CharSequence)hostIP).matches();
        }
        if(hostname){
            final char[] chars=hostIP.toCharArray();
            int size=1;
            for(final char ch : chars){
                if(ch=='.'){
                    ++size;
                }
            }
            final String[] domainSegment=new String[size];
            boolean match=true;
            int segmentCount=0;
            int segmentLength=0;
            while(match){
                final Matcher atomMatcher=Pattern.compile("([^\\s;/@&=,.?:+$]+)").matcher((CharSequence)hostIP);
                match=atomMatcher.find();
                if(match){
                    domainSegment[segmentCount]=atomMatcher.group(1);
                    segmentLength=domainSegment[segmentCount].length()+1;
                    hostIP=((segmentLength>=hostIP.length())?"":hostIP.substring(segmentLength));
                    ++segmentCount;
                }
            }
            if(segmentCount>1){
                final String topLevel=domainSegment[segmentCount-1];
                if(topLevel.length()<2){
                    return false;
                }
                final Matcher alphaMatcher=Pattern.compile("^[a-zA-Z]").matcher((CharSequence)topLevel.substring(0,1));
                if(!alphaMatcher.matches()){
                    return false;
                }
            }
        }
        if(!hostname&&!ipV4Address){
            return false;
        }
        final String port=authorityMatcher.group(4);
        if(port!=null){
            final Matcher portMatcher=Pattern.compile("^:(\\d{1,5})$").matcher((CharSequence)port);
            if(!portMatcher.matches()){
                return false;
            }
        }
        final String extra=authorityMatcher.group(5);
        return isBlankOrNull(extra);
    }
    protected boolean isValidPath(final String path){
        if(path==null){
            return false;
        }
        final Matcher pathMatcher=Pattern.compile("^(/[-\\w:@&?=+,.!/~*'%$_;]*)?$").matcher((CharSequence)path);
        if(!pathMatcher.matches()){
            return false;
        }
        final int slash2Count=this.countToken("//",path);
        if(this.isOff(2L)&&slash2Count>0){
            return false;
        }
        final int slashCount=this.countToken("/",path);
        final int dot2Count=this.countToken("/..",path);
        return dot2Count<=0||slashCount-slash2Count-1>dot2Count;
    }
    protected boolean isValidQuery(final String query){
        if(query==null){
            return true;
        }
        final Matcher queryMatcher=Pattern.compile("^(.*)$").matcher((CharSequence)query);
        return queryMatcher.matches();
    }
    protected boolean isValidFragment(final String fragment){
        return fragment==null||this.isOff(4L);
    }
    protected int countToken(final String token,final String target){
        int tokenIndex;
        int count;
        for(tokenIndex=0,count=0;tokenIndex!=-1;++tokenIndex,++count){
            tokenIndex=target.indexOf(token,tokenIndex);
            if(tokenIndex>-1){
            }
        }
        return count;
    }
    public static boolean isBlankOrNull(final String value){
        return value==null||value.trim().length()==0;
    }
    public boolean isOn(final long flag){
        return (this.options&flag)>0L;
    }
    public boolean isOff(final long flag){
        return (this.options&flag)==0x0L;
    }
}
