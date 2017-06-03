/**
 * Checkers.java
 * Reedit Syed Shahriar
 *
 * 31 May 2017 - 3 June 2017
 * 
 * This program launches a two-player game of checkers on the local
 * machine. It is used as an inheritance blueprint for the CheckersServer
 * and CheckersClient classes. 
 */

import javax.swing.*; // import frame and panel classes
import java.awt.*; // import drawing-related classes
import java.awt.event.*; // import event handler interfaces and classes
import java.awt.image.ImageObserver; // import ImageObserver
import java.io.File; // import I/O classes for image reading
import java.io.IOException;
import javax.imageio.ImageIO;

// class for starting game
public class Checkers extends JFrame{

	// serialization indicator
	private static final long serialVersionUID = 1L;

	// main method to create frame/start game
	public static void main(String[] args){new Checkers();}
	
	// constructor for creation of frame and inner panel
	public Checkers(){
		
		super("Checkers"); // make frame
		setSize(800,835);
		setLocation(300,50);
		setContentPane(new CheckersPanel()); // make panel
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		
	}

}

// class for game panel
class CheckersPanel extends JPanel implements MouseListener,KeyListener{
	
	// declare field variables
	protected static final long serialVersionUID = 1L; // serialization indicator
	protected Checker[] red,black; // checker arrays for each side
	protected Checker selPc; // selected checker
	protected boolean redTurn; // boolean for whose turn it is
	private Image redCrown,blkCrown; // images for drawing crown on kinged checkers
	
	// constructor for game panel
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
			redCrown = ImageIO.read(new File("redCrown.jpg"));
			blkCrown = ImageIO.read(new File("blkCrown.jpg"));
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
		
		// create new thread to check if game is over
		new CheckGameOverThread().start();
		
	}
	
	// method to check if tile at (x,y) has a piece
	// returns true if (x,y) is off the board
	public boolean hasPiece(int x,int y){
		
		// check if tile exists
		if (0 <= x && x <= 7 && 0 <= y && y <= 7)
			return (getPiece(x,y) != null);	// use getPiece method to check if tile has piece
		else return true;
		
	}
	
	// method to get piece on tile (x,y)
	public Checker getPiece(int x,int y){
		
		// iterates through arrays of checkers to find checker with same position
		for (int i=0;i<12;i++){
			if (x == red[i].getX() && y == red[i].getY() && !red[i].isTaken()) return red[i];
			if (x == black[i].getX() && y == black[i].getY() && !black[i].isTaken()) return black[i];
		}
		
		// if no match is found, return null
		return null;
		
	}
	
	// method to draw on panel
	public void paintComponent(Graphics g){
		
		// draw white background/tiles
		super.paintComponent(g);
		
		// draw all alternate tiles black
		g.setColor(Color.BLACK);
		for (int x=0;x<8;x++){
			for (int y=0;y<8;y++)
				if ((x+y)%2 == 1) g.fillRect(x*100,y*100,100,100); 
		}
		
		// draw checkers on board
		for (int i=0;i<12;i++){
			red[i].draw(g);
			black[i].draw(g);
		}
		
	}
	
	// method to check if one side has run out of checkers/has lost
	public boolean gameOver(){
		
		boolean redExists = false,blackExists = false;
		
		// iterate through arrays
		for (int i=0;i<12;i++){
			if (!red[i].isTaken())	// check if at least one checker in array hasn't been taken
				redExists = true;
			if (!black[i].isTaken())
				blackExists = true;
		}
		
		return !(redExists && blackExists); // return true if one boolean is false
		
	}
	
	// class to describe checker objects
	class Checker implements ImageObserver{
		
		// declare field variables for coordinates of location and status of checker
		private int x,y;
		private boolean red,kinged,taken,justTook;
		
		// constructor with arguments for location and color
		public Checker(int x,int y,boolean red){
			
			// initialize fields based on parameters
			this.x = x;
			this.y = y;
			this.red = red;
			
			// checker is not kinged, taken, and has not just taken a checker
			this.kinged = false;
			this.taken = false;
			this.justTook = false;
			
		}
		
		// method to describe checker based on location
		// used to serialize moves for network implementation, DO NOT CHANGE
		public String toString(){return "("+x+","+y+")";}
		
		// methods to change status of checker
		public void king(){this.kinged = true;}
		public void take(){this.taken = true;}
		
		// getters for location and status
		public int getX(){return x;}
		public int getY(){return y;}
		public boolean isRed(){return red;}
		public boolean isKinged(){return kinged;}
		public boolean isTaken(){return taken;}
		public boolean justTook(){return justTook;}
		
		// method to determine if a piece can jump
		// used for double jumps
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
		
		// method to determine if a move to the tile (x,y) is legal
		public boolean allowedMove(int x,int y){
			
			justTook = false;
			
			// if tile is black or has a piece, cannot move there
			if ((x+y)%2==1 || hasPiece(x,y)) return false;
			
			// if trying to move backward and not kinged, cannot move there
			else if ((y-this.y == 1 && red && !kinged) || (this.y-y == 1 && !red && !kinged))
				return false; // if trying to move backward and not kinged, cannot move there
			
			// if trying to move forward diagonally one tile, allowed
			if (Math.abs(this.x-x) == 1 && this.y-y == 1 && red) return true;
			if (Math.abs(this.x-x) == 1 && y-this.y == 1 && !red) return true;
			
			// if kinged and trying to move diagonally in any direction one tile, allowed
			if (Math.abs(this.x-x) == 1 && Math.abs(this.y-y) == 1 && kinged) return true;
			
			// if trying to take a piece correctly while kinged, allowed
			if (Math.abs(this.x-x) == 2 && Math.abs(this.y-y) == 2 && kinged && hasPiece((this.x+x)/2,(this.y+y)/2)){
				if (red != getPiece((this.x+x)/2,(this.y+y)/2).isRed()){
					getPiece((this.x+x)/2,(this.y+y)/2).take();
					justTook = true;
					return true;
				}
			}
			
			// if trying to take a piece correctly while not kinged, allowed
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
		
		// method to move checker to (x,y)
		public void moveTo(int x,int y){
			
			// change coordinates
			this.x = x;
			this.y = y;
			
			// if checker should be kinged, king it
			if (y == 7 && !red) king();
			else if (y == 0 && red) king();
			
		}
		
		// method to draw checker at correct location
		public void draw(Graphics g){
			
			// preserving old color, draw checker at location
			Color oldColor = g.getColor();
			
			// if selected, indicate tile in yellow
			g.setColor(Color.YELLOW);
			if (this.equals(selPc)) g.fillRect(x*100,y*100,100,100);
			
			// if the piece hasn't been taken, draw checker
			if (!taken){
				
				// draw checker with accurate color
				if (red) g.setColor(Color.RED);
				else g.setColor(Color.BLACK);
				g.fillOval(x*100,y*100,100,100);
				
				// draw crown on checker if kinged
				if (kinged){
					if (red) g.drawImage(redCrown,x*100+14,y*100+22,71,55,this);
					else g.drawImage(blkCrown,x*100+14,y*100+22,71,55,this);
				}
			}
			
			// preserve old color
			g.setColor(oldColor);
			
		}

		// interface method for image buffering and drawing
		public boolean imageUpdate(Image arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
			return false;
		}
		
	}
	
	// class for thread to check if game is over
	class CheckGameOverThread extends Thread{
		
		// method to be executed in parallel with game threads
		public void run(){
			
			// if game is over, close program
			while (true){
				if (gameOver()) 
					System.exit(0);
			}
			
		}
	
	}

	// mouse event handler method
	public void mouseClicked(MouseEvent e) {
		
		// request focus when mouse is clicked
		requestFocusInWindow();
		
		// get tile selected
		int x = e.getX()/100,y = e.getY()/100;
		
		// if a piece is selected
		if (selPc != null){
			
			// if piece can move to selected tile, move there
			if (selPc.allowedMove(x,y)){
				selPc.moveTo(x,y);
				
				// if multiple jump isn't  possible, deselect and change side
				if (!selPc.canJump() || !selPc.justTook()){
					selPc = null;
					redTurn = !redTurn;
				}
				
			// if there is a piece at the tile
			} else if (hasPiece(x,y)){
				
				// deselect if checker has been reclicked
				if (selPc.equals(getPiece(x,y))){
					if (selPc.justTook()) redTurn = !redTurn;
					selPc = null;
				
				// if a new checker on same side has been selected, change to that checker
				} else if (getPiece(x,y).isRed() == selPc.isRed())
					selPc = getPiece(x,y);
			}
			
		// if no piece is selected, and a piece can be found at the tile, select that piece if on correct side
		} else if (hasPiece(x,y)){
			if (getPiece(x,y).isRed() == redTurn)
				selPc = getPiece(x,y);
		}
		
		// update panel
		repaint();
		
	}
	
	// default interface event handler methods
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	// key event handler method
	public void keyPressed(KeyEvent e) {
		
		// if Ctrl+W or Alt+F4 is pressed, close window
		char ch = e.getKeyChar();
		if (ch == 'w' && e.isControlDown()) System.exit(0);
		else if (e.isAltDown() && ch == KeyEvent.VK_F4) System.exit(0);
		
	}
	
}
