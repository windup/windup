package org.jboss.windup.qs.skiparchives.nexusreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.util.Bits;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.context.IndexCreator;
import org.apache.maven.index.context.IndexUtils;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.updater.IndexUpdateRequest;
import org.apache.maven.index.updater.IndexUpdateResult;
import org.apache.maven.index.updater.IndexUpdater;
import org.apache.maven.index.updater.ResourceFetcher;
import org.apache.maven.index.updater.WagonHelper;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.observers.AbstractTransferListener;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.WindupPathUtil;


/**
 * Downloads Maven index from given repository and produces a list of all artifacts,
 * using this format: "SHA G:A:V[:C]".
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class IndexToGavMappingConverter
{
    private static final Logger log = Logging.get(IndexToGavMappingConverter.class);

    // Input
    private File dataDir;

    // Work objects

    private final PlexusContainer plexusContainer;
    private final Indexer indexer;
    private final IndexUpdater indexUpdater;
    private final Wagon httpWagon;
    private final IndexingContext centralContext;

    // Files where local cache is (if any) and Lucene Index should be located
    private final File centralLocalCache;
    private final File centralIndexDir;



    public IndexToGavMappingConverter(File dataDir, String id, String url)
        throws PlexusContainerException, ComponentLookupException, IOException
    {
        this.dataDir = dataDir;

        // Create Plexus container, the Maven default IoC container.
        final DefaultContainerConfiguration config = new DefaultContainerConfiguration();
        config.setClassPathScanning( PlexusConstants.SCANNING_INDEX );
        this.plexusContainer = new DefaultPlexusContainer(config);

        // Lookup the indexer components from plexus.
        this.indexer = plexusContainer.lookup( Indexer.class );
        this.indexUpdater = plexusContainer.lookup( IndexUpdater.class );
        // Lookup wagon used to remotely fetch index.
        this.httpWagon = plexusContainer.lookup( Wagon.class, "http" );

        // Files where local cache is (if any) and Lucene Index should be located
        this.centralLocalCache = new File( this.dataDir, id + "-cache" );
        this.centralIndexDir = new File( this.dataDir,   id + "-index" );

        // Creators we want to use (search for fields it defines).
        // See https://maven.apache.org/maven-indexer/indexer-core/apidocs/index.html?constant-values.html
        List<IndexCreator> indexers = new ArrayList();
        // https://maven.apache.org/maven-indexer/apidocs/org/apache/maven/index/creator/MinimalArtifactInfoIndexCreator.html
        indexers.add( plexusContainer.lookup( IndexCreator.class, "min" ) );
        // https://maven.apache.org/maven-indexer/apidocs/org/apache/maven/index/creator/JarFileContentsIndexCreator.html
        //indexers.add( plexusContainer.lookup( IndexCreator.class, "jarContent" ) );
        // https://maven.apache.org/maven-indexer/apidocs/org/apache/maven/index/creator/MavenPluginArtifactInfoIndexCreator.html
        //indexers.add( plexusContainer.lookup( IndexCreator.class, "maven-plugin" ) );

        // Create context for central repository index.
        this.centralContext = this.indexer.createIndexingContext(
                id + "Context", id, this.centralLocalCache, this.centralIndexDir,
                url, null, true, true, indexers );
    }




    /**
     * Does all the downloading / partial update.
     */
    public void updateIndex() throws IOException
    {
        int noUpdateDays = 7;

        // Local repo update time - TODO.
        Date localIndexUpdated = null;
        Date repoCurrentTimestamp = this.centralContext.getTimestamp();

        if (localIndexUpdated != null)
        {
            // Do not update if remote repo was not updated since last update.
            if (new Date().before(DateUtils.addDays(repoCurrentTimestamp, noUpdateDays)) )
                return;

            // Do not update if local repo was updated in last $noUpdateDays.
            if (this.centralIndexDir.exists())
            {
                final long lastMod = this.centralIndexDir.lastModified();
                if (lastMod != 0)
                {
                    final long nextMod = lastMod + noUpdateDays * 24 * 3600;
                    if (nextMod > System.currentTimeMillis() )
                        return;
                }
            }
        }


        // Let's go download.
        log.info("Updating Index. This might take a while on first run.");
        // Create ResourceFetcher implementation to be used with IndexUpdateRequest
        // Here, we use Wagon based one as shorthand, but all we need is a ResourceFetcher implementation
        ResourceFetcher resourceFetcher = new WagonHelper.WagonFetcher( httpWagon, createLoggingTransferListener(), null, null );
        IndexUpdateRequest updateRequest = new IndexUpdateRequest( this.centralContext, resourceFetcher );
        IndexUpdateResult updateResult = indexUpdater.fetchAndUpdateIndex( updateRequest );
        if ( updateResult.isFullUpdate() )
            log.info("Index of Maven repo '" + this.centralContext.getId() + "' updated. URL: " + this.centralContext.getRepositoryUrl());
        else if (updateResult.getTimestamp().equals(repoCurrentTimestamp))
            log.info("No update needed, index is up to date!");
        else
            log.info("Incremental update happened, change covered " + repoCurrentTimestamp
                    + " - " + updateResult.getTimestamp() + " period.");
    }


    /**
     * A listener that just logs.
     */
    private TransferListener createLoggingTransferListener()
    {
        TransferListener listener = new AbstractTransferListener()
        {
            public void transferStarted( TransferEvent transferEvent )
            {
                log.info("  Downloading " + transferEvent.getResource().getName() );
            }

            public void transferCompleted( TransferEvent transferEvent )
            {
                log.info( "  Downloaded." );
            }
        };
        return listener;
    }


    /**
     * Prints all artifacts from the index, using format: SHA1 = G:A:V[:C].
     */
    public void printAllArtifacts(Writer out) throws IOException
    {
        final IndexSearcher searcher = this.centralContext.acquireIndexSearcher();
        try
        {
            final IndexReader ir = searcher.getIndexReader();
            Bits liveDocs = MultiFields.getLiveDocs(ir);
            for ( int i = 0; i < ir.maxDoc(); i++ )
            {
                if ( liveDocs == null || liveDocs.get( i ) )
                {
                    final Document doc = ir.document( i );
                    final ArtifactInfo ai = IndexUtils.constructArtifactInfo( doc, this.centralContext );

                    if (ai == null)
                        continue;
                    if (ai.getSha1() == null)
                        continue;
                    if (ai.getSha1().length() != 40)
                        continue;
                    if ("javadoc".equals(ai.getClassifier()))
                        continue;
                    if ("sources".equals(ai.getClassifier()))
                        continue;

                    out.append(StringUtils.lowerCase(ai.getSha1())).append(' ');
                    out.append(ai.getGroupId()).append(":");
                    out.append(ai.getArtifactId()).append(":");
                    out.append(ai.getVersion()).append(":");
                    out.append(StringUtils.defaultString(ai.getClassifier()));
                    out.append('\n');
                }
            }
        }
        finally
        {
            this.centralContext.releaseIndexSearcher( searcher );
        }
    }


    public void close() throws IOException
    {
        this.indexer.closeIndexingContext( this.centralContext, false );
    }


    /**
     * Sorts the lines from input file alphabetically and writes to the output file.
     */
    public static void sortFile(File input, File output) throws FileNotFoundException, IOException {

        log.fine("  Reading...");
        List<String> lineList;
        try (FileReader fileReader = new FileReader(input))
        {
            BufferedReader reader = new BufferedReader(fileReader);
            String inputLine;
            lineList = new ArrayList((int) (input.length() / 50));
            while ((inputLine = reader.readLine()) != null) {
                lineList.add(inputLine);
            }
        }

        log.fine("  Sorting...");
		Collections.sort(lineList);

        log.fine("  Writing...");
        try (FileWriter fileWriter = new FileWriter(output))
        {
            PrintWriter out = new PrintWriter(fileWriter);
            for (String outputLine : lineList) {
                out.println(outputLine);
            }
        }
    }


    /**
     * Makes this tool invokable from command line.
     * @param args 1st param tells which directory to store index data to.
     */
    public static void main( String[] args ) throws Exception
    {
        // Where to store data.

        File dataDir = WindupPathUtil.getWindupUserDir().resolve("temp/mavenIndexesData/").toFile();
        if (args.length > 0)
        {
            final File dir = new File(args[0]);
            dataDir = dir;
        }
        else
            System.out.println("No data directory given, using default: " + dataDir);

        if (dataDir.exists() && !dataDir.isDirectory())
        {
            System.err.println("Given path for data directory already exists and is a file: " + dataDir);
            return;
        }
        dataDir.mkdirs();


        final IndexToGavMappingConverter converter = new IndexToGavMappingConverter(dataDir, "central", "http://repo1.maven.org/maven2");
        // Update
        converter.updateIndex();

        // Print
        final File shaToGavFile = new File(dataDir.getPath(), "central.SHA1toGAVs.txt");
        log.info("Printing all artifacts to: " + shaToGavFile);
        converter.printAllArtifacts(new FileWriter(shaToGavFile));

        // Sort
        log.info("Sorting: " + shaToGavFile);
        sortFile(shaToGavFile, new File(dataDir, "central.SHA1toGAVs.sorted.txt"));
    }
}
