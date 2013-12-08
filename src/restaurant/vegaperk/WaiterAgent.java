package restaurant.vegaperk;

import agent.Agent;
import restaurant.vegaperk.gui.WaiterGui;
import restaurant.vegaperk.interfaces.Customer;
import restaurant.vegaperk.interfaces.Waiter;

import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Restaurant Waiter Agent
 */
//The waiter is the agent we see seating customers and taking orders in the GUI
public class WaiterAgent extends Agent implements Waiter {
	private List<MyCustomer> customers = Collections.synchronizedList(new ArrayList<MyCustomer>());
	
	enum BreakState { REQUEST_BREAK, ON_BREAK, NONE, OFF_BREAK, GOING_ON_BREAK };
	BreakState breakState = BreakState.NONE;
	
	//agent members
	CookAgent cook = null;
	CashierAgent cashier = null;
	
	private String name;
	//this will pause the waiter whenever he is waiting for a response, and resume upon receiving a response 
	private Semaphore performingTasks = new Semaphore(0,true);

	public WaiterGui waiterGui = null;
	public Menu menu = new Menu();
	private int homePosition = -1;
	
	private HostAgent host;

	public WaiterAgent(String name, CashierAgent c) {
		super();
		this.name = name;
		this.cashier = c;
	}

	/** Accessors and setters */
	public String getName() {
		return name;
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
	
	public void setHost(HostAgent h){
		host = h;
	}
	
	public void setCook(CookAgent c){
		cook = c;
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
		performingTasks.release();
		stateChanged();
	}
	
	/** Messages from Customer */ 
	public void msgReadyToOrder(Customer c){
		MyCustomer mc = findCustomer(c);
		mc.state = MyCustomerState.READY_TO_ORDER;
		stateChanged();
	}
	public void msgHereIsMyOrder(Customer c, String choice){
		waiterGui.setOrderName(choice);
		performingTasks.release();
		MyCustomer mc = findCustomer(c);
		mc.choice = choice;
		stateChanged();
	}
	public void msgCustomerLeavingTable(Customer c) {
		MyCustomer mc = findCustomer(c);
		mc.state = MyCustomerState.LEAVING;
		stateChanged();
	}
	public void msgCannotPay(Customer c){
		performingTasks.release();
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
		performingTasks.release();
		stateChanged();
	}
	
	/** Messages from cook */
	public void msgOrderDone(String choice, int t){
		for(MyCustomer mc : customers){
			if(mc.table == t){
				mc.state = MyCustomerState.FOOD_READY;
			}
		}
		stateChanged();
	}
	public void msgOutOfChoice(String choice, int t){
		for(MyCustomer mc : customers){
			if(mc.table == t){
				mc.state = MyCustomerState.OUT_OF_CHOICE;
			}
		}
		stateChanged();
	}
	
	public void msgGotFatigue(){
		breakState = BreakState.REQUEST_BREAK;
		host.msgIWantBreak(this);
		stateChanged();
	}
	
	public void msgOffBreak(){
		performingTasks.release();
		breakState = BreakState.OFF_BREAK;
		stateChanged();
	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		/* Think of this next rule as:
            Does there exist a table and customer,
            so that table is unoccupied and customer is waiting.
            If so seat him at the table.
		 */
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
					c.state = MyCustomerState.SEATED;
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
		
		goWait();
		return false;
	}

	/** Actions called in the Scheduler */
	private void seatCustomer(MyCustomer customer, int table) {
		DoGoToHost();
		acquire(performingTasks);
		
		customer.c.msgSitAtTable(this, new Menu(),
				host.getTableMap().get(table).width, host.getTableMap().get(table).height);
		DoGoToTable(table);
		acquire(performingTasks);
		stateChanged();
	}
	
	private void takeOrder(MyCustomer c){
//		DoTakeOrder(); animation with speech bubble
		DoGoToTable(c.table);
		acquire(performingTasks);
		c.c.msgWhatWouldYouLike();
		acquire(performingTasks);
		
		if(c.state == MyCustomerState.LEAVING){
			return;
		}
		
		waiterGui.toggleHoldingOrder();
		DoGoToCook();
		acquire(performingTasks);
		Do("choice "+c.choice);
		cook.msgHereIsOrder(this, c.choice, c.table);
		waiterGui.toggleHoldingOrder();
		stateChanged();
	}
	
	private void getFood(MyCustomer c){
		if(c.state == MyCustomerState.LEAVING){
			return;
		}
		DoGoToCook();
		acquire(performingTasks);
		
		cook.msgGotFood(c.table);
		waiterGui.setOrderName(c.choice);
		waiterGui.toggleHoldingOrder();
		DoGoToTable(c.table);
		acquire(performingTasks);
		
		c.c.msgHereIsYourFood();
		waiterGui.toggleHoldingOrder();
	}
	
	private void getCheck(MyCustomer c){
		DoGoToCashier();
		Do("Going to cashier");
		acquire(performingTasks);
		
		double bill = menu.m.get(c.choice);
		cashier.msgDoneEating(c.c, bill, this);
		acquire(performingTasks);
		
		DoGoToTable(c.table);
		acquire(performingTasks);
		c.c.msgHereIsCheck(c.bill, cashier);
	}
	
	private void tellCustomerOutOfFood(MyCustomer c){
		DoGoToTable(c.table);
		acquire(performingTasks);
		c.c.msgOutOfChoice(c.choice);
	}
	
	private void tellHostFreeTable(int table){
		host.msgTableIsFree(table);
	}
	
	private void goOnBreak(){
		Do("Go On Break");
		DoGoOnBreak();
		acquire(performingTasks);
	}
	
	private void goOffBreak(){
		Do("Go off break");
		host.msgOffBreak(this);
	}
	
	private void goWait(){
		DoGoWait();
	}
	
	/** The animation DoXYZ() routines */
	private void DoGoToTable(int table) {
		waiterGui.DoGoToTable(host.getTableMap().get(table).width, host.getTableMap().get(table).height); 
	}
	
	private void DoGoToCook(){
		waiterGui.DoGoToCook();
	}
	private void DoGoToHost(){
		waiterGui.DoGoToHost();
	}
	private void DoGoWait(){
		waiterGui.DoGoToHomePosition(homePosition);
	}
	private void DoGoOnBreak(){
		waiterGui.DoGoOnBreak();
	}
	private void DoGoToCashier(){
		waiterGui.DoGoToCashier();
	}
	
	/** Classes */
	private enum MyCustomerState { NONE, WAITING, SEATED, READY_TO_ORDER,
		ORDERED, FOOD_READY, SERVED, DONE_EATING, PAYING, LEAVING, DONE, OUT_OF_CHOICE }
	
	private static class MyCustomer {
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
	private MyCustomer findCustomer(Customer c){
		synchronized(customers){
			for(MyCustomer mc : customers){
				if(mc.c == c){
					return mc;
				}
			}
		}
		return null;
	}
	private void acquire(Semaphore sem){
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	public int getCustomerCount(){
		return customers.size();
	}
}