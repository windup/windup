package org.jboss.windup.visitor;

import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.JavaMetadata;
import org.jboss.windup.metadata.type.JspMetadata;
import org.jboss.windup.metadata.type.ManifestMetadata;
import org.jboss.windup.metadata.type.ResourceMetadata;
import org.jboss.windup.metadata.type.TempSourceMetadata;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.jboss.windup.metadata.type.ZipEntryMetadata;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.metadata.type.archive.DirectoryMetadata;
import org.jboss.windup.metadata.type.archive.ZipMetadata;

public class EmptyMetaVisitor implements MetaVisitor {

	public void visit(FileMetadata clz) { }

	public void visit(JavaMetadata clz) { }

	public void visit(TempSourceMetadata clz) { }

	public void visit(JspMetadata clz) { }

	public void visit(ManifestMetadata clz) { }

	public void visit(ResourceMetadata clz) { }

	public void visit(XmlMetadata clz) { }

	public void visit(ZipEntryMetadata clz) { }

	public void visit(ArchiveMetadata clz) { }

	public void visit(DirectoryMetadata clz) { }

	public void visit(ZipMetadata clz) { }

}
