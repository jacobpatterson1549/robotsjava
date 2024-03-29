package com.ants280.robots;

import com.ants280.robots.mysql.*;
import com.ants280.robots.pieces.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Label;
import java.awt.Panel;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * The Robots game.
 */
public class Game extends Panel
{
	private Dimension dimension;
	private int level;
	private int score;
	private int numBots;
	private int safeTeleports;
	private Image playerAliveImage;
	private Image playerDeadImage;
	private Image robotImage;
	private Image wreckImage;
	private Image splatImage;
	private Label levelLabel;
	private Label scoreLabel;
	private Label safeTeleportsLabel;

	/**
	 * Queries and adds high scores to the selected database.
	 */
	private MysqlBot mysqlBot;

	/**
	 * Indicates if the score should be submitted to the database once the player dies.  Set to false if the database could not be connected to.
	 */
	 private boolean submitScore;

	/**
	 * The maximum number of safe teleports allowed in the game.
	 */
	 private final int MAX_SAFETELEPORTS = 10;

	/**
	 * Indicates if the game is in a continuous loop until all the robots are dead or the Player is dead.
	 */
	private boolean waitMode;

	/**
	 * Used to tell how much to increase the safeTeleports by if the Player survives waitMode.
	 */
	private int waitScore;

	/**
	 * Used to determine how large to draw the Panel.
	 */
	private final int jpegSize = 20;

	/**
	 * Used to randomly teleport the Player.
	 */
	private Random generator;

	/**
	 * The width of the board.
	 */
	private final int ROWS;

	/**
	 * The width of the board.
	 */
	private final int COLS;


	/**
	 * The container of all the Locations.
	 */
	private Location[][] board;

	/**
	 * Used for updating the board with updateBoard(int, int). Is cleared on each player move.
	 */
	private Location[][] tempBoard;

	/**
	 * The only Player on the board
	 */
	private Player human;

	/**
	 * Returns the player. There should only be 1 player (human).
	 */
	public Player getHuman() { return human; }

	/**
	 * Getter for the number of robots on the board.  A variable is returned to increase game speed. This makes the game not have an "int numBots()" to count the number of robots on the board every time it is called.
	 *
	 * @return The number of robots on the board.
	 */
	public int numBots() { return numBots; }

	/**
	 * Creates a new Game. Calls resetGame() to add the board.
	 *
	 * @param username The player of the game.  Used for keeping track of scores.
	 */
	public Game(String username)
	{
		super();
		
		levelLabel         = new Label();
		scoreLabel         = new Label();
		safeTeleportsLabel = new Label();
		generator = new Random();
		ROWS = 30;
		COLS = 40;
		dimension = new Dimension(COLS * jpegSize + 1, ROWS * (jpegSize + 1));
		this.setBackground(Color.WHITE);
		this.createMysqlBot(username);
		this.initializeimages();
		this.resetGame();
	}

	/**
	 * Creates the MysqlBot.
	 *
	 * @param username The user playing the game.
	 */
	private void createMysqlBot(String username)
	{
		String url      = "jdbc:mysql://localhost:3306/patterson";
		String user     = "patterson";
		String password = "patterson";

		mysqlBot = new MysqlBot(username, url, user, password);
	}

	/**
	 * Puts the robots on the board.  Same as Game's  void setNumBots(int), but the number of robots is capped at SIDE * SIDE / 2.  This prevents the Player from having no possible safe teleports because of the amount of SafeTeleports.
	 *
	 * @param numBots Starts at 2, increases by 1 on each level increase.
	 */
	private void setNumBots(int numBots)
	{
		if(numBots > ROWS * COLS / 2)
		{
			this.numBots = ROWS * COLS / 2;
		}
		else
		{
			this.numBots = numBots;
		}
	}

	/**
	 * Initializes the images of the painted Locations.
	 */
	private void initializeimages()
	{
		try
		{
			playerAliveImage = ImageIO.read(this.getClass().getResource("images/PlayerAlive.png"));
			playerDeadImage  = ImageIO.read(this.getClass().getResource("images/PlayerDead.png"));
			robotImage       = ImageIO.read(this.getClass().getResource("images/Robot.png"));
			wreckImage       = ImageIO.read(this.getClass().getResource("images/Wreck.png"));
			splatImage       = ImageIO.read(this.getClass().getResource("images/Splat!.png"));
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Draws the image using DoubleBuffered graphics.
	 * @param g Should be the Panel's Graphics.
	 */
	public void update(Graphics g)
	{
		BufferedImage lastDrawnImage = (BufferedImage)this.createImage(this.getWidth(), this.getHeight());
		
		//Draws the shape onto the BufferedImage
		this.paint(lastDrawnImage.getGraphics());

		//Draws the BufferedImage onto the PaintPanel
		g.drawImage(lastDrawnImage, 0, 0, this);
	}

	/**
	 * Paints the board to the specified graphics.
	 */
	public void paint(Graphics g)
	{
		this.setSize(dimension);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (COLS * jpegSize) + 10, (ROWS * jpegSize) + 10);
		g.setColor(Color.LIGHT_GRAY);

		//Draws the board.
		for(int row = 0; row < ROWS; row++)
		{
			for(int col = 0; col < COLS; col++)
			{
				g.drawLine(jpegSize * col, 0, (jpegSize * col) + 0, (jpegSize * row) + jpegSize);
				g.drawLine(0, jpegSize * row, (jpegSize * col) + jpegSize, (jpegSize * row) + 0);
				g.drawImage(this.getImage(row, col), jpegSize * col, jpegSize * row, null);
			}	
		}

		//Draws the edges of the board (S & E).
		g.drawLine(0, jpegSize * ROWS, jpegSize * COLS, jpegSize * ROWS);
		g.drawLine(jpegSize * COLS, 0, jpegSize * COLS, jpegSize * ROWS);
		
		//Sets the score label.
		// TODO: This should only be done if the score changes, causing the labels text to change in its own method.
		scoreLabel.setText("Score: " + score);


		//Paints the labels.
		g.setColor(Color.BLACK);
		g.drawString(scoreLabel.getText(),         1,                           jpegSize * ROWS + ROWS / 2);
		g.drawString(safeTeleportsLabel.getText(), jpegSize * ((COLS / 2) - 2), jpegSize * ROWS + ROWS / 2);
		g.drawString(levelLabel.getText(),         jpegSize *  (COLS - 3),      jpegSize * ROWS + ROWS / 2);

		// Tell the player to restart if he dies.  Tell the player how his score ranked on the mysql database table.
		if(!human.isAlive())
		{
			String message = new String();
			if(!submitScore)
			{
				// Indicate problems (now or previously during the game) about connecting to the database.
				message = "Error connecting to the database.";
			}
			else
			{
				switch(mysqlBot.feedScore(score))
				{
					case ConnectionError: 
						// The message already indicates that the score could not be submitted.
						break;
					case AccessError:
						message = "Error accessing the database.";
						break;
					case InsertionError:
						message = "Error inserting the high score.";
						break;
					case NormalScore:
						message = "GAME OVER.  Thanks for playing!";
						break;
					case PersonalHigh:
						message = "You made a personal high score!";
						break;
					case GlobalHigh:
						message = "You made a global high score!!!";
						break;
				}
			}

			g.setFont(new Font("serif", Font.BOLD, 32));
			g.setColor(Color.GREEN);
			g.drawString(message,                              jpegSize * COLS / 2 - 270, jpegSize * ROWS / 2 - 32);
			g.drawString("PRESS ANY KEY TO START A NEW GAME.", jpegSize * COLS / 2 - 335, jpegSize * ROWS / 2 + 32);
		}
	}

	/**
	 * Gets the image for location in the grid.
	 *
	 * @param row The row of the location on the board to get the image for
	 * @param col The column of the location on the board to get the image for.
	 * @return The image of the board's row and column.
	 */
	private Image getImage(int row, int col)
	{
		if(board[row][col] instanceof Player)
		{
			if( ((Player)board[row][col]).isAlive())
			{
				return playerAliveImage;
			}
			else
			{
				return playerDeadImage;
			}
		}
		else if(board[row][col] instanceof Robot)
		{
			return robotImage;
		}
		else if(board[row][col] instanceof Wreck)
		{
			if(((Wreck)board[row][col]).justSplat())
			{
				((Wreck)board[row][col]).triggerSplat();
				return splatImage;
			}
			else
			{
				return wreckImage;
			}
		}
		else
		{
			return null;
		}
	}

	/**
	 * Updates the board for the Player's current Location.  Runs even if the player will die in his/her current Location.
	 */
	private void updateBoard()
	{
		int tempScore = 0, numReturned;
		tempBoard = createBoard();
		tempBoard[human.getRow()][human.getCol()] = human; 
		for(int boardRow = 0; boardRow < ROWS; boardRow++)
		{
			for(int boardCol = 0; boardCol < COLS; boardCol++)
			{
				numReturned = updateBoard(boardRow, boardCol);
				numBots -= numReturned;
				tempScore += numReturned;
			}
		}
		safeTeleportsLabel.setText("SafeTeleports: " + safeTeleports);
		if(human.isAlive())
		{
			score += tempScore;

			if(waitMode)
			{
				waitScore += tempScore;
			}
		}
		board = tempBoard;
	}

	/**
	 * Used for updating the board. Called for each location. Saves the new piece to a location on the tempBoard.  If the selected piece lands on another piece on the tempBoard, A Wreck should created in the spot with the spot's location.
	 *
	 * @param boardRow The row of the piece being moved.
	 * @param boardCol The column of the piece being moved.
	 * @return Returns 1 if the piece to move dies.  Returns 2 if lands on another piece.  Otherwise, returns 0.
	 */
	private int updateBoard(int boardRow, int boardCol)
	{
		int rowsTo, colsTo, tempRow, tempCol;
		if(board[boardRow][boardCol] instanceof Wreck)
		{
			if(tempBoard[boardRow][boardCol] instanceof Robot)
			{
				tempBoard[boardRow][boardCol] = board[boardRow][boardCol];
				return 1;
			}
			tempBoard[boardRow][boardCol] = board[boardRow][boardCol];
		}
		else if(board[boardRow][boardCol] instanceof Robot)
		{
			rowsTo = human.getRow() - boardRow;
			colsTo = human.getCol() - boardCol;
			if(rowsTo < 0)
			{
				tempRow = boardRow - 1;
			}
			else if(rowsTo > 0)
			{
				tempRow = boardRow + 1;
			}
			else
			{
				tempRow = boardRow;
			}
			if(colsTo < 0)
			{
				tempCol = boardCol - 1;
			}
			else if(colsTo > 0)
			{
				tempCol = boardCol + 1;
			}		
			else
			{
				tempCol = boardCol;
			}
			if(tempBoard[tempRow][tempCol] instanceof Robot)
			{
				tempBoard[tempRow][tempCol] = new Wreck(tempRow, tempCol);
				return 2;
			}
			else if(tempBoard[tempRow][tempCol] instanceof Wreck)
			{
				return 1;
			}
			else if(tempBoard[tempRow][tempCol] instanceof Player)
			{
				human.die();
			}
			else //(tempBoard[tempRow][tempCol] instanceof Location)
			{
				tempBoard[tempRow][tempCol] = board[boardRow][boardCol];
			}
		}
		return 0;
	}

	/**
	 *  Creates a new array of Locations the size of the board.  Each Location refers to its spot int the array.
	 * 
	 * @return A board full of locations reflecting their spots on the board.
	 */
	private Location[][] createBoard()
	{
		Location[][] temp = new Location[ROWS][COLS];
		for(int row = 0; row < ROWS; row++)
		{
			for(int col = 0; col < COLS; col++)
			{
				temp[row][col] = new Location(row, col);
			}
		}
		return temp;
	}

	/**
	 *  Puts the robots on the board.  Should be called only for a clear board.
	 */
	private void fillBots()
	{
		int row, col;
		this.setNumBots(level * 5);
		for(int n = 0; n < numBots; )
		{
			row = generator.nextInt(ROWS);
			col = generator.nextInt(COLS);
			if(!(board[row][col] instanceof Robot))
			{
				board[row][col] = addEnemy(row, col, n);
				n++;
			}
		}
	}

	/**
	 * Decides what enemy to add.
	 *
	 * @param row The row to add the enemy to.
	 * @param col The column to add the enemy to.
	 * @param n The nth enemy being added.
	 * @return The enemy to add to the board.  The Location should return true for isEnemy().
	 */
	private Location addEnemy(int row, int col, int n)
	{
		return new Robot(row, col);
	}

	/**
	 * Increases the game level.  Called when the player beats his\her current level.  Clears the old board and adds a new number of Robots randomly positioned.  Positions the player in the middle of the board.
	 */
	public void increaseLevel()
	{
		level++;

		this.increaseSafeTeleports(waitScore);
		waitMode = false;

		do
		{
			board = createBoard();
			this.fillBots();
		}
		while(board[ROWS / 2][COLS / 2] instanceof Robot);
		human = new Player(ROWS / 2, COLS / 2);
		board[ROWS / 2][COLS / 2] = human;
		levelLabel.setText("Level: " + level);
		safeTeleportsLabel.setText("SafeTeleports: " + safeTeleports);

		this.repaint();
	}

	/**
	 * Decreases the amount of safe teleports by 1.
	 */
	public void decreaseSafeTeleports()
	{
		this.increaseSafeTeleports(-1);
	}

	/**
	 * Increases the amount of safe teleports.  Caps the number of safe teleports to MAX_SAFETELEPORTS.
	 *
	 * @param amount The amount to increase the number of safe teleports by.
	 */
	 public void increaseSafeTeleports(int amount)
	 {
		safeTeleports = (safeTeleports + amount >= MAX_SAFETELEPORTS) ? MAX_SAFETELEPORTS : (safeTeleports + amount);

		//Adds the remaining safe teleports to the Players safe teleport total.
		try
		{
			mysqlBot.increaseSafeTeleports(amount);
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
			submitScore = false;
		}
	 }

	/**
	 * Resets the game. Called if the player wants to play another game.  Updates the score and level labels.  Loads new safe telports.
	 */
	public void resetGame()
	{
		score = 0;
		level = 0;
		try
		{
			safeTeleports = mysqlBot.getSafeTeleports(MAX_SAFETELEPORTS);
			submitScore = true;
		}
		catch(SQLException ex)
		{
			ex.printStackTrace();
			safeTeleports = 0;
			//TODO: add some indication that the game will not add safe teleports/submit scores.
			submitScore = false;
		}
		
		scoreLabel.setText("Score: 0");
		this.increaseLevel();
	}

	/**
	 * Moves the player in the specified Direction.  Calls {@link #updateBoard() updateBoard}.
	 *
	 * @param dir The Direction to move.
	 */
	public void makeMove(Direction dir)
	{
		//Puts the game into waitMode if the Direction is WAIT and waitMode has not yet started.
		if(!waitMode)
		{
			waitScore = 0;
	
			if(dir ==Direction.WAIT)
			{
				waitMode = true;
			}
		}

		int row = 0, col = 0;
		if(dir == Direction.RANDOM || dir == Direction.SAFE)
		{
			boolean safe;
			do
			{
				safe = true;
				row = generator.nextInt(ROWS);
				col = generator.nextInt(COLS);

				//makes sure the selected location is valid
				if(dir == Direction.SAFE && safeTeleports > 0)
				{
					if(board[row][col].isEnemy())
					{
						safe = false;
						continue;
					}
					for(int r = row - 1; r <= row + 1 && safe; r++)
					{
						for(int c = col - 1; c <= col + 1 && safe; c++)
						{
							if(this.isValid(r, c) && board[r][c] instanceof Robot)
							{
								safe = false;
							}
						}
					}
				}
			}
			while(dir == Direction.SAFE && safeTeleports > 0 && !safe);
			if(dir == Direction.SAFE)
			{
				if(safeTeleports > 0)
				{
					this.decreaseSafeTeleports();
					safeTeleportsLabel.setText("SafeTeleports: " + safeTeleports);
				}
			}
			else // dir == Direction.RANDOM
			{
				//The special case that the Player randomly teleports onto an enemy
				if(board[row][col].isEnemy())
				{
					human.die();
				}
			}

			//Virtually move the human.
			human.updatePos(row, col);
		}
		else
		{
			Player loc = new Player(human);
			loc.updatePos(dir);
	
			if(!this.isValid(loc))
			{
				//Cancel the move because the player is trying to move off the board.
				return;
			}
			if(board[loc.getRow()][loc.getCol()].isEnemy())
			{
				if(board[loc.getRow()][loc.getCol()] instanceof Robot)
				{
					human.die();
				}
				else // insntanceof Wreck
				{
					Wreck wreck = (Wreck)board[loc.getRow()][loc.getCol()];
					if(this.isValid(wreck.updatePos(dir)) && board[wreck.getRow()][wreck.getCol()] instanceof Wreck)
					{
						//Not allowed to push a Wreck into a Wreck.
						return;
					}

					//Move Wreck.
					Location wreckDestination = new Location(wreck);
					wreck = new Wreck(loc);
					if(board[wreckDestination.getRow()][wreckDestination.getCol()] instanceof Robot)
					{
						wreck.triggerSplat();
						numBots--;
					}
					//"wreck" is not a wreck.  "loc" is still the Wreck.
					board[wreckDestination.getRow()][wreckDestination.getCol()] = wreck.updatePos(dir);
					//Make the old place Wreck was a location (The Player will move onto it.
					board[loc.getRow()][loc.getCol()] = new Location(loc);
				}
			}

			//Virtually Move the human.
			human.updatePos(dir);
		}

		//Actually do the updating.
		board[human.getRow()][human.getCol()] = human;
		this.updateBoard();
	}

	/**
	 * Tells if the specified row and column is on the board.
	 *
	 * @param row The row to test if is on the board.
	 * @param col The column to test if is on the board.
	 * @return True if the specified location is on the board. Otherwise, false.
	 */
	private boolean isValid(int row, int col)
	{
		return row >= 0 && row < ROWS && col >= 0 && col < COLS;
	}

	 /**
	 * Tells if the specified row and column is on the board.
	 *
	 * @param loc The Location to test if is on the board.
	 * @return True if the specified location is on the board. Otherwise, false.
	 */
	private boolean isValid(Location loc)
	{
		return this.isValid(loc.getRow(), loc.getCol());
	}
}
