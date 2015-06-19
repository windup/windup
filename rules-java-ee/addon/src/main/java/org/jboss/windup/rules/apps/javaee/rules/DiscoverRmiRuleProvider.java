package org.jboss.windup.rules.apps.javaee.rules;

import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.RMIServiceModel;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Scans for classes with JAX-WS related annotations, and adds JAX-WS related metadata for these.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class DiscoverRmiRuleProvider extends AbstractRuleProvider
{
    private static Logger LOG = Logging.get(DiscoverRmiRuleProvider.class);
    private static final String RMI_INHERITANCE = "rmiInheritance";

    public DiscoverRmiRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverRmiRuleProvider.class)
                    .setPhase(MigrationRulesPhase.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        String ruleIDPrefix = getClass().getSimpleName();
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(JavaClass
                                .references("java.rmi.Remote")
                                .at(TypeReferenceLocation.IMPORT)
                                .as(RMI_INHERITANCE))
                    .perform(Iteration.over(RMI_INHERITANCE).perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                        {
                            extractMetadata(event, payload);
                        }
                    }).endIteration())
                    .withId(ruleIDPrefix + "_RMIInheritanceRule");
    }

    private void extractMetadata(GraphRewrite event, JavaTypeReferenceModel typeReference)
    {
        JavaClassService jcs = new JavaClassService(event.getGraphContext());
        
        //get the rmi interface class from the graph
        JavaClassModel jcm = getJavaClass(typeReference);
    	
        if(!isRemote(jcm)) {
        	LOG.warning("Is not remote: "+jcm.getQualifiedName());
        	return;
        }
        
    	LOG.info("Processing: "+typeReference);
        //sets to decompile
    	((SourceFileModel) typeReference.getFile()).setGenerateSourceReport(true);
        
        GraphService<RMIServiceModel> rmiService = new GraphService<>(event.getGraphContext(), RMIServiceModel.class);

        if(jcm!=null) {
        	//now, look to see whether it extends Remote
        	Iterator<JavaClassModel> impls = jcm.getImplementedBy().iterator();
        	if(impls.hasNext()) {
        		RMIServiceModel rmiServiceModel = rmiService.create();
            	rmiServiceModel.setInterface(jcm);

                LOG.info("RMI Interface: "+jcm.getQualifiedName());
                //find something that implements the interface
                while(impls.hasNext()) {
                	JavaClassModel implModel = impls.next();
                	LOG.info(" -- Impementations: "+implModel.getQualifiedName());
                	rmiServiceModel.setImplementationClass(implModel);

                	//create the source report for the RMI Implementation.
                	
                	for(JavaSourceFileModel source : jcs.getJavaSource(implModel.getQualifiedName())) {
                		source.setGenerateSourceReport(true);
                	}
                }
        	}
        	else {
        		LOG.info("No implementations for RMI Interface: "+jcm.getQualifiedName());
        		RMIServiceModel rmiServiceModel = rmiService.create();
            	rmiServiceModel.setInterface(jcm);
        	}
        }
    }
    
    public boolean isRemote(JavaClassModel jcm) {
		LOG.info("Class: "+jcm.getQualifiedName());
    	for(JavaClassModel im : jcm.getImplements()) {
    		if(StringUtils.equals("java.rmi.Remote",im.getQualifiedName())) {
    			return true;
    		}
			LOG.info(" - Implements: "+im.getQualifiedName());
		}
    	if(jcm.getExtends() != null) {
    		LOG.info(" - Extends: "+jcm.getExtends().getQualifiedName());
    		return (StringUtils.equals("java.rmi.Remote",jcm.getExtends().getQualifiedName()));
    	}
    	return false;
    }

    private JavaClassModel getJavaClass(JavaTypeReferenceModel javaTypeReference)
    {
        JavaClassModel result = null;
        FileModel originalFile = javaTypeReference.getFile();
        if (originalFile instanceof JavaSourceFileModel)
        {
            JavaSourceFileModel javaSource = (JavaSourceFileModel) originalFile;
            for (JavaClassModel javaClassModel : javaSource.getJavaClasses())
            {
                // there can be only one public one, and the annotated class should be public
                if (javaClassModel.isPublic() != null && javaClassModel.isPublic())
                {
                    result = javaClassModel;
                    break;
                }
            }

            if (result == null)
            {
                // no public classes found, so try to find any class (even non-public ones)
                result = javaSource.getJavaClasses().iterator().next();
            }
        }
        else if (originalFile instanceof JavaClassFileModel)
        {
            result = ((JavaClassFileModel) originalFile).getJavaClass();
        }
        else
        {
            LOG.warning("Unrecognized file type with annotation found at: \"" + originalFile.getFilePath() + "\"");
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "DiscoverEJBAnnotatedClasses";
    }
}