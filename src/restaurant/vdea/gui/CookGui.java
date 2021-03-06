package restaurant.vdea.gui;

import java.awt.*;
import java.util.concurrent.Semaphore;

import agent.gui.Gui;
import restaurant.vdea.CookRole;

public class CookGui implements Gui {

    private CookRole role = null;

    private int xPos = -20, yPos = 50;
    private int xDestination = -20, yDestination = 50;//default start position
    private int xHome, yHome;
    private Semaphore atTurn = new Semaphore(0, true);
    private enum Command {noCommand ,bottom};
	private Command command=Command.noCommand;
    
    private final int WINDOWX = 600;
    private final int WINDOWY = 490;
    
    private final int buffer = 15;
	private final int grillW = 82;
	private final int grillH = 25;
	private final int grillX = WINDOWX-grillW-buffer;
	private final int grillY = buffer+15;
	
	private final int platingW = 25;
	private final int platingH = 80;
	private final int platingX = grillX-platingW;
	private final int platingY = grillY+grillH;
	private final int plateY = platingY+1;
	private final int plateX = platingX+2;
	static final int grillNum = 4;
	//private List<Grill> grills = new ArrayList<Grill>();    
    private boolean[] cooking = new boolean[grillNum];
    private String[] grills = new String[grillNum];
    private boolean[] plating = new boolean[grillNum];
    private String[] plates = new String[grillNum];

    public CookGui(CookRole role) {
        this.role = role;
        
        for (int i = 0; i<grillNum; i++){
        	cooking[i] = false;
        	grills[i] = "";
        	plating[i] = false;
        	plates[i] = "";
        }
        
        xHome = 515;
		yHome = 50;
        
    }//135
    
    public void DoGoToCookStation(){
    	goToBottom();
    	try {
			atTurn.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	xDestination = xHome;
		yDestination = yHome;
    }
    private void goToBottom(){
    	xDestination = xHome;
		yDestination = 135;
		command = Command.bottom;
    }

    public void DoLeave(){
    	xDestination = -20;
		yDestination = -20;
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
		
		if(xPos == xDestination && yPos == yDestination){
			if (command == Command.bottom){
				atTurn.release();
			}
			
			command=Command.noCommand;
		}
    }
    
    public boolean isPresent() {
        return true;
    }


    public void DoCooking(String f, int n) {
        grills[n-1] = f;
        plates[n-1] = f;
        cooking[n-1] = true;
    }
    
    public void doneCooking(int t){
    	  grills[t-1] = "";
          cooking[t-1] = false;
          plating[t-1] = true;
          
    }
    
    public void collected(int t){
    	plating[t-1] = false;
    	plates[t-1] = "";
    }
    
    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }
    
    public void draw(Graphics2D g) {
        g.setColor(Color.blue);
        g.fillRect(xPos, yPos, 20, 20);
        
        g.setColor(Color.black);
        if(cooking[0]){
    		if(grills[0].equals("Krabby Patty")){
    			g.drawString("KP", grillX, grillY);
    		}
    		if(grills[0].equals("Kelp Shake")){
    			g.drawString("KS", grillX, grillY);
    		}
    		if(grills[0].equals("Coral Bits")){
    			g.drawString("CB", grillX, grillY);
    		}
    		if(grills[0].equals("Kelp Rings")){
    			g.drawString("KR", grillX, grillY);
    		}
    	}
        if(cooking[1]){
    		int grillX1 = grillX+20;
    		if(grills[1].equals("Krabby Patty")){
    			g.drawString("KP", grillX1, grillY);
    		}
    		if(grills[1].equals("Kelp Shake")){
    			g.drawString("KS", grillX1, grillY);
    		}
    		if(grills[1].equals("Coral Bits")){
    			g.drawString("CB", grillX1, grillY);
    		}
    		if(grills[1].equals("Kelp Rings")){
    			g.drawString("KR", grillX1, grillY);
    		}
    	}
        if(cooking[2]){
    		int grillX2 = grillX+40;
    		if(grills[2].equals("Krabby Patty")){
    			g.drawString("KP", grillX2, grillY);
    		}
    		if(grills[2].equals("Kelp Shake")){
    			g.drawString("KS", grillX2, grillY);
    		}
    		if(grills[2].equals("Coral Bits")){
    			g.drawString("CB", grillX2, grillY);
    		}
    		if(grills[2].equals("Kelp Rings")){
    			g.drawString("KR", grillX2, grillY);
    		}
    	}
        if(cooking[3]){
    		int grillX3 = grillX+60;
    		if(grills[3].equals("Krabby Patty")){
    			g.drawString("KP", grillX3, grillY);
    		}
    		if(grills[3].equals("Kelp Shake")){
    			g.drawString("KS", grillX3, grillY);
    		}
    		if(grills[3].equals("Coral Bits")){
    			g.drawString("CB", grillX3, grillY);
    		}
    		if(grills[3].equals("Kelp Rings")){
    			g.drawString("KR", grillX3, grillY);
    		}
    	}
        
        if(plating[0]){
    		if(plates[0].equals("Krabby Patty")){
    			g.drawString("KP", plateX, plateY);
    		}
    		if(plates[0].equals("Kelp Shake")){
    			g.drawString("KS", plateX, plateY);
    		}
    		if(plates[0].equals("Coral Bits")){
    			g.drawString("CB", plateX, plateY);
    		}
    		if(plates[0].equals("Kelp Rings")){
    			g.drawString("KR", plateX, plateY);
    		}
    	}
        if(plating[1]){
    		int plateY1 = plateY+20;
    		if(plates[1].equals("Krabby Patty")){
    			g.drawString("KP", plateX, plateY1);
    		}
    		if(plates[1].equals("Kelp Shake")){
    			g.drawString("KS", plateX, plateY1);
    		}
    		if(plates[1].equals("Coral Bits")){
    			g.drawString("CB", plateX, plateY1);
    		}
    		if(plates[1].equals("Kelp Rings")){
    			g.drawString("KR", plateX, plateY1);
    		}
    	}
        if(plating[2]){
    		int plateY2 = plateY+40;
    		if(plates[2].equals("Krabby Patty")){
    			g.drawString("KP", plateX, plateY2);
    		}
    		if(plates[2].equals("Kelp Shake")){
    			g.drawString("KS", plateX, plateY2);
    		}
    		if(plates[2].equals("Coral Bits")){
    			g.drawString("CB", plateX, plateY2);
    		}
    		if(plates[2].equals("Kelp Rings")){
    			g.drawString("KR", plateX, plateY2);
    		}
    	}
        if(plating[3]){
    		int plateY3 = plateY+60;
    		if(plates[3].equals("Krabby Patty")){
    			g.drawString("KP", plateX, plateY3);
    		}
    		if(plates[3].equals("Kelp Shake")){
    			g.drawString("KS", plateX, plateY3);
    		}
    		if(plates[3].equals("Coral Bits")){
    			g.drawString("CB", plateX, plateY3);
    		}
    		if(plates[3].equals("Kelp Rings")){
    			g.drawString("KR", plateX, plateY3);
    		}
    	}

    }
   
}