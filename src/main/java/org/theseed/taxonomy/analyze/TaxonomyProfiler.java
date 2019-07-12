/**
 *
 */
package org.theseed.taxonomy.analyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.theseed.taxonomy.TreeOfLife;

/**
 * This class computes the taxonomic profile of one or more groups keyed by string ID.
 * For each such group, it uses a TaxonomyCounter to determine the number of entries
 * for every taxonomic grouping at each major taxonomy rank.
 *
 * @author Bruce Parrello
 *
 */
public class TaxonomyProfiler {

    // FIELDS
    /** map of group ID to taxonomy counter */
    private HashMap<String, TaxonomyCounter> groupMap;
    /** taxonomic tree of interest */
    private TreeOfLife tree;

    /**
     * Initialize a blank, empty profiler.
     */
    public TaxonomyProfiler(TreeOfLife taxTree) {
        this.groupMap = new HashMap<String, TaxonomyCounter>();
        this.tree = taxTree;
    }

    /**
     * Count a taxonomic grouping for a particular group ID.
     */
    public void register(String key, int taxId) {
        TaxonomyCounter groupCounter;
        if (! groupMap.containsKey(key)) {
            groupCounter = new TaxonomyCounter(key, this.tree);
            this.groupMap.put(key, groupCounter);
        } else {
            groupCounter = this.groupMap.get(key);
        }
        groupCounter.register(taxId);
    }

    /**
     * @return the taxonomic profile for the specified group
     *
     * @param key	ID of the relevant group
     */
    public TaxonomyCounter profileOf(String key) {
        return this.groupMap.get(key);
    }

    /**
     * @return a sorted list of all profiles, from most frequent to least frequent
     */
    public List<TaxonomyCounter> sortedProfiles() {
        ArrayList<TaxonomyCounter> retVal = new ArrayList<TaxonomyCounter>(this.groupMap.values());
        retVal.sort(null);
        return retVal;
    }

}
