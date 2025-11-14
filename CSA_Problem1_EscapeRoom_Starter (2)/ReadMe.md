# Hogwarts: Snape's Love Potion Adventure

Player navigates a dungeon grid to collect Amortentia potion ingredients while avoiding traps.

## Rubric Compliance

### Program Purpose & Design
- **Theme**: Harry Potter escape room from Snape's dungeon
- **House Selection**: Player chooses Hogwarts house (displayed as player png)

### User Interface
- **Display**: 8x5 grid with visual obstacles and player character
- **Input Method**: Terminal-based Scanner input (not GUI-based)
- **Output**: Real-time score, points, and game status

### Program Logic & Constructs

**Movement System:**
```java
// Regular movement (1 space)
game.movePlayer(SPACE_SIZE, 0);  // right
game.movePlayer(-SPACE_SIZE, 0); // left

// Jump movement (2 spaces)
game.jumpPlayer(2 * SPACE_SIZE, 0);
```

**Collision Detection:**
```java
// Center-point overlap detection
int playerCenterX = x + 20;
int playerCenterY = y + 20;
Rectangle playerRect = new Rectangle(playerCenterX, playerCenterY, 20, 20);
```

**Game State Management:**
- Score tracking (starts at 0, updates per action)
- Points system (1 point per successful move)
- Game over condition (trap collision or prize collection)
- Replay functionality with bonus calculation

---

## Key Code Snippets

**Command Processing:**
```java
if (command.equals("right") || command.equals("r")) {
  int result = game.movePlayer(SPACE_SIZE, 0);
  score += result;
  score++;
  if (result == 0) {
    points++;
  }
}
```

**Win Condition:**
```java
if (game.getCoinsCollected() == game.getTotalCoins()) {
  System.out.println("All Amortentia potion ingredients collected!");
  System.out.println("Congratulations! You escaped Snape's dungeon!");
}
```

---

## Milestones

| Date | Milestone |
|------|-----------|
| 11/12 | ✅ Completed terminal-based controls (movement, jumping, actions) |
| 11/13 | ✅ Fixed GUI rendering and implemented story theme with house-based player icons |

---

## Valid Commands

**Movement**: `right/r`, `left/l`, `up/u`, `down/d`  
**Jumping**: `jr/jumpright`, `jl/jumpleft`, `ju/jumpup`, `jd/jumpdown`  
**Actions**: `pickup/p`, `spring/s`  
**Game**: `help/?`, `replay`, `quit/q`

---

## Score System
- Successful movement: +1 point
- Prize pickup: +10 points
- Trap trigger: +5 points (penalty)
- Invalid command: -2 points
- Game completion: Final score displayed
