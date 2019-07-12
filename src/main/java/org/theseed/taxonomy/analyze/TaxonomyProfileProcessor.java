/**
 *
 */
package org.theseed.taxonomy.analyze;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.theseed.io.TabbedLineReader;
import org.theseed.taxonomy.Rank;
import org.theseed.taxonomy.TreeOfLife;

/**
 * This is the main execution class for taxonomy analysis.  It reads through a file of genomes with
 * taxonomic information and builds a profile based on a group ID.
 *
 * The positional parameter is the name of the load file for the taxonomy tree.  The standard input should
 * contain the genomes in the groups to be profiled.  It must be tab-delimited, with one genome per line,
 * and a header line.  The standard output will contain a formatted report.
 *
 * The following command-line options are supported.
 *
 * -c	the index (1-based) or name of the input column containing the group ID; the default is "group_id"
 * -t	the index (1-based) or name of the input column containing the taxonomy ID; the default is "taxon_id"
 * -v	display progress on STDERR
 *
 *
 * @author Bruce Parrello
 *
 */
public class TaxonomyProfileProcessor {

    // FIELDS
    /** main taxonomy tree */
    TreeOfLife taxTree;
    /** main taxonomy profiler */
    TaxonomyProfiler profiler;

    // COMMAND LINE

    /** help option */
    @Option(name="-h", aliases={"--help"}, help=true)
    private boolean help;

    /** group ID column spec */
    @Option(name="-c", aliases={"--col", "--groupCol"}, metaVar="group_id", usage="column containing group ID")
    String groupCol;

    /** tax ID column spec */
    @Option(name="-t", aliases={"--taxCol"}, metaVar="taxon_id", usage="column containing taxonomy ID")
    String taxCol;

    /** tracing display */
    @Option(name="-v", aliases={"--debug", "verbose"}, usage="display progress messages on STDERR")
    boolean debug;

    /** tree of life input file */
    @Argument(index=0, metaVar="taxonomy_file.dtx", usage="file containing full taxonomy tree", required=true)
    File taxFile;

    /** Parse the command line parameters and options. */
    public boolean parseCommand(String[] args) {
        boolean retVal = false;
        // Set the defaults.
        this.help = false;
        this.groupCol = "group_id";
        this.taxCol = "taxon_id";
        this.debug = false;
        this.taxFile = null;
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
            if (this.help) {
                parser.printUsage(System.err);
            } else {
                // Read in the taxonomy file.
                if (debug) System.err.println("Reading taxonomy tree from " + this.taxFile + ".");
                long start = System.currentTimeMillis();
                this.taxTree = TreeOfLife.load(this.taxFile);
                long duration = (System.currentTimeMillis() - start) / 1000;
                if (debug) System.err.println("Taxonomy tree read in " + duration + " seconds.");
                retVal = true;
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return retVal;
    }

    public void run() {
        try {
            // Create the profiler.
            this.profiler = new TaxonomyProfiler(this.taxTree);
            // Open the input file and find the columns.
            TabbedLineReader reader = new TabbedLineReader(System.in);
            int groupColIdx = reader.findField(this.groupCol);
            int taxColIdx = reader.findField(this.taxCol);
            // Loop through the input.
            for (TabbedLineReader.Line line : reader) {
                // Get the group ID and the taxon ID from this line.
                int taxId = line.getInt(taxColIdx);
                String groupId = line.get(groupColIdx);
                profiler.register(groupId, taxId);
                if (debug && reader.linesRead() % 5000 == 0) {
                    System.err.println(reader.linesRead() + " records processed.");
                }
            }
            reader.close();
            if (debug)
                System.err.println(reader.linesRead() + " total lines processed.");
            // Now we create the output report. To start, we only do family, genus, and species.
            System.out.println("group_id\tmembers\tfamilies\tgenera\tspecies\tinvalid");
            List<TaxonomyCounter> profiles = this.profiler.sortedProfiles();
            for (TaxonomyCounter counter : profiles) {
                System.out.format("%s\t%d\t%d\t%d\t%d\t%d%n", counter.getName(), counter.getTotal(),
                        counter.typesOf(Rank.FAMILY), counter.typesOf(Rank.GENUS),
                        counter.typesOf(Rank.SPECIES), counter.getInvalid());
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

}