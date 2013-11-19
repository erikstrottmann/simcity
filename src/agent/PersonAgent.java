package agent;

import java.util.ArrayList;
import java.util.List;
import agent.Role;

/**
 * Person Agent.
 * 
 * @author Erik Strottmann
 */
public class PersonAgent extends Agent {
	private String name;
	private int hunger; // lower values are hungrier - TODO use byte instead of int? or maybe an enum?
	List<Role> roles;
	Watch watch;
	private Wallet wallet;
	
	PersonEvent event;
	
	public PersonAgent(String name){
		super();
		
		this.name = name;
		this.roles = new ArrayList<Role>();
		
		// TODO Add meaningful defaults for wallet and watch
		this.wallet = new Wallet(20.00, 50.00, 10.00);
		this.watch = new Watch();
	}
	
	/* -------- Messages -------- */
	/* From an ActionListener on a Timer (probably the JFrame). The ActionListner updates all Persons every time the timer goes off. See the note below for more info. */
	void msgUpdateTime(Watch w) {
		this.watch = w;	
		stateChanged();
	}

	// from PassengerRole
	void msgArrivedAtDestination() {
		event = PersonEvent.ARRIVED_AT_LOCATION;
		stateChanged();
	}
	
	/* -------- Scheduler -------- */
	/**
	 * Scheduler - determine what action is called for, and do it.
	 */
	@Override
	protected boolean pickAndExecuteAnAction() {
		// First, the Role rules.
		for (Role r : roles) {
			if (r.isActive() && r.pickAndExecuteAnAction()) {
				return true;
				// Note: only one role should be active at a time
			}
		}

		// Next, make sure you're not already doing something
		
		if (event == PersonEvent.ARRIVED_AT_LOCATION) {
			// TODO uncomment scheduler
			// activateRole(getPassengerRole().getLocation());
		}
		

		// If not, decide what to do next.
		
		if (workStartsSoon()) {
			goToWork();
			return true;
		} else if (isStarving()) {
			if (wantToEatOut()) {
				// Restaurant r = chooseRestaurant();
				// goToRestaurant(r);
				return true;
			} else if (thereIsFoodAtHome()) {
				goHome();
				return true;
			} else {
				// Market m = chooseMarket();
				// goToMarket(m);
				return true;
			}
		} else if (needToGoToBank()) {
			// Bank b = chooseBank();
			// goToBank(b);
			return true;
		}

		// No actions were performed.
		return false;
	}

	/* -------- Actions -------- */
	void goHome() {
		// TODO implement goHome()
	}
	void goToWork() {
		// TODO implement goToWork()
	}
	/*
	void goToRestaurant(Restaurant r) {
		// TODO implement goToRestaurant
	}
	*/
	/*
	void goToMarket(Market m) {
		// TODO implement goToMarket
	}
	*/
	/*
	void goToBank(Bank b) {
		// TODO implement goToBank
	}
	*/
	
	/* -------- Utilities -------- */

	public String getName() {
		return name;
	}
	
	public String toString() {
		return "person " + getName();
	}
	
	public void addRole(Role r) {
		this.roles.add(r);
	}

	public Wallet getWallet() {
		return this.wallet;
	}
	
	// TODO Implement a general PersonGui?
	/*
	public void setGui(PersonGui g) {
		personGui = g;
	}

	public PersonGui getGui() {
		return personGui;
	}
	*/
	
	/**
	 * Returns the PersonAgent's PassengerRole, or the first one if there's more
	 * than one for some reason.
	 * 
	 * @return the PassengerRole; null if none exists
	 */
	/*
	public PassengerRole getPassengerRole() {
		// TODO implement getPassengerRole
		for (Role r : roles) {
			if (r instanceof PassengerRole) {
				return r;
			}
		}
		return null;
	}
	*/

	/**
	 * Returns the PersonAgent's ResidentRole, or the first one if there's more
	 * than one for some reason.
	 * 
	 * @return the ResidentRole; null if none exists
	 */
	/*
	public ResidentRole getResidentRole() {
		// TODO uncomment when ResidentRole is available
		for (Role r : roles) {
			if (r instanceof ResidentRole) {
				return r;
			}
		}
		return null;
	}
	*/
	
	/*
	Restaurant chooseRestaurant() {
		// TODO implement chooseRestaurant()
	}
	*/
	
	/*
	Market chooseMarket() {
		// TODO implement chooseRestaurant()
	}
	*/
	
	/*
	Bank chooseBank() {
		// TODO implement chooseBank()
	}
	*/
	
	boolean workStartsSoon() {
		// TODO implement workStartsSoon
		return false;
	}
	
	boolean isStarving() {
		// TODO implement isStarving
		return false;
	}
	
	boolean wantToEatOut() {
		// TODO implement wantToEatOut
		return true;
	}
	
	
	boolean thereIsFoodAtHome() {
		// TODO uncomment when ResidentRole is available
		/*
		return getResidentRole().thereIsFoodAtHome();
		*/
		return false;
	}
	
	boolean needToGoToBank() {
		return wallet.hasTooMuch() || wallet.hasTooLittle();
	}
	
	enum PersonEvent {NONE, ARRIVED_AT_LOCATION}
	
	class Watch {
		// Contains information about time. Updated by msgUpdateTime.
		public Watch() {
			// TODO implement watch
		}
	}
	
	public class Wallet {
		private double cashOnHand;
		private double tooMuch;
		private double tooLittle;
		
		/**
		 * @param cashOnHand
		 * @param tooMuch when cashOnHand > tooMuch, Person goes to bank
		 * @param tooLittle when cashOnHand < tooLittle, Person goes to bank
		 */
		public Wallet(double cashOnHand, double tooMuch, double tooLittle) {
			this.cashOnHand = cashOnHand;
			this.tooMuch = tooMuch;
			this.tooLittle = tooLittle;
		}
		
		public double getCashOnHand() { return this.cashOnHand; }
		public double getTooMuch() { return this.tooMuch; }
		public double getTooLittle() { return this.tooLittle; }
		
		public void setCashOnHand(double coh) { this.cashOnHand = coh; }
		public void setTooMuch(double tm) { this.tooMuch = tm; }
		public void setTooLittle(double tl) { this.tooLittle = tl; }
		
		public boolean hasTooMuch() {
			return cashOnHand > tooMuch;
		}
		
		public boolean hasTooLittle() {
			return cashOnHand < tooLittle;
		}
	}
}

