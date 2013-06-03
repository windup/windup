package org.jboss.windup.visitor;

import org.jboss.windup.resource.type.FileMeta;
import org.jboss.windup.resource.type.JavaMeta;
import org.jboss.windup.resource.type.JspMeta;
import org.jboss.windup.resource.type.ManifestMeta;
import org.jboss.windup.resource.type.ResourceMeta;
import org.jboss.windup.resource.type.TemporarySourceFile;
import org.jboss.windup.resource.type.XmlMeta;
import org.jboss.windup.resource.type.ZipEntryMeta;
import org.jboss.windup.resource.type.archive.ArchiveMeta;
import org.jboss.windup.resource.type.archive.DirectoryMeta;
import org.jboss.windup.resource.type.archive.ZipMeta;

public interface MetaVisitor<T extends ResourceMeta> {
	public void visit(FileMeta clz);

	public void visit(JavaMeta clz);

	public void visit(TemporarySourceFile clz);

	public void visit(JspMeta clz);

	public void visit(ManifestMeta clz);

	public void visit(ResourceMeta clz);

	public void visit(XmlMeta clz);

	public void visit(ZipEntryMeta clz);

	public void visit(ArchiveMeta clz);

	public void visit(DirectoryMeta clz);

	public void visit(ZipMeta clz);
}
