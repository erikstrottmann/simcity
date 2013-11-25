package housing;

import javax.swing.JPanel;

import CommonSimpleClasses.CityLocation;
import CommonSimpleClasses.XYPos;
import agent.Role;
import agent.interfaces.Person;
import gui.Building;
import housing.gui.HousingComplex;

public class ResidentialBuilding extends Building {
	XYPos entrancePos;
	HousingComplex complex = new HousingComplex();
	
	CityLocation housingComplex;

	public ResidentialBuilding(int x, int y, int width, int height) {
		super(x, y, width, height);
		entrancePos = new XYPos(x + width/2, height);
	}

	@Override
	public XYPos entrancePos() {
		return entrancePos;
	}

	@Override
	public Role getGreeter() {
		return (Role) complex.getPayRecipientRole();
	}

	@Override
	public LocationTypeEnum type() {
		return LocationTypeEnum.Apartment;
	}

	@Override
	public Role getCustomerRole(Person person) {
		
		return this.getGreeter();
	}

	@Override
	public JPanel getAnimationPanel() {
		return complex;
	}

	@Override
	public JPanel getInfoPanel() {
		// TODO Auto-generated method stub
		return new JPanel();
	}

}
