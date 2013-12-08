package restaurant.vegaperk;

import agent.Agent;

import java.awt.Dimension;
import java.util.*;
import java.util.concurrent.Semaphore;

import restaurant.gui.CookGui;
import restaurant.interfaces.Cook;
import restaurant.interfaces.Waiter;
import restaurant.test.mock.EventLog;

/**
 * Cook Agent
 */
public class CookAgent extends Agent implements Cook {
	private String name;
	private CookGui cookGui;
	public EventLog log = new EventLog();
	
	private boolean onOpening = true;
	
	private List<Order> orders = Collections.synchronizedList(new ArrayList<Order>());
	private List<Grill> grills = Collections.synchronizedList(new ArrayList<Grill>());
	private List<Dimension> grillPositions = Collections.synchronizedList(new ArrayList<Dimension>());
	private List<Dimension> platePositions = Collections.synchronizedList(new ArrayList<Dimension>());
	
	private List<PlateZone> plateZones = Collections.synchronizedList(new ArrayList<PlateZone>());
	
	private Semaphore performingTasks = new Semaphore(0, true);
	
	private Map<String, Integer> groceries = Collections.synchronizedMap(new HashMap<String, Integer>());
	
	private Timer timer = new Timer();
//	create an anonymous Map class to initialize the foods and cook times
	private static final Map<String, Integer> cookTimes =
			Collections.synchronizedMap(new HashMap<String, Integer>(){
		{
			put("Krabby Patty",1000);
			put("Kelp Rings", 700);
			put("Coral Bits", 500);
			put("Kelp Shake", 200);
		}
	});
	
	private List<MarketAgent> markets = Collections.synchronizedList(new ArrayList());
	private int orderFromMarket = 0;
	
	Map<String, Food> inventory = Collections.synchronizedMap(new HashMap<String, Food>(){
		{
			put("Krabby Patty", new Food("Krabby Patty", 2, 1000, 1, 3));
			put("Kelp Rings", new Food("Kelp Rings", 2, 700, 1, 3));
			put("Coral Bits", new Food("Coral Bits", 2, 500, 1, 3));
			put("Kelp Shake", new Food("Kelp Shake", 10, 200, 1, 3));
		}
	});

	public CookAgent(String name) {
		super();
		this.name = name;
		
		for(int i = 0; i < 4; i++){
			int startY = 50;
			
			int grillX = 370;
			int plateX = 430;
			
			grillPositions.add(new Dimension(grillX, startY + 50*i));
			platePositions.add(new Dimension(plateX, startY + 50*i));
			
			grills.add(new Grill(grillPositions.get(i).width, grillPositions.get(i).height));
			plateZones.add(new PlateZone(grillPositions.get(i).width, grillPositions.get(i).height));
			PlateZone pz = plateZones.get(i);
			pz = null;
		}
	}

	/** Accessor and setter methods */
	public String getName() {
		return name;
	}
	
	/** Messages from other agents */
	
	/** From Waiter */
	public void msgHereIsOrder(Waiter w, String c, int t){
		orders.add(new Order(c, t, w, OrderState.PENDING));
		stateChanged();
	}
	public void msgGotFood(int table){
		for(Order o : orders){
			if(o.table == table){
				DoRemovePlateFood(table);
				PlateZone pz = plateZones.get(table);
				pz = null;
			}
		}
		stateChanged();
	}
	/** From Market(s) */
	public void msgCannotDeliver(Map<String, Integer> list){
		Do("Can't fulfill order.");
		orderFromMarket++;
		
		if(orderFromMarket < markets.size()){
			orderFoodThatIsLow(list);
		}
		else{
			Do("Not at any markets!");
			orderFromMarket = 0;
		}
	}
	@Override
	public void msgHereIsDelivery() {
		Do("Received delivery.");
	}
	public void msgCanDeliver(Map<String, Integer> deliveries){
		for(Map.Entry<String, Integer> f: deliveries.entrySet()){
			groceries.remove(f.getKey());
			Food food = inventory.get(f.getKey());
			food.amount = food.capacity;
		}
	}
	
	/** From the Cook Gui */
	public void msgAtDestination(){
		performingTasks.release();
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	protected boolean pickAndExecuteAnAction() {
		if(onOpening){
			openStore();
			return true;
		}
		if(!groceries.isEmpty()){
			orderFoodThatIsLow(groceries);
		}
		synchronized(orders){
			for(Order o : orders){
				if(o.state==OrderState.COOKED){
					plateIt(o);
					return true;
				}
			}
			for(Order o : orders){
				if(o.state==OrderState.PENDING){
					tryToCookFood(o);
					return true;
				}
			}
		}
		return false;
	}
	
	/** Actions */
	private void tryToCookFood(Order o){
		o.state = OrderState.COOKING;
		Food f = inventory.get(o.choice);
		if(f.amount <= 0){
			print("Out of food");
			o.waiter.msgOutOfChoice(o.choice, o.table);
			orders.remove(o);
			return;
		}
		f.amount--;
		if(f.amount == f.low){
			groceries.put(f.type, f.capacity - f.amount);
		}
		
		DoGoToFridge();
		acquire(performingTasks);
		
		DoToggleHolding(o.choice);
		DoGoToGrill(o.table);
		acquire(performingTasks);
		
		DoPlaceFood(o.table, o.choice);
		
		DoToggleHolding(null);
		DoGoHome();
		acquire(performingTasks);
//		must iterate through by integer instead of pointers because of the timer below
		for(int i = 0; i < orders.size(); i++){
			if(orders.get(i) == o){
				timeFood(i);
			}
		}
	}
	
	private void timeFood(final int i){//timer takes in a final int to circumvent timer restrictions
		timer.schedule(new TimerTask() {
			public void run() {
				orders.get(i).state = OrderState.COOKED;
				stateChanged();
			}
		},
		//Retrieve from the map how long the food takes to cook
		3 * cookTimes.get(orders.get(i).choice));
	}
	
	private void plateIt(Order o){
		o.state = OrderState.FINISHED;
		print("Order Plated");
		o.waiter.msgOrderDone(o.choice, o.table);
		Do("Order done " + o.choice);
		plateZones.get(o.table).setOrder(o);
		
		DoPlateFood(o.table, o.choice);
		acquire(performingTasks);
	}
	
	private void openStore(){
		onOpening = false;
		DoDrawGrillAndPlates();
		Do("Opening restaurant");
		for(Map.Entry<String, Food> entry : inventory.entrySet()){
			Food f = entry.getValue();
			if(f.amount <= f.low){
				orderFoodThatIsLow(groceries);
			}
		}
	}
	
	private void orderFoodThatIsLow(Map<String, Integer> list){
		Do("Ordered food.");
		MarketAgent m = markets.get(orderFromMarket);
		m.msgNeedFood(list);
		list.clear();
	}
	
	/** Animation Functions */
	private void DoDrawGrillAndPlates(){
		cookGui.setGrillDrawPositions(grillPositions, platePositions);
	}
	private void DoGoToGrill(int grillIndex){
		cookGui.DoGoToGrill(grillIndex);
	}
	private void DoToggleHolding(String item){
		cookGui.DoToggleItem(item);
	}
	private void DoPlaceFood(int grillIndex, String food){
		cookGui.DoPlaceFood(grillIndex, food);
	}
	private void DoPlateFood(int grillIndex, String food){
		cookGui.DoPlateFood(grillIndex, food);
	}
	private void DoRemovePlateFood(int pos){
		cookGui.DoRemovePlateFood(pos);
	}
	private void DoGoToFridge(){
		cookGui.DoGoToFridge();
	}
	private void DoGoHome(){
		cookGui.DoGoHome();
	}
	
	/** Utility Functions */
	public void addMarket(MarketAgent m){
		markets.add(m);
	}
	private PlateZone findPlateZoneOrder(Order o){
		for(PlateZone pz : plateZones){
			if(pz.order == o){
				return pz;
			}
		}
		Do("Can't find plate zone!");
		return null;
	}
	public void setGui(CookGui gui){
		cookGui = gui;
	}
	private Grill findGrillOrder(Order o){
		for(Grill g : grills){
			if(g.order == o){
				return g;
			}
		}
		Do("Can't find grill!");
		return null;
	}
	private void acquire(Semaphore sem){
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/** Classes */
	enum OrderState { PENDING, COOKING, COOKED, FINISHED };
	private class Order{
		Waiter waiter;
		OrderState state;
		String choice;
		int table;
		
		Order(String c, int t, Waiter w, OrderState s){
			choice = c;
			table = t;
			waiter = w;
			state = s;
		}
	}
	
	private class Food{
		String type;
		int amount, cookTime, low, capacity;
		OrderState os;
		
		Food(String t, int amt, int ct, int lo, int cap){
			type = t;
			amount = amt;
			cookTime = ct;
			low = lo;
			capacity = cap;
		}
	}
	
	private class Grill{
		Order order;
		int x, y;
		
		Grill(int dx, int dy){
			order = null;
			x = dx;
			y = dy;
		}
		private void setOrder(Order o){
			order = o;
		}
	}
	
	private class PlateZone{
		Order order;
		int x, y;
		
		PlateZone(int dx, int dy){
			order = null;
			x = dx;
			y = dy;
		}
		private void setOrder(Order o){
			order = o;
		}
	}
}