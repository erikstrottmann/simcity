package restaurant.jack;

import restaurant.jack.interfaces.Customer;
import restaurant.jack.interfaces.Waiter;
import CommonSimpleClasses.CityLocation;
import agent.interfaces.Person;


public class WaiterRole extends WaiterRoleBase implements Waiter {
	


	private CookRole cook;
	
	
	public WaiterRole(Person p, CityLocation c) {
		super(p, c);
	}
	//Messages####################
	public void msgOutOfFood(int tableNum, String choice) {
		for(MyCustomer mc : myCustomers) {//locate customer through their table number
			if(mc.table == tableNum) {
				Do("dont have this item in stock!");//made it working here
				mc.state = customerState.needToReorder;
//				doGoToTable(mc.customer, mc.table);
//				acquireSemaphore(active);
			}
		}
		stateChanged();
	}
	
//	public void msgOrderIsReady(Customer c, String choice, int table, Dimension plateDim) {
//		MyCustomer mc = findCustomer(c);
//		mc.state = customerState.orderIsReady; 
//		plateAreaLocs.add(plateDim);
////		plateAreas.add(p);//TODO
//		stateChanged();
//	}
	
	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		if(!atWork) {
			goToWork();
			return true;
		}
		
		synchronized(myCustomers) {
			for(MyCustomer c : myCustomers) {
				if(c.state == customerState.waiting) {
					seatCustomer(c.customer, c.table);
					c.state = customerState.seated;
					return true;
				}
			}
		}

		synchronized(myCustomers) {
			for(MyCustomer c : myCustomers) { 
				if(c.state == customerState.readyToOrder) {
					takeOrder(c);
					c.state = customerState.ordered;
					return true;
				}
			}
		}

		synchronized(myCustomers) {
			for(MyCustomer c : myCustomers) {
				if(c.state == customerState.needToReorder) {
					reTakeOrder(c);
					c.state = customerState.seated;
					return true;
				}
			}
		}

		synchronized(myCustomers) {
			for(MyCustomer c : myCustomers) {
				if(c.state == customerState.orderIsReady) {
					deliverOrder(c.customer, c.table, c.choice);
					c.state=customerState.eating;
					return true;
				}
			}
		}

		synchronized(myCustomers) {
			for(MyCustomer c : myCustomers) {
				if(c.state == customerState.doneEating) {
					getCheck(c);
					c.state = customerState.paying;
					return true;
				}
			}
		}

		synchronized(myCustomers) {
			for(MyCustomer c : myCustomers) {
				if(c.state == customerState.leaving) {
					callHost(c);
					c.state=customerState.done; 
					return true;
				}	
			}
		}
		
		if(endWorkDay) {
			endWorkDay();
			return true;
		}


		return false;

	}
	
//	private void takeOrder(MyCustomer mc){
//
//		doGoToTable(mc.customer, mc.table);
////		Do("1");
//		acquireSemaphore(active);
////		Do("ICAN");
//		//doTakeOrder();
////		Do("2");
//		mc.customer.msgWhatWouldYouLike();
////		Do("3");
//		Do("What would you like customer?" + mc.customer);
////		Do("4");
//		acquireSemaphore(active);//released by customer msg
////		Do("5");
//		doGoToCook(530, 100);
//		acquireSemaphore(active);
//		cook.msgHereIsAnOrder(mc.choice, mc.table, mc.customer, this);
//		doDisplayChoice("");
//		doGoAway();
//		
//	}
//	
//	private void deliverOrder(Customer cust, int table, String choice) {
//		Do("Beginning order delivery process");
//		doGoToCook(plateAreaLocs.get(0).width, 50);
//		acquireSemaphore(active);
//		doGoToCook(plateAreaLocs.get(0).width, plateAreaLocs.get(0).height);
//		acquireSemaphore(active);
//
//		
//		cook.msgNullifyPlateArea(plateAreaLocs.get(0).height);
//		plateAreaLocs.remove(0);
////		plateAreas.get(0).o = null;//TODO
////		plateAreas.remove(0);
//		
//		doGoToCook(500, 50);
//		acquireSemaphore(active);
//		doGoToTable(cust, table);
//		doDisplayChoice(choice);
//		acquireSemaphore(active);
//		
//		Do("Here is your food " + cust);
//		doDisplayChoice("none");
//		cust.msgHereIsYourFood();
////		Do("Now i should leave you " + cust);
//		doGoAway();//stop creepily watching customer eat
//		
//	}
	
	public void setCook(CookRole c) {
		this.cook = c;
	}
	@Override
	public void takeOrder(MyCustomer mc) {
		doGoToTable(mc.customer, mc.table);
		//	Do("1");
		acquireSemaphore(active);
		//	Do("ICAN");
		//doTakeOrder();
		//	Do("2");
		mc.customer.msgWhatWouldYouLike();
		//	Do("3");
		Do("What would you like customer?" + mc.customer);
		//	Do("4");
		acquireSemaphore(active);//released by customer msg
		//	Do("5");
		doGoToCook(530, 100);
		acquireSemaphore(active);
		cook.msgHereIsAnOrder(mc.choice, mc.table, mc.customer, this);
		doDisplayChoice("");
		doGoAway();		
	}
	@Override
	public void deliverOrder(Customer cust, int table, String choice) {
		Do("Beginning order delivery process");
		doGoToCook(plateAreaLocs.get(0).width, 50);
		acquireSemaphore(active);
		doGoToCook(plateAreaLocs.get(0).width, plateAreaLocs.get(0).height);
		acquireSemaphore(active);


		cook.msgNullifyPlateArea(plateAreaLocs.get(0).height);
		plateAreaLocs.remove(0);
		//		plateAreas.get(0).o = null;//TODO
		//		plateAreas.remove(0);

		doGoToCook(500, 50);
		acquireSemaphore(active);
		doGoToTable(cust, table);
		doDisplayChoice(choice);
		acquireSemaphore(active);

		Do("Here is your food " + cust);
		doDisplayChoice("none");
		cust.msgHereIsYourFood();
		//		Do("Now i should leave you " + cust);
		doGoAway();//stop creepily watching customer eat

	}
}

