package restaurant.jack;

import gui.trace.AlertTag;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.Semaphore;

import restaurant.jack.gui.CashierGui;
import restaurant.jack.interfaces.Cashier;
import restaurant.jack.interfaces.Customer;
import restaurant.jack.interfaces.Market;
import restaurant.jack.interfaces.Waiter;
import restaurant.strottma.CashierRole.MyBill;
import CommonSimpleClasses.CityLocation;
import agent.WorkRole;
import agent.interfaces.Person;
//import restaurant.Customer.CustomerEvent;
//import restaurant.HostAgent.Table;

/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class CashierRole extends WorkRole implements Cashier {
	//static final int NTABLES = 3;//a global for the number of tables.
	//Notice that we implement waitingCustomers using ArrayList, but type it
	//with List semantics.
	//public List<Customer> waitingCustomers
	//= new ArrayList<Customer>();
	//public Collection<Table> tables;
	//private boolean isAtDesk = true;
	//note that tables is typed with Collection semantics.
	//Later we will see how it is implemented

	private String name;

	public CashierGui cashierGui = null;
	
	private double money = 500;
	
	boolean atWork;
	
	boolean endWorkDay = false;
	
	private Semaphore active = new Semaphore(0);//overall semaphore


//	boolean marketPaymentRequested = false;//handles paying market

	
	public static class MarketPayment {
		MarketPayment(Market m, double a) {
			setMarket(m);
			setAmount(a);
		}
		
		public Market getMarket() {
			return market;
		}
		public void setMarket(Market market) {
			this.market = market;
		}

		public double getAmount() {
			return amount;
		}

		public void setAmount(double amount) {
			this.amount = amount;
		}

		private Market market;
		private double amount;
	}
	 private List<MarketPayment> marketPayments = Collections.synchronizedList(new ArrayList<MarketPayment>());


	public enum customerState {doneEating, waitingForCheck, paying, leaving};
	public static class MyCustomer {
		MyCustomer(Customer customer, Waiter waiter, String choice, customerState state, double payment) {
			this.setCustomer(customer);
			this.waiter = waiter;
			this.setChoice(choice);
			this.setState(state);
			this.setPayment(payment);
		}
		
		public Customer getCustomer() {
			return customer;
		}
		public void setCustomer(Customer customer) {
			this.customer = customer;
		}
		public Waiter getWaiter() {
			return waiter;
		}

		public String getChoice() {
			return choice;
		}

		public void setChoice(String choice) {
			this.choice = choice;
		}

		public double getPayment() {
			return payment;
		}

		public void setPayment(double payment) {
			this.payment = payment;
		}

		public customerState getState() {
			return state;
		}

		public void setState(customerState state) {
			this.state = state;
		}

		private Customer customer;
		Waiter waiter;
		private String choice;
		private customerState state;
		private double payment;
		
		
	}
	
	
//	List<Order> Orders = new ArrayList<Order>();
	Timer CookTimer;
	
	private List<MyCustomer> myCustomers = Collections.synchronizedList(new ArrayList<MyCustomer>());
	private Map<String, Double> priceMenu = new HashMap<String, Double>();

	public class MyBill {
		private double amount;
		private market.interfaces.Cashier cashier;
		
		public MyBill(double amount, market.interfaces.Cashier cashier) {
			this.amount = amount;
			this.cashier = cashier;
		}
		
		public double getAmount() { return amount; }
		public void setAmount(double amount) { this.amount = amount; }
		
		public market.interfaces.Cashier getCashier() { return cashier; }
	}
	
	private List<MyBill> bills = Collections.synchronizedList(new ArrayList<MyBill>());
	DecimalFormat df = new DecimalFormat("#.##");
	
	
	public CashierRole(Person p, CityLocation c) {
		super(p, c);
		
		atWork = false;
		
		getPriceMenu().put("Krabby Patty", 15.99);//populates pricemenu map
		getPriceMenu().put("Kelp Shake", 10.99);
		getPriceMenu().put("Coral Bits", 5.99);
		getPriceMenu().put("Kelp Rings", 8.99);

		this.CookTimer = new Timer();
	}

	/**
	 * constructor used for JUnit testing
	 * @param string
	 */
	public CashierRole(String string) {
		getPriceMenu().put("Krabby Patty", 15.99);//populates pricemenu map
		getPriceMenu().put("Kelp Shake", 10.99);
		getPriceMenu().put("Coral Bits", 5.99);
		getPriceMenu().put("Kelp Rings", 8.99);
	}


	public String getMaitreDName() {
		return name;
	}

	public String getName() {
		return name;
	}


	// Messages ################################################
	//##########################################################
	public void msgComputeBill(Customer c, String choice, Waiter w) { //from waiter
		myCustomers.add(new MyCustomer(c, w, choice, customerState.doneEating, 0));
		stateChanged();
		
	}
	public void msgHereIsMyPayment(Customer c, double cash) { //from customer
		MyCustomer mc = findCustomer(c);
		mc.setPayment(cash);
		mc.setState(customerState.paying);
		stateChanged();
	}
	public void msgRequestPayment(Market m, double amount) {//request from market for payment upon delivery
//		marketPaymentAmount = amount;
		marketPayments.add(new MarketPayment(m, amount));
		stateChanged();
	}
	

	@Override
	public void msgHereIsYourTotal(double total,
			market.interfaces.Cashier cashier) {
		Do(AlertTag.RESTAURANT, "Received a bill from " + cashier);
		bills.add(new MyBill(total, cashier));
		stateChanged();		
	}



	/**
	 * Scheduler.  Determine what action is called for, and do it. ############################################################
	 */
	public boolean pickAndExecuteAnAction() {
		
		if(!atWork) {
			goToWork();
			return true;
		}
		
		if (money != 0) {
			synchronized (bills) {
				for (MyBill bill : bills) {
					payBill(bill);
					return true;
				}
			}
		}
		
		synchronized(myCustomers) {
			for(MyCustomer mc : myCustomers) {
				if(mc.getState()== customerState.doneEating) {
					calculateBill(mc);
					mc.setState(customerState.waitingForCheck);
					return true;
				}
			}
		}
		
		synchronized(myCustomers) {
			for(MyCustomer mc: myCustomers) {
				if(mc.getState() == customerState.paying) {
					calculateChange(mc);
					return true;
				}
			}
		}
		
		if(!marketPayments.isEmpty()) {
			//action pay market
			payMarket();
			return true;
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

	// Actions ///////////////////
	
	private void payBill(MyBill bill) {
		if (money >= bill.amount) {
			Do(AlertTag.RESTAURANT, "Paying $" + df.format(bill.amount) +
					" to " + bill.cashier.getName());
			bill.cashier.msgHereIsPayment(bill.amount, this);
			money -= bill.amount;
			bills.remove(bill);
			
		} else {
			// extra credit
			Do(AlertTag.RESTAURANT, "Cannot pay the market in full. "
					+ "Paying as much as I can...");
			bill.cashier.msgHereIsPayment(money, this);
			money = 0.00;
			bill.amount -= money;
		}
	}
	
	private void endWorkDay() { 
		goOffWork();
		atWork = false;
		endWorkDay = false;
		this.deactivate();
	}

	private void goOffWork() {
		addPaycheckToWallet();
		leaveWork();
	}
	
	private void leaveWork() {
		cashierGui.DoLeaveRestaurant();
		Do(AlertTag.RESTAURANT, "" +active.availablePermits());
	}
	

	
	private void addPaycheckToWallet() {
		this.getPerson().getWallet().addCash(300.0);
	}
	
	private void goToWork() {
		cashierGui.setName(person.getName());
		cashierGui.DoGoToDesk();
		Do(AlertTag.RESTAURANT, "" +active.availablePermits());
		acquireSemaphore(active);
		atWork = true;
	}
	
	private void acquireSemaphore(Semaphore a) {
		try {
			Do(AlertTag.RESTAURANT, "" +a.availablePermits());
			a.acquire();
		} catch (InterruptedException e) {
			print("AHHHHHHHHHH");
			e.printStackTrace();
		}
	}


	private void calculateBill(MyCustomer mc) {
		Do("calculateBill");
		cashierGui.DoDirectAWaiter();
		acquireSemaphore(active);
		cashierGui.DoGoToDesk();
		acquireSemaphore(active);
		double price = getPriceMenu().get(mc.getChoice());
		mc.waiter.msgHereIsCheck(mc.getCustomer(), price);
	}
	
	private void calculateChange(MyCustomer mc) {
		double price = getPriceMenu().get(mc.getChoice());
		double change = mc.getPayment() - price;
		money += mc.getPayment();
		//NOW check if covers bill for non-norm scenarios!
		mc.getCustomer().msgHereIsChange(change);
		mc.setState(customerState.leaving);
	}
	private void payMarket() {
		MarketPayment temp = marketPayments.get(0);
		
		if(temp.getAmount() <= getMoney()) {//if cashier has money to pay market
			Do("Paying Market for delivery: $" + temp.getAmount());
			temp.getMarket().msgHereIsPayment(temp.getAmount());
			setMoney(getMoney() - temp.getAmount());
			marketPayments.remove(0);
		}
		else {//if cashier doesn't have enough money, gives all he has
			Do("Cannot afford to pay Market for delivery, instead giving all I have: $" + getMoney());
			temp.getMarket().msgHereIsPayment(getMoney());
			setMoney(0);
			marketPayments.remove(0);
		}
	}


	//utilities
	public MyCustomer findCustomer(Customer c) {
		for (MyCustomer mc : myCustomers) {
			if(mc.getCustomer() == c) {
				return mc;
			}
		
		}
		 return null;
	}

	public void setGui(CashierGui gui) {
		cashierGui = gui;
	}
	
	public List<MyCustomer> getMyCustomers() {
		return myCustomers;
	}

	public CashierGui getGui() {
		return cashierGui;
	}


	public double getMoney() {
		return money;
	}


	public void setMoney(double money) {
		this.money = money;
	}
	
	public List<MarketPayment> getMarketPayments() {
		return marketPayments;
	}


	public Map<String, Double> getPriceMenu() {
		return priceMenu;
	}


	public void setPriceMenu(Map<String, Double> priceMenu) {
		this.priceMenu = priceMenu;
	}
	
	public void msgAtDestination() {
		active.release();
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

	public double getRestaurantMoney() {
		return money;
	}
	
	public void setRestaurantMoney(double amount) {
		this.money = amount;
	}

	@Override
	public void msgLeaveWork() {
		endWorkDay = true;
		stateChanged();
	}
	
	@Override
	public void activate() {
		super.activate();
		this.getPerson().setShouldDepositRestaurantMoney(true);
	}
	
	@Override
	public void deactivate() {
		super.deactivate();
	}
	
}

	

