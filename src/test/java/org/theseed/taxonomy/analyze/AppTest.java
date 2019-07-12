package org.theseed.taxonomy.analyze;

import junit.framework.Test;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.theseed.counters.CountMap;
import org.theseed.taxonomy.Rank;
import org.theseed.taxonomy.TreeOfLife;
import org.theseed.taxonomy.TreeOfLife.Taxon;


/**
 * Unit test for simple App.
 */
public class AppTest
    extends TestCase
{

    /**
     * Create the test case
     *
     * @param testName name of the test case
     * @throws IOException
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }


    /**
     * Test the rank enum
     */
    public void testRank() {
        assertThat("Domain fails", Rank.DOMAIN.toString(), equalTo("domain"));
        assertThat("Genus fails", Rank.GENUS.toString(), equalTo("genus"));
        assertThat("Species fails", Rank.SPECIES.toString(), equalTo("species"));
        assertThat("No-rank fails", Rank.OTHER.toString(), equalTo(""));
        assertThat("Domain not translated", Rank.rankOf("domain"), equalTo(Rank.DOMAIN));
        assertThat("Genus not translated", Rank.rankOf("genus"), equalTo(Rank.GENUS));
        assertThat("Species not translated", Rank.rankOf("species"), equalTo(Rank.SPECIES));
        assertThat("No-rank not translated", Rank.rankOf("no rank"), equalTo(Rank.OTHER));
    }

    /**
     * Test the tree of life.
     *
     * @throws IOException
     */
    public void testTree() throws IOException {
        TreeOfLife mainTree = new TreeOfLife(5000);
        Taxon root = mainTree.findOrInsert(1, 1, "no rank", "root", true);
        assertThat("Root has a parent.", root.getParent(), equalTo(null));
        assertThat("Root has wrong name.", root.getName(), equalTo("root"));
        assertThat("Root has wrong ID.", root.getId(), equalTo(1));
        assertSame("Root not found in map.", root, mainTree.find(1));
        assertTrue("Root is not hidden.", root.isHidden());
        Taxon proteo = mainTree.findOrInsert(1224, 2, "phylum", "Proteobacteria", false);
        Taxon bacteria = proteo.getParent();
        assertThat("Proteo has no parent", bacteria, notNullValue());
        assertThat("Proteo has wrong parent.", bacteria.getId(), equalTo(2));
        assertSame("Proteo parent not in map.", mainTree.find(2), bacteria);
        assertThat("Proteo has wrong name.", proteo.getName(), equalTo("Proteobacteria"));
        assertThat("Proteo has wrong ID.", proteo.getId(), equalTo(1224));
        assertSame("Proteo not found in map.", proteo, mainTree.find(1224));
        assertFalse("Proteo is hidden.", proteo.isHidden());
        assertThat("Proteo is not a phylum.", proteo.getRank(), equalTo(Rank.PHYLUM));
        Taxon bacteria2 = mainTree.findOrInsert(2, 1, "superkingdom", "Bacteria", false);
        assertSame("Bacteria not reused.", bacteria, bacteria2);
        assertSame("Bacteria is not under root.", root, bacteria.getParent());
        assertThat("Bacteria has wrong name.", bacteria.getName(), equalTo("Bacteria"));
        assertFalse("Bacteria is hidden.", bacteria.isHidden());
        assertThat("Bacteria not domain.", bacteria.getRank(), equalTo(Rank.DOMAIN));
        assertSame("Proteo has moved to wrong parent.", proteo.getParent(), bacteria);
        mainTree = TreeOfLife.load(new File("src/test", "taxonSmall.txt"));
        Taxon marinus = mainTree.find(1219);
        assertThat("Marinus not found.", marinus.getName(), equalTo("Prochlorococcus marinus"));
        assertThat("Marinus wrong rank.", marinus.getRank(), equalTo(Rank.SPECIES));
        assertTrue("Marinus not hidden.", marinus.isHidden());
        assertEquals("Marinus wrong parent.", 1218, marinus.getParent().getId());
        assertSame("Marinus parent duplicated.", mainTree.find(1218), marinus.getParent());
        proteo = mainTree.find(1224);
        assertThat("Proteo has wrong name.", proteo.getName(), equalTo("Proteobacteria"));
        assertThat("Proteo has wrong ID.", proteo.getId(), equalTo(1224));
        assertFalse("Proteo is hidden.", proteo.isHidden());
        assertThat("Proteo is not a phylum.", proteo.getRank(), equalTo(Rank.PHYLUM));
        assertNull("Found taxon not in input.", mainTree.find(83333));
    }

    public void testTaxonomyCounter() throws IOException {
        TreeOfLife ncbi = TreeOfLife.load(new File("src/test", "taxonMedium.dtx"));
        TaxonomyCounter newCounter = new TaxonomyCounter("test", ncbi);
        assertThat("Wrong counter name", newCounter.getName(), equalTo("test"));
        newCounter.register(1129793);
        assertThat("species off after first count", newCounter.typesOf(Rank.SPECIES), equalTo(1));
        assertThat("genus off after first count", newCounter.typesOf(Rank.GENUS), equalTo(1));
        assertThat("family off after first count", newCounter.typesOf(Rank.FAMILY), equalTo(1));
        assertThat("order off after first count", newCounter.typesOf(Rank.ORDER), equalTo(1));
        Taxon order135622 = ncbi.find(135622);
        Taxon order91347 = ncbi.find(91347);
        assertThat("Target order not counted after first count", newCounter.countOf(order135622), equalTo(1));
        assertThat("Wrong order counted after first count", newCounter.countOf(order91347), equalTo(0));
        Taxon species222814 = ncbi.find(222814);
        Taxon species1420916 = ncbi.find(1420916);
        assertThat("Target species not counted after first count", newCounter.countOf(species222814), equalTo(1));
        newCounter.register(1420916);
        assertThat("Second species not counted after second count", newCounter.countOf(species1420916), equalTo(1));
        assertThat("Old species changed after first count", newCounter.countOf(species222814), equalTo(1));
        assertThat("species off after second count", newCounter.typesOf(Rank.SPECIES), equalTo(2));
        assertThat("family off after second count", newCounter.typesOf(Rank.FAMILY), equalTo(1));
        Taxon family72275 = ncbi.find(72275);
        assertThat("Family count incorrect after second count.", newCounter.countOf(family72275), equalTo(2));
        assertThat("Invalid count wrong.", newCounter.getInvalid(), equalTo(0));
        newCounter.register(1420916);
        newCounter.register(119174);
        List<CountMap<Taxon>.Count> familyCounts = newCounter.sortedCounts(Rank.FAMILY);
        assertThat("Incorrect big family count ID.", familyCounts.get(0).getKey().getId(), equalTo(72275));
        assertThat("Incorrect big family count value.", familyCounts.get(0).getCount(), equalTo(3));
        assertThat("Incorrect small family count ID.", familyCounts.get(1).getKey().getId(), equalTo(4210));
        assertThat("Incorrect small family count value.", familyCounts.get(1).getCount(), equalTo(1));
        newCounter.register(1420916);
        newCounter.register(119174);
        assertThat("Invalid count with all valid IDs.", newCounter.getInvalid(), equalTo(0));
        newCounter.register(666666);
        assertThat("Invalid count not registering.", newCounter.getInvalid(), equalTo(1));
        assertThat("Total count wrong.", newCounter.getTotal(), equalTo(6));
    }

    /**
     * Test the taxonomy profiler
     * @throws IOException
     */
    public void testProfiler() throws IOException {
        TreeOfLife ncbi = TreeOfLife.load(new File("src/test", "taxonMedium.dtx"));
        TaxonomyProfiler newProfiler = new TaxonomyProfiler(ncbi);
        newProfiler.register("group4A", 107806);
        newProfiler.register("group4A", 1129793);
        newProfiler.register("group4A", 1420916);
        newProfiler.register("group4A", 119174);
        newProfiler.register("group4B", 107806);
        newProfiler.register("group4B", 1129793);
        newProfiler.register("group4B", 1420916);
        newProfiler.register("group4B", 119174);
        newProfiler.register("group6", 107806);
        newProfiler.register("group6", 1129793);
        newProfiler.register("group6", 1420916);
        newProfiler.register("group6", 119174);
        newProfiler.register("group6", 107806);
        newProfiler.register("group6", 1129793);
        newProfiler.register("group3", 107806);
        newProfiler.register("group3", 1129793);
        newProfiler.register("group3", 1420916);
        List<TaxonomyCounter> profiles = newProfiler.sortedProfiles();
        int prev = Integer.MAX_VALUE;
        for (TaxonomyCounter profile : profiles) {
            assertThat("Profile " + profile.getName() + " is out of order.",
                    profile.getTotal(), lessThanOrEqualTo(prev));
        }
        TaxonomyCounter profile1 = profiles.get(1);
        assertThat("Profile 4A in wrong position.", profile1.getName(), equalTo("group4A"));
        assertThat("Profile 4A has wrong total.", profile1.getTotal(), equalTo(4));
        Taxon family72275 = ncbi.find(72275);
        assertThat("Profile 4A has wrong family count.", profile1.countOf(family72275), equalTo(2));
    }
}
