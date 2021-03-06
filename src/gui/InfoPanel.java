package gui;

import housing.backend.ResidentialBuilding;
import housing.gui.HousingInfoPanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import bank.gui.BankBuilding;
import kelp.Kelp;
import kelp.KelpClass;
import market.gui.MarketBuilding;
import CommonSimpleClasses.CityLocation;
import CommonSimpleClasses.CityLocation.LocationTypeEnum;
import CommonSimpleClasses.Constants;
import CommonSimpleClasses.SingletonTimer;
import CommonSimpleClasses.TimeManager;
import agent.PersonAgent;

/**
 * A Panel that displays information about the person or building
 * Also contains some person/building controls
 * @author Victoria Dea
 *
 */
public class InfoPanel extends JPanel implements ActionListener{
	
	private Dimension d, cardDim;
	private JLabel info;
	TabbedInfoDisplay tabDisplay;
	JPanel card = new JPanel();
	JPanel staffCard = new JPanel();
	JPanel timePanel = new JPanel();
	JPanel personText;
	JLabel time;
	int hour, min, sec;
	String hourStr, minStr, secStr;
	
	TimeManager timeManager = TimeManager.getInstance();
	private Timer timer;
	
	GridLayout controlLayout;
	JLabel name, currLoc, job, residence, money, hunger, 
		   nameL, currLocL, jobL, residenceL, moneyL, hungerL;
	JPanel controls;
	JButton robberButton;
	JButton makeHungryButton;
	PersonAgent currentPerson;
	
	public InfoPanel(int w, int h){
		d = new Dimension(Constants.INFO_PANEL_WIDTH, h); //700 X 190
		setPreferredSize(d);
		setMaximumSize(d);
		setMinimumSize(d);
		setLayout(new BorderLayout());
		
		cardDim = new Dimension(Constants.INFO_PANEL_WIDTH, h-46); //700 X 145
		card.setPreferredSize(cardDim);
		card.setMaximumSize(cardDim);
		card.setMinimumSize(cardDim);
		card.setLayout(new CardLayout());
		
		//overall person panel
		personText = new JPanel();
		makePersonPanel();

		card.add(personText, "person");

		//Display current game time
		timer = SingletonTimer.getInstance();
    	// timer.scheduleAtFixedRate(new PrintTask(), 0, 500);
		time = new JLabel();
		timePanel.add(time);
		//timePanel.setBorder(BorderFactory.createEtchedBorder());
		//card.setBorder(BorderFactory.createEtchedBorder());
		
		//Staff Card
		Dimension staffCardDim = new Dimension(Constants.INFO_PANEL_WIDTH, d.height-46); //700 X 145
		staffCard.setPreferredSize(staffCardDim);
		staffCard.setMaximumSize(staffCardDim);
		staffCard.setMinimumSize(staffCardDim);
		staffCard.setLayout(new CardLayout());
		
		add(card, BorderLayout.SOUTH);
		add(timePanel, BorderLayout.NORTH);
	}
	
	private void makePersonPanel(){
		personText.setPreferredSize(d);
		personText.setMaximumSize(d);
		personText.setMinimumSize(d);
		personText.setLayout(new BorderLayout());
		
		//display labels
		JPanel textWest = new JPanel();
		Dimension textWDim = new Dimension((int)(Constants.INFO_PANEL_WIDTH*0.20), cardDim.height);
		textWest.setPreferredSize(textWDim);
		textWest.setMaximumSize(textWDim);
		textWest.setMinimumSize(textWDim);
		textWest.setLayout(new GridLayout(6, 1, 1, 1));
		
		nameL = new JLabel("", JLabel.RIGHT);
		currLocL = new JLabel("", JLabel.RIGHT);
		jobL = new JLabel("", JLabel.RIGHT);
		residenceL = new JLabel("", JLabel.RIGHT);
		moneyL = new JLabel("", JLabel.RIGHT);
		hungerL = new JLabel("", JLabel.RIGHT);
		
		//padding
		Border empty = new EmptyBorder(0, 0, 0, 7);
		nameL.setBorder(empty);
		currLocL.setBorder(empty);
		jobL.setBorder(empty);
		residenceL.setBorder(empty);
		moneyL.setBorder(empty);
		hungerL.setBorder(empty);
		
		textWest.add(nameL);
		textWest.add(currLocL);
		textWest.add(jobL);
		textWest.add(residenceL);
		textWest.add(moneyL);
		textWest.add(hungerL);
		
		JPanel textEast = new JPanel();
		Dimension textEDim = new Dimension((int)(Constants.INFO_PANEL_WIDTH*0.25), cardDim.height);
		textEast.setPreferredSize(textEDim);
		textEast.setMaximumSize(textEDim);
		textEast.setMinimumSize(textEDim);
		textEast.setLayout(new GridLayout(6, 1, 1, 1));
		
		name = new JLabel("");
		currLoc = new JLabel("");
		job = new JLabel("");
		residence = new JLabel("");
		money = new JLabel("");
		hunger = new JLabel("");
		
		textEast.add(name);
		textEast.add(currLoc);
		textEast.add(job);
		textEast.add(residence);
		textEast.add(money);
		textEast.add(hunger);
		
		
		//Person controls
		controlLayout = new GridLayout(0,1);
		controls = new JPanel();
		controls.setLayout(controlLayout);
		Dimension controlDim = new Dimension((int)(Constants.INFO_PANEL_WIDTH*0.55), cardDim.height);
		controls.setPreferredSize(controlDim);
		controls.setVisible(false);
		
		//Robber button
		robberButton = new JButton("Rob a Bank");
		robberButton.addActionListener(this);
		
		controls.add(robberButton);
		
		//MakeHungryButton
		makeHungryButton = new JButton("Make Hungry");
		makeHungryButton.addActionListener(this);
		
		controls.add(makeHungryButton);
		
		personText.add(textWest, BorderLayout.WEST);
		personText.add(textEast, BorderLayout.CENTER);
		personText.add(controls, BorderLayout.EAST);
	}
	
	
	private void getTimeDisplay(){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeManager.currentSimTime());
		
		SimpleDateFormat format = new SimpleDateFormat("E, MM-dd-yyyy, HH:mm");
		time.setText(format.format(cal.getTime()) + " in Encino, CA");
		
	}

	/**
	 * Displays the Person information
	 * @param p Person name
	 */
	public void updatePersonInfoPanel(PersonAgent person){
		CardLayout cl = (CardLayout)(card.getLayout());
		if(tabDisplay.getTabCount() == 3){
			tabDisplay.hideBuildingTabs();
		}
		
		if(!person.getTimeToRobABank()){
			robberButton.setEnabled(true);
		}
		
		cl.show(card, "person");
		//System.out.println("update info with "+person.getName());
		/*info.setText("<html><div>&nbsp;</div><div> "
						+ "Name: "+ person.getName() +"</div><div>&nbsp;</div>"
						+ "<div> Job: "+ (person.getWorkRole() == null ? "none" : person.getWorkRole().getShortName()) + "</div><div>&nbsp;</div>"
						+ "<div> Residence: "+ (person.getResidentRole() == null ? "none" : person.getResidentRole().getDwelling()) + "</div><div>&nbsp;</div>"
						+ "<div> Money: $"+ person.getWallet().getCashOnHand() +"</div><div>&nbsp;</div>"
						+ "<div> Hunger Level: "+ person.getHungerLevel() +"</div></html>"
				);*/
		
		currentPerson = person;
		controls.setVisible(true);
		
		nameL.setText("Name:  ");
		currLocL.setText("Current Location:  ");
		jobL.setText("Job:  ");
		residenceL.setText("Residence:  ");
		moneyL.setText("Money:  ");
		hungerL.setText("Hunger Level:  ");
		
		
		name.setText(person.getName());
		currLoc.setText(""+(person.getPassengerRole().getLocation()));
		job .setText((person.getWorkRole() == null ? "none" : person.getWorkRole().toString()));
		residence.setText(""+(person.getResidentRole() == null ? "none" : person.getResidentRole().getDwelling()));
		money.setText("$"+person.getWallet().getCashOnHand());
		hunger.setText(""+person.getHungerLevel());		
		validate();
	}
	
	/**
	 * Displays the Building information
	 * @param b Building name
	 */
	public void updateBuildingInfoPanel(Building b){
	
		if (b instanceof MarketBuilding){
			((MarketBuilding) b).UpdateInfoPanel();
		}	
		
		CardLayout cl = (CardLayout)(card.getLayout());
		CardLayout c2 = (CardLayout)(staffCard.getLayout());
		
		if(b.getInfoPanel() == null){
			cl.show(card, "blank");
		}
		else
		{
			if(b instanceof HospitalBuilding && tabDisplay.getTabCount() == 3){
				tabDisplay.hideBuildingTabs();
			}
			if(!(b instanceof HospitalBuilding) && tabDisplay.getTabCount() == 2){
				tabDisplay.showStaffTab();
				
			}
			c2.show(staffCard, b.getName());
			cl.show(card, b.getName());
		}

		validate();

	}
	
	/**
	 * Adds the Building's control panel to the cardlayout
	 * @param panel Building's info panel
	 * @param name Building's name
	 */
	public void addBuildingInfoPanel(JPanel panel, String name){
		card.add(panel, name);
	}
	
	public void addStaffInfoPanel(JPanel panel, String name){
		staffCard.add(panel, name);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == robberButton){
			for (CityLocation loc : KelpClass.getKelpInstance().placesNearMe(currentPerson.getPassengerRole().getLocation(), LocationTypeEnum.Bank)){
				if(loc instanceof BankBuilding && ((BankBuilding) loc).isOpen()){
					currentPerson.setTimeToRobABank();
					currentPerson.addRole(((BankBuilding) loc).getRobberRole(currentPerson));
					robberButton.setEnabled(false);
				}
			}
		}
		if (e.getSource() == makeHungryButton) {
			currentPerson.setHungerToStarving();
		}
	}
	
	class PrintTask extends TimerTask {
        public void run() {
        	getTimeDisplay();
        }
	}

	public void setTabDisplay(TabbedInfoDisplay tabbedInfoPane) {
		tabDisplay = tabbedInfoPane;
		tabDisplay.setStaffCard(staffCard);
	}

}
