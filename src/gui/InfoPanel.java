package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import CommonSimpleClasses.CityLocation.LocationTypeEnum;
import agent.PersonAgent;

/**
 * A Panel that displays information about the person or building
 * Also contains some person/building controls
 * @author Victoria Dea
 *
 */
public class InfoPanel extends JPanel implements ActionListener{
	
	private Dimension d;
	private JLabel info;
	//private JPanel textPanel; //info display
	//private JPanel controlPanel; //gui controls

	public InfoPanel(int w, int h){
		d = new Dimension(w-20, h-25);
		setPreferredSize(d);
		setMaximumSize(d);
		setMinimumSize(d);
		setLayout(new CardLayout());
		
		JPanel personText = new JPanel();
		personText.setPreferredSize(d);
		personText.setMaximumSize(d);
		personText.setMinimumSize(d);
		addBuildingInfoPanel(personText, "person");
			
		info = new JLabel("");
		//Test text
		info.setText("<html><div>&nbsp;</div><div> "
				+ "Name: "+ "Spongebob Squarepants" +"</div><div>&nbsp;</div>"
				+ "<div> Job: "+"Chef" +"</div><div>&nbsp;</div>"
				+ "<div> Residence: "+ "Pineapple" + "</div><div>&nbsp;</div>"
				+ "<div> Money: $"+ "500" +"</div><div>&nbsp;</div>"
				+ "<div> Hunger Level: "+"2" +"</div></html>"
		);
		personText.add(info);
		
		/*controlPanel = new JPanel();
		Dimension controlDim = new Dimension((int)(d.width*0.6), d.height);
		controlPanel.setPreferredSize(controlDim);
		controlPanel.setMaximumSize(controlDim);
		controlPanel.setMinimumSize(controlDim);
		controlPanel.setLayout(new CardLayout());*/
		
		
		
		
		//add(textPanel, BorderLayout.WEST);
		//add(controlPanel, BorderLayout.EAST);
	}

	/**
	 * Displays the Person information
	 * @param p Person name
	 */
	public void updatePersonInfoPanel(PersonAgent person){
		CardLayout cl = (CardLayout)(this.getLayout());
		cl.show(this, "person");
		//System.out.println("update info with "+person.getName());
		info.setText("<html><div>&nbsp;</div><div> "
						+ "Name: "+ person.getName() +"</div><div>&nbsp;</div>"
						//+ "<div> Job: "+ person.getJob() +"</div><div>&nbsp;</div>"
						//+ "<div> Residence: "+ person.getResidence + "</div><div>&nbsp;</div>"
						//+ "<div> Money: $"+ person.getMoney() +"</div><div>&nbsp;</div>"
						//+ "<div> Hunger Level: "+ person.getHungerLevel +"</div></html>"
				);
		validate();
	}
	
	/**
	 * Displays the Building information
	 * @param b Building name
	 */
	public void updateBuildingInfoPanel(Building b){
		//Building building = b;
		System.out.println(b.getName()+ " update info panel");
		
		CardLayout cl = (CardLayout)(this.getLayout());
		if(b.getInfoPanel() == null){
			cl.show(this, "blank");
		}
		else
		{
			cl.show(this, b.getName());
		}
		
		validate();

	}
	
	public void addBuildingInfoPanel(JPanel control, String name){
		add(control, name);
	}
	
	public void actionPerformed(ActionEvent e) {
		
	}

}
