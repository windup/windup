package org.apache.wicket.util.tester;

import java.net.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.util.io.*;
import org.apache.wicket.util.string.*;
import org.apache.wicket.util.diff.*;
import junit.framework.*;
import java.io.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.util.resource.*;
import org.slf4j.*;

public final class DiffUtil{
    private static final Logger log;
    private static final String ENCODING="UTF-8";
    public static final void replaceExpectedResultFile(final String document,final Class<?> clazz,final String file) throws IOException{
        String filename=clazz.getPackage().getName();
        filename=filename.replace('.','/');
        filename=filename+"/"+file;
        final URL url=clazz.getClassLoader().getResource(filename);
        filename=url.getFile();
        filename=filename.replaceAll("/target/test-classes/","/src/test/java/");
        final PrintWriter out=new PrintWriter(new FileOutputStream(filename));
        out.print(document);
        out.close();
    }
    public static final boolean validatePage(String document,final Class<?> clazz,final String file,final boolean failWithAssert) throws IOException{
        Args.notNull((Object)document,"document");
        String filename=clazz.getPackage().getName();
        filename=filename.replace('.','/');
        filename=filename+"/"+file;
        InputStream in=clazz.getClassLoader().getResourceAsStream(filename);
        if(in==null){
            throw new IOException("File not found: "+filename);
        }
        String reference=Streams.readString(in,(CharSequence)"UTF-8");
        reference=reference.replaceAll("\n\r","\n");
        reference=reference.replaceAll("\r\n","\n");
        document=document.replaceAll("\n\r","\n");
        document=document.replaceAll("\r\n","\n");
        final boolean equals=compareMarkup(document,reference);
        if(!equals){
            if(Boolean.getBoolean("wicket.replace.expected.results")){
                in.close();
                in=null;
                replaceExpectedResultFile(document,clazz,file);
                return true;
            }
            DiffUtil.log.error("File name: "+file);
            DiffUtil.log.error("===================");
            DiffUtil.log.error(reference);
            DiffUtil.log.error("===================");
            DiffUtil.log.error(document);
            DiffUtil.log.error("===================");
            final String[] test1=StringList.tokenize(reference,"\n").toArray();
            final String[] test2=StringList.tokenize(document,"\n").toArray();
            final Diff df=new Diff((Object[])test1);
            try{
                df.diff((Object[])test2);
            }
            catch(DifferentiationFailedException e){
                throw new RuntimeException((Throwable)e);
            }
            if(failWithAssert){
                Assert.assertEquals(filename,reference,document);
            }
        }
        return equals;
    }
    private static boolean compareMarkup(final String a,final String b){
        try{
            final MarkupStream amarkup=new MarkupStream(new MarkupParser(a).parse());
            final MarkupStream bmarkup=new MarkupStream(new MarkupParser(b).parse());
            return amarkup.equalTo(bmarkup);
        }
        catch(IOException e){
            DiffUtil.log.error(e.getMessage(),e);
        }
        catch(ResourceStreamNotFoundException e2){
            DiffUtil.log.error(e2.getMessage(),(Throwable)e2);
        }
        return false;
    }
    static{
        log=LoggerFactory.getLogger(DiffUtil.class);
    }
}
