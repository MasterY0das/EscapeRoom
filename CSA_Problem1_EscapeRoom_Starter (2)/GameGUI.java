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

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;

import java.io.File;
import javax.imageio.ImageIO;

import java.util.Random;

// game board displays grid with player and obstacles
public class GameGUI extends JComponent
{
  static final long serialVersionUID = 141L;

  private static final int SPACE_SIZE = 60;
  private static final int GRID_W = 8;
  private static final int GRID_H = 5;
  private static final int WIDTH = GRID_W * SPACE_SIZE + 30;
  private static final int HEIGHT = GRID_H * SPACE_SIZE + 60;
  private static final int PLAYER_RENDER_SIZE = 40;
  private static final int ITEM_RENDER_SIZE = 42;
  private static final int OBSTACLE_RENDER_SIZE = 50;
  private static final int START_LOC_X = (WIDTH - GRID_W * SPACE_SIZE) / 2;
  private static final int START_LOC_Y = (HEIGHT - GRID_H * SPACE_SIZE) / 2;
  
  // player position coords
  int x = START_LOC_X; 
  int y = START_LOC_Y;

  private Image bgImage;
  private Image player;
  private Point playerLoc;
  // count moves player makes
  private int playerSteps;
  // obstacles on board
  private int totalWalls;
  private Rectangle[] walls; 
  // collectible items
  private Image prizeImage;
  private int totalPrizes;
  private Rectangle[] prizes;
  // hidden hazards
  private int totalTraps;
  private Rectangle[] traps;
  // special objects
  private Image rockImage;
  private Image barrierImage;
  private int totalRocks;
  private int totalBarriers;
  private Rectangle[] rocks;
  private Rectangle[] barriers;
  
  // score values for game events
  private int prizeVal = 10;
  private int trapVal = 5;
  private int endVal = 10;
  private int offGridVal = 5;
  private int hitWallVal = 5;
  
  // game state tracking
  private int coinsCollected = 0;
  private boolean gameOver = false;
  // main window
  private JFrame frame;
  // score display
  private JLabel scoreLabel;
  private int currentScore;

  // setup game board, load images, create window
  // setup game board, load images, create window
  public GameGUI(String houseName)
  {
    // load background grid image
    try {
      bgImage = ImageIO.read(new File("grid.png"));      
    } catch (Exception e) {
      System.err.println("Could not open file grid.png");
    }      
    // load prize image
    try {
      prizeImage = ImageIO.read(new File("potion.png"));      
    } catch (Exception e) {
      System.err.println("Could not open file potion.png");
    }
  
    // load player image based on selected house
    String houseFile = "player.png";
    if (houseName != null && !houseName.isEmpty()) {
      houseFile = houseName + ".png";
    }
    try {
      player = ImageIO.read(new File(houseFile));      
    } catch (Exception e) {
     System.err.println("Could not open file " + houseFile + ", trying player.png");
     try {
       player = ImageIO.read(new File("player.png"));
     } catch (Exception e2) {
       System.err.println("Could not open file player.png");
     }
    }
    
    // load rock and barrier images
    try {
      rockImage = ImageIO.read(new File("rock.png"));      
    } catch (Exception e) {
     System.err.println("Could not open file rock.png");
    }
    
    try {
      barrierImage = ImageIO.read(new File("barrier.png"));      
    } catch (Exception e) {
     System.err.println("Could not open file barrier.png");
    }
    
    // init player location and score
    playerLoc = new Point(x,y);
    currentScore = 0;
    // create main window
    frame = new JFrame();
    frame.setTitle("Hogwarts: Escape from Snape's Dungeon");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setResizable(false);
    
    JPanel mainPanel = new JPanel(new BorderLayout());
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    scoreLabel = new JLabel("Score: 0");
    scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
    topPanel.add(scoreLabel);
    mainPanel.add(topPanel, BorderLayout.NORTH);
    
    setPreferredSize(new Dimension(WIDTH, HEIGHT));
    mainPanel.add(this, BorderLayout.CENTER);
    
    frame.add(mainPanel);
    frame.setSize(WIDTH, HEIGHT + 50);
    frame.setVisible(true);

    // set number of obstacles
    totalWalls = 12;
    totalPrizes = 3;
    totalTraps = 4;
    totalRocks = 3;
    totalBarriers = 3;
  }

  // create all board obstacles and items
  public void createBoard()
  {
    // setup traps
    traps = new Rectangle[totalTraps];
    createTraps();
    
    // setup prizes to collect
    prizes = new Rectangle[totalPrizes];
    createPrizes();

    // setup walls
    walls = new Rectangle[totalWalls];
    createWalls();
    
    // setup rocks and barriers
    rocks = new Rectangle[totalRocks];
    barriers = new Rectangle[totalBarriers];
    createRocksAndBarriers();
    
    coinsCollected = 0;
    gameOver = false;
  }
  
  public void updateScore(int score) {
    currentScore = score;
    scoreLabel.setText("Score: " + currentScore);
  }
  
  public int getScore() {
    return currentScore;
  }

  // move player by increment, check bounds and collisions
  public int movePlayer(int incrx, int incry)
  {
      // calculate new position
      int newX = x + incrx;
      int newY = y + incry;
      
      // increment step counter
      playerSteps++;

      // check if move goes off grid
      if ( (newX < 0 || newX > WIDTH-SPACE_SIZE) || (newY < 0 || newY > HEIGHT-SPACE_SIZE) )
      {
        System.out.println ("OFF THE GRID!");
        return -offGridVal;
      }

      // check if blocked by obstacle
      if (checkCollision(newX, newY, incrx, incry)) {
        return -hitWallVal;
      }
      
      // move succeeded
      x += incrx;
      y += incry;
      playerLoc.setLocation(x, y);
      
      repaint();   
      return 0;   
  }

  // try to disarm trap at offset from player
  // try to disarm trap at offset from player
  public int springTrap(int newx, int newy)
  {
    // calculate adjacent cell
    double px = playerLoc.getX() + newx;
    double py = playerLoc.getY() + newy;

    // check hidden traps
    for (Rectangle r: traps)
    {
      // found trap at location
      if (r.contains(px, py))
      {
        // if not already sprung
        if (r.getWidth() > 0)
        {
          // disarm it
          r.setSize(0,0);
          System.out.println("TRAP IS SPRUNG!");
          return trapVal;
        }
      }
    }
    
    // check visible barriers
    if (barriers != null) {
      for (Rectangle r : barriers) {
        if (r != null && r.getWidth() > 0) {
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
    
    System.out.println("THERE IS NO TRAP OR BARRIER HERE TO SPRING");
    return -trapVal;
  }

  public int springAdjacentTraps() {
    int[][] directions = {
      {SPACE_SIZE, 0},
      {-SPACE_SIZE, 0},
      {0, SPACE_SIZE},
      {0, -SPACE_SIZE}
    };
    int totalScore = 0;
    boolean sprungAny = false;
    for (int[] dir : directions) {
      if (trapOrBarrierAtOffset(dir[0], dir[1])) {
        totalScore += springTrap(dir[0], dir[1]);
        sprungAny = true;
      }
    }
    if (!sprungAny) {
      System.out.println("NO TRAPS OR BARRIERS NEARBY TO SPRING");
      totalScore -= trapVal;
    }
    return totalScore;
  }
  
  private boolean trapOrBarrierAtOffset(int newx, int newy) {
    double px = playerLoc.getX() + newx;
    double py = playerLoc.getY() + newy;
    for (Rectangle r : traps) {
      if (r != null && r.getWidth() > 0 && r.contains(px, py)) {
        return true;
      }
    }
    if (barriers != null) {
      for (Rectangle r : barriers) {
        if (r != null && r.getWidth() > 0) {
          if (px >= r.getX() && px < r.getX() + r.getWidth() &&
              py >= r.getY() && py < r.getY() + r.getHeight()) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
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

  public int pickupPrize()
  {
    double px = playerLoc.getX();
    double py = playerLoc.getY();

    for (Rectangle p: prizes)
    {
      if (p.getWidth() > 0 && p.contains(px, py))
      {
        System.out.println("You collected a Amortentia Potion ingredient!");
        p.setSize(0,0);
        coinsCollected++;
        System.out.println("Ingredients collected: " + coinsCollected + " / " + totalPrizes);
        repaint();
        return prizeVal;
      }
    }
    System.out.println("No potion ingredient here...");
    return -prizeVal;  
  }
  
  public int jumpPlayer(int incrx, int incry)
  {
    int newX = x + incrx;
    int newY = y + incry;
    
    playerSteps++;
    
    if ( (newX < 0 || newX > WIDTH-SPACE_SIZE) || (newY < 0 || newY > HEIGHT-SPACE_SIZE) )
    {
      System.out.println ("OFF THE GRID!");
      return -offGridVal;
    }
    
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
    
    x = newX;
    y = newY;
    if (checkCollisionForPosition(newX, newY)) {
      x = saveX;
      y = saveY;
      System.out.println("CANNOT JUMP - OBSTACLE AT DESTINATION");
      return -hitWallVal;
    }
    
    if (isTrapAtPosition(newX, newY)) {
      System.out.println("-----------------------------------------------------------");
      System.out.println("You jumped into Snape's trap! Game over!");
      System.out.println("You've been caught by Professor Snape!");
      System.out.println("-----------------------------------------------------------");
      gameOver = true;
      System.out.println("You can quit (q) or replay (replay)");
      x = saveX;
      y = saveY;
      playerLoc.setLocation(x, y);
      repaint();
      return -1000;
    }
    
    playerLoc.setLocation(x, y);
    repaint();
    return 0;
  }
  
  private boolean checkCollisionForPosition(int posX, int posY) {
    for (Rectangle r: walls) {
      int startX = (int)r.getX();
      int endX = (int)r.getX() + (int)r.getWidth();
      int startY = (int)r.getY();
      int endY = (int)r.getY() + (int)r.getHeight();
      
      if (posX < endX && posX + 40 > startX && posY < endY && posY + 40 > startY) {
        return true;
      }
    }
    
    if (barriers != null) {
      for (Rectangle r : barriers) {
        if (r != null && r.getWidth() > 0) {
          if (posX < r.getX() + r.getWidth() && posX + 40 > r.getX() && 
              posY < r.getY() + r.getHeight() && posY + 40 > r.getY()) {
            return true;
          }
        }
      }
    }
    
    return false;
  }

  public int getSteps()
  {
    return playerSteps;
  }
  
  public void setPrizes(int p) 
  {
    totalPrizes = p;
  }
  
  public int getCoinsCollected() {
    return coinsCollected;
  }
  
  public int getTotalCoins() {
    return totalPrizes;
  }
  
  public boolean isGameOver() {
    return gameOver;
  }
  
  public void resetGameOver() {
    gameOver = false;
  }
  
  public boolean isTrapAtPosition(int posX, int posY) {
    for (Rectangle r : traps) {
      if (r != null && r.getWidth() > 0) {
        if (posX < r.getX() + r.getWidth() && posX + PLAYER_RENDER_SIZE > r.getX() &&
            posY < r.getY() + r.getHeight() && posY + PLAYER_RENDER_SIZE > r.getY()) {
          return true;
        }
      }
    }
    return false;
  }
  
  public void setTraps(int t) 
  {
    totalTraps = t;
  }
  
  public void setWalls(int w) 
  {
    totalWalls = w;
  }

  public int replay()
  {
    int win = playerAtEnd();
  
    for (Rectangle p: prizes)
      p.setSize(SPACE_SIZE, SPACE_SIZE);
    for (Rectangle t: traps)
      t.setSize(SPACE_SIZE, SPACE_SIZE);
    
    if (barriers != null) {
      for (Rectangle b : barriers) {
        if (b != null) {
          b.setSize(SPACE_SIZE, SPACE_SIZE);
        }
      }
    }

    x = START_LOC_X;
    y = START_LOC_Y;
    playerLoc.setLocation(x, y);
    playerSteps = 0;
    coinsCollected = 0;
    gameOver = false;
    repaint();
    return win;
  }

  public int endGame() 
  {
    int win = playerAtEnd();
  
    setVisible(false);
    frame.dispose();
    return win;
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D)g;

    g2.setPaint(Color.WHITE);
    g2.fillRect(0, 0, WIDTH, HEIGHT);
    if (bgImage != null) {
      g.drawImage(bgImage, START_LOC_X, START_LOC_Y, GRID_W * SPACE_SIZE, GRID_H * SPACE_SIZE, null);
    }
    g2.setPaint(new Color(210, 210, 210));
    int gridWidth = GRID_W * SPACE_SIZE;
    int gridHeight = GRID_H * SPACE_SIZE;
    for (int col = 0; col <= GRID_W; col++) {
      int xLine = START_LOC_X + col * SPACE_SIZE;
      g2.drawLine(xLine, START_LOC_Y, xLine, START_LOC_Y + gridHeight);
    }
    for (int row = 0; row <= GRID_H; row++) {
      int yLine = START_LOC_Y + row * SPACE_SIZE;
      g2.drawLine(START_LOC_X, yLine, START_LOC_X + gridWidth, yLine);
    }

    for (Rectangle p : prizes)
    {
      if (p.getWidth() > 0) 
      {
      int px = (int)p.getX() + (SPACE_SIZE - ITEM_RENDER_SIZE)/2;
      int py = (int)p.getY() + (SPACE_SIZE - ITEM_RENDER_SIZE)/2;
      g.drawImage(prizeImage, px, py, ITEM_RENDER_SIZE, ITEM_RENDER_SIZE, null);
      }
    }

    for (Rectangle r : walls) 
    {
      g2.setPaint(Color.BLACK);
      g2.fill(r);
    }
    
    if (rockImage != null && rocks != null) {
      for (Rectangle r : rocks) 
      {
        if (r != null && r.getWidth() > 0) {
          int rx = (int)r.getX() + (SPACE_SIZE - OBSTACLE_RENDER_SIZE)/2;
          int ry = (int)r.getY() + (SPACE_SIZE - OBSTACLE_RENDER_SIZE)/2;
          g.drawImage(rockImage, rx, ry, OBSTACLE_RENDER_SIZE, OBSTACLE_RENDER_SIZE, null);
        }
      }
    }
    
    if (barrierImage != null && barriers != null) {
      for (Rectangle r : barriers) 
      {
        if (r != null && r.getWidth() > 0) {
          int bx = (int)r.getX() + (SPACE_SIZE - OBSTACLE_RENDER_SIZE)/2;
          int by = (int)r.getY() + (SPACE_SIZE - OBSTACLE_RENDER_SIZE)/2;
          g.drawImage(barrierImage, bx, by, OBSTACLE_RENDER_SIZE, OBSTACLE_RENDER_SIZE, null);
        }
      }
    }
   
    if (player != null) {
      g.drawImage(player, x, y, PLAYER_RENDER_SIZE, PLAYER_RENDER_SIZE, null);
    }
    playerLoc.setLocation(x,y);
  }

  private void createPrizes()
  {
    int s = SPACE_SIZE; 
    Random rand = new Random();
    int placed = 0;
    int attempts = 0;
    while (placed < totalPrizes && attempts < 500)
    {
      attempts++;
      int h = rand.nextInt(GRID_H);
      int w = rand.nextInt(GRID_W);

      int cellX = w * s + START_LOC_X;
      int cellY = h * s + START_LOC_Y;
      if (cellOccupied(prizes, cellX, cellY) || cellOccupied(traps, cellX, cellY)) {
        continue;
      }
      Rectangle r = new Rectangle(cellX, cellY, s, s);
      prizes[placed] = r;
      placed++;
    }
    while (placed < totalPrizes) {
      int cellX = START_LOC_X + placed * s;
      int cellY = START_LOC_Y;
      prizes[placed] = new Rectangle(cellX, cellY, s, s);
      placed++;
    }
  }

  private void createTraps()
  {
    int s = SPACE_SIZE; 
    Random rand = new Random();
    int placed = 0;
    int attempts = 0;
    while (placed < totalTraps && attempts < 500)
    {
      attempts++;
      int h = rand.nextInt(GRID_H);
      int w = rand.nextInt(GRID_W);

      int cellX = w * s + START_LOC_X;
      int cellY = h * s + START_LOC_Y;
      if (cellOccupied(traps, cellX, cellY)) {
        continue;
      }
      Rectangle r = new Rectangle(cellX, cellY, s, s);
      traps[placed] = r;
      placed++;
    }
    while (placed < totalTraps) {
      int cellX = START_LOC_X + placed * s;
      int cellY = START_LOC_Y + s;
      traps[placed] = new Rectangle(cellX, cellY, s, s);
      placed++;
    }
  }

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
         int wallX = START_LOC_X + w * s + s - 5;
         int wallY = START_LOC_Y + h * s;
         r = new Rectangle(wallX, wallY, 8, s);
       }
       else
       {
         int wallX = START_LOC_X + w * s;
         int wallY = START_LOC_Y + h * s + s - 5;
         r = new Rectangle(wallX, wallY, s, 8);
       }
       walls[numWalls] = r;
     }
  }
  
  private void createRocksAndBarriers()
  {
    int s = SPACE_SIZE;
    Random rand = new Random();
    
    for (int i = 0; i < totalRocks; i++) {
      boolean valid = false;
      int attempts = 0;
      while (!valid && attempts < 100) {
        int h = rand.nextInt(GRID_H);
        int w = rand.nextInt(GRID_W);
        int x = w * s + START_LOC_X;
        int y = h * s + START_LOC_Y;
        
        if (x == START_LOC_X && y == START_LOC_Y) {
          continue;
        }
        if (cellOccupied(traps, x, y) || cellOccupied(prizes, x, y)) {
          continue;
        }
        
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
        int h = rand.nextInt(GRID_H);
        int w = rand.nextInt(GRID_W);
        rocks[i] = new Rectangle(w * s + START_LOC_X, h * s + START_LOC_Y, s, s);
      }
    }
    
    for (int i = 0; i < totalBarriers; i++) {
      boolean valid = false;
      int attempts = 0;
      while (!valid && attempts < 100) {
        int h = rand.nextInt(GRID_H);
        int w = rand.nextInt(GRID_W);
        int x = w * s + START_LOC_X;
        int y = h * s + START_LOC_Y;
        
        if (x == START_LOC_X && y == START_LOC_Y) {
          continue;
        }
        if (cellOccupied(traps, x, y) || cellOccupied(prizes, x, y)) {
          continue;
        }
        
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
        int h = rand.nextInt(GRID_H);
        int w = rand.nextInt(GRID_W);
        barriers[i] = new Rectangle(w * s + START_LOC_X, h * s + START_LOC_Y, s, s);
      }
    }
  }
  
  private boolean isAdjacent(int x1, int y1, int x2, int y2, int spaceSize) {
    int dx = Math.abs(x1 - x2);
    int dy = Math.abs(y1 - y2);
    return (dx == spaceSize && dy == 0) || (dx == 0 && dy == spaceSize);
  }

  private boolean cellOccupied(Rectangle[] shapes, int cellX, int cellY) {
    if (shapes == null) {
      return false;
    }
    for (Rectangle r : shapes) {
      if (r != null && r.getWidth() > 0) {
        if ((int)r.getX() == cellX && (int)r.getY() == cellY) {
          return true;
        }
      }
    }
    return false;
  }
  
  private boolean checkCollision(int newX, int newY, int incrx, int incry) {
    int playerCenterX = newX + 20;
    int playerCenterY = newY + 20;
    
    for (Rectangle r: walls) {
      int wallX = (int)r.getX();
      int wallY = (int)r.getY();
      int wallW = (int)r.getWidth();
      int wallH = (int)r.getHeight();
      
      if (playerCenterX >= wallX && playerCenterX < wallX + wallW && 
          playerCenterY >= wallY && playerCenterY < wallY + wallH) {
        System.out.println("Wall collision: player center(" + playerCenterX + "," + playerCenterY + ") wall(" + wallX + "," + wallY + " " + wallW + "x" + wallH + ")");
        return true;
      }
    }
    
    if (rocks != null) {
      for (Rectangle r : rocks) {
        if (r != null && r.getWidth() > 0) {
          if (newX < r.getX() + r.getWidth() && newX + 40 > r.getX() && 
              newY < r.getY() + r.getHeight() && newY + 40 > r.getY()) {
            System.out.println("A ROCK IS IN THE WAY");
            return true;
          }
        }
      }
    }
    
    if (barriers != null) {
      for (Rectangle r : barriers) {
        if (r != null && r.getWidth() > 0) {
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
