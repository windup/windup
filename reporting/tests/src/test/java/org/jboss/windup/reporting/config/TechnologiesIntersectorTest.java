package org.jboss.windup.reporting.config;

import com.google.gson.JsonObject;
import com.syncleus.ferma.ClassInitializer;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.TEdge;
import com.syncleus.ferma.Traversable;
import com.syncleus.ferma.VertexFrame;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.config.metadata.RuleProviderMetadata;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.graph.model.TechnologyReferenceModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TechnologiesIntersectorTest {

    @Mock
    WindupConfigurationModel windupConfigurationModel;

    @Mock
    RuleProviderMetadata ruleProviderMetadata;


    @Test
    public void testExtractTargetTechnologiesWithNoMatchingId() {
        TechnologyReferenceModel t1 = createTechRef("eap", "[8,9)");
        List<TechnologyReferenceModel> configuredTargets = List.of(t1);
        TechnologyReferenceModel t2 = createTechRef("jakarta-ee", null);
        Set<TechnologyReference> ruleTargets = Set.of(new TechnologyReference(t2));

        when(windupConfigurationModel.getTargetTechnologies()).thenReturn(configuredTargets);
        when(ruleProviderMetadata.getTargetTechnologies()).thenReturn(ruleTargets);

        List<TechnologyReferenceModel> result = TechnologiesIntersector.extractTargetTechnologies(windupConfigurationModel, ruleProviderMetadata);

        assertEquals(0, result.size());
    }

    @Test
    public void testExtractTargetTechnologiesWithNoMatchingVersion() {
        TechnologyReferenceModel t1 = createTechRef("eap", "[8,9)");
        List<TechnologyReferenceModel> configuredTargets = List.of(t1);
        TechnologyReferenceModel t2 = createTechRef("eap", "[7]");
        Set<TechnologyReference> ruleTargets = Set.of(new TechnologyReference(t2));

        when(windupConfigurationModel.getTargetTechnologies()).thenReturn(configuredTargets);
        when(ruleProviderMetadata.getTargetTechnologies()).thenReturn(ruleTargets);

        List<TechnologyReferenceModel> result = TechnologiesIntersector.extractTargetTechnologies(windupConfigurationModel, ruleProviderMetadata);

        assertEquals(0, result.size());
    }

    @Test
    public void testExtractTargetTechnologiesWithMatchingIdAndVersion() {
        TechnologyReferenceModel t1 = createTechRef("eap", "[8,9)");
        List<TechnologyReferenceModel> configuredTargets = List.of(t1);
        TechnologyReferenceModel t2 = createTechRef("eap", "[8.1]");
        Set<TechnologyReference> ruleTargets = Set.of(new TechnologyReference(t2));

        when(windupConfigurationModel.getTargetTechnologies()).thenReturn(configuredTargets);
        when(ruleProviderMetadata.getTargetTechnologies()).thenReturn(ruleTargets);

        List<TechnologyReferenceModel> result = TechnologiesIntersector.extractTargetTechnologies(windupConfigurationModel, ruleProviderMetadata);

        assertEquals(1, result.size());
    }

    @Test
    public void testExtractTargetTechnologiesWithMatchingWithMultipleTechs() {
        TechnologyReferenceModel t1 = createTechRef("eap", "[8,9)");
        TechnologyReferenceModel t2 = createTechRef("jakarta-ee", "[9]");
        List<TechnologyReferenceModel> configuredTargets = List.of(t1, t2);
        TechnologyReferenceModel t3 = createTechRef("eap", "[8.3]");
        TechnologyReferenceModel t4 = createTechRef("jakarta-ee", "[8,9)");
        Set<TechnologyReference> ruleTargets = Set.of(new TechnologyReference(t3), new TechnologyReference(t4));

        when(windupConfigurationModel.getTargetTechnologies()).thenReturn(configuredTargets);
        when(ruleProviderMetadata.getTargetTechnologies()).thenReturn(ruleTargets);

        List<TechnologyReferenceModel> result = TechnologiesIntersector.extractTargetTechnologies(windupConfigurationModel, ruleProviderMetadata);

        assertEquals(1, result.size());
        assertEquals("eap", result.get(0).getTechnologyID());
    }

    private TechnologyReferenceModel createTechRef(String id, String version) {
        TechnologyReferenceModel techRef = new TechnologyRefenceModelImpl();
        techRef.setTechnologyID(id);
        techRef.setVersionRange(version);
        return techRef;
    }

    private class TechnologyRefenceModelImpl implements TechnologyReferenceModel {

        private String id;
        private String version;

        @Override
        public String getTechnologyID() {
            return this.id;
        }

        @Override
        public void setTechnologyID(String technologyID) {
            this.id = technologyID;
        }

        @Override
        public String getVersionRange() {
            return version;
        }

        @Override
        public void setVersionRange(String versionRange) {
            this.version = versionRange;
        }

        @Override
        public <N> N getId() {
            return null;
        }

        @Override
        public Set<String> getPropertyKeys() {
            return null;
        }

        @Override
        public void remove() {

        }

        @Override
        public Vertex getElement() {
            return null;
        }

        @Override
        public void setElement(Element element) {

        }

        @Override
        public FramedGraph getGraph() {
            return null;
        }

        @Override
        public <T> T getProperty(String name) {
            return null;
        }

        @Override
        public <T> T getProperty(String name, Class<T> type) {
            return null;
        }

        @Override
        public void setProperty(String name, Object value) {

        }

        @Override
        public Class<?> getTypeResolution() {
            return null;
        }

        @Override
        public void setTypeResolution(Class<?> type) {

        }

        @Override
        public void removeTypeResolution() {

        }

        @Override
        public <T> T addFramedEdge(String label, VertexFrame inVertex, ClassInitializer<T> initializer) {
            return null;
        }

        @Override
        public <T> T addFramedEdge(String label, VertexFrame inVertex, Class<T> kind) {
            return null;
        }

        @Override
        public <T> T addFramedEdgeExplicit(String label, VertexFrame inVertex, ClassInitializer<T> initializer) {
            return null;
        }

        @Override
        public <T> T addFramedEdgeExplicit(String label, VertexFrame inVertex, Class<T> kind) {
            return null;
        }

        @Override
        public TEdge addFramedEdge(String label, VertexFrame inVertex) {
            return null;
        }

        @Override
        public TEdge addFramedEdgeExplicit(String label, VertexFrame inVertex) {
            return null;
        }

        @Override
        public void linkOut(VertexFrame vertex, String... labels) {

        }

        @Override
        public void linkIn(VertexFrame vertex, String... labels) {

        }

        @Override
        public void linkBoth(VertexFrame vertex, String... labels) {

        }

        @Override
        public void unlinkOut(VertexFrame vertex, String... labels) {

        }

        @Override
        public void unlinkIn(VertexFrame vertex, String... labels) {

        }

        @Override
        public void unlinkBoth(VertexFrame vertex, String... labels) {

        }

        @Override
        public void setLinkOut(VertexFrame vertex, String... labels) {

        }

        @Override
        public void setLinkIn(VertexFrame vertex, String... labels) {

        }

        @Override
        public void setLinkBoth(VertexFrame vertex, String... labels) {

        }

        @Override
        public <K> K setLinkOut(ClassInitializer<K> initializer, String... labels) {
            return null;
        }

        @Override
        public <K> K setLinkOut(Class<K> kind, String... labels) {
            return null;
        }

        @Override
        public <K> K setLinkOutExplicit(ClassInitializer<K> initializer, String... labels) {
            return null;
        }

        @Override
        public <K> K setLinkOutExplicit(Class<K> kind, String... labels) {
            return null;
        }

        @Override
        public <K> K setLinkIn(ClassInitializer<K> initializer, String... labels) {
            return null;
        }

        @Override
        public <K> K setLinkIn(Class<K> kind, String... labels) {
            return null;
        }

        @Override
        public <K> K setLinkInExplicit(ClassInitializer<K> initializer, String... labels) {
            return null;
        }

        @Override
        public <K> K setLinkInExplicit(Class<K> kind, String... labels) {
            return null;
        }

        @Override
        public <K> K setLinkBoth(ClassInitializer<K> initializer, String... labels) {
            return null;
        }

        @Override
        public <K> K setLinkBoth(Class<K> kind, String... labels) {
            return null;
        }

        @Override
        public <K> K setLinkBothExplicit(ClassInitializer<K> initializer, String... labels) {
            return null;
        }

        @Override
        public <K> K setLinkBothExplicit(Class<K> kind, String... labels) {
            return null;
        }

        @Override
        public JsonObject toJson() {
            return null;
        }

        @Override
        public <T> T reframe(Class<T> kind) {
            return null;
        }

        @Override
        public <T> T reframeExplicit(Class<T> kind) {
            return null;
        }

        @Override
        public <T extends Traversable<?, ?>> T traverse(Function<GraphTraversal<Vertex, Vertex>, GraphTraversal<?, ?>> traverser) {
            return null;
        }

        @Override
        public GraphTraversal<? extends Vertex, ? extends Vertex> getRawTraversal() {
            return null;
        }

        @Override
        public void init() {

        }
    }

}