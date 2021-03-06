package agent;

import classifieds.ClassifiedsClass;
import gui.Building;
import housing.interfaces.Dwelling;
import agent.interfaces.Person;
import CommonSimpleClasses.CityLocation;
import CommonSimpleClasses.Constants;
import CommonSimpleClasses.ScheduleTask;
import CommonSimpleClasses.TimeManager;

/**
 * Common interface for jobs that people can work at. Provides a couple of
 * convenience methods for finding the next time work starts.
 * 
 * @author Erik Strottmann
 */
public abstract class WorkRole extends Role {
	
	TimeManager tm = TimeManager.getInstance();
	
	public WorkRole() {
		super();
		initShift();
		ClassifiedsClass.getClassifiedsInstance().addWorkRole(this);
	}
	
	public WorkRole(Person person) {
		super(person);
		initShift();
		ClassifiedsClass.getClassifiedsInstance().addWorkRole(this);
	}
	
	public WorkRole(Person person, CityLocation loc) {
		super(person, loc);
		initShift();
		ClassifiedsClass.getClassifiedsInstance().addWorkRole(this);
	}
	
	public WorkRole(CityLocation loc) {
		super(null, loc);
		initShift();
		ClassifiedsClass.getClassifiedsInstance().addWorkRole(this);
	}
	
	public String getShortName() {
		return getClass().getSimpleName();
	}
	
	public void setPerson(Person person) {
		super.setPerson(person);
	}
	
	public void initShift() {
		int initHour = (getShiftStartHour() - getWorkStartThresholdHours());
		int initMin = (getShiftStartMinute() - getWorkStartThresholdMinutes());
		
		ScheduleTask task = ScheduleTask.getInstance();
		
		Runnable command = new Runnable(){
			@Override
			public void run() {
				stateChanged();
			}
			
		};
		
		
		task.scheduleDailyTask(command, initHour, initMin);
		//get start time substract thresh hours times const.hour and thresh minute * const.minute 
		//create a daily task with new time i have with a delay of Constants.DAY / (llok at how other scheds are done)
	}
	
	public int getWorkStartThresholdHours() {
		return 2;
	}
	
	public int getWorkStartThresholdMinutes() {
		return 0;
	}
	/**
	 * The hour of day this person's work shift starts, in 24-hour time.
	 * @return an integer in the range [0,23]
	 */
	public int getShiftStartHour(){
		return ( ((Building) this.location).getOpeningHour());
	}
	/**
	 * The minute of the hour this person's work shift starts.
	 * @return an integer in the range [0,59]
	 */
	public int getShiftStartMinute(){
		return ( ((Building) this.location).getOpeningMinute());
	}
	/**
	 * The hour of day this person's work shift ends, in 24-hour time.
	 * @return an integer in the range [0,23]
	 */
	public int getShiftEndHour(){
		return ( ((Building) this.location).getClosingHour());
	}
	/**
	 * The minute of the hour this person's work shift ends.
	 * @return an integer in the range [0,59]
	 */
	public int getShiftEndMinute(){
		return ( ((Building) this.location).getClosingMinute());
	}
	
	/**
	 * Whether this person's shift starts before midnight and ends after
	 * midnight
	 */
	public boolean worksThroughMidnight() {
		return getShiftStartHour() > getShiftEndHour() || (getShiftStartHour() == getShiftEndHour()
				&& getShiftStartMinute() > getShiftEndMinute());
	}
	
	/** Whether the person is currently at work */
	public abstract boolean isAtWork();
	/**
	 * Whether the person is on a break from work (implies returning to work
	 * soon)
	 */
	public abstract boolean isOnBreak();
	
	/**
	 * The duration of work, in milliseconds. Accounts for daily and hourly
	 * overflow, but assumes that no single shift is longer than 23 hours and
	 * 59 minutes.
	 */
	public long workDuration() {
		int durationHours = getShiftEndHour() - getShiftStartHour();
		int durationMinutes = getShiftEndMinute() - getShiftStartMinute();
		
		// Account for overflow at midnight, if necessary
		if (worksThroughMidnight()) {
			durationHours += 24;
		}
		// Account for hourly overflow, if necessary
		if (getShiftStartMinute() > getShiftEndMinute()) {
			durationMinutes += 60;
			durationHours -= 1;
		}
		
		return durationHours * Constants.HOUR +
				durationMinutes * Constants.MINUTE;
	}
	
	/** The time the next shift starts, in milliseconds since the epoch. */
	public long nextShiftStartTime() {
		return tm.nextSuchTime(getShiftStartHour(), getShiftStartMinute());
	}
	/** The time the next shift ends, in milliseconds since the epoch. */
	public long nextShiftEndTime() {
		return TimeManager.nextSuchTime(nextShiftStartTime(),
				getShiftEndHour(), getShiftEndMinute());
	}
	/**
	 * The time the current or previous shift started, in milliseconds since
	 * the epoch.
	 */
	public long previousShiftStartTime() {
		return tm.previousSuchTime(getShiftStartHour(), getShiftStartMinute());
	}
	/**
	 * The time the current or previous shift ends or will end, in milliseconds
	 * since the epoch.
	 */
	public long previousShiftEndTime() {
		return TimeManager.nextSuchTime(previousShiftStartTime(),
				getShiftEndHour(), getShiftEndMinute());
	}
	
	/** Whether the person should be at work right now. */
	public boolean shouldBeAtWork() {
		return tm.isNowBetween(previousShiftStartTime(),
				previousShiftEndTime());
	}
	
	/**
	 * Whether the person is late for work. That is, whether the person should
	 * be at work, but isn't.
	 */
	public boolean isLate() {
		return !isAtWork() && !isOnBreak() && shouldBeAtWork(); 
	}
	
	/**
	 * Returns the start time (in milliseconds since the epoch) of the current
	 * or next shift.
	 */
	public long startTime() {
		if (shouldBeAtWork()) {
			return previousShiftStartTime();
		} else {
			return nextShiftStartTime();
		}
	}
	
	/**
	 * Ends the work day as soon as possible - for example, when a Waiter
	 * finishes helping all customers.
	 */
	public abstract void msgLeaveWork();
}
