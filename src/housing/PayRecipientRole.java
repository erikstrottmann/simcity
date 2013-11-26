package housing;

import housing.ResidentRole.TaskState;
import housing.interfaces.Dwelling;
import housing.interfaces.PayRecipient;
import housing.interfaces.Resident;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mock.EventLog;
import mock.MockScheduleTaskListener;
import CommonSimpleClasses.CityLocation;
import CommonSimpleClasses.ScheduleTask;
import agent.PersonAgent;
import agent.WorkRole;
import agent.interfaces.Person;

/**
 * PayRecipient is the generic money collector that charges
 * residents and tracks dues.
 * @author Zach VP
 *
 */
public class PayRecipientRole extends WorkRole implements PayRecipient {
	/* ----- Data ----- */
	public EventLog log = new EventLog();
	ScheduleTask schedule = new ScheduleTask();
	
	// used to create time delays and schedule events
	
	/* --- Constants --- */
	// TODO when should shift end?
	private final int SHIFT_START_HOUR = 6;
	private final int SHIFT_START_MINUTE = 0;
	private final int SHIFT_END_HOUR = 12;
	private final int SHIFT_END_MINUTE = 0;
	
	private final int IMPATIENCE_TIME = 7;
	
	enum TaskState { FIRST_TASK, NONE, DOING_TASK }
	TaskState task = TaskState.FIRST_TASK;	
	
	/* ----- Resident Data ----- */
	private List<MyResident> residents = Collections.synchronizedList(new ArrayList<MyResident>());
	enum PaymentState { NONE, PAYMENT_DUE, PAYMENT_PENDING, PAYMENT_RECEIVED,
		PAYMENT_PAID, DONE, };
	
	// class is public for testing purposes
	public class MyResident{
		Dwelling dwelling;
		double owes;
		double hasPaid;
		PaymentState state;
		
		MyResident(Dwelling dwelling){
			this.dwelling = dwelling;
			state = PaymentState.NONE;
		}
		
		// setters and getters
		public void setDwelling(Dwelling dwelling) { this.dwelling = dwelling; }
		public double getOwes() { return owes; }
		public double getPaid() { return hasPaid; }
	}
	
	public PayRecipientRole(Person payRecipientPerson, CityLocation residence) {
		super(payRecipientPerson, residence);
		
		// ask everyone for rent
		Runnable command = new Runnable() {
			@Override
			public void run() {
				synchronized(residents) {
					for(MyResident mr : residents) {
						mr.state = PaymentState.PAYMENT_DUE;
						stateChanged();
					}
				}
			}
		};
		
		// every day at noon
		int hour = 12;
		int minute = 0;
		
		schedule.scheduleDailyTask(command, hour, minute);
	}
	
	public PayRecipientRole(PersonAgent payRecipientPerson) {
		super(payRecipientPerson);
	}

	/* ----- Messages ----- */
	public void msgHereIsPayment(double amount, Resident r) {
		MyResident mr = findResident(r);
		if(mr == null) { return; }
		mr.hasPaid = amount;
		mr.state = PaymentState.PAYMENT_RECEIVED;
		log.add("Received message 'here is payment' " + amount + " from resident #" + mr.dwelling.getIDNumber());
		stateChanged();
	}

	/* ----- Scheduler ----- */
	public boolean pickAndExecuteAnAction() {
		
		if(task == TaskState.FIRST_TASK){
			task = TaskState.NONE;
		}
		
		synchronized(residents){
			for(MyResident mr : residents){
				if(mr.state == PaymentState.PAYMENT_DUE){
					chargeResident(mr);
					return true;
				}
			}
		}
		
		synchronized(residents){
			for(MyResident mr : residents){
				if(mr.state == PaymentState.PAYMENT_RECEIVED){
					checkResidentPayment(mr);
					return true;
				}
			}
		}
		
		return false;
	}

	/* ----- Actions ----- */
	private void checkResidentPayment(MyResident mr){
		log.add("Checking payment for resident #" + mr.dwelling.getIDNumber());
		mr.owes -= mr.hasPaid;
		
		if(mr.owes == 0){
			mr.state = PaymentState.DONE;
			log.add("Resident has paid in full, now owes " + mr.owes);
		}
		else if(mr.owes > 0) {
			log.add("Resident #" + mr.dwelling.getIDNumber() + " still owes money!");
			mr.state = PaymentState.NONE;
		}
		// this should never happen
		else {
			log.add("Resident overpaid! Money will go to next month.");
		}
	}
	
	public void chargeResident(MyResident mr){
		mr.state = PaymentState.PAYMENT_PENDING;
		mr.owes += mr.dwelling.getMonthlyPaymentAmount();
		
		mr.dwelling.getResident().msgPaymentDue(mr.dwelling.getMonthlyPaymentAmount());
		log.add("Charged resident in unit #" + mr.dwelling.getIDNumber());
	}
	
	/* ----- Utilities ----- */
	private MyResident findResident(Resident r){
		for(MyResident mr : residents){
			if(mr.dwelling.getResident() == r){
				return mr;
			}
		}
		log.add("Unable to find resident in list!");
		return null;
	}
	
	public void addResident(Dwelling dwelling){
		if(dwelling.getResident() != null){
			residents.add(new MyResident(dwelling));
			stateChanged();
		}
	}

	public List<MyResident> getResidents() {
		return residents;
	}

	public void setResidents(List<MyResident> residents) {
		this.residents = residents;
	}

	/* --- Abstract methods inherited from WorkRole --- */
	@Override
	public int getShiftStartHour() {
		return SHIFT_START_HOUR;
	}

	@Override
	public int getShiftStartMinute() {
		return SHIFT_START_MINUTE;
	}

	@Override
	public int getShiftEndHour() {
		return SHIFT_END_HOUR;
	}

	@Override
	public int getShiftEndMinute() {
		return SHIFT_END_MINUTE;
	}

	@Override
	public boolean isAtWork() {
		return isActive();
	}

	@Override
	public boolean isOnBreak() {
		return false;
	}

	@Override
	public void msgLeaveWork() {
		deactivate();
	}
}
