package transportation.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import CommonSimpleClasses.XYPos;
import transportation.CornerAgent;
import transportation.IntersectionAction;
import transportation.interfaces.BusstopRequester;
import transportation.interfaces.Corner;
import transportation.interfaces.Vehicle;
import transportation.test.mock.MockBus;
import transportation.test.mock.MockBusstop;
import transportation.test.mock.MockCorner;
import transportation.test.mock.MockVehicle;

public class CornerAgentTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	private CornerAgent corner;
	private MockCorner mockCorner;
	private MockVehicle mockVehicle1, mockVehicle2;
	private MockBusstop mockBusstop1, mockBusstop2;
	private MockBus mockBus;

	@Before
	public void setUp() throws Exception {
		corner = new CornerAgent("corner", new XYPos(0,0));
		mockCorner = new MockCorner("mockCorner", 100, 0);
		mockVehicle1 = new MockVehicle("mockVehicle1");
		mockVehicle2 = new MockVehicle("mockVehicle2");
		mockBusstop1 = new MockBusstop("mockBusstop1");
		mockBusstop2 = new MockBusstop("mockBusstop2");
		corner.addBusstop(mockBusstop1);
		corner.addBusstop(mockBusstop2);
		mockBus = new MockBus("MockBus");
	}

	@Test
	public void testOneDrivingThrough() {
		assertEquals(new Integer(1), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(0, corner.getWaitingToCross().size());
		
		corner.pickAndExecuteAnAction();
		
		assertEquals(new Integer(1), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(0, corner.getWaitingToCross().size());
		
		corner.msgIWantToDriveTo(new IntersectionAction
				(mockCorner, mockVehicle1));
		
		assertEquals(new Integer(1), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(1, corner.getWaitingToCross().size());
		
		corner.pickAndExecuteAnAction();
		
		assertEquals(new Integer(0), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(0, corner.getWaitingToCross().size());
		assertEquals("Got msgDriveNow()", 
				mockVehicle1.log.getLastLoggedEvent().getMessage());
		
		corner.msgIWantToDriveTo(new IntersectionAction
				(mockCorner, mockVehicle2));
		
		assertEquals(new Integer(0), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(1, corner.getWaitingToCross().size());
		
		corner.msgDoneCrossing();
		
		assertEquals(new Integer(1), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(1, corner.getWaitingToCross().size());
		
		corner.pickAndExecuteAnAction();
		
		assertEquals(new Integer(0), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(0, corner.getWaitingToCross().size());
		assertEquals("Got msgDriveNow()", 
				mockVehicle2.log.getLastLoggedEvent().getMessage());
	}
	
	@Test
	public void testTwoRequestingBusstops() {
		assertEquals(new Integer(1), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(0, corner.getWaitingToCross().size());
		
		corner.pickAndExecuteAnAction();
		
		assertEquals(new Integer(1), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(0, corner.getWaitingToCross().size());
		
		corner.msgYourBusStop(mockBus);
		
		assertEquals(new Integer(1), corner.getCrossroadBusy());
		assertEquals(1, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(0, corner.getWaitingToCross().size());
		
		corner.pickAndExecuteAnAction();
		
		assertEquals(new Integer(1), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(0, corner.getWaitingToCross().size());
		assertEquals("Received bsList of size 2", 
				mockBus.log.getLastLoggedEvent().getMessage());
		
	}

	@Test
	public void testTwoRequestingAdjCorners() {
		assertEquals(new Integer(1), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(0, corner.getWaitingToCross().size());
		
		corner.pickAndExecuteAnAction();
		
		assertEquals(new Integer(1), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(0, corner.getWaitingToCross().size());
		
		corner.msgYourAdjCorners(mockBus);
		
		assertEquals(new Integer(1), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(1, corner.getWaitingForCorners().size());
		assertEquals(0, corner.getWaitingToCross().size());
		
		corner.pickAndExecuteAnAction();
		
		assertEquals(new Integer(1), corner.getCrossroadBusy());
		assertEquals(0, corner.getWaitingForBusstops().size());
		assertEquals(0, corner.getWaitingForCorners().size());
		assertEquals(0, corner.getWaitingToCross().size());
		assertEquals("Received cList of size 0", 
				mockBus.log.getLastLoggedEvent().getMessage());
		
		
	}
}
