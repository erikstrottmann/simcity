package transportation.gui;

import gui.Building;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import CommonSimpleClasses.CityBuilding;
import CommonSimpleClasses.CityLocation;
import CommonSimpleClasses.Constants;
import CommonSimpleClasses.CardinalDirectionEnum;
import CommonSimpleClasses.XYPos;
import CommonSimpleClasses.CityLocation.LocationTypeEnum;
import transportation.gui.interfaces.PassengerGui;
import transportation.interfaces.Bus;
import transportation.interfaces.Busstop;
import transportation.interfaces.Corner;
import transportation.interfaces.Passenger;

public class PassengerGuiClass implements PassengerGui {

	private static final int PASSENGERW = 4;
	static final int PASSENGERH = 4;

	private int xPos, yPos;
	private Passenger passenger;
	private CityLocation destination;
	private CityLocation startLocation;
	private boolean isPresent = true;

	public PassengerGuiClass(Passenger passenger,
			CityLocation location) {
		setPassenger(passenger, location);
	}

	private void setPassenger(Passenger passenger, 
			CityLocation currentLocation) {
		this.passenger = passenger;
		this.startLocation =  currentLocation;
		this.destination = currentLocation;
		resetXY();
		TransportationGuiController.getInstance().addPassengerGUI(this);
	}
	
	public void updatePosition() {
		int xDestination = destinationPos().x;
		int yDestination = destinationPos().y;
		if (arrivedAtDestination(xDestination, yDestination)) {
			onPlace();
		} else if (destination.type() != LocationTypeEnum.Corner
				|| startLocation.type() != LocationTypeEnum.Corner) {
			if (xPos < xDestination)
				xPos += 1;
			else if (xPos > xDestination)
				xPos -= 1;
			else if (yPos < yDestination)
				yPos += 1;
			else if (yPos > yDestination)
				yPos -= 1;
		} else {
			switch (passenger.currentDirection()) {
			case North:
				yPos -= 1;
				break;
			case South:
				yPos += 1;
				break;
			case West:
				xPos -= 1;
				break;
			case East:
				xPos += 1;
				break;
			case None:
			default:
				try {
					throw new Exception("This shouldn't happen!"
							+ " Person won't move!");
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}

	}

	public boolean arrivedAtDestination(int xDestination, int yDestination) {
		if (destination.type() != LocationTypeEnum.Corner) {
			return xPos == xDestination && yPos == yDestination;
		} else {
			return atEdgeOfCorner();
		}
	}

	public boolean atEdgeOfCorner() {
		return manhattanDistanceFromDestination()
				< Constants.SPACE_BETWEEN_BUILDINGS;
	}

	private int manhattanDistanceFromDestination() {
		return Math.abs(xPos - destination.position().x) +
				Math.abs(yPos - destination.position().y);
	}

	private XYPos destinationPos() {
		if (destination instanceof Building) {
			Building cb = (Building) destination;
			XYPos response = new XYPos(cb.position());
			response.x += cb.entrancePos().x;
			response.y += cb.entrancePos().y;
			return response;
		} else return destination.position();
	}

	private void onPlace() {
		if (startLocation != destination){
			startLocation = destination;
			try {
				passenger.msgWeHaveArrived(destination);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void resetXY() {
		xPos = startLocation.position().x;
		yPos = startLocation.position().y;
		
		if (startLocation instanceof CityBuilding) {
			XYPos entrance = ((CityBuilding)startLocation).entrancePos();
			xPos += entrance.x;
			yPos += entrance.y;
		}
	}

	@Override
	public void draw(Graphics2D g) {
		XYPos drawingPos;
		
		try {
			drawingPos = calculateDrawingPosition();
		} catch (Exception e) {
			drawingPos = new XYPos(xPos,yPos);
			e.printStackTrace();
		}
		
		Rectangle2D r = new Rectangle2D.Double(drawingPos.x, drawingPos.y,
				PASSENGERW, PASSENGERH);
		
		BufferedImage img = passenger.getPerson().getImage();
		/*if (img == null)*/ g.setColor(Color.GREEN);
		/*
		else {
			g.setPaint(new TexturePaint(img, r));
		}
		*/
		g.fill(r);
	}
	
	private XYPos calculateDrawingPosition() throws Exception {
		XYPos response = new XYPos(xPos,yPos);
		
		//adjust from center to corner
		response.x -= PASSENGERW/2;
		response.y -= PASSENGERH/2;
		return response;
	}

	@Override
	public boolean isPresent() {
		return isPresent;
	}

	public void doSetLocation (CityLocation loc) {
		startLocation = loc;
		resetXY();
	}

	@Override
	public void doWalkTo(CityLocation cityLocation) {
		destination = cityLocation;
	}

	@Override
	public void doGetInBus(Bus b) {
		isPresent = false;
	}

	@Override
	public void doExitVehicle(CityLocation location) {
		startLocation = location;
		resetXY();
		isPresent = true;
	}

	@Override
	public void doBringOutCar() {
		isPresent = false;
		passenger.msgGotInCar();
	}

	@Override
	public XYPos getPos() {
		return new XYPos(xPos, yPos);
	}

	@Override
	public void doExitBus(Corner corner, boolean orientation) {
		try {
			doExitVehicle(corner.getBusstopWithDirection(orientation));
			startLocation = ((Busstop) startLocation).corner();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Animation glitches due to exception");
			doExitVehicle(corner);
		}
	}

	@Override
	public void startShowing() {
		isPresent = true;
	}

	@Override
	public void hide() {
		isPresent = false;
	}

}
