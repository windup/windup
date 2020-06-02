package org.jboss.windup.tooling;

import java.io.File;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.LogRecord;

import javax.inject.Inject;

import io.vertx.core.json.JsonObject;

public class ToolingModeRunner implements IProgressMonitorAdapter 
{
    @Inject
    private ExecutionBuilder executionBuilder;

    private WindupToolingProgressMonitor progressMonitor;

    public ToolingModeRunner() 
    {
        this.progressMonitor = new ProgressMonitorAdapter(this);
    }

    public void setProgressMonitor(WindupToolingProgressMonitor monitor) 
    {
        this.progressMonitor = monitor;
    }

    public ExecutionResults run(
        Set<String> input,
        String output,
        boolean sourceMode,
        boolean ignoreReport,
        List<String> ignorePatterns,
        String windupHome,
        List<String> source,
        List<String> target,
        List<File> rulesDir) 
    {
        try 
        {
            executionBuilder.setInput(input);
            executionBuilder.setOutput(output);
            executionBuilder.setOption(IOptionKeys.SOURCE_MODE, sourceMode);
            executionBuilder.setOption(IOptionKeys.SKIP_REPORTS, ignoreReport);
            for (Iterator<String> iter = ignorePatterns.iterator(); iter.hasNext();) 
            {
                executionBuilder.ignore(iter.next());
            }
            executionBuilder.setWindupHome(windupHome);
            executionBuilder.setOption(IOptionKeys.SOURCE, source);
            executionBuilder.setOption(IOptionKeys.TARGET, target);
            executionBuilder.setOption(IOptionKeys.CUSTOM_RULES_DIR, rulesDir);
            executionBuilder.setProgressMonitor((WindupToolingProgressMonitor) this.progressMonitor);
            ExecutionResults results = executionBuilder.execute();
            results.serializeToXML(Paths.get(output + File.separatorChar + "results.xml"));
            System.out.println(":progress: {\"op\":\"complete\"}");
            return results;
        } catch (RemoteException e) 
        {
            e.printStackTrace();
        }
        return null;
    }

    private void log(String msg) 
    {
        System.out.println(":progress: " + msg);
    }

    @Override
    public void beginTask(String task, int totalWork)
    {
        JsonObject load = new JsonObject();
        load.put("op", "beginTask");
        load.put("task", task);
        load.put("totalWork", totalWork);
        this.log(load.toString());
    }

    @Override
    public void done()
    {
        JsonObject load = new JsonObject();
        load.put("op", "done");
        this.log(load.toString());
    }

    @Override
    public boolean isCancelled()
    {
        return false;
    }

    @Override
    public void setCancelled(boolean value)
    {
    }

    @Override
    public void setTaskName(String name)
    {
        JsonObject load = new JsonObject();
        load.put("op", "setTaskName");
        load.put("value", name);
        this.log(load.toString());
    }

    @Override
    public void subTask(String name)
    {
        JsonObject load = new JsonObject();
        load.put("op", "subTask");
        load.put("value", name);
        this.log(load.toString());
    }

    @Override
    public void logMessage(LogRecord logRecord)
    {
        JsonObject load = new JsonObject();
        load.put("op", "logMessage");
        load.put("value", logRecord.getMessage());
        this.log(load.toString());
    }

    @Override
    public void worked(int work)
    {
        JsonObject load = new JsonObject();
        load.put("op", "worked");
        load.put("value", work);
        this.log(load.toString());
    }
}
