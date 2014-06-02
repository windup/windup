package org.apache.wicket.markup.html.form;

import org.apache.wicket.validation.validator.*;
import org.apache.wicket.model.*;
import org.apache.wicket.markup.*;
import org.apache.wicket.util.value.*;
import org.apache.wicket.util.lang.*;
import java.util.*;
import org.apache.wicket.validation.*;
import org.apache.wicket.util.convert.*;

public class NumberTextField<N extends Number> extends TextField<N>{
    private static final long serialVersionUID=1L;
    private static final Locale HTML5_LOCALE;
    private RangeValidator<N> validator;
    private N minimum;
    private N maximum;
    public NumberTextField(final String id){
        this(id,(IModel)null);
    }
    public NumberTextField(final String id,final IModel<N> model){
        this(id,model,null);
    }
    public NumberTextField(final String id,final IModel<N> model,final Class<N> type){
        super(id,model,type);
        this.validator=null;
        this.minimum=null;
        this.maximum=null;
    }
    public NumberTextField<N> setMinimum(final N minimum){
        this.minimum=(Number)minimum;
        return this;
    }
    public NumberTextField<N> setMaximum(final N maximum){
        this.maximum=(Number)maximum;
        return this;
    }
    public void onConfigure(){
        super.onConfigure();
        if(this.validator!=null){
            this.remove(this.validator);
        }
        this.add(this.validator=new RangeValidator<N>(this.getMinValue(),this.getMaxValue()));
    }
    private N getMinValue(){
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     0: aload_0         /* this */
        //     1: getfield        org/apache/wicket/markup/html/form/NumberTextField.minimum:Ljava/lang/Number;
        //     4: ifnull          15
        //     7: aload_0         /* this */
        //     8: getfield        org/apache/wicket/markup/html/form/NumberTextField.minimum:Ljava/lang/Number;
        //    11: astore_1        /* result */
        //    12: goto            25
        //    15: aload_0         /* this */
        //    16: invokespecial   org/apache/wicket/markup/html/form/NumberTextField.getNumberType:()Ljava/lang/Class;
        //    19: astore_2        /* numberType */
        //    20: aload_2         /* numberType */
        //    21: invokestatic    org/apache/wicket/util/lang/Numbers.getMinValue:(Ljava/lang/Class;)Ljava/lang/Number;
        //    24: astore_1        /* result */
        //    25: aload_1         /* result */
        //    26: areturn        
        //    Signature:
        //  ()TN;
        //    LocalVariableTable:
        //  Start  Length  Slot  Name        Signature
        //  -----  ------  ----  ----------  ----------------------------------------------------
        //  12     3       1     result      Ljava/lang/Number;
        //  20     5       2     numberType  Ljava/lang/Class;
        //  0      27      0     this        Lorg/apache/wicket/markup/html/form/NumberTextField;
        //  25     2       1     result      Ljava/lang/Number;
        //    LocalVariableTypeTable:
        //  Start  Length  Slot  Name        Signature
        //  -----  ------  ----  ----------  ---------------------------------------------------------
        //  12     3       1     result      TN;
        //  20     5       2     numberType  Ljava/lang/Class<TN;>;
        //  0      27      0     this        Lorg/apache/wicket/markup/html/form/NumberTextField<TN;>;
        //  25     2       1     result      TN;
        // 
        // The error that occurred was:
        // 
        // java.lang.ClassCastException: com.strobel.assembler.metadata.CoreMetadataFactory$UnresolvedType cannot be cast to com.strobel.assembler.metadata.CompoundTypeReference
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubType(MetadataHelper.java:1211)
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubType(MetadataHelper.java:585)
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubtypeUncheckedInternal(MetadataHelper.java:557)
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubTypeUnchecked(MetadataHelper.java:537)
        //     at com.strobel.assembler.metadata.MetadataHelper.isConvertible(MetadataHelper.java:518)
        //     at com.strobel.assembler.metadata.MetadataHelper.isConvertible(MetadataHelper.java:499)
        //     at com.strobel.assembler.metadata.MetadataHelper.isAssignableFrom(MetadataHelper.java:574)
        //     at com.strobel.assembler.metadata.MetadataHelper.findCommonSuperTypeCore(MetadataHelper.java:220)
        //     at com.strobel.assembler.metadata.MetadataHelper.findCommonSuperType(MetadataHelper.java:172)
        //     at com.strobel.assembler.ir.Frame.merge(Frame.java:368)
        //     at com.strobel.assembler.ir.Frame.merge(Frame.java:254)
        //     at com.strobel.decompiler.ast.AstBuilder.performStackAnalysis(AstBuilder.java:2164)
        //     at com.strobel.decompiler.ast.AstBuilder.build(AstBuilder.java:108)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:210)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:756)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:654)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:531)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:498)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:140)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:129)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:104)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at org.jboss.windup.engine.decompiler.procyon.ProcyonDecompiler.decompileType(ProcyonDecompiler.java:289)
        //     at org.jboss.windup.engine.decompiler.procyon.ProcyonDecompiler.decompileArchive(ProcyonDecompiler.java:212)
        //     at org.jboss.windup.addon.config.operation.ProcyonDecompilerOperation.perform(ProcyonDecompilerOperation.java:42)
        //     at org.jboss.windup.addon.config.operation.ProcyonDecompilerOperation.perform(ProcyonDecompilerOperation.java:22)
        //     at org.jboss.windup.addon.config.operation.ruleelement.AbstractIterationOperator.perform(AbstractIterationOperator.java:31)
        //     at org.jboss.windup.addon.config.operation.GraphOperation.perform(GraphOperation.java:24)
        //     at org.jboss.windup.addon.config.operation.Iteration.perform(Iteration.java:149)
        //     at org.jboss.windup.addon.config.operation.Iteration.perform(Iteration.java:134)
        //     at org.ocpsoft.rewrite.config.DefaultOperationBuilder$DefaultCompositeOperation.perform(DefaultOperationBuilder.java:56)
        //     at org.ocpsoft.rewrite.config.RuleBuilder.perform(RuleBuilder.java:136)
        //     at org.ocpsoft.rewrite.config.DefaultOperationBuilder$DefaultCompositeOperation.perform(DefaultOperationBuilder.java:56)
        //     at org.ocpsoft.rewrite.config.RuleBuilder.perform(RuleBuilder.java:136)
        //     at org.jboss.windup.addon.config.GraphSubset.perform(GraphSubset.java:126)
        //     at org.jboss.windup.engine.ConfigurationProcessorImpl.run(ConfigurationProcessorImpl.java:41)
        //     at org.jboss.windup.engine.WindupProcessorImpl.execute(WindupProcessorImpl.java:29)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor$1.call(ClassLoaderInterceptor.java:59)
        //     at org.jboss.forge.furnace.util.ClassLoaders.executeIn(ClassLoaders.java:34)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor.invoke(ClassLoaderInterceptor.java:75)
        //     at org.jboss.windup.engine.WindupProcessorImpl_$$_javassist_8f3ba1da-d311-4ce7-8ddb-f8110d3fe868.execute(WindupProcessorImpl_$$_javassist_8f3ba1da-d311-4ce7-8ddb-f8110d3fe868.java)
        //     at org.jboss.windup.tests.application.WindupArchitectureTest.testRunWindup(WindupArchitectureTest.java:58)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor$1.call(ClassLoaderInterceptor.java:59)
        //     at org.jboss.forge.furnace.util.ClassLoaders.executeIn(ClassLoaders.java:34)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor.invoke(ClassLoaderInterceptor.java:75)
        //     at org.jboss.windup.tests.application.WindupArchitectureTest_$$_javassist_27e69200-7c4c-45df-aad4-ddaa544aabcb.testRunWindup(WindupArchitectureTest_$$_javassist_27e69200-7c4c-45df-aad4-ddaa544aabcb.java)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderAdapterCallback$1.call(ClassLoaderAdapterCallback.java:98)
        //     at org.jboss.forge.furnace.util.ClassLoaders.executeIn(ClassLoaders.java:34)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderAdapterCallback.invoke(ClassLoaderAdapterCallback.java:71)
        //     at org.jboss.windup.tests.application.WindupArchitectureTest_$$_javassist_aa333864-7a51-45d4-a93e-f72c739d8d5f.testRunWindup(WindupArchitectureTest_$$_javassist_aa333864-7a51-45d4-a93e-f72c739d8d5f.java)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.arquillian.ForgeTestMethodExecutor.invoke(ForgeTestMethodExecutor.java:143)
        //     at org.jboss.arquillian.container.test.impl.execution.RemoteTestExecuter.execute(RemoteTestExecuter.java:109)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.invokeObservers(EventContextImpl.java:99)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:81)
        //     at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:145)
        //     at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:116)
        //     at org.jboss.arquillian.core.impl.EventImpl.fire(EventImpl.java:67)
        //     at org.jboss.arquillian.container.test.impl.execution.ClientTestExecuter.execute(ClientTestExecuter.java:57)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.invokeObservers(EventContextImpl.java:99)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:81)
        //     at org.jboss.arquillian.container.test.impl.client.ContainerEventController.createContext(ContainerEventController.java:142)
        //     at org.jboss.arquillian.container.test.impl.client.ContainerEventController.createTestContext(ContainerEventController.java:129)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.test.impl.TestContextHandler.createTestContext(TestContextHandler.java:102)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.test.impl.TestContextHandler.createClassContext(TestContextHandler.java:84)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.test.impl.TestContextHandler.createSuiteContext(TestContextHandler.java:65)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:145)
        //     at org.jboss.arquillian.test.impl.EventTestRunnerAdaptor.test(EventTestRunnerAdaptor.java:111)
        //     at org.jboss.arquillian.junit.Arquillian$6.evaluate(Arquillian.java:266)
        //     at org.jboss.arquillian.junit.Arquillian$4.evaluate(Arquillian.java:229)
        //     at org.jboss.arquillian.junit.Arquillian.multiExecute(Arquillian.java:317)
        //     at org.jboss.arquillian.junit.Arquillian.access$200(Arquillian.java:46)
        //     at org.jboss.arquillian.junit.Arquillian$5.evaluate(Arquillian.java:243)
        //     at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:271)
        //     at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:70)
        //     at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)
        //     at org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)
        //     at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)
        //     at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)
        //     at org.junit.runners.ParentRunner.access$000(ParentRunner.java:53)
        //     at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)
        //     at org.jboss.arquillian.junit.Arquillian$2.evaluate(Arquillian.java:188)
        //     at org.jboss.arquillian.junit.Arquillian.multiExecute(Arquillian.java:317)
        //     at org.jboss.arquillian.junit.Arquillian.access$200(Arquillian.java:46)
        //     at org.jboss.arquillian.junit.Arquillian$3.evaluate(Arquillian.java:202)
        //     at org.junit.runners.ParentRunner.run(ParentRunner.java:309)
        //     at org.jboss.arquillian.junit.Arquillian.run(Arquillian.java:150)
        //     at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:50)
        //     at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:467)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:683)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:390)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:197)
        // 
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     0: aload_0         /* this */
        //     1: getfield        org/apache/wicket/markup/html/form/NumberTextField.minimum:Ljava/lang/Number;
        //     4: ifnull          15
        //     7: aload_0         /* this */
        //     8: getfield        org/apache/wicket/markup/html/form/NumberTextField.minimum:Ljava/lang/Number;
        //    11: astore_1        /* result */
        //    12: goto            25
        //    15: aload_0         /* this */
        //    16: invokespecial   org/apache/wicket/markup/html/form/NumberTextField.getNumberType:()Ljava/lang/Class;
        //    19: astore_2        /* numberType */
        //    20: aload_2         /* numberType */
        //    21: invokestatic    org/apache/wicket/util/lang/Numbers.getMinValue:(Ljava/lang/Class;)Ljava/lang/Number;
        //    24: astore_1        /* result */
        //    25: aload_1         /* result */
        //    26: areturn        
        //    Signature:
        //  ()TN;
        //    LocalVariableTable:
        //  Start  Length  Slot  Name        Signature
        //  -----  ------  ----  ----------  ----------------------------------------------------
        //  12     3       1     result      Ljava/lang/Number;
        //  20     5       2     numberType  Ljava/lang/Class;
        //  0      27      0     this        Lorg/apache/wicket/markup/html/form/NumberTextField;
        //  25     2       1     result      Ljava/lang/Number;
        //    LocalVariableTypeTable:
        //  Start  Length  Slot  Name        Signature
        //  -----  ------  ----  ----------  ---------------------------------------------------------
        //  12     3       1     result      TN;
        //  20     5       2     numberType  Ljava/lang/Class<TN;>;
        //  0      27      0     this        Lorg/apache/wicket/markup/html/form/NumberTextField<TN;>;
        //  25     2       1     result      TN;
        // 
        // The error that occurred was:
        // 
        // java.lang.ClassCastException: com.strobel.assembler.metadata.CoreMetadataFactory$UnresolvedType cannot be cast to com.strobel.assembler.metadata.CompoundTypeReference
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubType(MetadataHelper.java:1211)
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubType(MetadataHelper.java:585)
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubtypeUncheckedInternal(MetadataHelper.java:557)
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubTypeUnchecked(MetadataHelper.java:537)
        //     at com.strobel.assembler.metadata.MetadataHelper.isConvertible(MetadataHelper.java:518)
        //     at com.strobel.assembler.metadata.MetadataHelper.isConvertible(MetadataHelper.java:499)
        //     at com.strobel.assembler.metadata.MetadataHelper.isAssignableFrom(MetadataHelper.java:574)
        //     at com.strobel.assembler.metadata.MetadataHelper.findCommonSuperTypeCore(MetadataHelper.java:220)
        //     at com.strobel.assembler.metadata.MetadataHelper.findCommonSuperType(MetadataHelper.java:172)
        //     at com.strobel.assembler.ir.Frame.merge(Frame.java:368)
        //     at com.strobel.assembler.ir.Frame.merge(Frame.java:254)
        //     at com.strobel.decompiler.ast.AstBuilder.performStackAnalysis(AstBuilder.java:2164)
        //     at com.strobel.decompiler.ast.AstBuilder.build(AstBuilder.java:108)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:210)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:756)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:654)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:531)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:498)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:140)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:129)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:104)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at org.jboss.windup.engine.decompiler.procyon.ProcyonDecompiler.decompileType(ProcyonDecompiler.java:289)
        //     at org.jboss.windup.engine.decompiler.procyon.ProcyonDecompiler.decompileArchive(ProcyonDecompiler.java:212)
        //     at org.jboss.windup.addon.config.operation.ProcyonDecompilerOperation.perform(ProcyonDecompilerOperation.java:42)
        //     at org.jboss.windup.addon.config.operation.ProcyonDecompilerOperation.perform(ProcyonDecompilerOperation.java:22)
        //     at org.jboss.windup.addon.config.operation.ruleelement.AbstractIterationOperator.perform(AbstractIterationOperator.java:31)
        //     at org.jboss.windup.addon.config.operation.GraphOperation.perform(GraphOperation.java:24)
        //     at org.jboss.windup.addon.config.operation.Iteration.perform(Iteration.java:149)
        //     at org.jboss.windup.addon.config.operation.Iteration.perform(Iteration.java:134)
        //     at org.ocpsoft.rewrite.config.DefaultOperationBuilder$DefaultCompositeOperation.perform(DefaultOperationBuilder.java:56)
        //     at org.ocpsoft.rewrite.config.RuleBuilder.perform(RuleBuilder.java:136)
        //     at org.ocpsoft.rewrite.config.DefaultOperationBuilder$DefaultCompositeOperation.perform(DefaultOperationBuilder.java:56)
        //     at org.ocpsoft.rewrite.config.RuleBuilder.perform(RuleBuilder.java:136)
        //     at org.jboss.windup.addon.config.GraphSubset.perform(GraphSubset.java:126)
        //     at org.jboss.windup.engine.ConfigurationProcessorImpl.run(ConfigurationProcessorImpl.java:41)
        //     at org.jboss.windup.engine.WindupProcessorImpl.execute(WindupProcessorImpl.java:29)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor$1.call(ClassLoaderInterceptor.java:59)
        //     at org.jboss.forge.furnace.util.ClassLoaders.executeIn(ClassLoaders.java:34)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor.invoke(ClassLoaderInterceptor.java:75)
        //     at org.jboss.windup.engine.WindupProcessorImpl_$$_javassist_8f3ba1da-d311-4ce7-8ddb-f8110d3fe868.execute(WindupProcessorImpl_$$_javassist_8f3ba1da-d311-4ce7-8ddb-f8110d3fe868.java)
        //     at org.jboss.windup.tests.application.WindupArchitectureTest.testRunWindup(WindupArchitectureTest.java:58)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor$1.call(ClassLoaderInterceptor.java:59)
        //     at org.jboss.forge.furnace.util.ClassLoaders.executeIn(ClassLoaders.java:34)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor.invoke(ClassLoaderInterceptor.java:75)
        //     at org.jboss.windup.tests.application.WindupArchitectureTest_$$_javassist_27e69200-7c4c-45df-aad4-ddaa544aabcb.testRunWindup(WindupArchitectureTest_$$_javassist_27e69200-7c4c-45df-aad4-ddaa544aabcb.java)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderAdapterCallback$1.call(ClassLoaderAdapterCallback.java:98)
        //     at org.jboss.forge.furnace.util.ClassLoaders.executeIn(ClassLoaders.java:34)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderAdapterCallback.invoke(ClassLoaderAdapterCallback.java:71)
        //     at org.jboss.windup.tests.application.WindupArchitectureTest_$$_javassist_aa333864-7a51-45d4-a93e-f72c739d8d5f.testRunWindup(WindupArchitectureTest_$$_javassist_aa333864-7a51-45d4-a93e-f72c739d8d5f.java)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.arquillian.ForgeTestMethodExecutor.invoke(ForgeTestMethodExecutor.java:143)
        //     at org.jboss.arquillian.container.test.impl.execution.RemoteTestExecuter.execute(RemoteTestExecuter.java:109)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.invokeObservers(EventContextImpl.java:99)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:81)
        //     at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:145)
        //     at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:116)
        //     at org.jboss.arquillian.core.impl.EventImpl.fire(EventImpl.java:67)
        //     at org.jboss.arquillian.container.test.impl.execution.ClientTestExecuter.execute(ClientTestExecuter.java:57)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.invokeObservers(EventContextImpl.java:99)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:81)
        //     at org.jboss.arquillian.container.test.impl.client.ContainerEventController.createContext(ContainerEventController.java:142)
        //     at org.jboss.arquillian.container.test.impl.client.ContainerEventController.createTestContext(ContainerEventController.java:129)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.test.impl.TestContextHandler.createTestContext(TestContextHandler.java:102)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.test.impl.TestContextHandler.createClassContext(TestContextHandler.java:84)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.test.impl.TestContextHandler.createSuiteContext(TestContextHandler.java:65)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:145)
        //     at org.jboss.arquillian.test.impl.EventTestRunnerAdaptor.test(EventTestRunnerAdaptor.java:111)
        //     at org.jboss.arquillian.junit.Arquillian$6.evaluate(Arquillian.java:266)
        //     at org.jboss.arquillian.junit.Arquillian$4.evaluate(Arquillian.java:229)
        //     at org.jboss.arquillian.junit.Arquillian.multiExecute(Arquillian.java:317)
        //     at org.jboss.arquillian.junit.Arquillian.access$200(Arquillian.java:46)
        //     at org.jboss.arquillian.junit.Arquillian$5.evaluate(Arquillian.java:243)
        //     at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:271)
        //     at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:70)
        //     at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)
        //     at org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)
        //     at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)
        //     at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)
        //     at org.junit.runners.ParentRunner.access$000(ParentRunner.java:53)
        //     at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)
        //     at org.jboss.arquillian.junit.Arquillian$2.evaluate(Arquillian.java:188)
        //     at org.jboss.arquillian.junit.Arquillian.multiExecute(Arquillian.java:317)
        //     at org.jboss.arquillian.junit.Arquillian.access$200(Arquillian.java:46)
        //     at org.jboss.arquillian.junit.Arquillian$3.evaluate(Arquillian.java:202)
        //     at org.junit.runners.ParentRunner.run(ParentRunner.java:309)
        //     at org.jboss.arquillian.junit.Arquillian.run(Arquillian.java:150)
        //     at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:50)
        //     at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:467)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:683)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:390)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:197)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
    private N getMaxValue(){
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     0: aload_0         /* this */
        //     1: getfield        org/apache/wicket/markup/html/form/NumberTextField.maximum:Ljava/lang/Number;
        //     4: ifnull          15
        //     7: aload_0         /* this */
        //     8: getfield        org/apache/wicket/markup/html/form/NumberTextField.maximum:Ljava/lang/Number;
        //    11: astore_1        /* result */
        //    12: goto            25
        //    15: aload_0         /* this */
        //    16: invokespecial   org/apache/wicket/markup/html/form/NumberTextField.getNumberType:()Ljava/lang/Class;
        //    19: astore_2        /* numberType */
        //    20: aload_2         /* numberType */
        //    21: invokestatic    org/apache/wicket/util/lang/Numbers.getMaxValue:(Ljava/lang/Class;)Ljava/lang/Number;
        //    24: astore_1        /* result */
        //    25: aload_1         /* result */
        //    26: areturn        
        //    Signature:
        //  ()TN;
        //    LocalVariableTable:
        //  Start  Length  Slot  Name        Signature
        //  -----  ------  ----  ----------  ----------------------------------------------------
        //  12     3       1     result      Ljava/lang/Number;
        //  20     5       2     numberType  Ljava/lang/Class;
        //  0      27      0     this        Lorg/apache/wicket/markup/html/form/NumberTextField;
        //  25     2       1     result      Ljava/lang/Number;
        //    LocalVariableTypeTable:
        //  Start  Length  Slot  Name        Signature
        //  -----  ------  ----  ----------  ---------------------------------------------------------
        //  12     3       1     result      TN;
        //  20     5       2     numberType  Ljava/lang/Class<TN;>;
        //  0      27      0     this        Lorg/apache/wicket/markup/html/form/NumberTextField<TN;>;
        //  25     2       1     result      TN;
        // 
        // The error that occurred was:
        // 
        // java.lang.ClassCastException: com.strobel.assembler.metadata.CoreMetadataFactory$UnresolvedType cannot be cast to com.strobel.assembler.metadata.CompoundTypeReference
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubType(MetadataHelper.java:1211)
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubType(MetadataHelper.java:585)
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubtypeUncheckedInternal(MetadataHelper.java:557)
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubTypeUnchecked(MetadataHelper.java:537)
        //     at com.strobel.assembler.metadata.MetadataHelper.isConvertible(MetadataHelper.java:518)
        //     at com.strobel.assembler.metadata.MetadataHelper.isConvertible(MetadataHelper.java:499)
        //     at com.strobel.assembler.metadata.MetadataHelper.isAssignableFrom(MetadataHelper.java:574)
        //     at com.strobel.assembler.metadata.MetadataHelper.findCommonSuperTypeCore(MetadataHelper.java:220)
        //     at com.strobel.assembler.metadata.MetadataHelper.findCommonSuperType(MetadataHelper.java:172)
        //     at com.strobel.assembler.ir.Frame.merge(Frame.java:368)
        //     at com.strobel.assembler.ir.Frame.merge(Frame.java:254)
        //     at com.strobel.decompiler.ast.AstBuilder.performStackAnalysis(AstBuilder.java:2164)
        //     at com.strobel.decompiler.ast.AstBuilder.build(AstBuilder.java:108)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:210)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:756)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:654)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:531)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:498)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:140)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:129)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:104)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at org.jboss.windup.engine.decompiler.procyon.ProcyonDecompiler.decompileType(ProcyonDecompiler.java:289)
        //     at org.jboss.windup.engine.decompiler.procyon.ProcyonDecompiler.decompileArchive(ProcyonDecompiler.java:212)
        //     at org.jboss.windup.addon.config.operation.ProcyonDecompilerOperation.perform(ProcyonDecompilerOperation.java:42)
        //     at org.jboss.windup.addon.config.operation.ProcyonDecompilerOperation.perform(ProcyonDecompilerOperation.java:22)
        //     at org.jboss.windup.addon.config.operation.ruleelement.AbstractIterationOperator.perform(AbstractIterationOperator.java:31)
        //     at org.jboss.windup.addon.config.operation.GraphOperation.perform(GraphOperation.java:24)
        //     at org.jboss.windup.addon.config.operation.Iteration.perform(Iteration.java:149)
        //     at org.jboss.windup.addon.config.operation.Iteration.perform(Iteration.java:134)
        //     at org.ocpsoft.rewrite.config.DefaultOperationBuilder$DefaultCompositeOperation.perform(DefaultOperationBuilder.java:56)
        //     at org.ocpsoft.rewrite.config.RuleBuilder.perform(RuleBuilder.java:136)
        //     at org.ocpsoft.rewrite.config.DefaultOperationBuilder$DefaultCompositeOperation.perform(DefaultOperationBuilder.java:56)
        //     at org.ocpsoft.rewrite.config.RuleBuilder.perform(RuleBuilder.java:136)
        //     at org.jboss.windup.addon.config.GraphSubset.perform(GraphSubset.java:126)
        //     at org.jboss.windup.engine.ConfigurationProcessorImpl.run(ConfigurationProcessorImpl.java:41)
        //     at org.jboss.windup.engine.WindupProcessorImpl.execute(WindupProcessorImpl.java:29)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor$1.call(ClassLoaderInterceptor.java:59)
        //     at org.jboss.forge.furnace.util.ClassLoaders.executeIn(ClassLoaders.java:34)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor.invoke(ClassLoaderInterceptor.java:75)
        //     at org.jboss.windup.engine.WindupProcessorImpl_$$_javassist_8f3ba1da-d311-4ce7-8ddb-f8110d3fe868.execute(WindupProcessorImpl_$$_javassist_8f3ba1da-d311-4ce7-8ddb-f8110d3fe868.java)
        //     at org.jboss.windup.tests.application.WindupArchitectureTest.testRunWindup(WindupArchitectureTest.java:58)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor$1.call(ClassLoaderInterceptor.java:59)
        //     at org.jboss.forge.furnace.util.ClassLoaders.executeIn(ClassLoaders.java:34)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor.invoke(ClassLoaderInterceptor.java:75)
        //     at org.jboss.windup.tests.application.WindupArchitectureTest_$$_javassist_27e69200-7c4c-45df-aad4-ddaa544aabcb.testRunWindup(WindupArchitectureTest_$$_javassist_27e69200-7c4c-45df-aad4-ddaa544aabcb.java)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderAdapterCallback$1.call(ClassLoaderAdapterCallback.java:98)
        //     at org.jboss.forge.furnace.util.ClassLoaders.executeIn(ClassLoaders.java:34)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderAdapterCallback.invoke(ClassLoaderAdapterCallback.java:71)
        //     at org.jboss.windup.tests.application.WindupArchitectureTest_$$_javassist_aa333864-7a51-45d4-a93e-f72c739d8d5f.testRunWindup(WindupArchitectureTest_$$_javassist_aa333864-7a51-45d4-a93e-f72c739d8d5f.java)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.arquillian.ForgeTestMethodExecutor.invoke(ForgeTestMethodExecutor.java:143)
        //     at org.jboss.arquillian.container.test.impl.execution.RemoteTestExecuter.execute(RemoteTestExecuter.java:109)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.invokeObservers(EventContextImpl.java:99)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:81)
        //     at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:145)
        //     at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:116)
        //     at org.jboss.arquillian.core.impl.EventImpl.fire(EventImpl.java:67)
        //     at org.jboss.arquillian.container.test.impl.execution.ClientTestExecuter.execute(ClientTestExecuter.java:57)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.invokeObservers(EventContextImpl.java:99)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:81)
        //     at org.jboss.arquillian.container.test.impl.client.ContainerEventController.createContext(ContainerEventController.java:142)
        //     at org.jboss.arquillian.container.test.impl.client.ContainerEventController.createTestContext(ContainerEventController.java:129)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.test.impl.TestContextHandler.createTestContext(TestContextHandler.java:102)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.test.impl.TestContextHandler.createClassContext(TestContextHandler.java:84)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.test.impl.TestContextHandler.createSuiteContext(TestContextHandler.java:65)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:145)
        //     at org.jboss.arquillian.test.impl.EventTestRunnerAdaptor.test(EventTestRunnerAdaptor.java:111)
        //     at org.jboss.arquillian.junit.Arquillian$6.evaluate(Arquillian.java:266)
        //     at org.jboss.arquillian.junit.Arquillian$4.evaluate(Arquillian.java:229)
        //     at org.jboss.arquillian.junit.Arquillian.multiExecute(Arquillian.java:317)
        //     at org.jboss.arquillian.junit.Arquillian.access$200(Arquillian.java:46)
        //     at org.jboss.arquillian.junit.Arquillian$5.evaluate(Arquillian.java:243)
        //     at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:271)
        //     at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:70)
        //     at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)
        //     at org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)
        //     at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)
        //     at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)
        //     at org.junit.runners.ParentRunner.access$000(ParentRunner.java:53)
        //     at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)
        //     at org.jboss.arquillian.junit.Arquillian$2.evaluate(Arquillian.java:188)
        //     at org.jboss.arquillian.junit.Arquillian.multiExecute(Arquillian.java:317)
        //     at org.jboss.arquillian.junit.Arquillian.access$200(Arquillian.java:46)
        //     at org.jboss.arquillian.junit.Arquillian$3.evaluate(Arquillian.java:202)
        //     at org.junit.runners.ParentRunner.run(ParentRunner.java:309)
        //     at org.jboss.arquillian.junit.Arquillian.run(Arquillian.java:150)
        //     at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:50)
        //     at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:467)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:683)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:390)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:197)
        // 
        // 
        // This method could not be decompiled.
        // 
        // Original Bytecode:
        // 
        //     0: aload_0         /* this */
        //     1: getfield        org/apache/wicket/markup/html/form/NumberTextField.maximum:Ljava/lang/Number;
        //     4: ifnull          15
        //     7: aload_0         /* this */
        //     8: getfield        org/apache/wicket/markup/html/form/NumberTextField.maximum:Ljava/lang/Number;
        //    11: astore_1        /* result */
        //    12: goto            25
        //    15: aload_0         /* this */
        //    16: invokespecial   org/apache/wicket/markup/html/form/NumberTextField.getNumberType:()Ljava/lang/Class;
        //    19: astore_2        /* numberType */
        //    20: aload_2         /* numberType */
        //    21: invokestatic    org/apache/wicket/util/lang/Numbers.getMaxValue:(Ljava/lang/Class;)Ljava/lang/Number;
        //    24: astore_1        /* result */
        //    25: aload_1         /* result */
        //    26: areturn        
        //    Signature:
        //  ()TN;
        //    LocalVariableTable:
        //  Start  Length  Slot  Name        Signature
        //  -----  ------  ----  ----------  ----------------------------------------------------
        //  12     3       1     result      Ljava/lang/Number;
        //  20     5       2     numberType  Ljava/lang/Class;
        //  0      27      0     this        Lorg/apache/wicket/markup/html/form/NumberTextField;
        //  25     2       1     result      Ljava/lang/Number;
        //    LocalVariableTypeTable:
        //  Start  Length  Slot  Name        Signature
        //  -----  ------  ----  ----------  ---------------------------------------------------------
        //  12     3       1     result      TN;
        //  20     5       2     numberType  Ljava/lang/Class<TN;>;
        //  0      27      0     this        Lorg/apache/wicket/markup/html/form/NumberTextField<TN;>;
        //  25     2       1     result      TN;
        // 
        // The error that occurred was:
        // 
        // java.lang.ClassCastException: com.strobel.assembler.metadata.CoreMetadataFactory$UnresolvedType cannot be cast to com.strobel.assembler.metadata.CompoundTypeReference
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubType(MetadataHelper.java:1211)
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubType(MetadataHelper.java:585)
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubtypeUncheckedInternal(MetadataHelper.java:557)
        //     at com.strobel.assembler.metadata.MetadataHelper.isSubTypeUnchecked(MetadataHelper.java:537)
        //     at com.strobel.assembler.metadata.MetadataHelper.isConvertible(MetadataHelper.java:518)
        //     at com.strobel.assembler.metadata.MetadataHelper.isConvertible(MetadataHelper.java:499)
        //     at com.strobel.assembler.metadata.MetadataHelper.isAssignableFrom(MetadataHelper.java:574)
        //     at com.strobel.assembler.metadata.MetadataHelper.findCommonSuperTypeCore(MetadataHelper.java:220)
        //     at com.strobel.assembler.metadata.MetadataHelper.findCommonSuperType(MetadataHelper.java:172)
        //     at com.strobel.assembler.ir.Frame.merge(Frame.java:368)
        //     at com.strobel.assembler.ir.Frame.merge(Frame.java:254)
        //     at com.strobel.decompiler.ast.AstBuilder.performStackAnalysis(AstBuilder.java:2164)
        //     at com.strobel.decompiler.ast.AstBuilder.build(AstBuilder.java:108)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:210)
        //     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:756)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:654)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:531)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:498)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:140)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:129)
        //     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:104)
        //     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
        //     at org.jboss.windup.engine.decompiler.procyon.ProcyonDecompiler.decompileType(ProcyonDecompiler.java:289)
        //     at org.jboss.windup.engine.decompiler.procyon.ProcyonDecompiler.decompileArchive(ProcyonDecompiler.java:212)
        //     at org.jboss.windup.addon.config.operation.ProcyonDecompilerOperation.perform(ProcyonDecompilerOperation.java:42)
        //     at org.jboss.windup.addon.config.operation.ProcyonDecompilerOperation.perform(ProcyonDecompilerOperation.java:22)
        //     at org.jboss.windup.addon.config.operation.ruleelement.AbstractIterationOperator.perform(AbstractIterationOperator.java:31)
        //     at org.jboss.windup.addon.config.operation.GraphOperation.perform(GraphOperation.java:24)
        //     at org.jboss.windup.addon.config.operation.Iteration.perform(Iteration.java:149)
        //     at org.jboss.windup.addon.config.operation.Iteration.perform(Iteration.java:134)
        //     at org.ocpsoft.rewrite.config.DefaultOperationBuilder$DefaultCompositeOperation.perform(DefaultOperationBuilder.java:56)
        //     at org.ocpsoft.rewrite.config.RuleBuilder.perform(RuleBuilder.java:136)
        //     at org.ocpsoft.rewrite.config.DefaultOperationBuilder$DefaultCompositeOperation.perform(DefaultOperationBuilder.java:56)
        //     at org.ocpsoft.rewrite.config.RuleBuilder.perform(RuleBuilder.java:136)
        //     at org.jboss.windup.addon.config.GraphSubset.perform(GraphSubset.java:126)
        //     at org.jboss.windup.engine.ConfigurationProcessorImpl.run(ConfigurationProcessorImpl.java:41)
        //     at org.jboss.windup.engine.WindupProcessorImpl.execute(WindupProcessorImpl.java:29)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor$1.call(ClassLoaderInterceptor.java:59)
        //     at org.jboss.forge.furnace.util.ClassLoaders.executeIn(ClassLoaders.java:34)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor.invoke(ClassLoaderInterceptor.java:75)
        //     at org.jboss.windup.engine.WindupProcessorImpl_$$_javassist_8f3ba1da-d311-4ce7-8ddb-f8110d3fe868.execute(WindupProcessorImpl_$$_javassist_8f3ba1da-d311-4ce7-8ddb-f8110d3fe868.java)
        //     at org.jboss.windup.tests.application.WindupArchitectureTest.testRunWindup(WindupArchitectureTest.java:58)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor$1.call(ClassLoaderInterceptor.java:59)
        //     at org.jboss.forge.furnace.util.ClassLoaders.executeIn(ClassLoaders.java:34)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderInterceptor.invoke(ClassLoaderInterceptor.java:75)
        //     at org.jboss.windup.tests.application.WindupArchitectureTest_$$_javassist_27e69200-7c4c-45df-aad4-ddaa544aabcb.testRunWindup(WindupArchitectureTest_$$_javassist_27e69200-7c4c-45df-aad4-ddaa544aabcb.java)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderAdapterCallback$1.call(ClassLoaderAdapterCallback.java:98)
        //     at org.jboss.forge.furnace.util.ClassLoaders.executeIn(ClassLoaders.java:34)
        //     at org.jboss.forge.furnace.proxy.ClassLoaderAdapterCallback.invoke(ClassLoaderAdapterCallback.java:71)
        //     at org.jboss.windup.tests.application.WindupArchitectureTest_$$_javassist_aa333864-7a51-45d4-a93e-f72c739d8d5f.testRunWindup(WindupArchitectureTest_$$_javassist_aa333864-7a51-45d4-a93e-f72c739d8d5f.java)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.forge.arquillian.ForgeTestMethodExecutor.invoke(ForgeTestMethodExecutor.java:143)
        //     at org.jboss.arquillian.container.test.impl.execution.RemoteTestExecuter.execute(RemoteTestExecuter.java:109)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.invokeObservers(EventContextImpl.java:99)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:81)
        //     at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:145)
        //     at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:116)
        //     at org.jboss.arquillian.core.impl.EventImpl.fire(EventImpl.java:67)
        //     at org.jboss.arquillian.container.test.impl.execution.ClientTestExecuter.execute(ClientTestExecuter.java:57)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.invokeObservers(EventContextImpl.java:99)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:81)
        //     at org.jboss.arquillian.container.test.impl.client.ContainerEventController.createContext(ContainerEventController.java:142)
        //     at org.jboss.arquillian.container.test.impl.client.ContainerEventController.createTestContext(ContainerEventController.java:129)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.test.impl.TestContextHandler.createTestContext(TestContextHandler.java:102)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.test.impl.TestContextHandler.createClassContext(TestContextHandler.java:84)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.test.impl.TestContextHandler.createSuiteContext(TestContextHandler.java:65)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        //     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
        //     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        //     at java.lang.reflect.Method.invoke(Method.java:606)
        //     at org.jboss.arquillian.core.impl.ObserverImpl.invoke(ObserverImpl.java:94)
        //     at org.jboss.arquillian.core.impl.EventContextImpl.proceed(EventContextImpl.java:88)
        //     at org.jboss.arquillian.core.impl.ManagerImpl.fire(ManagerImpl.java:145)
        //     at org.jboss.arquillian.test.impl.EventTestRunnerAdaptor.test(EventTestRunnerAdaptor.java:111)
        //     at org.jboss.arquillian.junit.Arquillian$6.evaluate(Arquillian.java:266)
        //     at org.jboss.arquillian.junit.Arquillian$4.evaluate(Arquillian.java:229)
        //     at org.jboss.arquillian.junit.Arquillian.multiExecute(Arquillian.java:317)
        //     at org.jboss.arquillian.junit.Arquillian.access$200(Arquillian.java:46)
        //     at org.jboss.arquillian.junit.Arquillian$5.evaluate(Arquillian.java:243)
        //     at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:271)
        //     at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:70)
        //     at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)
        //     at org.junit.runners.ParentRunner$3.run(ParentRunner.java:238)
        //     at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:63)
        //     at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:236)
        //     at org.junit.runners.ParentRunner.access$000(ParentRunner.java:53)
        //     at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:229)
        //     at org.jboss.arquillian.junit.Arquillian$2.evaluate(Arquillian.java:188)
        //     at org.jboss.arquillian.junit.Arquillian.multiExecute(Arquillian.java:317)
        //     at org.jboss.arquillian.junit.Arquillian.access$200(Arquillian.java:46)
        //     at org.jboss.arquillian.junit.Arquillian$3.evaluate(Arquillian.java:202)
        //     at org.junit.runners.ParentRunner.run(ParentRunner.java:309)
        //     at org.jboss.arquillian.junit.Arquillian.run(Arquillian.java:150)
        //     at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:50)
        //     at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:467)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:683)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:390)
        //     at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:197)
        // 
        throw new IllegalStateException("An error occurred while decompiling this method.");
    }
    private Class<N> getNumberType(){
        Class<N> numberType=this.getType();
        if(numberType==null&&this.getModelObject()!=null){
            numberType=(Class<N>)this.getModelObject().getClass();
        }
        return numberType;
    }
    protected void onComponentTag(final ComponentTag tag){
        super.onComponentTag(tag);
        final IValueMap attributes=tag.getAttributes();
        if(this.minimum!=null){
            attributes.put((Object)"min",(Object)Objects.stringValue((Object)this.minimum));
        }
        else{
            attributes.remove((Object)"min");
        }
        if(this.maximum!=null){
            attributes.put((Object)"max",(Object)Objects.stringValue((Object)this.maximum));
        }
        else{
            attributes.remove((Object)"max");
        }
    }
    protected String getInputType(){
        return "number";
    }
    protected String getModelValue(){
        final N value=this.getModelObject();
        if(value==null){
            return "";
        }
        return Objects.stringValue((Object)value);
    }
    protected void convertInput(){
        final IConverter<N> converter=this.getConverter(this.getNumberType());
        try{
            this.setConvertedInput((N)converter.convertToObject(this.getInput(),NumberTextField.HTML5_LOCALE));
        }
        catch(ConversionException e){
            final ValidationError error=new ValidationError();
            if(e.getResourceKey()!=null){
                error.addMessageKey(e.getResourceKey());
            }
            if(e.getTargetType()!=null){
                error.addMessageKey("ConversionError."+Classes.simpleName(e.getTargetType()));
            }
            error.addMessageKey("ConversionError");
            final Locale locale=e.getLocale();
            if(locale!=null){
                error.setVariable("locale",locale);
            }
            error.setVariable("exception",e);
            final Map<String,Object> variables=(Map<String,Object>)e.getVariables();
            if(variables!=null){
                error.getVariables().putAll(variables);
            }
            this.error(error);
        }
    }
    static{
        HTML5_LOCALE=new Locale("en","","wicket-html5");
    }
}
