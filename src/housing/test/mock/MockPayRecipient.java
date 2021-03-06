package housing.test.mock;

import mock.EventLog;
import mock.Mock;
import housing.backend.MaintenanceWorkerRole;
import housing.interfaces.Dwelling;
import housing.interfaces.MaintenanceWorker;
import housing.interfaces.PayRecipient;
import housing.interfaces.Resident;
import agent.PersonAgent;

public class MockPayRecipient extends Mock implements PayRecipient {
	/* ----- Data ----- */
	public EventLog log = new EventLog();
	public PersonAgent person = new PersonAgent("Mock Pay Recipient");
	public Resident myRes;
	public MaintenanceWorker worker;
	
	public MockPayRecipient(String name) {
		super(name);
	}

	/* ----- Messages ----- */
	public void msgHereIsPayment(double amount, Resident r) {
		myRes = r;
		if(r == null) return;
		log.add("Received message 'here is payment' " + amount + " from " + myRes);
	}

	@Override
	public void addResident(Dwelling dwelling) {
		log.add("Added resident.");
	}

	@Override
	public void msgServiceCharge(double charge, MaintenanceWorkerRole role) {
		// TODO Auto-generated method stub
		
	}
}
