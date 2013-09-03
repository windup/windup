package org.jboss.windup.lucene.collector;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

public class UniqueFieldCollector extends Collector {
	private Scorer scorer;
	private int docBase;
	
	@Override
	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}

	@Override
	public void collect(int doc) throws IOException {
		  System.out.println("doc=" + doc + docBase + " score=" + scorer.score());		
	}

	@Override
	public void setNextReader(AtomicReaderContext context) throws IOException {
		this.docBase = context.docBase;
	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

}
