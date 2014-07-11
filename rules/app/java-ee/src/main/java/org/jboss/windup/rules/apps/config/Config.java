package org.jboss.windup.rules.apps.config;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.blacklist.ModelCreatorGraphOperation;
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

    @Override
    public Configuration getConfiguration(GraphContext context)
    {

        Configuration configuration = ConfigurationBuilder
                    .begin()
//                    <windup:java-whitelist source-type="TYPE" regex="amx_"/>
                    .addRule().perform(WhiteListItem.add(getID(), "amx_", Types.add(ClassCandidateType.TYPE)));
        
        
//                            <windup:java-whitelist source-type="METHOD" regex="amx_"/>
//                            <windup:java-whitelist source-type="IMPORT"
//                                regex="((javax.naming.InitialContext)|(javax.naming.Context))$"/>
//                            <windup:java-whitelist source-type="METHOD"
//                                regex="((javax.naming.InitialContext)|(javax.naming.Context)).close"/>
//                            <windup:java-whitelist source-type="TYPE"
//                                regex="((javax.naming.InitialContext)|(javax.naming.Context))$"/>
//                            <windup:java-whitelist source-type="CONSTRUCT" regex="javax.naming.InitialContext\(\)"/>
//                            <windup:java-whitelist source-type="IMPORT" regex="javax.ejb.+$"/>
//                            <windup:java-whitelist
//                                regex="(javax.ejb.EntityContext.*)|(javax.ejb.RemoveException.*)|(javax.ejb.SessionContext.*)|(javax.ejb.EJBException.*)|(javax.ejb.CreateException$)|(javax.ejb.FinderException$)"/>
//                            <windup:java-whitelist source-type="IMPORT" regex="javax.sql.+$"/>
//                            <windup:java-whitelist source-type="IMPORT" regex="javax.management.+$"/>
//                            <windup:java-whitelist source-type="TYPE" regex="javax.management.+$"/>
//                            <windup:java-whitelist source-type="METHOD"
//                                regex="javax.management.remote.JMXConnector.close.+$"/>
//                            <windup:java-whitelist source-type="METHOD"
//                                regex="javax.management.remote.JMXConnector.getMBeanServerConnection\(\)"/>
//
//
//                            <!-- work around for the fact that the initial hits in the engine should be "candidates" and 
//                                the rules should validate the candidates. unfortunately, right now, any hint from the engine is seen as an "issue" rather 
//                                than a candidate issue. -->
//                            <windup:java-whitelist regex="java.io.LineNumberInputStream$"/>
//                            <windup:java-whitelist regex="java.io.ObjectInputStream$"/>
//                            <windup:java-whitelist regex="java.io.ObjectOutputStream.PutField$"/>
//                            <windup:java-whitelist regex="java.io.StreamTokenizer$"/>
//                            <windup:java-whitelist regex="java.io.StringBufferInputStream$"/>
//                            <windup:java-whitelist regex="java.lang.Character.UnicodeBlock.SURROGATES_AREA$"/>
//                            <windup:java-whitelist regex="java.lang.ClassLoader$"/>
//                            <windup:java-whitelist regex="java.lang.Runtime$"/>
//                            <windup:java-whitelist regex="java.lang.SecurityManager$"/>
//                            <windup:java-whitelist regex="java.lang.SecurityManager.inCheck$"/>
//                            <windup:java-whitelist regex="java.lang.System$"/>
//                            <windup:java-whitelist regex="java.lang.Thread$"/>
//                            <windup:java-whitelist regex="java.lang.ThreadGroup$"/>
//                            <windup:java-whitelist regex="java.net.DatagramSocketImpl$"/>
//                            <windup:java-whitelist regex="java.net.HttpURLConnection.HTTP_SERVER_ERROR$"/>
//                            <windup:java-whitelist regex="java.net.MulticastSocket$"/>
//                            <windup:java-whitelist regex="java.net.Socket$"/>
//                            <windup:java-whitelist regex="java.net.URLConnection$"/>
//                            <windup:java-whitelist regex="java.net.URLDecoder$"/>
//                            <windup:java-whitelist regex="java.net.URLEncoder$"/>
//                            <windup:java-whitelist regex="java.net.URLStreamHandler$"/>
//                            <windup:java-whitelist regex="com.ibm.ac.commonbaseevent101"/>
//                            <windup:java-whitelist regex="java.rmi.dgc.VMID$"/>
//                            <windup:java-whitelist regex="java.rmi.registry.RegistryHandler$"/>
//                            <windup:java-whitelist regex="java.rmi.RMISecurityException$"/>
//                            <windup:java-whitelist regex="java.rmi.server.LoaderHandler$"/>
//                            <windup:java-whitelist regex="java.rmi.server.LogStream$"/>
//                            <windup:java-whitelist regex="java.rmi.server.Operation$"/>
//                            <windup:java-whitelist regex="java.rmi.server.RemoteCall$"/>
//                            <windup:java-whitelist regex="java.rmi.server.RemoteRef$"/>
//                            <windup:java-whitelist regex="java.rmi.server.RemoteStub$"/>
//                            <windup:java-whitelist regex="java.rmi.server.RMIClassLoader$"/>
//                            <windup:java-whitelist regex="java.rmi.server.Skeleton$"/>
//                            <windup:java-whitelist regex="java.rmi.server.SkeletonMismatchException$"/>
//                            <windup:java-whitelist regex="java.rmi.server.SkeletonNotFoundException$"/>
//                            <windup:java-whitelist regex="java.rmi.ServerRuntimeException$"/>
//                            <windup:java-whitelist regex="java.security.Certificate$"/>
//                            <windup:java-whitelist regex="java.security.Identity$"/>
//                            <windup:java-whitelist regex="java.security.IdentityScope$"/>
//                            <windup:java-whitelist regex="java.security.Security$"/>
//                            <windup:java-whitelist regex="java.security.Signature$"/>
//                            <windup:java-whitelist regex="java.security.SignatureSpi$"/>
//                            <windup:java-whitelist regex="java.security.Signer$"/>
//                            <windup:java-whitelist regex="java.sql.CallableStatement$"/>
//                            <windup:java-whitelist regex="java.sql.Date$"/>
//                            <windup:java-whitelist regex="java.sql.DriverManager$"/>
//                            <windup:java-whitelist regex="java.sql.PreparedStatement$"/>
//                            <windup:java-whitelist regex="java.sql.ResultSet$"/>
//                            <windup:java-whitelist regex="java.sql.Time$"/>
//                            <windup:java-whitelist regex="java.sql.Timestamp$"/>
//                            <windup:java-whitelist regex="java.util.logging.Logger.global$"/>
//                            <windup:java-whitelist regex="java.util.Properties$"/>
//                            <windup:java-whitelist regex="javax.accessibility.AccessibleResourceBundle$"/>
//                            <windup:java-whitelist regex="javax.activation.ActivationDataFlavor$"/>
//                            <windup:java-whitelist regex="javax.imageio.spi.ImageReaderSpi.STANDARD_INPUT_TYPE$"/>
//                            <windup:java-whitelist regex="javax.imageio.spi.ImageWriterSpi.STANDARD_OUTPUT_TYPE$"/>
//                            <windup:java-whitelist regex="javax.jws.HandlerChain.name$"/>
//                            <windup:java-whitelist regex="javax.jws.soap.InitParam$"/>
//                            <windup:java-whitelist regex="javax.jws.soap.SOAPMessageHandler$"/>
//                            <windup:java-whitelist regex="javax.jws.soap.SOAPMessageHandlers$"/>
//                            <windup:java-whitelist regex="javax.management.AttributeValueExp$"/>
//                            <windup:java-whitelist regex="javax.management.DefaultLoaderRepository$"/>
//                            <windup:java-whitelist regex="javax.management.loading.DefaultLoaderRepository$"/>
//                            <windup:java-whitelist regex="javax.management.MBeanServer$"/>
//                            <windup:java-whitelist regex="javax.management.monitor.CounterMonitor$"/>
//                            <windup:java-whitelist regex="javax.management.monitor.CounterMonitorMBean$"/>
//                            <windup:java-whitelist regex="javax.management.monitor.GaugeMonitor$"/>
//                            <windup:java-whitelist regex="javax.management.monitor.GaugeMonitorMBean$"/>
//                            <windup:java-whitelist regex="javax.management.monitor.Monitor$"/>
//                            <windup:java-whitelist regex="javax.management.monitor.Monitor.alreadyNotified$"/>
//                            <windup:java-whitelist regex="javax.management.monitor.Monitor.dbgTag$"/>
//                            <windup:java-whitelist regex="javax.management.monitor.MonitorMBean$"/>
//                            <windup:java-whitelist regex="javax.management.monitor.StringMonitor$"/>
//                            <windup:java-whitelist regex="javax.management.monitor.StringMonitorMBean$"/>
//                            <windup:java-whitelist regex="javax.management.openmbean.OpenType.ALLOWED_CLASSNAMES$"/>
//                            <windup:java-whitelist regex="javax.management.StringValueExp$"/>
//                            <windup:java-whitelist regex="javax.management.ValueExp$"/>
//                            <windup:java-whitelist regex="javax.security.auth.Policy$"/>
//                            <windup:java-whitelist regex="javax.sql.rowset.BaseRowSet$"/>
//                            <windup:java-whitelist regex="javax.sql.rowset.CachedRowSet.COMMIT_ON_ACCEPT_CHANGES$"/>
//                            <windup:java-whitelist regex="javax.swing.AbstractButton$"/>
//                            <windup:java-whitelist regex="javax.swing.FocusManager$"/>
//                            <windup:java-whitelist regex="javax.swing.JComponent$"/>
//                            <windup:java-whitelist regex="javax.swing.JInternalFrame$"/>
//                            <windup:java-whitelist regex="javax.swing.JList$"/>
//                            <windup:java-whitelist regex="javax.swing.JMenuBar$"/>
//                            <windup:java-whitelist regex="javax.swing.JPasswordField$"/>
//                            <windup:java-whitelist regex="javax.swing.JPopupMenu$"/>
//                            <windup:java-whitelist regex="javax.swing.JRootPane$"/>
//                            <windup:java-whitelist regex="javax.swing.JRootPane.defaultPressAction$"/>
//                            <windup:java-whitelist regex="javax.swing.JRootPane.defaultReleaseAction$"/>
//                            <windup:java-whitelist regex="javax.swing.JTable$"/>
//                            <windup:java-whitelist regex="javax.swing.JViewport$"/>
//                            <windup:java-whitelist regex="javax.swing.JViewport.backingStore$"/>
//                            <windup:java-whitelist regex="javax.swing.KeyStroke$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicDesktopPaneUI.closeKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicDesktopPaneUI.maximizeKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicDesktopPaneUI.minimizeKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicDesktopPaneUI.navigateKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicDesktopPaneUI.navigateKey2$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicInternalFrameUI.openMenuKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicSplitPaneUI$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicSplitPaneUI.dividerResizeToggleKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicSplitPaneUI.downKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicSplitPaneUI.endKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicSplitPaneUI.homeKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicSplitPaneUI.keyboardDownRightListener$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicSplitPaneUI.keyboardEndListener$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicSplitPaneUI.keyboardHomeListener$"/>
//                            <windup:java-whitelist
//                                regex="javax.swing.plaf.basic.BasicSplitPaneUI.keyboardResizeToggleListener$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicSplitPaneUI.keyboardUpLeftListener$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicSplitPaneUI.leftKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicSplitPaneUI.rightKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicSplitPaneUI.upKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicTabbedPaneUI.downKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicTabbedPaneUI.leftKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicTabbedPaneUI.rightKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicTabbedPaneUI.upKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicToolBarUI.downKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicToolBarUI.leftKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicToolBarUI.rightKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.basic.BasicToolBarUI.upKey$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.metal.MetalComboBoxUI$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.metal.MetalComboBoxUI.MetalComboPopup$"/>
//                            <windup:java-whitelist regex="javax.swing.plaf.metal.MetalScrollPaneUI$"/>
//                            <windup:java-whitelist regex="javax.swing.ScrollPaneLayout$"/>
//                            <windup:java-whitelist regex="javax.swing.SwingUtilities$"/>
//                            <windup:java-whitelist regex="javax.swing.table.TableColumn$"/>
//                            <windup:java-whitelist regex="javax.swing.table.TableColumn.resizedPostingDisableCount$"/>
//                            <windup:java-whitelist regex="javax.swing.text.DefaultTextUI$"/>
//                            <windup:java-whitelist regex="javax.swing.text.html.FormView.RESET$"/>
//                            <windup:java-whitelist regex="javax.swing.text.html.FormView.SUBMIT$"/>
//                            <windup:java-whitelist regex="javax.swing.text.html.HTMLEditorKit.InsertHTMLTextAction$"/>
//                            <windup:java-whitelist regex="javax.swing.text.LabelView$"/>
//                            <windup:java-whitelist regex="javax.swing.text.TableView$"/>
//                            <windup:java-whitelist regex="javax.swing.text.TableView.TableCell$"/>
//                            <windup:java-whitelist regex="javax.swing.text.View$"/>
//                            <windup:java-whitelist regex="javax.swing.tree.DefaultTreeSelectionModel$"/>
//                            <windup:java-whitelist regex="javax.xml.bind.JAXBContext$"/>
//                            <windup:java-whitelist regex="javax.xml.bind.Unmarshaller$"/>
//                            <windup:java-whitelist regex="javax.xml.bind.Validator$"/>
//                            <windup:java-whitelist regex="javax.xml.soap.SOAPElementFactory$"/>
//                            <windup:java-whitelist regex="javax.xml.stream.XMLEventFactory$"/>
//                            <windup:java-whitelist regex="javax.xml.stream.XMLInputFactory$"/>
//                            <windup:java-whitelist regex="javax.xml.stream.XMLOutputFactory$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.Any$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.DynamicImplementation$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.DynAny$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.DynArray$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.DynEnum$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.DynFixed$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.DynSequence$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.DynStruct$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.DynUnion$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.DynValue$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.ORB$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.portable.InputStream$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.portable.OutputStream$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.Principal$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.PrincipalHolder$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.ServerRequest$"/>
//                            <windup:java-whitelist regex="org.omg.CORBA.TCKind$"/>
//                            <windup:java-whitelist regex="org.xml.sax.AttributeList$"/>
//                            <windup:java-whitelist regex="org.xml.sax.DocumentHandler$"/>
//                            <windup:java-whitelist regex="org.xml.sax.HandlerBase$"/>
//                            <windup:java-whitelist regex="org.xml.sax.helpers.AttributeListImpl$"/>
//                            <windup:java-whitelist regex="org.xml.sax.helpers.ParserFactory$"/>
//                            <windup:java-whitelist regex="org.xml.sax.Parser$"/>

        return configuration;
    }
}
