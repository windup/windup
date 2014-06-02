package org.apache.wicket.application;

import java.util.*;

public class WildcardMatcherHelper{
    public static final char ESC='\\';
    public static final char PATHSEP='.';
    public static final char STAR='*';
    public static Map<String,String> match(final String pat,final String str){
        final Matcher map=new Matcher(pat,str);
        if(map.isMatch()){
            return map.getMap();
        }
        return null;
    }
    private static class Matcher{
        private final char[] apat;
        private final int lpat;
        private final char[] astr;
        private final int lstr;
        private final Map<String,String> map;
        private final boolean matched;
        private int idx;
        private int ipat;
        private int istr;
        public Matcher(final String pat,final String str){
            super();
            this.map=(Map<String,String>)new HashMap();
            this.idx=0;
            this.ipat=0;
            this.istr=0;
            this.apat=pat.toCharArray();
            this.lpat=this.apat.length;
            this.astr=str.toCharArray();
            this.lstr=this.astr.length;
            this.add(str);
            this.matched=this.match();
        }
        public Map<String,String> getMap(){
            return this.map;
        }
        public boolean isMatch(){
            return this.matched;
        }
        private void add(final String aStr){
            this.map.put(String.valueOf(this.idx++),aStr);
        }
        private boolean match(){
            this.scanLiteralPrefix();
            if(this.ipat>=this.lpat&&this.istr>=this.lstr){
                return true;
            }
            if(this.ipat<this.lpat&&this.istr>=this.lstr){
                while(this.ipat<this.lpat&&this.apat[this.ipat]=='*'){
                    ++this.ipat;
                }
                if(this.ipat>=this.lpat){
                    this.add("");
                    return true;
                }
                return false;
            }
            else{
                if(this.ipat>=this.lpat&&this.istr<this.lstr){
                    return false;
                }
                if(this.apat[this.ipat]!='*'){
                    return false;
                }
                if(this.ipat<this.lpat-1&&this.apat[this.ipat+1]=='*'){
                    while(++this.ipat<this.lpat&&this.apat[this.ipat]=='*'){
                    }
                    if(this.ipat>=this.lpat){
                        this.add(new String(this.astr,this.istr,this.lstr-this.istr));
                        return true;
                    }
                    final int sipat=this.ipat;
                    while(this.ipat<this.lpat&&(this.apat[this.ipat]!='*'||(this.ipat>0&&this.apat[this.ipat-1]=='\\'))){
                        ++this.ipat;
                    }
                    if(this.ipat>=this.lpat){
                        return this.checkEnds(sipat,false);
                    }
                    int l;
                    int eistr;
                    for(l=this.ipat-sipat,eistr=this.lstr-l;this.istr<eistr&&!this.strncmp(this.apat,sipat,this.astr,eistr,l);--eistr){
                    }
                    if(this.istr>=eistr){
                        return false;
                    }
                    this.add(new String(this.astr,this.istr,eistr-this.istr));
                    this.istr=eistr+l;
                }
                else{
                    ++this.ipat;
                    if(this.ipat>=this.lpat){
                        final int sistr=this.istr;
                        while(this.istr<this.lstr&&this.astr[this.istr]!='.'){
                            ++this.istr;
                        }
                        if(this.istr>=this.lstr){
                            this.add(new String(this.astr,sistr,this.lstr-sistr));
                            return true;
                        }
                        return false;
                    }
                    else{
                        final int sipat=this.ipat;
                        while(this.ipat<this.lpat&&this.apat[this.ipat]!='*'&&(this.apat[this.ipat]!='\\'||(this.ipat<this.lpat-1&&this.apat[this.ipat+1]!='*'))&&this.apat[this.ipat]!='.'){
                            ++this.ipat;
                        }
                        if(this.ipat>=this.lpat){
                            return this.checkEnds(sipat,true);
                        }
                        if(this.apat[this.ipat]=='*'){
                            --this.ipat;
                        }
                        final int l=this.ipat-sipat+1;
                        final int sistr2=this.istr;
                        while(this.istr<this.lstr&&!this.strncmp(this.apat,sipat,this.astr,this.istr,l)){
                            ++this.istr;
                        }
                        if(this.istr>=this.lstr){
                            return false;
                        }
                        this.add(new String(this.astr,sistr2,this.istr-sistr2));
                        ++this.ipat;
                        this.istr+=l;
                    }
                }
                return this.match();
            }
        }
        private final void scanLiteralPrefix(){
            while(this.ipat<this.lpat&&this.istr<this.lstr&&((this.apat[this.ipat]=='\\'&&this.ipat<this.lpat-1&&this.apat[this.ipat+1]=='*'&&this.apat[++this.ipat]==this.astr[this.istr])||(this.apat[this.ipat]!='*'&&this.apat[this.ipat]==this.astr[this.istr]))){
                ++this.ipat;
                ++this.istr;
            }
        }
        private final boolean strncmp(final char[] a1,final int o1,final char[] a2,final int o2,final int l){
            int i;
            for(i=0;i<l&&o1+i<a1.length&&o2+i<a2.length&&a1[o1+i]==a2[o2+i];++i){
            }
            return i==l;
        }
        private final boolean checkEnds(final int sipat,final boolean isSingleStart){
            final int l=this.lpat-sipat;
            final int ostr=this.lstr-l;
            if(ostr>=0&&this.strncmp(this.apat,sipat,this.astr,ostr,l)){
                if(isSingleStart){
                    int i=ostr-this.istr;
                    while(i>this.istr){
                        if(this.astr[--i]=='.'){
                            return false;
                        }
                    }
                }
                this.add(new String(this.astr,this.istr,ostr-this.istr));
                return true;
            }
            return false;
        }
    }
}
