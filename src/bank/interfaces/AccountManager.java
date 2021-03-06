package bank.interfaces;


public interface AccountManager {

	public abstract void msgOpenNewAccount(Teller t, BankCustomer bc, double intitialDeposit);
	
	public abstract void msgDepositMoney(Teller t, BankCustomer bc, int accountId, double amount);
	
	public abstract void msgWithdrawMoney(Teller t, BankCustomer bc, int AccountId, double amount);

	public abstract void msgGiveMeTheMoney(Robber r, double amount);
	
	public abstract void msgUpdateMoney(double amount);
	
	public abstract String getName();

	public abstract void msgAtDestination();
	
}