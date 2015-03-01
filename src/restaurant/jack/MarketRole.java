package restaurant.jack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import restaurant.jack.gui.HostGui;
import restaurant.jack.interfaces.Cashier;
import restaurant.jack.interfaces.Cook;
import restaurant.jack.interfaces.Market;
import CommonSimpleClasses.CityLocation;
import agent.WorkRole;
//import restaurant.HostAgent.Table;
import agent.interfaces.Person;


public class MarketRole extends WorkRole implements Market {


	private String name;

	public HostGui hostGui = null;
	
	private Cashier cashier;
	
	
	public static class Food
	{
		Food(String type, int cookingTime, int amount, int threshold, int capactiy, boolean enroute) {
			this.type = type;
			this.cookingTime = cookingTime;
			this.amount = amount;
			this.threshold = threshold;
			this.capacity = capacity;
			this.enroute = enroute;
			
		}
		
		String type;
		int cookingTime;
		int amount;
		int threshold;
		int capacity;
		boolean enroute;
	}

	private Cook cook;
	List<String> foodsToSend = new ArrayList<String>();//need list of lists
	List<Integer> amountsToSend = new ArrayList<Integer>();//need list of lists
	
	private Map<String, Integer> cookingTimes;
	private Map<String, Food> foods = new HashMap<String, Food>();
	
	private List<String>requestedFoods = new ArrayList<String>();//store ordered lists
	private List<Integer>requestedAmounts = new ArrayList<Integer>();//store ordered lists
	
	Map<String, Integer> foodQuantities = new HashMap<String, Integer>();
	boolean orderRequested = false;//for scheduler action
	Timer deliverTimer;
//	map<String, int> cookTimes; 
	double myMoney = 0;
	
	public MarketRole(Person p, CityLocation c) {
		super(p, c);
		
		foodQuantities.put("Tofu", 10);//initialize quantities in map
		foodQuantities.put("Rice", 10);
		foodQuantities.put("Sushi", 10);
		foodQuantities.put("Noodles", 10);
		

		this.deliverTimer = new Timer();
		this.cookingTimes = new HashMap<String, Integer>();
		
		Food tofu = new Food("Tofu", 5*1000, 4, 2, 4, false);
		foods.put("Tofu", tofu);
		Food rice = new Food("Rice", 7*1000, 0, 2, 6, false);
		foods.put("Rice", rice);
		Food sushi = new Food("Sushi", 9*1000, 4, 2, 7, false);
		foods.put("Sushi", sushi);
		Food noodles = new Food("Noodles", 11*1000, 6, 3, 8, false);
		foods.put("Noodles", noodles);
		
		cookingTimes.put("Tofu", 5*1000);
		cookingTimes.put("Rice", 7*1000);//introduce food to map menu
		cookingTimes.put("Sushi", 9*1000);
		cookingTimes.put("Noodles", 11*1000);
		
	}
	
	public void setCashier(Cashier c) {
		this.cashier = c;
	}


	public String getMaitreDName() {
		return name;
	}

	public String getName() {
		return name;
	}


	// Messages ################################################
	
	public void msgLowOnFood(List<String> foods, List<Integer> amountsRequested, Cook cook) {//message from cook
		//stub
		this.cook = cook;
		orderRequested = true;
		requestedFoods = foods;
		requestedAmounts = amountsRequested;
		stateChanged();
	}
	
	public void msgHereIsPayment(double amount) {
		//TODO THISSISISISISI
		myMoney += amount;//hacky?
		Do("I have received $" + amount +" as payment and now have $" + myMoney);
	}


	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
	
		if(orderRequested) {
			processOrder();
			orderRequested = false;
			return true;
			//order has been requested, process it
		}


		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions ///////////////////

	private void processOrder() {
		Do("Processing Order");
		foodsToSend.clear();
		amountsToSend.clear();
		List<String> dontHaveFoods = new ArrayList<String>();
		List<Integer> dontHaveAmounts = new ArrayList<Integer>();
		for(int i = 0; i<requestedFoods.size(); i++) {
			if(foodQuantities.get(requestedFoods.get(i)) <= requestedAmounts.get(i)) {
				dontHaveFoods.add(requestedFoods.get(i));
				dontHaveAmounts.add(requestedAmounts.get(i));
				foodQuantities.put(requestedFoods.get(i), 0);
//				foodQuantities.put(requestedFoods.get(i), foodQuantities.get(requestedFoods.get(i)) - requestedAmounts.get(i));
			}
			//else set inventory of ITEM to ZERO and give cook all I have
//			else {
//				foodsToSend.add(requestedFoods.get(i));
//				amountsToSend.add(foodQuantities.get(requestedFoods.get(i)));//send ALL
//				foodQuantities.put(requestedFoods.get(i), 0);
//			}
		}
		
		for(int i = 0; i<requestedFoods.size(); i++) {
			if(foodQuantities.get(requestedFoods.get(i)) >= requestedAmounts.get(i)) {
				foodsToSend.add(requestedFoods.get(i));
				Do("H " + requestedAmounts.get(i));
				amountsToSend.add(requestedAmounts.get(i));
//				foodQuantities.put(requestedFoods.get(i), 0);
				foodQuantities.put(requestedFoods.get(i), foodQuantities.get(requestedFoods.get(i)) - requestedAmounts.get(i));
			}

		}
		cook.msgIDontHave(dontHaveFoods, dontHaveAmounts);
		deliverTimer.schedule(new TimerTask() {
			//Object cookie = 1;
			public void run() {
				Do("Delivering Order " + foodsToSend.size());
				cook.msgFoodDelivered(foodsToSend, amountsToSend);
				int cost = 0;
				for(int i : amountsToSend) {
					cost += i;
				}
				requestPayment(cost);
//				cashier.msgRequestPayment(this, foodsToSend.size());
				
				//TODO message to Cashier with requested payment
			}
		}, 5000);
	}

	//utilities
	
	public void requestPayment(int amount) {
		cashier.msgRequestPayment(this, amount);
	}

	public void setGui(HostGui gui) {
		hostGui = gui;
	}

	public HostGui getGui() {
		return hostGui;
	}


	@Override
	public boolean isAtWork() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean isOnBreak() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void msgLeaveWork() {
		// TODO Auto-generated method stub
		
	}


}

	

