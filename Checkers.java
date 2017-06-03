/**
 * Checkers.java
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Checkers extends JFrame{

	private static final long serialVersionUID = 1L;

	public static void main(String[] args){new Checkers();}
	
	public Checkers(){
		
		super("Checkers");
		setSize(800,835);
		setLocation(300,50);
		setContentPane(new CheckersPanel());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		
	}

}

class CheckersPanel extends JPanel implements MouseListener,KeyListener{
	
	protected static final long serialVersionUID = 1L;
	protected Checker[] red,black;
	protected Checker selPc;
	protected boolean redTurn;
	private Image crown;
	
	public CheckersPanel(){
		
		// panel settings
		setBackground(Color.WHITE);
		addMouseListener(this);
		
		// set fields
		red = new Checker[12];
		black = new Checker[12];
		selPc = null;
		redTurn = true;
		try {
			crown = ImageIO.read(new File("crown.jpg"));
		} catch (IOException e) {
			System.err.println("Couldn't read image files. Quitting...");
			System.exit(1);
		}
		
		// instantiate checkers with position
		int x = 0,y = 0;
		for (int i=0;i<12;i++){
			if (i%4 == 0 && i != 0) y++;
			if (x >= 8) x = (y%2);
			x %= 8;
			black[i] = new Checker(x,y,false);
			x += 2;
		}
		
		x = 1;
		y = 5;
		for (int i=0;i<12;i++){
			if (i%4 == 0 && i != 0) y++;
			if (x >= 8) x = (y%2);
			x %= 8;
			red[i] = new Checker(x,y,true);
			x += 2;
		}
		
		new CheckGameOverThread().start();
		
	}
	
	public boolean hasPiece(int x,int y){
		
		if (0 <= x && x <= 7 && 0 <= y && y <= 7)
			return (getPiece(x,y) != null);
		else return true;
		
	}
	
	public Checker getPiece(int x,int y){
		
		for (int i=0;i<12;i++){
			if (x == red[i].getX() && y == red[i].getY() && !red[i].isTaken()) return red[i];
			if (x == black[i].getX() && y == black[i].getY() && !black[i].isTaken()) return black[i];
		}
		
		return null;
		
	}
	
	public void paintComponent(Graphics g){
		
		super.paintComponent(g);
		
		g.setColor(Color.BLACK);
		for (int x=0;x<8;x++){
			for (int y=0;y<8;y++)
				if ((x+y)%2 == 1) g.fillRect(x*100,y*100,100,100); 
		}
		
		for (int i=0;i<12;i++){
			red[i].draw(g);
			black[i].draw(g);
		}
		
	}
	
	public boolean gameOver(){
		
		boolean redExists = false,blackExists = false;
		
		for (int i=0;i<12;i++){
			if (!red[i].isTaken())
				redExists = true;
			if (!black[i].isTaken())
				blackExists = true;
		}
		
		return !(redExists && blackExists);
		
	}
	
	class Checker{
		
		private int x,y;
		private boolean red,kinged,taken,justTook;
		
		public Checker(int x,int y,boolean red){
			
			this.x = x;
			this.y = y;
			this.red = red;
			this.kinged = false;
			this.taken = false;
			this.justTook = false;
			
		}
		
		public String toString(){return "("+x+","+y+")";} // DO NOT CHANGE (will ruin network implementation)
		
		public void king(){this.kinged = true;}
		public void take(){this.taken = true;}
		
		public int getX(){return x;}
		public int getY(){return y;}
		public boolean isRed(){return red;}
		public boolean isKinged(){return kinged;}
		public boolean isTaken(){return taken;}
		public boolean justTook(){return justTook;}
		
		public boolean canJump(){
			
			try{
				return ((hasPiece(x+1,y+1) && red != getPiece(x+1,y+1).isRed() && !hasPiece(x+2,y+2)) && (!red || (kinged && red))) || 
				   ((hasPiece(x-1,y+1) && red != getPiece(x-1,y+1).isRed() && !hasPiece(x-2,y+2)) && (!red || (kinged && red))) ||
				   ((hasPiece(x+1,y-1) && red != getPiece(x+1,y-1).isRed() && !hasPiece(x+2,y-2)) && (red || (kinged && !red))) ||
				   ((hasPiece(x-1,y-1) && red != getPiece(x-1,y-1).isRed() && !hasPiece(x-2,y-2)) && (red || (kinged && !red)));
			} catch (NullPointerException e){
				return false;
			}
			
		}
		
		public boolean allowedMove(int x,int y){
			
			justTook = false;
			
			if ((x+y)%2==1 || hasPiece(x,y)) return false;
			else if ((y-this.y == 1 && red && !kinged) || (this.y-y == 1 && !red && !kinged))
				return false;
			if (Math.abs(this.x-x) == 1 && this.y-y == 1 && red) return true;
			if (Math.abs(this.x-x) == 1 && y-this.y == 1 && !red) return true;
			if (Math.abs(this.x-x) == 1 && Math.abs(this.y-y) == 1 && kinged) return true;
			if (Math.abs(this.x-x) == 2 && Math.abs(this.y-y) == 2 && kinged && hasPiece((this.x+x)/2,(this.y+y)/2)){
				if (red != getPiece((this.x+x)/2,(this.y+y)/2).isRed()){
					getPiece((this.x+x)/2,(this.y+y)/2).take();
					justTook = true;
					return true;
				}
			}
			if (Math.abs(this.x-x) == 2 && hasPiece((this.x+x)/2,(this.y+y)/2)){
				if (this.y-y == 2 && red && !getPiece((this.x+x)/2,(this.y+y)/2).isRed()){
					getPiece((this.x+x)/2,y+1).take();
					justTook = true;
					return true;
				} else if (y-this.y == 2 && !red && getPiece((this.x+x)/2,(this.y+y)/2).isRed()){
					getPiece((this.x+x)/2,y-1).take();
					justTook = true;
					return true;
				}
			}
			return false;
			
		}
		
		public void moveTo(int x,int y){
			
			this.x = x;
			this.y = y;
			if (y == 7 && !red) king();
			else if (y == 0 && red) king();
			
		}
		
		public void draw(Graphics g){
			
			// preserving old color, draw checker at location
			Color oldColor = g.getColor();
			
			g.setColor(Color.YELLOW);
			if (this.equals(selPc)) g.fillRect(x*100,y*100,100,100);
			
			if (!taken){
				if (red) g.setColor(Color.RED);
				else g.setColor(Color.BLACK);
				g.fillOval(x*100,y*100,100,100);
				
				g.setColor(Color.YELLOW);
				g.setFont(new Font("Serif",Font.BOLD,60));
				if (kinged) g.drawString("K",x*100+25,y*100+70);
			}
			
			g.setColor(oldColor);
			
		}
		
	}
	
	class CheckGameOverThread extends Thread{
		
		public void run(){
			
			while (true){
				if (gameOver()) 
					System.exit(0);
			}
			
		}
	
	}

	public void mouseClicked(MouseEvent e) {
		
		requestFocusInWindow();
		int x = e.getX()/100,y = e.getY()/100;
		
		if (selPc != null){
			//System.out.println(selPc.canJump());
			if (selPc.allowedMove(x,y)){
				selPc.moveTo(x,y);
				if (!selPc.canJump() || !selPc.justTook()){
					selPc = null;
					redTurn = !redTurn;
				}
			} else if (hasPiece(x,y)){
				if (selPc.equals(getPiece(x,y))){
					if (selPc.justTook()) redTurn = !redTurn;
					selPc = null;
				}else if (getPiece(x,y).isRed() == selPc.isRed())
					selPc = getPiece(x,y);
			}
		} else if (hasPiece(x,y)){
			if (getPiece(x,y).isRed() == redTurn)
				selPc = getPiece(x,y);
		}
		
		repaint();
		
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

	public void keyPressed(KeyEvent e) {
		
		char ch = e.getKeyChar();
		if (ch == 'w' && e.isControlDown()) System.exit(0);
		
	}

	public void keyReleased(KeyEvent e) {}

	public void keyTyped(KeyEvent e) {}
	
}
