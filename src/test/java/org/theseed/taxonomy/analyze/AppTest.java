package org.theseed.taxonomy.analyze;

import junit.framework.Test;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;


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
    public void testRank()
    {
        assertThat("Domain fails", Rank.DOMAIN.toString(), equalTo("domain"));
        assertThat("Genus fails", Rank.GENUS.toString(), equalTo("genus"));
        assertThat("Species fails", Rank.SPECIES.toString(), equalTo("species"));
        assertThat("No-rank fails", Rank.OTHER.toString(), equalTo(""));
        assertThat("Domain not translated", Rank.rankOf("domain"), equalTo(Rank.DOMAIN));
        assertThat("Genus not translated", Rank.rankOf("genus"), equalTo(Rank.GENUS));
        assertThat("Species not translated", Rank.rankOf("species"), equalTo(Rank.SPECIES));
        assertThat("No-rank not translated", Rank.rankOf("no rank"), equalTo(Rank.OTHER));

    }
}
