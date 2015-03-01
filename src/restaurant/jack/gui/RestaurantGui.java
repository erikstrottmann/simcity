package restaurant.jack.gui;

import gui.AnimationPanel;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import restaurant.jack.WaiterRole;

import agent.PersonAgent;

public class RestaurantGui extends JPanel implements ActionListener {

	
	AnimationPanel animationPanel = new AnimationPanel();

	private LayoutGui layoutGui;
	GridLayout layout = new GridLayout(1,1);
	
	PersonAgent waiterPerson;
	
	public RestaurantGui () {
		layoutGui = new LayoutGui();
		animationPanel.addGui(layoutGui);
		
		this.add(animationPanel);
		this.setLayout(layout);
	}
	
	
	
	public AnimationPanel getAnimationPanel()  {
    	return animationPanel;
    }
	
	public void populateWorkRoles() {
//		System.out.println("WOOT");
//		waiterPerson = new PersonAgent("waiter1");
//		waiterPerson.startThread();
//		WaiterRole waiterRole = new WaiterRole();
//		WaiterGui waiterGui = new WaiterGui(waiterRole);
//		waiterRole.setGui(waiterGui);
//		animationPanel.addGui(waiterGui);
//		waiterPerson.addRole(waiterRole);
//		waiterRole.activate();
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	
}