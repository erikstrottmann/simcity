package gui.trace;

/**
 * These enums represent tags that group alerts together.  <br><br>
 * 
 * This is a separate idea from the {@link AlertLevel}.
 * A tag would group all messages from a similar source.  Examples could be: BANK_TELLER, RESTAURANT_ONE_WAITER,
 * or PERSON.  This way, the trace panel can sort through and identify all of the alerts generated in a specific group.
 * The trace panel then uses this information to decide what to display, which can be toggled.  You could have all of
 * the bank tellers be tagged as a "BANK_TELLER" group so you could turn messages from tellers on and off.
 * 
 * @author Keith DeRuiter
 *
 */
public enum AlertTag {
	PASSENGER,
	TRANSPORTATION,
	RESTAURANT,		//For the demo code where you make a new restaurant
	BANK,			//For the demo code where you make a new bank
	MARKET,
	HOUSING
}
