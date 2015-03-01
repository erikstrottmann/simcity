package restaurant.jack.gui;

import javax.swing.JFrame;



public class MainFile extends JFrame {
	
	RestaurantGui rest;
	OptionFrame optionFrame;
	public MainFile() {
		rest = new RestaurantGui();
		optionFrame = new OptionFrame(rest);
		optionFrame.setVisible(true);
		add(rest);
		this.setBounds(50, 50, 600, 490);

		//		this.add(bank);
		//		this.setTitle("SpongeBank");
		//		this.setVisible(true);
		//		this.setResizable(false);
	}


	public static void main(String[] args) {
		MainFile main = new MainFile();
		main.setTitle("Jack Restaurant");
		main.setVisible(true);
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.setResizable(false);
	}
}