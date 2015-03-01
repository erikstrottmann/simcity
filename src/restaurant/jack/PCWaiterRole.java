package restaurant.jack;

import java.awt.Dimension;

import CommonSimpleClasses.CityLocation;
import CommonSimpleClasses.Constants;
import CommonSimpleClasses.ScheduleTask;
import agent.interfaces.Person;
import restaurant.jack.WaiterRoleBase.MyCustomer;
import restaurant.jack.WaiterRoleBase.customerState;
import restaurant.jack.interfaces.Customer;
import restaurant.jack.interfaces.Waiter;

public class PCWaiterRole extends WaiterRoleBase implements Waiter {

	OrderWheel orderWheel;
	
	boolean timeToCheckOrderWheel= true;
	private ScheduleTask schedule = ScheduleTask.getInstance();
	int CHECK_ORDER_WHEEL_TIME = 7;
	
	public PCWaiterRole(Person p, CityLocation c) {
		super(p, c);
		// TODO Auto-generated constructor stub
	}

	//MESSAGES

	private void checkOrderWheel() {
		
		
		Customer c = orderWheel.getCookedOrderCustomer();
		if(findCustomer(c) == null) {
			return;
		}
		
		String choice = orderWheel.getCookedOrderString();
		int table = orderWheel.getCookedOrderTabelNum();
		Dimension dim = new Dimension(500, 30);//TODO hack to test
		msgOrderIsReady(c, choice, table, dim);
		
		
		orderWheel.changeServedOrderState();
	}
	
	
	@Override
	protected boolean pickAndExecuteAnAction() {
//		print("sched check");
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
		//COOK STUFF BELOW
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
		//END COOK STUFF
		
		synchronized(myCustomers) {
			for(MyCustomer c : myCustomers) {
				if(c.state == customerState.doneEating) {
					getCheck(c);
					c.state = customerState.paying;
					return true;//TODO added return true
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
		
		if(orderWheel.getCookedOrderSize()>0){
			checkOrderWheel();
			return true;
		}
		
		if(timeToCheckOrderWheel) {
//			print("TIME CHECK");
			timeToCheckOrderWheel = false;
//			print("tititi");
			Runnable command = new Runnable() {
				@Override
				public void run() {
					stateChanged();
					timeToCheckOrderWheel = true;
				}
			};
			schedule.scheduleTaskWithDelay(command, CHECK_ORDER_WHEEL_TIME * Constants.MINUTE);
			return true;
			//does not return true to call sched again
		}
		
		return false;
	}
	
	//ACTIONS
	@Override
	public void takeOrder(MyCustomer mc) {
		doGoToTable(mc.customer, mc.table);
		acquireSemaphore(active);
	
		mc.customer.msgWhatWouldYouLike();
		Do("What would you like customer?" + mc.customer);
		acquireSemaphore(active);//released by customer msg

		doGoToOrderWheel();
		acquireSemaphore(active);
		
		orderWheel.addOrder(mc.choice, mc.table, mc.customer, this);
//		cook.msgHereIsAnOrder(mc.choice, mc.table, mc.customer, this);
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


//		cook.msgNullifyPlateArea(plateAreaLocs.get(0).height);//TODO need way to nullify
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
	
	//ANIMATION
	private void doGoToOrderWheel() {
		waiterGui.DoGoToOrderWheel();
	}
	
	//utils
	
	public void setOrderWheel(OrderWheel o) {
		orderWheel = o;
	}


	
}