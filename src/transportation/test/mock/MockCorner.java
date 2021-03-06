package transportation.test.mock;

import java.util.List;

import mock.Mock;
import transportation.CornerAgent.CornerDirectionEnum;
import transportation.CornerAgent.MyCorner;
import transportation.IntersectionAction;
import transportation.interfaces.AdjCornerRequester;
import transportation.interfaces.Busstop;
import transportation.interfaces.BusstopRequester;
import transportation.interfaces.Corner;
import CommonSimpleClasses.CardinalDirectionEnum;
import CommonSimpleClasses.XYPos;

public class MockCorner extends Mock implements Corner {

	private XYPos location;

	public MockCorner(String name, int x, int y) {
		super(name);
		location = new XYPos(x, y);
	}

	@Override
	public LocationTypeEnum type() {
		return LocationTypeEnum.Busstop;
	}

	@Override
	public XYPos position() {
		return location;
	}

	@Override
	public void msgIWantToDriveTo(IntersectionAction a) {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgYourBusStop(BusstopRequester b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgYourAdjCorners(AdjCornerRequester c) {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgDoneCrossing() {
		// TODO Auto-generated method stub

	}

	@Override
	public Corner getCornerForDir(CardinalDirectionEnum dir) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Busstop> getBusstops() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Busstop getBusstopWithDirection(boolean busDirection)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAdjacentCorner(Corner c, CardinalDirectionEnum d) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addBusstop(Busstop b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startThreads() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<MyCorner> getAdjacentCorners() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CardinalDirectionEnum getDirForCorner(Corner corner) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void msgIAmCrossing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgChangeDir() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CornerDirectionEnum getCurrDir() {
		// TODO Auto-generated method stub
		return null;
	}

}
