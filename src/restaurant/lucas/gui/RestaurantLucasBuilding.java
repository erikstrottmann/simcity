package restaurant.lucas.gui;

import gui.Building;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import restaurant.lucas.CashierRole;
import restaurant.lucas.CookRole;
import restaurant.lucas.CustomerRole;
import restaurant.lucas.HostRole;
import restaurant.lucas.OrderWheel;
import restaurant.lucas.PCWaiterRole;
import restaurant.lucas.WaiterRole;
import restaurant.lucas.gui.CustomerGui;
import CommonSimpleClasses.Constants;
import CommonSimpleClasses.XYPos;
import agent.Role;
import agent.interfaces.Person;


public class RestaurantLucasBuilding extends Building {
	
	private Map<Person, CustomerRole> existingCustomers;
	private HostRole host;
	private CashierRole cashier;
	private CookRole cook;
	private List<WaiterRole> normalWaiters;
	private List<PCWaiterRole> pcWaiters;
	
	private InfoPanel infoPanel = new InfoPanel();

	// Constants for staggering opening/closing time
	private static int instanceCount = 0;
	private static final int timeDifference = 12;
	
	RestaurantGui restaurantGui = new RestaurantGui();
	
	OrderWheel orderWheel;
	
	public RestaurantLucasBuilding(int x, int y, int width, int height) {
		super(x, y, width, height);
		this.existingCustomers = new HashMap<Person, CustomerRole>();
		
		orderWheel = new OrderWheel();
		
		// Stagger opening/closing time
		this.timeOffset = (instanceCount * timeDifference) % 2;
		instanceCount++;
		
		initRoles();
	}
	
	private void initRoles() {
		host = new HostRole(null, this);
		cashier = new CashierRole(null, this);
		cook = new CookRole(null, this);
		
		//give roles to host so host can end work day
		host.addRole(cashier);
		host.addRole(cook);
		
		//Create GUIS
		HostGui hostGui = new HostGui(host);
		CookGui cookGui = new CookGui(cook);
		CashierGui cashierGui = new CashierGui(cashier);
		
		//add guis to roles
		host.setGui(hostGui);
		cook.setGui(cookGui);
		cashier.setGui(cashierGui);
		
		//add orderwheel to cook
		cook.setOrderWheel(orderWheel);
		
		//add to animation panel
		restaurantGui.getAnimationPanel().addGui(hostGui);
		restaurantGui.getAnimationPanel().addGui(cookGui);
		restaurantGui.getAnimationPanel().addGui(cashierGui);
		
		
		normalWaiters = new ArrayList<WaiterRole>();
		pcWaiters = new ArrayList<PCWaiterRole>();
		
//		//Creates Normal WaiterRoles
//		for (int i = 0; i < 2; i++) {
//			// Create the waiter and add it to the list
//			WaiterRole w = new WaiterRole(null, this);
//			w.setIdlePosition(i);
//			normalWaiters.add(w);
//			
//			//give roles to host so host can end work day
//			host.addRole(w);
//			
//			// Set references between the waiter and other roles
//			w.setOtherRoles(host, cashier);
//			w.setCook(cook);
//			//TODO make setcook method for normalWaiter
//			host.addWaiter(w);
//			
//			// Create and set up the waiter GUI
//			WaiterGui wGui = new WaiterGui(w, restaurantGui);
//			w.setGui(wGui);
//			restaurantGui.getAnimationPanel().addGui(wGui);
//		}
		
		//Creates PCWaiterRoles
		for (int i = 0; i < 2; i++) {
			// Create the waiter and add it to the list
			PCWaiterRole w = new PCWaiterRole(null, this);
			w.setIdlePosition(i);
			w.setOrderWheel(orderWheel);
			pcWaiters.add(w);
			
			//give roles to host so host can end work day
			host.addRole(w);
			
			// Set references between the waiter and other roles
			w.setOtherRoles(host, cashier);
//			w.setCook(cook);
			//TODO make setcook method for normalWaiter
			host.addWaiter(w);
			
			// Create and set up the waiter GUI
			WaiterGui wGui = new WaiterGui(w, restaurantGui);
			w.setGui(wGui);
			restaurantGui.getAnimationPanel().addGui(wGui);
		}
	}

	@Override
	public XYPos entrancePos() {
		return new XYPos(Constants.BUILDING_WIDTH/2, Constants.BUILDING_HEIGHT/2);
	}

	@Override
	public Role getGreeter() {
		return host;
	}

	@Override
	public LocationTypeEnum type() {
		return LocationTypeEnum.Restaurant;
	}

	@Override
	public Role getCustomerRole(Person person) {
		CustomerRole role = existingCustomers.get(person);
		if (role == null) {
			// Create a new role if none exists
			role = new CustomerRole(person, this);
			role.setLocation(this);
			role.setHost(host);
			
			CustomerGui custGui = new CustomerGui(role, restaurantGui);
			role.setGui(custGui);
			restaurantGui.getAnimationPanel().addGui(custGui);
			
		} else {
			// Otherwise use the existing role
			role.setPerson(person);
		}
		
		// Add the role to the person, and return it.
		person.addRole(role);
		return role;
	}

	@Override
	public JPanel getAnimationPanel() {
		return restaurantGui.getAnimationPanel();
	}

	@Override
	public JPanel getInfoPanel() {
		return infoPanel;
	}

	@Override
	public JPanel getStaffPanel() {
		return new JPanel();
	}
	
	public boolean isOpen() {
		return hostOnDuty() && cashierOnDuty() && cookOnDuty() &&
				waiterOnDuty();
	}
	
	public boolean hostOnDuty() {
		return host != null && host.isAtWork();
	}

	public boolean cashierOnDuty() {
		return cashier != null && cashier.isAtWork();
	}

	public boolean cookOnDuty() {
		return cook != null && cook.isAtWork();
	}

	public boolean waiterOnDuty() {
		for (WaiterRole w : normalWaiters) {
			if (w.isAtWork()) { return true; }
		}
		for (PCWaiterRole w : pcWaiters) {
			if (w.isAtWork()) { return true; }
		}
		return false;
	}
	
	
	
}