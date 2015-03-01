package restaurant.jack;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import bank.interfaces.AccountManager;

import restaurant.jack.gui.HostGui;
import restaurant.jack.gui.RestaurantLucasBuilding;
import restaurant.jack.interfaces.Customer;
import restaurant.jack.interfaces.Host;
import restaurant.jack.interfaces.Waiter;
import CommonSimpleClasses.CityLocation;
import CommonSimpleClasses.ScheduleTask;
import agent.WorkRole;
import agent.interfaces.Person;

/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class HostRole extends WorkRole implements Host {
	static final int NTABLES = 3;//a global for the number of tables.
	//Notice that we implement waitingCustomers using ArrayList, but type it
	//with List semantics.
	public List<CustomerRole> waitingCustomers	= new ArrayList<CustomerRole>();
	public List<CustomerRole> activeCustomers	= new ArrayList<CustomerRole>();

	public Collection<Table> tables;
	public List<MyWaiter> myWaiters = Collections.synchronizedList(new ArrayList<MyWaiter>());
	
	private Semaphore active = new Semaphore(0);//overall semaphore

	List<WorkRole> workRoles = Collections.synchronizedList(new ArrayList<WorkRole>());
	
	public int waiterChoice = 0;
	
	private Map<Integer, Dimension> tableMap = new HashMap<Integer, Dimension>();

	private String name;
	
	int waitingPositionNum = 0;


	public HostGui hostGui = null;
	
	int occupiedNum = 0;
	
	boolean atWork;
	
	public enum waiterState {working, breakRequested, onBreak};
	
	public static class MyWaiter {
		MyWaiter(WaiterRoleBase waiter2, waiterState state) {
			this.waiter = waiter2;
			this.state = state;
		}
		
		WaiterRoleBase waiter;
		boolean noCustomers = true;
		waiterState state;
		List<CustomerRole> customers = new ArrayList<CustomerRole>();
	}

	
	ScheduleTask closeTask = ScheduleTask.getInstance();
	boolean endWorkDay = false;;
	
	
	public HostRole(Person p, CityLocation c) {
		super(p, c);
		
		Runnable command = new Runnable(){
			@Override
			public void run() {
				//do stuff
				
				msgLeaveWork();
				
//				System.out.println("ClOCKS RUN");
				}
			
		};
		
		// every day at TIME to end
		int closeHour = ((RestaurantLucasBuilding) c).getClosingHour();
		int closeMinute = ((RestaurantLucasBuilding) c).getClosingMinute();
//		System.out.println("Close: " + closeHour);
		closeTask.scheduleDailyTask(command, closeHour, closeMinute);
	
		
		
		
		atWork = false;
		Dimension t1 = new Dimension(100, 100);
		Dimension t2 = new Dimension(200, 100);
		Dimension t3 = new Dimension(300, 100);
		Dimension t4 = new Dimension(400, 100);
		tableMap.put(0, t1);
		tableMap.put(1, t2);
		tableMap.put(2, t3);
		tableMap.put(3, t4);
		

		// make some tables
		tables = Collections.synchronizedList(new ArrayList<Table>(NTABLES));
		for (int ix = 0; ix <= NTABLES; ix++) {
			tables.add(new Table(ix, (ix%4 + 1) * 100, (ix/4 + 1) * 100  ));//how you add to a collections
		}
	}

	public String getMaitreDName() {
		return name;
	}
	
	public Map<Integer, Dimension> getTableMap() {
		return tableMap;
	}
	
	public void addWaiter(WaiterRoleBase waiter) {
		Do("Welcome new waiter: " + waiter.getName());
		myWaiters.add(new MyWaiter(waiter, waiterState.working));
		waiter.setIdlePosition(myWaiters.size()); //ensures unique idle positions
		stateChanged();
	}

	public String getName() {
		return name;
	}

	public List getWaitingCustomers() {
		return waitingCustomers;
	}

	public Collection getTables() {
		return tables;
	}
	
	// MESSAGES//////////////////////#########################################################

	public void msgLeaveWork() {
		endWorkDay = true;
		stateChanged();
	}
	
	public void msgIWantFood(CustomerRole cust) {
		waitingCustomers.add(cust);
		activeCustomers.add(cust);
		stateChanged();
	}

	public void msgLeavingTable(Customer customer) {
		for(MyWaiter mw : myWaiters) {
			for(int i = 0; i<mw.customers.size(); i++) {
				if(mw.customers.get(i) == customer) {
					mw.customers.remove(i);	
				}	
			}
		}
		for (Table table : tables) {
			if (table.getOccupant() == customer) {
				print(customer + " leaving " + table);
				table.setUnoccupied();
				stateChanged();
			}
		}
		activeCustomers.remove(customer);//to not close when customers present
	}
	
	public void msgIdLikeToGoOnBreak(WaiterRole w) {//v2.1 put waiter onbreak
		if(myWaiters.size()>1) { //HACKY, should tell waiter
			findMyWaiter(w).state = waiterState.breakRequested;//problem, waiter can be added after this
		}
		stateChanged();
	}

	public void msgNoCustomers (WaiterRole w) {
		MyWaiter mw = findMyWaiter(w);
		mw.noCustomers = true;
		stateChanged();
	}
	
	public void msgReturningToWork(WaiterRole w) {
		MyWaiter mw = findMyWaiter(w);
		mw.state = waiterState.working;
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
//		if(!waitingCustomers.isEmpty()) {
//			waitingCustomers.get(0).msgSetWaitingPosition(waitingPositionNum);
//			waitingPositionNum++;
//		}
		if(!atWork) {
			goToWork();
			return true;
		}
		
		synchronized(tables) {
			for (Table table : tables) {
				if (!table.isOccupied()) {
					if (!waitingCustomers.isEmpty() && !myWaiters.isEmpty()) {
						seatCustomer(waitingCustomers.get(0), table, chooseWaiter());//the action

						return true;//return true to the abstract agent to reinvoke the scheduler.
					}
				}
//				if(table.isOccupied()) {
//					
//					occupiedNum++;
//					Do("mMmMm" + occupiedNum);
//					if(occupiedNum == 3 && !waitingCustomers.isEmpty()) {//all tables occupied
//						Do("MEHHDHD");
//						waitingCustomers.get(0).goToWaitPosition(0);
//						occupiedNum = 0;
//						return true;
//					}
//					
//				}
			}
		}
		
		synchronized(myWaiters) {
			for(MyWaiter mw: myWaiters) {
				if(mw.state==waiterState.breakRequested && mw.noCustomers) {
					sendOnBreak(mw);
					return true;
				}
			}
		}
		
		if(endWorkDay) {
			endWorkDay();
			return true;
		}
		
		
		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions
	private void endWorkDay() {//tells all employees to leave
		if(activeCustomers.isEmpty()){
			for(WorkRole r: workRoles) {
				r.msgLeaveWork();
			}
//			goOffWork();
//			active.release();
			addPaycheckToWallet();
			hostGui.DoEndWorkDay();
			atWork = false;
			endWorkDay = false;
			this.deactivate();
		}
	}
	
	
	private void goOffWork() {
			addPaycheckToWallet();
			doEndWorkDay();
			
	}
	
	private void doEndWorkDay() {
		hostGui.DoEndWorkDay();
//		acquireSemaphore(active);
	}
	
	private WaiterRoleBase chooseWaiter() {
		 //chooses different waiter by increasing by one every time called if more available
		while(!myWaiters.isEmpty()){
			WaiterRoleBase w = myWaiters.get(waiterChoice%myWaiters.size()).waiter;
			waiterChoice++;
			if(findMyWaiter(w).state == waiterState.working) {
				if(findMyWaiter(w).waiter.isActive()){
					return w;
				}
				
			
			}	
		}
		
		return null;

	}
	
	private void goToWork() {
		hostGui.setName(person.getName());
		hostGui.DoGoToDesk();
		acquireSemaphore(active);
		atWork = true;
	}

	private void seatCustomer(CustomerRole customer, Table table, WaiterRoleBase waiterRoleBase) {

		MyWaiter mw = findMyWaiter(waiterRoleBase);
		mw.customers.add(customer);
		mw.noCustomers = false;
		table.setOccupant(customer);
		waitingCustomers.remove(customer);
		hostGui.DoDirectAWaiter();
		acquireSemaphore(active);
		hostGui.DoGoToDesk();
		acquireSemaphore(active);
		waiterRoleBase.msgSitAtTable(customer, table.tableNumber);
		

	}

	private void acquireSemaphore(Semaphore s) {
		try {
			s.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// The animation DoXYZ() routines
	private void DoSeatCustomer(CustomerRole customer, Table table) {
		//Notice how we print "customer" directly. It's toString method will do it.
		//Same with "table"
		
		print("Seating " + customer + " at " + table);
		hostGui.DoBringToTable( table.getXCoord(), table.getYCoord()); 

	}

	private void sendOnBreak(MyWaiter mw) {
		mw.waiter.msgGoOnBreak();
		mw.state = waiterState.onBreak;
	}
	//utilities

	public void addRole(WorkRole r) {
		workRoles.add(r);
	}
	
	private void addPaycheckToWallet() {
		this.getPerson().getWallet().addCash(300.0);
	}
	
	public MyWaiter findMyWaiter(WaiterRoleBase w) {
		for(MyWaiter mw: myWaiters) {
			if(mw.waiter == w)
				return mw;
		
		}
		return null;
		
	}
	
	public void setWaiterBreak(String waiterName) {//called from panel 
		for(MyWaiter mw: myWaiters) {
			if(mw.waiter.getName().equals(waiterName) && myWaiters.size() >= 1) {
				for(MyWaiter mw2: myWaiters) {
					if(mw2.state == waiterState.working && mw!=mw2) {
						Do("time for break " + waiterName);//TODO changed UNSURE
						mw.waiter.msgSetBreakBit();
					}
				}
			}
		}
	}
	
//	public void pauseAll() {
//		for(MyWaiter w: myWaiters) {
//			w.waiter.pause();
//			w.waiter.pauseAllCustomers();
//		}
//			
//	}
//	
//	public void restartAll() {
//		for(MyWaiter w: myWaiters) {
//			w.waiter.restart();
//			w.waiter.restartAllCustomers();
//		}
//	}
//	
	public void setGui(HostGui gui) {
		hostGui = gui;
	}

	public HostGui getGui() {
		return hostGui;
	}
	
	public void msgAtDestination() {
		active.release();
	}

	private class Table {
		CustomerRole occupiedBy;
		int tableNumber;
		int xCoord;
		int yCoord;
		

		Table(int tableNumber, int x, int y) {
			this.tableNumber = tableNumber;
			this.xCoord = x;
			this.yCoord = y;
		}

		void setOccupant(CustomerRole cust) {
			occupiedBy = cust;
		}

		void setUnoccupied() {
			occupiedBy = null;
		}

		CustomerRole getOccupant() {
			return occupiedBy;
		}

		boolean isOccupied() {
			return occupiedBy != null;
		}

		public String toString() {
			return "table " + tableNumber;
		}
		
		int getXCoord() {
			return xCoord;
		}
		
		int getYCoord() {
			return yCoord;
		}
		
		void setXcoord(int num) {
			xCoord = num;
		}
		
		void setYCoord(int num) {
			yCoord = num;
		}
		
	
		
	}

	@Override
	public void msgIWantFood(restaurant.jack.interfaces.Customer c) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void msgIdLikeToGoOnBreak(Waiter w) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgNoCustomers(Waiter w) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgReturningToWork(Waiter w) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isAtWork() {
		return isActive();
	}

	@Override
	public boolean isOnBreak() {
		// TODO Auto-generated method stub
		return false;
	}


}

