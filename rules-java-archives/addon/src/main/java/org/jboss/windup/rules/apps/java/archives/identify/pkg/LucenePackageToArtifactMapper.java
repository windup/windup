package org.jboss.windup.rules.apps.java.archives.identify.pkg;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.windup.rules.apps.java.archives.identify.LuceneArchiveIdentificationService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;

/**
 * Identifies archives by a contained package, using pre-created Lucene index. See the nexus-repository-indexer project.
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public class LucenePackageToArtifactMapper extends LuceneArchiveIdentificationService
{
    private static final Logger LOG = Logging.get(LucenePackageToArtifactMapper.class);

    // Field names, as created in nexus-repository-indexer.
    public static final String LUCENE_FIELD_COORDS = "coords";
    public static final String LUCENE_FIELD_PACKAGE = "package";


    public LucenePackageToArtifactMapper(File directory)
    {
        super(directory);
    }

    @Override
    public List<Coordinate> getCoordinates(String pkg)
    {

        Query query = new TermQuery(new Term(LUCENE_FIELD_PACKAGE, pkg));
        try
        {
            TopDocs results = getSearcher().search(query, 100); // TODO: Sort by version, descending?
            if (results.totalHits == 0)
                return Collections.EMPTY_LIST;

            List<Coordinate> coords = new ArrayList<>(results.totalHits);
            for (ScoreDoc scoreDoc : results.scoreDocs)
            {
                Document doc = getSearcher().doc(scoreDoc.doc);
                String coordsGAVPC = doc.get(LUCENE_FIELD_COORDS);
                coords.add(parseGAVPC(coordsGAVPC));
            }
            return coords;
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to find package to artefact mapping: " + pkg + "\n    " + e.getMessage(), e);
        }
    }


    // G:A:V[:C[:P]]
    // Maven uses G:A[:P[:C]]:V"
    public static final Pattern REGEX_GAVCP = Pattern.compile("([^: ]+):([^: ]+):([^: ]+)(:[^: ]+)?(:[^: ]+)?");

    private static final Coordinate parseGAVPC(String coordsGAVCP)
    {
        Matcher mat = REGEX_GAVCP.matcher(coordsGAVCP);
        if (!mat.matches())
            throw new IllegalArgumentException("Wrong Maven coordinates format, must be G:A:V[:C[:P]] . " + coordsGAVCP);

        return CoordinateBuilder.create()
                .setGroupId(mat.group(1))
                .setArtifactId(mat.group(2))
                .setVersion(mat.group(3))
                .setClassifier(mat.group(4))
                .setPackaging(mat.group(5));
    }

}
