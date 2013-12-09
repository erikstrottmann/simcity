package restaurant.lucas;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import restaurant.lucas.gui.CustomerGui;
import restaurant.lucas.interfaces.Cashier;
import restaurant.lucas.interfaces.Customer;
import restaurant.lucas.interfaces.Host;
import restaurant.lucas.interfaces.Waiter;

import CommonSimpleClasses.CityLocation;
import agent.PersonAgent.HungerLevel;
import agent.Role;
import agent.interfaces.Person;
import agent.interfaces.Person.Wallet;

/**
 * Restaurant customer role.
 * 
 * @author Jack Lucas
 */
public class CustomerRole extends Role implements Customer {
	
	private String name;
	private int hungerLevel = 5;        // determines length of meal
	int tableXCoord;
	int tableYCoord;
	Timer timer = new Timer();
	Random rnd = new Random();

	String choice = "";

	public class WaitPosition {
		private Semaphore occupied = new Semaphore(1);
		int x;
		int y;

		WaitPosition(int x, int y) {
			this.x = x;
			this.y =y;
		}

		public void tryAcquire(Semaphore s) {
			try {
				s.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		//semaphore and position
	}

	static List<WaitPosition> waitPositions = new ArrayList<WaitPosition>();


	List<String> menu = new ArrayList<String>();
	Map<String, Double> menuMap = new HashMap<String, Double>();

	int choiceNumber =0;

	private CustomerGui customerGui;

	// agent correspondents
	private Waiter waiter;
	private Host host;

	double myMoney = 23;
	double myBill = 0;
	double owedMoney = 0;
	boolean willLeaveIfFull = false;
	Cashier myCashier;


	//    private boolean isHungry = false; //hack for gui
	public enum CustomerState
	{DoingNothing, WaitingInRestaurant, BeingSeated, Seated, ReadyToOrder, Ordered, Eating, waitingForBill, paying, DoneEating, Leaving};
	private CustomerState state = CustomerState.DoingNothing;//The start state

	public enum CustomerEvent
	{none, gotHungry, followWaiter, reachedTable, decidedOrder, askedForOrder, askedToReorder, receivedFood, receivedBill, receivedChange, doneEating, doneLeaving, restaurantFull};
	CustomerEvent event = CustomerEvent.none;


	@Override
	public void gotHungry() {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgRestarauntIsFull() {
		event = CustomerEvent.restaurantFull;
		stateChanged();			
	}

	@Override
	public void msgFollowMeToTable(Waiter w, int x, int y, Map<String, Double> m) {
		this.waiter = w;
		for (Entry<String, Double> entry : m.entrySet()) {
		    String food = entry.getKey();
		    Double price = entry.getValue();
		    menuMap.put(food,  price);
		}
//		for(int i = 0; i<m.size(); i++ ) 
//		{
//			menu.add(m.get(i));//gives customer menu from waiter
//		}
		print("Following " + w.getName() + " to my table");
		tableXCoord = x;
		tableYCoord = y;
		event = CustomerEvent.followWaiter;
		stateChanged();
	}

	@Override
	public void msgWhatWouldYouLike() {
		Do("I've been asked for my order");
		event = CustomerEvent.askedForOrder;
		stateChanged();
	}

	@Override
	public void msgOutOfChoice(Map<String, Double> newMenu) {
		menuMap = newMenu;
		doDisplayChoice("?");
		event = CustomerEvent.askedToReorder;
		stateChanged();
	}

	@Override
	public void msgAnimationFinishedDecidingOrder() {
		event = CustomerEvent.decidedOrder;
		stateChanged();
	}

	@Override
	public void msgHereIsYourFood() {
		event = CustomerEvent.receivedFood;
		stateChanged();
	}

	@Override
	public void msgHereIsCheck(Cashier c, double check) {
		event = CustomerEvent.receivedBill;
		myCashier = c;
		myBill = check;
		stateChanged();
	}

	@Override
	public void msgDoneEating() {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgAnimationFinishedGoToSeat() {
		event = CustomerEvent.reachedTable;
		stateChanged();
	}

	@Override
	public void msgAnimationFinishedLeaveRestaurant() {
		event = CustomerEvent.doneLeaving;
		stateChanged();
	}

	@Override
	public void msgHereIsChange(double change) {
		myMoney += change;
		event = CustomerEvent.receivedChange;
		stateChanged();
	}

	@Override
	protected boolean pickAndExecuteAnAction() {

		if (state == CustomerState.DoingNothing && event == CustomerEvent.gotHungry ){
			state = CustomerState.WaitingInRestaurant;
			goToRestaurant();
			return true;
		}
		if (state == CustomerState.WaitingInRestaurant && event == CustomerEvent.followWaiter ){
			state = CustomerState.BeingSeated;
			SitDown();
			return true;
		}
//		if (state == CustomerState.WaitingInRestaurant && event == CustomerEvent.restaurantFull) {
//			if(name.equals("impatient")) {
//				Do("REST IS FULLLL");
//				leaveTable();
//				return true;
//			}
//		}
		
		if (state == CustomerState.BeingSeated && event == CustomerEvent.reachedTable){
			decideOrder();
			state = CustomerState.Seated;
			return true;
		}
		if (state == CustomerState.Seated && event == CustomerEvent.decidedOrder ) {
			callWaiter();
			state = CustomerState.ReadyToOrder;
			return true;
		}
		
		if (state == CustomerState.ReadyToOrder && event == CustomerEvent.askedForOrder ) {
			Do("GIVING ORDER");
			giveOrder();
			state =CustomerState.Ordered;
			return true;
		}
		
		if (state == CustomerState.Ordered && event == CustomerEvent.askedToReorder)//v2.1 out of food addition
		{
			
			decideOrder();
			state = CustomerState.Seated;
		}
		
		if (state == CustomerState.Ordered && event == CustomerEvent.receivedFood) {
			EatFood();
			state = CustomerState.Eating;
			return true;
		}
		if (state == CustomerState.Eating && event == CustomerEvent.doneEating) {
			readyToPay();
			state = CustomerState.waitingForBill;
			return true;	
		}
		if (state == CustomerState.waitingForBill && event == CustomerEvent.receivedBill ) {
			pay();
			state = CustomerState.paying;
		}
		if (state == CustomerState.paying && event == CustomerEvent.receivedChange) {
			leaveTable();
			state = CustomerState.Leaving;
		}
		/*if(state == CustomerState.Eating && event == CustomerEvent.doneEating {
		 * doneAndPaying();
		 * state = CustomerState.WaitingForBill;
		 * return true;
		 * }
		 * 
		 * if(state == CustomerState.WaitingForBill && event == CustomerEvent.billArrived) {
		 * 	givePayment();
		 * state = CustomerState.Paying;
		 * return true;
		 * }
		 */
		if (state==CustomerState.Leaving &&  event == CustomerEvent.doneLeaving) {
			state = CustomerState.DoingNothing;
			return true;
			//do nothing
		}
//		if (state == CustomerState.Eating && event == CustomerEvent.doneEating){
//			state = CustomerState.Leaving;
//			leaveTable();
//			return true;
//		}
//		if (state == CustomerState.Leaving && event == CustomerEvent.doneLeaving){
//			state = CustomerState.DoingNothing;
//			//no action
//			return true;
//		}	
		return false;
	}
	
	
	// Actions

		private void goToRestaurant() {
			Do("Going to restaurant, I have $" + myMoney);
			host.msgIWantFood(this);//send our instance, so he can respond to us
		}

		private void SitDown() {
			Do("Being seated. Going to table");
			customerGui.DoGoToSeat(tableXCoord, tableYCoord);//hack; only one table
		}
		
		private void decideOrder(){
			Do("Deciding Order");
			if(name.equals("Tofu") || name.equals("Rice") || name.equals("Sushi") || name.equals("Noodles")) {
				choice = name;
				name = "merp";
			}
			else {
				ArrayList<String> menuItems = new ArrayList<String>();

				for (String stringKey : menuMap.keySet()) {
				     menuItems.add(stringKey);
				}
				
				int choiceNum = rnd.nextInt(menuItems.size());
				
				choice = menuItems.get(choiceNum);
				if(menuMap.get(choice) > myMoney) {//costs more than i got
					for(int i = 1; i < menuItems.size(); i++) {
						choice = menuItems.get((choiceNum+ i) % menuItems.size());
						if(menuMap.get(choice) <= myMoney) { //if i can afford it
							break;
						}
					}
					if(menuMap.get(choice) > myMoney) {
						waiter.msgLeavingTable(this);
						Do("I can't afford anything, leaving!");
						customerGui.DoExitRestaurant();
						return;
					}
				}
			}
			timer.schedule(new TimerTask() {//timer to delay order choice
				public void run() {
					print("decided");
					msgAnimationFinishedDecidingOrder();
//					event = CustomerEvent.doneEating;
					//isHungry = false;
//					stateChanged();
				}
			},
			1400);

//			choice = chooseFromMenu();
		}
		
		private void callWaiter(){
			Do("Calling Waiter");
			waiter.msgImReadyToOrder(this);
		}
		
		private void giveOrder() {
				
//			int choiceNumber = rnd.nextInt() % 4;
//			System.out.println(choiceNumber);

			waiter.msgHereIsMyChoice(this, choice);
			
			doDisplayChoice(choice + "?");
			//send message to custgui to draw letter
			Do("Ordering " + choice);
		}

		private void EatFood() {
			Do("Eating Food");
			doDisplayChoice(choice);
			//This next complicated line creates and starts a timer thread.
			//We schedule a deadline of getHungerLevel()*1000 milliseconds.
			//When that time elapses, it will call back to the run routine
			//located in the anonymous class created right there inline:
			//TimerTask is an interface that we implement right there inline.
			//Since Java does not all us to pass functions, only objects.
			//So, we use Java syntactic mechanism to create an
			//anonymous inner class that has the public method run() in it.
			timer.schedule(new TimerTask() {
				Object cookie = 1;
				public void run() {
					print("Done eating");
					event = CustomerEvent.doneEating;
					//isHungry = false;
					stateChanged();
				}
			},
			14000);//getHungerLevel() * 1000);//how long to wait before running task
		}
		
		private void readyToPay() {
			waiter.msgReadyToPay(this);
		}
		
		private void pay() {
			myCashier.msgHereIsMyPayment(this, Math.ceil(myBill + owedMoney));
			myMoney = this.myMoney - Math.ceil(myBill + owedMoney);
		}

		private void leaveTable() {
			Do("Leaving Restaraunt");
			doDisplayChoice("");
			if(myMoney < 0) {//if i couldn't afford this meal
				Do("I couldn't afford it but will pay next time!");
				owedMoney = myMoney * -1;
				myMoney = 40;
			}
			waiter.msgLeavingTable(this);//sends this to host
			customerGui.DoExitRestaurant();
		}
		
		private void leaveBecauseFull() {
			customerGui.DoExitRestaurant();
			host.msgLeavingTable(this);
			//TODO
		}

		// Accessors, etc.

		public void doDisplayChoice(String choice) {
			customerGui.displayChoice(choice);
		}
		
		public void setWaiter(Waiter waiter) {
			this.waiter = waiter;
		}
		
		public String getName() {
			return name;
		}
		
		public int getHungerLevel() {
			return hungerLevel;
		}

		public void setHungerLevel(int hungerLevel) {
			this.hungerLevel = hungerLevel;
			//could be a state change. Maybe you don't
			//need to eat until hunger lever is > 5?
		}

		public String toString() {
			return "customer " + getName();
		}

		public void setGui(CustomerGui g) {
			customerGui = g;
		}

		public CustomerGui getGui() {
			return customerGui;
		}

		
		public void goToWaitPosition(int num) {
			customerGui.updateWaitPosition(num);
		}





}