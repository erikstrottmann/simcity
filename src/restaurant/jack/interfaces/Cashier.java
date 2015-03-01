package restaurant.jack.interfaces;

/**
 * A sample Customer interface built to unit test a CashierAgent.
 *
 * @author Jack Lucas
 *
 */
public interface Cashier extends market.interfaces.PhonePayer {

        public abstract void msgComputeBill(Customer c, String choice, Waiter w);

        public abstract void msgHereIsMyPayment(Customer c, double cash);
        
        public abstract void msgRequestPayment(Market m, double amount);
        
}