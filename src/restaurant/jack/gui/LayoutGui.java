package restaurant.jack.gui;

import java.awt.Color;
import java.awt.Graphics2D;



import agent.gui.Gui;

public class LayoutGui implements Gui {

	private boolean isPresent = true;
	private final int WINDOWX = 600;
	private final int WINDOWY = 490;
	
    public static final int xTable = 200;
    public static final int yTable = 250;
    public static final int tableColor = 50;
	
	@Override
	public void updatePosition() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(Color.lightGray);
		g.fillRect(0, 0, WINDOWX, WINDOWY);
		
		g.setColor(Color.BLUE);
		g.fillRect(520, 145, 30, 20);
		Graphics2D g2 = (Graphics2D)g;

//		g2.fillRect(0, 0, WINDOWX, WINDOWY);

		Graphics2D t1 = (Graphics2D)g;
		Graphics2D t2 = (Graphics2D)g;
		Graphics2D t3 = (Graphics2D)g;
		Graphics2D t4 = (Graphics2D)g;

		Graphics2D waiterIdleArea = (Graphics2D)g;

		Graphics2D cashierArea = (Graphics2D)g;
	        

//	        //Clear the screen by painting a rectangle the size of the frame
//	        t1.setColor(getBackground());
//	        t1.fillRect(0, 0, WINDOWX, WINDOWY );
//	        t2.setColor(getBackground());
//	        t2.fillRect(0, 0, WINDOWX, WINDOWY );
//	        t3.setColor(getBackground());
//	        t3.fillRect(0, 0, WINDOWX, WINDOWY );
//	        
	        
	        
	        //Here is the table
	        t1.setColor(Color.ORANGE);
	        t1.fillRect(100, 100, tableColor, tableColor);//200 and 250 need to be table params (locations)
	        t2.setColor(Color.ORANGE);
	        t2.fillRect(200, 100, tableColor, tableColor);//200 and 250 need to be table params (locations)
	        t3.setColor(Color.ORANGE);
	        t3.fillRect(300, 100, tableColor, tableColor);//200 and 250 need to be table params (locations)
	        t4.setColor(Color.ORANGE);
	        t4.fillRect(400, 100, tableColor, tableColor);//200 and 250 need to be table params (locations)

	        //Cashier area
	        cashierArea.setColor(Color.LIGHT_GRAY);
	        cashierArea.fillRect(400, 0, 50, 20);
	        cashierArea.setColor(Color.WHITE);
	        //cashierArea.drawString("Cashier", 400, 15);
	        
	        //Cook and Waiter areas

	        waiterIdleArea.setColor(Color.BLUE);
	        waiterIdleArea.fillRect(200, 0, 140, 22);
	        
	        
	        //Grill and Plating areas (and fridge)
	        //FRIDGE
			g.setColor(Color.BLUE);
			g.fillRect(520, 145, 30, 20);
			//Grills
			for(int i = 0; i < 4; i++) {
				g.setColor(Color.GRAY);
				g.fillRect(550, 50 + (i*25), 20, 20);
//				if(gr.o!=null) {
//					g.setColor(Color.RED);
//					g.drawString(gr.o.Choice, gr.x, gr.y);
//				}
			}
			
			for(int i = 0; i < 4; i++) {
				g.setColor(Color.PINK);
				g.fillRect(500, 50 + (i*25), 20, 20);
//				if(p.o!=null) {
//					g.setColor(Color.RED);
//					g.drawString(p.o.Choice, p.x, p.y);
//				}
			}
	        
		
	}

	@Override
	public boolean isPresent() {
		return isPresent;
	}
	
}