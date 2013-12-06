package housing.backend;

import agent.PersonAgent;
import agent.gui.Gui;
import mock.EventLog;
import classifieds.ClassifiedsClass;
import CommonSimpleClasses.Constants;
import housing.gui.LayoutGui;
import housing.interfaces.Dwelling;
import housing.interfaces.MaintenanceWorker;
import housing.interfaces.Resident;
import housing.interfaces.PayRecipient;

/**
 * Dwelling is a housing unit that can be slotted into an apartment complex
 * or expanded to be a full home. It contains all of the Role information
 * necessary for a house. HousingGui is the graphical representation of this.
 * @author Zach VP
 */

public class ResidentDwelling implements Dwelling {
	/* --- Data --- */
	public EventLog log = new EventLog();
	
	// building the dwelling belongs to
//	private ResidentialBuilding building;
	private HousingComplex complex;
	
	/* --- Housing slots --- */
	
	// roles
	private ResidentRole resident;
	private MaintenanceWorkerRole worker;
	private PayRecipientRole payRecipient;
	
	private double monthlyPaymentAmount;
	
	// example: Apartment unit number or house address
	private int IDNumber;

	// Tracks the deterioration of the building
	private Constants.Condition condition;
	
	// cost constant depending on housing condition
	private final int MAX_MONTHLY_PAYMENT = 64;
	
	// gui slots
	LayoutGui gui;
	
	// TODO just test people
	PersonAgent person;
	PersonAgent workPerson;
	PersonAgent payPerson;
	// end test
	
	/* --- Constructor --- */
	public ResidentDwelling(int ID, Constants.Condition startCondition,
			HousingComplex complex) {
		super();
		
		// TODO actual code below
		this.complex = complex;
		this.gui = new LayoutGui(ID);
		
		this.condition = startCondition;
		
		// determine the starting monthly payment for the property
		switch(this.condition){
			case GOOD : this.monthlyPaymentAmount = MAX_MONTHLY_PAYMENT; break;
			case FAIR : this.monthlyPaymentAmount = MAX_MONTHLY_PAYMENT * 0.75; break;
			case POOR : this.monthlyPaymentAmount = MAX_MONTHLY_PAYMENT * 0.5; break;
			case BROKEN : this.monthlyPaymentAmount = MAX_MONTHLY_PAYMENT * 0.5; break;
			default : this.monthlyPaymentAmount = 0; break;
		}
		
		// Adding to classifieds!
		ClassifiedsClass.getClassifiedsInstance().addDwelling(this);
	}
	
	public void addRole(String roleType) throws Exception {
		if(Constants.DEBUG){
			PersonAgent person = new PersonAgent("roleType");
			
			roleType.toLowerCase();
			
			switch(roleType) {
				case "resident" : { 
					resident = new ResidentRole(person, complex.getBuilding(), this, gui);
					person.addRole(resident);
					resident.activate();
					this.complex.addResident(resident);
					break;
				}
				case "worker" : {
					worker = new MaintenanceWorkerRole(person, complex.getBuilding());
					person.addRole(worker);
					worker.activate();
					break;
				}
				case "payrecipient" : {
					payRecipient = new PayRecipientRole(person, complex.getBuilding());
					person.addRole(payRecipient);
					payRecipient.activate();
					break;
				}
				default : {
					throw new Exception("Improper role type passed in parameter.");
				}
			}
			person.startThread();
		}
		else {
			this.payRecipient = complex.getPayRecipient();
			this.worker = complex.getWorker();
			this.resident = new ResidentRole(null, complex.getBuilding(), this, gui);
			this.complex.addResident(resident);
		}
	}

	public void setCondition(Constants.Condition condition){
		this.condition = condition;
	}
	
	public Constants.Condition getCondition(){
		return condition;
	}

	public int getIDNumber() {
		return IDNumber;
	}

	public void setIDNumber(int iDNumber) {
		IDNumber = iDNumber;
	}

	public Resident getResident() {
		return resident;
	}
	
	public Gui getResidentGui() {
		return resident.getGui();
	}
	
	public Gui getLayoutGui() {
		return gui;
	}
	
	public String toString() {
		return "Room in building " + complex.getBuilding();
	}

	public void setResident(ResidentRole resident) {
		this.resident = resident;
	}

	public PayRecipient getPayRecipient() {
		return payRecipient;
	}

	public void setPayRecipient(PayRecipient payRecipient) {
		this.payRecipient = (PayRecipientRole) payRecipient;
	}

	public double getMonthlyPaymentAmount() {
		return monthlyPaymentAmount;
	}

	public void setMonthlyPaymentAmount(double monthlyPaymentAmount) {
		this.monthlyPaymentAmount = monthlyPaymentAmount;
	}

	public MaintenanceWorker getWorker() {
		return worker;
	}
	
	public void degradeHousing() {
		// TODO test 
		condition = Constants.Condition.POOR;
		resident.msgDwellingDegraded();
	}

	public void setWorker(MaintenanceWorkerRole worker) {
		this.worker = worker;
	}
}