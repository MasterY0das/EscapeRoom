import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Image;
import java.awt.Point;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;

import java.io.File;
import javax.imageio.ImageIO;

import java.util.Random;

/**
 * A Game board on which to place and move players.
 * 
 * @author PLTW
 * @version 1.0
 */
public class GameGUI extends JComponent
{
  static final long serialVersionUID = 141L; // problem 1.4.1

  private static final int WIDTH = 510;
  private static final int HEIGHT = 360;
  private static final int SPACE_SIZE = 60;
  private static final int GRID_W = 8;
  private static final int GRID_H = 5;
  private static final int START_LOC_X = 15;
  private static final int START_LOC_Y = 15;
  
  // initial placement of player
  int x = START_LOC_X; 
  int y = START_LOC_Y;

  // grid image to show in background
  private Image bgImage;

  // player image and info
  private Image player;
  private Point playerLoc;
  private int playerSteps;

  // walls, prizes, traps
  private int totalWalls;
  private Rectangle[] walls; 
  private Image prizeImage;
  private int totalPrizes;
  private Rectangle[] prizes;
  private int totalTraps;
  private Rectangle[] traps;
  
  // rocks and barriers
  private Image rockImage;
  private Image barrierImage;
  private int totalRocks;
  private int totalBarriers;
  private Rectangle[] rocks;
  private Rectangle[] barriers;

  // scores, sometimes awarded as (negative) penalties
  private int prizeVal = 10;
  private int trapVal = 5;
  private int endVal = 10;
  private int offGridVal = 5; // penalty only
  private int hitWallVal = 5;  // penalty only

  // game frame
  private JFrame frame;
  
  // GUI components
  private JTextField commandField;
  private JButton enterButton;
  private JLabel scoreLabel;
  private int currentScore;
  
  // Command handler interface
  public interface CommandHandler {
    void handleCommand(String command);
  }
  private CommandHandler commandHandler;

  /**
   * Constructor for the GameGUI class.
   * Creates a frame with a background image and a player that will move around the board.
   */
  public GameGUI()
  {
    
    try {
      bgImage = ImageIO.read(new File("grid.png"));      
    } catch (Exception e) {
      System.err.println("Could not open file grid.png");
    }      
    try {
      prizeImage = ImageIO.read(new File("coin.png"));      
    } catch (Exception e) {
      System.err.println("Could not open file coin.png");
    }
  
    // player image, student can customize this image by changing file on disk
    try {
      player = ImageIO.read(new File("player.png"));      
    } catch (Exception e) {
     System.err.println("Could not open file player.png");
    }
    
    // rock image
    try {
      rockImage = ImageIO.read(new File("rock.png"));      
    } catch (Exception e) {
     System.err.println("Could not open file rock.png");
    }
    
    // barrier image
    try {
      barrierImage = ImageIO.read(new File("barrier.png"));      
    } catch (Exception e) {
     System.err.println("Could not open file barrier.png");
    }
    
    // save player location
    playerLoc = new Point(x,y);
    currentScore = 0;

    // create the game frame
    frame = new JFrame();
    frame.setTitle("EscapeRoom");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setResizable(false);
    
    // Create main panel with border layout
    JPanel mainPanel = new JPanel(new BorderLayout());
    
    // Create top panel for score
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    scoreLabel = new JLabel("Score: 0");
    scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
    topPanel.add(scoreLabel);
    mainPanel.add(topPanel, BorderLayout.NORTH);
    
    // Add game component
    setPreferredSize(new Dimension(WIDTH, HEIGHT));
    mainPanel.add(this, BorderLayout.CENTER);
    
    // Create bottom panel for command input
    JPanel bottomPanel = new JPanel(new FlowLayout());
    commandField = new JTextField(20);
    enterButton = new JButton("Enter");
    bottomPanel.add(new JLabel("Command: "));
    bottomPanel.add(commandField);
    bottomPanel.add(enterButton);
    mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    
    // Set up command handler
    ActionListener commandListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (commandHandler != null) {
          String command = commandField.getText().trim().toLowerCase();
          commandField.setText("");
          commandHandler.handleCommand(command);
        }
      }
    };
    commandField.addActionListener(commandListener);
    enterButton.addActionListener(commandListener);
    
    frame.add(mainPanel);
    frame.setSize(WIDTH, HEIGHT + 100); // Extra space for GUI components
    frame.setVisible(true);

    // set default config
    totalWalls = 20;
    totalPrizes = 3;
    totalTraps = 5;
    totalRocks = 5;
    totalBarriers = 5;
  }

 /**
  * After a GameGUI object is created, this method adds the walls, prizes, and traps to the gameboard.
  * Note that traps and prizes may occupy the same location.
  */
  public void createBoard()
  {
    traps = new Rectangle[totalTraps];
    createTraps();
    
    prizes = new Rectangle[totalPrizes];
    createPrizes();

    walls = new Rectangle[totalWalls];
    createWalls();
    
    rocks = new Rectangle[totalRocks];
    barriers = new Rectangle[totalBarriers];
    createRocksAndBarriers();
  }
  
  /**
   * Set the command handler for processing user commands
   */
  public void setCommandHandler(CommandHandler handler) {
    commandHandler = handler;
  }
  
  /**
   * Update the score display
   */
  public void updateScore(int score) {
    currentScore = score;
    scoreLabel.setText("Score: " + currentScore);
  }
  
  /**
   * Get current score
   */
  public int getScore() {
    return currentScore;
  }

  /**
   * Increment/decrement the player location by the amount designated.
   * This method checks for bumping into walls and going off the grid,
   * both of which result in a penalty.
   * <P>
   * precondition: amount to move is not larger than the board, otherwise player may appear to disappear
   * postcondition: increases number of steps even if the player did not actually move (e.g. bumping into a wall)
   * <P>
   * @param incrx amount to move player in x direction
   * @param incry amount to move player in y direction
   * @return penalty score for hitting a wall or potentially going off the grid, 0 otherwise
   */
  public int movePlayer(int incrx, int incry)
  {
      int newX = x + incrx;
      int newY = y + incry;
      
      // increment regardless of whether player really moves
      playerSteps++;

      // check if off grid horizontally and vertically
      if ( (newX < 0 || newX > WIDTH-SPACE_SIZE) || (newY < 0 || newY > HEIGHT-SPACE_SIZE) )
      {
        System.out.println ("OFF THE GRID!");
        return -offGridVal;
      }

      // Check collision with walls, rocks, and barriers
      if (checkCollision(newX, newY, incrx, incry)) {
        return -hitWallVal;
      }

      // all is well, move player
      x += incrx;
      y += incry;
      repaint();   
      return 0;   
  }

  /**
   * Check the space adjacent to the player for a trap or barrier. The adjacent location is one space away from the player, 
   * designated by newx, newy.
   * <P>
   * precondition: newx and newy must be the amount a player regularly moves, otherwise an existing trap may go undetected
   * <P>
   * @param newx a location indicating the space to the right or left of the player
   * @param newy a location indicating the space above or below the player
   * @return true if the new location has a trap or barrier that has not been sprung, false otherwise
   */
  public boolean isTrap(int newx, int newy)
  {
    double px = playerLoc.getX() + newx;
    double py = playerLoc.getY() + newy;


    for (Rectangle r: traps)
    {
      // DEBUG: System.out.println("trapx:" + r.getX() + " trapy:" + r.getY() + "\npx: " + px + " py:" + py);
      // zero size traps have already been sprung, ignore
      if (r.getWidth() > 0)
      {
        // if new location of player has a trap, return true
        if (r.contains(px, py))
        {
          System.out.println("A TRAP IS AHEAD");
          return true;
        }
      }
    }
    
    // Also check barriers (barriers are traps)
    if (barriers != null) {
      for (Rectangle r : barriers) {
        if (r != null && r.getWidth() > 0) {
          if (r.contains(px, py) || 
              (px >= r.getX() && px < r.getX() + r.getWidth() && 
               py >= r.getY() && py < r.getY() + r.getHeight())) {
            System.out.println("A BARRIER IS AHEAD");
            return true;
          }
        }
      }
    }
    
    // there is no trap or barrier where player wants to go
    return false;
  }

  /**
   * Spring the trap. Traps can only be sprung once and attempts to spring
   * a sprung task results in a penalty.
   * <P>
   * precondition: newx and newy must be the amount a player regularly moves, otherwise an existing trap may go unsprung
   * <P>
   * @param newx a location indicating the space to the right or left of the player
   * @param newy a location indicating the space above or below the player
   * @return a positive score if a trap is sprung, otherwise a negative penalty for trying to spring a non-existent trap
   */
  public int springTrap(int newx, int newy)
  {
    double px = playerLoc.getX() + newx;
    double py = playerLoc.getY() + newy;

    // check all traps, some of which may be already sprung
    for (Rectangle r: traps)
    {
      // DEBUG: System.out.println("trapx:" + r.getX() + " trapy:" + r.getY() + "\npx: " + px + " py:" + py);
      if (r.contains(px, py))
      {
        // zero size traps indicate it has been sprung, cannot spring again, so ignore
        if (r.getWidth() > 0)
        {
          r.setSize(0,0);
          System.out.println("TRAP IS SPRUNG!");
          return trapVal;
        }
      }
    }
    
    // Also check barriers (barriers are traps that can be sprung)
    if (barriers != null) {
      for (Rectangle r : barriers) {
        if (r != null && r.getWidth() > 0) {
          // Check if barrier is at the adjacent location
          if (r.contains(px, py) || 
              (px >= r.getX() && px < r.getX() + r.getWidth() && 
               py >= r.getY() && py < r.getY() + r.getHeight())) {
            r.setSize(0, 0);
            System.out.println("BARRIER IS SPRUNG!");
            repaint();
            return trapVal;
          }
        }
      }
    }
    
    // no trap or barrier here, penalty
    System.out.println("THERE IS NO TRAP OR BARRIER HERE TO SPRING");
    return -trapVal;
  }
  
  /**
   * Check if there is a barrier adjacent to the player
   * @param newx a location indicating the space to the right or left of the player
   * @param newy a location indicating the space above or below the player
   * @return true if there is a barrier at the adjacent location
   */
  public boolean isBarrier(int newx, int newy)
  {
    double px = playerLoc.getX() + newx;
    double py = playerLoc.getY() + newy;

    if (barriers != null) {
      for (Rectangle r : barriers) {
        if (r != null && r.getWidth() > 0) {
          if (r.contains(px, py) || 
              (px >= r.getX() && px < r.getX() + r.getWidth() && 
               py >= r.getY() && py < r.getY() + r.getHeight())) {
            System.out.println("A BARRIER IS AHEAD");
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Pickup a prize and score points. If no prize is in that location, this results in a penalty.
   * <P>
   * @return positive score if a location had a prize to be picked up, otherwise a negative penalty
   */
  public int pickupPrize()
  {
    double px = playerLoc.getX();
    double py = playerLoc.getY();

    for (Rectangle p: prizes)
    {
      // DEBUG: System.out.println("prizex:" + p.getX() + " prizey:" + p.getY() + "\npx: " + px + " py:" + py);
      // if location has a prize, pick it up
      if (p.getWidth() > 0 && p.contains(px, py))
      {
        System.out.println("YOU PICKED UP A PRIZE!");
        p.setSize(0,0);
        repaint();
        return prizeVal;
      }
    }
    System.out.println("OOPS, NO PRIZE HERE");
    return -prizeVal;  
  }
  
  /**
   * Jump the player two spaces in the specified direction.
   * Checks for collisions at both the intermediate and final positions.
   * @param incrx amount to move player in x direction (should be 2*SPACE_SIZE or -2*SPACE_SIZE)
   * @param incry amount to move player in y direction (should be 2*SPACE_SIZE or -2*SPACE_SIZE)
   * @return penalty score for hitting a wall/rock/barrier or going off grid, 0 otherwise
   */
  public int jumpPlayer(int incrx, int incry)
  {
    int newX = x + incrx;
    int newY = y + incry;
    
    // increment steps
    playerSteps++;
    
    // Check if off grid
    if ( (newX < 0 || newX > WIDTH-SPACE_SIZE) || (newY < 0 || newY > HEIGHT-SPACE_SIZE) )
    {
      System.out.println ("OFF THE GRID!");
      return -offGridVal;
    }
    
    // Check intermediate position (one space away) - temporarily move to check
    int saveX = x;
    int saveY = y;
    int midX = x + incrx / 2;
    int midY = y + incry / 2;
    x = midX;
    y = midY;
    if (checkCollisionForPosition(midX, midY)) {
      x = saveX;
      y = saveY;
      System.out.println("CANNOT JUMP - OBSTACLE IN THE WAY");
      return -hitWallVal;
    }
    
    // Check final position
    x = newX;
    y = newY;
    if (checkCollisionForPosition(newX, newY)) {
      x = saveX;
      y = saveY;
      System.out.println("CANNOT JUMP - OBSTACLE AT DESTINATION");
      return -hitWallVal;
    }
    
    // All clear, keep new position
    repaint();
    return 0;
  }
  
  /**
   * Check for collision at a specific position (used for jump)
   * Note: Rocks can be jumped over, but barriers cannot
   */
  private boolean checkCollisionForPosition(int posX, int posY) {
    // Check if position overlaps with any wall (walls are line segments)
    for (Rectangle r: walls) {
      int startX = (int)r.getX();
      int endX = (int)r.getX() + (int)r.getWidth();
      int startY = (int)r.getY();
      int endY = (int)r.getY() + (int)r.getHeight();
      
      // Check if player position overlaps with wall rectangle
      if (posX < endX && posX + 40 > startX && posY < endY && posY + 40 > startY) {
        return true;
      }
    }
    
    // Rocks can be jumped over, so don't check them here
    
    // Check barriers (barriers block jumps)
    if (barriers != null) {
      for (Rectangle r : barriers) {
        if (r != null && r.getWidth() > 0) {
          // Check if player position overlaps with barrier
          if (posX < r.getX() + r.getWidth() && posX + 40 > r.getX() && 
              posY < r.getY() + r.getHeight() && posY + 40 > r.getY()) {
            return true;
          }
        }
      }
    }
    
    return false;
  }

  /**
   * Return the numbers of steps the player has taken.
   * <P>
   * @return the number of steps
   */
  public int getSteps()
  {
    return playerSteps;
  }
  
  /**
   * Set the designated number of prizes in the game.  This can be used to customize the gameboard configuration.
   * <P>
   * precondition p must be a positive, non-zero integer
   * <P>
   * @param p number of prizes to create
   */
  public void setPrizes(int p) 
  {
    totalPrizes = p;
  }
  
  /**
   * Set the designated number of traps in the game. This can be used to customize the gameboard configuration.
   * <P>
   * precondition t must be a positive, non-zero integer
   * <P>
   * @param t number of traps to create
   */
  public void setTraps(int t) 
  {
    totalTraps = t;
  }
  
  /**
   * Set the designated number of walls in the game. This can be used to customize the gameboard configuration.
   * <P>
   * precondition t must be a positive, non-zero integer
   * <P>
   * @param w number of walls to create
   */
  public void setWalls(int w) 
  {
    totalWalls = w;
  }

  /**
   * Reset the board to replay existing game. The method can be called at any time but results in a penalty if called
   * before the player reaches the far right wall.
   * <P>
   * @return positive score for reaching the far right wall, penalty otherwise
   */
  public int replay()
  {

    int win = playerAtEnd();
  
    // resize prizes and traps to "reactivate" them
    for (Rectangle p: prizes)
      p.setSize(SPACE_SIZE/3, SPACE_SIZE/3);
    for (Rectangle t: traps)
      t.setSize(SPACE_SIZE/3, SPACE_SIZE/3);
    
    // reactivate barriers (barriers are traps)
    if (barriers != null) {
      for (Rectangle b : barriers) {
        if (b != null) {
          b.setSize(SPACE_SIZE, SPACE_SIZE);
        }
      }
    }

    // move player to start of board
    x = START_LOC_X;
    y = START_LOC_Y;
    playerSteps = 0;
    repaint();
    return win;
  }

 /**
  * End the game, checking if the player made it to the far right wall.
  * <P>
  * @return positive score for reaching the far right wall, penalty otherwise
  */
  public int endGame() 
  {
    int win = playerAtEnd();
  
    setVisible(false);
    frame.dispose();
    return win;
  }

  /*------------------- public methods not to be called as part of API -------------------*/

  /** 
   * For internal use and should not be called directly: Users graphics buffer to paint board elements.
   */
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D)g;

    // draw grid
    g.drawImage(bgImage, 0, 0, null);

    // add (invisible) traps
    for (Rectangle t : traps)
    {
      g2.setPaint(Color.WHITE); 
      g2.fill(t);
    }

    // add prizes
    for (Rectangle p : prizes)
    {
      // picked up prizes are 0 size so don't render
      if (p.getWidth() > 0) 
      {
      int px = (int)p.getX();
      int py = (int)p.getY();
      g.drawImage(prizeImage, px, py, null);
      }
    }

    // add walls
    for (Rectangle r : walls) 
    {
      g2.setPaint(Color.BLACK);
      g2.fill(r);
    }
    
    // add rocks
    if (rockImage != null && rocks != null) {
      for (Rectangle r : rocks) 
      {
        if (r != null && r.getWidth() > 0) {
          int rx = (int)r.getX();
          int ry = (int)r.getY();
          g.drawImage(rockImage, rx, ry, SPACE_SIZE, SPACE_SIZE, null);
        }
      }
    }
    
    // add barriers
    if (barrierImage != null && barriers != null) {
      for (Rectangle r : barriers) 
      {
        if (r != null && r.getWidth() > 0) {
          int bx = (int)r.getX();
          int by = (int)r.getY();
          g.drawImage(barrierImage, bx, by, SPACE_SIZE, SPACE_SIZE, null);
        }
      }
    }
   
    // draw player, saving its location
    g.drawImage(player, x, y, 40,40, null);
    playerLoc.setLocation(x,y);
  }

  /*------------------- private methods -------------------*/

  /*
   * Add randomly placed prizes to be picked up.
   * Note:  prizes and traps may occupy the same location, with traps hiding prizes
   */
  private void createPrizes()
  {
    int s = SPACE_SIZE; 
    Random rand = new Random();
     for (int numPrizes = 0; numPrizes < totalPrizes; numPrizes++)
     {
      int h = rand.nextInt(GRID_H);
      int w = rand.nextInt(GRID_W);

      Rectangle r;
      r = new Rectangle((w*s + 15),(h*s + 15), 15, 15);
      prizes[numPrizes] = r;
     }
  }

  /*
   * Add randomly placed traps to the board. They will be painted white and appear invisible.
   * Note:  prizes and traps may occupy the same location, with traps hiding prizes
   */
  private void createTraps()
  {
    int s = SPACE_SIZE; 
    Random rand = new Random();
     for (int numTraps = 0; numTraps < totalTraps; numTraps++)
     {
      int h = rand.nextInt(GRID_H);
      int w = rand.nextInt(GRID_W);

      Rectangle r;
      r = new Rectangle((w*s + 15),(h*s + 15), 15, 15);
      traps[numTraps] = r;
     }
  }

  /*
   * Add walls to the board in random locations 
   */
  private void createWalls()
  {
     int s = SPACE_SIZE; 

     Random rand = new Random();
     for (int numWalls = 0; numWalls < totalWalls; numWalls++)
     {
      int h = rand.nextInt(GRID_H);
      int w = rand.nextInt(GRID_W);

      Rectangle r;
       if (rand.nextInt(2) == 0) 
       {
         // vertical wall
         r = new Rectangle((w*s + s - 5),h*s, 8,s);
       }
       else
       {
         /// horizontal
         r = new Rectangle(w*s,(h*s + s - 5), s, 8);
       }
       walls[numWalls] = r;
     }
  }
  
  /*
   * Add rocks and barriers to the board in random locations.
   * Ensures rocks and barriers are never adjacent.
   */
  private void createRocksAndBarriers()
  {
    int s = SPACE_SIZE;
    Random rand = new Random();
    
    // Create rocks
    for (int i = 0; i < totalRocks; i++) {
      boolean valid = false;
      int attempts = 0;
      while (!valid && attempts < 100) {
        int h = rand.nextInt(GRID_H);
        int w = rand.nextInt(GRID_W);
        int x = w * s + START_LOC_X;
        int y = h * s + START_LOC_Y;
        
        // Check if position conflicts with player start
        if (x == START_LOC_X && y == START_LOC_Y) {
          continue;
        }
        
        // Check if adjacent to any existing rock or barrier
        valid = true;
        if (rocks != null) {
          for (int j = 0; j < i; j++) {
            if (rocks[j] != null && isAdjacent(x, y, (int)rocks[j].getX(), (int)rocks[j].getY(), s)) {
              valid = false;
              break;
            }
          }
        }
        if (valid && barriers != null) {
          for (int j = 0; j < totalBarriers; j++) {
            if (barriers[j] != null && isAdjacent(x, y, (int)barriers[j].getX(), (int)barriers[j].getY(), s)) {
              valid = false;
              break;
            }
          }
        }
        
        if (valid) {
          rocks[i] = new Rectangle(x, y, s, s);
        }
        attempts++;
      }
      if (!valid) {
        // Fallback: place anyway if we can't find a good spot
        int h = rand.nextInt(GRID_H);
        int w = rand.nextInt(GRID_W);
        rocks[i] = new Rectangle(w * s + START_LOC_X, h * s + START_LOC_Y, s, s);
      }
    }
    
    // Create barriers
    for (int i = 0; i < totalBarriers; i++) {
      boolean valid = false;
      int attempts = 0;
      while (!valid && attempts < 100) {
        int h = rand.nextInt(GRID_H);
        int w = rand.nextInt(GRID_W);
        int x = w * s + START_LOC_X;
        int y = h * s + START_LOC_Y;
        
        // Check if position conflicts with player start
        if (x == START_LOC_X && y == START_LOC_Y) {
          continue;
        }
        
        // Check if adjacent to any existing rock or barrier
        valid = true;
        if (rocks != null) {
          for (Rectangle rock : rocks) {
            if (rock != null && isAdjacent(x, y, (int)rock.getX(), (int)rock.getY(), s)) {
              valid = false;
              break;
            }
          }
        }
        if (valid) {
          for (int j = 0; j < i; j++) {
            if (barriers[j] != null && isAdjacent(x, y, (int)barriers[j].getX(), (int)barriers[j].getY(), s)) {
              valid = false;
              break;
            }
          }
        }
        
        if (valid) {
          barriers[i] = new Rectangle(x, y, s, s);
        }
        attempts++;
      }
      if (!valid) {
        // Fallback: place anyway if we can't find a good spot
        int h = rand.nextInt(GRID_H);
        int w = rand.nextInt(GRID_W);
        barriers[i] = new Rectangle(w * s + START_LOC_X, h * s + START_LOC_Y, s, s);
      }
    }
  }
  
  /**
   * Check if two positions are adjacent (horizontally or vertically)
   */
  private boolean isAdjacent(int x1, int y1, int x2, int y2, int spaceSize) {
    int dx = Math.abs(x1 - x2);
    int dy = Math.abs(y1 - y2);
    return (dx == spaceSize && dy == 0) || (dx == 0 && dy == spaceSize);
  }
  
  /**
   * Check for collision with walls, rocks, or barriers
   */
  private boolean checkCollision(int newX, int newY, int incrx, int incry) {
    // Check walls
    for (Rectangle r: walls) {
      int startX = (int)r.getX();
      int endX = (int)r.getX() + (int)r.getWidth();
      int startY = (int)r.getY();
      int endY = (int)r.getY() + (int)r.getHeight();

      // moving RIGHT, check to the right
      if ((incrx > 0) && (x <= startX) && (startX <= newX) && (y >= startY) && (y <= endY)) {
        System.out.println("A WALL IS IN THE WAY");
        return true;
      }
      // moving LEFT, check to the left
      else if ((incrx < 0) && (x >= startX) && (startX >= newX) && (y >= startY) && (y <= endY)) {
        System.out.println("A WALL IS IN THE WAY");
        return true;
      }
      // moving DOWN check below
      else if ((incry > 0) && (y <= startY && startY <= newY && x >= startX && x <= endX)) {
        System.out.println("A WALL IS IN THE WAY");
        return true;
      }
      // moving UP check above
      else if ((incry < 0) && (y >= startY) && (startY >= newY) && (x >= startX) && (x <= endX)) {
        System.out.println("A WALL IS IN THE WAY");
        return true;
      }
    }
    
    // Check rocks (block regular movement)
    if (rocks != null) {
      for (Rectangle r : rocks) {
        if (r != null && r.getWidth() > 0) {
          // Check if player rectangle overlaps with rock rectangle
          if (newX < r.getX() + r.getWidth() && newX + 40 > r.getX() && 
              newY < r.getY() + r.getHeight() && newY + 40 > r.getY()) {
            System.out.println("A ROCK IS IN THE WAY");
            return true;
          }
        }
      }
    }
    
    // Check barriers (block regular movement)
    if (barriers != null) {
      for (Rectangle r : barriers) {
        if (r != null && r.getWidth() > 0) {
          // Check if player rectangle overlaps with barrier rectangle
          if (newX < r.getX() + r.getWidth() && newX + 40 > r.getX() && 
              newY < r.getY() + r.getHeight() && newY + 40 > r.getY()) {
            System.out.println("A BARRIER IS IN THE WAY");
            return true;
          }
        }
      }
    }
    
    return false;
  }

  /**
   * Checks if player as at the far right of the board 
   * @return positive score for reaching the far right wall, penalty otherwise
   */
  private int playerAtEnd() 
  {
    int score;

    double px = playerLoc.getX();
    if (px > (WIDTH - 2*SPACE_SIZE))
    {
      System.out.println("YOU MADE IT!");
      score = endVal;
    }
    else
    {
      System.out.println("OOPS, YOU QUIT TOO SOON!");
      score = -endVal;
    }
    return score;
  
  }
}