package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

public class JDKConfig extends WindupRuleProvider
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
        Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    .when(
                    JavaClass.references("java.lang.ClassLoader$") .at(TypeReferenceLocation.TYPE) ) .perform(
                    Iteration.over().perform(
                    Classification.as(
                    "Java Classloader, must be migrated.").with( Link.to( "Red Hat Customer Portal: How to get resources via the ClassLoader in a JavaEE application in JBoss EAP" ,"https://access.redhat.com/knowledge/solutions/239033") ).withEffort( 1
                    ))
                    .endIteration()
                    )
                    .addRule()
                    .when(
                    JavaClass.references("org.xml.sax.AttributeList") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This interface has been replaced by the SAX2 Attributes interface, which includes Namespace support." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.security.Certificate") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. A new certificate handling package is created in the Java platform. This Certificate interface is entirely deprecated and is here to allow for a smooth transition to the new package." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.xml.sax.DocumentHandler") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This interface has been replaced by the SAX2 ContentHandler interface, which includes Namespace support." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.DynAny") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynAny instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.DynArray") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynArray instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.DynEnum") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynEnum instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.DynFixed") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynFixed instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.DynSequence") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynSequence instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.DynStruct") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynStruct instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.DynUnion") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynUnion instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.DynValue") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynValue instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LoaderHandler") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.xml.sax.Parser") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This interface has been replaced by the SAX2 XMLReader interface, which includes Namespace support." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.registry.RegistryHandler") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RemoteCall") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.Skeleton") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement. Skeletons are no longer required for remote method calls in the Java 2 platform v1.2 and greater." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.bind.Validator") .at(TypeReferenceLocation.INHERITANCE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. since JAXB 2.0" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.accessibility.AccessibleResourceBundle") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This class is deprecated as of version 1.3 of the Java Platform." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.xml.sax.helpers.AttributeListImpl") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This class implements a deprecated interface, AttributeList; that interface has been replaced by Attributes, which is implemented in the AttributesImpl helper class." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.DefaultLoaderRepository") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use MBeanServer.getClassLoaderRepository() instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.loading.DefaultLoaderRepository") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use MBeanServer.getClassLoaderRepository()} instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.text.DefaultTextUI") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.DynamicImplementation") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. org.omg.CORBA.DynamicImplementation" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.xml.sax.HandlerBase") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This class works with the deprecated DocumentHandler interface. It has been replaced by the SAX2 DefaultHandler class." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.security.Identity") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This class is no longer used. Its functionality has been replaced by java.security.KeyStore, the java.security.cert package, and java.security.Principal." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.security.IdentityScope") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This class is no longer used. Its functionality has been replaced by java.security.KeyStore, the java.security.cert package, and java.security.Principal." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.io.LineNumberInputStream") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This class incorrectly assumes that bytes adequately represent characters. As of JDK 1.1, the preferred way to operate on character streams is via the new character-stream classes, which include a class for counting line numbers." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LogStream") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.metal.MetalComboBoxUI.MetalComboPopup") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.4." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.Operation") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.xml.sax.helpers.ParserFactory") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This class works with the deprecated Parser interface." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.Principal") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Deprecated by CORBA 2.2." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.PrincipalHolder") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Deprecated by CORBA 2.2." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.security.Signer") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This class is no longer used. Its functionality has been replaced by java.security.KeyStore, the java.security.cert package, and java.security.Principal." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.soap.SOAPElementFactory") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use javax.xml.soap.SOAPFactory for creating SOAPElements." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.io.StringBufferInputStream") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This class does not properly convert characters into bytes. As of JDK 1.1, the preferred way to create a stream from a string is via the StringReader class." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.text.TableView.TableCell") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. A table cell can now be any View implementation." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.security.auth.Policy") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( null ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.RMISecurityException") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use SecurityException instead. Application code should never directly reference this class, and RMISecurityManager no longer throws this subclass of java.lang.SecurityException." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.ServerRuntimeException") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.SkeletonMismatchException") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement. Skeletons are no longer required for remote method calls in the Java 2 platform v1.2 and greater." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.SkeletonNotFoundException") .at(TypeReferenceLocation.IMPORT) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement. Skeletons are no longer required for remote method calls in the Java 2 platform v1.2 and greater." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.jws.soap.InitParam") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JSR-181 2.0 with no replacement." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.jws.soap.SOAPMessageHandler") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JSR-181 2.0 with no replacement." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.jws.soap.SOAPMessageHandlers") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JSR-181 2.0 with no replacement." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.openmbean.OpenType.ALLOWED_CLASSNAMES") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use ALLOWED_CLASSNAMES_LIST instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.Monitor.alreadyNotified") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. equivalent to Monitor.alreadyNotifieds[0]." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JViewport.backingStore") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicDesktopPaneUI.closeKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.sql.rowset.CachedRowSet.COMMIT_ON_ACCEPT_CHANGES") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Because this field is final (it is part of an interface), its value cannot be changed." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.CROSSHAIR_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.CROSSHAIR_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.Monitor.dbgTag") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. No replacement." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.DEFAULT_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.DEFAULT_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JRootPane.defaultPressAction") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JRootPane.defaultReleaseAction") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.dividerResizeToggleKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicToolBarUI.downKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.downKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicTabbedPaneUI.downKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.E_RESIZE_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.E_RESIZE_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.endKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.logging.Logger.global") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Initialization of this field is prone to deadlocks. The field must be initialized by the Logger class initialization which may cause deadlocks with the LogManager class initialization. In such cases two class initialization wait for each other to complete. The preferred way to get the global logger object is via the call Logger.getGlobal(). For compatibility with old JDK versions where the Logger.getGlobal() is not available use the call Logger.getLogger(Logger.GLOBAL_LOGGER_NAME) or Logger.getLogger(\"global\")." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.HAND_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.HAND_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.homeKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.net.HttpURLConnection.HTTP_SERVER_ERROR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. it is misplaced and shouldn't have existed." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.SecurityManager.inCheck") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This type of security checking is not recommended. It is recommended that the checkPermission call be used instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.keyboardDownRightListener") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.keyboardEndListener") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.keyboardHomeListener") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.keyboardResizeToggleListener") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.keyboardUpLeftListener") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicToolBarUI.leftKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.leftKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicTabbedPaneUI.leftKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicDesktopPaneUI.maximizeKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicDesktopPaneUI.minimizeKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.MOVE_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.MOVE_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.N_RESIZE_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.N_RESIZE_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicDesktopPaneUI.navigateKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicDesktopPaneUI.navigateKey2") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.NE_RESIZE_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.NE_RESIZE_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.NW_RESIZE_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.NW_RESIZE_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicInternalFrameUI.openMenuKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.datatransfer.DataFlavor.plainTextFlavor") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. as of 1.3. Use DataFlavor.getReaderForText(Transferable) instead of Transferable.getTransferData(DataFlavor.plainTextFlavor)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Cursor.predefined") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.7, the Cursor.getPredefinedCursor(int) method should be used instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.text.html.FormView.RESET") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.3, value comes from UIManager UIManager property FormView.resetButtonText" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.table.TableColumn.resizedPostingDisableCount") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. as of Java 2 platform v1.3" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicToolBarUI.rightKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.rightKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicTabbedPaneUI.rightKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.S_RESIZE_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.S_RESIZE_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.SE_RESIZE_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.SE_RESIZE_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.imageio.spi.ImageReaderSpi.STANDARD_INPUT_TYPE") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Instead of using this field, directly create the equivalent array { ImageInputStream.class }." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.imageio.spi.ImageWriterSpi.STANDARD_OUTPUT_TYPE") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Instead of using this field, directly create the equivalent array { ImageOutputStream.class }." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.text.html.FormView.SUBMIT") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.3, value now comes from UIManager property FormView.submitButtonText" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.Character.UnicodeBlock.SURROGATES_AREA") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of J2SE 5, use Character.UnicodeBlock.HIGH_SURROGATES, Character.UnicodeBlock.HIGH_PRIVATE_USE_SURROGATES, and Character.UnicodeBlock.LOW_SURROGATES. These new constants match the block definitions of the Unicode Standard. The Character.UnicodeBlock.of(char) and Character.UnicodeBlock.of(int) methods return the new constants, not SURROGATES_AREA." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.SW_RESIZE_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.SW_RESIZE_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.TEXT_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.TEXT_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicToolBarUI.upKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.upKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicTabbedPaneUI.upKey") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.W_RESIZE_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.W_RESIZE_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.WAIT_CURSOR") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Cursor.WAIT_CURSOR." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.action\\(Event, Object\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, should register this component as ActionListener on component which fires action events." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.List.addItem\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by add(String)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.List.addItem\\(String, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by add(String, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.BorderLayout.addLayoutComponent\\(String, Component\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by addLayoutComponent(Component, Object)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.CardLayout.addLayoutComponent\\(String, Component\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by addLayoutComponent(Component, Object)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.List.allowsMultipleSelections\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by isMultipleMode()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.ThreadGroup.allowThreadSuspension\\(boolean\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. The definition of this call depends on ThreadGroup.suspend(), which is deprecated. Further, the behavior of this call was never specified." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.TextArea.appendText\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by append(String)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Window.applyResourceBundle\\(ResourceBundle\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of J2SE 1.4, replaced by Component.applyComponentOrientation." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Window.applyResourceBundle\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of J2SE 1.4, replaced by Component.applyComponentOrientation." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.bounds\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getBounds()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.SecurityManager.checkMulticast\\(InetAddress, byte\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. se #checkPermission(java.security.Permission) instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.SecurityManager.classDepth\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This type of security checking is not recommended. It is recommended that the checkPermission call be used instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.SecurityManager.classLoaderDepth\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This type of security checking is not recommended. It is recommended that the checkPermission call be used instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.List.clear\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by removeAll()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.image.renderable.RenderContext.concetenateTransform\\(AffineTransform\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by concatenateTransform(AffineTransform)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Container.countComponents\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getComponentCount()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Menu.countItems\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getItemCount()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Choice.countItems\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getItemCount()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.List.countItems\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getItemCount()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.MenuBar.countMenus\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getMenuCount()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.Thread.countStackFrames\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. The definition of this call depends on Thread.suspend(), which is deprecated. Further, the results of this call were never well-defined." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.ORB.create_basic_dyn_any(TypeCode)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynAnyFactory API instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.ORB.create_dyn_any(Any)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynAnyFactory API instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.ORB.create_dyn_array(TypeCode)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynAnyFactory API instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.ORB.create_dyn_enum(TypeCode)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynAnyFactory API instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.ORB.create_dyn_sequence(TypeCode)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynAnyFactory API instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.ORB.create_dyn_struct(TypeCode)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynAnyFactory API instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.ORB.create_dyn_union(TypeCode)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the new DynAnyFactory API instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.ORB.create_recursive_sequence_tc(int, int)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use a combination of create_recursive_tc and create_sequence_tc instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.soap.SOAPElementFactory.create\\(Name\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use javax.xml.soap.SOAPFactory.createElement(javax.xml.soap.Name) instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.soap.SOAPElementFactory.create\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use javax.xml.soap.SOAPFactory.createElement(String localName) instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.soap.SOAPElementFactory.create\\(String, String, String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use javax.xml.soap.SOAPFactory.createElement(String localName, String prefix, String uri) instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.createKeyboardDownRightListener\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.createKeyboardEndListener\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.createKeyboardHomeListener\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.createKeyboardResizeToggleListener\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.createKeyboardUpLeftListener\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JTable.createScrollPaneForTable\\(JTable\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Swing version 1.0.2, replaced by new JScrollPane(aTable)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.text.TableView.createTableCell\\(Element\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Table cells can now be any arbitrary View implementation and should be produced by the ViewFactory rather than the table." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.bind.JAXBContext.createValidator\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. since JAXB2.0" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.SecurityManager.currentClassLoader\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This type of security checking is not recommended. It is recommended that the checkPermission call be used instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.SecurityManager.currentLoadedClass\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This type of security checking is not recommended. It is recommended that the checkPermission call be used instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.net.URLDecoder.decode\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. The resulting string may vary depending on the platform's default encoding. Instead, use the decode(String,String) method to specify the encoding." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.ClassLoader.defineClass\\(byte\\[\\], int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Replaced by defineClass(String, byte\\[\\], int, int)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.List.delItem\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by remove(String) and remove(int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.List.delItems\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, Not for public use in the future. This method is expected to be retained only as a package private method." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Container.deliverEvent\\(Event\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by dispatchEvent(AWTEvent e)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.deliverEvent\\(Event\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by dispatchEvent(AWTEvent e)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.MBeanServer.deserialize\\(ObjectName, byte\\[\\]\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use getClassLoaderFor to obtain the appropriate class loader for deserialization." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.MBeanServer.deserialize\\(String, byte\\[\\]\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use MBeanServer.getClassLoaderRepository() to obtain the class loader repository and use it to deserialize." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.MBeanServer.deserialize\\(String, ObjectName, byte\\[\\]\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use getClassLoader to obtain the class loader for deserialization." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.Thread.destroy\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method was originally designed to destroy this thread without any cleanup. Any monitors it held would have remained locked. However, the method was never implemented. If if were to be implemented, it would be deadlock-prone in much the manner of Thread.suspend(). If the target thread held a lock protecting a critical system resource when it was destroyed, no thread could ever access this resource again. If another thread ever attempted to lock this resource, deadlock would result. Such deadlocks typically manifest themselves as \"frozen processes\". For more information, see Why are Thread.stop, Thread.suspend and Thread.resume Deprecated?." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.disable\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setEnabled(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.MenuItem.disable\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setEnabled(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JComponent.disable\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by java.awt.Component.setEnabled(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.table.TableColumn.disableResizedPosting\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. as of Java 2 platform v1.3" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.FocusManager.disableSwingFocusManager\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. as of 1.4, replaced by KeyboardFocusManager.setDefaultFocusTraversalPolicy(FocusTraversalPolicy)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.Skeleton.dispatch\\(Remote, RemoteCall, int, long\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RemoteCall.done\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RemoteRef.done\\(RemoteCall\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. 1.2 style stubs no longer use this method. Instead of using a sequence of method calls to the remote reference (newCall, invoke, and done), a stub uses a single method, invoke(Remote, Method, Object\\[\\], int), on the remote reference to carry out parameter marshalling, remote method executing and unmarshalling of the return value." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.metal.MetalComboBoxUI.editablePropertyChanged\\(PropertyChangeEvent\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.4." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.enable\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setEnabled(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.MenuItem.enable\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setEnabled(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JComponent.enable\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by java.awt.Component.setEnabled(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.enable\\(boolean\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setEnabled(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.MenuItem.enable\\(boolean\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setEnabled(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.table.TableColumn.enableResizedPosting\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. as of Java 2 platform v1.3" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.net.URLEncoder.encode\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. The resulting string may vary depending on the platform's default encoding. Instead, use the encode(String,String) method to specify the encoding." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.security.SignatureSpi.engineGetParameter\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.security.SignatureSpi.engineSetParameter\\(String, Object\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Replaced by engineSetParameter." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.datatransfer.DataFlavor.equals\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As inconsistent with hashCode() contract, use isMimeTypeEqual(String) instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.ServerRequest.except\\(Any\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. use set_exception()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RemoteCall.executeCall\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.Any.extract_Principal()") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Deprecated by CORBA 2.2." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.SwingUtilities.findFocusOwner\\(Component\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.4, replaced by KeyboardFocusManager.getFocusOwner()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.ORB.get_current()") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. use resolve_initial_references." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.security.Security.getAlgorithmProperty\\(String, String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method used to return the value of a proprietary property in the master file of the \"SUN\" Cryptographic Service Provider in order to determine how to parse algorithm-specific parameters. Use the new provider-based and algorithm-independent AlgorithmParameters and KeyFactory engine classes (introduced in the J2SE version 1.2 platform) instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.CallableStatement.getBigDecimal\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. use getBigDecimal(int parameterIndex) or getBigDecimal(String parameterName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.ResultSet.getBigDecimal\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.ResultSet.getBigDecimal\\(String, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Polygon.getBoundingBox\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getBounds()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.String.getBytes\\(int, int, byte\\[\\], int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method does not properly convert characters into bytes. As of JDK 1.1, the preferred way to do this is via the String.getBytes() method, which uses the platform's default charset." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Graphics.getClipRect\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getClipBounds()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JMenuBar.getComponentAtIndex\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by getComponent(int i)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JPopupMenu.getComponentAtIndex\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by Container.getComponent(int)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.CheckboxGroup.getCurrent\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getSelectedCheckbox()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.getCursorType\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Component.getCursor()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Time.getDate\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.getDate\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.get(Calendar.DAY_OF_MONTH)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Time.getDay\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.getDay\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.get(Calendar.DAY_OF_WEEK)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.net.URLConnection.getDefaultRequestProperty\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. The instance specific getRequestProperty method should be used after an appropriate instance of URLConnection is obtained." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LogStream.getDefaultStream\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.GaugeMonitorMBean.getDerivedGauge\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by GaugeMonitorMBean.getDerivedGauge(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.CounterMonitor.getDerivedGauge\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by CounterMonitor.getDerivedGauge(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.StringMonitor.getDerivedGauge\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by StringMonitor.getDerivedGauge(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.StringMonitorMBean.getDerivedGauge\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by StringMonitorMBean.getDerivedGauge(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.GaugeMonitor.getDerivedGauge\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by GaugeMonitor.getDerivedGauge(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.CounterMonitorMBean.getDerivedGauge\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by CounterMonitorMBean.getDerivedGauge(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.GaugeMonitorMBean.getDerivedGaugeTimeStamp\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by GaugeMonitorMBean.getDerivedGaugeTimeStamp(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.CounterMonitor.getDerivedGaugeTimeStamp\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by CounterMonitor.getDerivedGaugeTimeStamp(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.StringMonitor.getDerivedGaugeTimeStamp\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by StringMonitor.getDerivedGaugeTimeStamp(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.StringMonitorMBean.getDerivedGaugeTimeStamp\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by StringMonitorMBean.getDerivedGaugeTimeStamp(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.GaugeMonitor.getDerivedGaugeTimeStamp\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by GaugeMonitor.getDerivedGaugeTimeStamp(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.CounterMonitorMBean.getDerivedGaugeTimeStamp\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by CounterMonitorMBean.getDerivedGaugeTimeStamp(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.basic.BasicSplitPaneUI.getDividerBorderSize\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3, instead set the border on the divider." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.bind.Validator.getEventHandler\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. since JAXB2.0" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Toolkit.getFontList\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. see GraphicsEnvironment.getAvailableFontFamilyNames()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.text.LabelView.getFontMetrics\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. FontMetrics are not used for glyph rendering when running in the JDK." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Toolkit.getFontMetrics\\(Font\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.2, replaced by the Font method getLineMetrics." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Toolkit.getFontPeer\\(String, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. see java.awt.GraphicsEnvironment#getAllFonts" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Date.getHours\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.getHours\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.get(Calendar.HOUR_OF_DAY)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.SecurityManager.getInCheck\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This type of security checking is not recommended. It is recommended that the checkPermission call be used instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RemoteCall.getInputStream\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.KeyStroke.getKeyStroke\\(char, boolean\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. use getKeyStroke(char)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.AbstractButton.getLabel\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Replaced by getText" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Scrollbar.getLineIncrement\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getUnitIncrement()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.Runtime.getLocalizedInputStream\\(InputStream\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK 1.1, the preferred way to translate a byte stream in the local encoding into a character stream in Unicode is via the InputStreamReader and BufferedReader classes." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.Runtime.getLocalizedOutputStream\\(OutputStream\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK 1.1, the preferred way to translate a Unicode character stream into a byte stream in the local encoding is via the OutputStreamWriter, BufferedWriter, and PrintWriter classes." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.DriverManager.getLogStream\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.FontMetrics.getMaxDecent\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1.1, replaced by getMaxDescent()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JRootPane.getMenuBar\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Swing version 1.0.3 replaced by getJMenuBar()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JInternalFrame.getMenuBar\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Swing version 1.0.3, replaced by getJMenuBar()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Date.getMinutes\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.getMinutes\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.get(Calendar.MINUTE)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Time.getMonth\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.getMonth\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.get(Calendar.MONTH)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JComponent.getNextFocusableComponent\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.4, replaced by FocusTraversalPolicy." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.MonitorMBean.getObservedObject\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by MonitorMBean.getObservedObjects()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.Monitor.getObservedObject\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by Monitor.getObservedObjects()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.Operation.getOperation\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.Skeleton.getOperations\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.ComponentOrientation.getOrientation\\(ResourceBundle\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of J2SE 1.4, use ComponentOrientation.getOrientation(java.util.Locale)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RemoteCall.getOutputStream\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LogStream.getOutputStream\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Scrollbar.getPageIncrement\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getBlockIncrement()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.security.Signature.getParameter\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.MenuComponent.getPeer\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, programs should not directly manipulate peers." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.getPeer\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, programs should not directly manipulate peers; replaced by boolean isDisplayable()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Font.getPeer\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Font rendering is now platform independent." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.bind.Validator.getProperty\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. since JAXB2.0" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RemoteCall.getResultStream\\(boolean\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Date.getSeconds\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.getSeconds\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.get(Calendar.SECOND)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RMIClassLoader.getSecurityContext\\(ClassLoader\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement. As of the Java 2 platform v1.2, RMI no longer uses this method to obtain a class loader's security context." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LoaderHandler.getSecurityContext\\(ClassLoader\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JList.getSelectedValues\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK 1.7, replaced by JList.getSelectedValuesList()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JPasswordField.getText\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.2, replaced by getPassword." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JPasswordField.getText\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.2, replaced by getPassword." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.CounterMonitor.getThreshold\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by CounterMonitor.getThreshold(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.CounterMonitorMBean.getThreshold\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by CounterMonitorMBean.getThreshold(ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.getTimezoneOffset\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by -(Calendar.get(Calendar.ZONE_OFFSET) + Calendar.get(Calendar.DST_OFFSET)) / (60 * 1000)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.net.DatagramSocketImpl.getTTL\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. use getTimeToLive instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.net.MulticastSocket.getTTL\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. use the getTimeToLive method instead, which returns an int instead of a byte." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.ResultSet.getUnicodeStream\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. use getCharacterStream in place of getUnicodeStream" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.ResultSet.getUnicodeStream\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. use getCharacterStream instead" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.ScrollPaneLayout.getViewportBorderBounds\\(JScrollPane\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version Swing1.1 replaced by JScrollPane.getViewportBorderBounds()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Scrollbar.getVisible\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getVisibleAmount()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Time.getYear\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.getYear\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.get(Calendar.YEAR)- 1900." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.gotFocus\\(Event, Object\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by processFocusEvent(FocusEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.handleEvent\\(Event\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1 replaced by processEvent(AWTEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Window.hide\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.5, replaced by Window.setVisible(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.hide\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setVisible(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Dialog.hide\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.5, replaced by setVisible(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.SecurityManager.inClass\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This type of security checking is not recommended. It is recommended that the checkPermission call be used instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.SecurityManager.inClassLoader\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This type of security checking is not recommended. It is recommended that the checkPermission call be used instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.Any.insert_Principal(Principal)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Deprecated by CORBA 2.2." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.text.html.HTMLEditorKit.InsertHTMLTextAction.insertAtBoundry\\(JEditorPane, HTMLDocument, int, Element, String, HTML.Tag, HTML.Tag\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3, use insertAtBoundary" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.TextArea.insertText\\(String, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by insert(String, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Container.insets\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getInsets()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.inside\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by contains(int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Rectangle.inside\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by contains(int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Polygon.inside\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by contains(int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RemoteRef.invoke\\(RemoteCall\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. 1.2 style stubs no longer use this method. Instead of using a sequence of method calls to the remote reference (newCall, invoke, and done), a stub uses a single method, invoke(Remote, Method, Object\\[\\], int), on the remote reference to carry out parameter marshalling, remote method executing and unmarshalling of the return value." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.DynamicImplementation.invoke\\(ServerRequest\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Deprecated by Portable Object Adapter" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JViewport.isBackingStoreEnabled\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3, replaced by getScrollMode()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.FocusManager.isFocusManagerEnabled\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.4, replaced by KeyboardFocusManager.getDefaultFocusTraversalPolicy()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.isFocusTraversable\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.4, replaced by isFocusable()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.Character.isJavaLetter\\(char\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Replaced by isJavaIdentifierStart(char)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.Character.isJavaLetterOrDigit\\(char\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Replaced by isJavaIdentifierPart(char)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JComponent.isManagingFocus\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.4, replaced by Component.setFocusTraversalKeys(int, Set) and Container.setFocusCycleRoot(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.List.isSelected\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by isIndexSelected(int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.Character.isSpace\\(char\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Replaced by isWhitespace(char)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.dgc.VMID.isUnique\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.bind.Unmarshaller.isValidating\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. since JAXB2.0, please see Unmarshaller.getSchema()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.keyDown\\(Event, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by processKeyEvent(KeyEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.keyUp\\(Event, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by processKeyEvent(KeyEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Container.layout\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by doLayout()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.layout\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by doLayout()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.ScrollPane.layout\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by doLayout()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RMIClassLoader.loadClass\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by loadClass(String,String) method" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LoaderHandler.loadClass\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LoaderHandler.loadClass\\(URL, String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Container.locate\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getComponentAt(int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.locate\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getComponentAt(int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.location\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getLocation()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LogStream.log\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.lostFocus\\(Event, Object\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by processFocusEvent(FocusEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.TextArea.minimumSize\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getMinimumSize()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Container.minimumSize\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getMinimumSize()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.TextField.minimumSize\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getMinimumSize()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.minimumSize\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getMinimumSize()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.List.minimumSize\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getMinimumSize()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.TextField.minimumSize\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getMinimumSize(int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.List.minimumSize\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getMinimumSize(int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.TextArea.minimumSize\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getMinimumSize(int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.text.View.modelToView\\(int, Shape\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.mouseDown\\(Event, int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by processMouseEvent(MouseEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.mouseDrag\\(Event, int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by processMouseMotionEvent(MouseEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.mouseEnter\\(Event, int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by processMouseEvent(MouseEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.mouseExit\\(Event, int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by processMouseEvent(MouseEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.mouseMove\\(Event, int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by processMouseMotionEvent(MouseEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.mouseUp\\(Event, int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by processMouseEvent(MouseEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.move\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setLocation(int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Rectangle.move\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setLocation(int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.Principal.name\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Deprecated by CORBA 2.2." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.Principal.name\\(byte\\[\\]\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Deprecated by CORBA 2.2." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RemoteRef.newCall\\(RemoteObject, Operation\\[\\], int, long\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. 1.2 style stubs no longer use this method. Instead of using a sequence of method calls on the stub's the remote reference (newCall, invoke, and done), a stub uses a single method, invoke(Remote, Method, Object\\[\\], int), on the remote reference to carry out parameter marshalling, remote method executing and unmarshalling of the return value." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.stream.XMLInputFactory.newInstance\\(String, ClassLoader\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method has been deprecated to maintain API consistency. All newInstance methods have been replaced with corresponding newFactory methods. The replacement XMLInputFactory.newFactory(java.lang.String, java.lang.ClassLoader) method defines no changes in behavior." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.stream.XMLEventFactory.newInstance\\(String, ClassLoader\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method has been deprecated to maintain API consistency. All newInstance methods have been replaced with corresponding newFactory methods. The replacement XMLEventFactory.newFactory(java.lang.String, java.lang.ClassLoader) method defines no changes in behavior." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.stream.XMLOutputFactory.newInstance\\(String, ClassLoader\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method has been deprecated because it returns an instance of XMLInputFactory, which is of the wrong class. Use the new method XMLOutputFactory.newFactory(java.lang.String, java.lang.ClassLoader) instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.nextFocus\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by transferFocus()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.datatransfer.DataFlavor.normalizeMimeType\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.activation.ActivationDataFlavor.normalizeMimeType\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.datatransfer.DataFlavor.normalizeMimeTypeParameter\\(String, String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.activation.ActivationDataFlavor.normalizeMimeTypeParameter\\(String, String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.tree.DefaultTreeSelectionModel.notifyPathChange\\(Vector, TreePath\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.7" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.ServerRequest.op_name()") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. use operation()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.ServerRequest.params\\(NVList\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. use the method arguments" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.parse\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by DateFormat.parse(String s)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LogStream.parseLevel\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.MenuComponent.postEvent\\(Event\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by dispatchEvent." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.MenuContainer.postEvent\\(Event\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1 replaced by dispatchEvent(AWTEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Window.postEvent\\(Event\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1 replaced by dispatchEvent(AWTEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.postEvent\\(Event\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by dispatchEvent(AWTEvent)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.image.renderable.RenderContext.preConcetenateTransform\\(AffineTransform\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. replaced by preConcatenateTransform(AffineTransform)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.TextArea.preferredSize\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getPreferredSize()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Container.preferredSize\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getPreferredSize()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.TextField.preferredSize\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getPreferredSize()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.preferredSize\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getPreferredSize()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.List.preferredSize\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getPreferredSize()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.TextField.preferredSize\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getPreferredSize(int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.List.preferredSize\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getPreferredSize(int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.TextArea.preferredSize\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getPreferredSize(int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.portable.InputStream.read_Principal()") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Deprecated by CORBA 2.2." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.io.ObjectInputStream.readLine\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method does not properly convert bytes to characters. see DataInputStream for the details and alternatives." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.registry.RegistryHandler.registryImpl\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement. As of the Java 2 platform v1.2, RMI no longer uses the RegistryHandler to obtain the registry's implementation." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.registry.RegistryHandler.registryStub\\(String, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement. As of the Java 2 platform v1.2, RMI no longer uses the RegistryHandler to obtain the registry's stub." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RemoteCall.releaseInputStream\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RemoteCall.releaseOutputStream\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.metal.MetalComboBoxUI.removeListeners\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.4." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.TextArea.replaceText\\(String, int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by replaceRange(String, int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JComponent.requestDefaultFocus\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.4, replaced by FocusTraversalPolicy.getDefaultComponent(Container).requestFocus()" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Window.reshape\\(int, int, int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setBounds(int, int, int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.reshape\\(int, int, int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setBounds(int, int, int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Rectangle.reshape\\(int, int, int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setBounds(int, int, int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.resize\\(Dimension\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setSize(Dimension)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.resize\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setSize(int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Rectangle.resize\\(int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setSize(int, int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.ServerRequest.result\\(Any\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. use the method set_result" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.ThreadGroup.resume\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method is used solely in conjunction with Thread.suspend and ThreadGroup.suspend, both of which have been deprecated, as they are inherently deadlock-prone. See Thread.suspend() for details." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.Thread.resume\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method exists solely for use with Thread.suspend(), which has been deprecated because it is deadlock-prone. For more information, see Why are Thread.stop, Thread.suspend and Thread.resume Deprecated?." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.System.runFinalizersOnExit\\(boolean\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method is inherently unsafe. It may result in finalizers being called on live objects while other threads are concurrently manipulating those objects, resulting in erratic behavior or deadlock." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.Runtime.runFinalizersOnExit\\(boolean\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method is inherently unsafe. It may result in finalizers being called on live objects while other threads are concurrently manipulating those objects, resulting in erratic behavior or deadlock." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Properties.save\\(OutputStream, String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method does not throw an IOException if an I/O error occurs while saving the property list. The preferred way to save a properties list is via the store(OutputStream out, String comments) method or the storeToXML(OutputStream os, String comment) method." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.net.MulticastSocket.send\\(DatagramPacket, byte\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the following code or its equivalent instead: ...... int ttl = mcastSocket.getTimeToLive(); mcastSocket.setTimeToLive(newttl); mcastSocket.send(p); mcastSocket.setTimeToLive(ttl); ......" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JViewport.setBackingStoreEnabled\\(boolean\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Java 2 platform v1.3, replaced by setScrollMode()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.CheckboxGroup.setCurrent\\(Checkbox\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setSelectedCheckbox(Checkbox)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Frame.setCursor\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Component.setCursor(Cursor)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Time.setDate\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.setDate\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.set(Calendar.DAY_OF_MONTH, int date)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.net.URLConnection.setDefaultRequestProperty\\(String, String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. The instance specific setRequestProperty method should be used after an appropriate instance of URLConnection is obtained. Invoking this method will have no effect." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LogStream.setDefaultStream\\(PrintStream\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.TextField.setEchoCharacter\\(char\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setEchoChar(char)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.bind.Validator.setEventHandler\\(ValidationEventHandler\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. since JAXB2.0" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Date.setHours\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.setHours\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.set(Calendar.HOUR_OF_DAY, int hours)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.AbstractButton.setLabel\\(String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Replaced by setText(text)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Scrollbar.setLineIncrement\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setUnitIncrement(int)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.DriverManager.setLogStream\\(PrintStream\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.AttributeValueExp.setMBeanServer\\(MBeanServer\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method has no effect. The MBean Server used to obtain an attribute value is QueryEval.getMBeanServer()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.ValueExp.setMBeanServer\\(MBeanServer\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method is not needed because a ValueExp can access the MBean server in which it is being evaluated by using QueryEval.getMBeanServer()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.StringValueExp.setMBeanServer\\(MBeanServer\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JRootPane.setMenuBar\\(JMenuBar\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Swing version 1.0.3 replaced by setJMenuBar(JMenuBar menu)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JInternalFrame.setMenuBar\\(JMenuBar\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Swing version 1.0.3 replaced by setJMenuBar(JMenuBar m)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Date.setMinutes\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.setMinutes\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.set(Calendar.MINUTE, int minutes)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.event.KeyEvent.setModifiers\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. as of JDK1.1.4" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Time.setMonth\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.setMonth\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.set(Calendar.MONTH, int month)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.List.setMultipleSelections\\(boolean\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setMultipleMode(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JComponent.setNextFocusableComponent\\(Component\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of 1.4, replaced by FocusTraversalPolicy" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.MonitorMBean.setObservedObject\\(ObjectName\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by MonitorMBean.addObservedObject(javax.management.ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.Monitor.setObservedObject\\(ObjectName\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by Monitor.addObservedObject(javax.management.ObjectName)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LogStream.setOutputStream\\(OutputStream\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Scrollbar.setPageIncrement\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setBlockIncrement()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.security.Signature.setParameter\\(String, Object\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use setParameter." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.bind.Validator.setProperty\\(String, Object\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. since JAXB2.0" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.RemoteStub.setRef\\(RemoteStub, RemoteRef\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement. The setRef method is not needed since RemoteStubs can be created with the RemoteStub(RemoteRef) CONSTRUCTOR_CALLor." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Date.setSeconds\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.setSeconds\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.set(Calendar.SECOND, int seconds)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.CounterMonitor.setThreshold\\(Number\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by CounterMonitor.setInitThreshold(java.lang.Number)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.monitor.CounterMonitorMBean.setThreshold\\(Number\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JMX 1.2, replaced by CounterMonitorMBean.setInitThreshold(java.lang.Number)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.net.DatagramSocketImpl.setTTL\\(byte\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. use setTimeToLive instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.net.MulticastSocket.setTTL\\(byte\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. use the setTimeToLive method instead, which uses int instead of byte as the type for ttl." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.PreparedStatement.setUnicodeStream\\(int, InputStream, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.sql.rowset.BaseRowSet.setUnicodeStream\\(int, InputStream, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. getCharacterStream should be used in its place" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.net.URLStreamHandler.setURL\\(URL, String, String, int, String, String\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use setURL(URL, String, String, int, String, String, String, String);" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.bind.Unmarshaller.setValidating\\(boolean\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. since JAXB2.0, please see Unmarshaller.setSchema(javax.xml.validation.Schema)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Time.setYear\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.setYear\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.set(Calendar.YEAR, year + 1900)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Window.show\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.5, replaced by Window.setVisible(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.show\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setVisible(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Dialog.show\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.5, replaced by setVisible(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.show\\(boolean\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by setVisible(boolean)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.Component.size\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by getSize()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JTable.sizeColumnsToFit\\(boolean\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of Swing version 1.0.3, replaced by doLayout()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.ThreadGroup.stop\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method is inherently unsafe. See Thread.stop() for details." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.Thread.stop\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method is inherently unsafe. Stopping a thread with Thread.stop causes it to unlock all of the monitors that it has locked (as a natural consequence of the unchecked ThreadDeath exception propagating up the stack). If any of the objects previously protected by these monitors were in an inconsistent state, the damaged objects become visible to other threads, potentially resulting in arbitrary behavior. Many uses of stop should be replaced by code that simply modifies some variable to indicate that the target thread should stop running. The target thread should check this variable regularly, and return from its run method in an orderly fashion if the variable indicates that it is to stop running. If the target thread waits for long periods (on a condition variable, for example), the interrupt method should be used to interrupt the wait. For more information, see Why are Thread.stop, Thread.suspend and Thread.resume Deprecated?." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.Thread.stop\\(Throwable\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method is inherently unsafe. See Thread.stop() for details. An additional danger of this method is that it may be used to generate exceptions that the target thread is unprepared to handle (including checked exceptions that the thread could not possibly throw, were it not for this method). For more information, see Why are Thread.stop, Thread.suspend and Thread.resume Deprecated?." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.ThreadGroup.suspend\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method is inherently deadlock-prone. See Thread.suspend() for details." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.Thread.suspend\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method has been deprecated, as it is inherently deadlock-prone. If the target thread holds a lock on the monitor protecting a critical system resource when it is suspended, no thread can access this resource until the target thread is resumed. If the thread that would resume the target thread attempts to lock this monitor prior to calling resume, deadlock results. Such deadlocks typically manifest themselves as \"frozen\" processes. For more information, see Why are Thread.stop, Thread.suspend and Thread.resume Deprecated?." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.toGMTString\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by DateFormat.format(Date date), using a GMT TimeZone." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.toLocaleString\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by DateFormat.format(Date date)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.Operation.toString\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LogStream.toString\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.io.ByteArrayOutputStream.toString\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method does not properly convert bytes into characters. As of JDK 1.1, the preferred way to do this is via the toString(String enc) method, which takes an encoding-name argument, or the toString() method, which uses the platform's default character encoding." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.io.File.toURL\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method does not automatically escape characters that are illegal in URLs. It is recommended that new code convert an abstract pathname into a URL by first converting it into a URI, via the toURI method, and then converting the URI into a URL via the URI.toURL method." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.plaf.metal.MetalScrollPaneUI.uninstallListeners\\(JScrollPane\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Replaced by MetalScrollPaneUI.uninstallListeners(JComponent)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date.UTC\\(int, int, int, int, int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.set(year + 1900, month, date, hrs, min, sec) or GregorianCalendar(year + 1900, month, date, hrs, min, sec), using a UTC TimeZone, followed by Calendar.getTime().getTime()." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.bind.Validator.validate\\(Object\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. since JAXB2.0" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.xml.bind.Validator.validateRoot\\(Object\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. since JAXB2.0" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.text.View.viewToModel\\(float, float, Shape\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. " ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.portable.OutputStream.write_Principal(Principal)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Deprecated by CORBA 2.2." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LogStream.write\\(byte\\[\\], int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.LogStream.write\\(int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.io.ObjectOutputStream.PutField.write\\(ObjectOutput\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method does not write the values contained by this PutField object in a proper format, and may result in corruption of the serialization stream. The correct way to write PutField data is by calling the ObjectOutputStream.writeFields() method." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.io.DataInputStream.readLine\\(\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Deprecated By JDK 6. \n" + 
                                "            This method does not properly convert bytes to characters. As of JDK 1.1, the preferred way to read lines of text is via the BufferedReader.readLine() method. Programs that use the DataInputStream class to read lines can be converted to use the BufferedReader class by replacing code of the form:\n" + 
                                "            ```java\n" + 
                                "               DataInputStream d = new DataInputStream(in);\n" + 
                                "            with:\n" + 
                                "             BufferedReader d\n" + 
                                "                  = new BufferedReader(new InputStreamReader(in));\n" + 
                                "            ```\n" + 
                                "            ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.swing.JComponent.reshape\\(int, int, int, int\\)") .at(TypeReferenceLocation.METHOD) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "            Deprecated By JDK 6. \n" + 
                                "            As of JDK 5, replaced by Component.setBounds(int, int, int, int).\n" + 
                                "            Moves and resizes this component.\n" + 
                                "            ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.management.AttributeValueExp\\(\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. An instance created with this CONSTRUCTOR_CALLor cannot be used in a query." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Date\\(int, int, int\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. instead use the CONSTRUCTOR_CALLor Date(long date)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date\\(int, int, int\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.set(year + 1900, month, date) or GregorianCalendar(year + 1900, month, date)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date\\(int, int, int, int, int\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.set(year + 1900, month, date, hrs, min) or GregorianCalendar(year + 1900, month, date, hrs, min)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date\\(int, int, int, int, int, int\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by Calendar.set(year + 1900, month, date, hrs, min, sec) or GregorianCalendar(year + 1900, month, date, hrs, min, sec)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.util.Date\\(java.lang.String\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JDK version 1.1, replaced by DateFormat.parse(String s)." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.awt.event.KeyEvent\\(java.awt.Component, int, long, int, int\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. as of JDK1.1" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.Operation\\(java.lang.String\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.RMISecurityException\\(java.lang.String\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.RMISecurityException\\(java.lang.String, java.lang.String\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.ServerRuntimeException\\(java.lang.String, java.lang.Exception\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.rmi.server.SkeletonMismatchException\\(java.lang.String\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. no replacement" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.net.Socket\\(java.net.InetAddress, int, boolean\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use DatagramSocket instead for UDP transport." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.net.Socket\\(java.lang.String, int, boolean\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use DatagramSocket instead for UDP transport." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.String\\(byte\\[\\], int\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method does not properly convert bytes into characters. As of JDK 1.1, the preferred way to do this is via the String CONSTRUCTOR_CALLors that take a Charset, charset name, or that use the platform's default charset." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.lang.String\\(byte\\[\\], int, int, int\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. This method does not properly convert bytes into characters. As of JDK 1.1, the preferred way to do this is via the String CONSTRUCTOR_CALLors that take a Charset, charset name, or that use the platform's default charset." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("org.omg.CORBA.TCKind\\(int\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Do not use this CONSTRUCTOR_CALLor as this method should be private according to the OMG specification. Use TCKind.from_int(int) instead." ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Time\\(int, int, int\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. Use the CONSTRUCTOR_CALLor that takes a milliseconds value in place of this CONSTRUCTOR_CALLor" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.sql.Timestamp\\(int, int, int, int, int, int, int\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. instead use the CONSTRUCTOR_CALLor Timestamp(long millis)" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("java.io.StreamTokenizer\\(java.io.InputStream\\)") .at(TypeReferenceLocation.CONSTRUCTOR_CALL) ) .perform( Iteration.over().perform( Hint.withText( "<![CDATA[\n" + 
                                "                 Deprecated By JDK 6 - As of JDK version 1.1, the preferred way to tokenize an input stream is to convert it into a character stream, for example:\n" + 
                                "    \n" + 
                                "                ```java\n" + 
                                "                    Reader r = new BufferedReader(new InputStreamReader(is));\n" + 
                                "                    StreamTokenizer st = new StreamTokenizer(r);\n" + 
                                "                ```\n" + 
                                "            ]]>" ).withEffort( 0 )
                    )
                    .endIteration()
                    )

                    .addRule()
                    .when(
                    JavaClass.references("javax.jws.HandlerChain.name") .at(TypeReferenceLocation.TYPE) ) .perform( Iteration.over().perform( Hint.withText( "Deprecated By JDK 6. As of JSR-181 2.0 with no replacement." ).withEffort( 0 )
                    )
                    .endIteration()
                    );

        return null;
    }
    // @formatter:on
}
