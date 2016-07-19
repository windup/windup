package org.jboss.windup.graph.tsgen;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Creates the TypeScript models which could accomodate the Frames models instances.
 * Also creates a mapping between discriminators (@TypeValue's) and the TS model classes.
 * In TypeScript it's not reliably possible to scan for all models.
 * 
 * TODO: This code will be moved to some RuleProvider and outside Graph Impl.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class TypeScriptModelsGenerator
{
    public static final Logger LOG = Logger.getLogger( TypeScriptModelsGenerator.class.getName() );
    private static final String MAPPING_DATA_CLASS_NAME = "DiscriminatorMappingData";
    
    // This needs to be parametrized, probably in Maven build.
    /// tests/unmarshaller/typescriptModels/...
    //private static final String PATH_TO_FRAMEMODEL = "../../../app/services/graph/FrameModel";
    private static final String PATH_TO_GRAPH_PKG = "../services/graph";
    
    private final Path modelFilesDir;

    public TypeScriptModelsGenerator(Path destDir)
    {
        this.modelFilesDir = destDir;
    }

    public static enum AdjacentMode { PROXIED, MATERIALIZED, MIXED; }



    /**
     * Generates the TypeScript files for models, copying the structure of WindupVertexModel's.
     * The "entry point".
     */
    public void generate(Set<Class<? extends WindupFrame<?>>> modelTypes, AdjacentMode mode)
    {
        try {
            Files.createDirectories(this.modelFilesDir);
            LOG.info("Creating TypeScript models in " + this.modelFilesDir.toAbsolutePath());
        }
        catch (IOException ex) {
            LOG.severe("Could not create directory for TS models: " + ex.getMessage() + "\n\t" + this.modelFilesDir);
        }

        Map<String, ModelDescriptor> classesMapping = new TreeMap<>(); // We want deterministic order.

        for (Class<? extends WindupFrame<?>> frameClass : modelTypes)
        {
            if (!(WindupVertexFrame.class.isAssignableFrom(frameClass)))
                continue;

            final Class<? extends WindupVertexFrame> frameClass2 = (Class<? extends WindupVertexFrame>) frameClass;
            ModelDescriptor modelDescriptor = createModelDescriptor(frameClass2);
            classesMapping.put(modelDescriptor.discriminator, modelDescriptor);
        }
        
        addClassesThatAreSkippedForSomeReason(classesMapping);
        
        for (ModelDescriptor modelDescriptor : classesMapping.values())
        {
            writeTypeScriptModelClass(modelDescriptor, mode);
        }
        
        writeTypeScriptClassesMapping(classesMapping);
    }
    
    private void addClassesThatAreSkippedForSomeReason(Map<String, ModelDescriptor> classesMapping)
    {
        // This is a hack in  AmbiguousReferenceMode and WindupVertexListModel
        if (!classesMapping.containsKey("WindupVertexFrame"))
        {
            ModelDescriptor md = new ModelDescriptor();
            md.discriminator = "ADummyDescriptorWVF";
            md.modelClassName = "WindupVertexFrame";
            classesMapping.put(md.discriminator, md);
        }
        
        // graphTypeManager.getRegisteredTypes()  skips the following for some reason.
        if (!classesMapping.containsKey("ResourceModel"))
        {
            ModelDescriptor md = new ModelDescriptor();
            md.discriminator = "ADummyDescriptorResource";
            md.modelClassName = "ResourceModel";
            classesMapping.put(md.discriminator, md);
        }
        
        if (!classesMapping.containsKey("DependencyReportDependencyGroupModel"))
        {
            ModelDescriptor md = new ModelDescriptor();
            md.discriminator = "ADummyDescriptorDRDGM";
            md.modelClassName = "DependencyReportDependencyGroupModel";
            classesMapping.put(md.discriminator, md);
        }
        
        /* TODO: Use this when debugged.
        List<String> artificiallyAddedModels = new ArrayList<>();
        // This is a hack in  AmbiguousReferenceMode and WindupVertexListModel
        artificiallyAddedModels.add("WindupVertexFrame");
        // graphTypeManager.getRegisteredTypes()  skips the following for some reason.
        artificiallyAddedModels.add("ResourceModel");
        artificiallyAddedModels.add("DependencyReportDependencyGroupModel");
        for (String className : artificiallyAddedModels) {
            if (!classesMapping.containsKey("ResourceModel"))
            {
                ModelDescriptor md = new ModelDescriptor();
                md.discriminator = "ADummyDiscr_" + className;
                md.modelClassName = className;
                classesMapping.put(md.discriminator, md);
            }
        }/**/
    }


    /**
     * Extracts the information from the given type, based on @Property and @Adjacent annotations.
     */
    private ModelDescriptor createModelDescriptor(Class<? extends WindupVertexFrame> frameClass)
    {
        ModelDescriptor modelDescriptor = new ModelDescriptor();

        // Get the type discriminator string
        TypeValue typeValueAnn = frameClass.getAnnotation(TypeValue.class);
        modelDescriptor.discriminator = typeValueAnn.value();

        modelDescriptor.modelClassName = frameClass.getSimpleName();
        
        if (frameClass.getInterfaces().length != 1)
            LOG.warning("Model extends more than 1 model. Current TS unmarshaller doesn't support that (yet).");
        ///if (frameClass.getInterfaces()[0] != WindupVertexFrame.class)
        modelDescriptor.extendedModels = Arrays.asList(frameClass.getInterfaces()).stream()
            .filter((x) -> WindupVertexFrame.class.isAssignableFrom(x) && ! WindupVertexFrame.class.equals(x))
            .map(Class::getSimpleName).collect(Collectors.toList());

        // These could be part of the ModelDescriptor.
        BidiMap<String, String> methodNameVsPropName = new DualHashBidiMap<>();
        BidiMap<String, String> methodNameVsEdgeLabel = new DualHashBidiMap<>();

        for (Method method : frameClass.getDeclaredMethods())
        {
            // Get the properties - @Property
            prop: {

                Property propAnn = method.getAnnotation(Property.class);
                if (propAnn == null)
                    break prop;

                final Class propertyType = TsGenUtils.getPropertyTypeFromMethod(method);
                if (propertyType == null)
                    break prop;


                final ModelRelation methodInfo = infoFromMethod(method);
                final String graphPropName = propAnn.value();
                final ModelProperty existing = modelDescriptor.properties.get(graphPropName);

                if (!checkMethodNameVsPropNameConsistency(methodNameVsPropName, methodInfo.beanPropertyName, graphPropName, method,
                        "Property name '%s' of method '%s' doesn't fit previously seen property name '%s' of other method for '%s'."
                        + "\nCheck the Frames model %s"))
                    // Names don't fit, warning printed.
                    continue;

                // Method base beanPropertyName already seen.
                if (existing != null)
                    continue;


                // This method beanPropertyName was not seen yet.

                final ModelProperty prop = new ModelProperty(methodInfo.beanPropertyName, graphPropName, PrimitiveType.from(propertyType));
                modelDescriptor.properties.put(prop.graphPropertyName, prop);
            }

            // Get the relations - @Adjacent
            adj: {

                Adjacency adjAnn = method.getAnnotation(Adjacency.class);
                if (adjAnn == null)
                    break adj;

                // Model class of the other end.
                final Class theOtherType = TsGenUtils.getPropertyTypeFromMethod(method);
                if (theOtherType == null)
                    break adj;

                final ModelRelation methodInfo = infoFromMethod(method);
                //final boolean alreadySeen = methodNameVsEdgeLabel.containsKey(methodInfo.beanPropertyName);
                final ModelRelation existing = modelDescriptor.relations.get(adjAnn.label());

                if (!checkMethodNameVsPropNameConsistency(methodNameVsEdgeLabel, methodInfo.beanPropertyName, adjAnn.label(), method,
                        "Edge label '%s' of method '%s' doesn't fit previously seen edge label '%s' of other method for '%s'."
                        + "\nCheck the Frames model %s"))
                    continue;

                // Method base beanPropertyName already seen. Override some traits.
                if (existing != null) {
                    existing.isIterable |= methodInfo.isIterable;
                    existing.methodsPresent.addAll(methodInfo.getMethodsPresent());
                    // We want the plural, which is assumably with methods working with Iterable.
                    if (methodInfo.isIterable)
                        existing.beanPropertyName = methodInfo.beanPropertyName;
                    continue;
                }

                ModelType adjType = ModelType.from(theOtherType);
                final ModelRelation modelRelation = new ModelRelation(
                        methodInfo.beanPropertyName,
                        adjAnn.label(),
                        adjAnn.direction().OUT.equals(adjAnn.direction()),
                        adjType,
                        methodInfo.isIterable /// Also for add/remove? (these don't take Iterable)
                );
                modelDescriptor.relations.put(modelRelation.edgeLabel, modelRelation);
            }
        }
        return modelDescriptor;
    }


    /**
     * @param methodNameVsPropName
     * @param methodPropName
     * @param graphPropName
     * @param method
     * @param messageFormat
     *
     * @return false if there was pre-existing mapping of bean property beanPropertyName (getter/setter "base beanPropertyName")
         to a graph property beanPropertyName or an edge label, and the examined method beanPropertyName doesn't fit that.
         true otherwise (No pre-existing mapping or the names fit.)
     */
    private boolean checkMethodNameVsPropNameConsistency(
            Map<String, String> methodNameVsPropName, final String methodPropName,
            final String graphPropName, Method method, String messageFormat)
    {
        final String existingPropName = methodNameVsPropName.get(methodPropName);
        if (existingPropName != null)
        {
            if (!graphPropName.equals(existingPropName))
            {
                LOG.warning(String.format(messageFormat,
                    graphPropName, method.toString(), existingPropName, methodPropName, method.getDeclaringClass().getName()));
                return false;
            }
        }

        methodNameVsPropName.put(methodPropName, graphPropName);
        return true;
    }



    /**
     * Derives the property beanPropertyName from the given method, assuming it's a getter, setter, adder or remover.
     * The returned ModelRelation has the beanPropertyName, the methodsPresent and the isIterable set.
     */
    private static ModelRelation infoFromMethod(Method method)
    {
        // Relying on conventions here. Might need some additional data in the Frames models.
        String name = method.getName();
        ModelRelation info = new ModelRelation();
        name = TsGenUtils.removePrefixAndSetMethodPresence(name, "getAll", info.methodsPresent, ModelRelation.BeanMethodType.GET);
        name = TsGenUtils.removePrefixAndSetMethodPresence(name, "get",    info.methodsPresent, ModelRelation.BeanMethodType.GET);
        name = TsGenUtils.removePrefixAndSetMethodPresence(name, "is",     info.methodsPresent, ModelRelation.BeanMethodType.GET);
        name = TsGenUtils.removePrefixAndSetMethodPresence(name, "set",    info.methodsPresent, ModelRelation.BeanMethodType.SET);
        name = TsGenUtils.removePrefixAndSetMethodPresence(name, "add",    info.methodsPresent, ModelRelation.BeanMethodType.ADD);
        name = TsGenUtils.removePrefixAndSetMethodPresence(name, "remove", info.methodsPresent, ModelRelation.BeanMethodType.REMOVE);
        name = StringUtils.uncapitalize(name);
        //name = StringUtils.removeEnd(modelClassName, "s"); // Better to have addItems(item: Item) than getItem(): Item[]
        info.beanPropertyName = name;

        if (Iterable.class.isAssignableFrom(method.getReturnType()))
            info.isIterable = true;
        else if (method.getParameterCount() > 0 && Iterable.class.isAssignableFrom(method.getParameterTypes()[0]))
            info.isIterable = true;

        return info;
    }


    /**
     * Writes a TypeScript class 'DiscriminatorMapping.ts' with the mapping
     * from the discriminator value (@TypeValue) to TypeScript model class.
     */
    private void writeTypeScriptClassesMapping(Map<String, ModelDescriptor> discriminatorToClassMapping)
    {
        final File mappingFile = this.modelFilesDir.resolve(MAPPING_DATA_CLASS_NAME + ".ts").toFile();
        try (FileWriter mappingWriter = new FileWriter(mappingFile))
        {
            mappingWriter.write("import {FrameModel} from '" + PATH_TO_GRAPH_PKG + "/FrameModel';\n");
            mappingWriter.write("import {DiscriminatorMapping} from '" + PATH_TO_GRAPH_PKG + "/DiscriminatorMapping';\n\n");
            
            for (Map.Entry<String, ModelDescriptor> entry : discriminatorToClassMapping.entrySet())
            {
                mappingWriter.write(String.format("import {%1$s} from './%1$s';\n", entry.getValue().modelClassName));
            }

            mappingWriter.write("\n" +
                "export class " + MAPPING_DATA_CLASS_NAME + " extends DiscriminatorMapping\n{\n" +
                "    //@Override\n" +
                "    public static getMapping(): { [key: string]: typeof FrameModel } {\n" +
                "        return this.mapping;\n" +
                "    }\n\n" +
                "    static mapping: { [key: string]: typeof FrameModel } = {\n");
            for (Map.Entry<String, ModelDescriptor> entry : discriminatorToClassMapping.entrySet())
            {
                mappingWriter.write("        \"" + entry.getKey() + "\": " + entry.getValue().modelClassName + ",\n");
            }
            mappingWriter.write("    };\n\n");
            mappingWriter.write("    constructor() { super(); };\n");
            mappingWriter.write("};\n");
        }
        catch (IOException ex)
        {
            Logger.getLogger(TypeScriptModelsGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Writes a TypeScript class file for the model described by given model descriptor.
     * @param modelDescriptor
     * @param mode  The mode in which the models will operate.
     *              This can be a passive way (properties and arrays), active way - proxies, or mixed, or both.
     *              Currently implemented is passive.
     */
    private void writeTypeScriptModelClass(ModelDescriptor modelDescriptor, AdjacentMode mode)
    {
        final File tsFile = this.modelFilesDir.resolve(modelDescriptor.modelClassName + ".ts").toFile();
        try (FileWriter tsWriter = new FileWriter(tsFile))
        {
            tsWriter.write("import {FrameModel} from '" + PATH_TO_GRAPH_PKG + "/FrameModel';\n\n");
            
            Set<String> imported = new HashSet<>();
            imported.add("FrameModel");
            
            // Import property and relation types.
            for (ModelRelation relation : modelDescriptor.relations.values())
            {
                final String typeScriptTypeName = relation.type.getTypeScriptTypeName();
                // Don't import this class
                if (typeScriptTypeName.equals(modelDescriptor.modelClassName))
                    continue;
                if (imported.add(typeScriptTypeName))
                    tsWriter.write(String.format("import {%1$s} from './%1$s';\n", typeScriptTypeName));
            }

            List<String> extendedModels = modelDescriptor.extendedModels;
            if (extendedModels == null || extendedModels.size() == 0)
                extendedModels = Collections.singletonList(AdjacentMode.PROXIED.equals(mode) ? "FrameProxy" : "FrameModel");

            // Import extended types.
            /*for (String modelClassName : extendedModels)
                if (imported.add(modelClassName))
                    tsWriter.write(String.format("import {%1$s} from './%1$s';\n", modelClassName)); *///
            tsWriter.write(
                extendedModels.stream().filter(imported::add)
                    .map((x)->{return String.format("import {%1$s} from './%1$s';\n", x);})
                    .collect(Collectors.joining())
            );

            tsWriter.write("\nexport class " + modelDescriptor.modelClassName + " extends " + String.join(" //", extendedModels) + "\n{\n");
            //tsWriter.write("    private vertexId: number;\n\n");
            tsWriter.write("    static discriminator: string = '" + modelDescriptor.discriminator + "';\n\n");

            // Data for mapping from the graph JSON object to Frame-based models.

            tsWriter.write("    static graphPropertyMapping: { [key:string]:string; } = {\n");
            for (ModelProperty property : modelDescriptor.properties.values())
            {
                ///tsWriter.write(String.format("        '%s': '%s',\n",
                ///    StringEscapeUtils.escapeEcmaScript(property.graphPropertyName), property.beanPropertyName));
                tsWriter.write(String.format("        %s: '%s',\n", escapeJSandQuote(property.graphPropertyName), property.beanPropertyName));
            }
            tsWriter.write("    };\n\n");
            tsWriter.write("    static graphRelationMapping: { [key:string]:string; } = {\n");
            for (ModelRelation relation : modelDescriptor.relations.values())
            {
                try {
                    // edgeLabel: 'propName[TypeValue'
                    // The TypeValue (discriminator) is useful on the client side because the generated JavaScript
                    // has no clue what type is coming or what types should it send back to the server.
                    // While it's coming in the "w:winduptype" value, this may contain several values,
                    // and the models unmarshaller needs to know which one to unmarshall to.
                    tsWriter.write("        " + escapeJSandQuote(relation.edgeLabel) + ": '" + relation.beanPropertyName
                        + (relation.isIterable ? "[" : "|")
                        + (relation.type instanceof FrameType ? ((FrameType)relation.type).getFrameDiscriminator() : "") + "',\n");
                    
                }
                catch (Exception ex){
                    String msg = "Error writing relation " + relation.beanPropertyName + ": " + ex.getMessage();
                    tsWriter.write("        // " + msg + "\n");
                    LOG.severe(msg);
                }
            }
            tsWriter.write("    };\n\n");


            // Actual properties and methods.
            for (ModelProperty property : modelDescriptor.properties.values())
            {
                tsWriter.write("    ");
                tsWriter.write(property.toTypeScript());
                tsWriter.write(";\n");
            }

            tsWriter.write("\n");

            for (ModelRelation relation : modelDescriptor.relations.values())
            {
                tsWriter.write(relation.toTypeScript(mode));
                tsWriter.write("\n");
            }

            tsWriter.write("}\n");
        }
        catch (IOException ex)
        {
            LOG.log(Level.SEVERE, "Failed creating TypeScript model for " + modelDescriptor.toString() + ":\n\t" + ex.getMessage(), ex);
        }
    }
    
    private String escapeJSandQuote(String str)
    {
        return String.format("'%s'", StringEscapeUtils.escapeEcmaScript(str));
    }
}

/**
 * Info about the type class from which the TypeScript model can be created.
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
class ModelDescriptor
{
    /** Model class name. */
    String modelClassName;
    /** Currently we only support 1 "parent" Model. For Models with more, a warning is printed. */
    List<String> extendedModels;
    /** Frames model class discriminator - determines the type. */
    String discriminator;
    Map<String, ModelProperty> properties = new HashMap<>();
    Map<String, ModelRelation> relations = new HashMap<>();

    @Override
    public String toString() {
        return "ModelDescriptor{modelClass=" + modelClassName + ", disc=" + discriminator + '}';
    }
}


/**
 * Handles String, boolean, byte, char, short, int, long, float, and double.
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
enum PrimitiveType implements ModelType
{
    STRING("string"), NUMBER("number"), BOOLEAN("boolean"), ENUM("string"), ANY("any");

    static PrimitiveType from(Class type)
    {
        if (Iterable.class.isAssignableFrom(type))
            throw new IllegalArgumentException("Given type is Iterable (not a primitive type): " + type.getName());
        if (String.class.isAssignableFrom(type))
            return STRING;
        if (Number.class.isAssignableFrom(type) || type.equals(Integer.TYPE) || type.equals(Long.TYPE) || type.equals(Double.TYPE) || type.equals(Short.TYPE) || type.equals(Float.TYPE) || type.equals(Byte.TYPE) || type.equals(Character.TYPE) || type.equals(Byte.TYPE))
            return NUMBER;
        if (Boolean.class.isAssignableFrom(type) || type.equals(Boolean.TYPE))
            return BOOLEAN;
        if (Enum.class.isAssignableFrom(type))
            return ENUM;
        //TypeScriptModelsGenerator.LOG.warning("Not a primitive type: " + type.getTypeName());
        return ANY;
    }

    private String typeScriptTypeName;

    PrimitiveType(String tsType){
        this.typeScriptTypeName = tsType;
    }

    @Override
    public String getTypeScriptTypeName()
    {
        return typeScriptTypeName;
    }
}

/**
 * Handles WindupVertexFrame's.
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
class FrameType implements ModelType
{
    private Class<? extends WindupVertexFrame> frameType;


    public FrameType(Class<? extends WindupVertexFrame> frameType)
    {
        this.frameType = frameType;
    }

    static FrameType from(Class cls){
        if (WindupVertexFrame.class.isAssignableFrom(cls))
            return new FrameType(cls);
        return null;
    }


    public String getFrameDiscriminator()
    {
        if(frameType.getAnnotation(TypeValue.class) == null)
            throw new IllegalStateException("Missing @"+TypeValue.class.getSimpleName()+": " + frameType.getName());
        return frameType.getAnnotation(TypeValue.class).value();
    }

    @Override
    public String getTypeScriptTypeName()
    {
        return frameType.getSimpleName();
    }
}

interface ModelType {
    String getTypeScriptTypeName();

    static ModelType from(Class cls) {
        return ObjectUtils.defaultIfNull(FrameType.from(cls), PrimitiveType.from(cls));
    }
}

/**
 * Common for properties and relations.
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
abstract class ModelMember
{
    String beanPropertyName;
    ModelType type;
    boolean isIterable;
    EnumSet<BeanMethodType> methodsPresent = EnumSet.noneOf(BeanMethodType.class);

    public static enum BeanMethodType { GET, SET, ADD, REMOVE; }
}


/**
 * A property - annotated with @Property in the original model.
 * Can be a {@link java.io.Serializable} too, but in Windup models, it's only primitive types IIRC.
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
class ModelProperty extends ModelMember
{
    String graphPropertyName;
    boolean hasSetter;
    boolean hasGetter;

    public ModelProperty(String beanPropertyName, String graphPropertyName, org.jboss.windup.graph.tsgen.PrimitiveType type)
    {
        this.beanPropertyName = beanPropertyName;
        this.graphPropertyName = graphPropertyName;
        this.type = type;
    }

    String toTypeScript()
    {
        return this.beanPropertyName + ": " + this.type.getTypeScriptTypeName();
    }
}


/**
 * A relation between WinudpVertexFrame's.
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
class ModelRelation extends ModelMember
{
    String edgeLabel;
    String query;
    boolean directionOut;

    public ModelRelation(String name, String edgeLabel, boolean directionOut, ModelType type, boolean iterable)
    {
        this.beanPropertyName = name;
        this.edgeLabel = edgeLabel;
        this.directionOut = directionOut;
        this.type = type;
        this.isIterable = iterable;
    }


    ModelRelation()
    {
    }

    /**
     * Query that is passed to the REST API to limit the set of returned objects.
     */
    public ModelRelation setQuery(String query)
    {
        this.query = query;
        return this;
    }

    public EnumSet<BeanMethodType> getMethodsPresent()
    {
        return methodsPresent;
    }


    public ModelType getType()
    {
        return type;
    }




    String toTypeScript(TypeScriptModelsGenerator.AdjacentMode mode)
    {
        switch(mode){
            case PROXIED: return toTypeScriptProxy();
            case MATERIALIZED: return toTypeScriptMaterialized();
            default: throw new UnsupportedOperationException();
        }
    }

    String toTypeScriptMaterialized()
    {
        String brackets = this.isIterable ? "[]" : "";
        return "    public " + this.beanPropertyName + ": " + this.type.getTypeScriptTypeName() + brackets + "; // edge label '"+ this.edgeLabel +"'\n";
    }


    String toTypeScriptProxy()
    {
        String brackets = this.isIterable ? "[]" : "";

        // A method calling a service to get the adjacent items.
        StringBuilder sb = new StringBuilder();
        sb.append("    function get").append(StringUtils.capitalize(this.beanPropertyName)).append("(): ").append(this.type.getTypeScriptTypeName()).append(brackets).append(" {\n");
        sb.append("        return this.queryAdjacent('").append(this.edgeLabel).append("', ");
        quoteIfNotNull(sb, this.query);
        sb.append(");\n");
        sb.append("    }\n\n");

        // A method calling a service to set the given adjacent item.
        sb.append("    function set").append(StringUtils.capitalize(this.beanPropertyName)).append("(item: ").append(this.type.getTypeScriptTypeName()).append(brackets).append(") {\n");
        sb.append("        return this.set('").append(this.edgeLabel).append("', item);\n");
        sb.append("    }\n\n");

        if (this.isIterable)
        {
            // A method calling a service to add the adjacent items.
            sb.append("    function add").append(StringUtils.capitalize(this.beanPropertyName)).append("(item: ").append(this.type.getTypeScriptTypeName()).append(") {\n");
            sb.append("        return this.add('").append(this.edgeLabel).append("', item);\n");
            sb.append("    }\n\n");

            // A method calling a service to remove the adjacent items.
            sb.append("    function remove").append(StringUtils.capitalize(this.beanPropertyName)).append("(item: ").append(this.type.getTypeScriptTypeName()).append(") {\n");
            sb.append("        return this.remove('").append(this.edgeLabel).append("', item);\n");
            sb.append("    }\n\n");
        }

        return sb.toString();
    }


    private static void quoteIfNotNull(StringBuilder sb, String val)
    {
        if (val == null)
            sb.append("null");
        else
            sb.append("'").append(val).append("'");
    }


    @Override
    public String toString()
    {
        return "Relation{" + "edge: " + edgeLabel + ", name: " + beanPropertyName +
            ", type: " + (type == null ? "null" : type.getTypeScriptTypeName()) + (this.isIterable ? "[]" : "") + '}';
    }

}
