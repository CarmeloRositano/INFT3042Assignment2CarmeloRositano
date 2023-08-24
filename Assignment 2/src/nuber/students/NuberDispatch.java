package nuber.students;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Future;

/**
 * The core Dispatch class that instantiates and manages everything for Nuber
 * 
 * @author james
 *
 */
public class NuberDispatch {

	/**
	 * The maximum number of idle drivers that can be awaiting a booking 
	 */
	private final int MAX_DRIVERS = 999;
	boolean shuttingDown = false;
	private int numOfBookings = 0;
	private Queue<Driver> driverQueue = new LinkedList<>();
	private HashMap<String, NuberRegion> regionList = new HashMap<String, NuberRegion>();
	HashMap<String, Integer> regionInfo;
	private boolean logEvents = false;
	
	/**
	 * Creates a new dispatch objects and instantiates the required regions and any other objects required.
	 * It should be able to handle a variable number of regions based on the HashMap provided.
	 * 
	 * @param regionInfo Map of region names and the max simultaneous bookings they can handle
	 * @param logEvents Whether logEvent should print out events passed to it
	 */
	public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents) {
		
		System.out.println("Creating Nuber Dispatch");
		System.out.println("Creating " + regionInfo.size() + " Region(s)");
		//Go through all param in hashmap and add them to local hashmap
		for (String keys : regionInfo.keySet()) {
			regionList.put(keys, new NuberRegion(this, keys, regionInfo.get(keys)));
			System.out.println("Creating Nuber Region for " + keys);
		}
		
		this.regionInfo = regionInfo;
		this.logEvents = logEvents;
		System.out.println("Done " + regionInfo.size() + " Region(s)");
	}
	
	/**
	 * Adds drivers to a queue of idle driver.
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @param The driver to add to the queue.
	 * @return Returns true if driver was added to the queue
	 */
	public synchronized boolean addDriver(Driver newDriver) {
		/**
		 * Add driver to queue and notifyall that driver has been added so get function
		 * can be continue if driver queue is empty
		 */
		try { 
			driverQueue.add(newDriver);
			notify();
			return true;
		} catch (Exception e) {
			System.err.println(e);
			System.err.println("ERROR: Adding driver to queue");
			return false;
		}
	}
	
	/**
	 * Gets a driver from the front of the queue
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @return A driver that has been removed from the queue
	 */
	public synchronized Driver getDriver() {
		while (driverQueue.isEmpty()) { //Wait while driver queue is empty until notified
			try {
				wait();
			} catch (InterruptedException e) {}
		}
		Driver tempDriver = driverQueue.poll();
		numOfBookings--;
		return tempDriver;		
	}

	/**
	 * Prints out the string
	 * 	    booking + ": " + message
	 * to the standard output only if the logEvents variable passed into the constructor was true
	 * 
	 * @param booking The booking that's responsible for the event occurring
	 * @param message The message to show
	 */
	public void logEvent(Booking booking, String message) {
		
		if (!logEvents) return;
		
		System.out.println(booking + ": " + message);
		
	}

	/**
	 * Books a given passenger into a given Nuber region.
	 * 
	 * Once a passenger is booked, the getBookingsAwaitingDriver() should be returning one higher.
	 * 
	 * If the region has been asked to shutdown, the booking should be rejected, and null returned.
	 * 
	 * @param passenger The passenger to book
	 * @param region The region to book them into
	 * @return returns a Future<BookingResult> object
	 */
	public synchronized Future<BookingResult> bookPassenger(Passenger passenger, String region) {
		if (shuttingDown) return null;
		
		Future<BookingResult> booked;
		booked = regionList.get(region).bookPassenger(passenger);
		numOfBookings++;
		return booked;
	}

	/**
	 * Gets the number of non-completed bookings that are awaiting a driver from dispatch
	 * 
	 * Once a driver is given to a booking, the value in this counter should be reduced by one
	 * 
	 * @return Number of bookings awaiting driver, across ALL regions
	 */
	public int getBookingsAwaitingDriver() {
		return numOfBookings;
	}
	
	/**
	 * Tells all regions to finish existing bookings already allocated, and stop accepting new bookings
	 */
	public void shutdown() {
		//Shutdown all threads in all regions
		for (String keys : regionList.keySet()) {
			regionList.get(keys).shutdown();
		}
		shuttingDown = true;
	}	
}
