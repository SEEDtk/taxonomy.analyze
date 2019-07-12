package org.theseed.taxonomy;

import java.util.HashMap;

/**
 * This enumeration describes the major taxonomic ranks.  All the minor ranks are assigned OTHER.
 *
 * @author Bruce Parrello
 *
 */
public enum Rank {
    DOMAIN("domain"), KINGDOM("kingdom"), PHYLUM("phylum"), CLASS("class"), ORDER("order"),
    FAMILY("family"), GENUS("genus"), SPECIES("species"), OTHER("");

    /** basic array of all ranks */
    private static final Rank[] values = values();

    /** label of rank */
    private String label;

    /** conversion table for labels to rank objects (built on first use) */
    private static HashMap<String, Rank> labelMap = null;

    private Rank(String label) {
        this.label = label;
    }

    /**
     * @return the idx
     */
    public int getIdx() {
        return this.ordinal();
    }

    /**
     * @return the label
     */
    public String toString() {
        return label;
    }

    /**
     * @return the rank corresponding to a label
     *
     * @param label	label whose rank object is desired
     */
    public static Rank rankOf(String label) {
        if (labelMap == null) {
            labelMap = new HashMap<String, Rank>();
            for (Rank rnk : values) {
                labelMap.put(rnk.label, rnk);
            }
            // Domain is called superkingdom by NCBI.
            labelMap.put("superkingdom", Rank.DOMAIN);
        }
        Rank retVal = labelMap.getOrDefault(label, Rank.OTHER);
        return retVal;
    }

    /**
     * @return the number of good ranks
     */
    public static int nGood() {
        return Rank.values.length - 1;
    }


}
