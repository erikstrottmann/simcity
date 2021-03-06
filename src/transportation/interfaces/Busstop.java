package transportation.interfaces;

import CommonSimpleClasses.CityLocation;
import CommonSimpleClasses.CardinalDirectionEnum;

/*Handles interaction for waiting for a bus, and what 
 * to do on its arrival. Note: Position is generated 
 * from the corner's position and the direction.
 */

// TODO Update DD
public interface Busstop extends CityLocation {
	
	//Direction the bus is going when it reaches this stop.
	public CardinalDirectionEnum direction();

	/* Message from Passenger that tells 
	 * the bus stop that he's waiting for the bus.
	 */
	public void msgIAmHere(Passenger p);
	
	/* Message from Bus that gets a list of the 
	 * people waiting for the bus.
	 */
	public void msgGiveMePeople(Bus b);

	public Corner corner();

	public void startThread();
}
