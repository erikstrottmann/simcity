package housing.test.mock;

import agent.Constants.Condition;
import agent.mock.EventLog;
import housing.interfaces.Dwelling;
import housing.interfaces.PayRecipient;
import housing.interfaces.Resident;

public class MockDwelling implements Dwelling {
	EventLog log = new EventLog();
	
	// role info
	public Resident resident;
	public PayRecipient payRecipient;
	
	// hard dwelling data
	public double monthlyPaymentAmount;
	public int IDNumber;
	public String startCondition;
	public Condition condition;
	
	// constants
	public static int MAX_MONTHLY_PAYMENT = 64;
	
	public MockDwelling(Resident resident, PayRecipient payRecipient, Condition condition) {
		this.resident = resident;
		this.payRecipient = payRecipient;
		
		switch(condition){
			case GOOD : this.monthlyPaymentAmount = MAX_MONTHLY_PAYMENT; break;
			case FAIR : this.monthlyPaymentAmount = MAX_MONTHLY_PAYMENT * 0.75; break;
			case POOR : this.monthlyPaymentAmount = MAX_MONTHLY_PAYMENT * 0.5; break;
			case BROKEN : this.monthlyPaymentAmount = MAX_MONTHLY_PAYMENT * 0.5; break;
		}
	}

	@Override
	public double getMonthlyPaymentAmount() { 
		return monthlyPaymentAmount; }

	@Override
	public Resident getResident() {
		return resident;
	}

	@Override
	public int getIDNumber() {
		return 0;
	}

	@Override
	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	@Override
	public Condition getCondition() {
		return condition;
	}

}