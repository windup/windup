package com.tinkerpop.frames;

import com.tinkerpop.blueprints.TransactionalGraph;

/**
 * An implementation of {@link FramedGraph} that supports transactions. 
 * 
 * @author Bryn Cooke
 *
 * @param <T>
 */
public class FramedTransactionalGraph<T extends TransactionalGraph> extends FramedGraph<T> implements TransactionalGraph {


	protected FramedTransactionalGraph(T baseGraph, FramedGraphConfiguration config) {
		super(baseGraph, config);
		
	}

	/* (non-Javadoc)
	 * @see com.tinkerpop.blueprints.TransactionalGraph#commit()
	 */
	@Override
	public void commit() {
		getBaseGraph().commit();
	}
	
	/* (non-Javadoc)
	 * @see com.tinkerpop.blueprints.TransactionalGraph#rollback()
	 */
	@Override
	public void rollback() {
		getBaseGraph().rollback();
	}

	/* (non-Javadoc)
	 * @see com.tinkerpop.blueprints.TransactionalGraph#stopTransaction(com.tinkerpop.blueprints.TransactionalGraph.Conclusion)
	 */
	@Override
	@Deprecated
	public void stopTransaction(Conclusion conclusion) {
		getBaseGraph().stopTransaction(conclusion);
	}
	
	
	
	
}
