package com.tinkerpop.frames;

import org.junit.Test;
import org.mockito.Mockito;

import com.tinkerpop.blueprints.TransactionalGraph;

public class FramedTransactionalGraphTest {
	@Test
	public void testTransactions() {

		TransactionalGraph t = Mockito.mock(TransactionalGraph.class);
		FramedTransactionalGraph<TransactionalGraph> g = new FramedTransactionalGraph<TransactionalGraph>(t, new FramedGraphConfiguration());
		g.commit();
		Mockito.verify(t).commit();
		Mockito.reset(t);
		
		g.rollback();
		Mockito.verify(t).rollback();
	}
}
