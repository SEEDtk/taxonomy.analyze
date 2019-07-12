package org.theseed.taxonomy.analyze;


/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        TaxonomyProfileProcessor runObject = new TaxonomyProfileProcessor();
        boolean ok = runObject.parseCommand(args);
        if (ok) {
            runObject.run();
        }
    }
}
