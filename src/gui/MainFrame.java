package gui;


import gui.test.MockBuilding;
import housing.backend.ResidentialBuilding;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import kelp.KelpClass;
import market.gui.MarketBuilding;
import parser.BuildingDef;
import parser.BuildingDef.CreatorEnum;
import parser.BuildingPosParser;
import parser.CornersWithBusstopsParser;
import restaurant.jack.gui.RestaurantLucasBuilding;
import restaurant.anthony.gui.RestaurantBuilding;
import restaurant.strottma.gui.RestaurantStrottmaBuilding;
import restaurant.vegaperk.backend.RestaurantVegaPerkBuilding;
import restaurant.vonbeck.gui.RestaurantVonbeckBuilding;
import restaurant.vdea.gui.RestaurantVDeaBuilding;
import sun.net.www.content.text.PlainTextInputStream;
import transportation.BusAgent;
import transportation.interfaces.Busstop;
import transportation.interfaces.Corner;
import transportation.mapbuilder.MapBuilder;
import CommonSimpleClasses.CityLocation;
import CommonSimpleClasses.CityLocation.LocationTypeEnum;
import CommonSimpleClasses.Constants;
import CommonSimpleClasses.SingletonTimer;
import CommonSimpleClasses.sound.Sound;
import bank.gui.BankBuilding;


/** 
 * Main GUI class
 * Contains the main frame and structure for panels
 * @author Victoria Dea
 *
 */
public class MainFrame extends JFrame implements ActionListener {

	private int WINDOWX = 1200;
	private int WINDOWY = 700;

	private BufferedInputStream stream;
	private List<BuildingDef> needToBuild;

	//Panel Slots
	private JPanel cityViewSlot = new JPanel();
	private JPanel buildingViewSlot = new JPanel();
	private JPanel infoPanelSlot = new JPanel();
	private JPanel InfoListSlot = new JPanel();

	//Panels
	private BuildingView buildingViewPanel;
	private CityMap map;
	public InfoPanel infoPanel;	

	private InfoList buildingList;
	private InfoList personList;
	private CitizenRecords citizenRecords;
	private Sound openingSound = Sound.getInstance();
	
	private ArrayList<Building> constructedBuildings = new ArrayList<Building>();
	HospitalBuilding hospital;
	private Semaphore semaphore = new Semaphore(0);
	private Timer timer = SingletonTimer.getInstance();

	public MainFrame(){

		//FullScreen frame
		//Toolkit tk = Toolkit.getDefaultToolkit();  
		//WINDOWX = ((int) tk.getScreenSize().getWidth());  
		//WINDOWY = ((int) tk.getScreenSize().getHeight()); 
		//setExtendedState(Frame.MAXIMIZED_BOTH);

		setBounds(50,50, WINDOWX, WINDOWY);

		try {
			setContentPane(new JLabel(new ImageIcon(ImageIO.read(getClass().getResource("sky_background.png")))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		setLayout(new BorderLayout(5,10));

		try {
			stream = (BufferedInputStream)getClass().getResource("BuildingConfig.csv").getContent();
			needToBuild = BuildingPosParser.parseBuildingPos(stream);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Internal Building View
		Dimension buildingDim = new Dimension((int)(WINDOWX * .5), (int) (WINDOWY * .7));
		buildingViewSlot.setPreferredSize(buildingDim);
		buildingViewSlot.setMaximumSize(buildingDim);
		buildingViewSlot.setMinimumSize(buildingDim);
		buildingViewSlot.setOpaque(false);
		buildingViewPanel = new BuildingView(buildingDim.width, buildingDim.height);
		buildingViewSlot.add(buildingViewPanel);

		//City map view
		Dimension cityDim = new Dimension(((int)(WINDOWX * .5))-10, (int) (WINDOWY * .7));
		cityViewSlot.setPreferredSize(cityDim);
		cityViewSlot.setMaximumSize(cityDim);
		cityViewSlot.setMinimumSize(cityDim);
		cityViewSlot.setOpaque(false);
		map = new CityMap();
		map.setBuildingView(buildingViewPanel);
		cityViewSlot.add(map);

		//Information Panel 720x210
		TabbedInfoDisplay tabbedInfoPane = new TabbedInfoDisplay();
		tabbedInfoPane.setOpaque(false);
		
		Dimension infoDim = new Dimension((int)(WINDOWX * .6), (int) (WINDOWY * .3));
		infoPanelSlot.setPreferredSize(infoDim);
		infoPanelSlot.setMaximumSize(infoDim);
		infoPanelSlot.setMinimumSize(infoDim);
		infoPanelSlot.setOpaque(false);
		infoPanelSlot.setLayout(new BorderLayout());
		//InfoPanel is now a JTabbedPane
		infoPanel = new InfoPanel(infoDim.width, infoDim.height);  
		tabbedInfoPane.addTab("Info", infoPanel);
		tabbedInfoPane.addTab("Log", new LogDisplay());
		infoPanel.setTabDisplay(tabbedInfoPane);
		infoPanelSlot.add(tabbedInfoPane, BorderLayout.CENTER);
		//infoPanelSlot.add(infoPanel, BorderLayout.CENTER);

		//List of Buildings/People buttons
		Dimension listDim = new Dimension((int)(WINDOWX * .4), (int) (WINDOWY * .3));
		InfoListSlot.setPreferredSize(listDim);
		InfoListSlot.setMaximumSize(listDim);
		InfoListSlot.setMinimumSize(listDim);
		InfoListSlot.setOpaque(false);
		//InfoListSlot.setLayout(new BorderLayout());

		//Lists displaying People and Buildings in the city
		//Dimension tabDim = new Dimension(listDim.width-60, listDim.height-47);
		JTabbedPane tabbedListPane = new JTabbedPane();
		tabbedListPane.setOpaque(false);
		//tabbedPane.setPreferredSize(tabDim);
		buildingList = new InfoList(listDim.width, listDim.height);
		personList = new InfoList(listDim.width, listDim.height);
		buildingList.setOtherTab(personList);
		personList.setOtherTab(buildingList);
		buildingList.setInfoPanel(infoPanel);
		personList.setInfoPanel(infoPanel);
		citizenRecords = new CitizenRecords(this);
		personList.setCitizenRecords(citizenRecords);
		buildingList.setBuildingList(map.getBuildings());
		citizenRecords.setInfoPanel(infoPanel);
		buildingList.setBuildingView(buildingViewPanel);
		tabbedListPane.addTab("Buildings", buildingList);
		tabbedListPane.addTab("People", personList);
		InfoListSlot.add(tabbedListPane);//, BorderLayout.CENTER);
		map.setInfoPanel(infoPanel);

		//JPanel to hold infoPanelSlot and buildingListSlot
		JPanel infoHolder = new JPanel();
		Dimension infoHolderDim = new Dimension(WINDOWX, (int) (WINDOWY * .3));
		infoHolder.setPreferredSize(infoHolderDim);
		infoHolder.setMaximumSize(infoHolderDim);
		infoHolder.setMinimumSize(infoHolderDim);
		infoHolder.setOpaque(false);
		infoHolder.setLayout(new BorderLayout(5,10));
		infoHolder.add(infoPanelSlot, BorderLayout.WEST);
		infoHolder.add(InfoListSlot, BorderLayout.EAST);

		add(buildingViewSlot, BorderLayout.EAST);
		add(cityViewSlot, BorderLayout.WEST);
		add(infoHolder, BorderLayout.SOUTH);        

		//Constructs buildings from config file
		constructCity(needToBuild);
		openingSound.playSound("OpeningSceneSound.wav");
	}

	/**
	 * Builds the entire city
	 * @param list A list of Buildings defined in the config file
	 */
	private void constructCity(List<BuildingDef> list) {
		int x;
		int y;
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				semaphore.release();
				
			}
		}, 0000);
		
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(BuildingDef b : list){
			String buildingName = b.getName();
			LocationTypeEnum type = b.getType();
			CreatorEnum creator = b.getCreator();

			x = map.getNextBuildingX();
			y = map.getNextBuildingY();

			if(type == LocationTypeEnum.Bank){
				BankBuilding bank = new BankBuilding(x, y, Constants.BUILDING_WIDTH, Constants.BUILDING_HEIGHT); //Rectangle
				bank.setName(buildingName);
				construct(bank);
				
				//keep Buildings in a list for kelp
				//calc pos of new rect -- getNextBuildingPos() from CityMap
				//JPanel BankAnimaitonPanel = bank.getAnimationPanel()
				//bank.gui.AnimationPanel BankAnimationPanel = new bank.gui.AnimationPanel();

				//construct(buildingName, BankAnimationPanel, LocationTypeEnum.Bank);

				//buildingViewPanel.addCard(BankAnimationPanel, buildingName);//creates card and corresponding button
				//cityViewPanel.addBuildingToMap(buildingName, LocationTypeEnum.Bank);
				// add method to set control panel to infoPanel, should
				//		follow addBuildingToMap() path
				//add building details
			}
			

			if(type == LocationTypeEnum.Apartment){
				/*
				AptBuilding apt = new AptBuilding(x, y, Constants.BUILDING_WIDTH, Constants.BUILDING_HEIGHT);
				apt.setName(buildingName);
				construct(apt);
				*/
				try {
					throw new Exception("Can't instantiate apartments,"
							+ " doing houses instead (I think)");
				} catch (Exception e) {
					e.printStackTrace();
					type = LocationTypeEnum.House;
				}
			}
			if(type == LocationTypeEnum.House){
				ResidentialBuilding house = new ResidentialBuilding(x, y, Constants.BUILDING_WIDTH, Constants.BUILDING_HEIGHT);
				house.setName(buildingName);
				construct(house); 
			}
			if(type == LocationTypeEnum.Restaurant){
				Building restaurant;
				if (creator == CreatorEnum.ANTHONY) {
					restaurant = new RestaurantBuilding(x, y, Constants.BUILDING_WIDTH, Constants.BUILDING_HEIGHT);
				} else if (creator == CreatorEnum.JACK) {
					restaurant = new RestaurantLucasBuilding(x, y, Constants.BUILDING_WIDTH, Constants.BUILDING_HEIGHT);
				} else if (creator == CreatorEnum.VICTORIA) {
					restaurant = new RestaurantVDeaBuilding(x, y, Constants.BUILDING_WIDTH, Constants.BUILDING_HEIGHT);
				} else if (creator == CreatorEnum.ZACH) {
					restaurant = new RestaurantVegaPerkBuilding(x, y, Constants.BUILDING_WIDTH, Constants.BUILDING_HEIGHT);
				} else if (creator == CreatorEnum.DIEGO) {
					restaurant = new RestaurantVonbeckBuilding(x, y, Constants.BUILDING_WIDTH, Constants.BUILDING_HEIGHT);
				} else { // creator == CreatorEnum.ERIK
					restaurant = new RestaurantStrottmaBuilding(x, y, Constants.BUILDING_WIDTH, Constants.BUILDING_HEIGHT);
				}

				restaurant.setName(buildingName);
				construct(restaurant);
			}
			if(type == LocationTypeEnum.Market){
				MarketBuilding market = new MarketBuilding(x, y, Constants.BUILDING_WIDTH, Constants.BUILDING_HEIGHT);
				market.setName(buildingName);
				construct(market);
			}
			if(type == LocationTypeEnum.Hospital){
				hospital = new HospitalBuilding(x, y, Constants.BUILDING_WIDTH, Constants.BUILDING_HEIGHT);
				hospital.setRecords(citizenRecords);				
				hospital.setName(buildingName);
				construct(hospital);
			}
			if(type == LocationTypeEnum.None){
				MockBuilding mock = new MockBuilding(x, y, Constants.BUILDING_WIDTH, Constants.BUILDING_HEIGHT);
				mock.setName(buildingName);
				construct(mock);
			}
		}
		initializeCornerMapAndKelp(constructedBuildings);
		hospital.setBuildings(constructedBuildings);
	}

	/**
	 * Adds Building GUI components to the MainFrame
	 * @param building A Building
	 */
	private void construct(Building building){		
		buildingViewPanel.addCard(building.getAnimationPanel(), building.getName());//creates card and corresponding button
		map.addBuildingToMap(building);
		if(building.type() != LocationTypeEnum.None){
			buildingList.addToList(building.getName());
		}	
		building.setCitizenRecords(citizenRecords);
		infoPanel.addBuildingInfoPanel(building.getInfoPanel(), building.getName());
		infoPanel.addStaffInfoPanel(building.getStaffPanel(), building.getName());
		constructedBuildings.add(building);
	}

	void initializeCornerMapAndKelp(List<Building> buildings) {
		try {
			PlainTextInputStream stream = 
					(PlainTextInputStream)getClass()
					.getResource("CornersWithBusstops.txt").getContent();
			Set<Integer> cornersWithBusstops = 
					CornersWithBusstopsParser.parseCornersWithBusstops(stream);
			MapBuilder.createMap(buildings.size(), cornersWithBusstops);
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<Corner> corners = MapBuilder.getCreatedCorners();
		List<Corner> busRoute = MapBuilder.getBusRoute();

		ArrayList<CityLocation> locations =
				new ArrayList<CityLocation>(buildings);

		for (Corner corner : corners) {
			locations.add(corner);
			List<Busstop> busstops = corner.getBusstops();
			for (Busstop busstop : busstops) {
				locations.add(busstop);
			}
		}

		try {
			KelpClass.getKelpInstance().setData(locations, busRoute);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		for (Corner corner : corners) {
			corner.startThreads();
		}
		
		BusAgent busAgent = new BusAgent(corners.get(1),
				true, busRoute);

		busAgent.startThread();
		busAgent.startVehicle();

		busAgent = new BusAgent(corners.get(0),
				false, busRoute);

		busAgent.startThread();
		busAgent.startVehicle();
		
		
	}

	/** Utilities **/

	public InfoList getPersonInfoList(){
		return personList;
	}
	public CitizenRecords getCitizenRecords(){
		return citizenRecords;
	}
	public InfoPanel getInfoPanel(){
		return infoPanel;
	}
	public void actionPerformed(ActionEvent e) {

	}

	/**
	 * Main routine to get GUI started
	 */
	public static void main(String[] args) {
		MainFrame gui = new MainFrame();
		gui.setTitle("SimCity");
		gui.setVisible(true);
		gui.setResizable(false);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}