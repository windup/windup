package org.jboss.windup.rules.apps.diva.analysis;

import java.util.function.Consumer;
import java.util.function.Function;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.diva.model.DivaConstraintModel;
import org.jboss.windup.rules.apps.diva.model.DivaContextModel;
import org.jboss.windup.rules.apps.diva.model.DivaOpModel;
import org.jboss.windup.rules.apps.diva.model.DivaRequestParamModel;
import org.jboss.windup.rules.apps.diva.model.DivaRestCallOpModel;
import org.jboss.windup.rules.apps.diva.model.DivaSqlOpModel;
import org.jboss.windup.rules.apps.diva.model.DivaStackTraceModel;
import org.jboss.windup.rules.apps.diva.model.DivaTxModel;
import org.jboss.windup.rules.apps.diva.service.DivaStackTraceService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.java.service.JavaMethodService;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.IMethod.SourcePosition;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.strings.StringStuff;

import io.tackle.diva.Report;
import io.tackle.diva.Trace;

public class DivaToWindup<T extends WindupVertexFrame> implements Report {

    static final String CONSTRAINTS = "constraints";

    GraphService<T> service = null;
    GraphContext gc;
    Consumer<T> addEdge = null;

    public DivaToWindup(GraphContext context, Class<T> model) {
        this(context, model, null);
    }

    public DivaToWindup(GraphContext context, Class<T> model, Consumer<T> addEdge) {
        this.gc = context;
        this.service = new GraphService<T>(context, model);
        this.addEdge = addEdge;
    }

    @Override
    public void add(Named.Builder builder) {
        T model = service.create();
        if (addEdge != null) {
            addEdge.accept(model);
        }
        builder.build(new Named<>(gc, model));
    }

    public void add(T model) {
        if (addEdge != null) {
            addEdge.accept(model);
        }
    }

    @Override
    public void add(Builder builder) {

    }

    @Override
    public void add(String data) {

    }

    public static class Named<T extends WindupVertexFrame> implements Report.Named {

        GraphContext gc;
        T model;

        public Named(GraphContext context, T model) {
            this.gc = context;
            this.model = model;
        }

        @Override
        public void putPrimitive(String key, Object value) {
            if (model instanceof DivaTxModel && key.equals(TXID)) {
                ((DivaTxModel) model).setTxid((int) value);

            } else if (model instanceof DivaOpModel && key.equals(SQL)) {
                DivaSqlOpModel m = GraphService.addTypeToModel(gc, model, DivaSqlOpModel.class);
                this.model = (T) m;
                m.setSql((String) value);

            } else if (model instanceof DivaRestCallOpModel) {
                if (key.equals(HTTP_METHOD)) {
                    ((DivaRestCallOpModel) model).setHttpMethod((String) value);

                } else if (key.equals(URL_PATH)) {
                    ((DivaRestCallOpModel) model).setUrlPath(DivaLauncher.stripBraces((String) value));

                } else if (!key.equals(CLIENT_CLASS)) {
                    return;

                } else {
                    DivaRequestParamModel param = new GraphService<DivaRequestParamModel>(gc,
                            DivaRequestParamModel.class).create();
                    param.setParamName(key);
                    param.setParamValue(value.toString());
                    ((DivaRestCallOpModel) model).addCallParam(param);
                }
            }
        }

        @Override
        public void put(String key, Builder builder) {
            if (model instanceof DivaOpModel && key.equals(REST_CALL)) {
                DivaRestCallOpModel m = GraphService.addTypeToModel(gc, model, DivaRestCallOpModel.class);
                this.model = (T) m;
                builder.build(new Named<DivaRestCallOpModel>(gc, m));
            }
        }

        @Override
        public void put(String key, io.tackle.diva.Report.Builder builder) {
            if (model instanceof DivaContextModel && key.equals(CONSTRAINTS)) {
                builder.build(
                        new DivaToWindup<>(gc, DivaConstraintModel.class, ((DivaContextModel) model)::addConstraint));

            } else if (model instanceof DivaContextModel && key.equals(TRANSACTIONS)) {
                builder.build(new DivaToWindup<>(gc, DivaTxModel.class, ((DivaContextModel) model)::addTransaction));

            } else if (model instanceof DivaTxModel && key.equals(TRANSACTION)) {
                int[] counter = new int[] { 0 };
                builder.build(new DivaToWindup<>(gc, DivaOpModel.class, op -> {
                    op.setOrdinal(counter[0]++);
                    ((DivaTxModel) model).addOp(op);
                }));
            }
        }

        @Override
        public <S> void put(String key, S data, Function<S, Report.Builder> fun) {
            if (model instanceof DivaOpModel && key.equals(STACKTRACE)) {
                DivaStackTraceService service = new DivaStackTraceService(gc);
                JavaClassService classService = new JavaClassService(gc);
                JavaMethodService methodService = new JavaMethodService(gc);
                DivaStackTraceModel parent = null;
                DivaStackTraceModel current = null;
                for (Trace t : ((Trace) data).reversed()) {
                    IMethod m = t.node().getMethod();
                    SourcePosition p = null;
                    try {
                        p = m.getSourcePosition(t.site().getProgramCounter());
                    } catch (InvalidClassFileException | NullPointerException e) {
                        // No source position, e.g., JPA's commit at method exit
                    }
                    if (p != null) {
                        JavaClassModel classModel = classService
                                .create(StringStuff.jvmToBinaryName(m.getDeclaringClass().getName().toString()));
                        JavaMethodModel methodModel = methodService.createJavaMethod(classModel,
                                m.getName().toString());

                        FileModel sourceFile = classModel.getOriginalSource();
                        if (sourceFile == null) {
                            sourceFile = classModel.getDecompiledSource();
                        }
                        current = service.getOrCreate(sourceFile, p.getFirstLine(), p.getFirstCol(),
                                p.getLastOffset() - p.getFirstOffset(), parent, methodModel);
                        parent = current;
                    }
                }
                ((DivaOpModel) model).setStackTrace(current);

                if (((Trace) data).site() != null) {
                    MethodReference mref = ((Trace) data).site().getDeclaredTarget();
                    JavaClassModel classModel = classService
                            .create(StringStuff.jvmToBinaryName(mref.getDeclaringClass().getName().toString()));
                    JavaMethodModel methodModel = methodService.createJavaMethod(classModel, mref.getName().toString());
                    ((DivaOpModel) model).setMethod(methodModel);

                }
            } else {
                put(key, fun.apply(data));
            }
        }
    }

}
