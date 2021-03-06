package mock;

import CommonSimpleClasses.ScheduleTaskInterface;

/**
 * Upon completion of the ScheduleTask event, MockScheduleTaskListener will
 * notify the test that should be waiting for a response.
 * @author Zach VP
 *
 */
public class MockScheduleTaskListener  {
	private ScheduleTaskInterface Event;
	public EventLog log = new EventLog();
	
	public MockScheduleTaskListener() {
		// TODO Auto-generated constructor stub
	}

	public void taskFinished(ScheduleTaskInterface Event) {
		this.Event = Event;
		
		synchronized(this) {
			notifyAll();
		}
	}
	
	public ScheduleTaskInterface getAsynchronousRequest(){
		return Event;
	}

}
