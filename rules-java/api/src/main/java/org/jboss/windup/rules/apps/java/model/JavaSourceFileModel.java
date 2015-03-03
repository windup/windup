package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents a source ".java" file on disk.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(JavaSourceFileModel.TYPE)
public interface JavaSourceFileModel extends FileModel, SourceFileModel
{
    public static final String UNPARSEABLE_JAVA_CLASSIFICATION = "Unparseable Java File";
    public static final String UNPARSEABLE_JAVA_DESCRIPTION = "This Java file could not be parsed";

    public static final String TYPE = "JavaSourceFileModel";
    public static final String PACKAGE_NAME = "packageName";
    public static final String JAVA_CLASS_MODEL = "javaClass";
    public static final String ROOT_SOURCE_FOLDER = "rootSourceFolder";

    /**
     * This is the "root" directory for this source file.
     * 
     * For example, if you have a file at "/project/src/main/java/org/example/Foo.java" then this would point the directory "/project/src/main/java".
     * 
     */
    @Adjacency(label = ROOT_SOURCE_FOLDER, direction = Direction.OUT)
    FileModel getRootSourceFolder();

    /**
     * This is the "root" directory for this source file.
     * 
     * For example, if you have a file at "/project/src/main/java/org/example/Foo.java" then this would point the directory "/project/src/main/java".
     * 
     */
    @Adjacency(label = ROOT_SOURCE_FOLDER, direction = Direction.OUT)
    void setRootSourceFolder(FileModel fileModel);

    /**
     * Contains the Java package name
     */
    @Property(PACKAGE_NAME)
    String getPackageName();

    /**
     * Contains the Java package name
     */
    @Property(PACKAGE_NAME)
    void setPackageName(String packageName);

    /**
     * Contains the primary {@link JavaClassModel} in the class
     */
    void setMainJavaClass(JavaClassModel javaClassModel);

    /**
     * Contains the primary {@link JavaClassModel} in the class
     */
    JavaClassModel getMainJavaClass();

    /**
     * Lists the {@link JavaClassModel}s contained within this source file
     */
    @Adjacency(label = JAVA_CLASS_MODEL, direction = Direction.OUT)
    Iterable<JavaClassModel> getJavaClasses();

    /**
     * Lists the {@link JavaClassModel}s contained within this source file
     */
    @Adjacency(label = JAVA_CLASS_MODEL, direction = Direction.OUT)
    void addJavaClass(JavaClassModel javaClassModel);
}