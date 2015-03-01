package restaurant.jack;

import java.util.ArrayList;
import java.util.List;

import org.omg.PortableServer.POAManagerPackage.State;

import restaurant.jack.OrderWheel.Order.state;
import restaurant.jack.interfaces.Customer;
import restaurant.jack.interfaces.Waiter;

public class OrderWheel {
	
	
	
	public static class Order
	{
		Order(String choice, int table, Customer customer, Waiter w, state s) {
			Choice = choice;
			this.table = table;
			cust = customer;
			waiter = w;
			orderState = s;
		}
		Customer cust;
		int table;
		public String Choice;
		Waiter waiter;
		public enum state {none, received, cooking, cooked, givenToWaiter, completed};
		private state orderState;
		
	};
	
	List<Order> orderList = new ArrayList<Order>();
	
	public OrderWheel() {
		
	}
	
	public void addOrder(String choice, int tableNum, Customer c, Waiter w){
		orderList.add(new Order(choice, tableNum, c, w, state.received));
		
	}
	
	public Order getTopOrder() {
		return orderList.get(0);
	}
	
	public int getOrderListSize() {
		return orderList.size();
	}
	
	/**
	 * used by cook to see if list has orders that need
	 * to be cooked
	 */
	public int getReceivedOrdersSize() {
		int num =0;
		for(Order o : orderList) {
			if(o.orderState == state.received) {
				num ++;
			}
		}
		return num;
	}
	
	public Order getReceivedOrder() {
		for(Order o : orderList) {
			if(o.orderState == state.received) {
				return o;
			}
		}
		return null;//problem occurred
	}
	
	public String getReceivedOrderString() {
		for(Order o : orderList) {
			if(o.orderState == state.received) {
				return o.Choice;
			}
		}
		return null;//problem occurred
	}
	
	public int getReceivedOrderTabelNum() {
		for(Order o : orderList) {
			if(o.orderState == state.received) {
				return o.table;
			}
		}
		return -1;//problem occurred
	}
	
	public Customer getReceivedOrderCustomer() {
		for(Order o : orderList) {
			if(o.orderState == state.received) {
				return o.cust;
			}
		}
		return null;//problem occurred
	}
	
	public Waiter getReceivedOrderWaiter() {
		for(Order o : orderList) {
			if(o.orderState == state.received) {
				return o.waiter;
			}
		}
		return null;//problem occurred
	}
	
	public void changeReceivedOrderState() {
		for(Order o : orderList) {
			if(o.orderState == state.received) {
				o.orderState = state.cooking;
			}
		}
	}
	public void changeCookingOrderState(Customer c) {
		for(Order o : orderList) {
			if(o.cust == c) {
				o.orderState = state.cooked;
			}
		}
	}
	
	public int getNoneOrderStateSize() {
		int num =0;
		for(Order o : orderList) {
			if(o.orderState == state.none) {
				num ++;
			}
		}
		return num;
	}
	
	public Customer getNoneOrderStateCustomer() {
		for(Order o : orderList) {
			if(o.orderState == state.none) {
				
				o.orderState = state.completed;
				return o.cust;
			}
		}
		return null;//bad things happened
	}
	
	//USED BY PCWAITER TO TELL WHEN TO CHECK SCHED
	public int getCookedOrderSize(){
		int num =0;
		for(Order o : orderList) {
			if(o.orderState == state.cooked) {
				num ++;
			}
		}
		return num;
	}
	
	public void changeServedOrderState() {
		for(Order o : orderList) {
			if(o.orderState == state.cooked) {
				o.orderState = state.none;//Served to customer
			}
		}
	}
	
	public String getCookedOrderString() {
		for(Order o : orderList) {
			if(o.orderState == state.cooked) {
				return o.Choice;
			}
		}
		return null;//problem occurred
	}
	
	public int getCookedOrderTabelNum() {
		for(Order o : orderList) {
			if(o.orderState == state.cooked) {
				return o.table;
			}
		}
		return -1;//problem occurred
	}
	
	public Customer getCookedOrderCustomer() {
		for(Order o : orderList) {
			if(o.orderState == state.cooked) {
				return o.cust;
			}
		}
		return null;//problem occurred
	}
}