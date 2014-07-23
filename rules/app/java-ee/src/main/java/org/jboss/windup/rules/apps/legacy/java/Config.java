package org.jboss.windup.rules.apps.legacy.java;

import java.util.ArrayList;
import java.util.List;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.blacklist.ASTEventEvaluatorsBufferOperation;
import org.jboss.windup.rules.apps.java.blacklist.BlackListRegex;
import org.jboss.windup.rules.apps.java.blacklist.Types;
import org.jboss.windup.rules.apps.java.blacklist.WhiteListItem;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

public class Config extends WindupRuleProvider
{

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        context.put(RuleMetadata.CATEGORY, "Java");
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        List<WhiteListItem> items = new ArrayList<WhiteListItem>();
        List<BlackListRegex> hints = new ArrayList<BlackListRegex>();
        
        items.add(new WhiteListItem(getID(), "amx_", Types.add(ClassCandidateType.TYPE)));
        items.add(new WhiteListItem(getID(), "amx_", Types.add(ClassCandidateType.METHOD)));
        items.add(new WhiteListItem(getID(), "((javax.naming.InitialContext)|(javax.naming.Context))$", Types.add(ClassCandidateType.IMPORT)));
        items.add(new WhiteListItem(getID(), "((javax.naming.InitialContext)|(javax.naming.Context)).close", Types.add(ClassCandidateType.METHOD)));
        items.add(new WhiteListItem(getID(), "((javax.naming.InitialContext)|(javax.naming.Context))$", Types.add(ClassCandidateType.TYPE)));
        items.add(new WhiteListItem(getID(), "javax.naming.InitialContext\\(\\)", Types.add(ClassCandidateType.CONSTRUCTOR_CALL)));
        items.add(new WhiteListItem(getID(), "javax.ejb.+$", Types.add(ClassCandidateType.IMPORT)));
        items.add(new WhiteListItem(getID(), "(javax.ejb.EntityContext.*)|(javax.ejb.RemoveException.*)|(javax.ejb.SessionContext.*)|(javax.ejb.EJBException.*)|(javax.ejb.CreateException$)|(javax.ejb.FinderException$)"));
        items.add(new WhiteListItem(getID(), "javax.sql.+$", Types.add(ClassCandidateType.IMPORT)));
        items.add(new WhiteListItem(getID(), "javax.management.+$", Types.add(ClassCandidateType.IMPORT)));
        items.add(new WhiteListItem(getID(), "javax.management.+$", Types.add(ClassCandidateType.TYPE)));
        items.add(new WhiteListItem(getID(), "javax.management.remote.JMXConnector.close.+$", Types.add(ClassCandidateType.METHOD)));
        items.add(new WhiteListItem(getID(), "javax.management.remote.JMXConnector.getMBeanServerConnection\\(\\)", Types.add(ClassCandidateType.METHOD)));
        items.add(new WhiteListItem(getID(), "java.io.LineNumberInputStream$"));
        items.add(new WhiteListItem(getID(), "java.io.ObjectInputStream$"));
        items.add(new WhiteListItem(getID(), "java.io.ObjectOutputStream.PutField$"));
        items.add(new WhiteListItem(getID(), "java.io.StreamTokenizer$"));
        items.add(new WhiteListItem(getID(), "java.io.StringBufferInputStream$"));
        items.add(new WhiteListItem(getID(), "java.lang.Character.UnicodeBlock.SURROGATES_AREA$"));
        items.add(new WhiteListItem(getID(), "java.lang.ClassLoader$"));
        items.add(new WhiteListItem(getID(), "java.lang.Runtime$"));
        items.add(new WhiteListItem(getID(), "java.lang.SecurityManager$"));
        items.add(new WhiteListItem(getID(), "java.lang.SecurityManager.inCheck$"));
        items.add(new WhiteListItem(getID(), "java.lang.System$"));
        items.add(new WhiteListItem(getID(), "java.lang.Thread$"));
        items.add(new WhiteListItem(getID(), "java.lang.ThreadGroup$"));
        items.add(new WhiteListItem(getID(), "java.net.DatagramSocketImpl$"));
        items.add(new WhiteListItem(getID(), "java.net.HttpURLConnection.HTTP_SERVER_ERROR$"));
        items.add(new WhiteListItem(getID(), "java.net.MulticastSocket$"));
        items.add(new WhiteListItem(getID(), "java.net.Socket$"));
        items.add(new WhiteListItem(getID(), "java.net.URLConnection$"));
        items.add(new WhiteListItem(getID(), "java.net.URLDecoder$"));
        items.add(new WhiteListItem(getID(), "java.net.URLEncoder$"));
        items.add(new WhiteListItem(getID(), "java.net.URLStreamHandler$"));
        items.add(new WhiteListItem(getID(), "com.ibm.ac.commonbaseevent101"));
        items.add(new WhiteListItem(getID(), "java.rmi.dgc.VMID$"));
        items.add(new WhiteListItem(getID(), "java.rmi.registry.RegistryHandler$"));
        items.add(new WhiteListItem(getID(), "java.rmi.RMISecurityException$"));
        items.add(new WhiteListItem(getID(), "java.rmi.server.LoaderHandler$"));
        items.add(new WhiteListItem(getID(), "java.rmi.server.LogStream$"));
        items.add(new WhiteListItem(getID(), "java.rmi.server.Operation$"));
        items.add(new WhiteListItem(getID(), "java.rmi.server.RemoteCall$"));
        items.add(new WhiteListItem(getID(), "java.rmi.server.RemoteRef$"));
        items.add(new WhiteListItem(getID(), "java.rmi.server.RemoteStub$"));
        items.add(new WhiteListItem(getID(), "java.rmi.server.RMIClassLoader$"));
        items.add(new WhiteListItem(getID(), "java.rmi.server.Skeleton$"));
        items.add(new WhiteListItem(getID(), "java.rmi.server.SkeletonMismatchException$"));
        items.add(new WhiteListItem(getID(), "java.rmi.server.SkeletonNotFoundException$"));
        items.add(new WhiteListItem(getID(), "java.rmi.ServerRuntimeException$"));
        items.add(new WhiteListItem(getID(), "java.security.Certificate$"));
        items.add(new WhiteListItem(getID(), "java.security.Identity$"));
        items.add(new WhiteListItem(getID(), "java.security.IdentityScope$"));
        items.add(new WhiteListItem(getID(), "java.security.Security$"));
        items.add(new WhiteListItem(getID(), "java.security.Signature$"));
        items.add(new WhiteListItem(getID(), "java.security.SignatureSpi$"));
        items.add(new WhiteListItem(getID(), "java.security.Signer$"));
        items.add(new WhiteListItem(getID(), "java.sql.CallableStatement$"));
        items.add(new WhiteListItem(getID(), "java.sql.Date$"));
        items.add(new WhiteListItem(getID(), "java.sql.DriverManager$"));
        items.add(new WhiteListItem(getID(), "java.sql.PreparedStatement$"));
        items.add(new WhiteListItem(getID(), "java.sql.ResultSet$"));
        items.add(new WhiteListItem(getID(), "java.sql.Time$"));
        items.add(new WhiteListItem(getID(), "java.sql.Timestamp$"));
        items.add(new WhiteListItem(getID(), "java.util.logging.Logger.global$"));
        items.add(new WhiteListItem(getID(), "java.util.Properties$"));
        items.add(new WhiteListItem(getID(), "javax.accessibility.AccessibleResourceBundle$"));
        items.add(new WhiteListItem(getID(), "javax.activation.ActivationDataFlavor$"));
        items.add(new WhiteListItem(getID(), "javax.imageio.spi.ImageReaderSpi.STANDARD_INPUT_TYPE$"));
        items.add(new WhiteListItem(getID(), "javax.imageio.spi.ImageWriterSpi.STANDARD_OUTPUT_TYPE$"));
        items.add(new WhiteListItem(getID(), "javax.jws.HandlerChain.name$"));
        items.add(new WhiteListItem(getID(), "javax.jws.soap.InitParam$"));
        items.add(new WhiteListItem(getID(), "javax.jws.soap.SOAPMessageHandler$"));
        items.add(new WhiteListItem(getID(), "javax.jws.soap.SOAPMessageHandlers$"));
        items.add(new WhiteListItem(getID(), "javax.management.AttributeValueExp$"));
        items.add(new WhiteListItem(getID(), "javax.management.DefaultLoaderRepository$"));
        items.add(new WhiteListItem(getID(), "javax.management.loading.DefaultLoaderRepository$"));
        items.add(new WhiteListItem(getID(), "javax.management.MBeanServer$"));
        items.add(new WhiteListItem(getID(), "javax.management.monitor.CounterMonitor$"));
        items.add(new WhiteListItem(getID(), "javax.management.monitor.CounterMonitorMBean$"));
        items.add(new WhiteListItem(getID(), "javax.management.monitor.GaugeMonitor$"));
        items.add(new WhiteListItem(getID(), "javax.management.monitor.GaugeMonitorMBean$"));
        items.add(new WhiteListItem(getID(), "javax.management.monitor.Monitor$"));
        items.add(new WhiteListItem(getID(), "javax.management.monitor.Monitor.alreadyNotified$"));
        items.add(new WhiteListItem(getID(), "javax.management.monitor.Monitor.dbgTag$"));
        items.add(new WhiteListItem(getID(), "javax.management.monitor.MonitorMBean$"));
        items.add(new WhiteListItem(getID(), "javax.management.monitor.StringMonitor$"));
        items.add(new WhiteListItem(getID(), "javax.management.monitor.StringMonitorMBean$"));
        items.add(new WhiteListItem(getID(), "javax.management.openmbean.OpenType.ALLOWED_CLASSNAMES$"));
        items.add(new WhiteListItem(getID(), "javax.management.StringValueExp$"));
        items.add(new WhiteListItem(getID(), "javax.management.ValueExp$"));
        items.add(new WhiteListItem(getID(), "javax.security.auth.Policy$"));
        items.add(new WhiteListItem(getID(), "javax.sql.rowset.BaseRowSet$"));
        items.add(new WhiteListItem(getID(), "javax.sql.rowset.CachedRowSet.COMMIT_ON_ACCEPT_CHANGES$"));
        items.add(new WhiteListItem(getID(), "javax.swing.AbstractButton$"));
        items.add(new WhiteListItem(getID(), "javax.swing.FocusManager$"));
        items.add(new WhiteListItem(getID(), "javax.swing.JComponent$"));
        items.add(new WhiteListItem(getID(), "javax.swing.JInternalFrame$"));
        items.add(new WhiteListItem(getID(), "javax.swing.JList$"));
        items.add(new WhiteListItem(getID(), "javax.swing.JMenuBar$"));
        items.add(new WhiteListItem(getID(), "javax.swing.JPasswordField$"));
        items.add(new WhiteListItem(getID(), "javax.swing.JPopupMenu$"));
        items.add(new WhiteListItem(getID(), "javax.swing.JRootPane$"));
        items.add(new WhiteListItem(getID(), "javax.swing.JRootPane.defaultPressAction$"));
        items.add(new WhiteListItem(getID(), "javax.swing.JRootPane.defaultReleaseAction$"));
        items.add(new WhiteListItem(getID(), "javax.swing.JTable$"));
        items.add(new WhiteListItem(getID(), "javax.swing.JViewport$"));
        items.add(new WhiteListItem(getID(), "javax.swing.JViewport.backingStore$"));
        items.add(new WhiteListItem(getID(), "javax.swing.KeyStroke$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicDesktopPaneUI.closeKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicDesktopPaneUI.maximizeKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicDesktopPaneUI.minimizeKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicDesktopPaneUI.navigateKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicDesktopPaneUI.navigateKey2$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicInternalFrameUI.openMenuKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicSplitPaneUI$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicSplitPaneUI.dividerResizeToggleKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicSplitPaneUI.downKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicSplitPaneUI.endKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicSplitPaneUI.homeKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicSplitPaneUI.keyboardDownRightListener$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicSplitPaneUI.keyboardEndListener$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicSplitPaneUI.keyboardHomeListener$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicSplitPaneUI.keyboardResizeToggleListener$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicSplitPaneUI.keyboardUpLeftListener$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicSplitPaneUI.leftKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicSplitPaneUI.rightKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicSplitPaneUI.upKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicTabbedPaneUI.downKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicTabbedPaneUI.leftKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicTabbedPaneUI.rightKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicTabbedPaneUI.upKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicToolBarUI.downKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicToolBarUI.leftKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicToolBarUI.rightKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.basic.BasicToolBarUI.upKey$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.metal.MetalComboBoxUI$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.metal.MetalComboBoxUI.MetalComboPopup$"));
        items.add(new WhiteListItem(getID(), "javax.swing.plaf.metal.MetalScrollPaneUI$"));
        items.add(new WhiteListItem(getID(), "javax.swing.ScrollPaneLayout$"));
        items.add(new WhiteListItem(getID(), "javax.swing.SwingUtilities$"));
        items.add(new WhiteListItem(getID(), "javax.swing.table.TableColumn$"));
        items.add(new WhiteListItem(getID(), "javax.swing.table.TableColumn.resizedPostingDisableCount$"));
        items.add(new WhiteListItem(getID(), "javax.swing.text.DefaultTextUI$"));
        items.add(new WhiteListItem(getID(), "javax.swing.text.html.FormView.RESET$"));
        items.add(new WhiteListItem(getID(), "javax.swing.text.html.FormView.SUBMIT$"));
        items.add(new WhiteListItem(getID(), "javax.swing.text.html.HTMLEditorKit.InsertHTMLTextAction$"));
        items.add(new WhiteListItem(getID(), "javax.swing.text.LabelView$"));
        items.add(new WhiteListItem(getID(), "javax.swing.text.TableView$"));
        items.add(new WhiteListItem(getID(), "javax.swing.text.TableView.TableCell$"));
        items.add(new WhiteListItem(getID(), "javax.swing.text.View$"));
        items.add(new WhiteListItem(getID(), "javax.swing.tree.DefaultTreeSelectionModel$"));
        items.add(new WhiteListItem(getID(), "javax.xml.bind.JAXBContext$"));
        items.add(new WhiteListItem(getID(), "javax.xml.bind.Unmarshaller$"));
        items.add(new WhiteListItem(getID(), "javax.xml.bind.Validator$"));
        items.add(new WhiteListItem(getID(), "javax.xml.soap.SOAPElementFactory$"));
        items.add(new WhiteListItem(getID(), "javax.xml.stream.XMLEventFactory$"));
        items.add(new WhiteListItem(getID(), "javax.xml.stream.XMLInputFactory$"));
        items.add(new WhiteListItem(getID(), "javax.xml.stream.XMLOutputFactory$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.Any$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.DynamicImplementation$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.DynAny$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.DynArray$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.DynEnum$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.DynFixed$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.DynSequence$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.DynStruct$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.DynUnion$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.DynValue$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.ORB$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.portable.InputStream$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.portable.OutputStream$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.Principal$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.PrincipalHolder$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.ServerRequest$"));
        items.add(new WhiteListItem(getID(), "org.omg.CORBA.TCKind$"));
        items.add(new WhiteListItem(getID(), "org.xml.sax.AttributeList$"));
        items.add(new WhiteListItem(getID(), "org.xml.sax.DocumentHandler$"));
        items.add(new WhiteListItem(getID(), "org.xml.sax.HandlerBase$"));
        items.add(new WhiteListItem(getID(), "org.xml.sax.helpers.AttributeListImpl$"));
        items.add(new WhiteListItem(getID(), "org.xml.sax.helpers.ParserFactory$"));
        items.add(new WhiteListItem(getID(), "org.xml.sax.Parser$"));
        hints.add(new BlackListRegex(getID(), "edu.oswego.cs.dl.util.concurrent", "Upgrade to javax.util.concurrent in Java 5+", 0));
        hints.add(new BlackListRegex(getID(), "edu.emory.mathcs.backport.java.util", "Upgrade to javax.util.concurrent in Java 5+", 0));
        hints.add(new BlackListRegex(getID(), "java.lang.Class.classForName", "Ensure class is available to JBoss", 1, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "oracle.sql.*", "Oracle-specific SQL code", 1, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "org.osoa.sca.annotations.+", "Remove import", 0, Types.add(ClassCandidateType.IMPORT)));
        hints.add(new BlackListRegex(getID(), "org.osoa.sca.annotations.Property", "SCA Property Injection; replace with Spring Property Injection", 0, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "org.osoa.sca.annotations.Reference", "SCA Bean Injection; replace with Spring Bean Injection", 0, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "org.osoa.sca.annotations.Init", "SCA Initialization Hook; Use the property: init-method='example' on the Spring Bean, where example is the initialization method", 0, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "org.osoa.sca.annotations.Destroy", "SCA Destroy Hook; Use the property: destroy-method='example' on the Spring Bean, where example is the destroy method", 0, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "com.ibm.ctg.client.JavaGateway", "IBM CICS Adapter", 0, Types.add(ClassCandidateType.TYPE)));
        hints.add(new BlackListRegex(getID(), "((javax.naming.InitialContext)|(javax.naming.Context)).lookup", "\"<![CDATA[\"\n" + 
            "                    + \"Ensure that the JNDI Name does not need to change for JBoss\" +\n" + 
            "                \n" + 
            "                \"\"*For Example:*\n" + 
            "                \n" + 
            "                ```java\n" + 
            "                (ConnectionFactory)initialContext.lookup(\"weblogic.jms.ConnectionFactory\");\n" + 
            "                ```\n" + 
            "                \n" + 
            "                *should become:*\n" + 
            "                \n" + 
            "                ```java\n" + 
            "                (ConnectionFactory)initialContext.lookup(\"/ConnectionFactory\");\n" + 
            "                ```\n" + 
            "                \n" + 
            "                \n" + 
            "                ]]>\"", 1, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "javax.naming.InitialContext\\(.+\\)", "Ensure that the InitialContext connection properties do not need to change for JBoss", 1, Types.add(ClassCandidateType.CONSTRUCTOR_CALL)));
        hints.add(new BlackListRegex(getID(), "javax.management.remote.JMXServiceURL\\(.+\\)", "Ensure that the connection properties do not need to change for JBoss", 0, Types.add(ClassCandidateType.CONSTRUCTOR_CALL)));
        hints.add(new BlackListRegex(getID(), "javax.management.ObjectName\\(.+\\)", "Ensure that the ObjectName exists in JBoss", 1, Types.add(ClassCandidateType.CONSTRUCTOR_CALL)));
        hints.add(new BlackListRegex(getID(), "javax.management.remote.JMXConnectorFactory.connect\\(.+\\)", "Ensure that the connection properties do not need to change for JBoss", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "java.sql.DriverManager", "Move to a JCA Connector unless this class is used for batch processes, then refactor as necessary", 0, Types.add(ClassCandidateType.METHOD)));
        hints.add(new BlackListRegex(getID(), "java.sql.DriverManager$", "Migrate to JCA Connector", 0, Types.add(ClassCandidateType.IMPORT)));
        hints.add(new BlackListRegex(getID(), "amx_.+", "Tibco ActiveMatrix Stub; regenerate the SOAP Client for the class", 0, Types.add(ClassCandidateType.IMPORT)));
        hints.add(new BlackListRegex(getID(), "com.tibco.matrix.java.annotations.WebParam$", "Tibco specific annotation; replace with javax.jws.WebParam", 0));
        hints.add(new BlackListRegex(getID(), "com.tibco.amf.platform.runtime.extension.exception.SOAPCode$", "Tibco specific annotation", 0));
        hints.add(new BlackListRegex(getID(), "com.tibco.matrix.java.annotations.WebServiceInterface$", "Tibco specific annotation; replace with javax.jws.WebService", 0));
        hints.add(new BlackListRegex(getID(), "com.tibco.matrix.java.annotations.WebMethod$", "Tibco specific annotation; replace with javax.jws.WebMethod", 0));
        hints.add(new BlackListRegex(getID(), "com.tibco.matrix.java.annotations.WebFault$", "Tibco specific annotation; replace with javax.xml.ws.WebFault", 0));
        hints.add(new BlackListRegex(getID(), "org.mule.transformers.AbstractTransformer$", "Mule specific; replace with org.apache.camel.Converter annotation", 0)); 
        hints.add(new BlackListRegex(getID(), "org.mule.umo.UMOMessage.getPayload.+", "Mule specific; replace with org.apache.camel.Message.getBody()", 0, Types.add(ClassCandidateType.METHOD))); 
        
        
        
        Configuration configuration = ConfigurationBuilder.begin()
            .addRule().perform(new ASTEventEvaluatorsBufferOperation().add(items).add(hints));
        
        return configuration;
    }
    // @formatter:on
}
