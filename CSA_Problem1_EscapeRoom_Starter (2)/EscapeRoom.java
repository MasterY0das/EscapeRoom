/*
* Problem 1: Escape Room
* 
* V1.0
* 10/10/2019
* Copyright(c) 2019 PLTW to present. All rights reserved
*/

/**
 * Create an escape room game where the player must navigate
 * to the other side of the screen in the fewest steps, while
 * avoiding obstacles and collecting prizes.
 */
public class EscapeRoom
{

      // describe the game with brief welcome message
      // determine the size (length and width) a player must move to stay within the grid markings
      // Allow game commands:
      //    right, left, up, down: if you try to go off grid or bump into wall, score decreases
      //    jump over 1 space: you cannot jump over walls
      //    if you land on a trap, spring a trap to increase score: you must first check if there is a trap, if none exists, penalty
      //    pick up prize: score increases, if there is no prize, penalty
      //    help: display all possible commands
      //    end: reach the far right wall, score increase, game ends, if game ended without reaching far right wall, penalty
      //    replay: shows number of player steps and resets the board, you or another player can play the same board
      // Note that you must adjust the score with any method that returns a score
      // Optional: create a custom image for your player use the file player.png on disk

  private static final int SPACE_SIZE = 60;
  private static int score = 0;
  private static GameGUI game;
  private static boolean play = true;

  public static void main(String[] args) 
  {      
    // welcome message
    System.out.println("Welcome to EscapeRoom!");
    System.out.println("Get to the other side of the room, avoiding walls and invisible traps,");
    System.out.println("pick up all the prizes.\n");
    
    game = new GameGUI();
    game.createBoard();
    
    // Set up command handler
    game.setCommandHandler(new GameGUI.CommandHandler() {
      public void handleCommand(String command) {
        processCommand(command);
      }
    });
    
    // Update initial score
    game.updateScore(score);
    
    // Keep the program running
    while (play) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        break;
      }
    }
  }
  
  /**
   * Process a user command and update the game state
   */
  private static void processCommand(String command) {
    String[] validCommands = { "right", "left", "up", "down", "r", "l", "u", "d",
        "jr", "jl", "ju", "jd", "jumpright", "jumpleft", "jumpup", "jumpdown",
        "pickup", "p", "quit", "q", "replay", "help", "?", "spring", "s"};
    
    boolean isValid = false;
    for (String valid : validCommands) {
      if (command.equals(valid)) {
        isValid = true;
        break;
      }
    }
    
    if (!isValid) {
      System.out.println("Invalid command: " + command);
      score -= 2; // Small penalty for invalid command
      game.updateScore(score);
      return;
    }
    
    // Handle movement commands
    if (command.equals("right") || command.equals("r")) {
      score += game.movePlayer(SPACE_SIZE, 0);
    }
    else if (command.equals("left") || command.equals("l")) {
      score += game.movePlayer(-SPACE_SIZE, 0);
    }
    else if (command.equals("up") || command.equals("u")) {
      score += game.movePlayer(0, -SPACE_SIZE);
    }
    else if (command.equals("down") || command.equals("d")) {
      score += game.movePlayer(0, SPACE_SIZE);
    }
    // Handle jump commands
    else if (command.equals("jr") || command.equals("jumpright")) {
      score += game.jumpPlayer(2 * SPACE_SIZE, 0);
    }
    else if (command.equals("jl") || command.equals("jumpleft")) {
      score += game.jumpPlayer(-2 * SPACE_SIZE, 0);
    }
    else if (command.equals("ju") || command.equals("jumpup")) {
      score += game.jumpPlayer(0, -2 * SPACE_SIZE);
    }
    else if (command.equals("jd") || command.equals("jumpdown")) {
      score += game.jumpPlayer(0, 2 * SPACE_SIZE);
    }
    // Handle pickup command
    else if (command.equals("pickup") || command.equals("p")) {
      score += game.pickupPrize();
    }
    // Handle spring trap command
    else if (command.equals("spring") || command.equals("s")) {
      // Check for trap in each direction
      boolean trapFound = false;
      if (game.isTrap(SPACE_SIZE, 0)) {
        score += game.springTrap(SPACE_SIZE, 0);
        trapFound = true;
      } else if (game.isTrap(-SPACE_SIZE, 0)) {
        score += game.springTrap(-SPACE_SIZE, 0);
        trapFound = true;
      } else if (game.isTrap(0, SPACE_SIZE)) {
        score += game.springTrap(0, SPACE_SIZE);
        trapFound = true;
      } else if (game.isTrap(0, -SPACE_SIZE)) {
        score += game.springTrap(0, -SPACE_SIZE);
        trapFound = true;
      }
      if (!trapFound) {
        // No trap adjacent, penalty
        score += game.springTrap(SPACE_SIZE, 0); // This will return penalty
      }
    }
    // Handle help command
    else if (command.equals("help") || command.equals("?")) {
      printHelp();
    }
    // Handle replay command
    else if (command.equals("replay")) {
      System.out.println("Player steps: " + game.getSteps());
      int replayBonus = game.replay();
      score = 0 + replayBonus; // Reset score to 0, then add replay bonus/penalty
      game.updateScore(score);
    }
    // Handle quit command
    else if (command.equals("quit") || command.equals("q")) {
      score += game.endGame();
      System.out.println("Final score: " + score);
      System.out.println("Total steps: " + game.getSteps());
      play = false;
      System.exit(0);
    }
    
    // Update score display
    game.updateScore(score);
  }
  
  /**
   * Print help message with all valid commands
   */
  private static void printHelp() {
    System.out.println("\n=== ESCAPE ROOM COMMANDS ===");
    System.out.println("Movement:");
    System.out.println("  right, r    - Move right one space");
    System.out.println("  left, l     - Move left one space");
    System.out.println("  up, u       - Move up one space");
    System.out.println("  down, d     - Move down one space");
    System.out.println("\nJump (2 spaces):");
    System.out.println("  jr, jumpright  - Jump right two spaces");
    System.out.println("  jl, jumpleft   - Jump left two spaces");
    System.out.println("  ju, jumpup     - Jump up two spaces");
    System.out.println("  jd, jumpdown   - Jump down two spaces");
    System.out.println("\nActions:");
    System.out.println("  pickup, p   - Pick up a prize at current location");
    System.out.println("  spring, s   - Spring a trap or barrier adjacent to player");
    System.out.println("\nNote: Rocks can be jumped over. Barriers block jumps but can be sprung.");
    System.out.println("\nOther:");
    System.out.println("  help, ?     - Show this help message");
    System.out.println("  replay     - Reset the board (shows steps)");
    System.out.println("  quit, q    - End the game");
    System.out.println("============================\n");
  }
}

        