package housing.interfaces;

import housing.backend.MaintenanceWorkerRole;

public interface PayRecipient {
	Object myRes = null;

	/* ----- Normative Messages ----- */
	/** From Resident */
	public void msgHereIsPayment(double amount, Resident r);

	public void addResident(Dwelling dwelling);

	public void msgServiceCharge(double charge, MaintenanceWorkerRole role);

}
