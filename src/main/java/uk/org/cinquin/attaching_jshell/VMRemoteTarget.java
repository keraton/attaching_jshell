package uk.org.cinquin.attaching_jshell;


/**
 * This class is for testing purpose only, the idea is to show that we can put a break point inside the method theGoodsForTesting
 * And when this method is called by using Jshell, the debug will stop at the break point.
 */
public class VMRemoteTarget {


    private static int counter = 0;

    public static void theGoodsForTesting() {
        System.out.println("ARE HERE " + ++counter); // Put break point in here !!
    }

}
