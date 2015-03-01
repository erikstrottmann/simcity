package restaurant.jack.interfaces;

import java.util.List;

/**
 * A sample Customer interface built to unit test a CashierAgent.
 *
 * @author Jack Lucas
 *
 */
public interface Market {

	public abstract void msgLowOnFood(List<String> foods, List<Integer> amountsRequested, Cook cook) ; //implement after animation all good to go
        //TODO create cook interface
	public abstract void msgHereIsPayment(double amount);
	
	
}