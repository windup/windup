package org.jboss.windup.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.windup.WindupEngine;
import org.jboss.windup.WindupEnvironment;
import org.jboss.windup.WindupService;
import org.jboss.windup.reporting.ReportEngine;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class WindupServiceImpl implements WindupService
{
   @Override
   public void execute(String[] args)
   {
      if (args != null && args.length > 0)
         process(args);
   }

   private static final Logger LOG = LoggerFactory.getLogger(WindupServiceImpl.class);
   private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();
   private static final String WINDUP_COMMAND = "java -jar jboss-windup.jar";

   @SuppressWarnings("static-access")
   public void process(String[] args)
   {
      CommandLineParser parser = new PosixParser();

      Options options = new Options();
      Option input = OptionBuilder.withArgName("file / dir").hasArg().isRequired()
               .withDescription("file to generate windup report (required)").create("input");
      Option output = OptionBuilder.withArgName("dir").hasArg()
               .withDescription("directory where to generate windup report (required)").create("output");
      Option javaPkgs = OptionBuilder.withArgName("string").hasArg()
               .withDescription("client Java packages to target for inspection").create("javaPkgs");
      Option excludePkgs = OptionBuilder.withArgName("string").hasArg()
               .withDescription("client Java packages to target for inspection").create("excludePkgs");
      Option logLevel = OptionBuilder.withArgName("string").hasArg()
               .withDescription("log level for root logger (defaults to info)").create("logLevel");
      Option captureLog = OptionBuilder.withArgName("boolean").hasArg().withDescription("persist to file")
               .create("captureLog");
      Option fetchRemote = OptionBuilder.withArgName("boolean").hasArg()
               .withDescription("fetch version information from remote repository when not found")
               .create("fetchRemote");
      Option targetPlatform = OptionBuilder.withArgName("string").hasArg().withDescription("target platform")
               .create("targetPlatform");
      Option isSource = OptionBuilder.withArgName("boolean").hasArg().withDescription("is running on source")
               .create("source");
      options.addOption(input);
      options.addOption(output);
      options.addOption(javaPkgs);
      options.addOption(logLevel);
      options.addOption(captureLog);
      options.addOption(fetchRemote);
      options.addOption(targetPlatform);
      options.addOption(excludePkgs);
      options.addOption(isSource);

      CommandLine line = null;
      try
      {
         line = parser.parse(options, args, true);
         processInput(line, options);
      }
      catch (ParseException exp)
      {
         LOG.error("Unexpected exception: " + exp.getMessage(), exp);
         HELP_FORMATTER.printHelp(WINDUP_COMMAND, options);
      }
   }

   private void processInput(CommandLine line, Options options)
   {
      try
      {
         if (line.getOptions().length < 1)
         {
            HELP_FORMATTER.printHelp(WINDUP_COMMAND, options);
         }
         else
         {
            // Map the environment settings from the input arguments.
            WindupEnvironment settings = new WindupEnvironment();
            if (line.hasOption("javaPkgs"))
            {
               settings.setPackageSignature(line.getOptionValue("javaPkgs"));
            }
            if (line.hasOption("excludePkgs"))
            {
               settings.setExcludeSignature(line.getOptionValue("excludePkgs"));
            }

            if (line.hasOption("targetPlatform"))
            {
               settings.setTargetPlatform(line.getOptionValue("targetPlatform"));
            }

            if (line.hasOption("fetchRemote"))
            {
               settings.setFetchRemote(line.getOptionValue("fetchRemote"));
            }

            String inputLocation = line.getOptionValue("input");
            inputLocation = StringUtils.trim(inputLocation);
            File inputPath = new File(inputLocation);

            File outputPath = null;
            String outputLocation = line.getOptionValue("output");
            outputLocation = StringUtils.trim(outputLocation);
            if (StringUtils.isNotBlank(outputLocation))
            {
               outputPath = new File(outputLocation);
            }

            boolean isSource = false;
            if (BooleanUtils.toBoolean(line.getOptionValue("source")))
            {
               isSource = true;
            }
            settings.setSource(isSource);

            boolean captureLog = false;
            if (BooleanUtils.toBoolean(line.getOptionValue("captureLog")))
            {
               captureLog = true;
            }

            String logLevel = line.getOptionValue("logLevel");
            logLevel = StringUtils.trim(logLevel);

            settings.setCaptureLog(captureLog);
            settings.setLogLevel(logLevel);

            // Run Windup.
            ReportEngine engine = new ReportEngine(settings);
            engine.process(inputPath, outputPath);
         }
      }
      catch (FileNotFoundException e)
      {
         LOG.error("Input does not exist:" + e.getMessage(), e);
         HELP_FORMATTER.printHelp(WINDUP_COMMAND, options);
      }
      catch (IOException e)
      {
         LOG.error("Exception while writing report: " + e.getMessage(), e);
         HELP_FORMATTER.printHelp(WINDUP_COMMAND, options);
      }
   }

}
