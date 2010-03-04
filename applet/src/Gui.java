import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JApplet;
import javax.swing.Timer;
import Pieces.Direction;

/**
 * The home control point for the game.  Includes options and information about the game.
 */
public class Gui extends JApplet implements KeyListener, ActionListener
{
	/** 
	 * The game being played.
	 */
	private Game game;	
	/**
	 * The system that actual triggers game moves for the game.
	 */
	private Timer timer;			
	/**
	 * Tells the Timer if the player wants to keep moving until he dies or moves on.
	 */
	private boolean continous;
	/**
	 * The move being made.
	 */
	private Direction move;

	/**
	 * The entry point for the Applet.
	 */
	public void init()
	{
		//Creates a timer with no ititial delay and a short delay if is run multiple times before being stopped (if continous).
		timer = new Timer(0, this);
		timer.setDelay(200);

		//Creates and Launches the Game.
		this.game = new Game();
		this.showGui(game);
	}

	/**
	 * Creates a default, classic game.  Sets up all components and some frames
	 */
	public void showGui(Game game)
	{
		this.game.addKeyListener(this);

		this.add(game);
	}

	/**
	 * Handles Keyboard Input.
	 * Handles Keys :<p>
	 * Keypad Keys:<p>
	 * S - Settings.<p>
	 * 1 - W.<p>
	 * 2 - S.<p>
	 * 3 - SE.<p>
	 * 4 - W.<p>
	 * 5 - Moves the robots without the player moving.<p>
	 * 6 - E.<p>
	 * 7 - NW.<p>
	 * 8 - N.<p>
	 * 9 - NE.<p>
	 * + - Teleports Randomly.<p>
	 * ENTER - Moves the robots toward you (in the 'SAME' position) until either you or all of them die. 
	 *
	 * @param key The event triggered when a key is pressed
	 */
	public void keyTyped(KeyEvent key)
	{
		if(game.getHuman().isAlive())
		{
			switch(key.getKeyChar())
			{
				case '1' : move = Direction.SW;     timer.start(); break;
				case '2' : move = Direction.S;      timer.start(); break;
				case '3' : move = Direction.SE;     timer.start(); break;
				case '4' : move = Direction.W;      timer.start(); break;
				case '5' : move = Direction.SAME;   timer.start(); break;
				case '6' : move = Direction.E;      timer.start(); break;
				case '7' : move = Direction.NW;     timer.start(); break;
				case '8' : move = Direction.N;      timer.start(); break;
				case '9' : move = Direction.NE;     timer.start(); break;
				case '+' : move = Direction.SAFE;   timer.start(); break;
				case '*' : move = Direction.RANDOM; timer.start(); break;
				case KeyEvent.VK_ENTER : 
					continous = true;
					move = Direction.CONTINUOUS;
					timer.start();
					break;
			}
		}
		else
		{
			game.resetBoard();
		}
	}

	/**
	 *Not implemented.
	 */
	public void keyPressed(KeyEvent key)  { }

	/**
	 * Not implemented.
	 */
	public void keyReleased(KeyEvent key) { }

	/**
	 * Moves the player in the specified Direction.  Also moves the player the correct number of steps in the game.
	 */
	public void actionPerformed(ActionEvent event)
	{
		game.makeMove(move);
		game.repaint();

		//Determines if game should stop moving.
		if( !(continous && game.getHuman().isAlive() && game.numBots() != 0) )
		{
			continous = false;
			timer.stop();
		}
		
		if(game.numBots() == 0 && game.getHuman().isAlive())
		{
			game.increaseLevel();
		}
	}
}
