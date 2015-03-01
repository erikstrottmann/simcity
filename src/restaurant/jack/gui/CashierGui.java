


package restaurant.jack.gui;



import java.awt.Color;
import java.awt.Graphics2D;

import restaurant.jack.CashierRole;
import restaurant.jack.HostRole;

import agent.gui.Gui;


public class CashierGui implements Gui {

    private CashierRole agent = null;

    String name = "";

    private int xPos = -20, yPos = -20;//default waiter position
    private int xDestination = -20, yDestination = -20;//default start position

    public static final int xTable = 200;
    public static final int yTable = 250;
    public static final int agentDim = 20;
    
    private int deskX = 400;
    private int deskY = 0;

    int entranceX = -30; 
    int entranceY = 0;

    boolean canRelease = false;
    
    public CashierGui(CashierRole agent) {
        this.agent = agent;
        xPos = entranceX;
        yPos = entranceY;
        xDestination = entranceX;
        yDestination = entranceY;
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
        if((xPos == -20 && yPos == -20)) {	//boolean
        	//agent.msgAtDesk();
        }
        if (xPos == xDestination && yPos == yDestination
        		& (xDestination >= 0) & (yDestination >= 0)) {
        	if(canRelease)
        		atDestination();
        }
        //if(xPos == xDestination && yPos == yDestination & (xDestination <0) & (yDestination < 0)) {

    }

    public void draw(Graphics2D g) {
        g.setColor(Color.RED);
        g.fillRect(xPos, yPos, agentDim, agentDim);
        
		g.setColor(Color.black);
		g.drawString(name, xPos, yPos + 10);
		
		
    }

    public boolean isPresent() {
        return true;
    }
    
	public void setName(String str){
		name = str;
	}

    public void DoBringToTable(int x, int y) {
        xDestination = x + agentDim;
        yDestination = y - agentDim;
    }
    
    public void DoDirectAWaiter(){
    	canRelease = true;
    	xDestination = deskX + 30;
    	yDestination = deskY;
    }
    
    public void DoLeaveRestaurant() {
    	canRelease = true;
    	xDestination = entranceX;
    	yDestination = entranceY;
    }
    
    public void DoGoToDesk() {
    	canRelease = true;
    	xDestination = deskX;
    	yDestination = deskY;
    }
    
    public void DoGoToLoc(int x, int y) {
        xDestination = x + agentDim;
        yDestination = y - agentDim;
    }

    public void DoLeaveCustomer() {
        xDestination = -agentDim;
        yDestination = -agentDim;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }
    
    public void atDestination() {
    	canRelease = false;
    	agent.msgAtDestination();
    	
    }
}
