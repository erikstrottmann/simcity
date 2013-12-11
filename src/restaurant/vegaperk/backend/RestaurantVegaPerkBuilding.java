package restaurant.vegaperk.backend;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import agent.Role;
import agent.interfaces.Person;
import gui.Building;
import gui.StaffDisplay;
import CommonSimpleClasses.XYPos;
import restaurant.InfoPanel;
import restaurant.vegaperk.gui.RestaurantGui;
import restaurant.vegaperk.gui.RestaurantPanel;

@SuppressWarnings("serial")
public class RestaurantVegaPerkBuilding extends Building {
	private XYPos entrancePos;
	
	private RestaurantGui gui = new RestaurantGui(this);
	
	private Map<Person, CustomerRole> existingCustomerRoles = new HashMap<Person, CustomerRole>();
	
	private StaffDisplay staff;
	private InfoPanel infoPanel = new InfoPanel(this);
	
	// Constants for staggering opening/closing time
	private static int instanceCount = 0;
	private static final int timeDifference = 12;
	
	public RestaurantVegaPerkBuilding(int x, int y, int width, int height) {
		super(x, y, width, height);
		
		this.entrancePos = new XYPos(width / 2, height);
		
		staff = super.getStaffPanel();
		staff.addAllWorkRolesToStaffList();
		
		infoPanel = new restaurant.InfoPanel(this);
		infoPanel.setKrabbyPattyPrice("1.25");
		infoPanel.setKelpShakePrice("2.00");
		infoPanel.setCoralBitsPrice("1.50");
		infoPanel.setKelpRingsPrice("2.00");
		
		// Stagger opening/closing time
		this.timeOffset = (instanceCount * timeDifference) % 2;
		instanceCount++;
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
		
		// TODO implement person, building constructor for customer
		if(role == null) {
			role = ((RestaurantPanel) getInfoPanel()).addCustomer("Customers", person.getName(), person);
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
		return true;
	}

}
