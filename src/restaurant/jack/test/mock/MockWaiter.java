package restaurant.jack.test.mock;


import java.awt.Dimension;
import java.util.Map;

import restaurant.jack.interfaces.Cashier;
import restaurant.jack.interfaces.Customer;




/**
 * A sample MockCustomer built to unit test a CashierAgent.
 *
 * @author Jack Lucas
 *
 */
public class MockWaiter extends Mock implements restaurant.jack.interfaces.Waiter {

        /**
         * Reference to the Cashier under test that can be set by the unit test.
         */
        public Cashier cashier;
        public EventLog log = new EventLog();;

        public MockWaiter(String name) {
                super(name);

        }

		@Override
		public void msgAtDestination() {
			// TODO Auto-generated method stub
			
		}

//		@Override
//		public void msgSitAtTable(Customer c, int table) {
//			// TODO Auto-generated method stub
//			
//		}

		@Override
		public void msgSetBreakBit() {
			// TODO Auto-generated method stub
			
		}
//
//		@Override
//		public void msgImReadyToOrder(Customer c) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void msgHereIsMyChoice(Customer c, String choice) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void msgOutOfFood(int tableNum, String choice) {
//			// TODO Auto-generated method stub
//			
//		}

//		@Override
//		public void msgOrderIsReady(Customer c, String choic, int table,
//				Dimension p) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void msgReadyToPay(Customer c) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void msgHereIsCheck(Customer c, double check) {
//			log.add("received msgHereIsCheck from cashier" + check);
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void msgLeavingTable(Customer c) {
//			// TODO Auto-generated method stub
//			
//		}

		@Override
		public void msgGoOnBreak() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgSitAtTable(restaurant.jack.interfaces.Customer c,
				int table) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgImReadyToOrder(restaurant.jack.interfaces.Customer c) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgHereIsMyChoice(restaurant.jack.interfaces.Customer c,
				String choice) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgReadyToPay(restaurant.jack.interfaces.Customer c) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgHereIsCheck(restaurant.jack.interfaces.Customer c,
				double check) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void msgLeavingTable(restaurant.jack.interfaces.Customer c) {
			// TODO Auto-generated method stub
			
		}

		
       

}