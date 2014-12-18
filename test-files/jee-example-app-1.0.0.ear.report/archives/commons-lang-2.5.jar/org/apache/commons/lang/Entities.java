package org.apache.commons.lang;

import java.util.TreeMap;
import java.util.HashMap;
import org.apache.commons.lang.IntHashMap;
import java.util.Map;
import java.io.StringWriter;
import java.io.IOException;
import org.apache.commons.lang.UnhandledException;
import java.io.Writer;

class Entities{
    private static final String[][] BASIC_ARRAY;
    private static final String[][] APOS_ARRAY;
    static final String[][] ISO8859_1_ARRAY;
    static final String[][] HTML40_ARRAY;
    public static final Entities XML;
    public static final Entities HTML32;
    public static final Entities HTML40;
    private final EntityMap map;
    static void fillWithHtml40Entities(final Entities entities){
        entities.addEntities(Entities.BASIC_ARRAY);
        entities.addEntities(Entities.ISO8859_1_ARRAY);
        entities.addEntities(Entities.HTML40_ARRAY);
    }
    public Entities(){
        super();
        this.map=new LookupEntityMap();
    }
    Entities(final EntityMap emap){
        super();
        this.map=emap;
    }
    public void addEntities(final String[][] entityArray){
        for(int i=0;i<entityArray.length;++i){
            this.addEntity(entityArray[i][0],Integer.parseInt(entityArray[i][1]));
        }
    }
    public void addEntity(final String name,final int value){
        this.map.add(name,value);
    }
    public String entityName(final int value){
        return this.map.name(value);
    }
    public int entityValue(final String name){
        return this.map.value(name);
    }
    public String escape(final String str){
        final StringWriter stringWriter=this.createStringWriter(str);
        try{
            this.escape(stringWriter,str);
        }
        catch(IOException e){
            throw new UnhandledException(e);
        }
        return stringWriter.toString();
    }
    public void escape(final Writer writer,final String str) throws IOException{
        for(int len=str.length(),i=0;i<len;++i){
            final char c=str.charAt(i);
            final String entityName=this.entityName(c);
            if(entityName==null){
                if(c>'\u007f'){
                    writer.write("&#");
                    writer.write(Integer.toString(c,10));
                    writer.write(59);
                }
                else{
                    writer.write(c);
                }
            }
            else{
                writer.write(38);
                writer.write(entityName);
                writer.write(59);
            }
        }
    }
    public String unescape(final String str){
        final int firstAmp=str.indexOf(38);
        if(firstAmp<0){
            return str;
        }
        final StringWriter stringWriter=this.createStringWriter(str);
        try{
            this.doUnescape(stringWriter,str,firstAmp);
        }
        catch(IOException e){
            throw new UnhandledException(e);
        }
        return stringWriter.toString();
    }
    private StringWriter createStringWriter(final String str){
        return new StringWriter((int)(str.length()+str.length()*0.1));
    }
    public void unescape(final Writer writer,final String str) throws IOException{
        final int firstAmp=str.indexOf(38);
        if(firstAmp<0){
            writer.write(str);
            return;
        }
        this.doUnescape(writer,str,firstAmp);
    }
    private void doUnescape(final Writer writer,final String str,final int firstAmp) throws IOException{
        writer.write(str,0,firstAmp);
        for(int len=str.length(),i=firstAmp;i<len;++i){
            final char c=str.charAt(i);
            if(c=='&'){
                final int nextIdx=i+1;
                final int semiColonIdx=str.indexOf(59,nextIdx);
                if(semiColonIdx==-1){
                    writer.write(c);
                }
                else{
                    final int amphersandIdx=str.indexOf(38,i+1);
                    if(amphersandIdx!=-1&&amphersandIdx<semiColonIdx){
                        writer.write(c);
                    }
                    else{
                        final String entityContent=str.substring(nextIdx,semiColonIdx);
                        int entityValue=-1;
                        final int entityContentLen=entityContent.length();
                        if(entityContentLen>0){
                            if(entityContent.charAt(0)=='#'){
                                if(entityContentLen>1){
                                    final char isHexChar=entityContent.charAt(1);
                                    try{
                                        switch(isHexChar){
                                            case 'X':
                                            case 'x':{
                                                entityValue=Integer.parseInt(entityContent.substring(2),16);
                                                break;
                                            }
                                            default:{
                                                entityValue=Integer.parseInt(entityContent.substring(1),10);
                                                break;
                                            }
                                        }
                                        if(entityValue>65535){
                                            entityValue=-1;
                                        }
                                    }
                                    catch(NumberFormatException e){
                                        entityValue=-1;
                                    }
                                }
                            }
                            else{
                                entityValue=this.entityValue(entityContent);
                            }
                        }
                        if(entityValue==-1){
                            writer.write(38);
                            writer.write(entityContent);
                            writer.write(59);
                        }
                        else{
                            writer.write(entityValue);
                        }
                        i=semiColonIdx;
                    }
                }
            }
            else{
                writer.write(c);
            }
        }
    }
    static{
        BASIC_ARRAY=new String[][] { { "quot","34" }, { "amp","38" }, { "lt","60" }, { "gt","62" } };
        APOS_ARRAY=new String[][] { { "apos","39" } };
        ISO8859_1_ARRAY=new String[][] { { "nbsp","160" }, { "iexcl","161" }, { "cent","162" }, { "pound","163" }, { "curren","164" }, { "yen","165" }, { "brvbar","166" }, { "sect","167" }, { "uml","168" }, { "copy","169" }, { "ordf","170" }, { "laquo","171" }, { "not","172" }, { "shy","173" }, { "reg","174" }, { "macr","175" }, { "deg","176" }, { "plusmn","177" }, { "sup2","178" }, { "sup3","179" }, { "acute","180" }, { "micro","181" }, { "para","182" }, { "middot","183" }, { "cedil","184" }, { "sup1","185" }, { "ordm","186" }, { "raquo","187" }, { "frac14","188" }, { "frac12","189" }, { "frac34","190" }, { "iquest","191" }, { "Agrave","192" }, { "Aacute","193" }, { "Acirc","194" }, { "Atilde","195" }, { "Auml","196" }, { "Aring","197" }, { "AElig","198" }, { "Ccedil","199" }, { "Egrave","200" }, { "Eacute","201" }, { "Ecirc","202" }, { "Euml","203" }, { "Igrave","204" }, { "Iacute","205" }, { "Icirc","206" }, { "Iuml","207" }, { "ETH","208" }, { "Ntilde","209" }, { "Ograve","210" }, { "Oacute","211" }, { "Ocirc","212" }, { "Otilde","213" }, { "Ouml","214" }, { "times","215" }, { "Oslash","216" }, { "Ugrave","217" }, { "Uacute","218" }, { "Ucirc","219" }, { "Uuml","220" }, { "Yacute","221" }, { "THORN","222" }, { "szlig","223" }, { "agrave","224" }, { "aacute","225" }, { "acirc","226" }, { "atilde","227" }, { "auml","228" }, { "aring","229" }, { "aelig","230" }, { "ccedil","231" }, { "egrave","232" }, { "eacute","233" }, { "ecirc","234" }, { "euml","235" }, { "igrave","236" }, { "iacute","237" }, { "icirc","238" }, { "iuml","239" }, { "eth","240" }, { "ntilde","241" }, { "ograve","242" }, { "oacute","243" }, { "ocirc","244" }, { "otilde","245" }, { "ouml","246" }, { "divide","247" }, { "oslash","248" }, { "ugrave","249" }, { "uacute","250" }, { "ucirc","251" }, { "uuml","252" }, { "yacute","253" }, { "thorn","254" }, { "yuml","255" } };
        HTML40_ARRAY=new String[][] { { "fnof","402" }, { "Alpha","913" }, { "Beta","914" }, { "Gamma","915" }, { "Delta","916" }, { "Epsilon","917" }, { "Zeta","918" }, { "Eta","919" }, { "Theta","920" }, { "Iota","921" }, { "Kappa","922" }, { "Lambda","923" }, { "Mu","924" }, { "Nu","925" }, { "Xi","926" }, { "Omicron","927" }, { "Pi","928" }, { "Rho","929" }, { "Sigma","931" }, { "Tau","932" }, { "Upsilon","933" }, { "Phi","934" }, { "Chi","935" }, { "Psi","936" }, { "Omega","937" }, { "alpha","945" }, { "beta","946" }, { "gamma","947" }, { "delta","948" }, { "epsilon","949" }, { "zeta","950" }, { "eta","951" }, { "theta","952" }, { "iota","953" }, { "kappa","954" }, { "lambda","955" }, { "mu","956" }, { "nu","957" }, { "xi","958" }, { "omicron","959" }, { "pi","960" }, { "rho","961" }, { "sigmaf","962" }, { "sigma","963" }, { "tau","964" }, { "upsilon","965" }, { "phi","966" }, { "chi","967" }, { "psi","968" }, { "omega","969" }, { "thetasym","977" }, { "upsih","978" }, { "piv","982" }, { "bull","8226" }, { "hellip","8230" }, { "prime","8242" }, { "Prime","8243" }, { "oline","8254" }, { "frasl","8260" }, { "weierp","8472" }, { "image","8465" }, { "real","8476" }, { "trade","8482" }, { "alefsym","8501" }, { "larr","8592" }, { "uarr","8593" }, { "rarr","8594" }, { "darr","8595" }, { "harr","8596" }, { "crarr","8629" }, { "lArr","8656" }, { "uArr","8657" }, { "rArr","8658" }, { "dArr","8659" }, { "hArr","8660" }, { "forall","8704" }, { "part","8706" }, { "exist","8707" }, { "empty","8709" }, { "nabla","8711" }, { "isin","8712" }, { "notin","8713" }, { "ni","8715" }, { "prod","8719" }, { "sum","8721" }, { "minus","8722" }, { "lowast","8727" }, { "radic","8730" }, { "prop","8733" }, { "infin","8734" }, { "ang","8736" }, { "and","8743" }, { "or","8744" }, { "cap","8745" }, { "cup","8746" }, { "int","8747" }, { "there4","8756" }, { "sim","8764" }, { "cong","8773" }, { "asymp","8776" }, { "ne","8800" }, { "equiv","8801" }, { "le","8804" }, { "ge","8805" }, { "sub","8834" }, { "sup","8835" }, { "sube","8838" }, { "supe","8839" }, { "oplus","8853" }, { "otimes","8855" }, { "perp","8869" }, { "sdot","8901" }, { "lceil","8968" }, { "rceil","8969" }, { "lfloor","8970" }, { "rfloor","8971" }, { "lang","9001" }, { "rang","9002" }, { "loz","9674" }, { "spades","9824" }, { "clubs","9827" }, { "hearts","9829" }, { "diams","9830" }, { "OElig","338" }, { "oelig","339" }, { "Scaron","352" }, { "scaron","353" }, { "Yuml","376" }, { "circ","710" }, { "tilde","732" }, { "ensp","8194" }, { "emsp","8195" }, { "thinsp","8201" }, { "zwnj","8204" }, { "zwj","8205" }, { "lrm","8206" }, { "rlm","8207" }, { "ndash","8211" }, { "mdash","8212" }, { "lsquo","8216" }, { "rsquo","8217" }, { "sbquo","8218" }, { "ldquo","8220" }, { "rdquo","8221" }, { "bdquo","8222" }, { "dagger","8224" }, { "Dagger","8225" }, { "permil","8240" }, { "lsaquo","8249" }, { "rsaquo","8250" }, { "euro","8364" } };
        final Entities xml=new Entities();
        xml.addEntities(Entities.BASIC_ARRAY);
        xml.addEntities(Entities.APOS_ARRAY);
        XML=xml;
        final Entities html32=new Entities();
        html32.addEntities(Entities.BASIC_ARRAY);
        html32.addEntities(Entities.ISO8859_1_ARRAY);
        HTML32=html32;
        final Entities html2=new Entities();
        fillWithHtml40Entities(html2);
        HTML40=html2;
    }
    static class PrimitiveEntityMap implements EntityMap{
        private final Map mapNameToValue;
        private final IntHashMap mapValueToName;
        PrimitiveEntityMap(){
            super();
            this.mapNameToValue=new HashMap();
            this.mapValueToName=new IntHashMap();
        }
        public void add(final String name,final int value){
            this.mapNameToValue.put(name,new Integer(value));
            this.mapValueToName.put(value,name);
        }
        public String name(final int value){
            return (String)this.mapValueToName.get(value);
        }
        public int value(final String name){
            final Object value=this.mapNameToValue.get(name);
            if(value==null){
                return -1;
            }
            return (int)value;
        }
    }
    abstract static class MapIntMap implements EntityMap{
        protected final Map mapNameToValue;
        protected final Map mapValueToName;
        MapIntMap(final Map nameToValue,final Map valueToName){
            super();
            this.mapNameToValue=nameToValue;
            this.mapValueToName=valueToName;
        }
        public void add(final String name,final int value){
            this.mapNameToValue.put(name,new Integer(value));
            this.mapValueToName.put(new Integer(value),name);
        }
        public String name(final int value){
            return this.mapValueToName.get(new Integer(value));
        }
        public int value(final String name){
            final Object value=this.mapNameToValue.get(name);
            if(value==null){
                return -1;
            }
            return (int)value;
        }
    }
    static class HashEntityMap extends MapIntMap{
        public HashEntityMap(){
            super(new HashMap(),new HashMap());
        }
    }
    static class TreeEntityMap extends MapIntMap{
        public TreeEntityMap(){
            super(new TreeMap(),new TreeMap());
        }
    }
    static class LookupEntityMap extends PrimitiveEntityMap{
        private String[] lookupTable;
        private final int LOOKUP_TABLE_SIZE=256;
        public String name(final int value){
            if(value<256){
                return this.lookupTable()[value];
            }
            return super.name(value);
        }
        private String[] lookupTable(){
            if(this.lookupTable==null){
                this.createLookupTable();
            }
            return this.lookupTable;
        }
        private void createLookupTable(){
            this.lookupTable=new String[256];
            for(int i=0;i<256;++i){
                this.lookupTable[i]=super.name(i);
            }
        }
    }
    static class ArrayEntityMap implements EntityMap{
        protected final int growBy;
        protected int size;
        protected String[] names;
        protected int[] values;
        public ArrayEntityMap(){
            super();
            this.size=0;
            this.growBy=100;
            this.names=new String[this.growBy];
            this.values=new int[this.growBy];
        }
        public ArrayEntityMap(final int growBy){
            super();
            this.size=0;
            this.growBy=growBy;
            this.names=new String[growBy];
            this.values=new int[growBy];
        }
        public void add(final String name,final int value){
            this.ensureCapacity(this.size+1);
            this.names[this.size]=name;
            this.values[this.size]=value;
            ++this.size;
        }
        protected void ensureCapacity(final int capacity){
            if(capacity>this.names.length){
                final int newSize=Math.max(capacity,this.size+this.growBy);
                final String[] newNames=new String[newSize];
                System.arraycopy(this.names,0,newNames,0,this.size);
                this.names=newNames;
                final int[] newValues=new int[newSize];
                System.arraycopy(this.values,0,newValues,0,this.size);
                this.values=newValues;
            }
        }
        public String name(final int value){
            for(int i=0;i<this.size;++i){
                if(this.values[i]==value){
                    return this.names[i];
                }
            }
            return null;
        }
        public int value(final String name){
            for(int i=0;i<this.size;++i){
                if(this.names[i].equals(name)){
                    return this.values[i];
                }
            }
            return -1;
        }
    }
    static class BinaryEntityMap extends ArrayEntityMap{
        public BinaryEntityMap(){
            super();
        }
        public BinaryEntityMap(final int growBy){
            super(growBy);
        }
        private int binarySearch(final int key){
            int low=0;
            int high=this.size-1;
            while(low<=high){
                final int mid=low+high>>>1;
                final int midVal=this.values[mid];
                if(midVal<key){
                    low=mid+1;
                }
                else{
                    if(midVal<=key){
                        return mid;
                    }
                    high=mid-1;
                }
            }
            return -(low+1);
        }
        public void add(final String name,final int value){
            this.ensureCapacity(this.size+1);
            int insertAt=this.binarySearch(value);
            if(insertAt>0){
                return;
            }
            insertAt=-(insertAt+1);
            System.arraycopy(this.values,insertAt,this.values,insertAt+1,this.size-insertAt);
            this.values[insertAt]=value;
            System.arraycopy(this.names,insertAt,this.names,insertAt+1,this.size-insertAt);
            this.names[insertAt]=name;
            ++this.size;
        }
        public String name(final int value){
            final int index=this.binarySearch(value);
            if(index<0){
                return null;
            }
            return this.names[index];
        }
    }
    interface EntityMap{
        void add(String p0,int p1);
        String name(int p0);
        int value(String p0);
    }
}
