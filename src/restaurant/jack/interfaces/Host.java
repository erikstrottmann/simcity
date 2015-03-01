package restaurant.jack.interfaces;

public interface Host {
	public abstract void msgIWantFood(Customer c);
	
	public abstract void msgLeavingTable(Customer c);
	
	public abstract void msgIdLikeToGoOnBreak(Waiter w);
	
	public abstract void msgNoCustomers(Waiter w);
	
	public abstract void msgReturningToWork(Waiter w);
	
}