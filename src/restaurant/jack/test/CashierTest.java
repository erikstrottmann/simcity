package restaurant.jack.test;


import gui.Building;
import CommonSimpleClasses.CityLocation;
import agent.PersonAgent;
import agent.interfaces.Person;
import restaurant.jack.CashierRole;
import restaurant.jack.CashierRole.customerState;
import restaurant.jack.gui.RestaurantLucasBuilding;
import restaurant.jack.test.mock.MockCustomer;
import restaurant.jack.test.mock.MockMarket;
import restaurant.jack.test.mock.MockWaiter;
import junit.framework.TestCase;

/**
 * 
 *
 */
public class CashierTest extends TestCase
{
        //these are instantiated for each test separately via the setUp() method.
        CashierRole cashier;
        MockWaiter waiter;
        MockCustomer customer1;
        MockCustomer customer2;
        MockMarket market1;
        MockMarket market2;
        
        
        /**
         * This method is run before each test. You can use it to instantiate the class variables
         * for your agent and mocks, etc.
         */
        public void setUp() throws Exception{
                super.setUp();  
                Person person = new PersonAgent("J");
                CityLocation loc = new RestaurantLucasBuilding(0,0,0,0);
                cashier = new CashierRole(person, loc);                
                customer1 = new MockCustomer("mockcustomer1"); 
                customer2 = new MockCustomer("mockcustomer2");
                waiter = new MockWaiter("mockwaiter");
                market1 = new MockMarket("mockmarket1");
                market2 = new MockMarket("mockmarket2");
        }        
        /**
         * This tests the cashier under very simple terms: one customer is ready to pay the exact bill.
         */
        
        public void testInitializationTest() { //tests to ensure Cashier is constructed properly //CHANGE
//        	  customer1.cashier = cashier;//You can do almost anything in a unit test.                        
              
              //check preconditions
              assertEquals("Cashier should have 0 customers in it. It doesn't.",cashier.getMyCustomers().size(), 0); 
              assertEquals("Cashier did not start with 500 money", cashier.getMoney(),500.0);//CHANGE
              assertEquals("marketPayments list did not start empty", cashier.getMarketPayments().size(), 0);
              assertEquals("myCustomers list did not start at size zero", cashier.getMyCustomers().size(), 0);
              assertEquals("menuMap did not start with 4 items in it", cashier.getPriceMenu().size(), 4);
        }
        
        public void testOneNormalCustomerScenario()
        {
        	assertEquals("myCustomers list should start at zero", cashier.getMyCustomers().size(), 0);
        	//cashier will FIRST be sent message from waiter, 
        	//then will check sched,
        	//then will perform action calculateBill
        	////test for price being correct then new msg sent to waiter
            cashier.msgComputeBill(customer1, "Krabby Patty", waiter);
            assertEquals("size of myCustomers list is not 1", cashier.getMyCustomers().size(), 1);
            assertEquals("myCustomer list has stored incorrect customer", cashier.getMyCustomers().get(0).getCustomer(), customer1);
            assertEquals("muCustomer list has stored incorrect waiter", cashier.getMyCustomers().get(0).getWaiter(), waiter);
            assertTrue("myCustomer list has stored wrong choice", cashier.getMyCustomers().get(0).getChoice().equals("Krabby Patty"));
            assertEquals("myCustomer list has stored wrong payment", cashier.getMyCustomers().get(0).getPayment(), 0.0);
            assertEquals("myCustomer has stored incorrect state", cashier.getMyCustomers().get(0).getState(), customerState.doneEating);
            
        	//INTERLEAVING OF OTHER MESSAGE TO SHOW IT DOESNT DISRUPT CASHIER
            cashier.msgRequestPayment(market1, 7);
            assertTrue("Cashier's marketPayments list should not be empty after receiving Market's message", cashier.getMarketPayments().size()== 1);
            assertTrue("marketPayment created does not have correct market in it", cashier.getMarketPayments().get(0).getMarket() == market1 );
            assertTrue("marketPayment amount is incorrect", cashier.getMarketPayments().get(0).getAmount() == 7);
            cashier.msgRequestPayment(market2, 23);
            assertTrue("Cashier's marketPayments list should not be empty after receiving Market's message", cashier.getMarketPayments().size()== 2);
            assertTrue("marketPayment created does not have correct market in it", cashier.getMarketPayments().get(1).getMarket() == market2 );
            assertTrue("marketPayment amount is incorrect", cashier.getMarketPayments().get(1).getAmount() == 23);
            //END INTERLEAVING
            
            //Test Scheduler
            
            assertEquals("Cashier scheduler not returning true", cashier.pickAndExecuteAnAction(), true);
            assertEquals("myCustomer has stored incorrect state", cashier.getMyCustomers().get(0).getState(), customerState.waitingForCheck);
            //Test action and message sending 
            assertTrue("waiter did not receive message", waiter.log.containsString("received msgHereIsCheck from cashier" + 15.99));
            
            cashier.msgHereIsMyPayment(customer1, 15.99);
            
            assertEquals("incorrect size of myCustomer list", cashier.getMyCustomers().size(), 1);
            assertEquals("cash amount incorrectly transferred to MyCustomer", cashier.getMyCustomers().get(0).getPayment(), 15.99);
            assertEquals("MyCustomer is in wrong state", cashier.getMyCustomers().get(0).getState(), customerState.paying);
            
            assertTrue("scheduler not returning true", cashier.pickAndExecuteAnAction());
            
            assertTrue("customer did not receive msgHereIsChange", customer1.log.containsString("I have received change " + 0));
            assertEquals("Cashier's money not updated with what customer paid", cashier.getMoney(), 1015.99);
            assertEquals("MyCustomer in incorrect state", cashier.getMyCustomers().get(0).getState(), customerState.leaving);
            
            
            
            
        	
        	
        	//setUp() runs first before this test!
                
                    //TODO check how to get cashier log when he is not a mock         
//                assertEquals("CashierAgent should have an empty event log before the Cashier's HereIsBill is called. Instead, the Cashier's event log reads: "
//                                                + cashier.log.toString(), 0, cashier.log.size());
        }
        
        public void testMultipleCustomerScenario() {//tests cashier working with multiple customers simultaneously
        	assertEquals("myCustomers list should start at zero", cashier.getMyCustomers().size(), 0);
        	cashier.msgComputeBill(customer1, "Rice", waiter);
        	assertEquals("size of myCustomers list is not 1", cashier.getMyCustomers().size(), 1);
        	cashier.msgComputeBill(customer2, "Sushi", waiter);
        	assertEquals("size of myCustomers list is not 2", cashier.getMyCustomers().size(), 2);
        	assertEquals("myCustomer list has stored incorrect customer", cashier.getMyCustomers().get(0).getCustomer(), customer1);
        	assertEquals("myCustomer list has stored incorrect customer", cashier.getMyCustomers().get(1).getCustomer(), customer2);
        	assertEquals("muCustomer list has stored incorrect waiter", cashier.getMyCustomers().get(0).getWaiter(), waiter);
        	assertEquals("muCustomer list has stored incorrect waiter", cashier.getMyCustomers().get(1).getWaiter(), waiter);
        	
        	assertTrue("myCustomer list has stored wrong choice", cashier.getMyCustomers().get(0).getChoice().equals("Rice"));
        	assertTrue("myCustomer list has stored wrong choice", cashier.getMyCustomers().get(1).getChoice().equals("Sushi"));
        	
        	assertEquals("myCustomer list has stored wrong payment", cashier.getMyCustomers().get(0).getPayment(), 0.0);
        	assertEquals("myCustomer list has stored wrong payment", cashier.getMyCustomers().get(1).getPayment(), 0.0);
        	
        	assertEquals("myCustomer has stored incorrect state", cashier.getMyCustomers().get(0).getState(), customerState.doneEating);
        	assertEquals("myCustomer has stored incorrect state", cashier.getMyCustomers().get(1).getState(), customerState.doneEating);
        	
        	//INTERLEAVING OF OTHER MESSAGE TO SHOW IT DOESNT DISRUPT CASHIER
            cashier.msgRequestPayment(market1, 7);
            assertTrue("Cashier's marketPayments list should not be empty after receiving Market's message", cashier.getMarketPayments().size()== 1);
            assertTrue("marketPayment created does not have correct market in it", cashier.getMarketPayments().get(0).getMarket() == market1 );
            assertTrue("marketPayment amount is incorrect", cashier.getMarketPayments().get(0).getAmount() == 7);
            cashier.msgRequestPayment(market2, 23);
            assertTrue("Cashier's marketPayments list should not be empty after receiving Market's message", cashier.getMarketPayments().size()== 2);
            assertTrue("marketPayment created does not have correct market in it", cashier.getMarketPayments().get(1).getMarket() == market2 );
            assertTrue("marketPayment amount is incorrect", cashier.getMarketPayments().get(1).getAmount() == 23);
            //END INTERLEAVING
            
        	 assertEquals("Cashier scheduler not returning true", cashier.pickAndExecuteAnAction(), true);
        	 assertEquals("myCustomer has stored incorrect state", cashier.getMyCustomers().get(0).getState(), customerState.waitingForCheck);
        	 assertTrue("waiter did not receive message", waiter.log.containsString("received msgHereIsCheck from cashier" + 10.99));

        	 
        	 assertEquals("Cashier scheduler not returning true", cashier.pickAndExecuteAnAction(), true);
        	 assertEquals("myCustomer has stored incorrect state", cashier.getMyCustomers().get(1).getState(), customerState.waitingForCheck);
        	 assertTrue("waiter did not receive message", waiter.log.containsString("received msgHereIsCheck from cashier" + 5.99));

        	 
        	 cashier.msgHereIsMyPayment(customer1, 10.99);
        	 cashier.msgHereIsMyPayment(customer2, 5.99);
        	 
        	 
        	 assertEquals("incorrect size of myCustomer list", cashier.getMyCustomers().size(), 2);
        	 assertEquals("cash amount incorrectly transferred to MyCustomer", cashier.getMyCustomers().get(0).getPayment(), 10.99);
        	 assertEquals("cash amount incorrectly transferred to MyCustomer", cashier.getMyCustomers().get(1).getPayment(), 5.99);

        	 assertEquals("MyCustomer is in wrong state", cashier.getMyCustomers().get(0).getState(), customerState.paying);
        	 assertEquals("MyCustomer is in wrong state", cashier.getMyCustomers().get(1).getState(), customerState.paying);
        	 
        	 assertTrue("scheduler not returning true", cashier.pickAndExecuteAnAction());
             assertEquals("Cashier's money not updated with what customer paid", cashier.getMoney(), 1010.99);
        	 
        	 assertTrue("customer did not receive msgHereIsChange", customer1.log.containsString("I have received change " + 0));
             assertEquals("MyCustomer in incorrect state", cashier.getMyCustomers().get(0).getState(), customerState.leaving);
             
             assertTrue("scheduler not returning true", cashier.pickAndExecuteAnAction());
             assertEquals("Cashier's money not updated with what customer paid", cashier.getMoney(), 1016.98);
             
             assertTrue("customer did not receive msgHereIsChange", customer2.log.containsString("I have received change " + 0));
             assertEquals("MyCustomer in incorrect state", cashier.getMyCustomers().get(1).getState(), customerState.leaving);
        }
        
        public void testOneMarketInteraction() {
            //(1) market sends message msgRequestPayment to cashier
            //(2) cashier receives message, and a new MarketPayemtn is created with market m and amount
            //(3) scheduler is activated, returns true
            //(4) cashier sends message msgHereIsPayment to Market m
            //(5) Used MarketPayment is removed, marketPayments list is empty
            
            cashier.msgRequestPayment(market1, 7);
          
            
            //STEP 1 OF TEST
            assertTrue("Cashier's marketPayments list should not be empty after receiving Market's message", cashier.getMarketPayments().size()== 1);
            assertTrue("Cashier should start with 1000 money, but did not", cashier.getMoney() == 1000);

            //STEP 2 
            assertTrue("marketPayment created does not have correct market in it", cashier.getMarketPayments().get(0).getMarket() == market1 );
            assertTrue("marketPayment amount is incorrect", cashier.getMarketPayments().get(0).getAmount() == 7);
            //STEP 3
            assertTrue("Cashier scheduler not returning true", cashier.pickAndExecuteAnAction());
            assertTrue("Money was not taken correctly out of cashier's money", cashier.getMoney() == 993);
            //STEP 5
            assertTrue("Market did not receive correct payment", market1.log.containsString("I have received payment of amount " + 7));
            assertTrue("Size of marketPayments is not 0 when it should be", cashier.getMarketPayments().isEmpty());
            
            
           
            
            
            
        }
        
        public void testTwoMarketInteraction() {
        	   
            cashier.msgRequestPayment(market1, 7);
            cashier.msgRequestPayment(market2, 14);
            
            //STEP 1 OF TEST
            assertTrue("Cashier's marketPayments list should not be empty after receiving Market's message", cashier.getMarketPayments().size()== 2);
            assertTrue("Cashier should start with 1000 money, but did not", cashier.getMoney() == 1000);

            //STEP 2 
            assertTrue("marketPayment created does not have correct market in it", cashier.getMarketPayments().get(0).getMarket() == market1 );
            assertTrue("marketPayment created does not have correct market in it", cashier.getMarketPayments().get(1).getMarket() == market2 );
            assertTrue("marketPayment amount is incorrect", cashier.getMarketPayments().get(0).getAmount() == 7);
            assertTrue("marketPayment amount is incorrect", cashier.getMarketPayments().get(1).getAmount() == 14);
            //STEP 3
            assertTrue("Cashier scheduler not returning true", cashier.pickAndExecuteAnAction());
            assertTrue("Money was not taken correctly out of cashier's money", cashier.getMoney() == 993);
            assertTrue("Market did not receive correct payment", market1.log.containsString("I have received payment of amount " + 7));
            assertTrue("Size of MarketPayments should have been reduced", cashier.getMarketPayments().size() == 1);
            //STEP 5
            assertTrue("Cashier scheduler not returning true", cashier.pickAndExecuteAnAction());
            assertTrue("Money was not taken correctly out of cashier's money", cashier.getMoney() == 979);
            assertTrue("Market did not receive correct payment", market2.log.containsString("I have received payment of amount " + 14));
            assertTrue("Size of marketPayments is not 0 when it should be", cashier.getMarketPayments().isEmpty());
           
        }
        
        public void testNotEnoughMoneyToPayMarket() {
        	  cashier.msgRequestPayment(market1, 2000);
              
              
              //STEP 1 OF TEST
              assertTrue("Cashier's marketPayments list should not be empty after receiving Market's message", cashier.getMarketPayments().size()== 1);
              assertTrue("Cashier should start with 1000 money, but did not", cashier.getMoney() == 1000);

              //STEP 2 
              assertTrue("marketPayment created does not have correct market in it", cashier.getMarketPayments().get(0).getMarket() == market1 );
              assertTrue("marketPayment amount is incorrect", cashier.getMarketPayments().get(0).getAmount() == 2000);
              //STEP 3
              assertTrue("Cashier scheduler not returning true", cashier.pickAndExecuteAnAction());
              assertEquals("Money was not taken correctly out of cashier's money", cashier.getMoney(), 0.0);
              //STEP 5
              assertTrue("Market did not receive correct payment", market1.log.containsString("I have received payment of amount " + 1000));
              assertTrue("Size of marketPayments is not 0 when it should be", cashier.getMarketPayments().isEmpty());
              
              
        }
                
}