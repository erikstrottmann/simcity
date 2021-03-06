package transportation;

import java.util.ArrayList;
import java.util.List;

import CommonSimpleClasses.CardinalDirectionEnum;
import transportation.interfaces.Bus;
import transportation.interfaces.Busstop;
import transportation.interfaces.Corner;
import transportation.interfaces.Passenger;

//TODO will instantly get and remove passengers, should fix?
public class BusAgent extends VehicleAgent implements Bus {


	//List of Passengers in the bus.
	List<Passenger> passengerList = new ArrayList<Passenger>(); 
	
	//List of `Passenger`s waiting in the `currentBusstop`.
	List<Passenger> waitingPassengerList = new ArrayList<Passenger>(); 

	//Route the bus must load.
	List<Corner> busRoute; 

	//Busstop the Bus is currently in.
	Busstop currentBusstop;
		
	/* Specifies if the bus is following the route forwards 
	 * (`true`) or backwards (`false`).
	 */
	boolean orientation;
	
	//State the bus is in.
	BusStateEnum busState = BusStateEnum.Moving; 
	enum BusStateEnum {
		Moving,
		RequestingBusstop,
		LettingPassengersExit,
		RequestingPassengers,
		CallingPassengers
	}
		
	//Event the bus did.
	BusEventEnum busEvent = BusEventEnum.Initial;
	enum BusEventEnum {
		Initial,
		ReceivedBusstop,
		PassengersLeft,
		PassengersReceived,
		PassengersOnBus
	};
	
	public BusAgent(Corner currentCorner, boolean orientation,
			List<Corner> busRoute) {
		super(currentCorner, true);
		this.orientation = orientation;
		this.busRoute = busRoute;
	}
	
	@Override
	public void msgMyBusStop(List<Busstop> bsList) {
		if (bsList.size() > 0) {
			CardinalDirectionEnum myDir;
			try {
				myDir = myDirection();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception: Failed to find bus "
						+ "direction, will skip a busstop.");
				busEvent = BusEventEnum.PassengersOnBus;
				busState = BusStateEnum.CallingPassengers;
				return;
			} 
			for (Busstop bs : bsList) {
				if (bs.direction() == myDir) {
					currentBusstop = bs;
					break;
				}
			}
			busEvent = BusEventEnum.ReceivedBusstop;
		} else {
			busState = BusStateEnum.Moving;
			busEvent = BusEventEnum.Initial;
		}
		event = VehicleEventEnum.ReceivedAdjCornersAndBusS;
		stateChanged();
		return;

	}

	private CardinalDirectionEnum myDirection() throws Exception {
		Corner nextCorner = currentPath.get(0);
		int i;
		for (i = 0; i < adjCorners.size(); i++) {
			if (adjCorners.get(i).c == nextCorner) {
				break;
			}
		}
		
		if (i == adjCorners.size()) {
			throw new Exception("Next corner was not found "
					+ "in adjCorners");
		}
		return adjCorners.get(i).d;
	}
	

	@Override
	public void msgHereArePeople(List<Passenger> people) {
		waitingPassengerList = new ArrayList<Passenger> (people);
		busEvent = BusEventEnum.PassengersReceived;
		stateChanged();
	}

	@Override
	public void msgPayingFare(double fare) { // TODO decide if we're doing this
		stateChanged();
	}

	// TODO this does nothing cause we're not synchronizing
	// the exiting of passengers.
	@Override
	public void msgExiting(Passenger p) {
		synchronized (passengerList) {
			passengerList.remove(p);
		}
		
		/* TODO add counting mechanism if going to make 
		 * passengers exit in order
		 */
	}
	
	@Override
	protected boolean pickAndExecuteAnAction() {
		if (state == VehicleStateEnum.Requesting
				&& event == VehicleEventEnum.ReceivedAdjCorners
				&& !currentPath.isEmpty()
				&& busState == BusStateEnum.Moving) {
			busState = BusStateEnum.RequestingBusstop;
			currentCorner.msgYourBusStop(this);
			return true;
		} else if (busState == BusStateEnum.RequestingBusstop &&
				busEvent == BusEventEnum.ReceivedBusstop) {
			busState = BusStateEnum.LettingPassengersExit;
			letPassengersExit();
			return true;
		} else if (busState == BusStateEnum.LettingPassengersExit
				&& busEvent == BusEventEnum.PassengersLeft){
			currentBusstop.msgGiveMePeople(this);
			busState = BusStateEnum.RequestingPassengers;
			return true;
		} else if (busState == BusStateEnum.RequestingPassengers
				&& busEvent == BusEventEnum.PassengersReceived) {
			busState = BusStateEnum.CallingPassengers;
			letPassengersIn();
			return true;
		} else if (busState == BusStateEnum.CallingPassengers &&
				busEvent == BusEventEnum.PassengersOnBus) {
			busState = BusStateEnum.Moving;
			return super.pickAndExecuteAnAction();
		} else if (busState == BusStateEnum.Moving)
			return super.pickAndExecuteAnAction();
		else return false;
		
	}
	
	/* Messages all the passengers in the bus and lets them know
	 *  what `Corner` we are on so that they can decide to leave.
	 */
	private void letPassengersExit() {
		synchronized (passengerList) {
			for (Passenger passenger : passengerList) {
				try {
					passenger.msgWeHaveArrived(currentCorner);
				} catch (Exception e) {
					System.out.println("THIS SHOULDN'T HAPPEN!");
					e.printStackTrace();
				}
			}
		}

		//TODO here we're not waiting for passengers to exit
		busEvent = BusEventEnum.PassengersLeft;
	}
	
	/* Messages all the passengers waiting for the bus, and waits
	 *  for them to come in.
	 */
	private void letPassengersIn() {
		for (Passenger passenger : waitingPassengerList) {
			passenger.msgWelcomeToBus(this, 0); //TODO give fare?
			passengerList.add(passenger);
		}
		// TODO Everyone will come in instantly
		busEvent = BusEventEnum.PassengersOnBus;
	}

	@Override
	void endTravel() {
		currentPath = new ArrayList<Corner> (busRoute);
		if (!orientation) { 
			java.util.Collections.reverse(currentPath);
		}
	}

	@Override
	public boolean orientation() {
		return orientation;
	}

}
