


package restaurant.lucas.gui;



import java.awt.Color;
import java.awt.Graphics2D;

import restaurant.lucas.HostRole;

import agent.gui.Gui;


public class HostGui implements Gui {

    private HostRole agent = null;


    private int xPos = -20, yPos = -20;//default waiter position
    private int xDestination = -20, yDestination = -20;//default start position

    public static final int xTable = 200;
    public static final int yTable = 250;
    public static final int agentDim = 20;
    
    private int deskX;
    private int deskY;

    public HostGui(HostRole agent) {
        this.agent = agent;
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
           //agent.msgAtTable();
        }
        //if(xPos == xDestination && yPos == yDestination & (xDestination <0) & (yDestination < 0)) {

    }

    public void draw(Graphics2D g) {
        g.setColor(Color.MAGENTA);
        g.fillRect(xPos, yPos, agentDim, agentDim);
    }

    public boolean isPresent() {
        return true;
    }

    public void DoBringToTable(int x, int y) {
        xDestination = x + agentDim;
        yDestination = y - agentDim;
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
}
