package market.gui;



import market.interfaces.*;
import market.ItemCollectorRole;







import java.awt.*;

import agent.gui.Gui;

public class ItemCollectorGui implements Gui, ItemCollectorGuiInterfaces {

    private ItemCollector agent = null;

    private int xPos = 130, yPos = -30;//default cashier position
    private int xDestination = 130, yDestination = -30;//default start position
    
    private static final int ItemCollectorWidth = 10;
    private static final int ItemCollectorHeight = 10;
    
    private String currentTask = "??";
    
    private  int HomePosX = 195;
    private  int HomePosY = 200;
    
    private  int CollectItemX = 220;
    private  int CollectItemY = 260;
    
    private static final int ExitX1 = 130;
    private static final int ExitY1 = 150;
    
    private static final int ExitX = 130;
    private static final int ExitY = -30;

    private MarketInfoPanel panel;
    
    private enum Command {noCommand, GoHome, CollectItem, GoToExit, NotAtWork, GoToExit1, GoToWork1};
	private Command command=Command.NotAtWork;
    
    public ItemCollectorGui(ItemCollector ic) {
        this.agent = ic;
    }
    
    /* (non-Javadoc)
	 * @see market.gui.ItemCollectorGuiInterfaces#setItemCollectorNumber(int)
	 */
    @Override
	public void setItemCollectorNumber(int i){
    	if (i == 0){
    		HomePosX = 225;
    	    CollectItemX = 225;
    	}
    	else{
    		HomePosX = 255;
    	    CollectItemX = 280;
    	}
    }
    
    /* (non-Javadoc)
	 * @see market.gui.ItemCollectorGuiInterfaces#setMarketControlPanel(market.gui.MarketInfoPanel)
	 */
    @Override
	public void setMarketControlPanel(MarketInfoPanel p){
    	panel = p;
    }

    /* (non-Javadoc)
	 * @see market.gui.ItemCollectorGuiInterfaces#updatePosition()
	 */
    @Override
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

        	if (command==Command.GoToWork1){
        		BackReadyStation();
        		return;
        	}
        	
        	else if (command==Command.GoHome) {
				agent.Ready();
				currentTask = "AtBench";
        	}
        	
			else if (command==Command.CollectItem) {
				agent.AtCollectStation();
				currentTask = "AtCollectStation";
			}
			else if (command==Command.GoToExit1){
				ContinueToExit();
				return;
			}
			else if (command==Command.GoToExit){
				command= Command.NotAtWork;
				agent.AtExit();
				currentTask = "AtExit";
				return;
			}
        	
			command=Command.noCommand;
        }

        
        
    }
    /* (non-Javadoc)
	 * @see market.gui.ItemCollectorGuiInterfaces#UpdateInventoryLevel()
	 */
    @Override
	public void UpdateInventoryLevel(){
    	if (panel == null)
    		return;
    	panel.UpdateInventoryLevelWithoutButton();
    }
    
    
    /* (non-Javadoc)
	 * @see market.gui.ItemCollectorGuiInterfaces#BackReadyStation()
	 */
    @Override
	public void BackReadyStation(){
    	xDestination = HomePosX;
    	yDestination = HomePosY;
    	command=Command.GoHome;
    	currentTask = "Going To Ready Station";
    	UpdateInventoryLevel();
    }
    
    public void GoToWork(){
    	xDestination = ExitX1;
    	yDestination = ExitY1;
    	command = Command.GoToWork1;
    	currentTask = "Going To Work";
    }
    
    /* (non-Javadoc)
	 * @see market.gui.ItemCollectorGuiInterfaces#CollectItems()
	 */
    @Override
	public void CollectItems(){
    	xDestination = CollectItemX;
    	yDestination = CollectItemY;
    	command=Command.CollectItem;
    	currentTask = "Going To Collect Items";
    }
    
    public void ContinueToExit(){
    	xDestination = ExitX;
    	yDestination = ExitY;
    	command = Command.GoToExit;
    	currentTask = "Going To Exit";
    }
    
    /* (non-Javadoc)
	 * @see market.gui.ItemCollectorGuiInterfaces#OffWork()
	 */
    @Override
	public void OffWork(){
    	xDestination = ExitX1;
    	yDestination = ExitY1;
    	command=Command.GoToExit1;
    	currentTask = "Going To Exit";
    }
    
    /* (non-Javadoc)
	 * @see market.gui.ItemCollectorGuiInterfaces#ContinueOffWork()
	 */


    /* (non-Javadoc)
	 * @see market.gui.ItemCollectorGuiInterfaces#draw(java.awt.Graphics2D)
	 */
    @Override
	public void draw(Graphics2D g) {
        g.setColor(Color.PINK);
        g.fillRect(xPos, yPos, ItemCollectorWidth, ItemCollectorHeight);
        
        g.drawString(currentTask, xPos, yPos);
    }

    /* (non-Javadoc)
	 * @see market.gui.ItemCollectorGuiInterfaces#isPresent()
	 */
    @Override
	public boolean isPresent() {
        return true;
    }

    /* (non-Javadoc)
	 * @see market.gui.ItemCollectorGuiInterfaces#getXPos()
	 */
    @Override
	public int getXPos() {
        return xPos;
    }

    /* (non-Javadoc)
	 * @see market.gui.ItemCollectorGuiInterfaces#getYPos()
	 */
    @Override
	public int getYPos() {
        return yPos;
    }
}
