package restaurant.vegaperk.backend;

import CommonSimpleClasses.CityBuilding;
import CommonSimpleClasses.ScheduleTask;
import agent.Role;
import agent.WorkRole;
import agent.interfaces.Person;
import gui.Building;
import gui.trace.AlertTag;

import java.awt.Dimension;
import java.util.*;

import restaurant.vegaperk.gui.HostGui;
import restaurant.vegaperk.interfaces.Cashier;
import restaurant.vegaperk.interfaces.Cook;
import restaurant.vegaperk.interfaces.Customer;
import restaurant.vegaperk.interfaces.Waiter;

/**
 * Restaurant Host Agent
 */

public class HostRole extends WorkRole {
	/* --- Constants --- */
	static final int NTABLES = 4;//a global for the number of tables.
	private static final int TABLECOLNUM = 4;
	private static final int TABLEROWNUM = 1;
	private static final int TABLESPACING = 100;
	
	private ScheduleTask schedule = ScheduleTask.getInstance();
	
	private Cook cook;
	private Cashier cashier;
	
	private HostGui gui;
	
	private boolean shouldWork = false;
	
	int chooseWaiter = -1;//cycles through the list of waiters
	
	public List<MyCustomer> waitingCustomers =
			Collections.synchronizedList(new ArrayList<MyCustomer>());
	private List<MyWaiter> waiters =
			Collections.synchronizedList(new ArrayList<MyWaiter>());
	
	private Collection<Table> tables =
			Collections.synchronizedList(new ArrayList<Table>(NTABLES));
	//map stores an index and a dimension containing x & y coordinates
	
	public Map<Integer, Dimension> tableMap =
			Collections.synchronizedMap(new HashMap<Integer, Dimension>());

	private String name;

	public HostRole(Person person, CityBuilding building) {
		super(person, building);
		
		// make some tables
		synchronized(tables){
			for (int ix = 0; ix < NTABLES; ix++) {
				int x = (ix%TABLECOLNUM+1)*TABLESPACING - 25;
				int y = (ix/TABLEROWNUM+1)*TABLESPACING;
				tables.add(new Table(ix, x, TABLESPACING + 200));//how you add to a collections
				Dimension tableCoords = new Dimension(x, TABLESPACING + 200);
				tableMap.put(ix, tableCoords);
			}
		}
		
		Runnable command = new Runnable() {
			public void run(){
				Do("Leave work everyone!");
				msgLeaveWork();
			}
		};
		
		int closingHour = ((RestaurantVegaPerkBuilding) building).getOpeningHour();
		int closingMinute = ((RestaurantVegaPerkBuilding) building).getOpeningMinute();
		
		schedule.scheduleDailyTask(command, closingHour, closingMinute);
	}

	/** Messages from other agents */
	public void msgIWantFood(Customer c) {
		waitingCustomers.add(new MyCustomer(c));
		stateChanged();
	}
	
	public void msgTableIsFree(int t){
		for(Table table : tables){
			if(table.getTableID() == t){
				table.setUnoccupied();
			}
		}
		for(MyCustomer mc : waitingCustomers) {
			if(mc.table == t) {
				waitingCustomers.remove(mc);
			}
		}
		stateChanged();
	}
	
	public void msgIWantBreak(Waiter w){
		MyWaiter mw = findWaiter(w);
		Do("wants break");
		mw.state = WaiterState.REQUESTED_BREAK;
		stateChanged();
	}
	public void msgOffBreak(Waiter w){
		MyWaiter mw = findWaiter(w);
		Do("off break");
		mw.state = WaiterState.NONE;
		stateChanged();
	}

	/**
	 * Scheduler. Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		/* Think of this next rule as:
            Does there exist a table and customer,
            so that table is unoccupied and customer is waiting.
            If so seat him at the table.
		 */
		if(!shouldWork && waitingCustomers.isEmpty()) {
			closeRestaurant();
			return true;
		}
		
		synchronized(tables){
			
			for (Table table : tables) {
				if (!waitingCustomers.isEmpty() && !table.isOccupied() && !waiters.isEmpty()) {
					seatCustomer(waitingCustomers.get(0), table);//the action
					return true;//return true to the abstract agent to reinvoke the scheduler.
				}
			}
		}
		
		synchronized(waitingCustomers){
			for(MyCustomer c : waitingCustomers){
				if(c.customer.getName().equals("impatient")){
					tellCustomerNoTables(c.customer);
					return true;
				}
			}
		}
		
		synchronized(waiters){
			for(MyWaiter waiter : waiters){
				if(waiter.state == WaiterState.REQUESTED_BREAK){
					Do("schedule break");
					tryToPutOnBreak(waiter);
					return true;
				}
			}
		}
		
		return false;
	}

	/** Actions. Implement the methods called in the scheduler. */
	private void seatCustomer(MyCustomer c, Table table) {
		findLeastBusyWaiter().waiter.msgPleaseSeatCustomer(c.customer, table.getTableID());
		Do("seat at table " + table.getTableID());
		table.setOccupant(c.customer);
		c.state = CustomerState.EATING;
		c.table = table.tableID;
//		waitingCustomers.remove(customer);
		stateChanged();
	}
	
	private void tellCustomerNoTables(Customer c){
		Do("Can't seat anywhere!");
		c.msgTablesAreFull();
		waitingCustomers.remove(c);
		stateChanged();
	}
	
	private void tryToPutOnBreak(MyWaiter w){
		Do("trying to put on break");
		for(MyWaiter mw : waiters){
			if(mw.state == WaiterState.NONE){
				w.state = WaiterState.ON_BREAK;
				w.waiter.msgCanGoOnBreak();
				return;
			}
		}
		w.state = WaiterState.NONE;
		w.waiter.msgDenyBreak();
	}
	
	/** Utility functions */
	public void addWaiter(Waiter w){
		if(!((Role) w).isActive()) return;
		
		MyWaiter mw = new MyWaiter(w.getName(), w);
		waiters.add(mw);
		stateChanged();
	}
	
	private MyWaiter findLeastBusyWaiter(){
		chooseWaiter++;
		while(waiters.get(chooseWaiter%waiters.size()).state == WaiterState.ON_BREAK){
			chooseWaiter++;
		}
		
		return waiters.get(chooseWaiter%waiters.size());
	}
	
	public void setPresent(boolean b) {
		gui.setPresent(b);
	}
	
	private MyWaiter findWaiter(Waiter w){
		synchronized(waiters){
			for(MyWaiter temp : waiters){
				if(temp.waiter == w){
					return temp;
				}
			}
		}
		return null;
	}
	
	private enum CustomerState { NONE, SEATED, EATING }
	private class MyCustomer {
		Customer customer;
		CustomerState state;
		int table;
		
		MyCustomer(Customer c) {
			this.customer = c;
			this.state = CustomerState.NONE;
		}
	}
	
	private enum WaiterState {NONE, REQUESTED_BREAK, ON_BREAK};
	private class MyWaiter{
		Waiter waiter;
		WaiterState state;
		
		MyWaiter(String n, Waiter w){
			waiter = w;
			state = WaiterState.NONE;
		}
	}
	
	/* --- Retrievers and setters --- */
	public String getMaitreDName() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setCook(Cook cook) {
		this.cook = cook;
	}
	
	public void setCashier(Cashier cashier) {
		this.cashier = cashier;
	}
	
	public List<MyCustomer> getWaitingCustomers() {
		return waitingCustomers;
	}

	public Map<Integer, Dimension> getTableMap() {
		return tableMap;
	}

	private boolean tablesOccupied() {
		for(Table t : tables) {
			if(t.isOccupied()) return false;
		}
		return true;
	}
	
	private class Table {
		Customer occupiedBy;
		int tableID;
		int tableX;
		int tableY;

		Table(int tableNumber, int tx, int ty) {
			this.tableID = tableNumber;
			this.tableX = tx;
			this.tableY = ty;
		}

		void setOccupant(Customer cust) {
			occupiedBy = cust;
		}

		void setUnoccupied() {
			occupiedBy = null;
		}

		Customer getOccupant() {
			return occupiedBy;
		}

		boolean isOccupied() {
			return occupiedBy != null;
		}

		public String toString() {
			return "table " + tableID;
		}
		
		public int getTableCount(){
			return NTABLES;
		}
		public int getTableID(){
			return tableID;
		}
		public int getX(){
			return tableX;
		}
		public int getY(){
			return tableY;
		}
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

	@Override
	public void activate() {
		super.activate();
		gui.setPresent(true);
		gui.DoGoHome();
		
		for(MyWaiter mw : waiters) {
			((WaiterRoleBase) mw.waiter).activate();
		}
		((CookRole) cook).activate();
		((CashierRole) cashier).activate();
		
		shouldWork = true;
	}
	
	@Override
	public void msgLeaveWork() {
		shouldWork = false;
		stateChanged();
	}
	
	public void closeRestaurant() {
		for(MyWaiter mw : waiters) {
			((WaiterRoleBase) mw.waiter).msgLeaveWork();
		}
		
		((CookRole) cook).msgLeaveWork();
		((CashierRole) cashier).msgLeaveWork();
		
		this.gui.DoLeaveWork();
		waitForInput();
		
		this.deactivate();
	}

	public void msgAtDestination() {
		doneWaitingForInput();
		stateChanged();
	}
	
	public void setGui(HostGui gui) {
		this.gui = gui;
	}

	public boolean onDuty() {
		return isActive();
	}
}
