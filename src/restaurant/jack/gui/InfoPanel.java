package restaurant.jack.gui;

import java.awt.Dimension;

import javax.swing.JPanel;

import CommonSimpleClasses.Constants;

public class InfoPanel extends JPanel {
	
	
	public InfoPanel() {
		Dimension d = new Dimension(Constants.INFO_PANEL_WIDTH, Constants.INFO_PANEL_HEIGHT);
		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);
		
		Dimension half = new Dimension((int)(d.width*0.5), d.height);
	}
}