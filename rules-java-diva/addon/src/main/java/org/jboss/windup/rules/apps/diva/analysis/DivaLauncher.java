package org.jboss.windup.rules.apps.diva.analysis;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectDependencyModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.diva.EnableTransactionAnalysisOption;
import org.jboss.windup.rules.apps.diva.model.DivaAppModel;
import org.jboss.windup.rules.apps.diva.model.DivaConstraintModel;
import org.jboss.windup.rules.apps.diva.model.DivaContextModel;
import org.jboss.windup.rules.apps.diva.model.DivaEndpointModel;
import org.jboss.windup.rules.apps.diva.model.DivaEntryMethodModel;
import org.jboss.windup.rules.apps.diva.model.DivaRequestConstraintModel;
import org.jboss.windup.rules.apps.diva.model.DivaRequestParamModel;
import org.jboss.windup.rules.apps.diva.model.DivaRestApiModel;
import org.jboss.windup.rules.apps.diva.model.DivaRestCallOpModel;
import org.jboss.windup.rules.apps.diva.service.DivaEntryMethodService;
import org.jboss.windup.rules.apps.java.archives.model.IgnoredArchiveModel;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.model.JarArchiveModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.jboss.windup.rules.apps.java.model.PropertiesModel;
import org.jboss.windup.rules.apps.java.model.WarArchiveModel;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.ibm.wala.cast.java.ipa.callgraph.JavaSourceAnalysisScope;
import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl;
import com.ibm.wala.cast.java.translator.jdt.ecj.ECJClassLoaderFactory;
import com.ibm.wala.classLoader.BinaryDirectoryTreeModule;
import com.ibm.wala.classLoader.ClassLoaderFactory;
import com.ibm.wala.classLoader.ClassLoaderFactoryImpl;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.classLoader.SourceDirectoryTreeModule;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeCT.AnnotationsReader.ConstantElementValue;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.strings.StringStuff;
import com.ibm.wala.util.warnings.Warnings;

import io.tackle.diva.Constants;
import io.tackle.diva.Context;
import io.tackle.diva.Context.EntryConstraint;
import io.tackle.diva.Framework;
import io.tackle.diva.Report;
import io.tackle.diva.Trace;
import io.tackle.diva.Util;
import io.tackle.diva.analysis.JDBCAnalysis;
import io.tackle.diva.analysis.JPAAnalysis;
import io.tackle.diva.analysis.QuarkusAnalysis;
import io.tackle.diva.analysis.ServletAnalysis;
import io.tackle.diva.analysis.SpringBootAnalysis;
import io.tackle.diva.irgen.DivaIRGen;
import io.tackle.diva.irgen.DivaSourceLoaderImpl;
import io.tackle.diva.irgen.FilteredClassHierarchy;
import io.tackle.diva.irgen.ModularAnalysisScope;

public class DivaLauncher extends GraphOperation {

    private static final Logger LOG = Logger.getLogger(DivaLauncher.class.getName());

    @Override
    public void perform(GraphRewrite event, EvaluationContext context) {

        Boolean enableDiva = (Boolean) event.getGraphContext().getOptionMap()
                .getOrDefault(EnableTransactionAnalysisOption.NAME, Boolean.FALSE);
        if (!enableDiva) {
            LOG.info("Skipping Diva analysis as " + EnableTransactionAnalysisOption.NAME + " option isn't set.");
            return;
        }
        try {
            Util.injectedCall(DivaIRGen.advices(), new String[] { "org.jboss.windup.rules.apps.diva.analysis" },
                    new String[] {}, DivaLauncher.class.getName() + ".launch", event, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void launch(GraphRewrite event, EvaluationContext context) throws Exception {
        GraphContext gc = event.getGraphContext();

        Boolean sourceMode = (Boolean) event.getGraphContext().getOptionMap().getOrDefault(SourceModeOption.NAME,
                Boolean.FALSE);

        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(gc);
        WindupJavaConfigurationModel javaCfg = WindupJavaConfigurationService.getJavaConfigurationModel(gc);

        List<? extends ProjectModel> projects = gc.getQuery(ProjectModel.class)
                .traverse(g -> g.filter(
                        __.out(ProjectModel.PROJECT_MODEL_TO_FILE).has(WindupFrame.TYPE_PROP, SourceFileModel.TYPE)))
                .toList(ProjectModel.class);
        List<? extends ProjectModel> notMaven = Util
                .makeList(Util.filter(projects, p -> !(p instanceof MavenProjectModel)));

        AnalysisScope scope;
        String[] stdlibs;
        ClassLoaderFactory clf;

        Path temp = Files.createTempDirectory("diva-temp");
        Util.LOGGER.info("tempdir=" + temp);

        if (sourceMode && !projects.isEmpty() && notMaven.isEmpty()) {

            ModularAnalysisScope mods = new ModularAnalysisScope();
            scope = mods;
            stdlibs = Framework.loadStandardLib(mods, temp);
            FileUtils.forceDeleteOnExit(temp.toFile());

            // For now, assume each p in projects has at-most-1 depending
            // p'. Partly due to wala's tree-not-dag class loaders (and class lookup
            // redundantly defined both in cha and loader-impl.)

            for (ProjectModel p : projects) {
                LOG.info("Project: " + p.toPrettyString());

                Stack<ProjectModel> todo = new Stack<>();
                todo.push(p);

                while (true) {
                    List<ProjectModel> deps = Util.makeList(
                            Util.filter(Util.map(p.getDependencies(), ProjectDependencyModel::getProjectModel),
                                    projects::contains));
                    if (deps.isEmpty())
                        break;
                    p = deps.get(0);
                    todo.push(p);
                }
                ClassLoaderReference parent = ClassLoaderReference.Application;
                while (!todo.isEmpty()) {
                    p = todo.pop();
                    File f = new File(p.getRootFileModel().getFilePath() + "/src/main/java");
                    if (f.exists()) {
                        parent = mods.findOrCreateModuleLoader(p.getName(), new SourceDirectoryTreeModule(f), parent);
                    }
                }
            }

            clf = new ClassLoaderFactoryImpl(scope.getExclusions()) {
                @Override
                protected IClassLoader makeNewClassLoader(ClassLoaderReference classLoaderReference,
                        IClassHierarchy cha, IClassLoader parent, AnalysisScope unused) throws IOException {
                    if (mods.moduleLoaderRefs().contains(classLoaderReference)) {
                        IClassLoader cl = new DivaSourceLoaderImpl(classLoaderReference, parent, cha, stdlibs);
                        cl.init(mods.getModules(classLoaderReference));
                        return cl;
                    } else {
                        return super.makeNewClassLoader(classLoaderReference, cha, parent, scope);
                    }
                }
            };

        } else {
            scope = new JavaSourceAnalysisScope() {
                @Override
                public boolean isApplicationLoader(IClassLoader loader) {
                    return loader.getReference().equals(ClassLoaderReference.Application)
                            || loader.getReference().equals(JavaSourceAnalysisScope.SOURCE);
                }
            };
            // add standard libraries to scope
            stdlibs = Framework.loadStandardLib(scope, temp);
            FileUtils.forceDeleteOnExit(temp.toFile());

            if (sourceMode) {
                List<String> sourceDirs = Util.makeList(Util.map(cfg.getInputPaths(), FileModel::getFilePath));
                LOG.info("Using root source dirs: " + sourceDirs + " due to non-maven projects: " + notMaven);

                for (String sourceDir : sourceDirs) {
                    scope.addToScope(JavaSourceAnalysisScope.SOURCE,
                            new SourceDirectoryTreeModule(new File(sourceDir)));
                }

            } else {
                for (ProjectModel p : projects) {
                    LOG.info("Project: " + p.toPrettyString());

                    FileModel rootFileModel = p.getRootFileModel();
                    if (rootFileModel instanceof WarArchiveModel) {
                        // use WEB-INF/classes and lib?
                        Path unzippedPath = Paths.get(((WarArchiveModel) rootFileModel).getUnzippedDirectory());
                        Path classRoot = unzippedPath.resolve("WEB-INF").resolve("classes");
                        if (classRoot.toFile().isDirectory()) {
                            scope.addToScope(ClassLoaderReference.Application,
                                    new BinaryDirectoryTreeModule(classRoot.toFile()));
                        }

                    } else if (rootFileModel instanceof JarArchiveModel) {
                        LOG.info("JAR: " + rootFileModel);
                        if (p instanceof MavenProjectModel) {
                            LOG.info(rootFileModel.getSHA1Hash() + " " + ((MavenProjectModel) p).getMavenIdentifier());
                        } else {
                            LOG.info(rootFileModel.getSHA1Hash() + " "
                                    + ((JarArchiveModel) rootFileModel).getArchiveName());
                        }
                        if (rootFileModel instanceof IgnoredArchiveModel)
                            continue;
                        scope.addToScope(ClassLoaderReference.Application,
                                new JarFileModule(new JarFile(rootFileModel.getFilePath())));
                    }
                }

            }

            clf = new ECJClassLoaderFactory(scope.getExclusions()) {
                @Override
                protected JavaSourceLoaderImpl makeSourceLoader(ClassLoaderReference classLoaderReference,
                        IClassHierarchy cha, IClassLoader parent) {
                    return new DivaSourceLoaderImpl(classLoaderReference, parent, cha, stdlibs);
                }
            };
        }

        DivaIRGen.init();

        // build the class hierarchy
        IClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope, clf);
        LOG.info(cha.getNumberOfClasses() + " classes");
        LOG.info(Warnings.asString());

        Set<IClass> relevantClasses = new HashSet<>();
        Set<IClass> appClasses = new HashSet<>();
        Framework.relevantJarsAnalysis(cha, relevantClasses, appClasses,
                c -> JDBCAnalysis.checkRelevance(c) || JPAAnalysis.checkRelevance(c));

        IClassHierarchy filteredCha = new FilteredClassHierarchy(cha, appClasses::contains);
        IClassHierarchy relevantCha = new FilteredClassHierarchy(cha, relevantClasses::contains);

        List<IMethod> entries = new ArrayList<>();
        entries.addAll(ServletAnalysis.getEntries(filteredCha));
        entries.addAll(SpringBootAnalysis.getEntries(filteredCha));
        entries.addAll(QuarkusAnalysis.getEntries(filteredCha));

        if (entries.isEmpty()) {
            LOG.info("Diva: found no entry methods for anlaysis");
            return;
        }

        List<IMethod> cgEntries = new ArrayList<>();
        cgEntries.addAll(entries);
        cgEntries.addAll(SpringBootAnalysis.getInits(relevantCha));

        JPAAnalysis.getEntities(relevantCha);

        AnalysisOptions options = new AnalysisOptions();
        Supplier<CallGraph> builder = Framework.chaCgBuilder(relevantCha, options, cgEntries,
                m -> relevantClasses.contains(m.getDeclaringClass()));

        LOG.info("building call graph...");
        CallGraph cg = builder.get();

        Framework fw = new Framework(cha, cg);

        for (CGNode n : cg) {
            if (entries.contains(n.getMethod())) {
                fw.recordContraint(new Context.EntryConstraint(n));
            }
        }
        fw.traverse(cg.getNode(0), ServletAnalysis.getContextualAnalysis(fw));

        List<Context> contexts = Context.calculateDefaultContexts(fw);
        // List<Context> contexts = Context.loadContexts(fw,
        // "/Users/aki/git/tackle-diva/dt-contexts.yml");

        DivaToWindup<DivaContextModel> report = new DivaToWindup<>(gc, DivaContextModel.class);

        DivaEntryMethodService entryMethodService = new DivaEntryMethodService(gc);
        GraphService<DivaRequestParamModel> requestParamService = new GraphService<>(gc, DivaRequestParamModel.class);

        for (Context cxt : contexts) {

            try {
                CGNode entry = null;
                for (Context.Constraint c : cxt) {
                    if (c instanceof Context.EntryConstraint) {
                        entry = ((Context.EntryConstraint) c).node();
                    }
                }
                if (entry != null) {
                    CGNode n = entry;
                    Trace.Visitor txAnalysis = JDBCAnalysis.getTransactionAnalysis(fw, cxt)
                            .with(SpringBootAnalysis.getTransactionAnalysis(fw, cxt)
                                    .with(JPAAnalysis.getTransactionAnalysis(fw, cxt)
                                            .with(QuarkusAnalysis.getTransactionAnalysis(fw, cxt))));

                    fw.calculateTransactions(entry, cxt, new Util.LazyReport() {
                        @Override
                        public void accept(Report.Builder txs) {
                            report.add((Report.Named map) -> {
                                map.put(DivaToWindup.CONSTRAINTS, (Report r) -> {
                                    DivaToWindup<DivaConstraintModel> cs = (DivaToWindup<DivaConstraintModel>) r;
                                    for (Context.Constraint c : cxt) {
                                        if (c.category().equals(Report.ENTRY)) {
                                            IMethod m = ((EntryConstraint) c).node().getMethod();
                                            DivaEntryMethodModel model = entryMethodService.getOrCreate(
                                                    StringStuff.jvmToBinaryName(
                                                            m.getDeclaringClass().getName().toString()),
                                                    m.getName().toString());
                                            for (Annotation a : Util.getAnnotations(m)) {
                                                // fill rest api if any
                                                if (a.getType().getName() == Constants.LJavaxWsRsGET) {
                                                    model.setHttpMethod("GET");
                                                } else if (a.getType().getName() == Constants.LJavaxWsRsPOST) {
                                                    model.setHttpMethod("POST");
                                                } else if (a.getType().getName() == Constants.LJavaxWsRsPATCH) {
                                                    model.setHttpMethod("PATCH");
                                                } else if (a.getType().getName() == Constants.LJavaxWsRsDELETE) {
                                                    model.setHttpMethod("DELETE");
                                                }
                                                if (a.getType().getName() == Constants.LJavaxWsRsPath) {
                                                    model.setUrlPath(DivaLauncher.stripBraces(((ConstantElementValue) a
                                                            .getNamedArguments().get("value")).val.toString()));
                                                }
                                                // @TODO. @WebServlet("/app")
                                            }
                                            cs.add(model);

                                        } else if (c.category().equals(Report.HTTP_PARAM)) {
                                            DivaRequestParamModel model = requestParamService.getOrCreateByProperties(
                                                    DivaRequestParamModel.PARAM_NAME, c.type(),
                                                    DivaRequestParamModel.PARAM_VALUE, c.value());
                                            cs.add(GraphService.addTypeToModel(gc, model,
                                                    DivaRequestConstraintModel.class));
                                        }
                                    }
                                });
                                map.put(Report.TRANSACTIONS, txs);
                            });
                        }
                    }, txAnalysis);
                }
                gc.getGraph().tx().commit();
            } catch (RuntimeException e) {
                gc.getGraph().tx().rollback();
            }
        }

        endpointResolution(gc, projects);

        LOG.info("Diva: DONE");
    }

    public static String stripBraces(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            b.append(s.charAt(i));
            if (s.charAt(i) == '{') {
                for (; s.charAt(i) != '}'; i++)
                    ;
                b.append('}');
            }
        }
        return b.toString();
    }

    public static void endpointResolution(GraphContext gc, List<? extends ProjectModel> projects) {

        // 1) obtaining project -> application.properties mapping

        Map<String, Properties> appProps = new LinkedHashMap<>();
        FileService files = new FileService(gc);
        for (ProjectModel p : projects) {
            String targetPath = Paths.get(p.getRootFileModel().getFilePath())
                    .resolve("src/main/resources/application.properties").toString();
            PropertiesModel file = (PropertiesModel) files.findByPath(targetPath);
            if (file != null) {
                try {
                    appProps.put(p.getName(), file.getProperties());
                } catch (IOException e) {
                }
            }
        }

        Function<ProjectModel, DivaAppModel> toApp = p -> {
            DivaAppModel app;
            if (p instanceof DivaAppModel) {
                app = (DivaAppModel) p;
            } else {
                app = GraphService.addTypeToModel(gc, p, DivaAppModel.class);
                if (appProps.containsKey(p.getName())) {
                    String datasource = (String) appProps.get(p.getName()).getOrDefault("quarkus.datasource.jdbc.url",
                            null);
                    if (datasource != null) {
                        app.setDatasource(datasource);
                    }
                }
            }
            return app;
        };

        // 2) checking docker-compose.yml for hostname resolution for each project

        for (FileModel dockerComposeYaml : gc.getQuery(FileModel.class)
                .traverse(g -> g.has(FileModel.FILE_NAME, "docker-compose.yml")).toList(FileModel.class)) {
            try {
                Object o = Util.YAML_SERIALIZER.readValue(new File(dockerComposeYaml.getFilePath()), Object.class);
                for (Map.Entry<String, Map<String, Map<String, String>>> e : ((Map<String, Map<String, Map<String, Map<String, String>>>>) o)
                        .get("services").entrySet()) {
                    String targetPath = null;
                    if (e.getValue().getOrDefault("build", Collections.EMPTY_MAP).containsKey("dockerfile")) {
                        targetPath = Paths.get(dockerComposeYaml.getParentFile().getFilePath())
                                .resolve(e.getValue().get("build").get("dockerfile")).toFile().getCanonicalPath();

                    } else if (e.getValue().getOrDefault("build", Collections.EMPTY_MAP).containsKey("context")) {
                        targetPath = Paths.get(dockerComposeYaml.getParentFile().getFilePath())
                                .resolve(e.getValue().get("build").get("context")).toFile().getCanonicalPath();
                    }
                    if (targetPath != null) {
                        String thePath = targetPath;
                        List<? extends ProjectModel> ps = gc.getQuery(FileModel.class)
                                .traverse(
                                        g -> g.has(FileModel.FILE_PATH, thePath).in(ProjectModel.PROJECT_MODEL_TO_FILE))
                                .toList(ProjectModel.class);
                        for (ProjectModel p : ps) {
                            DivaAppModel app = toApp.apply(p);
                            app.setEndpointName(e.getKey());
                        }
                    }
                }
            } catch (IOException e1) {
            }
        }

        // 3) Attaching list of contexts to each app-model

        for (DivaContextModel cxt : gc.findAll(DivaContextModel.class)) {
            ProjectModel p = cxt.traverse(g -> g.out(DivaContextModel.CONSTRAINTS).in(JavaClassModel.JAVA_METHOD)
                    .out(JavaClassModel.CLASS_FILE, JavaClassModel.ORIGINAL_SOURCE)
                    .in(ProjectModel.PROJECT_MODEL_TO_FILE)).next(ProjectModel.class);
            if (p != null) {
                DivaAppModel app = toApp.apply(p);
                app.addContext(cxt);
            }
        }

        // 4) Mapping each rest-call operation to its endpoint

        for (DivaRestCallOpModel call : gc.findAll(DivaRestCallOpModel.class)) {
            if (call.getMethod() == null)
                continue;
            JavaMethodModel meth = call.getMethod();
            JavaClassModel cls = meth.getJavaClass();
            List<? extends ProjectModel> ps = cls
                    .traverse(g -> g.out(JavaClassModel.ORIGINAL_SOURCE).in(ProjectModel.PROJECT_MODEL_TO_FILE))
                    .toList(ProjectModel.class);
            if (ps.isEmpty())
                continue;
            Properties props = appProps.getOrDefault(ps.get(0).getName(), null);
            if (props == null)
                continue;
            // org.apache.geronimo.daytrader.javaee6.accounts.service.PortfoliosRemoteCallService/mp-rest/url=http://daytrader-portfolios:8080/
            URL url;
            try {
                url = new URL((String) props.getOrDefault(cls.getQualifiedName() + "/mp-rest/url", null));
            } catch (RuntimeException | MalformedURLException e) {
                continue;
            }
            DivaAppModel app = gc.getQuery(DivaAppModel.class)
                    .traverse(g -> g.has(DivaEndpointModel.ENDPOINT_NAME, url.getHost())).next(DivaAppModel.class);
            if (app != null) {
                call.setEndpoint(app);
                List<? extends DivaEntryMethodModel> ms = app
                        .traverse(g -> g.out(DivaAppModel.CONTEXTS).out(DivaContextModel.CONSTRAINTS)
                                .has(DivaRestApiModel.URL_PATH, call.getUrlPath())
                                .has(DivaRestApiModel.HTTP_MEHOD, call.getHttpMethod()))
                        .toList(DivaEntryMethodModel.class);
                for (DivaEntryMethodModel m : ms) {
                    call.setEndpointMethod(m);
                    for (DivaContextModel cxt : m.getContexts()) {
                        if (Util.all(cxt.getConstraints(), r -> {
                            if (!(r instanceof DivaRequestConstraintModel))
                                return true;
                            DivaRequestConstraintModel p = (DivaRequestConstraintModel) r;
                            return Util.all(call.getCallParams(), q -> !q.getParamName().equals(p.getParamName())
                                    || q.getParamValue().equals(p.getParamValue()));
                        })) {
                            call.addEndpointContext(cxt);
                        }
                    }
                    break;
                }

            }
        }

    }

}
