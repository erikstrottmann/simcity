package restaurant.vegaperk.gui;


import restaurant.vegaperk.backend.WaiterRoleBase;

import java.awt.*;

import javax.swing.ImageIcon;

import agent.gui.Gui;

public class WaiterGui implements Gui {

    private WaiterRoleBase agent = null;
    private boolean canRelease = false;//this prevents excessive releases from occurring
    private boolean waiting = false;//checks if waiter is going to waiting zone
    
    private boolean isPresent = false;
    
    String imgSrc = "resource/spongebob.png";
    Image img = new ImageIcon(imgSrc).getImage();
    
    String orderName = "Test";//text of customer order
    private boolean holdingOrder = false;

    private int xPos = 0, yPos = 0;//default waiter position
    private int xDestination = 0, yDestination = 0;//default start position

    private static final int hostX = 0;
    private static final int hostY = 0;
    
    private static final int cookX = 420;
    private static final int cookY = 50;
    
    private static final int homeX = 100;
    private static final int homeY = 50;
    
    private static final int cashX = 260;
    private static final int cashY = 40;
    
    private RestaurantGui gui;
    
    public WaiterGui(WaiterRoleBase w, RestaurantGui g) {
        this.agent = w;
        this.gui = g;
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

        if (xPos == xDestination && yPos == yDestination && canRelease && !waiting ) {
        	canRelease = false;
        	agent.msgAtDest();
        }
    }

    public void draw(Graphics2D g) {
    	g.setColor(Color.GRAY);
    	g.fillRect(xPos, yPos, 20, 20);
    	g.drawImage(img, xPos, yPos, null);
    	
    	if(holdingOrder && orderName != null){
    		g.setColor(Color.BLACK);
    		g.drawString(orderName, xPos, yPos);
    	}
    }

    public boolean isPresent() {
        return isPresent;
    }
    
    public void setPresent(boolean p) {
    	this.isPresent = p;
    }
    
    public void setFatigued(){
    	System.out.println("I'm tired!");
    	agent.msgGotFatigue();
    }
    
    public void offBreak(){
    	agent.msgOffBreak();
    }
    
    public void denyBreak(){
    	gui.setWaiterEnabled(agent);
    }
    
    public WaiterRoleBase getAgent(){
    	return agent;
    }

    public void DoGoToTable(int x, int y) {
    	waiting = false;
        xDestination = x + 20;
        yDestination = y - 20;
        canRelease = true;
    }

    public void DoGoToHost() {
    	waiting = false;
        xDestination = hostX;
        yDestination = hostY;
        canRelease = true;
    }
    
    public void DoGoToHomePosition(int positionSlot) {
        xDestination = homeX + 30*positionSlot;
        yDestination = homeY;
        waiting = true;
    }
    
    public void DoGoToCook(){
    	waiting = false;
        xDestination = cookX;
        yDestination = cookY;
        canRelease = true;
    }
    
    public void DoGoToCenter(){
    	waiting = false;
        xDestination = cashX;
        canRelease = true;
    }
    
    public void DoGoToCashier(){
    	waiting = false;
        xDestination = cashX;
        yDestination = cashY;
        canRelease = true;
    }
    
    public void DoGoOnBreak(){
    	waiting = false;
        xDestination = 30;
        yDestination = 170;
        canRelease = false;
    }
    
    public void DoLeaveWork(){
    	waiting = false;
        xDestination = -20;
        yDestination = -20;
        canRelease = false;
    }
    
    public void setOrderName(String n){
    	orderName = n;
    }
    
    public void toggleHoldingOrder(){
    	if(holdingOrder==true){
    		holdingOrder = false;
    		return;
    	}
    	holdingOrder = true;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }
}
