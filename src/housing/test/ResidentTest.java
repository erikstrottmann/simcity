package housing.test;

import agent.PersonAgent;
import agent.mock.MockScheduleTaskListener;
import housing.ResidentRole;
import housing.interfaces.ResidentGui;
import housing.test.mock.MockPayRecipient;
import housing.test.mock.MockResidentGui;
import junit.framework.TestCase;

public class ResidentTest extends TestCase {
	// testing Roles and Agents
	PersonAgent residentPerson = new PersonAgent("Resident");
	ResidentRole resident = new ResidentRole(residentPerson);
	ResidentGui gui = new MockResidentGui(resident);
	
	// mock roles
	MockPayRecipient mockPayRecipient = new MockPayRecipient("Mock Pay Recipient");
	
	// constants
	private final double PAYMENT_AMOUNT = 64;
	
	protected void setUp() throws Exception {
		super.setUp();
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
		resident.msgPaymentDue(PAYMENT_AMOUNT);
		assertEquals("Resident should now owe " + PAYMENT_AMOUNT + ".",
				PAYMENT_AMOUNT, resident.getMoneyOwed());
		//check that the pay recipient received the pay message from the resident
		assertTrue("Resident's scheduler should have returned true, reacting to the payment due message",
				resident.pickAndExecuteAnAction());
		assertEquals("Pay recipient should have the instance of resident.",
				resident, mockPayRecipient.myRes);
	}
	
	/** Tests the simplest resident cooking and eating case.
	 * Resident will be hungry, have food to use, and will have enough
	 * time to cook.
	 * */
	public void testNormativeCookAndEat() throws InterruptedException {
		// set the resident gui to the mock gui
		resident.setGui(gui);
		// listens for the taskScheduler to finish. Use for routine that have a time delay
		MockScheduleTaskListener mockRequestListener = new MockScheduleTaskListener();
		
		/* --- Test Preconditions --- */
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
		// handle the delay from the food cooking
		synchronized(mockRequestListener){
			mockRequestListener.wait(5000);
		}

		// resident eats food after food has finished cooking
		assertTrue("Scheduler should return true after resident puts food on stove.",
				resident.pickAndExecuteAnAction());
		// handle delay for eating food
		synchronized(mockRequestListener){
			mockRequestListener.wait(5000);
		}
		// resident's hunger is now satisfied
		assertFalse("Hungry should be false", resident.isHungry());
		assertEquals("Food should be NULL", null, resident.getFood());
	}
}
