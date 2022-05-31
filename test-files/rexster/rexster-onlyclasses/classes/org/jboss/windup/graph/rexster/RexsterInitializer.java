package org.jboss.windup.graph.rexster;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.rexster.protocol.EngineConfiguration;
import com.tinkerpop.rexster.protocol.EngineController;
import com.tinkerpop.rexster.server.DefaultRexsterApplication;
import com.tinkerpop.rexster.server.HttpRexsterServer;
import com.tinkerpop.rexster.server.RexProRexsterServer;
import com.tinkerpop.rexster.server.RexsterProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.forge.furnace.repositories.AddonRepository;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.listeners.AfterGraphInitializationListener;
import org.jboss.windup.graph.listeners.BeforeGraphCloseListener;

public class RexsterInitializer implements AfterGraphInitializationListener, BeforeGraphCloseListener {
    private static final Logger log = Logger.getLogger(RexsterInitializer.class.getName());
    boolean started = false;
    private String rexsterExtractDirectory;
    private Map<String, Object> configuration;
    private RexProRexsterServer rexPro;
    private HttpRexsterServer rexsterServer;

    private Addon getAddon() {
        Set<Addon> addons = SimpleContainer.getFurnace(RexsterInitializer.class.getClassLoader()).getAddonRegistry(new AddonRepository[0]).getAddons();
        Iterator var2 = addons.iterator();

        Addon addon;
        boolean isRexster;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            addon = (Addon) var2.next();
            isRexster = addon.getId().getName().contains("rexster");
        } while (!isRexster);

        return addon;
    }

    public void start(FramedGraph<EventGraph<TitanGraph>> graph) {
        try {
            PrintWriter out = new PrintWriter("rexster.xml");
            Throwable var3 = null;

            try {
                String path = this.getClass().getResource("/public").getPath();
                if (path.contains("!")) {
                    path = path.split("!")[0];
                }

                if (path.contains(":")) {
                    path = path.split(":")[1];
                }

                if (path.endsWith(".jar")) {
                    File rexsterAddonDir = new File(path);
                    (new File(this.rexsterExtractDirectory)).mkdirs();
                    this.extractZipFile(rexsterAddonDir, this.rexsterExtractDirectory);
                } else {
                    String substring = path.substring(0, path.length() - 8);
                    this.rexsterExtractDirectory = substring;
                }

                out.println(this.createRexsterXmlFileString(this.configuration));
                out.flush();
                RexsterProperties properties = new RexsterProperties("rexster.xml");
                this.configureScriptEngine(properties);
                this.rexsterServer = new HttpRexsterServer(properties);
                this.rexsterServer.start(new DefaultRexsterApplication("main", graph.getBaseGraph()));
                this.rexPro = new RexProRexsterServer(properties, true);
                this.rexPro.start(new DefaultRexsterApplication("main", graph));
                this.started = true;
            } catch (Throwable var15) {
                var3 = var15;
                throw var15;
            } finally {
                if (out != null) {
                    if (var3 != null) {
                        try {
                            out.close();
                        } catch (Throwable var14) {
                            var3.addSuppressed(var14);
                        }
                    } else {
                        out.close();
                    }
                }

            }
        } catch (FileNotFoundException var17) {
            log.warning("rexster was not able to run");
        } catch (Exception var18) {
            log.warning("Error while creating rexster.xml");
        }

    }

    public boolean isStarted() {
        return this.started;
    }

    private void extractZipFile(File jarFile, String destDir) throws IOException {
        JarFile jar = new JarFile(jarFile);
        Enumeration enumEntries = jar.entries();

        while (true) {
            while (enumEntries.hasMoreElements()) {
                JarEntry file = (JarEntry) enumEntries.nextElement();
                File f = new File(destDir + File.separator + file.getName());
                if (file.isDirectory()) {
                    f.mkdir();
                } else {
                    InputStream is = jar.getInputStream(file);
                    FileOutputStream fos = new FileOutputStream(f);

                    while (is.available() > 0) {
                        fos.write(is.read());
                    }

                    fos.close();
                    is.close();
                }
            }

            return;
        }
    }

    private void configureScriptEngine(RexsterProperties properties) {
        List<EngineConfiguration> configuredScriptEngines = new ArrayList();
        List<HierarchicalConfiguration> configs = properties.getScriptEngines();
        Iterator var4 = configs.iterator();

        while (var4.hasNext()) {
            HierarchicalConfiguration config = (HierarchicalConfiguration) var4.next();
            configuredScriptEngines.add(new EngineConfiguration(config));
        }

        EngineController.configure(configuredScriptEngines);
    }

    private String createRexsterXmlFileString(Map<String, Object> conf) {
        String fileString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<rexster>\n  <http>\n    <server-port>8182</server-port>\n    <server-host>0.0.0.0</server-host>\n    <base-uri>http://localhost</base-uri>\n    <web-root>" + this.rexsterExtractDirectory + "/public</web-root>\n" + "    <character-set>UTF-8</character-set>\n" + "    <enable-jmx>false</enable-jmx>\n" + "    <enable-doghouse>true</enable-doghouse>\n" + "    <max-post-size>2097152</max-post-size>\n" + "    <max-header-size>8192</max-header-size>\n" + "    <upload-timeout-millis>30000</upload-timeout-millis>\n" + "    <thread-pool>\n" + "      <worker>\n" + "        <core-size>8</core-size>\n" + "        <max-size>8</max-size>\n" + "      </worker>\n" + "      <kernal>\n" + "        <core-size>4</core-size>\n" + "        <max-size>4</max-size>\n" + "      </kernal>\n" + "    </thread-pool>\n" + "    <io-strategy>leader-follower</io-strategy>\n" + "  </http>\n" + "  <rexpro>\n" + "    <server-port>8184</server-port>\n" + "    <server-host>0.0.0.0</server-host>\n" + "    <session-max-idle>1790000</session-max-idle>\n" + "    <session-check-interval>3000000</session-check-interval>\n" + "    <enable-jmx>false</enable-jmx>\n" + "    <read-buffer>65536</read-buffer>\n" + "    <thread-pool>\n" + "      <worker>\n" + "          <core-size>8</core-size>\n" + "        <max-size>8</max-size>\n" + "      </worker>\n" + "      <kernal>\n" + "        <core-size>4</core-size>\n" + "        <max-size>4</max-size>\n" + "      </kernal>\n" + "    </thread-pool>\n" + "    <io-strategy>leader-follower</io-strategy>\n" + "  </rexpro>\n" + "  <shutdown-port>8183</shutdown-port>\n" + "  <shutdown-host>127.0.0.1</shutdown-host>\n" + "  <config-check-interval>10000</config-check-interval>\n" + "  <script-engines>\n" + "    <script-engine>\n" + "      <name>gremlin-groovy</name>\n" + "      <reset-threshold>-1</reset-threshold>\n" + "      <init-scripts>config/init.groovy</init-scripts>\n" + "      <imports>com.tinkerpop.rexster.client.*</imports>\n" + "      <static-imports>java.lang.Math.PI</static-imports>\n" + "    </script-engine>\n" + "  </script-engines>\n" + "  <security>\n" + "    <authentication>\n" + "        <type>default</type>\n" + "        <configuration>\n" + "          <users>\n" + "            <user>\n" + "              <username>rexster</username>\n" + "              <password>rexster</password>\n" + "            </user>\n" + "          </users>\n" + "        </configuration>\n" + "    </authentication>\n" + "  </security>\n" + "    <metrics>\n" + "        <reporter>\n" + "            <type>jmx</type>\n" + "        </reporter>\n" + "        <reporter>\n" + "            <type>http</type>\n" + "        </reporter>\n" + "        <reporter>\n" + "            <type>console</type>\n" + "            <properties>\n" + "                <rates-time-unit>SECONDS</rates-time-unit>\n" + "                <duration-time-unit>SECONDS</duration-time-unit>\n" + "                <report-period>10</report-period>\n" + "                <report-time-unit>MINUTES</report-time-unit>\n" + "                <includes>http.rest.*</includes>\n" + "                <excludes>http.rest.*.delete</excludes>\n" + "            </properties>\n" + "        </reporter>\n" + "    </metrics>\n" + "  <graphs>\n" + "<graph>\n" + "    <graph-name>titan</graph-name>\n" + "    <graph-type>com.thinkaurelius.titan.tinkerpop.rexster.TitanGraphConfiguration</graph-type>\n" + "    <graph-location> " + conf.get("storage.directory") + "</graph-location>\n" + "    <graph-read-only>false</graph-read-only>\n" + "    <properties>\n" + "        <storage.backend>" + conf.get("storage.backend") + "</storage.backend>\n" + "        <storage.directory>" + conf.get("storage.directory") + "</storage.directory>\n" + "        <index.search.backend>" + conf.get("index.search.backend") + "</index.search.backend>\n" + "        <index.search.directory> " + conf.get("index.search.directory") + "</index.search.directory>\n" + "    </properties>\n" + "    <extensions>\n" + "        <allows>\n" + "            <allow>tp:gremlin</allow>\n" + "        </allows>\n" + "    </extensions>\n" + " </graph>" + "  </graphs>\n" + "</rexster>";
        return fileString;
    }

    public void afterGraphStarted(Map<String, Object> configuration, GraphContext graphContext) {
        this.configuration = configuration;
        this.rexsterExtractDirectory = this.getAddon().getRepository().getAddonDescriptor(this.getAddon().getId()).getParent() + "/rexster-extract";
        this.start(graphContext.getFramed());
    }

    public void beforeGraphClose() {
        try {
            if (this.rexPro != null) {
                this.rexPro.stop();
            }

            if (this.rexsterServer != null) {
                this.rexsterServer.stop();
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }
}
