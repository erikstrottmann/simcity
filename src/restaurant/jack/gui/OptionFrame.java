package restaurant.jack.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;



public class OptionFrame extends JFrame implements ActionListener {
	RestaurantGui restGui;
	GridLayout layout;
	JButton addCustomer;
	JButton populateWorkRolesButton;
	
	
	public OptionFrame(RestaurantGui r) {
		restGui = r;
		layout = new GridLayout(2,3);
		this.setLayout(layout);
		addCustomer = new JButton("add Customer");
		populateWorkRolesButton = new JButton("populate work roles");
		populateWorkRolesButton.addActionListener(this);
		this.add(addCustomer);
		this.add(populateWorkRolesButton);
		this.setBounds(50, 50, 300, 245);
		
	}
	
	
	
	public void setRestaurantGui(RestaurantGui r) {
		restGui = r;
	}



	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == addCustomer){
			
		}
		if(e.getSource() == populateWorkRolesButton) {
//			restGui.populateWorkRoles();
		}
		
	}
}