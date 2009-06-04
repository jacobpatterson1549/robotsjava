import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import java.awt.Dimension;

/**
 * The default form of the robots game.
 */
public abstract class Game extends JPanel
{
	private static final long serialVersionUID = -5019512195986383612L;
	private Dimension dimension;
	private int level;
	private int score;
	private int numBots;
	private Image playerAliveImage;
	private Image playerDeadImage;
	private Image robotImage;
	private Image wreckImage;

	/**
	 * Method to tell if the game has safe teleports.
	 *
	 * @return True if the game has safe teleports.  Otherwise, false.
	 */
	public boolean isSafeTeleportsGame() { return false; }

	/**
	 * Used to randomly teleport the Player.
	 */
	protected Random generator;

	/**
	 * The width of the board.
	 */
	protected final int ROWS;

	/**
	 * The width of the board.
	 */
	protected final int COLS;


	/**
	 * The container of all the Locations.
	 */
	protected Location[][] board;

	/**
	 * The only Player on the board
	 */
	protected Player human;

	/**
	 * Getter for the player. There should only be 1 player (human).
	 *
	 * @return The player.
	 */
	public Player getHuman() { return human; }

	/**
	 * Getter for the level of the game.
	 *
	 * @return The level of the game.
	 */
	public int getLevel() { return level; }

	/**
	 * Getter for the score of the game.
	 *
	 * @return The score of the game.
	 */
	public int getScore() { return score; }

	/**
	 * Getter for the number of robots on the board.  A variable is returned to increase game speed. This makes the game not have an "int numBots()" to count the number of robots on the board every time it is called.
	 *
	 * @return The number of robots on the board.
	 */
	public int numBots() { return numBots; }

	/**
	 * Creates a new Game. Calls resetBoard to add the board.
	 */
	public Game()
	{
		super(false);
		generator = new Random();
		ROWS = 30;
		COLS = 40;
		resetBoard();
		dimension = new Dimension((COLS * 20) + 10, (ROWS * 20) + 10);
		initializeImages();
	}

	/**
	 * Puts the robots on the board.  Same as Game's  void setNumBots(int), but the number of robots is capped at SIDE * SIDE / 2.  This prevents the Player from having no possible safe teleports because of the amount of SafeTeleports.
	 *
	 * @param numBots Starts at 2, increases by 1 on each level increase.
	 */
	protected void setNumBots(int numBots)
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
	 * Used for updating the board with updateBoard(int, int). Is cleared on each player move.
	 */
	protected Location[][] tempBoard;

	/**
	 * Initializes the images of the painted Locations.
	 */
	protected void initializeImages()
	{
		try
		{
			playerAliveImage = ImageIO.read(new File("PlayerAlive.jpg"));
			playerDeadImage = ImageIO.read(new File("PlayerDead.jpg"));
			robotImage = ImageIO.read(new File("Robot.jpg"));
			wreckImage = ImageIO.read(new File("Wreck.jpg"));
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * Paints the board.
	 */
	public void paint(Graphics g)
	{
		this.setSize(dimension);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, (COLS * 20) + 10, (ROWS * 20) + 10);
		g.setColor(Color.LIGHT_GRAY);
  		for(int row = 0; row < ROWS; row++)
		{
			for(int col = 0; col < COLS; col++)
			{
				g.drawLine(20 * col, 0, (20 * col) + 0, (20 * row) + 20);
				g.drawLine(0, 20 * row, (20 * col) + 20, (20 * row) + 0);
				g.drawImage( getImage(row, col), 20 * col, 20 * row, null);
			}	
    		}
		g.drawLine(0, 0, 0, 20 * ROWS);
		g.drawLine(0, 0, 20 * COLS, 0);
		g.drawLine(0, 20 * ROWS, 20 * COLS, 20 * ROWS);
		g.drawLine(20 * COLS, 0, 20 * COLS, 20 * ROWS);
	}

	/**
	 * Gets the image for location in the grid.
	 *
	 * @param row The row of the location on the board to get the image for
	 * @param col The column of the location on the board to get the image for.
	 * @return The image of the board's row and column.
	 */
	protected Image getImage(int row, int col)
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
			return wreckImage;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Prints the board. Calls void paint(Graphics).
	 */
	public void printBoard()
	{
		paint(this.getGraphics());
		//paintToConsole();
	}

	//paints the board to the console
	private void paintToConsole()
	{
		System.out.println("\n\n\n");
		System.out.println("--Score = " + score + (score < 10 ? '-' : ""));
		System.out.println("----Key-----");
		System.out.println("  1 = Robot");
		System.out.println("  * = Wreck");
		System.out.println("  # = Player (X = DEAD)");
		for(int c = -1; c <= COLS; c++)
		{
			System.out.print('-');
		}
		System.out.println();
		for(int r = 0; r < ROWS; r++)
		{
			System.out.print('|');
			for(int c = 0; c < COLS; c++)
			{
				System.out.print(board[r][c].value());
			}
			System.out.println('|');
		}
		for(int c = -1; c < COLS; c++)
		{
			System.out.print('-');
		}
		System.out.println();
	}

	/**
	 * Updates the board for the Player's current Location.  Runs even if the player will die in his/her current Location.
	 */
	protected void updateBoard()
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
		if(human.isAlive())
		{
			score += tempScore;	
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
	protected int updateBoard(int boardRow, int boardCol)
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


	// Creates a new array of Locations the size of the board.  Each Location refers to its spot int the array.
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

	// Puts the robots on the board.  Should be called only for a clear board.
	private void fillBots()
	{
		int row, col;
		this.setNumBots(level + 1);
		for(int n = 0; n < numBots; n++)
		{
			row = generator.nextInt(ROWS);
			col = generator.nextInt(COLS);
			if(!board[row][col].getClass().getName().equals( "Location"))
			{
				n--;
			}
			else
			{
				board[row][col] = addEnemy(row, col, n);
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
	protected Location addEnemy(int row, int col, int n)
	{
		return new Robot(row, col);
	}

	/**
	 * Increases the game level.  Called when the player beats his\her current level.  Clears the old board and adds a new number of Robots randomly positioned.  Positions the player in the middle of the board.
	 */
	public void increaseLevel()
	{
		level++;
		human = new Player(ROWS / 2, COLS / 2, generator, ROWS, COLS);
		board = createBoard();
		board[ROWS / 2][COLS / 2] = human;
		fillBots();
	}

	/**
	 * Resets the game. Called if the player wants to play another game.
	 */
	public void resetBoard()
	{
		score = 0;
		level = 0;
		this.increaseLevel();
	}

	/**
	 * Moves the player in the specified Direction.  Calls {@link #updateBoard() updateBoard}.
	 *
	 * @param dir The Direction to move.
	 * @return True if the move is successful, otherwise, false.
	 */
	public boolean makeMove(Direction dir)
	{
		if(isValid(human, dir))
		{
			Player loc = new Player(human);
			loc.updatePos(dir);
			if(board[loc.getRow()][loc.getCol()].isEnemy())
			{
				human.die();
			}
			if(dir != Direction.SAME && dir != Direction.CONTINOUS)
			{
				board[human.getRow()][human.getCol()] = new Location(human);
				human.updatePos(dir);
				board[human.getRow()][human.getCol()] = human;
			}
			updateBoard();
			return true;
		}
		return false;
	}

	/**
	 * Sees if it is safe for the specified Location to move in the specified Direction.  If the Direction is CONTINOUS or RANDOM, true will be returned.  Uses {@link #isValid(Location) isValid(Location)} and {@link #locAround(Location) locsAround(Location)}.
	 *
	 * @param testLocation The starting Location.
	 * @param dir The Direction to see if is valid for the testLocation to move to.
	 */
	protected boolean isValid(Location testLocation, Direction dir)
	{
		if(dir == Direction.RANDOM || dir == Direction.CONTINOUS)
		{
			return true;
		}
		Location desiredLocation = new Location(testLocation);
		desiredLocation = desiredLocation.updatePos(dir);
		if(isValid(desiredLocation))
		{
			if(board[desiredLocation.getRow()][desiredLocation.getCol()].isEnemy())
			{
				return false;
			}
			for(Location spot : locsAround(desiredLocation))
			{
				if(spot instanceof Robot)
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Gets a list of valid locations around the specified location.
	 *
	 * @param loc The location to get the list of locations around.
	 * @return The list of locations.
	 */
	protected Location[] locsAround(Location loc)
	{
		Location[] locationList = new Location[8];
		Location testLocation;
		int pos = 0;
		for(Direction dir : Direction.values())
		{
			if(dir.ordinal() < 9 && dir != Direction.SAME)
			{
				testLocation = new Location(loc);
				testLocation.updatePos(dir);
				if(isValid(testLocation))
				{
					locationList[pos++] = board[testLocation.getRow()][testLocation.getCol()];
				}
			}
		}
		return locationList;
	}

	/**
	 * Tells if a Location is on the board.
	 *
	 * @param loc The location to see if is valid.
	 * @return True if the specified location is on the board. Otherwise, false.
	 */
	protected boolean isValid(Location loc)
	{
 		if(loc.getRow() >= 0 && loc.getRow() < ROWS && loc.getCol() >= 0 && loc.getCol() < COLS)
		{
			return true;
		}
		return false;
	}
}
