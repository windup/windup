package org.jboss.windup.graph.tsgen;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jboss.windup.graph.MapInProperties;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.tsgen.TypeScriptModelsGenerator.AdjacentMode;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class TypeScriptModelsGeneratorTest
{

    public TypeScriptModelsGeneratorTest()
    {
    }


    @Test
    public void testGenerate()
    {
        System.out.println("TESTING generate()");
        Set<Class<? extends WindupFrame<?>>> modelTypes = new HashSet<>();
        modelTypes.add(TestGeneratorModel.class);
        modelTypes.add(TestPlanetModel.class);
        modelTypes.add(TestShipModel.class);

        final Path modelsDir = Paths.get("target/tsModels");
        TypeScriptModelsGenerator instance = new TypeScriptModelsGenerator(modelsDir);
        instance.generate(modelTypes, AdjacentMode.MATERIALIZED);

        assertTrue(modelsDir.toFile().exists());
    }


    @Test
    public void testGetPropertyTypeFromMethod() throws NoSuchMethodException
    {
        System.out.println("getPropertyTypeFromMethod");
        Method method = TestGeneratorModel.class.getMethod("getName");
        Class result = TsGenUtils.getPropertyTypeFromMethod(method);
        assertEquals(String.class, result);
    }

}

@TypeValue("TestGenerator")
interface TestGeneratorModel extends WindupVertexFrame
{
    // Standard.
    @Property("name")
    String getName();

    @Property("name")
    void setName(String name);

    // Only setter.
    @Property("rank")
    void setRank(String rank);

    // Different name and discriminator value.
    @Property("bar")
    String getBoo();

    @Property("bar")
    TestGeneratorModel setBoo(String boo);

    // Other method base name than previous "bar"s. Should give a warning.
    @Property("bar")
    TestGeneratorModel setBooze(String boo);

    // Setter returns model type (this).
    @Property("bar")
    TestGeneratorModel setBar(String bar);


    // @Adjacency

    // Single
    @Adjacency(label = "commands", direction = Direction.IN)
    TestShipModel getShip();

    @Adjacency(label = "commands", direction = Direction.IN)
    TestGeneratorModel setShip(TestShipModel ship);

    // Iterable
    @Adjacency(label = "colonizes", direction = Direction.OUT)
    Iterable<TestPlanetModel> getColonizedPlanets();

    @Adjacency(label = "colonizes", direction = Direction.OUT)
    TestGeneratorModel setColonizedPlanets(Iterable<TestPlanetModel> planet);

    @Adjacency(label = "colonizes", direction = Direction.OUT)
    TestGeneratorModel addColonizedPlanet(TestPlanetModel planet);

    @Adjacency(label = "colonizes", direction = Direction.OUT)
    TestGeneratorModel removeColonizedPlanet(TestPlanetModel planet);

    // Should somehow be bound to the other "colonizes"
    @Adjacency(label = "colonizes", direction = Direction.OUT)
    TestGeneratorModel someWeirdName(TestPlanetModel planet);

    @MapInProperties
    Map<String, String> getSomeMap();
}


@TypeValue("TestShip")
interface TestShipModel extends WindupVertexFrame
{
    @Property("name")
    String getName();

    @Property("name")
    void setName(String name);
}

@TypeValue("TestPlanet")
interface TestPlanetModel extends WindupVertexFrame
{
    @Property("name")
    String getName();

    @Property("name")
    void setName(String name);
}