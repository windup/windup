package org.jboss.windup.rules.apps.java.scan.dao.impl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.jboss.windup.rules.apps.ejb.dao.JarArchiveDao;
import org.jboss.windup.graph.dao.exception.ArchiveIndexReaderException;
import org.jboss.windup.graph.dao.impl.BaseDaoImpl;
import org.jboss.windup.rules.apps.java.scan.model.JarArchiveModel;

@Singleton
public class JarArchiveDaoImpl extends BaseDaoImpl<JarArchiveModel> implements JarArchiveDao
{

    public JarArchiveDaoImpl()
    {
        super(JarArchiveModel.class);
    }

    public Iterable<JarArchiveModel> findArchiveByMD5(String value) throws ArchiveIndexReaderException
    {
        return this.getByProperty("md5Hash", value);
    }

    public Iterable<JarArchiveModel> findArchiveBySHA1(String value) throws ArchiveIndexReaderException
    {
        return this.getByProperty("sha1Hash", value);
    }

    public Iterable<JarArchiveModel> findArchiveByName(String value) throws ArchiveIndexReaderException
    {
        return this.getByProperty("archiveName", value);
    }

    public Iterable<JarArchiveModel> findArchiveByQualifiedClassName(String clz) throws ArchiveIndexReaderException
    {
        return null;
    }

    public Iterable<JarArchiveModel> findUnusedJars()
    {
        List<JarArchiveModel> iterable = new LinkedList<JarArchiveModel>();
        for (JarArchiveModel archive : getAll())
        {
            if (archive.providesForArchives().iterator().hasNext())
            {
                continue;
            }
            iterable.add(archive);
        }

        return iterable;
    }

    public Iterable<JarArchiveModel> findCircularReferences(JarArchiveModel archive)
    {
        Set<JarArchiveModel> results = new HashSet<>();

        // if it is both providing for and depending on, is circular.
        Set<String> set = new HashSet<>();
        for (JarArchiveModel d : archive.dependsOnArchives())
        {
            // set.add(d.asFile().getAbsolutePath());
        }
        for (JarArchiveModel p : archive.providesForArchives())
        {
            // if (set.contains(p.asFile().getAbsolutePath()))
            // {
            // results.add(p);
            // }
        }

        return results;
    }

}
