import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;


/**
 * A fast robots game with no safe teleports.
 */
public class FastRobotsGame extends SafeTeleportsGame
{
	private Image fastRobotImage;

	//Tells void updateBoard(int, int) wether or not to only update fast robot's position.
	private boolean fastOnly;

	/**
	 * Creates a new FastRobtsGame.
	 *
	 * @param hasSafeTeleports Whether or not the game has safe teleports.

	 */
	public FastRobotsGame(boolean hasSafeTeleports)
	{
		super(hasSafeTeleports);
	}
	/**
	 * Initializes the images of the painted Locations.
	 */
	protected void initializeImages()
	{
		try
		{
			fastRobotImage = ImageIO.read(new File("FastRobot.jpg"));
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		super.initializeImages();
	}

	/**
	 * Adds the FastRobotImage to the group to be returned.
	 *
	 * @param row The row of the location on the board to get the image for
	 * @param col The column of the location on the board to get the image for.
	 * @return The image of the board's row and column.
	 */
	protected Image getImage(int row, int col)
	{
		if(board[row][col] instanceof FastRobot)
		{
			return fastRobotImage;
		}
		return super.getImage(row, col);
	}

	/**
	 * Moves the player in the specified Direction. Moves the fast robots twice.
	 *
	 * @param dir The Direction to move.
	 * @return True if the move is sucessful, otherwise, false.
	 */
	public boolean makeMove(Direction dir)
	{
		Location[][] fastTempBoard = board;
		Player tempHuman = new Player(human);

		if(super.makeMove(dir))
		{
			// Makes only the fast robots move again.  Moves for SAME to return true or false.
			fastOnly = true;
			boolean sucessful = super.makeMove(Direction.SAME);
			fastOnly = false;

			if(dir != Direction.CONTINOUS && !sucessful)
			{
				board = fastTempBoard;
				board[human.getRow()][human.getCol()] = new Location(human.getRow(), human.getCol());
				human = tempHuman;
				board[human.getRow()][human.getCol()] = human;
				return false;
			}

			// Both moves worked
			return true;
		}

		// First move did not work.
		return false;
	}

	/**
	 * Used for updating the board. Called for each location. Saves the new piece to a location on the tempBoard.  If the selected piece lands on another piece on the tempBoard, A Wreck should created in the boardSpot with the boardSpot's location.
	 *
	 * @param boardRow The row of the piece being moved.
	 * @param boardCol The column of the piece being moved.
	 * @return Returns 1 if the piece to move dies.  Returns 2 if lands on another piece.  Otherwise returns 0.
	 */
	protected int updateBoard(int boardRow, int boardCol)
	{
		Location boardSpot = board[boardRow][boardCol];
		if(!fastOnly || boardSpot instanceof FastRobot)
		{
			return super.updateBoard(boardRow, boardCol);
		}
		if(boardSpot instanceof FastRobot)
		{
			return super.updateBoard(boardRow, boardCol);
		}

		// Normal move  of pice (direct translation).   Yes, this else block is unneeded, but it adds to clarity.
		if(tempBoard[boardRow][boardCol] instanceof FastRobot)
		{
			if(boardSpot instanceof Robot)
			{
				tempBoard[boardRow][boardCol] = new Wreck(boardRow, boardCol);
				return 2;
			}
			if(boardSpot instanceof Wreck)
			{
				tempBoard[boardRow][boardCol] = boardSpot;
				return 1;
			}
			if(boardSpot instanceof Player)
			{
				((Player)boardSpot).die();
				return 1;
			}
		}
		else
		{
			tempBoard[boardRow][boardCol] = boardSpot;
		}
		return 0;
	}

	/**
	 * Adds normal robots on every number or a fast bot on odd numbers. Only adds fast robots if the number of robots to add is greater than 2.
	 *
	 * @param row The row to add the robot to.
	 * @param col The column to add the robot to.
	 * @param pos The nth enemy being added.
	 * @return The enemy to add to the board.  The Location should return true for isEnemy().
	 */
	protected Location addEnemy(int row, int col, int pos)
	{
		if(pos > 2 && pos % 2 == 1)
		{
			return new FastRobot(row, col);
		}
		else //Adds a robot
		{
			return super.addEnemy(row, col, pos);
		}
	}
}