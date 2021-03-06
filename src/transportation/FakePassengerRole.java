package transportation;

import transportation.interfaces.Bus;
import transportation.interfaces.Passenger;
import transportation.interfaces.PassengerRequester;
import CommonSimpleClasses.CityLocation;
import CommonSimpleClasses.CardinalDirectionEnum;
import agent.PersonAgent;
import agent.Role;

public class FakePassengerRole extends PassengerRole {

	//CityLocation the Passenger is ultimately trying to get to.
	CityLocation destination = null;
	private PassengerRequester requesterRole;
	
	public FakePassengerRole(CityLocation startingLocation) {
		location = startingLocation;
	}

	@Override
	public void msgGoToLocation(CityLocation loc, boolean willingToUseBus) {
		destination = loc;
		stateChanged();
	}

	@Override
	public void msgWelcomeToBus(Bus b, double fare) {}

	@Override
	public void msgWeHaveArrived(CityLocation loc) {}

	@Override
	public boolean pickAndExecuteAnAction() {
		if (destination != null) {
			location = destination;
			destination = null;
			if (requesterRole == null) ((PersonAgent) getPerson()).msgArrivedAtDestination();
			else{
				requesterRole.msgArrivedAtDestination();
				requesterRole = null;
			}
			deactivate();
			return true;
		}
		return false;
	}

	@Override
	public void msgGoToLocation(CityLocation loc, boolean willingToUseBus,
			PassengerRequester requesterRole) {
		this.requesterRole = requesterRole;
		msgGoToLocation(loc, willingToUseBus);
		
	}

	@Override
	public void msgGotInCar() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CardinalDirectionEnum currentDirection() {
		// TODO Auto-generated method stub
		return null;
	}


}
