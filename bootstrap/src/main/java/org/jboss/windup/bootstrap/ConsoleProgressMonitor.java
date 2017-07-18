package org.jboss.windup.bootstrap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.jboss.windup.exec.WindupProgressMonitor;

/**
 * Implements basic progress monitoring behavior for the Windup command.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ConsoleProgressMonitor implements WindupProgressMonitor
{
    private static final Logger LOG = Logger.getLogger(ConsoleProgressMonitor.class.getName());

    private int totalWork;
    private int currentWork;
    private boolean cancelled;

    @Override
    public void beginTask(String name, int totalWork)
    {
        this.totalWork = totalWork;

        String message = String.format("%s [%d/%d] %s", getCachedTime(), currentWork, totalWork, name);
        System.out.println(message);
        LOG.info(message);
    }

    @Override
    public void done()
    {
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    @Override
    public void setTaskName(String name)
    {
        String message = String.format("%s [%d/%d] \t", getCachedTime(), currentWork, totalWork, name);
        System.out.println(message);
        LOG.info(message);
    }

    @Override
    public void subTask(String subTask)
    {
        String message = String.format("%s [%d/%d] %s", getCachedTime(), currentWork, totalWork, subTask);
        if (subTask.endsWith("\r"))
        {
            System.out.print(message);
        } 
        else 
        {
            System.out.println("\r" + message);
        }
        LOG.info(message);
    }

    @Override
    public void worked(int work)
    {
        this.currentWork += work;
    }
    
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
    private static long lastFormatted = 0;
    private static String lastFormattedString = "";
    
    private static String getCachedTime() {
        long now = System.currentTimeMillis();
        if (now > lastFormatted + 60_000) {
            Date date = new Date(now);
            String format;
            // SimpleDateFormat is not thread safe.
            synchronized (DATE_FORMATTER) {
                format = DATE_FORMATTER.format(date);
                lastFormatted = now;
                lastFormattedString = format;
            }
        }
        return lastFormattedString;
    }
}
