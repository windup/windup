package org.jboss.windup.classprofiler.lucene.transformer;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.jboss.windup.classprofiler.metadata.ArchiveVO;

public class ArchiveTransformer {

	public static final String ARCHIVE_NAME = "archiveName";
	public static final String ARCHIVE_VERSION = "archiveVersion";
	public static final String ARCHIVE_SHA1 = "archiveSHA1";
	public static final String ARCHIVE_MD5 = "archiveMD5";
	
	public static ArchiveVO fromDocument(Document doc) {
		ArchiveVO archive = new ArchiveVO();

		for(IndexableField indexField : doc.getFields()) {
			if(StringUtils.equals(ARCHIVE_NAME, indexField.name())) {
				archive.setName(doc.getField(ARCHIVE_NAME).stringValue());
			}
			else if(StringUtils.equals(ARCHIVE_VERSION, indexField.name())) {
				archive.setVersion(doc.getField(ARCHIVE_VERSION).stringValue());
			}
			else if(StringUtils.equals(ARCHIVE_SHA1, indexField.name())) {
				archive.setSha1(doc.getField(ARCHIVE_SHA1).stringValue());
			}
			else if(StringUtils.equals(ARCHIVE_MD5, indexField.name())) {
				archive.setMd5(doc.getField(ARCHIVE_MD5).stringValue());
			}
			else {
				archive.getProperties().put(indexField.name(), indexField.stringValue());
			}
		}
		
		return archive;
	}

	public static Document toDocument(ArchiveVO archive) {
		Document document = new Document();
		document.add(new StringField(ARCHIVE_NAME, archive.getName(), Field.Store.YES));
		document.add(new StringField(ARCHIVE_VERSION, archive.getName(), Field.Store.YES));
		document.add(new StringField(ARCHIVE_SHA1, archive.getSha1(), Field.Store.YES));
		document.add(new StringField(ARCHIVE_MD5, archive.getMd5(), Field.Store.YES));

		for(String key : archive.getProperties().keySet()) {
			document.add(new StringField(key, archive.getProperties().get(key), Field.Store.YES));
		}
		
		
		return document;
	}

}
