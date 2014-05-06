/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.jboss.windup.addon.config.example.providers;

import javax.enterprise.inject.Vetoed;

import org.jboss.windup.addon.config.WindupConfigurationProvider;
import org.jboss.windup.addon.config.example.java.Graph;
import org.jboss.windup.addon.config.example.java.MavenDependency;
import org.jboss.windup.addon.config.example.java.MavenPomFile;
import org.jboss.windup.addon.config.example.java.XMLFile;
import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.selectables.Selection;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@Vetoed
public class MavenExampleConfigurationProvider extends WindupConfigurationProvider
{

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()

                    .addRule()
                    .when(Selection.ofAll(XMLFile.class, "xmls")
                                .withDoctype("http://maven.apache.org/POM/4.0.0")
                    )
                    .perform(Iteration.over(XMLFile.class, "xmls", "pom").as(MavenPomFile.class)
                                .perform(
                                            Graph.replace(Selection.current(XMLFile.class))
                                                        .with(Selection.current(MavenPomFile.class))
                                )
                    )

                    .addRule()
                    .when(Selection.ofAll(MavenPomFile.class, "poms"))
                    .perform(Iteration.over(MavenPomFile.class, "poms", "pom")
                                .when(Selection.ofAll(MavenDependency.class, "methods")
                                            .blacklisted(false)
                                )
                                .perform(
                                            Graph.insert(Selection.current(MavenDependency.class))
                                                        .to(Selection.current(MavenPomFile.class))
                                )
                    );
    }

}
