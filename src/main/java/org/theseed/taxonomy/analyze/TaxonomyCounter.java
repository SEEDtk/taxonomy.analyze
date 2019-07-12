/**
 *
 */
package org.theseed.taxonomy.analyze;

import java.util.List;

import org.theseed.counters.CountMap;
import org.theseed.taxonomy.Rank;
import org.theseed.taxonomy.TreeOfLife;
import org.theseed.taxonomy.TreeOfLife.Taxon;

/**
 * This object counts the number of distinct taxonomic groupings found at each rank.  The client presents
 * it with taxonomic IDs, and these are stored in the object.  At any time, the number of distinct values
 * found at each ranking level can be interrogated.
 *
 * @author Bruce Parrello
 *
 */
public class TaxonomyCounter implements Comparable<TaxonomyCounter> {

    // FIELDS
    /** array of bags, one per rank, containing the taxonomies found at each rank and how many
     * times each one was found */
    private CountMap<Taxon> counters[];
    /** taxonomic tree for this counter */
    private TreeOfLife mainTree;
    /** number of invalid group registrations presented */
    private int invalidCount;
    /** total count of group registrations presented */
    private int totalCount;
    /** name of this counter */
    private String name;

    /**
     * Construct an empty taxonomic counter for a single group.
     *
     * @param tree	taxonomic tree relevant to this counter
     */
    @SuppressWarnings("unchecked")
    public TaxonomyCounter(String name, TreeOfLife tree) {
        this.counters = (CountMap<Taxon>[]) new CountMap[Rank.nGood()];
        for (int i = 0; i < this.counters.length; i++) {
            this.counters[i] = new CountMap<Taxon>();
        }
        this.name = name;
        this.mainTree = tree;
        this.invalidCount = 0;
        this.totalCount = 0;
    }

    /**
     * Register the occurrence of a member of a particular taxonomic grouping.
     *
     * @param taxId		taxonomic ID of the low-level grouping whose member was encountered
     */
    public void register(int taxId) {
        // Get the taxon object for this grouping.
        Taxon found = mainTree.find(taxId);
        // We should always find one, but there are a few glitches always.
        if (found == null) {
            this.invalidCount++;
        } else {
            // Loop until we fall off the tree.
            while (found != null) {
                // Count this group.
                if (found.getRank() != Rank.OTHER) {
                    this.counters[found.getRank().getIdx()].count(found);
                }
                // Percolate to the parent.
                found = found.getParent();
            }
            // Add to the total count.
            this.totalCount++;
        }
    }

    /**
     * @return the number of distinct groups at the specified rank level.
     *
     * @param rnk	rank level of interest
     */
    public int typesOf(Rank rnk) {
        return this.counters[rnk.getIdx()].size();
    }

    /**
     * @return the number of times the specified taxonomic group (or one of its descendants) occurred
     * 		   in this counter.
     *
     * @param taxon		taxonomic grouping of interest
     */
    public int countOf(Taxon taxon) {
        Rank rnk = taxon.getRank();
        return this.counters[rnk.getIdx()].getCount(taxon);
    }

    /**
     * @return the number of invalid groups encountered
     */
    public int getInvalid() {
        return this.invalidCount;
    }

    /**
     * @return the total number of registrations
     */
    public int getTotal() {
        return this.totalCount;
    }

    /**
     * @return the individual counts for the specified rank, sorted from most frequent to least
     * 		   frequent
     *
     * @param rnk	rank of interest
     */
    public List<CountMap<Taxon>.Count> sortedCounts(Rank rnk) {
        return this.counters[rnk.getIdx()].sortedCounts();
    }

    /**
     * @return the name of this counter
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sort by highest total count, then lowest invalid count, then name.
     */
    @Override
    public int compareTo(TaxonomyCounter o) {
        int retVal = o.totalCount - this.totalCount;
        if (retVal == 0) {
            retVal = this.invalidCount - o.invalidCount;
            if (retVal == 0) {
                retVal = this.name.compareTo(o.name);
            }
        }
        return retVal;
    }

}
