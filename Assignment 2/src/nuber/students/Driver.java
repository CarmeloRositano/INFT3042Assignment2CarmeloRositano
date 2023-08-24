package nuber.students;

public class Driver extends Person {

	Passenger currentPassanger;
	
	public Driver(String driverName, int maxSleep) {
		super(driverName, maxSleep);
	}
	
	/**
	 * Stores the provided passenger as the driver's current passenger and then
	 * sleeps the thread for between 0-maxDelay milliseconds.
	 * 
	 * @param newPassenger Passenger to collect
	 * @throws InterruptedException
	 */
	public void pickUpPassenger(Passenger newPassenger)	{
		currentPassanger = newPassenger;
		try {
			Thread.sleep((int)(Math.random() * ((maxSleep) +1)));
		} catch (InterruptedException e) {}
	}

	/**
	 * Sleeps the thread for the amount of time returned by the current 
	 * passenger's getTravelTime() function
	 * 
	 * @throws InterruptedException
	 */
	public void driveToDestination() {
		try {
			Thread.sleep(currentPassanger.getTravelTime());
		} catch (InterruptedException e) {
			System.err.println("ERROR: Driver -> driveToDestination -> currentPassanger -> getTravelTime Error");
		}
	}
	
}
