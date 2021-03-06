package bank;

import gui.trace.AlertTag;

import java.util.concurrent.Semaphore;

import CommonSimpleClasses.CityLocation;
import agent.Role;
import agent.interfaces.Person;
import bank.gui.BankBuilding;
import bank.interfaces.BankCustomer;
import bank.interfaces.BankCustomerGuiInterface;
import bank.interfaces.LoanManager;
import bank.interfaces.SecurityGuard;
import bank.interfaces.Teller;

/**
 * Restaurant customer agent.
 */



//Build should not be problem
public class BankCustomerRole extends Role implements BankCustomer {
	private String name;
	
	double cashInAccount;
	
	private boolean endWorkShift = false;
	private int accountId = -1;//if -1, has not been assigned

	protected Semaphore active = new Semaphore(0, true);
	
	private int loanManagerXPos;
	
	BankCustomerGuiInterface bankCustomerGui;
	/**
	 * Constructor for CustomerAgent class
	 *
	 * @param name name of the customer
	 * @param gui  reference to the customergui so the customer can send it messages
	 */
//	PassengerRole passengerRole;

	
	public BankCustomerRole(Person person, CityLocation bank){
		super(person, bank);
		
//		passengerRole = new FakePassengerRole(fakeCityLoc);
//		this.getPerson().addRole(passengerRole);
//		this.name = name;
		state = State.enteredBank;//TODO update, if role reused, maybe change leaving to enteredBank?
//		this.getPerson().getWallet().setCashOnHand(9001);
//		person.getWallet().setCashOnHand(10);//TODO fix this, only done for testing
//		myCash.amount = 10;
//		myCash.amount = initialMoney;
//		myCash.capacity = initialMoney + 20;
//		this.getPerson().getWallet().getTooLittle() = initialMoney -20;
		
		// Set the security guard if necessary
		if (securityGuard == null && bank instanceof BankBuilding) {
			if (bank instanceof BankBuilding) {
				BankBuilding bankBuilding = (BankBuilding) bank;
				securityGuard = (SecurityGuard) bankBuilding.getGreeter();
				// hopefully this works...
			} else {
				print("Error: Bank is not a BankBuilding.");
			}
		}
	}
	
	public BankCustomerRole(Person person, CityLocation bank, int accountId, String name) { //CONSTRUCTOR USED FOR TESTING
		super(person, bank);
		state = State.enteredBank;
		this.accountId = accountId;
		if(name.equals("withdraw")){
			this.getPerson().getWallet().setCashOnHand(10);
			this.getPerson().getWallet().setTooMuch(100);
			this.getPerson().getWallet().setTooLittle(20);
//			myCash.amount = 10;
//			this.getPerson().getWallet().getTooLittle() = 20;
//			myCash.capacity = 100;
			cashInAccount = 300;
		}
		if(name.equals("deposit")){
			this.getPerson().getWallet().setCashOnHand(100);
			this.getPerson().getWallet().setTooMuch(80);
			this.getPerson().getWallet().setTooLittle(20);
//			myCash.amount = 100;
//			this.getPerson().getWallet().getTooLittle() = 20;
//			myCash.capacity = 80;
			cashInAccount = 0;
		}
		if(name.equals("loan")){
			this.getPerson().getWallet().setCashOnHand(0);
			this.getPerson().getWallet().setTooMuch(100);
			this.getPerson().getWallet().setTooLittle(10);
//			myCash.amount = 0;
//			this.getPerson().getWallet().getTooLittle() = 10;
//			myCash.capacity =100;
			cashInAccount = 0;
		}
//		myCash.amount = initialCash;
//		cashInAccount = 0;
//		this.getPerson().getWallet().getTooLittle() = initialCash - 20;
//		myCash.capacity = initialCash + 20;
	}
		
//	class Cash {
//		Cash() {
//			
//		}
//		Cash(double amount, double threshold, double capacity, double cashInAccount) {
//			this.amount = amount;
//			this.threshold = threshold;
//			this.capacity = capacity;
//			this.cashInAccount = cashInAccount;
//		}
//		double amount;
//		double threshold;
//		double capacity;
//		double cashInAccount;
//	}
	
	
	
//	Cash myCash = new Cash();// = new Cash(50, 30, 70, 0);
	public enum State {waiting, openingAccount, depositing, withdrawing, gettingLoan, atLoanManager, leaving, enteredBank, waitingAtGuard};
	enum Event {gotToTeller, accountOpened, depositSuccessful, withdrawSuccessful, sentToLoanManager, loanCompleted, goingToTeller, goingToSecurityGuard};
	
	State state;
	Event event;
	
	private double cashAdjustAmount = 0;
	double loanAmount = 0;
	private LoanManager loanManager;
	Teller teller;
	int tellerXLoc;
	SecurityGuard securityGuard;
	int guardX;
	int guardY;
	

	/**
	 * hack to establish connection to Host agent.
	 */


	public String getCustomerName() {
		return name;
	}
	
	public void activate() {
		super.activate();
		msgGoToSecurityGuard( ((BankBuilding) getLocation()).getSecurity() );
	}
	
	// Messages

	
	
	public void msgLeaveWork() {
//		endWorkShift = true;
//		stateChanged();
	}
	public void msgGoToSecurityGuard(SecurityGuard sg){
//		System.out.println("WOW SUCH SECURITY. WOW");
		securityGuard = sg;
		event = Event.goingToSecurityGuard;
		stateChanged();
	}
	
	public void msgGoToTeller(int xLoc, Teller t) {
		teller = t;
		event = Event.goingToTeller;
		tellerXLoc = xLoc;
		stateChanged();
	}
	
	public void msgGotToTeller() {
		Do(AlertTag.BANK, "msggottoteller");
//		Do("HI");
		event = Event.gotToTeller;
		stateChanged();
	}
	
	public void msgAccountOpened(int accountIdNumber) {
		event = Event.accountOpened;
		this.setAccountId(accountIdNumber);
		stateChanged();
	}
	
	public void msgDepositSuccessful(double depositAmount) {
		setCashAdjustAmount((depositAmount * -1));
		event = Event.depositSuccessful;
		stateChanged();
		
	}
	
	public void msgWithdrawSuccessful(double withdrawAmount) {
		setCashAdjustAmount(withdrawAmount);
		event = Event.withdrawSuccessful;
		stateChanged();
	}
	
	public void msgSpeakToLoanManager(LoanManager lm, int xPos) {
		setLoanManager(lm);
		loanManagerXPos = xPos;
		event = Event.sentToLoanManager;
		stateChanged();
	}
	
	public void msgLoanApproved(double amount) {
		loanAmount = amount;
		event = Event.loanCompleted;
		stateChanged();
	}
	
	public void msgAtDestination() {
//		Do("Made it");
		active.release();
	}
	

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	public boolean pickAndExecuteAnAction() {
//		Do("Entered scheduler");
//		Do("entered scheduler");
		if(state == State.enteredBank && event == Event.goingToSecurityGuard) {
			goToSecurityGuard();
			return true;
		}
		if(state == State.waitingAtGuard && event ==Event.goingToTeller) {
			goToTeller(tellerXLoc);
			return true;
		}
		if(state == State.waiting && event == Event.gotToTeller) {
			speakToTeller();
			return true;
		}
		if(state == State.openingAccount && event == Event.accountOpened) {
			leaveBank();
			return true;
		}
		if(state == State.depositing && event == Event.depositSuccessful) {
			leaveBank();
			return true;
		}
		if(state == State.withdrawing && event == Event.withdrawSuccessful) {
			leaveBank();
			return true;
		}
		if(state==State.gettingLoan && event == Event.sentToLoanManager){
			askForLoan();
			return true;
		}
		if(state==State.atLoanManager && event == Event.loanCompleted) {
			leaveBank();
			return true;
		}
		if(endWorkShift) {
			goOffWork();
			return true;
		}

		return false;
	}

	// Actions
	private void goToSecurityGuard(){
		Do(AlertTag.BANK, "going to guard");
		doGoToSecurityGuard();
		acquireSemaphore(active);
		state = State.waitingAtGuard;
		securityGuard.msgCustomerArrived(this);
	}
	
	private void goToTeller(int xLoc) {
		doGoToTeller(xLoc);
		acquireSemaphore(active);
//		Do("YO");
//		state = State.waiting;
//		msgGotToTeller();
		speakToTeller();
	}
	
	protected void speakToTeller() {
		Do(AlertTag.BANK, "speaking to teller");//TODO check that moneyNeeded is money needed ON TOP OF money I have, not just amount of expensive item

		
		if(accountId == -1) {//have not been assigned accountID yet
			Do(AlertTag.BANK, "need to open account");
			double initialDepositAmount = 10;//this.getPerson().getWallet().getCashOnHand() * .2;
			
			if(this.getPerson().getWallet().getCashOnHand() < initialDepositAmount) {//is too poor/worthless to afford initialdeposit
				teller.msgINeedALoan(this);
				state= State.gettingLoan;
				return;
			}
			teller.msgIWantToOpenAccount(this, initialDepositAmount);//TODO does this work or constant?
			state = State.openingAccount;
			setCashAdjustAmount(-initialDepositAmount);//TODO for testing
//			Do("made it here");
			return;
		}
		if(this.getPerson().getWallet().getMoneyNeeded() > 0 && cashInAccount > this.getPerson().getWallet().getMoneyNeeded()) { //if i need money and have enough in my account, i will withdraw it
			double moneyNeeded = this.getPerson().getWallet().getMoneyNeeded();
			Do(AlertTag.BANK, "I'm withdrawing needed money");
			teller.msgWithdrawMoney(this, accountId, moneyNeeded);//TODO for testing
			state= State.withdrawing;
			return;
		}
		if(this.getPerson().getWallet().getMoneyNeeded() > 0 && cashInAccount < this.getPerson().getWallet().getMoneyNeeded()) {//if i need money and dont have enough in account, i will take a loan for ALL OF IT (NOTE: possibly make it possible to withdraw some and get rest in loan?
			teller.msgINeedALoan(this);
			state= State.gettingLoan;
			return;
		}
		if(this.getPerson().getWallet().getCashOnHand() < myTooLittle() && cashInAccount > ((myTooLittle() + myTooMuch())/2)){
			Do(AlertTag.BANK, "I'm withdrawing");
			double withdrawAmount = (myTooLittle() + myTooMuch())/2;
			teller.msgWithdrawMoney(this, accountId, withdrawAmount);
			state= State.withdrawing;
			return;
		}
		if(this.getPerson().getWallet().getCashOnHand() > myTooMuch()) {
			Do(AlertTag.BANK, "Im depositing");
			double depositAmount = myCash() - ((myTooMuch() + myTooLittle())/2);
			teller.msgDepositMoney(this, accountId, depositAmount); 
			state = State.depositing;
			return;
		}
		if(this.getPerson().getWallet().getCashOnHand() < myTooLittle() && cashInAccount< ((myTooLittle() + myTooMuch())/2)) {
			Do("Im getting Loan");
			teller.msgINeedALoan(this);
			state= State.gettingLoan;
			return;
		}
		Do(AlertTag.BANK, "ERROR, no condtion met");//not good
	}
	

	
	private void askForLoan() {//TODO check that moneyNeeded is money needed ON TOP OF money I have, not just amount of expensive item
		Do(AlertTag.BANK, "asking for loan");
		doGoToLoanManager(loanManagerXPos);
		acquireSemaphore(active);
		if(this.getPerson().getWallet().getMoneyNeeded() == 0) {
			getLoanManager().msgINeedALoan(this, ((myTooLittle() + myTooMuch())/2)); //gets loan to get them in safe money range for them
		}
		if(this.getPerson().getWallet().getMoneyNeeded() > 0) {//has money needed for something
			
			getLoanManager().msgINeedALoan(this, this.getPerson().getWallet().getMoneyNeeded());
			this.getPerson().getWallet().setMoneyNeeded(0);//TODO check
		}
		state = State.atLoanManager;
	}
	
	protected void leaveBank() {
		Do(AlertTag.BANK, "leaving bank " + accountId);
//		this.getPerson().getWallet().setCashOnHand(this.getPerson().getWallet().getCashOnHand() + getCashAdjustAmount());
		this.getPerson().getWallet().addCash(getCashAdjustAmount());
		this.getPerson().getWallet().addCash(loanAmount);
//		this.getPerson().getWallet().setCashOnHand(this.getPerson().getWallet().getCashOnHand() + loanAmount);
//		myCash.amount += loanAmount;
		cashInAccount -= getCashAdjustAmount();
		setCashAdjustAmount(0);
		loanAmount = 0;
		state = State.leaving;
		securityGuard.msgLeavingBank(this);
		doLeaveBank();
		acquireSemaphore(active);
		
		state = State.enteredBank;
		this.deactivate();
	}
	
	private void goOffWork() {
//		doEndWorkDay();
//		acquireSemaphore(active);
//		this.deactivate();
		
	}

	//ANIMATION##########################
	private void doGoToSecurityGuard() {
		bankCustomerGui.DoGoToSecurityGuard(this.getPerson().getWallet().getCashOnHand(), cashInAccount);
	}
	private void doGoToLoanManager(int x) {
		bankCustomerGui.DoGoToLoanManager(x, this.getPerson().getWallet().getCashOnHand(), cashInAccount);
	}
	protected void doLeaveBank() {
		bankCustomerGui.DoLeaveBank(this.getPerson().getWallet().getCashOnHand(), cashInAccount);
	}
	private void doGoToTeller(int xLoc) {
		bankCustomerGui.DoGoToTeller(xLoc, this.getPerson().getWallet().getCashOnHand(), cashInAccount);
	}
	private void doEndWorkDay() {
		bankCustomerGui.DoEndWorkDay();
	}
	
	
	// Accessors, etc.

	public double getStrangeAmount() {
		 return (myTooLittle() + myTooMuch())/2;
	}
	
	public void addTeller(TellerRole t) {
		teller = t;
	}
	
	private double myCash() {
		return this.getPerson().getWallet().getCashOnHand();
	}
	
	private double myTooLittle() {
		return this.getPerson().getWallet().getTooLittle();
	}
	
	private double myTooMuch() {
		return this.getPerson().getWallet().getTooMuch();
	}
	
	private double myMoneyNeeded() {
		return this.getPerson().getWallet().getMoneyNeeded();
	}
	
	public void setGui(BankCustomerGuiInterface b) {
		bankCustomerGui = b;
	}
	
	public void setTeller(Teller t) {//TODO this is a hack
		teller = t;
	}
	public double getWalletAmount() {
		return this.getPerson().getWallet().getCashOnHand();//myCash.amount;
	}
	public void setWalletAmount(double amount) {
		this.getPerson().getWallet().setCashOnHand(amount);
//		myCash.amount = amount;
	}
	public double getWalletCapacity() {
		return this.getPerson().getWallet().getTooMuch();
//				myCash.capacity;
	}
	public double getWalletThreshold() {
		return this.getPerson().getWallet().getTooLittle();
	}
	public void setMoney(double money) {
		this.getPerson().getWallet().setCashOnHand(money);
//		myCash.amount = money;
		this.getPerson().getWallet().setTooMuch(money+20);
//		myCash.capacity = money+20;
		this.getPerson().getWallet().setTooLittle(money - 20);// = money-20;
	}

	public void setAccountAmount(double money) {
		cashInAccount = money;
	}
	
	public double getAccountAmount() {
		return cashInAccount;
	}
	
	public State getState() {
		return state;
	}
	
	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public LoanManager getLoanManager() {
		return loanManager;
	}

	public void setLoanManager(LoanManager loanManager) {
		this.loanManager = loanManager;
	}

	public double getCashAdjustAmount() {
		return cashAdjustAmount;
	}

	public void setCashAdjustAmount(double cashAdjustAmount) {
		this.cashAdjustAmount = cashAdjustAmount;
	}
	
	public void acquireSemaphore(Semaphore s) {
		try {
			s.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void Do(String str){
		Do(AlertTag.BANK, str);
	}



//	@Override
//	public void msgFollowMeToTable(Waiter w, int x, int y, Map<String, Double> m) {
//		// TODO Auto-generated method stub
//		
//	}
}

