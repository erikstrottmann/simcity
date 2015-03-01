package restaurant.jack;

import gui.Building;
import gui.trace.AlertTag;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import kelp.Kelp;
import kelp.KelpClass;

import market.Item;

import restaurant.jack.CookRole.Order.state;
import restaurant.jack.gui.CookGui;
import restaurant.jack.gui.RestaurantLucasBuilding;
import restaurant.jack.interfaces.Cashier;
import restaurant.jack.interfaces.Cook;
import restaurant.jack.interfaces.Customer;
import restaurant.jack.interfaces.Waiter;

import CommonSimpleClasses.CityLocation;
import CommonSimpleClasses.Constants;
import CommonSimpleClasses.ScheduleTask;
import CommonSimpleClasses.CityLocation.LocationTypeEnum;
import agent.WorkRole;
import agent.interfaces.Person;
//import restaurant.HostAgent.Table;

/**
 * Restaurant Host Agent
 */
//We only have 2 types of agents in this prototype. A customer and an agent that
//does all the rest. Rather than calling the other agent a waiter, we called him
//the HostAgent. A Host is the manager of a restaurant who sees that all
//is proceeded as he wishes.
public class CookRole extends WorkRole implements Cook {

	private String name  = " ";

	public CookGui cookGui = null;
	
	private Semaphore active = new Semaphore(0, true);
	

	boolean atWork;

	public static class Order
	{
		Order(String choice, int table, Customer customer, Waiter w, state s) {
			Choice = choice;
			this.table = table;
			cust = customer;
			waiter = w;
			orderState = s;
		}
		Customer cust;
		int table;
		public String Choice;
		Waiter waiter;
		public enum state {none, received, cooking, cooked, givenToWaiter};
		private state orderState;
		
	};
	
	public static class Grill {
		Grill(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public Order o = null;
		public int x;
		public int y;
		
	}
	
	public static class PlateArea {
		PlateArea(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public Order o=null;
		public int x;
		public int y;
	}
	
	List<Grill> grills = new ArrayList<Grill>();
	List<PlateArea> plateAreas = new ArrayList<PlateArea>();
	
	public static class Food
	{
		Food(String type, int cookingTime, int amount, int threshold, int capacity, boolean enroute) {
			this.type = type;
			this.cookingTime = cookingTime;
			this.amount = amount;
			this.threshold = threshold;
			this.capacity = capacity;
			this.enroute = enroute;
			this.futureQuantity = amount;//enroute amount
			
		}
		
		String type;
		int cookingTime;
		int amount;
		int threshold;
		int capacity;
		int futureQuantity;
		boolean enroute;
		
		void decrementQuantity() {
			this.amount -= 1;
			this.futureQuantity -= 1;
		}
	}

	private Map<String, Integer> cookingTimes;
	private Map<String, Food> foods = new HashMap<String, Food>();
	
	private List<String> deliveredFoods = new ArrayList<String>();
	private List<Integer> deliveredAmounts = new ArrayList<Integer>();//used to transfer from message to updating foods action
	
	
	
	int previousMarket = 0;
	
	List<Order> Orders = Collections.synchronizedList(new ArrayList<Order>());
	Timer CookTimer;
	
	
	
	boolean endWorkDay = false;
	
	private List<MarketRole> markets = new ArrayList<MarketRole>();
//	map<String, int> cookTimes; 
	
	/**
	 * used to work with PCWaiter with data sharing
	 */
	boolean timeToCheckOrderWheel =true;
	OrderWheel orderWheel;
//	Timer checkOrderWheelTimer;
	private ScheduleTask schedule = ScheduleTask.getInstance();
	int CHECK_ORDER_WHEEL_TIME = 7;
	
	//DELIVERY CRAP
	private Kelp kelp = KelpClass.getKelpInstance();
	
	private enum DeliveryState {PLACED, NEED_TO_REORDER, COMPLETE};
	private class MyDelivery {
		Set<Item> items;
		Set<Item> itemsToReorder;
		List<CityLocation> markets;
		DeliveryState state;
		
		MyDelivery(Set<Item> items) {
			this.items = items;
			this.itemsToReorder = new HashSet<Item>();
			this.markets = new ArrayList<CityLocation>();
			this.state = DeliveryState.PLACED;
		}
		
		MyDelivery() {
			this(new HashSet<Item>());
		}
	}
	
	RestaurantLucasBuilding rest;
	
	private List<MyDelivery> deliveries;
	Cashier cashier;
	
	public CookRole(Person p, CityLocation c) {
		super(p, c);
		
		rest = (RestaurantLucasBuilding) c;
		
		atWork = false;
		
//		this.checkOrderWheelTimer = new Timer();
		this.CookTimer = new Timer();
		this.cookingTimes = new HashMap<String, Integer>();
		
		this.deliveries = new ArrayList<MyDelivery>();
		
		Food kPatty = new Food("Krabby Patty", 5*1000, 1, 2, 4, false);
		foods.put("Krabby Patty", kPatty);
		Food kShake = new Food("Kelp Shake", 7*1000, 6, 2, 6, false);
		foods.put("Kelp Shake", kShake);
		Food cBits = new Food("Coral Bits", 9*1000, 6, 2, 7, false);
		foods.put("Coral Bits", cBits);
		Food kRings = new Food("Kelp Rings", 11*1000, 6, 3, 8, false);
		foods.put("Kelp Rings", kRings);
		
//		rest.updateInfoPanelInventory(1, 6, 6, 6);
		
		cookingTimes.put("Krabby Patty", 5*1000);
		cookingTimes.put("Kelp Shake", 7*1000);//introduce food to map menu
		cookingTimes.put("Coral Bits", 9*1000);
		cookingTimes.put("Kelp Rings", 11*1000);
		
		grills.add(new Grill(550, 50));
		grills.add(new Grill(550, 75));
		grills.add(new Grill(550, 100));
		grills.add(new Grill(550, 125));
		
		plateAreas.add(new PlateArea(500, 50));
		plateAreas.add(new PlateArea(500, 75));
		plateAreas.add(new PlateArea(500, 100));
		plateAreas.add(new PlateArea(500, 125));
		
		
		
		
//		inventoryMap.put("Tofu", 5);
	}


	public String getMaitreDName() {
		return name;
	}

	public String getName() {
		return name;
	}


	// Messages ################################################
	/**
	 * check orderWheel for updated info
	 * makes PCWaiter data sharing work
	 * called on Timer
	 */
	public void checkOrderWheel() {
		if(orderWheel.getReceivedOrdersSize() != 0){//if there are orders needing cooks attention
			String choice = orderWheel.getReceivedOrderString();
			int tableNum = orderWheel.getReceivedOrderTabelNum();
			Customer c = orderWheel.getReceivedOrderCustomer();
			Waiter w = orderWheel.getReceivedOrderWaiter();
			
			msgHereIsAnOrder(choice, tableNum, c, w);
			orderWheel.changeReceivedOrderState();//changes state of order so it is not looked at again
		}
		
	}
	
	public void checkPlateAreas() {
		for(PlateArea p: plateAreas) {
			if(p.o.cust == orderWheel.getNoneOrderStateCustomer()){
				p.o = null;
				return;
			}
		}
	}
	
	public void msgHereIsAnOrder(String choice, int tableNum, Customer customer, Waiter w) {//sent from waiter
		Do("I have received order");
		Orders.add(new Order(choice, tableNum, customer, w, state.received));
//		w.doDisplayChoice("none");//TODO too much sharing
		stateChanged();
		
	}

	public void msgOrderCooked(int orderNum) {
		Order o = Orders.get(orderNum);
		o.orderState = state.cooked;
		stateChanged();
		
	}
	
	//STUFF FROM DELIVERY RECEIVER
	@Override
	public void msgHereIsYourItems(List<Item> DeliverList) {
		for (Item item : DeliverList) {
			Food f = foods.get(item.name);
			if (f != null && item.amount > 0) {
				Do(AlertTag.RESTAURANT, "Received delivery of " + item.amount
						+ " " + item.name);
				f.amount += item.amount;
			}
		}
	}


	@Override
	public void msgHereIsMissingItems(List<Item> MissingItemList, int orderNum) {
		if (!MissingItemList.isEmpty()) {
			Do(AlertTag.RESTAURANT, "Market couldn't complete order");
			deliveries.get(orderNum).itemsToReorder.addAll(MissingItemList);
			deliveries.get(orderNum).state= DeliveryState.NEED_TO_REORDER;
			stateChanged();
		}		
	}
	
	public void msgIDontHave(List<String> foodList, List<Integer> amountList) {//for Market implementation NOTE CHANGE TO MSGIDONTHAVE
		//stub for now
		if(foodList.size() == 0) {
			Do("Order from market has everything");
			return;
		}
		else {
			markets.get(++previousMarket%markets.size()).msgLowOnFood(foodList, amountList, this);//hack, need way to pick next market
		}
		stateChanged();
	}
	
	public void msgNullifyPlateArea(int yPos) {
		for(PlateArea p : plateAreas) {
			if (p.y == yPos) {
				p.o = null;
			}
		}
	}
		
//		for(int i = 0; i<foodList.size(); i++) {
//				if(foods.get("Tofu").amount < foods.get("Tofu").threshold) {
//					for(String str: foodList) {
//						if(str.equals("Tofu")) {
//							
//						}
//					}
//					foods.get("Tofu").amount = foods.get("Tofu").capacity;
//				}
//				if(foods.get("Rice").amount < foods.get("Rice").threshold) {
//					foods.get("Rice").amount = foods.get("Rice").capacity;
//				}
//				if(foods.get("Sushi").amount < foods.get("Sushi").threshold) {
//					foods.get("Sushi").amount = foods.get("Sushi").capacity;
//				}
//				if(foods.get("Noodles").amount < foods.get("Noodles").threshold) {
//					foods.get("Noodles").amount = foods.get("Noodles").capacity;
//				}
			
//			if(amountList.get(i) >= foods.get(foodList.get(i)).capacity - foods.get(foodList.get(i)).amount) {//if delivery would completely fulfill order requested
//				foods.get(foodList.get(i)).enroute = true;//update flag that this FOOD is BEINGFULFILLED
//			}
		

	
	public void msgFoodDelivered(List<String> foodList, List<Integer> amountList) {//for Market implementation
		
		
		deliveredFoods = foodList;
//		Do(deliveredFoods.get(0));
		deliveredAmounts = amountList;
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		/* Think of this next rule as:
            Does there exist a table and customer,
            so that table is unoccupied and customer is waiting.
            If so seat him at the table.
		 */
		
//		for(Order o : Orders) {
//			if(o.orderState == state.received) {
//				CookOrder(o);
//				o.orderState = state.cooking;
//				return true;
//			}
//		}
//		
//		Do("Entered sched");

		/* TODO LOOK HERE FOR HELP WITH RESTAURANT-MARKET INTEGRATION (3/7) */
		synchronized (foods) {
			for (Food f : foods.values()) {
				if (f.futureQuantity <= f.threshold) {
					restockFood();
					return true;
				}
			}
		}
		
		/* TODO LOOK HERE FOR HELP WITH RESTAURANT-MARKET INTEGRATION (4/7) */
		synchronized (deliveries) {
			for (int i = 0; i < deliveries.size(); i++) {
				MyDelivery delivery = deliveries.get(i);
				if (delivery.state == DeliveryState.NEED_TO_REORDER) {
					retryDelivery(delivery, i);
					return true;
				}
			}
		}
		//END ERIKSTUFF
		
		if(!atWork) {
			goToWork();
			return true;
		}
		
		synchronized(Orders) {
			for(int i = 0; i < Orders.size(); i++) {
				if(Orders.get(i).orderState == state.received){
					//				Do("first cook sched");
					CookOrder(Orders.get(i), i);
					//				Orders.get(i).orderState = state.cooking;
					return true;
				}
			}
		}

		synchronized(Orders) {
			for(Order o : Orders) {
				if(o.orderState == state.cooked) {
					OrderCooked(o);
					o.orderState = state.givenToWaiter; 
					return true;
				}
			}
		}
		
//		if(!deliveredFoods.isEmpty()) {
////			Do("CONDITON MeT");
//			updateInventory(deliveredFoods, deliveredAmounts);
//			return true;
//		}
		
		if(endWorkDay) {
			endWorkDay();
			return true;
		}

		if(orderWheel.getReceivedOrdersSize() > 0){
			checkOrderWheel();
			return true;
		}
		
		if(orderWheel.getNoneOrderStateSize() > 0) {
			checkPlateAreas();
			return true;
		}
		
		if(timeToCheckOrderWheel) {
			timeToCheckOrderWheel = false;
			Runnable command = new Runnable() {
				public void run() {
					stateChanged();
					timeToCheckOrderWheel = true;
				}
			};
			schedule.scheduleTaskWithDelay(command, CHECK_ORDER_WHEEL_TIME * Constants.MINUTE);
			return true;
			//does not return true to call sched again
		}


		return false;

	}

	// Actions ///////////////////
	
	private void endWorkDay() { 
		goOffWork();
		atWork = false;
		endWorkDay = false;
		this.deactivate();
	}
	
	private void goOffWork() {
			addPaycheckToWallet();
			leaveWork();
	}
	

	
	private void addPaycheckToWallet() {
		this.getPerson().getWallet().addCash(300.0);
	}
	
	private void goToWork() {
		cookGui.setName(person.getName());
		cookGui.DoGoAboveKitchen();
		acquireSemaphore(active);
		cookGui.DoGoToDesk();
		acquireSemaphore(active);
		atWork = true;
	}
	
	private void leaveWork() {
		cookGui.DoGoAboveKitchen();
		acquireSemaphore(active);
		cookGui.DoLeaveRestaurant();
		acquireSemaphore(active);
		
	}
	
	private void CookOrder(Order o, final int orderNum) {
	
		Food f = foods.get(o.Choice);
		if(f.amount < f.threshold) { //need to order more from market
//			Do("TRYING to orDEr");
			restockFood();//hack, need to choose
			
		}
		if(f.amount <= 0) {
			((WaiterRole) o.waiter).msgOutOfFood(o.table, o.Choice);
			Orders.remove(o);
			return;
		}
		for(Grill g : grills) {
			if (g.o==null) {
				goToFridge();
				acquireSemaphore(active);
				goToGrill(g);
				acquireSemaphore(active);
				g.o = o;
				break;
				//animate going to grill TODO
			}
		}
		doGoToIdle();
		
		//DoCooking();//animation
		CookTimer.schedule(new TimerTask() {
			//Object cookie = 1;
			public void run() {
//				print("Done cooking ");
				msgOrderCooked(orderNum);
			}
		}, cookingTimes.get(o.Choice));
		f.decrementQuantity();
		
		int kp = foods.get("Krabby Patty").amount;
		int ks = foods.get("Kelp Shake").amount;
		int cb = foods.get("Coral Bits").amount;
		int kr = foods.get("Kelp Rings").amount;
	
		rest.updateInfoPanelInventory(kp, ks, cb, kr);

		o.orderState=state.cooking;
	}

///
	void restockFood() {
		// Find all the items we still need to order.
		Set<Item> itemsToOrder = new HashSet<Item>();
		for (Map.Entry<String, Food> entry : foods.entrySet()) {
			Food f = entry.getValue();
			if (f.futureQuantity <= f.threshold) {
				Do(AlertTag.RESTAURANT, "Out of " + f.type);
				itemsToOrder.add(new Item(f.type,
						f.capacity - f.futureQuantity));
				f.futureQuantity = f.capacity;
			}
		}
		
		if (itemsToOrder.isEmpty()) {
			return;
		}
				
		// Find a market to order from
		Building market = null;
		Building restaurant = (Building) getLocation();
		
		List<CityLocation> openMarkets = kelp.placesNearMe(getLocation(),
				LocationTypeEnum.Market);
		if (openMarkets != null && !openMarkets.isEmpty()) {
			market = (Building) openMarkets.get(0);
		}
		
		// Request the delivery
		MyDelivery delivery = new MyDelivery(itemsToOrder);
		delivery.markets = new ArrayList<CityLocation>();
		int orderNum = 0;
		
		market.interfaces.Cashier marketCashier =
				(market.interfaces.Cashier) market.getGreeter();
		
		Do(AlertTag.RESTAURANT, "Placing an order with market " + market);
		marketCashier.msgPhoneOrder(new ArrayList<Item>(delivery.items),
				this.cashier, this, restaurant, orderNum);
		delivery.markets.add(market);
		
		int kp = foods.get("Krabby Patty").amount;
		int ks = foods.get("Kelp Shake").amount;
		int cb = foods.get("Coral Bits").amount;
		int kr = foods.get("Kelp Rings").amount;

		rest.updateInfoPanelInventory(kp, ks, cb, kr);
	}
	
	private void retryDelivery(MyDelivery delivery, int orderNum) {
		// Find a market that hasn't been tried yet.
		List<CityLocation> openMarkets = kelp.placesNearMe(getLocation(),
				LocationTypeEnum.Market);
		Building market = null;
		for (CityLocation m : openMarkets) {
			if (!delivery.markets.contains(m)) {
				market = (Building) m;
				break;
			}
		}
		if (market == null) {
			Do(AlertTag.RESTAURANT, "No market can complete this order.");
			delivery.state = DeliveryState.COMPLETE;
			return;
		}
		
		// Request the delivery from the new market
		delivery.markets.add(market);
		
		Building restaurant = (Building) getLocation();
		market.interfaces.Cashier marketCashier =
				(market.interfaces.Cashier) market.getGreeter();
		
		Do(AlertTag.RESTAURANT, "Placing an order with market " + market);
		marketCashier.msgPhoneOrder(new ArrayList<Item>(delivery.items),
				this.cashier, this, restaurant, orderNum);
	}


	
///
//	public void checkInventory() {//take market as param?
//		
//		List<String> lowFoodsNames = new ArrayList<String>();
//		List<Integer> lowFoodsAmount = new ArrayList<Integer>();
//		
//		if((foods.get("Tofu").amount < foods.get("Tofu").threshold) && !foods.get("Tofu").enroute) {
//			Food temp = foods.get("Tofu");
//			lowFoodsNames.add(temp.type);
//			lowFoodsAmount.add(temp.capacity - temp.amount);
//		}
//		if((foods.get("Rice").amount < foods.get("Rice").threshold) && !foods.get("Rice").enroute) {
//			Food temp = foods.get("Rice");
//			Do("A " + temp.capacity);
//			Do("B " + foods.get("Rice").capacity + " " + foods.get("Tofu").capacity);
//			lowFoodsNames.add(temp.type);
//			lowFoodsAmount.add(temp.capacity - temp.amount);
//		}
//		if((foods.get("Sushi").amount < foods.get("Sushi").threshold) && !foods.get("Sushi").enroute) {
//			Food temp = foods.get("Sushi");
//			lowFoodsNames.add(temp.type);
//			lowFoodsAmount.add(temp.capacity - temp.amount);
//		}
//		if((foods.get("Noodles").amount < foods.get("Noodles").threshold) && !foods.get("Noodles").enroute) {
//			Food temp = foods.get("Noodles");
//			lowFoodsNames.add(temp.type);
//			lowFoodsAmount.add(temp.capacity - temp.amount);
//		}
//		
////		if(lowFoodsNames.size() > 0) {
//			markets.get(previousMarket % markets.size()).msgLowOnFood(lowFoodsNames, lowFoodsAmount, this);
////		}
//	}
	
//	public void updateInventory(List<String> deliveredFoodsList, List<Integer> deliveredAmountsList) {
//		Do("updating inventory");//NEVER CALLED
//		for(int i = 0; i < deliveredFoodsList.size(); i++) {
//			if(deliveredFoodsList.get(i).equals("Tofu")) {
//				foods.get("Tofu").amount = deliveredAmountsList.get(i);//increase amount have with amount delivered
//				
//			}
//			if(deliveredFoodsList.get(i).equals("Rice")) {
//				Do("Received Rice " + deliveredAmountsList.get(i));
//				foods.get("Rice").amount = deliveredAmountsList.get(i);//increase amount have with amount delivered
//			
//			}
//			if(deliveredFoodsList.get(i).equals("Sushi")) {
//				foods.get("Sushi").amount = deliveredAmountsList.get(i);//increase amount have with amount delivered
//				
//			}
//			if(deliveredFoodsList.get(i).equals("Noodles")) {
//				foods.get("Noodles").amount = deliveredAmountsList.get(i);//increase amount have with amount delivered
//				
//			}
//		}
//		deliveredFoods.clear();
//		deliveredAmounts.clear();
//		//update amount Cook has
//	}

	

	
		
	private void OrderCooked(Order o) {
		for (Grill g: grills) {
			if(g.o == o) {
				
				
				goToGrill(g);
				acquireSemaphore(active);
				g.o = null;
				break;
				
				//animate go to this grill TODO
			}
		}
		doGoToIdle();
		for(PlateArea p: plateAreas) {
			if(p.o == null) {
				
				
				goToPlateArea(p);
				Dimension plateDim = new Dimension(p.x, p.y);
				if(o.waiter instanceof WaiterRole){
					((WaiterRole) o.waiter).msgOrderIsReady(o.cust, o.Choice, o.table, plateDim);
				}
				if(o.waiter instanceof PCWaiterRole) {
					orderWheel.changeCookingOrderState(o.cust);
				}
				acquireSemaphore(active);
				p.o = o;
				break;
				
				
				//animate go to plating area TODO
			
			}
		}
		doGoToIdle();
				o.orderState = state.givenToWaiter;
	}



	//utilities
	
	public Cashier getCashier() {
		return cashier;
	}
	
	public void setCashier(Cashier cashier) {
		this.cashier = cashier;
	}
	
	public void setOrderWheel (OrderWheel o) {
		orderWheel = o;
	}
	
	public void acquireSemaphore(Semaphore a) {
		try {
			a.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	public void addMarket(MarketRole m) {
		markets.add(m);
	}

	public void setGui(CookGui gui) {
		cookGui = gui;
	}

	public CookGui getGui() {
		return cookGui;
	}
	
	public List<Grill> getGrills() {
		return grills;
	}
	
	public List<PlateArea> getPlateAreas() {
		return plateAreas;
	}
	
	public void goToGrill(Grill g) {
		cookGui.DoGoToGrill(g);
	}
	
	public void goToPlateArea(PlateArea p) {
		cookGui.DoGoToPlateArea(p);
	}
	
	public void doGoToIdle() {
		cookGui.DoGoIdle();
	}
	
	private void goToFridge() {
		cookGui.DoGoToFridge();
		
	}
	
	public void msgAtDestination() {
		active.release();
	}


	@Override
	public boolean isAtWork() {
		return isActive();
	}


	@Override
	public boolean isOnBreak() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void msgLeaveWork() {
		endWorkDay = true;
		stateChanged();
	}


	public void setLowOnFood() {
		foods.get("Krabby Patty").amount = 1;
		foods.get("Kelp Shake").amount = 1;
		foods.get("Coral Bits").amount = 1;
		foods.get("Kelp Rings").amount = 1;
	}


	public boolean hasAnyFood() {
		for(Food f: foods.values()){
			if(f.amount > 0){
				return true;
			}
		}
		return false;
	}

	
	
	

	

}

	

