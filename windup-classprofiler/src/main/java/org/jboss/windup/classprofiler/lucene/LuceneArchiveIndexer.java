package org.jboss.windup.classprofiler.lucene;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.grouping.SearchGroup;
import org.apache.lucene.search.grouping.term.TermFirstPassGroupingCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.jboss.windup.classprofiler.ArchiveIndexer;
import org.jboss.windup.classprofiler.exception.ArchiveIndexReaderException;
import org.jboss.windup.classprofiler.exception.ArchiveIndexWriteException;
import org.jboss.windup.classprofiler.lucene.transformer.ArchiveTransformer;
import org.jboss.windup.classprofiler.lucene.transformer.ClassTransformer;
import org.jboss.windup.classprofiler.metadata.ArchiveVO;

public class LuceneArchiveIndexer implements ArchiveIndexer {

	private final Directory classIndexDir;
	private final Directory archiveIndexDir;
	private final IndexWriterConfig iwc;
	
	public LuceneArchiveIndexer(File luceneDirectory) throws IOException {
		File luceneArchiveIndexDirectory = new File(luceneDirectory, "archiveIndex");
		File luceneClassIndexDirectory = new File(luceneDirectory, "classIndex");
		
		archiveIndexDir = FSDirectory.open(luceneArchiveIndexDirectory);
		classIndexDir = FSDirectory.open(luceneClassIndexDirectory);
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
		iwc = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	
		IndexWriter writer = null;
		try {
			writer = new IndexWriter(archiveIndexDir, iwc);
			writer.commit();
		}
		finally {
			IOUtils.closeQuietly(writer);
		}
	}

	public void addArchive(ArchiveVO archive) throws ArchiveIndexWriteException {
		Document document =  ArchiveTransformer.toDocument(archive);
		
		IndexWriter writer = null;
		try {
			writer = new IndexWriter(archiveIndexDir, iwc);
			writer.updateDocument(new Term(ArchiveTransformer.ARCHIVE_SHA1, archive.getSha1()), document);
			writer.commit();
		}
		catch(Exception e) {
			throw new ArchiveIndexWriteException("Exception writing class: "+archive.getName(), e);
		}
		finally {
			IOUtils.closeQuietly(writer);
		}
	}

	@Override
	public Collection<ArchiveVO> findArchiveByMD5(String value) throws ArchiveIndexReaderException {
		return findArchiveByField(ArchiveTransformer.ARCHIVE_MD5, value);
	}

	@Override
	public Collection<ArchiveVO> findArchiveBySHA1(String value) throws ArchiveIndexReaderException {
		return findArchiveByField(ArchiveTransformer.ARCHIVE_SHA1, value);
	}

	@Override
	public Collection<ArchiveVO> findArchiveByName(String value) throws ArchiveIndexReaderException {
		return findArchiveByField(ArchiveTransformer.ARCHIVE_NAME, value);
	}
	
	public Collection<ArchiveVO> findArchiveByField(String field, String value) throws ArchiveIndexReaderException {
		Term term = new Term(field, value);
		BooleanQuery query = new BooleanQuery();
		query.add(new TermQuery(term), BooleanClause.Occur.MUST);
		return findArchiveByField(query);
	}
	
	protected Collection<ArchiveVO> findArchiveByField(Query query) throws ArchiveIndexReaderException {
		Set<ArchiveVO> results = new HashSet<ArchiveVO>();
		
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(archiveIndexDir);
			IndexSearcher searcher = new IndexSearcher(reader);
			
			int numResults = 100;
			ScoreDoc[] hits = searcher.search(query, numResults).scoreDocs;
			
			for(int i=0; i< hits.length; i++) {
				Document doc = searcher.doc(hits[i].doc);
				ArchiveVO archive = ArchiveTransformer.fromDocument(doc);
				results.add(archive);
			}
		}
		catch(Exception e) {
			throw new ArchiveIndexReaderException("Exception querying archive.", e);
		}
		finally {
			IOUtils.closeQuietly(reader);
		}
		
		return results;
	}

	public Collection<ArchiveVO> findArchiveByQualifiedClassName(String clz) throws ArchiveIndexReaderException {
		Set<String> archiveSHA1References = new HashSet<String>();
		
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(classIndexDir);
			IndexSearcher searcher = new IndexSearcher(reader);

			BooleanQuery query = new BooleanQuery();
			query.add(new TermQuery(new Term(ClassTransformer.QUALIFIED_NAME, clz)), BooleanClause.Occur.MUST);

			TermFirstPassGroupingCollector fpgc = new TermFirstPassGroupingCollector(ArchiveTransformer.ARCHIVE_SHA1, Sort.INDEXORDER, 100);
			searcher.search(query, fpgc);
			Collection<SearchGroup<BytesRef>> results = fpgc.getTopGroups(0, true);
			
			for(SearchGroup<BytesRef> ref : results) {
				archiveSHA1References.add(ref.groupValue.utf8ToString());
			}
		}
		catch(Exception e) {
			throw new ArchiveIndexReaderException("Exception querying class: "+clz, e);
		}
		finally {
			IOUtils.closeQuietly(reader);
		}
		
		Set<ArchiveVO> archives = new HashSet<ArchiveVO>();
		//now, for each, find archive by SHA1.
		for(String sha1Ref : archiveSHA1References) {
			archives.addAll(findArchiveByField(ArchiveTransformer.ARCHIVE_SHA1, sha1Ref));
		}
		
		return archives;
	}

	public Collection<ArchiveVO> findArchiveLeveragingDependency(String clz) throws ArchiveIndexReaderException {
		Set<String> archiveSHA1References = new HashSet<String>();
		
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(classIndexDir);
			IndexSearcher searcher = new IndexSearcher(reader);

			BooleanQuery query = new BooleanQuery();
			query.add(new TermQuery(new Term(ClassTransformer.DEPENDENCY, clz)), BooleanClause.Occur.MUST);
			
			TermFirstPassGroupingCollector fpgc = new TermFirstPassGroupingCollector(ArchiveTransformer.ARCHIVE_SHA1, Sort.INDEXORDER, 100);
			searcher.search(query, fpgc);
			Collection<SearchGroup<BytesRef>> results = fpgc.getTopGroups(0, true);
			
			for(SearchGroup<BytesRef> ref : results) {
				archiveSHA1References.add(ref.groupValue.utf8ToString());
			}
		}
		catch(Exception e) {
			throw new ArchiveIndexReaderException("Exception querying class: "+clz, e);
		}
		finally {
			IOUtils.closeQuietly(reader);
		}
		
		Set<ArchiveVO> archives = new HashSet<ArchiveVO>();
		//now, for each, find archive by SHA1.
		for(String sha1Ref : archiveSHA1References) {
			archives.addAll(findArchiveByField(ArchiveTransformer.ARCHIVE_SHA1, sha1Ref));
		}
		
		return archives;
	}

	@Override
	public Collection<ArchiveVO> findArchiveByNameAndVersion(String name, String version) throws ArchiveIndexReaderException {
		Term nameTerm = new Term(ArchiveTransformer.ARCHIVE_NAME, name);
		Term versionTerm = new Term(ArchiveTransformer.ARCHIVE_VERSION, version);
		BooleanQuery query = new BooleanQuery();
		query.add(new TermQuery(nameTerm), BooleanClause.Occur.MUST);
		query.add(new TermQuery(versionTerm), BooleanClause.Occur.MUST);
		return findArchiveByField(query);
	}

	


}
