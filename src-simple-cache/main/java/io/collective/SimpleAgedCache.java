package io.collective;
import java.time.Clock;


/**
 * SimpleAgedCache collects the keys, values, and retention time in milliseconds for all items to be cached.
 * It contains a clock to timestamp when these items are entered, removed, or retrieved from the cache, and also
 * checks for when the cache times out and/or when it needs to be cleared entirely. This class represents how the
 * cache will handle this data.
 * <p>
 * To check if the tasks being cached are made in time, there is a clock to check if any tasks have expired before
 * they can be added into the cache. If an entry/task is expired, then the cache is to be emptied and any future
 * entries are to be made in a "refreshed" cache with no old tasks. The class ExpirableEntry will be utilized to store,
 * retrieve, and time information being sent, and also check if the cache has timed out.
 * </p><p>
 * Initially, using three separate arrays (two Object arrays key and value, one int array retentionInMillis) was the
 * goal to store all data. However, due to complications with the permanent sizing and/or resizing of arrays, it was
 * later desired that a Queue in the form of a Linked List was more suitable because of the capabilities that nodes
 * provide to store or query any information.
 * </p>
 *
 * ---NOTE---
 * Only the Java side of work has been done for this project; no Kotlin (optional) was used for the tests or for any
 * implementation or programming.
 *
 */
public class SimpleAgedCache {

    public Clock clock; // the clock variable used throughout
    private int putCount; // count variable

    /*
     * Utilize two class object instances of ExpirableEntry listed top and bottom, tasked with storing entries in the
     * form of first and last nodes, respectively.
     */
    ExpirableEntry top;
    ExpirableEntry bottom;


    /***
     * Create a new instance of a SimpleAgedCache while initializing a clock to open the cache.
     *
     * @param clock initiate a new version of the public clock value
     */
    public SimpleAgedCache(Clock clock) {
        this();
        this.clock = clock;
    }

    /**
     * A SimpleAgedCache Constructor setting the clock to a default time zone.
     * Also initializes a count variable to zero (0) and setting null the two ExpirableEntry instances.
     */
    public SimpleAgedCache() {

        this.clock = Clock.systemDefaultZone();
        this.top = this.bottom = null;
        this.putCount = 0;

    }

    /**
     * Retrieve the key, value, and retention in milliseconds being inputted to be added into the cache.
     * Implemented like an enqueue() function generally required for a Queue via nodes for a Linked List.
     * Written with checking for any attribute or entry that is not in the cache or may have been entered
     * after the cache expired through the ExpirableEntry class.
     *
     * @param key the Object key to be inputted into the cache
     * @param value the Object value to be inputted into the cache
     * @param retentionInMillis the Integer retention time (in milliseconds) to be inputted into the cache
     *
     * @throws NullPointerException if any entry component is deemed null
     */
    public void put(Object key, Object value, int retentionInMillis) {

        if (key == null || value == null || retentionInMillis == 0) {
            throw new NullPointerException("No entry or incomplete entry");
        }
        ExpirableEntry tempEE = new ExpirableEntry(key, value, retentionInMillis, clock);
        if (this.top == null) {
            this.top = this.bottom = tempEE;
            putCount++;
        }
        else {
            ExpirableEntry getter = retrieveKey(key);
            if (getter == null) {
                bottom.next = tempEE;
                this.bottom = tempEE;
                putCount++;
            } else {
               /*
                Check if any of the attributes in the Queue node came after time of cache expired
                or not and set the temp ExpirableEntry getter appropriately.
                */
                tempEE.setValue(value);
                tempEE.setOffsetMilliseconds(getter.navigateOffSetMillis());
            }
        }

    }


    /**
     * Determine if a cache is empty or not, using size() to send back a boolean value.
     *
     * @return true if there is a nonzero value of tasks in cache; false otherwise
     */
    public boolean isEmpty() { return (size() == 0);  }


    /**
     * Get the quantity of the number of tasks in the cache. Cache is cleared if it is a nonzero value.
     *
     * @return putCount the number of tasks in the cache
     */
    public int size() {

        clearCache();
        return putCount;

    }

    /**
     * A getter function which takes the key to be searched and returns its corresponding value. Written with
     * checking for any attribute or entry that is not in the cache or may have been entered after the cache
     * expired through the ExpirableEntry class.
     *
     * @param key the Object key required to navigate the corresponding value for the entry
     * @return getValue, the corresponding value Object with respect to the argument key Object; null otherwise
     */
    public Object get(Object key) {

        Object getValue = null;
        ExpirableEntry retrieve = retrieveKey(key);
        if (retrieve != null &&
                retrieve.navigateKey().equals(key) &&
                !retrieve.checkOffsetClock(clock)) {
            getValue = retrieve.navigateValue();
        }
        return getValue;

    }

    /**
     * Clears the cache if there is a nonzero value for putCount as seen in size(). Gets the current iteration of
     * ExpirableEntry to empty the cache. Implemented like a dequeue() function generally required for a Queue via
     * nodes for a Linked List. This will remove ALL entries from the cache/queue, not one-by-one.
     * <p>
     * Utilizes a temporary ExpirableEntry instance getting the top node. </p>
     */
    public void clearCache() {

        ExpirableEntry tempEE = this.top;
        if((putCount == 0) || (this.top == null) || (this.bottom == null)) {
            this.top = null;
            this.bottom = null;
        }
        while(tempEE != null){
            if (tempEE.checkOffsetClock(clock)){
                if(tempEE.equals(top)){
                    this.top = tempEE.next;
                }
                if(tempEE.equals(bottom)){
                    tempEE.next = null;
                }
                putCount--;
            }
            tempEE = tempEE.next;
        }

    }

    /**
     * This method will return the corresponding node in the Queue from ExpirableEntry based on the key Object value
     * that is passed in as an argument.
     *
     * @param key the argument key Object from the ExpirableEntry node to find a matching node
     * @return getter a temporary ExpirableEntry variable that; null otherwise
     */
    private ExpirableEntry retrieveKey(Object key){

        ExpirableEntry getter = this.top;
        while (getter != null &&
                !getter.navigateKey().equals(key) &&
                !getter.equals(this.bottom)) {

//          Set the getter to be the next ExpirableEntry object
            getter = getter.next;
        }
//        Return the node
        if (getter != null && getter.navigateKey().equals(key)) {
            return getter;

        } else return null;
    }
}





/**
 * This class checks if all the tasks and/or entries into the cache are expired. It will take the information from the
 * SimpleAgedCache class to determine if the entry can or cannot be added to the cache. The ExpirableEntry class is
 * written in the form of a Queue and stores the SimpleAgedCache entry details in a node, appropriately initialized in
 * this class as "next". The Queue is implemented most similar to a Linked List with how the info is stored.
 */
class ExpirableEntry {

    public ExpirableEntry next;
    private Object key;
    private Object value;
    int retentionInMillis;
    int offsetMilliseconds;

    /**
     * Constructor initializing the next ExpirableEntry node as null
     */
    public ExpirableEntry() { this.next = null; }

    /**
     * Constructor initializing the next ExpirableEntry entry values in a node to be added to the Queue.
     * The information in this entry will check if the cache's time expired before adding it to the cache.
     *
     * @param key the corresponding key Object from SimpleAgedCache
     * @param value the corresponding value Object from SimpleAgedCache
     * @param retentionInMillis the corresponding retentionInMillis integer (int) value from SimpleAgedCache
     * @param clock the universally used Clock instance clock from SimpleAgedCache
     */
    public ExpirableEntry(Object key, Object value, int retentionInMillis, Clock clock) {

        this();
        this.key = key;
        this.value = value;
        this.retentionInMillis = retentionInMillis;
        this.offsetMilliseconds = (int)(clock.millis() + retentionInMillis);

    }

    /**
     * Gets the key to the corresponding cache entry from the linked list node.
     *
     * @return key the corresponding key Object from the cache entry being searched
     */
    public Object navigateKey() {
        return key;
    }

    /**
     * Gets the value to the corresponding cache entry from the linked list node.
     *
     * @return value the corresponding value Object from the cache entry being searched
     */
    public Object navigateValue() {
        return value;
    }

    /**
     * Sets the value to the corresponding cache entry from the linked list node.
     *
     * @param value the corresponding value Object initialize this class value Object
     */
    public void setValue(Object value){
        this.value=value;
    }

    /**
     * Gets the time after the cache's time expired (the retention/retentionInMillis) to be initialized locally
     * in this class.
     *
     * @return offsetMilliseconds the amount of time after the cache time expired
     */
    public int navigateOffSetMillis(){ return offsetMilliseconds;  }

    /**
     * Sets the time after the cache's time expired (the retention/retentionInMillis) to be initialized locally
     * in this class.
     *
     * @param offsetMilliseconds the amount of time after the cache time expired
     */
    public void setOffsetMilliseconds(int offsetMilliseconds){
        this.offsetMilliseconds=offsetMilliseconds;
    }

    /**
     * Checks if the time of the cache has run out or expired from the SimpleAgedCache class, if and when the cache
     * starts or stops taking in tasks or entries. Based on the current clock instance being passed, this method
     * will decide if the current time elapsed (after the cache time expired) exceeds the originally initialized
     * cache time at the time of when the ExpirableEntry constructor was called when the Clock instance clock was
     * passed as an argument.
     *
     * @param clock the widely used Clock instance argument being passed from the SimpleAgedCache class
     * @return true if the current time post-active cache exceeds the initial cache time; false otherwise
     */
    public boolean checkOffsetClock(Clock clock) {
        int millis = (int)clock.millis();
        return millis > offsetMilliseconds;
    }

}