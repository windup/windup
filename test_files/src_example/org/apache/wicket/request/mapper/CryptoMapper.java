package org.apache.wicket.request.mapper;

import org.apache.wicket.util.*;
import org.apache.wicket.util.crypt.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.request.*;
import org.apache.wicket.util.string.*;
import java.util.*;
import org.slf4j.*;

public class CryptoMapper implements IRequestMapper{
    private static final Logger log;
    private final IRequestMapper wrappedMapper;
    private final IProvider<ICrypt> cryptProvider;
    public CryptoMapper(final IRequestMapper wrappedMapper,final Application application){
        this(wrappedMapper,(IProvider<ICrypt>)new ApplicationCryptProvider(application));
    }
    public CryptoMapper(final IRequestMapper wrappedMapper,final IProvider<ICrypt> cryptProvider){
        super();
        this.wrappedMapper=(IRequestMapper)Args.notNull((Object)wrappedMapper,"wrappedMapper");
        this.cryptProvider=(IProvider<ICrypt>)Args.notNull((Object)cryptProvider,"cryptProvider");
    }
    public int getCompatibilityScore(final Request request){
        return this.wrappedMapper.getCompatibilityScore(request);
    }
    public Url mapHandler(final IRequestHandler requestHandler){
        final Url url=this.wrappedMapper.mapHandler(requestHandler);
        if(url==null){
            return null;
        }
        return this.encryptUrl(url);
    }
    public IRequestHandler mapRequest(final Request request){
        final Url url=this.decryptUrl(request,request.getUrl());
        if(url==null){
            return this.wrappedMapper.mapRequest(request);
        }
        return this.wrappedMapper.mapRequest(request.cloneWithUrl(url));
    }
    protected final ICrypt getCrypt(){
        return (ICrypt)this.cryptProvider.get();
    }
    protected final IRequestMapper getWrappedMapper(){
        return this.wrappedMapper;
    }
    private Url encryptUrl(final Url url){
        if(url.getSegments().isEmpty()&&url.getQueryParameters().isEmpty()){
            return url;
        }
        final String encryptedUrlString=this.getCrypt().encryptUrlSafe(url.toString());
        final Url encryptedUrl=new Url(url.getCharset());
        encryptedUrl.getSegments().add(encryptedUrlString);
        final int numberOfSegments=url.getSegments().size();
        final HashedSegmentGenerator generator=new HashedSegmentGenerator(encryptedUrlString);
        for(int segNo=0;segNo<numberOfSegments;++segNo){
            encryptedUrl.getSegments().add(generator.next());
        }
        return encryptedUrl;
    }
    private Url decryptUrl(final Request request,final Url encryptedUrl){
        if(encryptedUrl.getSegments().isEmpty()){
            return encryptedUrl;
        }
        final List<String> encryptedSegments=(List<String>)encryptedUrl.getSegments();
        if(encryptedSegments.size()<1){
            return null;
        }
        Url url=new Url(request.getCharset());
        try{
            final String encryptedUrlString=(String)encryptedSegments.get(0);
            if(Strings.isEmpty((CharSequence)encryptedUrlString)){
                return null;
            }
            final String decryptedUrl=this.getCrypt().decryptUrlSafe(encryptedUrlString);
            if(decryptedUrl==null){
                return null;
            }
            final Url originalUrl=Url.parse(decryptedUrl,request.getCharset());
            final int originalNumberOfSegments=originalUrl.getSegments().size();
            final int encryptedNumberOfSegments=encryptedUrl.getSegments().size();
            final HashedSegmentGenerator generator=new HashedSegmentGenerator(encryptedUrlString);
            int segNo;
            for(segNo=1;segNo<encryptedNumberOfSegments;++segNo){
                if(segNo>originalNumberOfSegments){
                    break;
                }
                final String next=generator.next();
                final String encryptedSegment=(String)encryptedSegments.get(segNo);
                if(!next.equals(encryptedSegment)){
                    break;
                }
                url.getSegments().add(originalUrl.getSegments().get(segNo-1));
            }
            while(segNo<encryptedNumberOfSegments){
                url.getSegments().add(encryptedUrl.getSegments().get(segNo));
                ++segNo;
            }
            url.getQueryParameters().addAll(originalUrl.getQueryParameters());
        }
        catch(Exception e){
            CryptoMapper.log.error("Error decrypting URL",e);
            url=null;
        }
        return url;
    }
    static{
        log=LoggerFactory.getLogger(CryptoMapper.class);
    }
    private static class ApplicationCryptProvider implements IProvider<ICrypt>{
        private final Application application;
        public ApplicationCryptProvider(final Application application){
            super();
            this.application=application;
        }
        public ICrypt get(){
            return this.application.getSecuritySettings().getCryptFactory().newCrypt();
        }
    }
    private static class HashedSegmentGenerator{
        private char[] characters;
        private int hash;
        public HashedSegmentGenerator(final String string){
            super();
            this.hash=0;
            this.characters=string.toCharArray();
        }
        public String next(){
            final char a=this.characters[Math.abs(this.hash%this.characters.length)];
            ++this.hash;
            final char b=this.characters[Math.abs(this.hash%this.characters.length)];
            ++this.hash;
            final char c=this.characters[Math.abs(this.hash%this.characters.length)];
            String segment=""+a+b+c;
            this.hash=this.hashString(segment);
            segment+=String.format("%02x",new Object[] { Math.abs(this.hash%256) });
            this.hash=this.hashString(segment);
            return segment;
        }
        private int hashString(final String str){
            int hash=97;
            for(final int i : str.toCharArray()){
                final char c=(char)i;
                hash=47*hash+i;
            }
            return hash;
        }
    }
}
