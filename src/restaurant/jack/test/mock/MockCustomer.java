package restaurant.jack.test.mock;


import java.util.Map;

import restaurant.jack.interfaces.Cashier;
import restaurant.jack.interfaces.Customer;
import restaurant.jack.interfaces.Waiter;





/**
 * A sample MockCustomer built to unit test a CashierAgent.
 *
 * @author Jack Lucas
 *
 */
public class MockCustomer extends Mock implements Customer {

        /**
         * Reference to the Cashier under test that can be set by the unit test.
         */
        public Cashier cashier;
        public EventLog log = new EventLog();

        public MockCustomer(String name) {
                super(name);

        }

		@Override
		public void gotHungry() {
//			System.out.println("YOLO");
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgRestarauntIsFull() {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void msgWhatWouldYouLike() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgOutOfChoice(Map<String, Double> newMenu) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgAnimationFinishedDecidingOrder() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgHereIsYourFood() {
			// TODO Auto-generated method stub
			
		}



		@Override
		public void msgDoneEating() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgAnimationFinishedGoToSeat() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgAnimationFinishedLeaveRestaurant() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgHereIsChange(double change) {
			log.add("I have received change " + change);
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgFollowMeToTable(Waiter w, int x, int y,
				Map<String, Double> m) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgHereIsCheck(Cashier c, double check) {
			// TODO Auto-generated method stub
			
		}

       

}