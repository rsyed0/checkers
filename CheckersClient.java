import javax.swing.*;
import java.net.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class CheckersClient extends JFrame{

	public static void main(String[] args) {new CheckersClient();}

	public CheckersClient(){
		
		super("Checkers (Client)");
		setSize(800,835);
		setLocation(300,50);
		setContentPane(new CheckersClientPanel());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
		
	}

}

class CheckersClientPanel extends CheckersPanel{
	
	private static final long serialVersionUID = 1L;
	private Scanner sc;
	private PrintWriter pw;
	
	public CheckersClientPanel(){
		
		super();
		
		Scanner xterm = new Scanner(System.in);
		System.out.print("Enter IP address: ");
		String ip = xterm.next();
		System.out.print("Enter port: ");
		int port = xterm.nextInt();
		
		System.out.println("Waiting for connection...");
		
		try{
			Socket s = new Socket(ip,port);
			sc = new Scanner(s.getInputStream());
			pw = new PrintWriter(s.getOutputStream());
		} catch (IOException e){
			System.err.println("Couldn't connect.");
			System.exit(1);
		}
		
		System.out.println("Connection found!");
		
		new ReceiveThread().start();
		
	}
	
	public void analyze(String msg){
		
		int fromX = Integer.parseInt(msg.charAt(1)+"");
		int fromY = Integer.parseInt(msg.charAt(3)+"");
		int toX = Integer.parseInt(msg.charAt(10)+"");
		int toY = Integer.parseInt(msg.charAt(12)+"");
		
		if (Math.abs(toX-fromX) == 2 && Math.abs(toY-fromY) == 2)
			getPiece((toX+fromX)/2,(toY+fromY)/2).take();
		
		selPc = getPiece(fromX,fromY);
		selPc.moveTo(toX,toY);
		
		if (!msg.contains("temp")){
			selPc = null;
			redTurn = !redTurn;
		}
		
		repaint();
		
	}
	
	public void mouseClicked(MouseEvent e){
		
		requestFocusInWindow();
		int x = e.getX()/100,y = e.getY()/100;
		
		if (selPc != null){
			if (selPc.allowedMove(x,y)){
				pw.print(selPc);
				selPc.moveTo(x,y);
				if (!selPc.canJump() || !selPc.justTook()){
					pw.println(" to "+selPc);
					selPc = null;
					redTurn = !redTurn;
				} else pw.println(" to "+selPc+" temp.");
				pw.flush();
			} else if (hasPiece(x,y)){
				if (getPiece(x,y).isRed() == selPc.isRed())
					selPc = getPiece(x,y);
			}
		} else if (hasPiece(x,y)){
			//System.out.println(getPiece(x,y).isRed());
			if (!getPiece(x,y).isRed() && !redTurn) selPc = getPiece(x,y);
		}
		
		repaint();
		
	}
	
	class ReceiveThread extends Thread{
		
		public void run(){
			
			while (true){
				try{
					String line = sc.nextLine();
					System.out.println(line);
					analyze(line);
				} catch (NoSuchElementException e){
					System.err.println("Connection terminated. Quitting...");
					System.exit(0);
				}
			}
			
		}
		
	}
	
}
