import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JTextField;

/**
 * Gets the player's name for a new high score.
 */
public class NameGetterFrame extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 7881462332496478373L;
	private final int score;
	private HighScoresFrame highScoresFrame;
	private JTextField inputField;
	private Gui currentGui;

	/**
	 * Creates a frame that gets the name of the player to add to the list of high scores.
	 * 
	 * @param highScoresFrame The high scores frame.  Used to open the high scores frame when a name is entered.
	 * @param score The score that has just been made.
	 * @param currentGui  The current gui of the game being played.  Gets hidden when the frame is made visible.
	 */
	public NameGetterFrame(HighScoresFrame highScoresFrame, final int score, Gui currentGui)
	{
		super("Congragulations!");
		
		this.highScoresFrame = highScoresFrame;
		this.score = score;
		this.currentGui = currentGui;
		JLabel highScoreLabel = new JLabel("You got a new high score of " + score + "!");
		this.inputField = new JTextField("Anonymous");
		JButton okButton = new JButton("Ok");
			okButton.setActionCommand("ok");
			okButton.addActionListener(this);

		this.add(highScoreLabel, BorderLayout.NORTH);
		this.add(inputField, BorderLayout.CENTER);
		this.add(okButton, BorderLayout.SOUTH);
		this.pack();
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.setLocationRelativeTo(currentGui);
		
		currentGui.setVisible(false);
		this.setVisible(true);
	}

	/**
	 * The action performed when a button is pressed.
	 *
	 * @param event The event triggered by the close button.
	 */
	public void actionPerformed(ActionEvent event)
	{
		if(event.getActionCommand().equals("ok"))
		{
			this.highScoresFrame.addScore(this.currentGui.getGameType(), this.inputField.getText() + ' ' + score);
			this.highScoresFrame.saveScores();
			this.highScoresFrame.setVisible(true, true);
		}
		this.dispose();
	}
}
