package restaurant.vegaperk.backend;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import market.interfaces.DeliveryReceiver;
import market.interfaces.PhonePayer;
import agent.Role;
import agent.interfaces.Person;
import gui.Building;
import gui.RestaurantFakeOrderInterface;
import gui.StaffDisplay;
import CommonSimpleClasses.XYPos;
import restaurant.InfoPanel;
import restaurant.vegaperk.gui.RestaurantGui;
import restaurant.vegaperk.gui.RestaurantPanel;

@SuppressWarnings("serial")
public class RestaurantVegaPerkBuilding extends Building
	implements RestaurantFakeOrderInterface{
	private XYPos entrancePos;
	
	private RestaurantGui gui = new RestaurantGui(this);
	
	private Map<Person, CustomerRole> existingCustomerRoles = new HashMap<Person, CustomerRole>();
	
	private StaffDisplay staff;
	private InfoPanel infoPanel = new InfoPanel(this);
	
	// Constants for staggering opening/closing time
	private static int instanceCount = 1;
	private static final int timeDifference = 12;
	
	public RestaurantVegaPerkBuilding(int x, int y, int width, int height) {
		super(x, y, width, height);
		// Stagger opening/closing time
		this.timeOffset = (instanceCount * timeDifference) % 2;
		instanceCount++;
		
		this.entrancePos = new XYPos(width / 2, height);
		
		staff = super.getStaffPanel();
		staff.addAllWorkRolesToStaffList();
		
		infoPanel = new restaurant.InfoPanel(this);
		infoPanel.setKrabbyPattyPrice("1.25");
		infoPanel.setKelpShakePrice("2.00");
		infoPanel.setCoralBitsPrice("1.50");
		infoPanel.setKelpRingsPrice("2.00");
		
		
//		System.out.println("VP Closing hour " + this.getClosingHour());
//		System.out.println("VP Closing minute " + this.getClosingMinute());
	}

	@Override
	public XYPos entrancePos() {
		return entrancePos;
	}

	@Override
	public Role getGreeter() {
		return gui.getHost();
	}

	@Override
	public LocationTypeEnum type() {
		return LocationTypeEnum.Restaurant;
	}

	@Override
	public Role getCustomerRole(Person person) {
		CustomerRole role = existingCustomerRoles.get(person);
		
		if(role == null) {
			role = ((RestaurantPanel) getRestPanel()).addCustomer("Customers", person.getName(), person);
		}
		else {
			role.setPerson(person);
		}
		
		person.addRole(role);
		return role;
	}

	@Override
	public JPanel getAnimationPanel() {
		return gui.getAnimationPanel();
	}

	@Override
	public JPanel getInfoPanel() {
		infoPanel.setBuildingName(getName());
		return infoPanel;
	}

	@Override
	public StaffDisplay getStaffPanel() {
		return staff;
	}
	
	@Override public boolean isOpen() {
		return getRestPanel().getHost().onDuty() && getRestPanel().cashier.onDuty() &&
				getRestPanel().cook.onDuty() && getRestPanel().cook.hasFood()
				&& super.isOpen();
	}

	public RestaurantPanel getRestPanel() {
		return gui.getRestPanel();
	}
	

	@Override
	public DeliveryReceiver getCook() {
		return gui.getRestPanel().cook;
	}

	@Override
	public PhonePayer getCashier() {
		return gui.getRestPanel().cashier;
	}

	@Override
	public void makeLowOnFood() {
		gui.getRestPanel().cook.makeLowOnFood();
	}

	public void updateInfoPanel(Map<String, Integer> inventoryList) {
		this.infoPanel.UpdateInfoPanel(inventoryList);
	}

}
