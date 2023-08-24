package nuber.students;

import java.util.Date;
import java.util.concurrent.Callable;

/**
 * 
 * Booking represents the overall "job" for a passenger getting to their destination.
 * 
 * It begins with a passenger, and when the booking is commenced by the region 
 * responsible for it, an available driver is allocated from dispatch. If no driver is 
 * available, the booking must wait until one is. When the passenger arrives at the destination,
 * a BookingResult object is provided with the overall information for the booking.
 * 
 * The Booking must track how long it takes, from the instant it is created, to when the 
 * passenger arrives at their destination. This should be done using Date class' getTime().
 * 
 * Booking's should have a globally unique, sequential ID, allocated on their creation. 
 * This should be multi-thread friendly, allowing bookings to be created from different threads.
 * 
 * @author james
 *
 */
public class Booking implements Callable<BookingResult>{

	NuberDispatch dispatch;
	Passenger passenger;
	String curDriver;
	public static int globalid = 0;
	private int id = 0;
	
	/**
	 * Creates a new booking for a given Nuber dispatch and passenger, noting that no
	 * driver is provided as it will depend on whether one is available when the region 
	 * can begin processing this booking.
	 * 
	 * @param dispatch
	 * @param passenger
	 */
	public Booking(NuberDispatch dispatch, Passenger passenger)	{
		globalid++;
		id = globalid;
		dispatch.logEvent(this, "Creating Booking");
		this.dispatch = dispatch;
		this.passenger = passenger;
	}
	
	/**
	 * At some point, the Nuber Region responsible for the booking can start it (has free spot),
	 * and calls the Booking.call() function, which:
	 * 1.	Asks Dispatch for an available driver
	 * 2.	If no driver is currently available, the booking must wait until one is available. 
	 * 3.	Once it has a driver, it must call the Driver.pickUpPassenger() function, with the 
	 * 			thread pausing whilst as function is called.
	 * 4.	It must then call the Driver.driveToDestination() function, with the thread pausing 
	 * 			whilst as function is called.
	 * 5.	Once at the destination, the time is recorded, so we know the total trip duration. 
	 * 6.	The driver, now free, is added back into Dispatch’s list of available drivers. 
	 * 7.	The call() function the returns a BookingResult object, passing in the appropriate 
	 * 			information required in the BookingResult constructor.
	 *
	 * @return A BookingResult containing the final information about the booking 
	 */
	public BookingResult call() {
		dispatch.logEvent(this, "Starting Booking, Getting Driver");
		//Start is used to time how long it takes to run
		long start = 0;
		start = new Date().getTime();
		//Get first available driver in queue
		Driver availableDriver = null;
		availableDriver = dispatch.getDriver();
		this.curDriver = availableDriver.name;
		
		dispatch.logEvent(this, "Starting Booking, on way to Passenger");
		availableDriver.pickUpPassenger(passenger);
		
		dispatch.logEvent(this, "Collected Passenger, on way to destination");
		availableDriver.driveToDestination();
		
		dispatch.logEvent(this, "At destination, driver is now free");
		//Work out how long the call took to complete
		long totalTime = new Date().getTime() - start;
		//Add driver back into queue
		dispatch.addDriver(availableDriver);
		BookingResult newResult = new BookingResult(0, passenger, null, totalTime);
		return newResult;
	}
	
	/***
	 * Should return the:
	 * - booking ID, 
	 * - followed by a colon, 
	 * - followed by the driver's name (if the driver is null, it should show the word "null")
	 * - followed by a colon, 
	 * - followed by the passenger's name (if the passenger is null, it should show the word "null")
	 * 
	 * @return The compiled string
	 */
	@Override
	public String toString() {
		String passengerName = this.passenger == null ? "null" : this.passenger.name;
		return id + ":" + curDriver + ":" 
				+ passengerName;
	}

}
