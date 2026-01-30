import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {

    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U';
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;

            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char previousDirection = this.direction;
            this.direction = direction;
            updateVelocity();

            int nextX = this.x + this.velocityX;
            int nextY = this.y + this.velocityY;

            for (Block wall : walls) {
                if (collision(nextX, nextY, this.width, this.height, wall)) {
                    this.direction = previousDirection;
                    updateVelocity();
                    return;
                }
            }

            this.x = nextX;
            this.y = nextY;
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize / 4;
            } else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize / 4;
            } else if (this.direction == 'L') {
                this.velocityX = -tileSize / 4;
                this.velocityY = 0;
            } else if (this.direction == 'R') {
                this.velocityX = tileSize / 4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = { 'U', 'D', 'L', 'R' };
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();
        initGhostDirections();

        gameLoop = new Timer(50, this);
        gameLoop.start();
    }

    private void initGhostDirections() {
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.direction = newDirection;
            ghost.updateVelocity();
        }
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();
        pacman = null;

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileType = row.charAt(c);

                int x = c * tileSize;
                int y = r * tileSize;

                if (tileType == 'X') {
                    walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                } else if (tileType == 'b') {
                    ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize));
                } else if (tileType == 'o') {
                    ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize));
                } else if (tileType == 'p') {
                    ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize));
                } else if (tileType == 'r') {
                    ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize));
                } else if (tileType == 'P') {
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                } else if (tileType == ' ') {
                    foods.add(new Block(null, x + 14, y + 14, 4, 4));
                }
            }
        }

        if (pacman == null) {
            pacman = new Block(pacmanRightImage, tileSize, tileSize, tileSize, tileSize);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillOval(food.x, food.y, food.width, food.height);
        }

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        if (pacman != null) {
            g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over! Final Score: " + score, tileSize / 2, tileSize / 2);
        } else {
            g.drawString("x" + lives + "  Score: " + score, tileSize / 2, tileSize / 2);
        }
    }

    public void move() {
        if (gameOver || pacman == null) return;

        int nextPacX = pacman.x + pacman.velocityX;
        int nextPacY = pacman.y + pacman.velocityY;

        boolean hitWall = false;
        for (Block wall : walls) {
            if (collision(nextPacX, nextPacY, pacman.width, pacman.height, wall)) {
                hitWall = true;
                break;
            }
        }
        if (!hitWall) {
            pacman.x = nextPacX;
            pacman.y = nextPacY;
        }

        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives <= 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }

            if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;

                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }

        Iterator<Block> it = foods.iterator();
        while (it.hasNext()) {
            Block food = it.next();
            if (collision(pacman, food)) {
                it.remove();
                score += 10;
                break;
            }
        }

        if (foods.isEmpty()) {
            loadMap();
            initGhostDirections();
            resetPositions();
        }
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    public boolean collision(int ax, int ay, int aw, int ah, Block b) {
        return ax < b.x + b.width &&
               ax + aw > b.x &&
               ay < b.y + b.height &&
               ay + ah > b.y;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;

        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            initGhostDirections();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }

        if (pacman == null || gameOver) return;

        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.direction = 'U';
            pacman.updateVelocity();
            pacman.image = pacmanUpImage;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.direction = 'D';
            pacman.updateVelocity();
            pacman.image = pacmanDownImage;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.direction = 'L';
            pacman.updateVelocity();
            pacman.image = pacmanLeftImage;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.direction = 'R';
            pacman.updateVelocity();
            pacman.image = pacmanRightImage;
        }
    }
}