public class EscapeRoom
{

  private static final int SPACE_SIZE = 60;
  private static int score = 0;
  private static int points = 0;
  private static GameGUI game;
  private static boolean play = true;
  private static String playerHouse = "";
  private static java.util.Scanner scanner = new java.util.Scanner(System.in);

  public static void main(String[] args) 
  {      
    System.out.println("-----------------------------------------------------------");
    System.out.println(" Welcome to Hogwarts: Escape from Snape's Dungeon!");
    System.out.println("-----------------------------------------------------------");
    System.out.println("\nYou've been caught by Professor Snape and locked in his dungeon!");
    System.out.println("Collect all Amortentia ingredients to escape!");
    System.out.println("But beware - Snape has set traps throughout the dungeon...\n");
    
    selectHouse();
    
    game = new GameGUI(playerHouse);
    game.createBoard();
    game.updateScore(score);
    
    while (play) {
      System.out.print("> ");
      String command = scanner.nextLine().trim().toLowerCase();
      if (!command.isEmpty()) {
        processCommand(command);
      }
    }
  }
  
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
      score -= 2;
      game.updateScore(score);
      return;
    }
    
    if (game.isGameOver()) {
      if (command.equals("quit") || command.equals("q")) {
        System.out.println("Final points: " + points);
        System.out.println("Total steps: " + game.getSteps());
        play = false;
        System.exit(0);
      } else if (command.equals("replay")) {
        System.out.println("Player steps: " + game.getSteps());
        int replayBonus = game.replay();
        score = 0 + replayBonus;
        points = 0;
        game.updateScore(score);
        game.resetGameOver();
        System.out.println("Game reset! Starting new game...");
        return;
      } else {
        System.out.println("GAME OVER! Snape has caught you!");
        System.out.println("You can quit (q) or replay (replay)");
        return;
      }
    }
    
    if (command.equals("right") || command.equals("r")) {
      int result = game.movePlayer(SPACE_SIZE, 0);
      score += result;
      score++;
      if (result == 0) {
        points++;
        System.out.println("Points: " + points);
      }
    }
    else if (command.equals("left") || command.equals("l")) {
      int result = game.movePlayer(-SPACE_SIZE, 0);
      score += result;
      score++;
      if (result == 0) {
        points++;
        System.out.println("Points: " + points);
      }
    }
    else if (command.equals("up") || command.equals("u")) {
      int result = game.movePlayer(0, -SPACE_SIZE);
      score += result;
      score++;
      if (result == 0) {
        points++;
        System.out.println("Points: " + points);
      }
    }
    else if (command.equals("down") || command.equals("d")) {
      int result = game.movePlayer(0, SPACE_SIZE);
      score += result;
      score++;
      if (result == 0) {
        points++;
        System.out.println("Points: " + points);
      }
    }
    else if (command.equals("jr") || command.equals("jumpright")) {
      int result = game.jumpPlayer(2 * SPACE_SIZE, 0);
      score += result;
      if (result == -1000) {
        return;
      }
      score++;
      if (result == 0) {
        points++;
        System.out.println("Points: " + points);
      }
    }
    else if (command.equals("jl") || command.equals("jumpleft")) {
      int result = game.jumpPlayer(-2 * SPACE_SIZE, 0);
      score += result;
      if (result == -1000) {
        return;
      }
      score++;
      if (result == 0) {
        points++;
        System.out.println("Points: " + points);
      }
    }
    else if (command.equals("ju") || command.equals("jumpup")) {
      int result = game.jumpPlayer(0, -2 * SPACE_SIZE);
      score += result;
      if (result == -1000) {
        return;
      }
      score++;
      if (result == 0) {
        points++;
        System.out.println("Points: " + points);
      }
    }
    else if (command.equals("jd") || command.equals("jumpdown")) {
      int result = game.jumpPlayer(0, 2 * SPACE_SIZE);
      score += result;
      if (result == -1000) {
        return;
      }
      score++;
      if (result == 0) {
        points++;
        System.out.println("Points: " + points);
      }
    }
    else if (command.equals("pickup") || command.equals("p")) { 
      int result = game.pickupPrize();
      score += result;
      
      if (game.getCoinsCollected() == game.getTotalCoins()) {
        System.out.println("-----------------------------------------------------------");
        System.out.println("All Amortentia potion ingredients collected!");
        System.out.println("Congratulations! You escaped Snape's dungeon!");
        System.out.println(playerHouse + " has triumphed once again!");
        System.out.println("Final score: " + score);
        System.out.println("Final points: " + points);
        System.out.println("Total steps: " + game.getSteps());
        System.out.println("-----------------------------------------------------------");
        play = false;
        System.exit(0);
      }
    }
    else if (command.equals("spring") || command.equals("s")) {
      score += game.springAdjacentTraps();
    }
    else if (command.equals("help") || command.equals("?")) {
      printHelp();
    }
    else if (command.equals("replay")) {
      System.out.println("Player score: " + score);
      System.out.println("Player steps: " + game.getSteps());
      int replayBonus = game.replay();
      score = replayBonus;
      points = 0;
      game.updateScore(score);
      game.resetGameOver();
      System.out.println("The dungeon has been reset! Starting new game...");
    }
    else if (command.equals("quit") || command.equals("q")) {
      score += game.endGame();
      System.out.println("\nYou have left Snape's dungeon.");
      System.out.println("Final score: " + score);
      System.out.println("Final points: " + points);
      System.out.println("Total steps: " + game.getSteps());
      System.out.println("Until we meet again, " + playerHouse + "!\n");
      play = false;
      System.exit(0);
    }
    
    game.updateScore(score);
  }
  
  private static void selectHouse() {
    System.out.println("Which Hogwarts house are you from?");
    System.out.println("1. Gryffindor (Gryffindor.png)");
    System.out.println("2. Hufflepuff (Hufflepuff.png)");
    System.out.println("3. Ravenclaw (Ravenclaw.png)");
    System.out.println("4. Slytherin (Slytherin.png)");
    System.out.print("Enter your choice (1-4): ");
    
    String choice = scanner.nextLine().trim();
    
    switch(choice) {
      case "1":
        playerHouse = "Gryffindor";
        System.out.println("\nBrave Gryffindor! May your courage guide you through Snape's dungeon!");
        break;
      case "2":
        playerHouse = "Hufflepuff";
        System.out.println("\nLoyal Hufflepuff! Your determination will help you escape!");
        break;
      case "3":
        playerHouse = "Ravenclaw";
        System.out.println("\nWise Ravenclaw! Use your wit to outsmart Snape's traps!");
        break;
      case "4":
        playerHouse = "Slytherin";
        System.out.println("\nCunning Slytherin! Your ambition will lead you to freedom!");
        break;
      default:
        playerHouse = "Gryffindor";
        System.out.println("\nInvalid choice. Defaulting to Gryffindor!");
        break;
    }
    System.out.println("Your house crest will be displayed as your player character.\n");
  }
  
  public static String getPlayerHouse() {
    return playerHouse;
  }
  
  private static void printHelp() {
    System.out.println("\n-----------------------------------------------------------");
    System.out.println(" HOGWARTS DUNGEON ESCAPE - COMMANDS");
    System.out.println("-----------------------------------------------------------");
    System.out.println("\nMovement:");
    System.out.println("  right, r    - Move right one space (gains 1 point)");
    System.out.println("  left, l     - Move left one space (gains 1 point)");
    System.out.println("  up, u       - Move up one space (gains 1 point)");
    System.out.println("  down, d     - Move down one space (gains 1 point)");
    System.out.println("\nJump (2 spaces):");
    System.out.println("  jr, jumpright  - Jump right two spaces (gains 1 point)");
    System.out.println("  jl, jumpleft   - Jump left two spaces (gains 1 point)");
    System.out.println("  ju, jumpup     - Jump up two spaces (gains 1 point)");
    System.out.println("  jd, jumpdown   - Jump down two spaces (gains 1 point)");
    System.out.println("\nActions:");
    System.out.println("  pickup, p   - Pick up a Amortentia Potion ingredient");
    System.out.println("  spring, s   - Disarm a trap or barrier adjacent to you");
    System.out.println("\nGame Rules:");
    System.out.println("  - You gain 1 point each time you move successfully");
    System.out.println("  - Collect all potion ingredients to escape");
    System.out.println("  - Cannot move into Snape's traps, but jumping into one ends the game!");
    System.out.println("  - Hidden traps show 'ANOMALY DETECTED NEARBY' when nearby");
    System.out.println("  - Rocks can be jumped over. Barriers block jumps but can be disarmed.");
    System.out.println("\nOther:");
    System.out.println("  help, ?     - Show this help message");
    System.out.println("  replay     - Reset the dungeon (shows steps)");
    System.out.println("  quit, q    - End the game");
    System.out.println("-----------------------------------------------------------\n");
  }
}

        