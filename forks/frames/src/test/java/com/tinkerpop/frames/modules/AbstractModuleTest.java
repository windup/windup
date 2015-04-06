package com.tinkerpop.frames.modules;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.modules.AbstractModule;

public class AbstractModuleTest {

	@Test
	public void testNoWrapping() {
		Graph baseGraph = Mockito.mock(Graph.class);
		TransactionalGraph baseTransactionalGraph = Mockito.mock(TransactionalGraph.class);
		
		FramedGraphConfiguration config = new FramedGraphConfiguration();
		AbstractModule module = Mockito.mock(AbstractModule.class);
		Mockito.when(module.doConfigure(Mockito.any(Graph.class), Mockito.any(FramedGraphConfiguration.class))).thenCallRealMethod();
		
		
		Graph configuredGraph = module.configure(baseGraph, config);
		Assert.assertEquals(baseGraph, configuredGraph);
		Mockito.verify(module).doConfigure(Mockito.any(Graph.class), Mockito.any(FramedGraphConfiguration.class));
		Mockito.verify(module).doConfigure(Mockito.any(FramedGraphConfiguration.class));
		
		
		Mockito.reset(module);
		Mockito.when(module.doConfigure(Mockito.any(TransactionalGraph.class), Mockito.any(FramedGraphConfiguration.class))).thenCallRealMethod();
		Graph configuredTransactionalGraph = module.configure(baseTransactionalGraph, config);
		Assert.assertEquals(baseTransactionalGraph, configuredTransactionalGraph);
		Mockito.verify(module).doConfigure(Mockito.any(TransactionalGraph.class), Mockito.any(FramedGraphConfiguration.class));
		Mockito.verify(module).doConfigure(Mockito.any(FramedGraphConfiguration.class));
	}
	
	
	@Test
	public void testWrapping() {
		Graph baseGraph = Mockito.mock(Graph.class);
		TransactionalGraph baseTransactionalGraph = Mockito.mock(TransactionalGraph.class);
		
		Graph wrappedGraph = Mockito.mock(Graph.class);
		TransactionalGraph wrappedTransactionalGraph = Mockito.mock(TransactionalGraph.class);
		
		FramedGraphConfiguration config = new FramedGraphConfiguration();
		AbstractModule module = Mockito.mock(AbstractModule.class);
		Mockito.when(module.doConfigure(Mockito.any(Graph.class), Mockito.any(FramedGraphConfiguration.class))).thenReturn(wrappedGraph);
		
		
		Graph configuredGraph = module.configure(baseGraph, config);
		Assert.assertEquals(wrappedGraph, configuredGraph);
		
		
		Mockito.reset(module);
		Mockito.when(module.doConfigure(Mockito.any(TransactionalGraph.class), Mockito.any(FramedGraphConfiguration.class))).thenReturn(wrappedTransactionalGraph);
		Graph configuredTransactionalGraph = module.configure(baseTransactionalGraph, config);
		Assert.assertEquals(wrappedTransactionalGraph, configuredTransactionalGraph);
		
		
		
	}
}
