package com.tinkerpop.frames.core;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Query.Compare;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.domain.classes.Person;
import com.tinkerpop.frames.domain.incidences.Knows;

/**
 * @author Bryn Cooke
 *
 */
public class FramedGraphQueryImplTest {
	
	@Test
	public void testDelegation() {
		GraphQuery mockGraphQuery = mock(GraphQuery.class);
		FramedGraph framedGraph = new FramedGraphFactory().create(null);
		
		FramedGraphQueryImpl query = new FramedGraphQueryImpl(framedGraph, mockGraphQuery);
		stub(mockGraphQuery.has("")).toReturn(mockGraphQuery);
		query.has("");
		verify(mockGraphQuery).has("");
		
		
		stub(mockGraphQuery.has("", "bar")).toReturn(mockGraphQuery);
		query.has("", "bar");
		verify(mockGraphQuery).has("", "bar");
		
		
		Predicate predicate = new Predicate() {
			
			@Override
			public boolean evaluate(Object first, Object second) {
				return false;
			}
		};
		stub(mockGraphQuery.has("", predicate, "bar")).toReturn(mockGraphQuery);
		query.has("", predicate, "bar");
		verify(mockGraphQuery).has(eq(""), same(predicate), eq("bar"));
		
		
		stub(mockGraphQuery.has("", 2, Compare.EQUAL)).toReturn(mockGraphQuery);
		query.has("", 2, Compare.EQUAL);
		verify(mockGraphQuery).has(eq(""), same(2), eq(Compare.EQUAL));
		
		stub(mockGraphQuery.hasNot("")).toReturn(mockGraphQuery);
		query.hasNot("");
		verify(mockGraphQuery).hasNot(eq(""));
		
		stub(mockGraphQuery.hasNot("", "bar")).toReturn(mockGraphQuery);
		query.hasNot("", "bar");
		verify(mockGraphQuery).hasNot(eq(""), eq("bar"));
		
		
		stub(mockGraphQuery.interval("", "bar", "bif")).toReturn(mockGraphQuery);
		query.interval("", "bar", "bif");
		verify(mockGraphQuery).interval(eq(""), eq("bar"), eq("bif"));
		
		stub(mockGraphQuery.limit(1)).toReturn(mockGraphQuery);
		query.limit(1);
		verify(mockGraphQuery).limit(1);
		
		
		
		List<Vertex> v = new ArrayList<Vertex>();
		stub(mockGraphQuery.vertices()).toReturn(v);
		query.vertices();
		verify(mockGraphQuery).vertices();
		
		Iterable<Person> people = query.vertices(Person.class);
		verify(mockGraphQuery, times(2)).vertices();
		assertFalse(people.iterator().hasNext());
		
		
		List<Edge> e = new ArrayList<Edge>();
		stub(mockGraphQuery.edges()).toReturn(e);
		query.edges();
		verify(mockGraphQuery).edges();
		
		Iterable<Knows> knows = query.edges(Knows.class);
		verify(mockGraphQuery, times(2)).edges();
		assertFalse(knows.iterator().hasNext());
		
	}
	
	
}
