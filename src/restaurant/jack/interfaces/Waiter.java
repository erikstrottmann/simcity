package restaurant.jack.interfaces;

import java.awt.Dimension;




/**
 * A sample Customer interface built to unit test a CashierAgent.
 *
 * @author Jack Lucas
 *
 */
public interface Waiter {
	public abstract void msgAtDestination();
	
	public abstract void msgSitAtTable(Customer c, int table);
	
	public abstract void msgSetBreakBit();
	
	public abstract void msgImReadyToOrder(Customer c);
	
	public abstract void msgHereIsMyChoice(Customer c, String choice);
	
//	public abstract void msgOutOfFood(int tableNum, String choice);
	
//	public abstract void msgOrderIsReady(Customer c, String choic, int table, Dimension p);
	
	public abstract void msgReadyToPay(Customer c);
	
	public abstract void msgHereIsCheck(Customer c, double check);
	
	public abstract void msgLeavingTable(Customer c);
	
	public abstract void msgGoOnBreak();
	
	public abstract String getName();
	
	
}