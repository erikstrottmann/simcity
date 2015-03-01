package restaurant.jack.gui;

import java.awt.Color;
import java.awt.Graphics2D;


import restaurant.jack.CustomerRole;

import agent.gui.Gui;

public class CustomerGui implements Gui {
	
	private CustomerRole agent = null;
	private boolean isPresent = false;
	private boolean isHungry = false;

	String name = "";
	//private HostAgent host;
	RestaurantGui gui;

	private int xPos, yPos;
	private int xDestination, yDestination;
	private enum Command {noCommand, GoToSeat, LeaveRestaurant};
	private Command command=Command.noCommand;
	
	String choiceDisplay = "";

	public static final int xTable = 200;
	public static final int yTable = 250;
	public static final int agentDim = 20;

	public CustomerGui(CustomerRole c, RestaurantGui gui){ //HostAgent m) {
		agent = c;
		xPos = -20;
		yPos = 0;
		xDestination = -20;
		yDestination = 0;
		//maitreD = m;
		this.gui = gui;
	}

	public void updatePosition() {
		if (xPos < xDestination)
			xPos++;
		else if (xPos > xDestination)
			xPos--;

		if (yPos < yDestination)
			yPos++;
		else if (yPos > yDestination)
			yPos--;

		if (xPos == xDestination && yPos == yDestination) {
			if (command==Command.GoToSeat) agent.msgAnimationFinishedGoToSeat();
			else if (command==Command.LeaveRestaurant) {
				agent.msgAnimationFinishedLeaveRestaurant();
				System.out.println("about to call .setCustomerEnabled(agent);");
				isHungry = false;
//				gui.setCustomerEnabled(agent);//TODO check this to make sure its not important
			}
			command=Command.noCommand;
		}
	}

	public void draw(Graphics2D g) {//abstract definition, needed for Graphics
		g.setColor(Color.RED);
		g.fillRect(xPos, yPos, agentDim, agentDim);
		
		if(!choiceDisplay.equals("")) {
			g.drawString(choiceDisplay, xPos, yPos);
		}
		
		g.setColor(Color.black);
		g.drawString(name, xPos, yPos + 10);
		

	}
	
	public void setName(String str) {
		name = str;
	}
	
	public void displayChoice(String choice) {
		choiceDisplay = choice;
	}
	
	public void drawLetter(Graphics2D g, String letter) {
		g.setColor(Color.black);
		g.drawString(letter, xPos+10, yPos+10);
		
	}

	public boolean isPresent() {
		return isPresent;
	}
	public void setHungry(int factor) {//TODO int waitposition
		isHungry = true;
		agent.gotHungry();
		setPresent(true);
		if(factor < 4) {
			xDestination = 0;
			yDestination = (20 * factor) + factor;
			return;
		}
		if (factor == 4) {
			xDestination = 21;
			yDestination = 21;
			return;
		}
		if (factor == 5) {
			xDestination = 21;
			yDestination = 63;
			return;
		}
		if(factor > 5) {
			int num = factor - 5;
			xDestination = 42;
			yDestination = ((20 * num) + num);
			return;
		}
		
	}
	public void updateWaitPosition(int factor) {
		xDestination = 20;
		yDestination = (20 * factor) + factor;
	}
	public boolean isHungry() {
		return isHungry;
	}

	public void setPresent(boolean p) {
		isPresent = p;
	}

	public void DoGoToSeat(int x, int y) {//later you will map seatnumber to table coordinates. and stuff
		System.out.println("SOWHA");
		xDestination = x;
		yDestination = y;
		command = Command.GoToSeat;
	}

	public void DoExitRestaurant() {
		xDestination = -40;
		yDestination = -40;
		command = Command.LeaveRestaurant;
	}

	
}