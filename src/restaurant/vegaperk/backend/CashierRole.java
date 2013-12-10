package restaurant.vegaperk.backend;

import CommonSimpleClasses.CityBuilding;
import agent.WorkRole;
import agent.interfaces.Person;
import gui.trace.AlertTag;

import java.text.DecimalFormat;
import java.util.*;

import restaurant.vegaperk.gui.CashierGui;
import restaurant.vegaperk.interfaces.Cashier;
import restaurant.vegaperk.interfaces.Customer;
import restaurant.vegaperk.interfaces.Waiter;
import mock.EventLog;

/**
 * Cook Agent
 */
public class CashierRole extends WorkRole implements Cashier {
	String name;
	private double money = 500.00;
	
	public EventLog log = new EventLog();
	
	private DecimalFormat df = new DecimalFormat("#.##");
	
	private boolean leaveWork = false;
	
	private CashierGui gui;
	
	private List<MyCustomer> customers = Collections.synchronizedList(new ArrayList<MyCustomer>());
	private List<MyBill> bills = Collections.synchronizedList(new ArrayList<MyBill>());
	
	// create an anonymous Map class to initialize the foods and cook times

	public CashierRole(Person person, CityBuilding building) {
		super(person, building);
		
		this.customers = Collections.synchronizedList(new ArrayList<MyCustomer>());
	}

	/** Accessor and setter methods */
	public String getName() {
		return name;
	}
	
	/** Messages from other agents */
	
	/** From WaiterAgent */
	public void msgDoneEating(Customer c, double b, Waiter w){
		MyCustomer mc = findCustomer(c);
		if(mc == null){
			getCustomers().add(new MyCustomer(c, w, b));
		}
		else{
			mc.waiter = w;
			mc.setPayment(0);
			mc.setBill(mc.getBill() + b);
			mc.state = CustomerState.DONE_EATING;
		}
		stateChanged();
	}
	
	/** From Customer */
	public void msgHereIsPayment(Customer c, double p){
		Do("Received customer payment");
		MyCustomer mc = findCustomer(c);
		mc.setPayment(p);
		mc.state = CustomerState.PAID;
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	public boolean pickAndExecuteAnAction(){
		synchronized(bills){
			for(MyBill bill : bills){
				if(bill.state == BillState.PENDING){
					payMarket(bill);
					return true;
				}
			}
		}
		
		synchronized(customers){
			for(MyCustomer c : getCustomers()){
				if(c.state==CustomerState.DONE_EATING){
					calculateCheck(c);
					return true;
				}
			}
			for(MyCustomer c : getCustomers()){
				if(c.state==CustomerState.PAID){
					giveChange(c);
					return true;
				}
			}
		}
		return false;
	}
	
	/** Actions */
	private void calculateCheck(MyCustomer c){
		Do("Calculating bill. Bill is " + c.getBill());
		c.waiter.msgHereIsCheck(c.getCustomer(), c.getBill());
		c.state = CustomerState.BILLED;
	}
	private void giveChange(MyCustomer c){
		double change = c.getPayment() - c.getBill();
		
		if(change >= 0){
			Do("Giving change " + change);
			c.getCustomer().msgHereIsChange(change);
			c.setBill(0.00);
		}
		else{
			Do("Still owe " + df.format(Math.abs(change)));
			c.getCustomer().msgHereIsChange(0.00);
		}
		c.state = CustomerState.DONE;
		stateChanged();
	}
	
	
	private void payMarket(MyBill bill){
		bill.state = BillState.DONE;
		if(money >= bill.getAmount()){
			Do("Paying $" + bill.amount + " to " + bill.cashier.getName());
			bill.cashier.msgHereIsPayment(bill.getAmount(), this);
			money -= bill.getAmount();
		}
		else{
			Do("Don't have enough money to pay bill! Paid all my money " + money);
			bill.cashier.msgHereIsPayment(money, this);
			money = 0;
			bill.amount -= money;
		}
		bills.remove(bill);
	}
	
	/** Utilities */
	private MyCustomer findCustomer(Customer c){
		synchronized(getCustomers()){
			for(MyCustomer mc : getCustomers()){
				if(mc.getCustomer() == c){
					return mc;
				}
			}
		}
		return null;
	}
	
	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}

	public List<MyBill> getBills() {
		return bills;
	}

	public void setBills(List<MyBill> bills) {
		this.bills = bills;
	}

	public List<MyCustomer> getCustomers() {
		return customers;
	}

	public void setCustomers(List<MyCustomer> customers) {
		this.customers = customers;
	}

	/** Classes */
	public enum CustomerState { DONE_EATING, BILLED, PAID, DONE };
	public class MyCustomer{
		private Customer customer;
		Waiter waiter;
		private double bill;
		private double payment;
		public CustomerState state;
		
		MyCustomer(Customer c, Waiter w, double b){
			setCustomer(c);
			waiter = w;
			setBill(b);
			setPayment(0);
			state = CustomerState.DONE_EATING;
		}

		public double getPayment() {
			return payment;
		}

		public void setPayment(double payment) {
			this.payment = payment;
		}

		public double getBill() {
			return bill;
		}

		public void setBill(double bill) {
			this.bill = bill;
		}

		public Customer getCustomer() {
			return customer;
		}

		public void setCustomer(Customer customer) {
			this.customer = customer;
		}
	}

	enum BillState { PENDING, DONE }
	public class MyBill{
		private double amount;
		private market.interfaces.Cashier cashier;
		BillState state;
		
		MyBill(double a, market.interfaces.Cashier c){
			setAmount(a);
			this.cashier = cashier;
		}
		
		public double getAmount() {
			return amount;
		}

		public void setAmount(double amount) {
			this.amount = amount;
		}
		
		public market.interfaces.Cashier getCashier() {
			return cashier;
		}
	}
	
	/* --- Animation Routines --- */
	private void DoLeaveWork() {
		gui.DoLeaveWork();
		waitForInput();
	}
	
	private void DoGoHome() {
		gui.DoGoHome();
		waitForInput();
	}
	
	@Override
	protected void Do(String msg) {
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
	public void msgLeaveWork() {
		leaveWork = true;
	}
	
	@Override
	public void activate() {
		super.activate();
		DoGoHome();
		stateChanged();
	}

	@Override
	public void msgAtDestination() {
		doneWaitingForInput();
		stateChanged();
	}
	
	public void setGui(CashierGui gui) {
		this.gui = gui;
	}

	@Override
	public void msgHereIsYourTotal(double total,
			market.interfaces.Cashier cashier) {
		Do("Received a bill from " + cashier);
		bills.add(new MyBill(total, cashier));
		stateChanged();
	}
}