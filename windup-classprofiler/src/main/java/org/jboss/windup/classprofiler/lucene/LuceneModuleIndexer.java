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
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jboss.windup.classprofiler.ModuleIndexer;
import org.jboss.windup.classprofiler.exception.ArchiveIndexReaderException;
import org.jboss.windup.classprofiler.exception.ModuleIndexReaderException;
import org.jboss.windup.classprofiler.exception.ModuleIndexWriteException;
import org.jboss.windup.classprofiler.lucene.transformer.ModuleTransformer;
import org.jboss.windup.classprofiler.metadata.ArchiveVO;
import org.jboss.windup.classprofiler.metadata.ModuleVO;

public class LuceneModuleIndexer implements ModuleIndexer {

	private final LuceneArchiveIndexer archiveIndexer;
	
	private final Directory moduleIndexDir;
	private final IndexWriterConfig iwc;
	
	public LuceneModuleIndexer(File luceneDirectory) throws IOException {
		archiveIndexer = new LuceneArchiveIndexer(luceneDirectory);
		
		File luceneModuleIndexDirectory = new File(luceneDirectory, "moduleIndex");
		moduleIndexDir = FSDirectory.open(luceneModuleIndexDirectory);
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
		iwc = new IndexWriterConfig(Version.LUCENE_44, analyzer);
		iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	
		IndexWriter writer = null;
		try {
			writer = new IndexWriter(moduleIndexDir, iwc);
			writer.commit();
		}
		finally {
			IOUtils.closeQuietly(writer);
		}
	}
	
	@Override
	public void addModule(ModuleVO module) throws ModuleIndexWriteException {
		Document document = ModuleTransformer.toDocument(module);
		IndexWriter writer = null;
		try {
			writer = new IndexWriter(moduleIndexDir, iwc);
			writer.updateDocument(new Term(ModuleTransformer.MODULE_ID, document.get(ModuleTransformer.MODULE_ID)), document);
			writer.commit();
		}
		catch(Exception e) {
			throw new ModuleIndexWriteException("Exception writing class: "+module.toString(), e);
		}
		finally {
			IOUtils.closeQuietly(writer);
		}
	}
	
	@Override
	public Collection<ModuleVO> findModuleProvidingClass(String clz) throws ModuleIndexReaderException {
		Set<ModuleVO> modules = new HashSet<ModuleVO>();
		
		//first, find all archives containing the class.
		try {
			Collection<ArchiveVO> archives = archiveIndexer.findArchiveByQualifiedClassName(clz);
			//find all references between module and archive by sha1.
			modules.addAll(findModulesContainingArchive(archives));
		} catch (ArchiveIndexReaderException e) {
			throw new ModuleIndexReaderException("Exception finding module for class: "+clz, e);
		}
		
		return modules;
	}
	
	protected Collection<ModuleVO> findModulesContainingArchive(Collection<ArchiveVO> archives) throws ModuleIndexReaderException {
		Set<ModuleVO> modules = new HashSet<ModuleVO>();
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(moduleIndexDir);
			IndexSearcher searcher = new IndexSearcher(reader);
			
			BooleanQuery query = new BooleanQuery();
			for(ArchiveVO archive : archives) {
				query.add(new TermQuery(new Term(ModuleTransformer.MODULE_ARCHIVE, archive.getSha1())), BooleanClause.Occur.SHOULD);
			}
			query.setMinimumNumberShouldMatch(1);
			
			int numResults = 100;
			ScoreDoc[] hits = searcher.search(query, numResults).scoreDocs;
			
			for(int i=0; i< hits.length; i++) {
				Document doc = searcher.doc(hits[i].doc);
				modules.add(ModuleTransformer.fromDocument(doc));
			}
		}
		catch(Exception e) {
			throw new ModuleIndexReaderException("Exception querying archives.", e);
		}
		finally {
			IOUtils.closeQuietly(reader);
		}
		
		return modules;
	}
}
