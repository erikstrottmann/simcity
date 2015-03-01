package restaurant.jack.interfaces;

import java.util.Map;



/**
 * A sample Customer interface built to unit test a CashierAgent.
 *
 * @author Jack Lucas
 *
 */
public interface Customer {
   
	public abstract void gotHungry();
	
	public abstract void msgRestarauntIsFull();
	
	public abstract void msgFollowMeToTable(Waiter w, int x, int y, Map<String, Double> m);
	
	public abstract void msgWhatWouldYouLike();
	
	public abstract void msgOutOfChoice(Map<String, Double> newMenu);
	
	public abstract void msgAnimationFinishedDecidingOrder();
	
	public abstract void msgHereIsYourFood();
	
	public abstract void msgHereIsCheck(Cashier c, double check);
	
	public abstract void msgDoneEating();
	
	public abstract void msgAnimationFinishedGoToSeat();
	
	public abstract void msgAnimationFinishedLeaveRestaurant();

	public abstract void msgHereIsChange(double change);
	
	
}