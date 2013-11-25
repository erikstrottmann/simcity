package market.interfaces;
import java.util.List;

import CommonSimpleClasses.CityLocation;
import market.Item;


public interface DeliveryGuy {
	
	// Messages
		public abstract boolean msgAreYouAvailable();
	
		public abstract void msgDeliverIt(List<Item> DeliveryList, Customer OrdePerson, CityLocation building);
		
		public abstract void msgArrivedDestination();
		
		public abstract void msgLeaveWork();
	
	//Utilities
		public abstract String getMaitreDName();
		public abstract String getName();
		public abstract void setCashier(Cashier ca);

	//Animations
		public abstract void Ready();
		public abstract void AtExit();
		public abstract void AtDeliverExit();
}