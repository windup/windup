package org.jboss.windup.graph.rexster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.listeners.AfterGraphInitializationListener;
import org.jboss.windup.graph.listeners.BeforeGraphCloseListener;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.rexster.protocol.EngineConfiguration;
import com.tinkerpop.rexster.protocol.EngineController;
import com.tinkerpop.rexster.server.DefaultRexsterApplication;
import com.tinkerpop.rexster.server.HttpRexsterServer;
import com.tinkerpop.rexster.server.RexProRexsterServer;
import com.tinkerpop.rexster.server.RexsterProperties;

public class RexsterInitializer implements AfterGraphInitializationListener, BeforeGraphCloseListener
{
    private String rexsterExtractDirectory;
    private Map<String, Object> configuration;
    private static final Logger log = Logger.getLogger(RexsterInitializer.class.getName());
    boolean started = false;
    private RexProRexsterServer rexPro;

    public RexsterInitializer()
    {
    }

    private Addon getAddon()
    {
        Set<Addon> addons = SimpleContainer.getFurnace(RexsterInitializer.class.getClassLoader()).getAddonRegistry().getAddons();
        for (Addon addon : addons)
        {
            boolean isRexster = addon.getId().getName().contains("rexster");
            if (isRexster)
            {
                return addon;
            }
        }
        return null;
    }

    public void start(FramedGraph<EventGraph<TitanGraph>> graph)
    {
        try (PrintWriter out = new PrintWriter("rexster.xml"))
        {
            String path = getClass().getResource("/public").getPath();
            if (path.contains("!"))
            {
                path = path.split("!")[0];
            }
            if (path.contains(":"))
            {
                path = path.split(":")[1];
            }
            if (path.endsWith(".jar"))
            {
                File rexsterAddonDir = new File(path);

                new File(rexsterExtractDirectory).mkdirs();
                extractZipFile(rexsterAddonDir, rexsterExtractDirectory);
            }
            else
            {
                // remove the "public" from the end
                String substring = path.substring(0, path.length() - 8);
                rexsterExtractDirectory = substring;
            }
            out.println(createRexsterXmlFileString(configuration));
            out.flush();
            RexsterProperties properties = new RexsterProperties("rexster.xml");
            configureScriptEngine(properties);
            HttpRexsterServer rexsterServer = new HttpRexsterServer(properties);
            rexsterServer.start(new DefaultRexsterApplication("main", graph.getBaseGraph()));

            rexPro = new RexProRexsterServer(properties, true);
            rexPro.start(new DefaultRexsterApplication("main", graph));
            started = true;

        }
        catch (FileNotFoundException e)
        {
            log.warning("rexster was not able to run");
        }
        catch (Exception e)
        {
            log.warning("Error while creating rexster.xml");
        }
    }

    public boolean isStarted()
    {
        return started;
    }

    private void extractZipFile(File jarFile, String destDir) throws IOException
    {
        java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
        java.util.Enumeration enumEntries = jar.entries();
        while (enumEntries.hasMoreElements())
        {
            java.util.jar.JarEntry file = (java.util.jar.JarEntry) enumEntries.nextElement();
            java.io.File f = new java.io.File(destDir + java.io.File.separator + file.getName());
            if (file.isDirectory())
            { // if its a directory, create it
                f.mkdir();
                continue;
            }
            java.io.InputStream is = jar.getInputStream(file); // get the input stream
            java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
            while (is.available() > 0)
            {  // write contents of 'is' to 'fos'
                fos.write(is.read());
            }
            fos.close();
            is.close();
        }
    }

    private void configureScriptEngine(RexsterProperties properties)
    {
        // the EngineController needs to be configured statically before requests start serving so that it can
        // properly construct ScriptEngine objects with the correct reset policy. allow scriptengines to be
        // configured so that folks can drop in different gremlin flavors.
        final List<EngineConfiguration> configuredScriptEngines = new ArrayList<EngineConfiguration>();
        final List<HierarchicalConfiguration> configs = properties.getScriptEngines();
        for (HierarchicalConfiguration config : configs)
        {
            configuredScriptEngines.add(new EngineConfiguration(config));
        }

        EngineController.configure(configuredScriptEngines);
    }

    private String createRexsterXmlFileString(Map<String, Object> conf)
    {
        String fileString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<rexster>\n" +
                    "  <http>\n" +
                    "    <server-port>8182</server-port>\n" +
                    "    <server-host>0.0.0.0</server-host>\n" +
                    "    <base-uri>http://localhost</base-uri>\n" +
                    "    <web-root>" + rexsterExtractDirectory + "/public</web-root>\n" +
                    // "    <http.web-root> </http.web-root> \n" +
                    "    <character-set>UTF-8</character-set>\n" +
                    "    <enable-jmx>false</enable-jmx>\n" +
                    "    <enable-doghouse>true</enable-doghouse>\n" +
                    "    <max-post-size>2097152</max-post-size>\n" +
                    "    <max-header-size>8192</max-header-size>\n" +
                    "    <upload-timeout-millis>30000</upload-timeout-millis>\n" +
                    "    <thread-pool>\n" +
                    "      <worker>\n" +
                    "        <core-size>8</core-size>\n" +
                    "        <max-size>8</max-size>\n" +
                    "      </worker>\n" +
                    "      <kernal>\n" +
                    "        <core-size>4</core-size>\n" +
                    "        <max-size>4</max-size>\n" +
                    "      </kernal>\n" +
                    "    </thread-pool>\n" +
                    "    <io-strategy>leader-follower</io-strategy>\n" +
                    "  </http>\n" +
                    "  <rexpro>\n" +
                    "    <server-port>8184</server-port>\n" +
                    "    <server-host>0.0.0.0</server-host>\n" +
                    "    <session-max-idle>1790000</session-max-idle>\n" +
                    "    <session-check-interval>3000000</session-check-interval>\n" +
                    "    <enable-jmx>false</enable-jmx>\n" +
                    "    <read-buffer>65536</read-buffer>\n" +
                    "    <thread-pool>\n" +
                    "      <worker>\n" +
                    "          <core-size>8</core-size>\n" +
                    "        <max-size>8</max-size>\n" +
                    "      </worker>\n" +
                    "      <kernal>\n" +
                    "        <core-size>4</core-size>\n" +
                    "        <max-size>4</max-size>\n" +
                    "      </kernal>\n" +
                    "    </thread-pool>\n" +
                    "    <io-strategy>leader-follower</io-strategy>\n" +
                    "  </rexpro>\n" +
                    "  <shutdown-port>8183</shutdown-port>\n" +
                    "  <shutdown-host>127.0.0.1</shutdown-host>\n" +
                    "  <config-check-interval>10000</config-check-interval>\n" +
                    "  <script-engines>\n" +
                    "    <script-engine>\n" +
                    "      <name>gremlin-groovy</name>\n" +
                    "      <reset-threshold>-1</reset-threshold>\n" +
                    "      <init-scripts>config/init.groovy</init-scripts>\n" +
                    "      <imports>com.tinkerpop.rexster.client.*</imports>\n" +
                    "      <static-imports>java.lang.Math.PI</static-imports>\n" +
                    "    </script-engine>\n" +
                    "  </script-engines>\n" +
                    "  <security>\n" +
                    "    <authentication>\n" +
                    "        <type>default</type>\n" +
                    "        <configuration>\n" +
                    "          <users>\n" +
                    "            <user>\n" +
                    "              <username>rexster</username>\n" +
                    "              <password>rexster</password>\n" +
                    "            </user>\n" +
                    "          </users>\n" +
                    "        </configuration>\n" +
                    "    </authentication>\n" +
                    "  </security>\n" +
                    "    <metrics>\n" +
                    "        <reporter>\n" +
                    "            <type>jmx</type>\n" +
                    "        </reporter>\n" +
                    "        <reporter>\n" +
                    "            <type>http</type>\n" +
                    "        </reporter>\n" +
                    "        <reporter>\n" +
                    "            <type>console</type>\n" +
                    "            <properties>\n" +
                    "                <rates-time-unit>SECONDS</rates-time-unit>\n" +
                    "                <duration-time-unit>SECONDS</duration-time-unit>\n" +
                    "                <report-period>10</report-period>\n" +
                    "                <report-time-unit>MINUTES</report-time-unit>\n" +
                    "                <includes>http.rest.*</includes>\n" +
                    "                <excludes>http.rest.*.delete</excludes>\n" +
                    "            </properties>\n" +
                    "        </reporter>\n" +
                    "    </metrics>\n" +
                    "  <graphs>\n" +
                    "<graph>\n" +
                    "    <graph-name>titan</graph-name>\n" +
                    "    <graph-type>com.thinkaurelius.titan.tinkerpop.rexster.TitanGraphConfiguration</graph-type>\n" +
                    "    <graph-location> " + conf.get("storage.directory") + "</graph-location>\n" +
                    "    <graph-read-only>false</graph-read-only>\n" +
                    "    <properties>\n" +
                    "        <storage.backend>" + conf.get("storage.backend") + "</storage.backend>\n" +
                    "        <storage.directory>" + conf.get("storage.directory") + "</storage.directory>\n" +
                    "        <index.search.backend>" + conf.get("index.search.backend")
                    + "</index.search.backend>\n" +
                    "        <index.search.directory> " + conf.get("index.search.directory")
                    + "</index.search.directory>\n" +
                    "    </properties>\n" +
                    "    <extensions>\n" +
                    "        <allows>\n" +
                    "            <allow>tp:gremlin</allow>\n" +
                    "        </allows>\n" +
                    "    </extensions>\n" +
                    " </graph>" +
                    "  </graphs>\n" +
                    "</rexster>";
        return fileString;

    }

    @Override
    public void afterGraphStarted(Map<String, Object> configuration, GraphContext graphContext)
    {
        this.configuration = configuration;
        rexsterExtractDirectory = getAddon().getRepository().getAddonDescriptor(getAddon().getId()).getParent() + "/rexster-extract";
        start(graphContext.getFramed());
    }

    @Override
    public void beforeGraphClose()
    {
        try
        {
            if(rexPro !=null) {
               rexPro.stop();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
