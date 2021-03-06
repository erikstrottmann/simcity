package restaurant.vegaperk.backend;

import CommonSimpleClasses.CityBuilding;
import agent.WorkRole;
import agent.interfaces.Person;
import restaurant.vegaperk.gui.WaiterGui;
import restaurant.vegaperk.interfaces.Customer;
import restaurant.vegaperk.interfaces.Waiter;
import gui.trace.AlertTag;

import java.util.*;

/**
 * Restaurant Waiter Agent
 */
//The waiter is the agent we see seating customers and taking orders in the GUI

// TODO Step 1: copy this class from the old waiter into a new class
public abstract class WaiterRoleBase extends WorkRole implements Waiter {
	protected List<MyCustomer> customers = Collections.synchronizedList(new ArrayList<MyCustomer>());
	
	enum BreakState { REQUEST_BREAK, ON_BREAK, NONE, OFF_BREAK, GOING_ON_BREAK };
	BreakState breakState = BreakState.NONE;
	
	// TODO Step 2: cut all references to the cook and paste into the old waiter role
	
	//agent members
	CashierRole cashier = null;
	
	private boolean shouldWork = false;
	
	protected WaiterGui waiterGui;
	protected int homePosition = -1;
	
	protected Menu menu = new Menu();
	
	protected HostRole host;

	public WaiterRoleBase(Person person, CityBuilding building) {
		super(person, building);
	}

	/** Accessors and setters */
	public String getName() {
		return super.getName();
	}

	public int getWaitingCustomers() {
		return customers.size();
	}
	
	public void setGui(WaiterGui gui) {
		waiterGui = gui;
	}

	public WaiterGui getGui() {
		return waiterGui;
	}
	
	public void setHost(HostRole h){
		host = h;
	}
	
	/** Messages - Received from Other Agents. */
	
	/** Messages from HostAgent */
	public void msgPleaseSeatCustomer(Customer c, int t){
		MyCustomer mc = findCustomer(c);
		if(mc == null){
			customers.add(new MyCustomer(c,t));
		}else{
			mc.state = MyCustomerState.WAITING;
		}
		stateChanged();
	}
	
	public void msgCanGoOnBreak(){
		Do("Going on break!");
		breakState = BreakState.GOING_ON_BREAK;
		stateChanged();
	}
	
	public void msgDenyBreak(){
		Do("break denied =(");
		breakState = BreakState.NONE;
		waiterGui.denyBreak();
		stateChanged();
	}
	
	public void msgHomePosition(int position){
		homePosition = position;
	}
	
	/** Messages from WaiterGui */
	public void msgAtDest() {//from animation
		doneWaitingForInput();
		stateChanged();
	}
	
	/** Messages from Customer */ 
	public void msgReadyToOrder(Customer c){
		MyCustomer mc = findCustomer(c);
		mc.state = MyCustomerState.READY_TO_ORDER;
		stateChanged();
	}
	
	public void msgHereIsMyOrder(Customer c, String choice){
		doneWaitingForInput();
		
		MyCustomer mc = findCustomer(c);
		mc.choice = choice;
		
		waiterGui.setOrderName(choice);
		stateChanged();
	}
	
	public void msgCustomerLeavingTable(Customer c) {
		MyCustomer mc = findCustomer(c);
		mc.state = MyCustomerState.LEAVING;
		stateChanged();
	}
	
	public void msgCannotPay(Customer c){
		doneWaitingForInput();
		stateChanged();
	}
	
	public void msgIAmDoneEating(Customer c){
		MyCustomer mc = findCustomer(c);
		mc.state = MyCustomerState.DONE_EATING;
		stateChanged();
	}
	
	public void msgHereIsCheck(Customer c, double check){
		Do("Got check.");
		MyCustomer mc = findCustomer(c);
		mc.bill = check;
		doneWaitingForInput();
		stateChanged();
	}
	
	public void msgGotFatigue(){
		breakState = BreakState.REQUEST_BREAK;
		host.msgIWantBreak(this);
		stateChanged();
	}
	
	public void msgOffBreak(){
		doneWaitingForInput();
		breakState = BreakState.OFF_BREAK;
		stateChanged();
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		try{
			for (MyCustomer c : customers) {
				if (c.state==MyCustomerState.WAITING) {
					c.state = MyCustomerState.SEATED;
					seatCustomer(c, c.table);//the action
					return true;//return true to the abstract agent to reinvoke the scheduler.
				}
			}
			for(MyCustomer c : customers){
				if(c.state==MyCustomerState.READY_TO_ORDER){
					c.state = MyCustomerState.ORDERED;
					takeOrder(c);
					return true;
				}
			}
			for(MyCustomer c : customers){
				if(c.state==MyCustomerState.FOOD_READY){
					c.state = MyCustomerState.SERVED;
					getFood(c);
					return true;
				}
			}
			for(MyCustomer c : customers){
				if(c.state==MyCustomerState.LEAVING){
					c.state = MyCustomerState.DONE;
					tellHostFreeTable(c.table);
					return true;
				}
			}
			for(MyCustomer c : customers){
				if(c.state==MyCustomerState.OUT_OF_CHOICE){
					tellCustomerOutOfFood(c);
					return true;
				}
			}
			for(MyCustomer c : customers){
				if(c.state==MyCustomerState.DONE_EATING){
					c.state = MyCustomerState.PAYING;
					getCheck(c);
				}
			}
			
			if(breakState == BreakState.OFF_BREAK){
				breakState = BreakState.NONE;
				goOffBreak();
				return true;
			}
			
			if(breakState == BreakState.GOING_ON_BREAK){
				boolean hasCustomers = false;
				for(MyCustomer c : customers){
					if(c.state != MyCustomerState.DONE){
						hasCustomers = true;
					}
				}
				if(hasCustomers == false){
					breakState = BreakState.ON_BREAK;
					goOnBreak();
				}
				return true;
			}
		}
		catch(ConcurrentModificationException e){
			return true;
		}
		
		if(shouldWork) goWait();
		else DoLeaveWork();
		
		return false;
	}

	/** Actions called in the Scheduler */
	protected void seatCustomer(MyCustomer customer, int table) {
		DoGoToHost();
		waitForInput();
		
		customer.c.msgSitAtTable(this, new Menu(),
				host.getTableMap().get(table).width, host.getTableMap().get(table).height);
		DoGoToTable(table);
		waitForInput();
		stateChanged();
	}
	
	// TODO step 3: make these two methods abstract, since they both reference the cook
	abstract protected void takeOrder(MyCustomer c);
	
	abstract protected void getFood(MyCustomer c);
	// END TODO	GO TO OLD WAITER
	
	protected void getCheck(MyCustomer c){
		Do("Going to cashier");
		
		DoGoToCenter();
		waitForInput();
		
		DoGoToCashier();
		waitForInput();
		
		double bill = menu.m.get(c.choice);
		
		cashier.msgDoneEating(c.c, this, bill);
		Do("Going to cashier");
		
		DoGoToTable(c.table);
		waitForInput();
		c.c.msgHereIsCheck(c.bill, cashier);
	}
	
	protected void tellCustomerOutOfFood(MyCustomer c){
		c.state = MyCustomerState.SEATED;
		
		DoGoToTable(c.table);
		waitForInput();
		
		c.c.msgOutOfChoice(c.choice);
	}
	
	protected void tellHostFreeTable(int table){
		host.msgTableIsFree(table);
	}
	
	protected void goOnBreak(){
		Do("Go On Break");
		DoGoOnBreak();
		waitForInput();
	}
	
	protected void goOffBreak(){
		Do("Go off break");
		host.msgOffBreak(this);
	}
	
	protected void goWait(){
		setPresent(true);
		DoGoWait();
	}
	
	/** The animation DoXYZ() routines */
	protected void DoGoToTable(int table) {
		waiterGui.DoGoToTable(host.getTableMap().get(table).width, host.getTableMap().get(table).height); 
	}
	
	protected void DoLeaveWork() {
		waiterGui.DoLeaveWork();
	}
	
	protected void DoGoToHost(){
		waiterGui.DoGoToHost();
	}
	protected void DoGoToCook(){
		waiterGui.DoGoToCook();
	}
	protected void DoGoWait(){
		waiterGui.DoGoToHomePosition(homePosition);
	}
	protected void DoGoOnBreak(){
		waiterGui.DoGoOnBreak();
	}
	protected void DoGoToCenter(){
		waiterGui.DoGoToCenter();
	}
	protected void DoGoToCashier(){
		waiterGui.DoGoToCashier();
	}
	
	/** Classes */
	protected enum MyCustomerState { NONE, WAITING, SEATED, READY_TO_ORDER,
		ORDERED, FOOD_READY, SERVED, DONE_EATING, PAYING, LEAVING, DONE, OUT_OF_CHOICE }
	
	class MyCustomer {
		Customer c;
		int table;
		String choice;
		double bill;
		MyCustomerState state;
		
		MyCustomer(Customer mc, int mt){
			c = mc;
			table = mt;
			this.state = MyCustomerState.WAITING; 
		}
	};
	
	public class Menu{
		Map<String, Double> m;
		
		Menu(){
			m = new HashMap<String, Double>();
			m.put("Krabby Patty", 1.25);
			m.put("Kelp Rings", 2.00);
			m.put("Coral Bits", 1.50);
			m.put("Kelp Shake", 2.00);
		}
	} 
	
	/** Utility Functions */
	protected MyCustomer findCustomer(Customer c){
		synchronized(customers){
			for(MyCustomer mc : customers){
				if(mc.c == c){
					return mc;
				}
			}
		}
		return null;
	}
	
	public int getCustomerCount(){
		return customers.size();
	}
	
	@Override
	public void Do(String msg) {
		Do(AlertTag.RESTAURANT, msg);
	}

	@Override
	public boolean isAtWork() {
		return isActive();
	}

	@Override
	public boolean isOnBreak() {
		return isActive();
	}

	public void setPresent(boolean p) {
		waiterGui.setPresent(p);
	}
	
	@Override
	public void activate() {
		super.activate();
		shouldWork = true;
	}
	
	@Override
	public void msgLeaveWork() {
		Do("Leaving Work");
		shouldWork = false;
		DoLeaveWork();
		waitForInput();
		
		this.deactivate();
	}

	@Override
	public void setCashier(CashierRole cashier) {
		this.cashier = cashier;
	}
}