package org.jboss.windup.reporting.xslt.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.jboss.windup.util.exception.WindupException;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class XmlUtils {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger( XmlUtils.class );
    
    /**
     *  Creates this app's standard marshaller.
     *  TODO: Use it in methods below.
     */
    public static JAXBContext createJaxbContext( Class cls ) throws JAXBException {
        Map<String, Object> props = new HashMap();
        props.put( Marshaller.JAXB_FORMATTED_OUTPUT, true );
        props.put( Marshaller.JAXB_ENCODING, "UTF-8");
        //marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.jboss.org/schema/as-migration.xsd as-migration.xsd");
        JAXBContext jaxbCtx = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[]{cls}, props);
        return jaxbCtx;
        // JDK way: Marshaller mar = JAXBContext.newInstance(MigrationReportJaxbBean.class).createMarshaller();
        
    }
    
    public static Marshaller createMarshaller( Class cls ) throws JAXBException {
        return createJaxbContext( cls ).createMarshaller();
    }
    
    /**
     *  Convenience - calls the override with must = true.
     */
    public static <T> T unmarshallBean( File xmlFile, Class<T> cls ) throws WindupException {
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(cls).createUnmarshaller();
            return (T) unmarshaller.unmarshal(xmlFile);
        } catch( JAXBException ex ) {
            throw new WindupException("Failed unmarshalling "+xmlFile+" to "+cls.getSimpleName()+": " + ex.getMessage(), ex);
        }
    }
    
    /**
     *  Convenience - calls the override with must = true.
     */
    public static <T> T unmarshallBean( File docFile, String xpath, Class<T> cls ) throws WindupException{
        return unmarshallBean( true, docFile, xpath, cls );
    }
    
    /**
     *  Read XML from the File, look for nodes by XPath, and unmarshall them into given Class.
     *  If Class is Origin.Wise, the origin is stored.
     */
    public static <T> T unmarshallBean( boolean must, File docFile, String xpath, Class<T> cls ) throws WindupException{
        if( ! docFile.exists() ){
            if( must )  throw new WindupException("File to unmarshall not found: " + docFile);
            else        return null;
        }
            
        List<T> beans = unmarshallBeans( docFile, xpath, cls );
        if( beans.isEmpty() )
            throw new WindupException("XPath "+xpath+" returned no nodes from " + docFile);
        return beans.get(0);
    }
    
    /**
     *  Read XML from the File, look for nodes by XPath, and unmarshall them into given Class.
     *  If Class is Origin.Wise, the origin is stored.
     * 
     *  Caution: Uses JDK's XPathFactoryImpl - Saxon doesn't do well with namespaces.
     */
    public static <T> List<T> unmarshallBeans( File docFile, String xpath, Class<T> cls ) throws WindupException{
        
        List<T> beans = new LinkedList();
        DocumentBuilder docBuilder = createXmlDocumentBuilder();
        try {
            // Parse
            Document doc = docBuilder.parse(docFile);

            // XPath
            //XPathFactory xpf = XPath xp = XPathFactory.newInstance();
            //XPathFactory xpf = new net.sf.saxon.xpath.XPathFactoryImpl();
            // We need Sun's XPath as it ignores namespaces if not specified.
            //XPathFactory xpf = new com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl(); // Warning
            XPathFactory xpf;
            try {
                xpf = (XPathFactory) Class.forName("com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl").newInstance();
            } catch( Exception ex ){ throw new IllegalStateException("Shouldn't happen: " + ex.getMessage(), ex ); }
            
            XPath xp = xpf.newXPath();
            
            NodeList nodes = (NodeList) xp.evaluate(xpath, doc, XPathConstants.NODESET);
            
            
            
            // Unmarshall
            //JAXBContext.newInstance(cls).createUnmarshaller();
            //Unmarshaller unmarshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(cls).createUnmarshaller();
            Unmarshaller unmarshaller = createJaxbContext( cls ).createUnmarshaller();
            for( int i = 0; i < nodes.getLength(); i++ ) {
                Node node = nodes.item( i );
                T bean = (T) unmarshaller.unmarshal(node);
                beans.add( bean );
                
                // Origin - set File and XPath.
                if( bean instanceof Origin.Wise ){
                    final Origin orig = new Origin( docFile, xpath ).setOffset( i );
                    ((Origin.Wise) bean).setOrigin( orig );
                }
            }
        }
        catch( SAXException | IOException | XPathExpressionException | JAXBException ex ) {
            throw new WindupException("Failed parsing bean from a XML file " + docFile.getPath() + ":\n    " + ex.getMessage(), ex);
        }
        return beans;
    }
    
    
    /**
     *  Convenience - calls the override with must = true.
     */
    public static <T> T readXmlConfigFile( File file, String xpath, Class<T> cls, String confAreaDesc ) throws WindupException{
        return readXmlConfigFile( true, file, xpath, cls, confAreaDesc );
    }
    
    /**
     *  Reads given XML file, finds the first node matching given XPath, and reads it using given JAXB class.
     *  @param confAreaDesc  Used for exception message.
     *  @throws WindupException wrapping any Exception.
     */
    public static <T> T readXmlConfigFile( boolean must, File file, String xpath, Class<T> cls, String confAreaDesc ) throws WindupException{
        if( ! file.exists() ){
            if( must )  throw new WindupException("File to unmarshall not found: " + file);
            else        return null;
        }
            
        try {
            return XmlUtils.unmarshallBean( must, file, xpath, cls );
        } catch( Exception ex ) {
            throw new WindupException("Failed loading "+confAreaDesc+" config from "+file.getPath()+":\n    " + ex.getMessage(), ex);
        }
    }

    public static <T> List<T> readXmlConfigFileMulti( File file, String xpath, Class<T> cls, String confAreaDesc ) throws WindupException{
        return readXmlConfigFileMulti( true, file, xpath, cls, confAreaDesc );
    }
    
    /**
     *  Reads given XML file, finds all nodes matching given XPath, and reads them into a list using given JAXB class.
     *  @param confAreaDesc  Used for exception message.
     *  @throws WindupException wrapping any Exception.
     */
    public static <T> List<T> readXmlConfigFileMulti( boolean must, File file, String xpath, Class<T> cls, String confAreaDesc ) throws WindupException{
        if( ! file.exists() ){
            if( must )  throw new WindupException("File to unmarshall not found: " + file);
            else        return null;
        }
            
        try {
            return XmlUtils.unmarshallBeans( file, xpath, cls );
        } catch( Exception ex ) {
            throw new WindupException("Failed loading "+confAreaDesc+" config from "+file.getPath()+":\n    " + ex.getMessage(), ex);
        }
    }

    public static <T> List<T> readXmlConfigFiles( File baseDir, String filesPattern, String xpath, Class<? extends T> cls, String confAreaDesc ) throws WindupException{
        if( ! baseDir.exists() )
            return Collections.EMPTY_LIST;
            
        List<File> files;
        try {
            //files = new PatternDirWalker( filesPattern ).list( baseDir );
            files = new DirScanner( filesPattern ).listAsFiles( baseDir );
        } catch( IOException ex ) {
            throw new WindupException("Failed finding files matching '"+filesPattern+"' in " + baseDir + ":\n  " + ex.getMessage(), ex);
        }
        
        List<T> res = new LinkedList();
        for( File file : files ) {
            try {
                //res.addAll( XmlUtils.unmarshallBeans( new File(baseDir, file.getPath()), xpath, cls ) );
                res.addAll( XmlUtils.unmarshallBeans( file, xpath, cls ) );
            } catch( Exception ex ) {
                throw new WindupException("Failed loading "+confAreaDesc+" config from "+file.getPath()+":\n    " + ex.getMessage(), ex);
            }
        }
        return res;
    }

    

    /**
     * Creates a new default document builder.
     *
     *
     */
    public static DocumentBuilder createXmlDocumentBuilder() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware( false );
        String feat = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
        try {
            dbf.setFeature( feat, false );
        } catch( ParserConfigurationException ex ) {
            log.warn( "Couldn't set " + feat + " to false. The parser may attempt to load DTD." );
        }
        try {
            return dbf.newDocumentBuilder();
        } catch( ParserConfigurationException ex ) {
            throw new RuntimeException( ex );
        }
    }


    /**
     * Saves given XML Document into given File.
     */
    public static File transformDocToFile( Document srcDoc, File dest ) throws TransformerException {
        return transformDocToFile( srcDoc, dest, null );
    }
    
    /**
     * Transforms given Document into given File, using given XSLT.
     * @param xsltIS may be null -> just saves.
     */
    public static File transformDocToFile( Document srcDoc, File dest, InputStream xsltIS ) throws TransformerException {
        Transformer transformer = createTransformer( xsltIS );
        transformer.transform( new DOMSource(srcDoc), new StreamResult(dest) );
        return dest;
    }
    
    public static File transform( File src, File dest, InputStream xsltIS ) throws TransformerException {
        Transformer transformer = createTransformer( xsltIS );
        transformer.transform( new StreamSource( src ), new StreamResult(dest) );
        return dest;
    }
    
    private static Transformer createTransformer( InputStream xsltIS ) throws TransformerConfigurationException {
        //final TransformerFactory tf = TransformerFactory.newInstance();
        final TransformerFactory tf = new net.sf.saxon.TransformerFactoryImpl(); // XSLT 2.0
        
        final Transformer transformer = xsltIS == null ? tf.newTransformer() : tf.newTransformer( new StreamSource( xsltIS ) );
        transformer.setOutputProperty( OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
        return transformer;
    }
    
    /**
     *  Calls transformDocToFile(), wraps exception to WindupException.
     */
    public static void saveXmlToFile( Document doc, File file ) throws WindupException {
        try {
            transformDocToFile( doc, file );
        } catch( TransformerException ex ) {
            throw new WindupException("Failed saving XML document to " + file.getPath()+":\n    " + ex.getMessage(), ex);
        }
    }
    
    

    
    /**
     * Creates clean Document used in other classes for working with XML
     *
     * @return clean Document
     * @throws ParserConfigurationException if creation of document fails
     */
    public static Document createDoc() throws ParserConfigurationException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setIgnoringComments( true );
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.getDOMImplementation().createDocument( null, null, null );
        return doc;
    }


    /**
     * @deprecated TODO: useless?
     */
    public static Document parseFileToXmlDoc( File file ) throws SAXException, IOException {
        DocumentBuilder db = XmlUtils.createXmlDocumentBuilder();
        Document doc = db.parse( file );
        return doc;
    }


    /** 
     *  "toString()" for @XmlLocator.
     */
    public static String formatLocation( Locator location ) {
        if( location == null ) return "(unknown location)";
        return location.getPublicId() == null ?
            String.format("line %d, col %d in %s",
                location.getLineNumber(),
                location.getColumnNumber(),
                location.getSystemId()
            )
                :
            String.format("Pub: %s Sys: %s Line: %d Col: %d",
                location.getPublicId(),
                location.getSystemId(),
                location.getLineNumber(),
                location.getColumnNumber()
           );
    }

}// class
