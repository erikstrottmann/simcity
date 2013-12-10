package restaurant.anthony.interfaces;

import java.util.List;

import restaurant.anthony.CookRole;
import restaurant.anthony.CustomerRole;
import restaurant.anthony.HostRole;
import restaurant.anthony.WaiterRole;
import restaurant.anthony.CashierRole.Check;
import restaurant.anthony.WaiterRole.Order;
import restaurant.anthony.gui.WaiterGui;

public interface Waiter {

	public abstract String getMaitreDName();

	public abstract String getName();

	public abstract List getMyCustomers();

	public abstract void AskForPermission();

	public abstract void msgLeavingTable(Customer cust);

	public abstract void SitAtTable(CustomerRole cust, int table);

	public abstract void HeresMyChoice(Customer cust, String CH);

	public abstract void OrderIsReady(Order o);

	public abstract void msgNoMoreFood(String choice, Order o);

	public abstract void AskForCheck(Customer customer);

	public abstract void HereIsCheck(Check ch);

	public abstract void msgAtTable(int tnumb);

	public abstract void msgIdle();

	public abstract void msgAtCook();

	public abstract void msgAtCashier();

	public abstract void GoBreak(boolean permission);

	public abstract boolean IsOnBreak();

	public abstract void setGui(WaiterGui gui);

	public abstract WaiterGui getGui();

	public abstract void setHost(HostRole ho);

	public abstract void setCook(CookRole co);

	public abstract void setCashier(Cashier ca);

	public abstract void msgAtWaitingLine();

	public abstract int getWaiterNumber();

	public abstract void msgAtHome();

	public abstract void AtExit();

}