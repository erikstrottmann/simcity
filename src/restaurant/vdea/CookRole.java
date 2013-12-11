package restaurant.vdea;

import gui.Building;
import gui.trace.AlertTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kelp.Kelp;
import kelp.KelpClass;
import market.Item;
import restaurant.vdea.gui.*;
import restaurant.vdea.interfaces.*;
import CommonSimpleClasses.CityLocation;
import CommonSimpleClasses.Constants;
import CommonSimpleClasses.CityLocation.LocationTypeEnum;
import agent.WorkRole;
import agent.interfaces.Person;

public class CookRole extends WorkRole implements Cook{

	public List<Order> orders = Collections.synchronizedList(new ArrayList<Order>());
	public enum OrderStatus
	{pending, enoughFood, notEnoughFood, cooking, cooked, done, none, shipmentIncomplete};
	//private OrderStatus status = OrderStatus.pending;//The start state
	private OrderStatus shipmentStatus = OrderStatus.none;

	private Cashier cashier;
	private String name;	
	private Timer timer = new Timer();
	public CookGui cookGui = null;
	//public List<Market> markets = new ArrayList<Market>();
	//int marketTried = 0;

	//Food(name, init inventory numbers)
	public Food kelpShake = new Food("Kelp Shake", 9);
	public Food coralBits = new Food("Coral Bits", 8);
	public Food kelpRings = new Food("Kelp Rings", 10);
	public Food krabbyPatty = new Food("Krabby Patty", 8);
	public List<Food> inventory = new ArrayList<Food>();
	boolean orderSent;
	
	private boolean offWork = false;

	List<MyDelivery> deliveries;
	private Kelp kelp = KelpClass.getKelpInstance();
	
	public CookRole(Person person, CityLocation location) {
		super(person, location);

		this.name = super.getName();

		inventory.add(kelpShake);
		inventory.add(kelpRings);
		inventory.add(coralBits);
		inventory.add(krabbyPatty);
		
		deliveries = new ArrayList<MyDelivery>();
	}
	
	@Override
	public void activate() {
		super.activate();
		offWork = false;
		cookGui.DoGoToCookStation();
	}

	public void setCashier(Cashier c){
		cashier = c;
	}

	//public void addMarket(Market m){
	//	markets.add(m);
	//}

	// Messages

	//from waiter
	public void msgThereIsAnOrder(Waiter w, String choice, int table) {
		orders.add(new Order(w, choice, table));
		//status = OrderStatus.pending;
		stateChanged();
	}

	//from market
	/*public void msgOrderFufillment(List<Food> shipment, boolean orderFull){
		print("RESTOCKING INVENTORY");
		for (Food f: shipment){
			if(f.equals(krabbyPatty)){
				krabbyPatty.addInventory(f.quantity);
				//print("krabbyPatty in "+ f.quantity);
			}
			else if(f.equals(kelpShake)){
				kelpShake.addInventory(f.quantity);
				//print("kelpShake in "+ f.quantity);
			}
			else if(f.equals(coralBits)){
				coralBits.addInventory(f.quantity);
				//print("coralBits in "+ f.quantity);
			}
			else if(f.equals(kelpRings)){
				kelpRings.addInventory(f.quantity);
				//print("kelpRings in "+ f.quantity);
			}
		}
		orderSent = false; //received back order

		for(Food i: inventory){
			Do(AlertTag.RESTAURANT, "new "+ i.getName() + " quantity: "+ i.quantity);
		}

		if(!orderFull){
			Do(AlertTag.RESTAURANT, "Shipment from market incomplete");
			shipmentStatus = OrderStatus.shipmentIncomplete;
			stateChanged();
		}
		else{
			Do(AlertTag.RESTAURANT, "Inventory is full!");
		}
	}*/
	
	@Override
	public void msgHereIsYourItems(List<Item> DeliverList) {
		print("RESTOCKING INVENTORY");
		for (Item f: DeliverList){
			if(f.name.equals("Krabby Patty")){
				krabbyPatty.addInventory(f.amount);
				print("krabbyPatty in "+ f.amount);
			}
			else if(f.name.equals("Kelp Shake")){
				kelpShake.addInventory(f.amount);
				//print("kelpShake in "+ f.quantity);
			}
			else if(f.name.equals("Coral Bits")){
				coralBits.addInventory(f.amount);
				//print("coralBits in "+ f.quantity);
			}
			else if(f.name.equals("Kelp Rings")){
				kelpRings.addInventory(f.amount);
				//print("kelpRings in "+ f.quantity);
			}
		}
		orderSent = false; //received back order

		for(Food i: inventory){
			Do(AlertTag.RESTAURANT, "new "+ i.getName() + " quantity: "+ i.quantity);
		}
		((RestaurantVDeaBuilding)location).updateInventory();
		Do(AlertTag.RESTAURANT, "Inventory is filled!");		
	}

	@Override
	public void msgHereIsMissingItems(List<Item> MissingItemList, int orderNum) {
		if (!MissingItemList.isEmpty()) {
			Do(AlertTag.RESTAURANT, "Market couldn't complete order");
			deliveries.get(orderNum).itemsToReorder.addAll(MissingItemList);
			//shipmentStatus = OrderStatus.shipmentIncomplete;
			deliveries.get(orderNum).state = DeliveryState.NEED_TO_REORDER;
			stateChanged();
		}
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {

		/*if  (shipmentStatus == OrderStatus.shipmentIncomplete){
			restockInventory();
		}
		else{*/
		/* TODO LOOK HERE FOR HELP WITH RESTAURANT-MARKET INTEGRATION (3/7) */
		synchronized (inventory) {
			for (Food f : inventory) {
				if (f.newStock <= f.threshold) {
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
		
		
			synchronized(orders){
				for (Order order : orders) {
					if(order.status == OrderStatus.pending){
						checkOrder(order);
					}
					if (order.status == OrderStatus.enoughFood) {
						cookOrder(order);
					}
					if (order.status == OrderStatus.notEnoughFood){
						sendBackOrder(order);
						return true;
					}
					if (order.status == OrderStatus.cooked){
						orderDone(order);
						return true;
					}
				}
			}
		//}
		if (offWork) { 
			deactivate(); 
			cookGui.DoLeave();
		}
		return false;
	}

	// Actions

	private void checkOrder(Order o){
		boolean enough = checkFoodInventory(o.choice);	//check if there is enough food

		if (enough){
			Do(AlertTag.RESTAURANT, "There is enough " + o.choice);
			decreaseQuantity(o);
			o.status = OrderStatus.enoughFood;
			//stateChanged();
		}
		else{
			Do(AlertTag.RESTAURANT, "There is not enough " + o.choice);
			o.status = OrderStatus.notEnoughFood;
			//restockInventory();	//TODO MarketAgent test
			//stateChanged();
		}
	}

	private void decreaseQuantity(Order o){
		if(o.choice.getName().equals("Krabby Patty")){
			krabbyPatty.cook();
			//System.out.println(krabbyPatty.quantity + " krabbyPattys left");
		}
		else if(o.choice.getName().equals("Kelp Shake")){
			kelpShake.cook();
		}
		else if(o.choice.getName().equals("coral Bits")){
			coralBits.cook();
		}
		else if(o.choice.getName().equals("Kelp Rings")){
			kelpRings.cook();
		}
	}


	//do we have enough to make one more food item?
	//and check if food hits threshold
	public boolean checkFoodInventory(Food f){
		Food compare = new Food();
		Do(AlertTag.RESTAURANT, "Checking amount of " + f);
		if(f.equals("krabbyPatty")){
			compare = krabbyPatty;
		}
		else if(f.equals("Kelp Shake")){
			compare = kelpShake;
		}
		else if(f.equals("Coral Bits")){
			compare = coralBits;
		}
		else if(f.equals("Kelp Rings")){
			compare = kelpRings;
		}


		if(!orderSent && compare.quantity <= compare.threshold){	//TODO inventory
			//print("low on " + compare);
			Do(AlertTag.RESTAURANT, "under threshold, need to restock");
			//restockInventory();
			restockFood();
		}

		//System.out.println(compare + ", amount: " + compare.amount + ", threshold: " + compare.threshold);
		return (compare.quantity) >= 1;//compare.threshold;
	}


	//if one of the foods is low, triggers to check all inventory
	/*private void restockInventory() {
		Do(AlertTag.RESTAURANT, "restocking inventory");
		List<Food> shoppingList = new ArrayList<Food>();
		if(shipmentStatus == OrderStatus.shipmentIncomplete){
			for (Food f: inventory){
				if (f.quantity <= f.capacity){
					shoppingList.add(f);
				}
			}
			shipmentStatus = OrderStatus.none;
		}
		else{
			for (Food f: inventory){
				if (f.quantity <= f.threshold){

					shoppingList.add(f);
				}
			}
		}

		orderSent = true;
		markets.get(marketTried).msgOrderRequest(this, shoppingList, cashier); //TODO
		marketTried = marketTried == markets.size()-1 ? 0 : marketTried + 1;	//switches between markets
	}*/

	private void sendBackOrder(Order o){
		o.w.msgNotEnoughFood(o.choice.getName(), o.table);
		orders.remove(o);
		//o.status = OrderStatus.done;
	}


	private void cookOrder(final Order o) { //TODO fix
		Do(AlertTag.RESTAURANT, "Cooking " + o.choice + "...");
		o.status = OrderStatus.none;
		cookGui.DoCooking(o.choice.getName(), o.table);
		timer.schedule(new TimerTask() {
			public void run() {
				o.status = OrderStatus.cooked;
				stateChanged();
			}
		},
		o.getOrderCookTime() * 500);
	}

	private void orderDone(Order o){
		((RestaurantVDeaBuilding)location).updateInventory();
		cookGui.doneCooking(o.table);
		Do(AlertTag.RESTAURANT, o.choice +" is ready!");
		o.w.msgOrderReady(o.choice.getName(), o.table);
		orders.remove(o);
		o.status = OrderStatus.done;
	}
	
	
	void restockFood() {
		// Find all the items we still need to order.
		Set<Item> itemsToOrder = new HashSet<Item>();
		for (Food entry : inventory) {
			Food f = entry;
			if (f.newStock <= f.threshold) {
				Do(AlertTag.RESTAURANT, "Out of " + f.name);
				itemsToOrder.add(new Item(f.name,
						f.capacity - f.newStock));
				f.newStock = f.capacity;
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
	}
	
	private void retryDelivery(MyDelivery delivery, final int orderNum) {
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
			// TODO BIG IMPORTANT I CHANGED HOW THIS WORKS --------------------
			// No market can complete our order! 
			TimerTask task = new TimerTask() {
				/**
				 * Decrement the "future quantity", but after a delay so we
				 * don't keep retrying the same order every few milliseconds.
				 */
				@Override
				public void run() {
					Do(AlertTag.RESTAURANT,
							"No market can complete this order.");
					
					MyDelivery d = deliveries.get(orderNum);
					for (Item i : d.itemsToReorder) {
						for(Food f: inventory){
							if(f.name.equals(i.name)){
								f.newStock -= i.amount;
								break;
							}
						}
					}
					d.state = DeliveryState.COMPLETE;
				}
			};
			timer.schedule(task, 2 * Constants.MINUTE);
			return;
		}
		
		// Request the delivery from the new market
		delivery.markets.add(market);
		
		Building restaurant = (Building) getLocation();
		market.interfaces.Cashier marketCashier =
				(market.interfaces.Cashier) market.getGreeter();
		
		Do(AlertTag.RESTAURANT, "Placing an order with market " + market);
		marketCashier.msgPhoneOrder(
				new ArrayList<Item>(delivery.itemsToReorder),
				this.cashier, this, restaurant, orderNum);
		/* TODO IMPORTANT, I CHANGED THIS - ONLY REORDER itemsToReorder, NOT
		 * ALL ITEMS */
	}

	//utilities

	public void setGui(CookGui gui) {
		cookGui = gui;
	}

	public CookGui getGui() {
		return cookGui;
	}

	public void gotFood(int table){
		cookGui.collected(table);
	}


	private class Order {
		Waiter w;
		int table;
		Food choice;
		OrderStatus status = OrderStatus.pending;//The start state

		Order(Waiter inW, String inChoice, int t){
			w = inW;
			table = t;
			choice = new Food(inChoice);
		}

		public int getOrderCookTime(){
			return choice.getCookTime();
		}
	}


	@Override
	public boolean isAtWork() {
		return isActive() && !isOnBreak();
	}

	@Override
	public boolean isOnBreak() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void msgLeaveWork() {
		offWork = true;
	}

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

}
