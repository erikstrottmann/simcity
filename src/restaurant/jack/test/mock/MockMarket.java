package restaurant.jack.test.mock;


import java.util.List;

import restaurant.jack.interfaces.Cashier;
import restaurant.jack.interfaces.Cook;



/**
 * A sample MockCustomer built to unit test a CashierAgent.
 *
 * @author Jack Lucas
 *
 */
public class MockMarket extends Mock implements restaurant.jack.interfaces.Market {

        /**
         * Reference to the Cashier under test that can be set by the unit test.
         */
        public Cashier cashier;

        public EventLog log = new EventLog();
        
        public MockMarket(String name) {
                super(name);

        }

//		@Override
//		public void msgLowOnFood(List<String> foods,
//				List<Integer> amountsRequested, Cook cook) {
//			// TODO Auto-generated method stub
//			
//		}

		@Override
		public void msgHereIsPayment(double amount) {
			log.add("I have received payment of amount " + amount);
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgLowOnFood(List<String> foods,
				List<Integer> amountsRequested,
				restaurant.jack.interfaces.Cook cook) {
			// TODO Auto-generated method stub
			
		}

		
       

}