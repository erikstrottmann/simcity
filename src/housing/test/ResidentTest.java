package housing.test;

import CommonSimpleClasses.Constants.Condition;
import agent.PersonAgent;
import housing.backend.ResidentRole;
import housing.interfaces.Dwelling;
import housing.interfaces.DwellingLayoutGui;
import housing.interfaces.MaintenanceWorker;
import housing.interfaces.PayRecipient;
import housing.interfaces.Resident.FoodState;
import housing.interfaces.ResidentGui;
import housing.test.mock.MockDwelling;
import housing.test.mock.MockDwellingGui;
import housing.test.mock.MockMaintenanceWorker;
import housing.test.mock.MockPayRecipient;
import housing.test.mock.MockResidentGui;
import junit.framework.TestCase;

public class ResidentTest extends TestCase {
	// testing Roles and Agents
	PersonAgent residentPerson = new PersonAgent("Resident");
	ResidentRole resident;
	ResidentGui gui;
	
	// mock roles
	PayRecipient mockPayRecipient = new MockPayRecipient("Mock Pay Recipient");
	Dwelling dwelling = new MockDwelling(resident, mockPayRecipient, Condition.GOOD);
	MaintenanceWorker worker = new MockMaintenanceWorker("Worker", dwelling);
	
	DwellingLayoutGui mockLayoutGui = new MockDwellingGui();
	
	// constants
	private final double PAYMENT_AMOUNT = 64;
	
	protected void setUp() throws Exception {
		super.setUp();
		dwelling.setCondition(Condition.GOOD);
		dwelling.setWorker(worker);
		
		resident = new ResidentRole(residentPerson, null, dwelling, mockLayoutGui);
		gui = new MockResidentGui(resident);
		gui.setLayoutGui(mockLayoutGui);
		dwelling.setResident(resident);
		
		worker.setDwelling(dwelling);
	}
	
	/**
	 * Tests the simplest case of the resident paying
	 * PayRecipient will notify the resident that a payment is due
	 * and the resident will have enough money to pay it.
	 *
	*/
	public void testNormativeResidentPaysDues(){
		/* --- Set up Variables --- */
		resident.setPayee(mockPayRecipient);
		resident.getPerson().getWallet().setCashOnHand(PAYMENT_AMOUNT);
		
		/* --- Test Preconditions --- */
		assertEquals("Resident should owe $0.0.", resident.getMoneyOwed(), 0.0);
		assertEquals("Resident should have an empty event log before the message is called. Instead, the Resident's event log reads: "
				+ resident.log.toString(), 0, resident.log.size());
		
		/* --- Run Scenario --- */
		resident.msgPaymentDue(PAYMENT_AMOUNT, mockPayRecipient);
		assertEquals("Resident should now owe " + PAYMENT_AMOUNT + ".",
				PAYMENT_AMOUNT, resident.getMoneyOwed());
		
		// check that the resident's money updated properly
		assertTrue("Resident's scheduler should have returned true, reacting to the payment due message",
				resident.pickAndExecuteAnAction());
		assertEquals("Pay recipient should owe no money now.",
				0.0, resident.getMoneyOwed());
		assertEquals("Person should have money - " + PAYMENT_AMOUNT,
				0.0, residentPerson.getWallet().getCashOnHand());
	}
	
	/** Tests the simplest resident cooking and eating case.
	 * Resident will be hungry, have food to use, and will have enough
	 * time to cook.
	 * */
	public void testNormativeCookAndEat() throws InterruptedException {
		// set preliminary variables
		resident.setHungry(false);
		
		// set the resident gui to the mock gui
		resident.setGui(gui);
		
		/* --- Test Preconditions --- */
		// check money owed
		assertEquals("Resident should owe $0.0.", resident.getMoneyOwed(), 0.0);
		// check event log
		assertEquals("Resident should have an empty event log before the message is called. Instead, the Resident's event log reads: "
				+ resident.log.toString(), 0, resident.log.size());
		
		// check food conditions
		assertFalse("Resident should not be hungry to start.",
				resident.isHungry());
		assertEquals("Food resident is handling should be null.",
				null, resident.getFood());
		assertTrue("Resident grocery list should be empty.",
				resident.getGroceries().isEmpty());
		assertFalse("Resident should not have an empty fridge.",
				resident.getRefrigerator().isEmpty());
		
		/* --- Run the Scenario --- */
		resident.setHungry(true);
		
		// scheduler runs and resident executes cookFood()
		assertTrue("Scheduler should return true after resident is hungry "
				+ "and there are items in the fridge.", resident.pickAndExecuteAnAction());
		
		// simulate cooking the food
		resident.getFood().setState(FoodState.COOKED);
		
		// resident eats food after food has finished cooking
		assertTrue("Scheduler should return true after resident puts food on stove.",
				resident.pickAndExecuteAnAction());
		
		// resident's hunger is now satisfied
		assertFalse("Hungry should be false", resident.isHungry());
		assertEquals("Food should be NULL", null, resident.getFood());
	}
	
	public void testCallMaintenanceWorker(){
		/* --- Test Preconditions --- */
		assertEquals("Resident should have an empty event log before the message is called."
				+ "Instead, the Resident's event log reads: " + resident.log.toString(), 0, resident.log.size());
		assertEquals("Dwelling should have a starting condition of GOOD.",
				Condition.GOOD, resident.getDwelling().getCondition());
		
		/* --- Run the Scenario --- */
		
		//  set the dwelling to a poor state, initializing the scenario
		resident.getDwelling().setCondition(Condition.POOR);
		
		// call the scheduler. Resident should notify the maintenance worker
		assertTrue("Scheduler should return true after noting the poor dwelling state.",
				resident.pickAndExecuteAnAction());
		
		// send the 'fixed' message to the resident
		resident.msgDwellingFixed();
		assertEquals("Dwelling condition should now be GOOD.",
				Condition.GOOD, dwelling.getCondition());
	}
}
