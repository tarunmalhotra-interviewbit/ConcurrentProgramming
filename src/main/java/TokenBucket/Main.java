package TokenBucket;

class Main {
    public static void main( String args[] ) throws InterruptedException {
        System.out.println("Starting at " + (System.currentTimeMillis() / 1000));
        TokenBucketFilter.runTestMaxTokenIsTen();
    }
}
