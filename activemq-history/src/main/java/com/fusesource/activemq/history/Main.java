package com.fusesource.activemq.history;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  public static final String DEFAULT_SCRIPT_NAME = "activemq-history";
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  
  public static final String AMQ = "amq";
  public static final String KAHADB = "kahadb";
  
  public static final File storeDirectory = new File(TargetBroker.BROKERNAME);

  public static final String DATEFORMAT = "yyyyMMddHHmm";

  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    String journalDir;
    String queue;
    String fromDate;
    String toDate;
    String archiveDir;
    int maxFileLength = 32;
    String targetStoreType;
    String storeType;
    ArrayList<File> dirs = new ArrayList<File>();
    DateFormat dateFormatter = new SimpleDateFormat(DATEFORMAT);

    try {
      CommandLine cli = new PosixParser().parse(options, args);

      if (cli.hasOption("help")) {
        printUsageAndExit();
      }

      journalDir = cli.getOptionValue('j');
      if (journalDir == null) { 
        printUsageAndExit("You must specify a value for journalDir with the -j option.");
      }
      
      archiveDir = cli.getOptionValue('a');
      
      dirs.add(new File(journalDir));
      if (archiveDir != null) {
        dirs.add(new File(archiveDir));
      }
      
      queue = cli.getOptionValue('q');
      fromDate = cli.getOptionValue('f');
      toDate = cli.getOptionValue('t');
      
      Date from = null;  
      if (fromDate != null) {
          from = dateFormatter.parse(fromDate);
      }
      
      Date to = null; 
      if (toDate != null) { 
          to = dateFormatter.parse(toDate);
      }

      
      MessageHistory messageHistory = new KahaDBMessageHistory();
      messageHistory.setJournalDirs(dirs);
      messageHistory.setQueue(queue);
      messageHistory.setFrom(from == null ? 0l : from.getTime());
      messageHistory.setTo(to == null ? 0l : to.getTime());
      messageHistory.setScanReporter(new ConsoleScanReporter());

      
      
      messageHistory.run();

    } catch (ParseException ex) {
      printUsageAndExit();
    } catch (java.lang.Exception ex) {
      System.err.println("Unexpected error; details: " + ex.getMessage());
    }

    System.out.printf("Elapsed time %s.",
            DurationFormatUtils.formatDuration(
            System.currentTimeMillis() - start, "HH:mm:ss:SSS"));
  }

  private static void printUsageAndExit(String message) {
    if (message != null) {
      System.out.println(message);
    }

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(DEFAULT_SCRIPT_NAME, options);

    System.exit(0);
  }

  private static void printUsageAndExit() {
    printUsageAndExit(null);
  }
  
    private static final Options options = new Options();

    static {
        Option journalDir = OptionBuilder.withArgName("dir").
                hasArg().
                withDescription("The directory containing the broker's AMQ Journal").withLongOpt("journalDir").create("j");
        
        Option queue = OptionBuilder.withArgName("queue").
                hasArg().
                withDescription("The queue to browse").withLongOpt("queue").create("q");

        Option fromDate = OptionBuilder.withArgName("date (" + DATEFORMAT + ")").
                hasArg().
                withDescription("Match from specified date").withLongOpt("from").create("f");

        Option toDate = OptionBuilder.withArgName("date (" + DATEFORMAT + ")").
                hasArg().
                withDescription("Match to specified date").withLongOpt("to").create("t");

        Option archiveDir = OptionBuilder.withArgName("dir").
                hasArg().
                withDescription("The directory containing the broker's AMQ Journal archives.").withLongOpt("archiveDir").create("a");

        Option help = OptionBuilder.withDescription("Prints this message.").withLongOpt("help").create("h");

        options.addOption(journalDir);
        options.addOption(archiveDir);
        options.addOption(queue);
        options.addOption(fromDate);
        options.addOption(toDate);
        options.addOption(help);

    }  
    
    
}
