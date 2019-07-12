/**
 *
 */
package org.theseed.taxonomy;

import java.util.Scanner;

import org.clapper.util.misc.SparseArrayList;

import java.io.File;
import java.io.IOException;

/**
 * This class represents the full taxonomy tree. It is loaded from the dtx load file for the Shrub.
 * Every taxonomic grouping is in here, along with its rank and other information.  Access via ID
 * is provided, along with the ability to traverse the tree upward.
 *
 * @author Bruce Parrello
 *
 */
public class TreeOfLife {

    // FIELDS
    /** set of all taxons; we currently do this with a sparse array list, since the taxonomy numbers
     * are very dense (82%) */
    private SparseArrayList<Taxon> taxMap;

    /**
     * This class represents a single taxonomic grouping in the taxonomy tree.
     */
    public class Taxon implements Comparable<Taxon> {

        // FIELDS
        /** taxonomic ID */
        private int id;
        /** taxonomic rank */
        private Rank rank;
        /** scientific name */
        private String name;
        /** parent taxon */
        private Taxon parent;
        /** TRUE if this is a hidden group */
        private boolean hidden;

        /**
         * Construct a blank taxon from its ID.
         */
        private Taxon(int taxId) {
            storeTaxId(taxId);
            this.rank = Rank.OTHER;
            this.name = "<unknown>";
            this.parent = null;
            this.hidden = true;
        }

        private void storeTaxId(int taxId) {
            this.id = taxId;
            taxMap.set(taxId, this);
        }

        @Override
        public int compareTo(Taxon arg0) {
            return this.id - arg0.id;
        }

        /**
         * @return the ID of this taxonomic grouping
         */
        public int getId() {
            return this.id;
        }

        /**
         * @return the rank of this taxonomic grouping
         */
        public Rank getRank() {
            return this.rank;
        }

        /**
         * @return the name of this taxonomic grouping
         */
        public String getName() {
            return this.name;
        }

        /**
         * @return the parent taxonomic grouping
         */
        public Taxon getParent() {
            return this.parent;
        }

        /**
         * @return TRUE if this taxonomic grouping is hidden in taxonomy strings
         */
        public boolean isHidden() {
            return this.hidden;
        }


        /**
         * Store the data about this taxonomic grouping.
         *
         * @param newParent		ID of the parent group
         * @param newRank		label for this group's rank
         * @param newName		name of this group
         * @param newHiddenFlag	TRUE if this group should be hidden in taxonomy lists
         */
        private void storeTaxData(int newParent, String newRank, String newName, boolean newHiddenFlag) {
            // First find the parent.
            if (newParent == this.id) {
                this.parent = null;
            } else {
                Taxon parentO = find(newParent);
                if (parentO == null) {
                    parentO = new Taxon(newParent);
                }
                this.parent = parentO;
            }
            this.name = newName;
            this.rank = Rank.rankOf(newRank);
            this.hidden = newHiddenFlag;
        }

        @Override
        public int hashCode() {
            return this.id;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Taxon other = (Taxon) obj;
            if (this.id != other.id)
                return false;
            return true;
        }

        @Override
        public String toString() {
            return this.id + " " + this.name;
        }

    }

    /**
     * Create a new, empty tree of life.
     *
     * @param capacity	number of slots to allocate for the taxonomic groupings
     */
    public TreeOfLife(int capacity) {
        // Create the main hash.
        this.taxMap = new SparseArrayList<Taxon>(capacity);
    }

    /**
     * @return the taxonomic grouping with the specified ID, or NULL if none exists
     *
     * @param taxId		ID of the group desired
     */
    public Taxon find(int taxId) {
        Taxon retVal = null;
        if (taxId < this.taxMap.size()) {
            retVal = this.taxMap.get(taxId);
        }
        return retVal;
    }

    /**
     * Insert a new taxonomic grouping in the tree and return it.  If the grouping already exists,
     * its data will be overwritten.
     *
     * @param newTaxId		ID of this group
     * @param newParent		ID of the parent group
     * @param newRank		label for this group's rank
     * @param newName		name of this group
     * @param newHiddenFlag	TRUE if this group should be hidden in taxonomy lists
     */
    public Taxon findOrInsert(int taxId, int newParent, String newRank, String newName, boolean newHiddenFlag) {
        // Get the appropriate taxon object.
        Taxon retVal = find(taxId);
        if (retVal == null) {
            retVal = new Taxon(taxId);
        }
        retVal.storeTaxData(newParent, newRank, newName, newHiddenFlag);
        return retVal;
    }

    /**
     * Load the tree of life from a database load file.  Such files have six tab-delimited fields:  (0) ID,
     * (1) parent ID, (2) domain flag [ignored], (3) hidden flag, (5) rank, (6) name.
     *
     * @param taxonFile	file from which the taxonomy tree is to be loaded
     *
     * @return	the new tree of life
     * @throws IOException
     */
    public static TreeOfLife load(File taxonFile) throws IOException {
        // Create a blank tree.
        int capacity = (int) taxonFile.length() / 40;
        if (capacity < 100) capacity = 100;
        TreeOfLife retVal = new TreeOfLife(capacity);
        // Set up a scanner to read the tokens in the file.
        Scanner reader = new Scanner(taxonFile);
        reader.useDelimiter("\\t|\\r*\\n");
        while (reader.hasNext()) {
            // Get the next record.  Start with the ID and the parent ID.
            int taxId = reader.nextInt();
            int parentId = reader.nextInt();
            // Skip the domain flag.
            reader.nextInt();
            // Get the hidden flag.
            boolean hiddenFlag = (reader.nextInt() != 0);
            // Get the rank and name.
            String rankLabel = reader.next();
            String taxName = reader.next();
            // Insert the taxonomic group.
            retVal.findOrInsert(taxId, parentId, rankLabel, taxName, hiddenFlag);
        }
        reader.close();
        return retVal;

    }

}
