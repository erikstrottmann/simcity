package agent;

import gui.Building;
import gui.HospitalBuilding;
import gui.trace.AlertTag;
import housing.backend.ResidentRole;
import housing.backend.ResidentialBuilding;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kelp.Kelp;
import kelp.KelpClass;
import market.DeliveryGuyRole;
import transportation.CarAgent;
import transportation.PassengerRole;
import transportation.RealPassengerRole;
import transportation.interfaces.Car;
import transportation.interfaces.Corner;
import CommonSimpleClasses.CityLocation;
import CommonSimpleClasses.CityLocation.LocationTypeEnum;
import CommonSimpleClasses.Constants;
import CommonSimpleClasses.ScheduleTask;
import CommonSimpleClasses.TimeManager;
import CommonSimpleClasses.XYPos;
import agent.interfaces.Person;
import agent.interfaces.Person.Wallet.IncomeLevel;
import bank.RobberRole;
import bank.gui.BankBuilding;

/**
 * A PersonAgent is the heart and soul of SimCity. Nearly all interactions in
 * the city are between PersonAgents and the {@link Role}s they control.
 * 
 * @author Erik Strottmann
 */
public class PersonAgent extends Agent implements Person {
	private String name;
	private Set<Role> roles;
	
	private Map<String, Integer> shoppingList;
	
	private PersonEvent event;
	private HungerLevel hungerLevel;
	private boolean forgotAboutFoodAtHome = false;
	
	private TimeManager timeManager;
	
	private Kelp kelp;
	
	private long lastTimeEatingOut;
	/**
	 * How long this person likes to wait between each time eating out (in game
	 * time, not real time)
	 */
	private long eatingOutWaitPeriod;
	
	/**
	 * If there is less than this much time before work starts, the person
	 * considers work to be starting soon. (In game time, not real time.)
	 */
	private long workStartThreshold;
	
	private Wallet wallet;
	private Map<String, Integer> inventory;
	private BufferedImage image;
	
	private Car car;
	private boolean clearFoodAtHome = false;
	
	private boolean timeToRobABank = false;
	
	/**
	 * set true when rest cashier role activated
	 * set false when businessbankcustomerrole activated
	 */
	private boolean shouldDepositRestaurantMoney = false;
	
	public PersonAgent(String name, IncomeLevel incomeLevel,
			HungerLevel hunger, boolean goToRestaurant, boolean foodAtHome,
			BufferedImage image){
		super();
		updateHungerLevel();
		
		this.name = name;
		this.image = image;
		this.roles = Collections.synchronizedSet(new HashSet<Role>());
		
		this.shoppingList = Collections.synchronizedMap(new HashMap<String, Integer>());
		
		this.event = PersonEvent.NONE;
		this.hungerLevel = hunger;
		
		this.timeManager = TimeManager.getInstance();
		
		this.kelp = KelpClass.getKelpInstance();
		
		setWantsToEatOut(goToRestaurant);
		clearFoodAtHome = (!foodAtHome);
		
		this.workStartThreshold = 2 * Constants.HOUR;
		
		this.wallet = new Wallet(incomeLevel); // medium income level
		// this.wallet.setCashOnHand(9001.00);
		this.inventory = new HashMap<String, Integer>();
		
	}
	
	public PersonAgent(String name){
		this(name, IncomeLevel.MEDIUM, HungerLevel.NEUTRAL, true, true, null);
	}
	
	/* -------- Messages -------- */
	
	// from PassengerRole
	@Override
	public void msgArrivedAtDestination() {
		event = PersonEvent.ARRIVED_AT_LOCATION;
		stateChanged();
		print(AlertTag.PASSENGER, this.getName(),
				"Arrived at " + getPassengerRole().getLocation());
	}
	
	/* -------- Scheduler -------- */
	/**
	 * Scheduler - determine what action is called for, and do it.
	 */
	@Override
	protected boolean pickAndExecuteAnAction() {
		// Clear the fridge if necessary
		if (clearFoodAtHome && getResidentRole() != null) {
			getResidentRole().clearFoodAtHome();
			clearFoodAtHome = false;
		}
		
		// First, the Role rules.
		
		boolean roleExecuted = false;
		boolean roleActive = false;
		
		List<Role> activeRoles = new ArrayList<Role>();
		
		
		
		synchronized (roles) {
			for (Role r : roles) {
				if (r.isActive()) {
					//				if (r.isAwaitingInput()) {
					//					// The Role is paused. Return false, but don't activate
					//					// another Role!
					//					return false;
					//				} else {
					activeRoles.add(r);
					roleActive = true;
					//				}
				}
			}
		}
		
		for (Role r : activeRoles) {
			roleExecuted = r.pickAndExecuteAnAction() || roleExecuted;
		}
		
		// if at least one role's scheduler returned true, return true
		if (roleExecuted) { return true; }
		if (roleActive) { return false; }

		
		// If you just arrived somewhere, activate the appropriate Role. 
		if (event == PersonEvent.ARRIVED_AT_LOCATION) {
			PassengerRole pass = getPassengerRole();
			if (getWorkRole() instanceof DeliveryGuyRole) {
				DeliveryGuyRole dg = (DeliveryGuyRole) getWorkRole();
				if (!dg.msgAreYouAvailable()) {
					// if the delivery guy is on a delivery
					if (!dg.getOrders().isEmpty()){
						for (int i = 0 ; i< dg.getOrders().size(); i++){
							if (pass.getLocation().equals(dg.getOrders().get(i).getBuilding())) {
								Do(AlertTag.MARKET, name,
										"Arrived at delivery location.");
								dg.msgArrivedDestination();
								return true;
							} else if (pass.getLocation().equals(dg.getLocation())) {
								Do(AlertTag.MARKET, name, "Returning to work.");
								dg.msgArrivedDestination();
								dg.activate();
								return true;
							}
						}
					}
				}
			}
			activateRoleForLoc(pass.getLocation(), atLocationForWork());
			event = PersonEvent.NONE;
			return true;
		}
		

		// If you didn't just arrive somewhere, decide what to do next.
		if (timeToRobABank) {
			CityLocation bank = chooseBank();
			if(bank!= null) {
				goToBank(bank);
				return true;
			}
		}
		if (workStartsSoon()) {
			goToWork();
			return true;
		}
		if (isHungry()) {
			if (wantsToEatOut()) {
				 CityLocation restaurant = chooseRestaurant();
				 if (restaurant != null) {
					goToRestaurant(restaurant);
					return true;
				 }
			}
			if (getResidentRole() != null && getResidentRole().getDwelling() != null && hasFoodAtHome() || hasFoodInInventory()) {
				goHome();
				return true;
			} else {
				CityLocation market = chooseMarket();
				if (market != null) {
					goToMarket(market);
					return true;
				}
			}
		}
		if (needToGoToBank()) {
			CityLocation bank = chooseBank();
			if (bank != null) {
				goToBank(bank);
				return true;
			}
		}
		
		if (getResidentRole() != null) {
			goHome();
			return true;
		}

		// No actions were performed.

		return false;
	}

	/* -------- Actions -------- */
	
	/**
	 * Starts up the {@link PassengerRole} to take the Person to the given
	 * {@link CityLocation}. This is one of PersonAgent's most important
	 * actions - and several other actions can simply call this one.
	 */
	private void goToLoc(CityLocation loc) {
		PassengerRole pass = getPassengerRole();
		pass.msgGoToLocation(loc, true);
		// TODO person is hard coded to be willing to use the bus
		pass.activate();
	}
	
	private void goHome() {
		 CityLocation home = getResidentRole().getLocation();
		 goToLoc(home);
	}
	
	private void goToWork() {
		CityLocation work = getWorkRole().getLocation();
		goToLoc(work);
	}
	
	private void goToRestaurant(CityLocation restaurant) {
		goToLoc(restaurant);
	}
	
	private void goToMarket(CityLocation market) {
		goToLoc(market);
	}
	
	private void goToBank(CityLocation bank) {
		goToLoc(bank);
	}
	
	/* -------- Utilities -------- */
	
	@Override
	/** Return the PersonAgent's name for output e.g. messages. */
	public String getName() {
		return name;
	}
	
	@Override
	/**
	 * Returns the string representation of this person: the word "person"
	 * followed by the person's name.
	 */
	public String toString() {
		return "person " + getName();
	}
	
	/**
	 * Adds the given Role to the PersonAgent's list. The person will not call
	 * the Role's scheduler if the Role is inactive. Also calls
	 * {@link #stateChanged()}.
	 */
	@Override
	public void addRole(Role r) {
		this.roles.add(r);
		stateChanged();
	}
	
	/**
	 * Returns true if the person scheduler has a task awaiting it.
	 */
	public boolean hasSomethingToDo() {
		return workStartsSoon() || isHungry()  || needToGoToBank()
				|| timeToRobABank;
	}
	
	/**
	 * Removes given role from PersonAgent's list.
	 * Primarily used for removing robber, because
	 * it is created for a one-time robbery by InfoPanel
	 */
	@Override
	public void removeRole(Role r) {
		roles.remove(r);
		stateChanged();
	}
	
	/**
	 * Activates the current location's Role. Won't activate a WorkRole
	 * unless forWork is true; won't activate a PassengerRole ever.
	 * 
	 * @param loc
	 * @param forWork only activates a WorkRole if this is true
	 */
	private void activateRoleForLoc(CityLocation loc, boolean forWork) {
		
		if (loc instanceof Building && !(loc instanceof ResidentialBuilding)
				&& !forWork && !((Building) loc).isOpen()) {
			// Only enter a building if it's open.
			return;
		}
		
		synchronized (roles) {
			for (Role r : roles) {
				if (loc.equals(r.getLocation())
						&& (timeToRobABank == (r instanceof RobberRole))
						&& (forWork == (r instanceof WorkRole))
						&& !(r instanceof PassengerRole)) {
					
					timeToRobABank = false;
					r.activate();
					return;
				}
			}
		}
		
		if (forWork) {
			// You tried to go here for work, but you don't work here. Oops.
			return;
		}
		//you should deposit a restaurant's money and you are at a bank
		//so get a businessBankCustomerRole and activate it
		if (shouldDepositRestaurantMoney && loc instanceof BankBuilding){
			BankBuilding bank = (BankBuilding) loc;
			Role role = bank.getBusinessBankCustomerRole(this);
			role.activate();
			return;
		}
		// There is no role for this location! Get a new one.
		if (loc instanceof Building && !(loc instanceof HospitalBuilding) && 
				!(loc instanceof ResidentialBuilding)) {
			Building building = (Building) loc;
			Role role = building.getCustomerRole(this);
			role.activate();
			return;
		} // else there isn't a role for this location
	}
	
	@Override
	public Wallet getWallet() {
		return this.wallet;
	}
	
	@Override
	public Car getCar() {
		return this.car;
	}
	
	@Override
	public void setCar(Car car) {
		this.car = car;
	}
	
	//updates hunger level to decrease as day goes on
	public void updateHungerLevel() {
		int interval = 3;
		ScheduleTask task = ScheduleTask.getInstance();
		
		Runnable command = new Runnable(){
			@Override
			public void run() {
					doUpdateHungerLevel();
				}
			
		};
		task.scheduleTaskAtInterval(command, TimeManager.getInstance().currentSimTime(), 4 * Constants.HOUR);
		
	}
	
	private void doUpdateHungerLevel() {
		if(this.hungerLevel == HungerLevel.FULL) {
			this.hungerLevel = HungerLevel.SATISFIED;
			stateChanged();
			return;
		}
		else if(this.hungerLevel == HungerLevel.SATISFIED) {
			this.hungerLevel = HungerLevel.NEUTRAL;
			stateChanged();
			return;
		}
		else if(this.hungerLevel == HungerLevel.NEUTRAL) {
			this.hungerLevel = HungerLevel.HUNGRY;
			stateChanged();
			return;
		}
		else if(this.hungerLevel == HungerLevel.HUNGRY) {
			this.hungerLevel = HungerLevel.STARVING;
			stateChanged();
			return;
		}
		
	}
	
	// ---- Eating out
	
	/**
	 * @return how long this person likes to wait between each time eating out
	 * 		   (in game time, not real time)
	 */
	public long getEatingOutWaitPeriod() {
		return this.eatingOutWaitPeriod;
	}
	
	/**
	 * @param newWait how long this person will now like to wait between each
	 * time eating out (in game time, not real time)
	 */
	public void setEatingOutWaitPeriod(long newWait) {
		this.eatingOutWaitPeriod = newWait;
	}
	
	/** @return the date and time of the last time the person ate out. */
	public long getLastTimeEatingOut() {
		return this.lastTimeEatingOut;
	}
	
	/** Modifies the last time the person thinks he or she ate out. */
	public void setLastTimeEatingOut(long newLastTime) {
		this.lastTimeEatingOut = newLastTime;
	}
	
	
	// ---- Hunger
	
	public HungerLevel getHungerLevel() {
		return this.hungerLevel;
	}
	
	/**
	 * Sets the hunger level to the given; if eatingOut, updates
	 * lastTimeEatingOut.
	 * 
	 * @param newHungerLevel
	 * @param eatingOut whether this hunger modification was due to eating out
	 * @see #getLastTimeEatingOut()
	 */
	public void setHungerLevel(HungerLevel newHungerLevel, boolean eatingOut) {
		if (eatingOut) {
			this.lastTimeEatingOut = timeManager.currentSimTime();
		}
		this.hungerLevel = newHungerLevel;
		stateChanged();
	}
	
	/**
	 * Sets the hunger level to the given, without modifying lastTimeEatingOut.
	 * Convenience for {@link #setHungerLevel(HungerLevel, boolean)}.
	 * 
	 * @param newHungerLevel
	 * @see #getLastTimeEatingOut()
	 */
	public void setHungerLevel(HungerLevel newHungerLevel) {
		setHungerLevel(newHungerLevel, false);
	}
	
	/**
	 * Sets the hunger level to full; if eatingOut, updates lastTimeEatingOut.
	 * Convenience for {@link #setHungerLevel(HungerLevel, boolean)}.
	 * 
	 * @param newHungerLevel
	 * @param eatingOut whether this hunger modification was due to eating out
	 * @see #getLastTimeEatingOut()
	 */
	public void setHungerToFull(boolean eatingOut) {
		setHungerLevel(HungerLevel.FULL, eatingOut);
	}
	
	/**
	 * Sets the hunger level to full, without modifying lastTimeEatingOut.
	 * Convenience for {@link #setHungerLevel(HungerLevel, boolean)}.
	 * 
	 * @param newHungerLevel
	 * @see #getLastTimeEatingOut()
	 */
	public void setHungerToFull() {
		setHungerToFull(false);
	}
	
	/**
	 * Used by infopanel's button to set person to being
	 * very hungry
	 */
	public void setHungerToStarving() {
		setHungerLevel(HungerLevel.STARVING);
	}
	
	// ---- Work starting soon
	
	/**
	 * If there is less than this much time before work starts, the person
	 * considers work to be starting soon. (In game time, not real time.)
	 */
	public long getWorkStartThreshold() {
		return this.workStartThreshold;
	}
	
	/**
	 * @param if there is less than this much time before work starts, the
	 * person will now consider work to be starting soon. (In game time, not
	 * real time.)
	 */
	public void setWorkStartThreshold(long newThresh) {
		this.workStartThreshold = newThresh;
	}
	
	/**
	 * called by CashierRole in restaurant
	 * makes person go to bank when off shift to
	 * deposit restaurant money in account
	 */
	public void setShouldDepositRestaurantMoney(boolean b) {
		shouldDepositRestaurantMoney = b;
	}
	
	public double getCashierMoney() {
		for(Role r: roles) {
			if(r instanceof restaurant.jack.CashierRole){
				return ((restaurant.jack.CashierRole) r).getRestaurantMoney();
			}
		}
		return -1;
	}
	
	// ---- Methods for finding special roles
	
	@Override
	public synchronized PassengerRole getPassengerRole() {
		synchronized (roles) {
			// Get the PassengerRole if there is one.
			for (Role r : roles) {
				if (r instanceof PassengerRole) {
					return (PassengerRole) r;
				}
			}
		}
		// Else, create a new one.
		CityLocation hospital = null;
		for (CityLocation loc : kelp.placesNearMe(new XYPos(),
				LocationTypeEnum.Hospital)) {
			
			// Choose the first hospital, since we only have one for now.
			hospital = loc;
			break;
		}
		if (hospital == null) {
			throw new NullPointerException("Kelp has no hospital roles.");
		}
		PassengerRole pass = new RealPassengerRole(this, hospital);
		addRole(pass);
		return pass;
	}

	@Override
	public ResidentRole getResidentRole() {
		synchronized (roles) {
			for (Role r : roles) {
				if (r instanceof ResidentRole) {
					return (ResidentRole) r;
				}
			}
		}
		return null;
	}
	
	@Override
	public WorkRole getWorkRole() {
		synchronized (roles) {
			for (Role r : roles) {
				if (r instanceof WorkRole) {
					return (WorkRole) r;
				}
			}
		}
		return null;
	}
	
		
	// ---- Choosing locations to patronize

	private CityLocation chooseRestaurant() {
		// get a list of nearby restaurants
		List<CityLocation> restaurants = kelp.placesNearMe(
				getPassengerRole().getLocation(),
				CityLocation.LocationTypeEnum.Restaurant);
		
		// choose a restaurant
		for (CityLocation r : restaurants) {
			// TODO don't just choose the first restaurant
			// TODO DIEGO WAS HERE CHECKING IF BUILDINGS ARE OPEN
			if ( ((Building) r).isOpen() ) return r;
		}
		
		// if no restaurants exist
		return null;
	}
	
	private CityLocation chooseMarket() {
		// get a list of nearby markets
		List<CityLocation> markets = kelp.placesNearMe(
				getPassengerRole().getLocation(),
				CityLocation.LocationTypeEnum.Market);
		
		// choose a market
		for (CityLocation m : markets) {
			// TODO don't just choose the first market
			// TODO DIEGO WAS HERE CHECKING IF BUILDINGS ARE OPEN
			if ( ((Building) m).isOpen() ) return m;
		}
		
		// if no markets exist
		return null;
	}
	
	private CityLocation chooseBank() {
		// get a list of nearby banks
		List<CityLocation> banks = kelp.placesNearMe(
				getPassengerRole().getLocation(),
				CityLocation.LocationTypeEnum.Bank);
		
		// choose a bank
		for (CityLocation b : banks) {
			// TODO don't just choose the first bank
			// TODO DIEGO WAS HERE CHECKING IF BUILDINGS ARE OPEN
			if ( ((Building) b).isOpen() ) return b;
		}
		
		// if no banks exist
		return null;
	}
	
	// ---- Boolean methods (for deciding what to do next)
	
	@Override
	public boolean workStartsSoon() {
		WorkRole workRole = getWorkRole();
		if (workRole == null) {
			return false;
		}
		long workStartTime = workRole.startTime();
		if (workRole.getLocation().type() == LocationTypeEnum.Bank &&
				timeManager.isTimeOnWeekend(workStartTime)) {
			return false; // banks are only open on weekdays!
		}
		return timeManager.timeUntil(workStartTime) <= this.workStartThreshold;
	}
	
	/**
	 * Whether you're here to work. Helps determine whether to activate a
	 * WorkRole.
	 */
	private boolean atLocationForWork() {
		// You're not here for work if work doesn't start soon.
		if (!workStartsSoon()) {
			return false;
		}
		
		WorkRole workRole = getWorkRole();
		if (workRole == null) {
			// If you don't have a job, you're not here to work.
			return false;
		}
		
		// Otherwise, you're here for work iff this is your work location. 
		return getPassengerRole().getLocation()
				.equals(workRole.getLocation());
	}
	
	@Override
	public boolean isStarving() {
		return hungerLevel == HungerLevel.STARVING;
	}
	
	@Override
	public boolean isHungry() {
		return hungerLevel == HungerLevel.STARVING ||
				hungerLevel == HungerLevel.HUNGRY; 
	}
	
	/**
	 * Whether this person wants to eat out. Based on the last time this person
	 * ate out and on how long this person likes to wait between each time
	 * eating out.
	 */
	public boolean wantsToEatOut() {
		return wallet.getIncomeLevel() != IncomeLevel.POOR
				&& -timeManager.timeUntil(lastTimeEatingOut)
				>= this.eatingOutWaitPeriod;
	}
	
	/**
	 * Sets whether or not the person will eat out next time it's hungry
	 * (within the next two days)
	 */
	public void setWantsToEatOut(boolean eatOut) {
		if (eatOut) {
			this.lastTimeEatingOut =
					timeManager.fakeStartTime() - 3*Constants.DAY;
			this.eatingOutWaitPeriod = Constants.DAY/2;
		} else {
			this.lastTimeEatingOut =
					timeManager.fakeStartTime() - Constants.DAY/2;
			this.eatingOutWaitPeriod = 3*Constants.DAY;
		}
	}
	
	/** Whether this person has food at home. */
	public boolean hasFoodAtHome() {
		if (forgotAboutFoodAtHome) return false;
		ResidentRole res = getResidentRole();
		return (res != null) && res.thereIsFoodAtHome();
	}
	
	public void forgetAboutFoodAtHome() {
		forgotAboutFoodAtHome = true;
	}
	
	public void rememberAboutFoodAtHome() {
		forgotAboutFoodAtHome = false;
	}
	
	public boolean hasFoodInInventory() {
		for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
			if (entry.getValue() >= 1 &&
					Constants.FOODS.contains(entry.getKey())) {
				
				// There's some food in the inventory
				return true;
			}
		}
		// No foods in inventory
		return false;
	}
	
	public void setTimeToRobABank(){
		timeToRobABank = true;
		stateChanged();
	}
	
	public boolean getTimeToRobABank() {
		return timeToRobABank;
	}
	
	public boolean needToGoToBank() {
		return wallet.hasTooMuch() || wallet.hasTooLittle()
				|| wallet.needsMoney() || shouldDepositRestaurantMoney;
	}
	
	// ---- Market/Inventory
	
	@Override
	public Map<String, Integer> getInventory() {
		return this.inventory;
	}
	
	@Override
	public void addItemsToInventory(String name, int amount) {
		Integer currentCount = this.inventory.get(name);
		if (currentCount == null) { currentCount = 0; }
		this.inventory.put(name, currentCount + amount);
	}
	
	@Override
	public void removeItemsFromInventory(String name, int amount) {
		Integer currentCount = this.inventory.get(name);
		if (currentCount == null) { currentCount = 0; }
		int newAmount = currentCount - amount;
		if (newAmount < 0) { newAmount = 0; }
		this.inventory.put(name, newAmount);
	}
	
	// ---- Enumerations
	
	private enum PersonEvent {NONE, ARRIVED_AT_LOCATION}
	public enum HungerLevel {STARVING, HUNGRY, NEUTRAL, SATISFIED, FULL}
	
	// ---- Methods to avoid weird inheritance issues
	
	@Override
	public void agentStateChanged() {
		stateChanged();
	}
	
	@Override
	public void printMsg(String msg) {
		print(AlertTag.PASSENGER, this.getName(), msg);
	}
	
	@Override
	public void printMsg(String msg, Throwable e) {
		print(msg, e);
	}

	@Override
	public void agentDo(AlertTag tag, String name, String msg) {
		Do(tag, this.getName(), msg);
	}
	
	@Override
	public void agentDo(String msg) {
		Do(msg);
	}

	public void instantiateCar() {
		Corner nearestCorner = (Corner) kelp.placesNearMe
				(getPassengerRole().location,
						LocationTypeEnum.Corner).get(0);
		
		setCar(new CarAgent(nearestCorner));
	}

	@Override
	public Map<String, Integer> getShoppingList() {
		return shoppingList;
	}

	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}

	
}

