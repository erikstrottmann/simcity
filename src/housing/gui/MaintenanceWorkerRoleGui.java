package housing.gui;

import housing.backend.HousingComplex;
import housing.backend.MaintenanceWorkerRole;
import housing.interfaces.MaintenanceWorker;
import housing.interfaces.MaintenanceWorkerGui;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import agent.gui.Gui;

/**
 * ResidentGui displays the Resident role of a PersonAgent
 * when that agent returns home. The gui moves around and performs tasks,
 * waking up the PersonAgent upon reaching a destination. 
 * @author Zach VP
 *
 */

public class MaintenanceWorkerRoleGui implements Gui, MaintenanceWorkerGui {

	private MaintenanceWorker worker;
	
	// needs to know the complex so gui can enter and exit buildings
	private HousingComplex complex;
	
	// this prevents excessive releases from occurring
	private boolean canRelease = false;
	
	// checks if worker is going to be idle
	private boolean waiting = false;
	
	// determines whether to render the gui
	private boolean present = true;
	
	// image for Resident
	private BufferedImage workerImage;
	private ImageIcon workerIcon;
	
	// image for tool
	private BufferedImage tool;
	private ImageIcon toolIcon;

	// set up labels
	String guiName = "Worker";
	boolean showTool = false;

	/* --- Hardcoded Positions --- */
	// default resident position
	private int xPos = 130; 
	private int yPos = 15;
	
	private int xDestination, yDestination;//default start position
	
	int unit;
	
	/* --- Constructor --- */
	public MaintenanceWorkerRoleGui(MaintenanceWorkerRole role) {
		this.worker = role;
		
		try {
			workerImage = ImageIO.read(getClass().getResource("spongebob.png"));
			tool = ImageIO.read(getClass().getResource("krabby_patty.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		workerIcon = new ImageIcon(workerImage);
		toolIcon = new ImageIcon(tool);
	}

	/**
	 * So long as the gui is not at the destination coordinates,
	 * it will move toward the destination.
	 */
	public void updatePosition() {
		if (xPos < xDestination) xPos++;
		
		else if (xPos > xDestination) xPos--;

		if (yPos < yDestination) yPos++;
		
		else if (yPos > yDestination) yPos--;

		if (xPos == xDestination && yPos == yDestination && canRelease && !waiting ) {
			canRelease = false;
			worker.msgAtDestination();
		}
	}

	/* --- Draw the Resident Graphics --- */
	public void draw(Graphics2D g) {
		// draw the worker image
		g.drawImage(workerIcon.getImage(), xPos, yPos, null);
	    	
//		g.setColor(Color.WHITE);
//		g.fillRect(xPos, yPos, 20, 20);
		
//		g.drawString(eatingFood, xPos+5, yPos+15);
		if(showTool == true){
			g.drawImage(toolIcon.getImage(), xPos, yPos, null);
		}
	}

	public boolean isPresent() {
		return present;
	}

	public MaintenanceWorker getAgent(){
		return worker;
	}

	public void DoGoToLoc() {
		waiting = false;
		xDestination = 0;
		yDestination = 0;
		canRelease = true;
	}
	
	public void setTool(boolean state){
		showTool = state;
	}

	public int getXPos() {
		return xPos;
	}

	public int getYPos() {
		return yPos;
	}
	
	public void setID(int id) {
		this.unit = id;
	}

	@Override
	public void DoGoToDwelling(int unit) {
//		this.complex.removeGuiFromDwelling(this, unit);
		this.complex.addGuiToDwelling(this, unit);
		worker.msgAtDestination();
	}

	@Override
	public void DoFixProblem() {
		waiting = false;
		showTool = true;		
		xDestination = 60;
		yDestination = 50;
		canRelease = false;
	}

	@Override
	public void DoReturnHome(int unit) {
		showTool = false;
		
		
		this.complex.removeGuiFromDwelling(this, unit);
//		this.complex.addGuiToDwelling(this, this.unit);
	}

	@Override
	public void setComplex(HousingComplex complex) {
		this.complex = complex;
	}
	
}