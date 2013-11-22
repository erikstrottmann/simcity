package agent.interfaces;

import housing.ResidentRole;
import transportation.PassengerRole;
import transportation.interfaces.Car;
import agent.Role;

public interface Person {
	/* -------- Messages -------- */
	
	/** Called by PassengerRole when it arrives at its destination. */
	public void msgArrivedAtDestination();
		
	/* -------- Utilities -------- */
	
	/**
	 * Adds the given Role to the Person's list. The person should not call
	 * the Role's scheduler if the Role is inactive.
	 */
	public void addRole(Role r);
	
	/**
	 * @see Wallet
	 */
	public Wallet getWallet();
	
	/**
	 * @see Car
	 */
	public Car getCar();
	
	/**
	 * @see Car
	 */
	public void setCar(Car car);
	
	
	// ---- Methods for finding special roles
	
	/**
	 * Returns the PersonAgent's PassengerRole, or the first one if there's
	 * more than one for some reason.
	 * 
	 * @return the PassengerRole; null if none exists
	 */
	public PassengerRole getPassengerRole();
	
	/**
	 * Returns the PersonAgent's ResidentRole, or the first one if there's more
	 * than one for some reason.
	 * 
	 * @return the ResidentRole; null if none exists
	 */
	public ResidentRole getResidentRole();
	
	// TODO uncomment and add JavaDoc when WorkRole is merged into master
	// public WorkRole getWorkRole();
	
	
	// ---- Boolean methods (for deciding what to do next)
	
	/**
	 * If work starts soon, the Person shouldn't start any tasks that are less
	 * important than work.
	 */
	public boolean workStartsSoon();
	
	/**
	 * If starving, finding food should be one of the Person's highest
	 * priorities.
	 */
	public boolean isStarving();
	
	
	// ---- Inner classes
	
	/**
	 * Holds a Person's money on hand, and thresholds for when the Person will
	 * go to the bank to withdraw or deposit cash.
	 */
	public static class Wallet {
		private double cashOnHand;
		private double tooMuch;
		private double tooLittle;
		
		private IncomeLevel incomeLevel;
		
		public enum IncomeLevel {POOR, MEDIUM, RICH}
				
		/**
		 * Creates a wallet with amounts of money corresponding to the
		 * income level.
		 * 
		 * @param incomeLevel one of POOR, MEDIUM, or RICH
		 */
		public Wallet(IncomeLevel incomeLevel) {
			this.incomeLevel = incomeLevel;
			
			switch (incomeLevel) {
				case POOR:
					this.cashOnHand = 5;
					this.tooMuch = 50;
					this.tooLittle = 0;
					break;
				case RICH:
					this.cashOnHand = 100;
					this.tooMuch = 300;
					this.tooLittle = 50;
					break;
				case MEDIUM:
					this.cashOnHand = 30;
					this.tooMuch = 60;
					this.tooLittle = 15;
					// fall through to default
				default:
					break;
			}
		}
		
		/**
		 * Creates a new medium income level wallet.
		 * 
		 * @see #Wallet(IncomeLevel)
		 */
		public Wallet() {
			this(IncomeLevel.MEDIUM);
		}
		
		public IncomeLevel getIncomeLevel() {
			return this.incomeLevel;
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
