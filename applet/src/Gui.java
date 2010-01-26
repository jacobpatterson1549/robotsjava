import java.awt.BorderLayout;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.GridLayout;
import javax.swing.JApplet;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import Pieces.Direction;

/**
 * The home control point for the game.  Includes options and information about the game.
 */
public class Gui extends JApplet implements KeyListener
{
	private static final long serialVersionUID = -4028299973870072557L;

	private boolean continous;
	private Game game;
	private JPanel southPanel;

	public void init()
	{
		showGui(new Game());
	}

	/**
	 * Creates a default, classic game.  Sets up all components and some frames
	 */
	public void showGui(Game game)
	{
		this.game = game;

		// Initial construction of panel to hold various labels.
		southPanel = new JPanel(new GridLayout(1, 3));
		southPanel.setBackground(java.awt.Color.GRAY);
		southPanel.add(game.getLevelLabel());
		southPanel.add(game.getScoreLabel());
		southPanel.add(game.getSafeTeleportsLabel());

		this.setLayout(new GridLayout(2, 1));
		this.add(game);
		this.add(southPanel);
		this.addKeyListener(this); 
	}

	/**
	 * Not implemented.
	 *
	 * @param key The event triggered when a key is pressed
	 */
	public void keyTyped(KeyEvent key) { /* Does nothing. */ }

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
	public void keyPressed(KeyEvent key)
	{
		if(game.getHuman().isAlive())
		{
			if(key.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD)
			{
				continous = false;
				switch(key.getKeyChar())
				{
					case '1' : performAction(Direction.SW);  break;
					case '2' : performAction(Direction.S);   break;
					case '3' : performAction(Direction.SE);  break;
					case '4' : performAction(Direction.W);   break;
					case '5' : performAction(Direction.SAME);break;
					case '6' : performAction(Direction.E);   break;
					case '7' : performAction(Direction.NW);  break;
					case '8' : performAction(Direction.N);   break;
					case '9' : performAction(Direction.NE);  break;
					case '+' : performAction(Direction.RANDOM);break;
					case KeyEvent.VK_ENTER : 
						continous = true;
						performAction(Direction.CONTINUOUS);
						break;
				}
			}
		}
		else // game.gutHuman.IsAlive == false
		{
			performAction(null);	
		}
	}

	/**
	 * Not implemented.
	 *
	 * @param key The event triggered when a key is released.
	 */
	public void keyReleased(KeyEvent key) { /*Does nothing. */ }

	/**
	 * Moves the player in the specified Direction.  Also moves the player the correct number of steps in the game.
	 *
	 * @param move The Direction to move the Player.
	 */
	protected void performAction(Direction move)
	{
			if(move != null)
			{
					do
					{
							game.makeMove(move);
							game.printBoard();

							if(!game.getHuman().isAlive() || game.numBots() == 0)
							{
									continous = false;
							}
							if(continous)
							{
									//Wait here.
							}
					}
					while(continous);
			}
			if(game.getHuman().isAlive())
			{
					if(game.numBots() == 0)
					{
							//wait here
							game.increaseLevel();
							game.printBoard();
					}
			}
			else
			{
					this.removeKeyListener(this);
					final int choice = JOptionPane.showConfirmDialog(this, "Do you want to start a new game?", "Restart?", JOptionPane.YES_NO_OPTION);
					if(choice == JOptionPane.YES_OPTION)
					{
						game.resetBoard();
						game.printBoard();
					}
					else if(choice == JOptionPane.NO_OPTION)
					{
							System.exit(0);
					}
					this.addKeyListener(this);
			}
	}
}
