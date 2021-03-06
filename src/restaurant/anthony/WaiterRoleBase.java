package restaurant.anthony;

import CommonSimpleClasses.CityLocation;
import agent.Agent;
import agent.WorkRole;
import agent.interfaces.Person;
import restaurant.anthony.CashierRole.Check;
import restaurant.anthony.CustomerRole.AgentState;
import restaurant.anthony.gui.WaiterGui;
import restaurant.anthony.interfaces.Cashier;
import restaurant.anthony.interfaces.Cook;
import restaurant.anthony.interfaces.Customer;
import restaurant.anthony.interfaces.Waiter;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Restaurant Host Agent
 */
// Waiter Agent
public abstract class WaiterRoleBase extends WorkRole implements Waiter {

	
	public List<MyCustomer> MyCustomers = new ArrayList<MyCustomer>();

	public List<Check> CheckList = new ArrayList<Check>();
	protected int waiterNumber;
	protected HostRole host;
	protected Cook cook;
	protected Cashier cashier;
	protected Semaphore atTable = new Semaphore(0, true);
	protected Semaphore atCook = new Semaphore(0, true);
	protected Semaphore Deliver = new Semaphore(0, true);
	protected Semaphore atCashier = new Semaphore(0, true);
	protected Semaphore atWaitingLine = new Semaphore(0,true);
	protected Semaphore atHome = new Semaphore(0,true);
	protected Semaphore atExit = new Semaphore(0,true);

	public enum AgentState {
		Idle, OffWork, TakingCustomer, AtCook, AtCashier, TakingFood, GoingToCook, TakingOrder, DeliverBadNews, GivingCheck, returning, NotAtWork
	};
	public enum AgentEvent {
		NotAtWork, AtWork
	};

	protected AgentEvent event = AgentEvent.NotAtWork;
	protected AgentState state = AgentState.NotAtWork;

	public WaiterGui waiterGui = null;
	protected boolean Break = false;

	public WaiterRoleBase(Person person, CityLocation location) {
		super(person, location);
		
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#getMaitreDName()
	 */
	@Override
	public String getMaitreDName() {
		return person.getName();
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#getName()
	 */
	@Override
	public String getName() {
		return person.getName();
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#getMyCustomers()
	 */
	@Override
	public List getMyCustomers() {
		return MyCustomers;
	}

	// Messages

	/* (non-Javadoc)
	 * @see restaurant.Waiter#AskForPermission()
	 */
	@Override
	public void AskForPermission() {
		if (Break) {
			//print("I am back to work");
			Break = false;
			return;
		}
		if (!Break) {
			//print("Ask For Permission");
			host.RequestABreak(this);
		}
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#msgLeavingTable(restaurant.interfaces.Customer)
	 */
	@Override
	public void msgLeavingTable(Customer cust) {
		for (int i = 0; i < MyCustomers.size(); i++) {
			if (cust == MyCustomers.get(i).c) {
				int table = MyCustomers.get(i).t;
				TableIsClear(table);
				MyCustomers.remove(i);
				return;
			}
		}
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#SitAtTable(restaurant.CustomerAgent, int)
	 */
	@Override
	public void SitAtTable(CustomerRole cust, int table) {
		MyCustomer C = new MyCustomer(cust, table);
		MyCustomers.add(C);
		stateChanged();
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#HeresMyChoice(restaurant.interfaces.Customer, java.lang.String)
	 */
	@Override
	public void HeresMyChoice(Customer cust, String CH) {
		print ("HeresMyChoice");
		for (int i=0;i<MyCustomers.size();i++){
			if (MyCustomers.get(i).c == cust){
				MyCustomers.get(i).choice = CH;
				MyCustomers.get(i).state = CustomerState.AboutToOrder;
			}
		}
		stateChanged();
	}

	
	

	/* (non-Javadoc)
	 * @see restaurant.Waiter#AskForCheck(restaurant.interfaces.Customer)
	 */
	@Override
	public void AskForCheck(Customer customer) {
		for (int i = 0; i < MyCustomers.size(); i++) {
			if (customer == MyCustomers.get(i).c) {
				MyCustomers.get(i).NeedCheck = true;
				// Check check= new Check(MyCustomers.get(i).t ,
				// MyCustomers.get(i).c.choice, this);
				// CheckList.add(check);
			}
		}
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#HereIsCheck(restaurant.CashierAgent.Check)
	 */
	@Override
	public void HereIsCheck(Check ch) {
		ch.doneComputing = true;
		CheckList.add(ch);
		for (int i = 0; i < CheckList.size(); i++) {
			if (ch == CheckList.get(i)) {
				CheckList.get(i).doneComputing = true;
				stateChanged();
			}
		}
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#msgAtTable(int)
	 */
	@Override
	public void msgAtTable(int tnumb) {// from animation
		// //print("msgAtTable() called");
		//print(" at table " + tnumb);
		atTable.release();
		if (state == AgentState.TakingCustomer){
			for (int i=0;i<MyCustomers.size();i++){
				if (MyCustomers.get(i).t == tnumb){
					MyCustomers.get(i).state = CustomerState.BeingSeated;
				}
					
			}
		}
		if (state == AgentState.TakingFood) {
			print ("GivingFoodTocust");
			Deliver.release();
			
			GiveFoodToCust(tnumb);
			waiterGui.DoLeaveCustomer(tnumb);
			//state = AgentState.Idle;

			stateChanged();
		}
		if (state == AgentState.DeliverBadNews) {
			DoTellCustomerBadNews(tnumb);
			waiterGui.DoLeaveCustomer(tnumb);
			//state = AgentState.Idle;
			stateChanged();
		}

		for (int i = 0; i < MyCustomers.size(); i++) {
			if (tnumb == MyCustomers.get(i).t
					&& MyCustomers.get(i).NeedCheck == true) {
				MyCustomers.get(i).NeedCheck = false;
				waiterGui.DoLeaveCustomer(tnumb);
				//state = AgentState.Idle;
				stateChanged();
			}
		}
		for (int i = 0; i < CheckList.size(); i++) {
			// print (CheckList.get(i).table + " This is the table number");
			if (tnumb == CheckList.get(i).table) {
				DoGiveCheckToCustomer(CheckList.get(i));
				CheckList.remove(i);
				waiterGui.DoLeaveCustomer(tnumb);
				//state = AgentState.Idle;
				stateChanged();
			}
		}

	}
	public void msgAtHome(){
		state = AgentState.Idle;
		atHome.release();
		stateChanged();
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#msgIdle()
	 */
	@Override
	public void msgIdle() {

		state = AgentState.Idle;
		stateChanged();
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#msgAtCook()
	 */
	@Override
	public void msgAtCook() {
		// Do (this + "At Cook");
		atCook.release();
		state = AgentState.AtCook;
		stateChanged();
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#msgAtCashier()
	 */
	@Override
	public void msgAtCashier() {
		//print("At Cashier now");
		atCashier.release();
		state = AgentState.AtCashier;
		stateChanged();
	}

	public void msgAtWaitingLine() {
		//print("At WaitingLine now");
		atWaitingLine.release();
	}
	
	@Override
	public void msgLeaveWork() {
		event = AgentEvent.NotAtWork;
		stateChanged();
	}
	
	public void AtExit(){
		atExit.release();
	}
	/**
	 * Scheduler. Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		try{
			
		if (state == AgentState.NotAtWork){
			GoToWork();
			return true;
		}
		/*
		 * Think of this next rule as: Does there exist a table and customer, so
		 * that table is unoccupied and customer is waiting. If so seat him at
		 * the table.
		 */
		if (state == AgentState.Idle) {
			for (int i = 0; i < MyCustomers.size(); i++) {
				if (MyCustomers.get(i).state == CustomerState.WaitingInRestaurant) {
					SeatCustomer(MyCustomers.get(i));
					return true;
				}
			}
			for (int i = 0; i < MyCustomers.size(); i++) {
				if (MyCustomers.get(i).FailOrder) {
					GoTellCustomer(MyCustomers.get(i));
					return true;
				}
			}
			for (int i = 0; i < MyCustomers.size(); i++) {
				if (MyCustomers.get(i).state == CustomerState.AboutToOrder) {
					GoTakeOrder(MyCustomers.get(i));
					return true;
				}

			}
			for (int i = 0; i < MyCustomers.size(); i++) {
				if (MyCustomers.get(i).order != null) {
					if (MyCustomers.get(i).order.process
							&& MyCustomers.get(i).state == CustomerState.Ordered) {
						DoPickUpOrder();
						return true;
					}
				}
			}
			for (int i = 0; i < MyCustomers.size(); i++) {
				if (MyCustomers.get(i).NeedCheck) {
					GoToCashier();
					return true;
				}
			}
			for (int i = 0; i < CheckList.size(); i++) {
				if (CheckList.get(i).doneComputing) {
					GoToCashier();
					return true;
				}
			}

			/*
			 * for (int i=0;i<CheckList.size();i++){ if (CheckList.get(i).price
			 * != -1){ BringCheckToCustomer(CheckList.get(i)); return true; } }
			 */

		}
		if (state == AgentState.AtCashier) {
			GiveCashierOrder();
			return true;
		}

		if (state == AgentState.AtCook) {
			GiveCookOrder();
			return true;
		}
		}
		catch (ConcurrentModificationException e){
			return false;
		}
		
		if (event == AgentEvent.NotAtWork && MyCustomers.size() == 0){
			OffWork();
			return true;
		}

		return false;
		// we have tried all our rules and found
		// nothing to do. So return false to main loop of abstract agent
		// and wait.
	}

	// Actions
	protected void OffWork(){
		waiterGui.GoToExit();
		try {
			atExit.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.deactivate();
		state = AgentState.NotAtWork;
		event = AgentEvent.AtWork;
	}
	
	protected void GoToWork(){
		event = AgentEvent.AtWork;
		waiterGui.GoToWork();
		try {
			atHome.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void TableIsClear(int t) {
		host.msgTableIsClear(t);
	}

	protected void SeatCustomer(MyCustomer c) {
		
		waiterGui.GoToWaitingLine();
		try {
			atWaitingLine.acquire();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		host.msgIAmTakingCust();
		c.c.msgSitAtTable(c.t, this, new Menu());
		DoSeatCustomer(c.c, c.t, new Menu());

		state = AgentState.TakingCustomer;
		stateChanged();
		// System.out.//print("Available permits :" + atTable.availablePermits()
		// );
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		waiterGui.DoLeaveCustomer(c.t);
		// System.out.//print("Available permits :" + atTable.availablePermits()
		// );

	}

	abstract protected void GoTakeOrder(MyCustomer c);

	abstract protected void GiveCookOrder();

	protected void GiveCashierOrder() {
		waiterGui.DoLeaveCashier();
		state = AgentState.returning;
		for (int i = 0; i < MyCustomers.size(); i++) {
			if (MyCustomers.get(i).NeedCheck) {
				// compute the bill
				//print("Compute Bill");
				MyCustomers.get(i).NeedCheck = false;
				cashier.ComputeBill(MyCustomers.get(i).choice,
						MyCustomers.get(i).t, this);
			}
		}
		for (int i = 0; i < CheckList.size(); i++) {
			if (CheckList.get(i).doneComputing) {
				//print("Delivering Check");
				CheckList.get(i).doneComputing = false;
				waiterGui.GoToTable(CheckList.get(i).table);
				state = AgentState.GivingCheck;
				try {
					atTable.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}
				return;
			}
		}

	}

	protected void GoTellCustomer(MyCustomer C) {
		state = AgentState.DeliverBadNews;
		stateChanged();
		waiterGui.GoToTable(C.t);
		try {
			atTable.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void DoTellCustomerBadNews(int TableNumber) {
		for (int i = 0; i < MyCustomers.size(); i++) {
			if (MyCustomers.get(i).t == TableNumber) {
				//print("Delivered Bad News to Customer");
				MyCustomers.get(i).FailOrder = false;
				waiterGui.DoLeaveCustomer(TableNumber);
				Menu menu = new Menu();
				menu.NoFood(MyCustomers.get(i).choice);
				MyCustomers.get(i).c.msgOrderFail(menu);
				//state = AgentState.Idle;
				stateChanged();
				return;
			}
		}
	}

	protected void DoGiveCheckToCustomer(Check ch) {
		for (int i = 0; i < MyCustomers.size(); i++) {
			if (MyCustomers.get(i).t == ch.table) {
				MyCustomers.get(i).c.HeresYourCheck(ch);
				waiterGui.DoLeaveCustomer(MyCustomers.get(i).t);
				//state = AgentState.Idle;
				stateChanged();
			}
		}
	}

	protected void GiveFoodToCust(int table) {
		for (int i = 0; i < MyCustomers.size(); i++) {
			if (MyCustomers.get(i).order != null){
				if (MyCustomers.get(i).order.IsProcessed()
						&& MyCustomers.get(i).t == table) {
					MyCustomers.get(i).c.HeresYourOrder(MyCustomers.get(i).order);
					waiterGui.DoneServing();
				}
			}
		}
	}

	// The animation DoXYZ() routines
	protected void DoSeatCustomer(Customer customer, int table, Menu m) {
		// Notice how we print "customer" directly. It's toString method will do
		// it.
		// Same with "table"
		//print("Seating " + customer + " at " + table);
		waiterGui.DoBringToTable(customer, table);
	}

	protected void DoGoCustomer(Customer customer, int table) {
		// Notice how we print "customer" directly. It's toString method will do
		// it.
		// Same with "table"
		//print("Going to " + customer + " at " + table);
		waiterGui.GoToTable(table);
	}

	protected void DoPickUpOrder() {
		// print ("Going to Cook to pick up the order");
		waiterGui.GoToCook();
		try {
			atCook.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void DeliveOrder(MyCustomer c, int table) {
		//print("Going To deliver Order to " + c.c + " at " + table);
		// print the food's name
		waiterGui.ServeOrder(c.choice);
		waiterGui.GoToTable(table);
		c.state = CustomerState.Eating;
	}

	protected void GoToCashier() {
		//print("Going to Cashier and Compute Bill");
		waiterGui.GoToCashier();
		try {
			atCashier.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#GoBreak(boolean)
	 */
	@Override
	public void GoBreak(boolean permission) {
		Break = permission;
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#IsOnBreak()
	 */
	@Override
	public boolean IsOnBreak() {
		return Break;
	}

	// utilities

	/* (non-Javadoc)
	 * @see restaurant.Waiter#setGui(restaurant.gui.WaiterGui)
	 */
	@Override
	public void setGui(WaiterGui gui) {
		waiterGui = gui;
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#getGui()
	 */
	@Override
	public WaiterGui getGui() {
		return waiterGui;
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#setHost(restaurant.HostAgent)
	 */
	
	public void setHost(HostRole ho) {
		host = ho;
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#setCook(restaurant.CookAgent)
	 */
	
	public void setCook(Cook co) {
		cook = co;
	}

	/* (non-Javadoc)
	 * @see restaurant.Waiter#setCashier(restaurant.CashierAgent)
	 */
	
	public void setCashier(Cashier ca) {
		cashier = ca;
	}
	
	public void setWaiterNumber (int x){
		waiterNumber = x;
	}
	
	public int getWaiterNumber (){
		return waiterNumber;
	}

	public enum CustomerState {
		DoingNothing, WaitingInRestaurant, BeingSeated, Seated, AboutToOrder, Ordered, Eating, DoneEating, ReadyForCheck, GoToCashier, NowLeave, Leaving
	};

	
	class MyCustomer {
		Customer c;
		int t;
		Order order;
		CustomerState state = CustomerState.WaitingInRestaurant;
		boolean FailOrder = false;
		boolean NeedCheck = false;
		String choice = null;

		MyCustomer(Customer cust, int tableNumber) {
			c = cust;
			t = tableNumber;
		}
	}

	public class Menu {
		List<Item> Menulist = new ArrayList<Item>();
		int number = 4;
		{
			Item KelpRings = new Item("Kelp Rings", (double) 10);
			Item KelpShake = new Item("Kelp Shake", (double) 20);
			Item KrabbyPatty = new Item("Krabby Patty", (double) 30);
			Item CoralBits = new Item("Coral Bits", (double) 50);
			Menulist.add(KelpRings);
			Menulist.add(KelpShake);
			Menulist.add(KrabbyPatty);
			Menulist.add(CoralBits);
		}

		public void NoFood(String choice) {
			number = 3;
			if (choice == "Kelp Rings") {
				Menulist.remove(0);
				return;
			}
			if (choice == "Kelp Shake") {
				Menulist.remove(1);
				return;
			}
			if (choice == "Krabby Patty") {
				Menulist.remove(2);
				return;
			}
			if (choice == "Coral Bits") {
				Menulist.remove(3);
				return;
			}
		}
	}

	public class Item {
		String name;
		double price;

		Item(String NA, double P) {
			name = NA;
			price = P;
		}
	}

	public class Order {
		String name;
		boolean process;
		int table;
		int stove;
		restaurant.anthony.interfaces.Waiter Waiter;

		Order(String NA, int t, restaurant.anthony.interfaces.Waiter wa) {
			name = NA;
			table = t;
			Waiter = wa;
			

		}

		public boolean IsProcessed() {
			if (process == false)
				return false;
			else
				return true;
		}

		public void Processed() {
			process = true;
		}
	}

	@Override
	public boolean isAtWork() {
		return isActive() && !isOnBreak();
	}

	@Override
	public boolean isOnBreak() {
		// TODO Auto-generated method stub
		return false;
	}

	
}
