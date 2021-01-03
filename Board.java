package TetrisGame;

import java.awt.BasicStroke;
import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;


@SuppressWarnings("unused")
public class Board extends JPanel implements KeyListener, MouseListener, MouseMotionListener{

    //Assets

    /**
     *
     */
	private final int LINES_PER_LEVEL=10;
    private static final long serialVersionUID = 1L;
    private int topscore=0;
    private int level=0;
    private int lines;
    private Clip music;
    private BufferedImage blocks, background, pause, refresh,refresh2,exit,exit2;

    //board dimensions (the playing area)

    private final int boardHeight = 20, boardWidth = 10;

    // block size
	private final int blockSize = 30;

    // field

    private int[][] board = new int[boardHeight][boardWidth];

    // array with all the possible shapes

    private Shape[] shapes = new Shape[7];

    // currentShape

    private static Shape currentShape, nextShape;

    // game loop

    private Timer looper;

    private int FPS = 60;

    private int delay = 1000/FPS;

    // mouse events variables

    private int mouseX, mouseY,MOUSEX,MOUSEY;

    private boolean leftClick = false;

    private Rectangle stopBounds, refreshBounds1,refreshBounds2,exitBound,exitBound2;

    private boolean gamePaused = false;
    private boolean gameStop=true;

    private boolean gameOver = true;
    final static int scoreX=400;
    final static int scoreY=330;

    // buttons press lapse

    private Timer buttonLapse = new Timer(300, new ActionListener(){

        @Override
        public void actionPerformed(ActionEvent e) {
            buttonLapse.stop();
        }});

    // score

    private int score = 0;
    public Board(){
        // load Assets
        blocks = ImageLoader.loadImage("/tiles.png"); 
        background = ImageLoader.loadImage("/background.png");
        pause = ImageLoader.loadImage("/pause.png");
        refresh = ImageLoader.loadImage("/refresh.png");
        refresh2 = ImageLoader.loadImage("/refresh2.png");
        music = ImageLoader.LoadSound("/music.wav");
        exit = ImageLoader.loadImage("/exit.png");
        exit2 = ImageLoader.loadImage("/exit2.png");

        music.loop(Clip.LOOP_CONTINUOUSLY);



        mouseX = 0;
        mouseY = 0;
        MOUSEX=0;
        MOUSEY=0;

        stopBounds = new Rectangle(330, 470, pause.getWidth(), pause.getHeight() + pause.getHeight()/2);
        refreshBounds1 = new Rectangle(160, 500 - refresh.getHeight() - 200,refresh.getWidth(),
                refresh.getHeight() + refresh.getHeight()/2);
        refreshBounds2 = new Rectangle(160, 500 - refresh2.getHeight() - 200,refresh2.getWidth(),
                refresh2.getHeight() + refresh2.getHeight()/2);
        exitBound = new Rectangle(160, 500 - exit.getHeight() - 150,exit.getWidth(),
                exit.getHeight() + exit.getHeight()/2);
        exitBound2 = new Rectangle(160, 500 - exit2.getHeight() - 150,exit2.getWidth(),
                exit2.getHeight() + exit2.getHeight()/2);
        // create game looper

        looper = new Timer(delay, new GameLooper());

        // create shapes

        shapes[0] = new Shape(new int[][]{
                {1, 1, 1, 1}   // I shape;
        }, blocks.getSubimage(0, 0, blockSize, blockSize), this, 1);

        shapes[1] = new Shape(new int[][]{
                {1, 1, 1},
                {0, 1, 0},   // T shape;
        }, blocks.getSubimage(blockSize, 0, blockSize, blockSize), this, 2);

        shapes[2] = new Shape(new int[][]{
                {1, 1, 1},
                {1, 0, 0},   // L shape;
        }, blocks.getSubimage(blockSize*2, 0, blockSize, blockSize), this, 3);

        shapes[3] = new Shape(new int[][]{
                {1, 1, 1},
                {0, 0, 1},   // J shape;
        }, blocks.getSubimage(blockSize*3, 0, blockSize, blockSize), this, 4);

        shapes[4] = new Shape(new int[][]{
                {0, 1, 1},
                {1, 1, 0},   // S shape;
        }, blocks.getSubimage(blockSize*4, 0, blockSize, blockSize), this, 5);

        shapes[5] = new Shape(new int[][]{
                {1, 1, 0},
                {0, 1, 1},   // Z shape;
        }, blocks.getSubimage(blockSize*5, 0, blockSize, blockSize), this, 6);

        shapes[6] = new Shape(new int[][]{
                {1, 1},
                {1, 1},   // O shape;
        }, blocks.getSubimage(blockSize*6, 0, blockSize, blockSize), this, 7);


    }
    private void update(){
        if(stopBounds.contains(mouseX, mouseY) && leftClick && !buttonLapse.isRunning() && !gameOver)
        {
            buttonLapse.start();
            gamePaused = !gamePaused;
        }

        if(refreshBounds1.contains(mouseX, mouseY) && leftClick)
        {
            startGame();
            level=0;
        }

        if(refreshBounds2.contains(mouseX, mouseY) && leftClick)
        {
            startGame();
            level=0;
        }
        if(exitBound.contains(MOUSEX, MOUSEY) && leftClick)
        {
            System.exit(0);
        }
        if(exitBound2.contains(MOUSEX, MOUSEY) && leftClick)
        {
            System.exit(0);
        }
        
        if(gamePaused || gameOver)
        {
            return;
        }
        currentShape.update();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);

        g.drawImage(background, 0, 0, null);


        for(int row = 0; row < board.length; row++)
        {
            for(int col = 0; col < board[row].length; col ++)
            {

                if(board[row][col] != 0)
                {

                    g.drawImage(blocks.getSubimage((board[row][col] - 1)*blockSize,
                            0, blockSize, blockSize), col*blockSize, row*blockSize, null);
                }

            }
        }
        for(int row = 0; row < nextShape.getCoords().length; row ++)
        {
            for(int col = 0; col < nextShape.getCoords()[0].length; col ++)
            {
                if(nextShape.getCoords()[row][col] != 0)
                {
                    g.drawImage(nextShape.getBlock(), col*30 + 320, row*30 + 50, null);
                }
            }
        }
        currentShape.render(g);

        if(stopBounds.contains(mouseX, mouseY))
            g.drawImage(pause.getScaledInstance(pause.getWidth() + 3, pause.getHeight() + 3, BufferedImage.SCALE_DEFAULT)
                    , stopBounds.x + 3, stopBounds.y + 3, null);
        else
            g.drawImage(pause, stopBounds.x, stopBounds.y, null);


        if(gamePaused)
        {
            String gamePausedString = "GAME PAUSED";
            g.setColor(Color.WHITE);
            g.setFont(new Font("Georgia", Font.BOLD, 30));
            g.drawString(gamePausedString, 35, Window.HEIGHT/2);
        }
      
        if(level <3 && score >10)
        {
     	  level++;
          nextLevel();
        }
        else if(level==3 && score >10)
        {
        	g.setColor(Color.WHITE);
     		g.fillRoundRect(15, 80, 280, 275, 20, 20);//border of all end game things
     		g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.45f));
     		g.fillRoundRect(40, 120, 225, 50, 20, 20); //border of End Game
     		g.fillRoundRect(45, 214, 100, 30, 20, 20); //border of score
     		g.fillRoundRect(160, 214, 130, 30, 20, 20); //border of best
     		g.fillRoundRect(45, 260, 100,30,20,20); // border of playagain
     		g.fillRoundRect(45, 310, 100,30,20,20); // border of exit
     		g.setColor(Color.BLACK);
     		g.setFont(new Font("TimesRoman", Font.BOLD, 43)); 
     		g.drawString("Game Over", 48, 158);
     		g.setFont(new Font("Monospaced", Font.BOLD, 15)); 
     		g.drawString("Play Again", 50, 280);
     		if(refreshBounds2.contains(mouseX, mouseY))
            g.drawImage(refresh2.getScaledInstance(refresh2.getWidth() + 3, refresh2.getHeight() + 3,
                    BufferedImage.SCALE_DEFAULT), refreshBounds2.x + 3, refreshBounds2.y + 3, null);
            else
            g.drawImage(refresh2, refreshBounds2.x, refreshBounds2.y, null);

     		g.setFont(new Font("Monospaced", Font.BOLD, 15)); 
     		g.drawString("Score:",50, 234);
     		g.drawString(""+score, 104, 234);
     		g.drawString("BestScore:", 165, 234);
     		 try {
     			BufferedReader br= new BufferedReader(new FileReader("topscore.txt"));
     			 g.drawString(br.readLine()+"", 260, 234);
     		} catch (FileNotFoundException e) {
     			e.printStackTrace();
     		} catch (IOException e) {
				e.printStackTrace();
			}
     		g.drawString("Exit",70,330);
     		if(exitBound2.contains(MOUSEX, MOUSEY))
                g.drawImage(exit2.getScaledInstance(exit2.getWidth() + 3, exit2.getHeight() + 3,
                        BufferedImage.SCALE_DEFAULT), exitBound2.x + 3, exitBound2.y + 3, null);
                else
                g.drawImage(exit2, exitBound2.x, exitBound2.y, null);
     	    gamePaused=true;
        }
        if(gameOver)
        {
        	SaveTopScore();
        	g.setColor(Color.WHITE);
     		g.fillRoundRect(15, 80, 280, 275, 20, 20);//border of all end game things
     		g.setColor(new Color(0.5f, 0.5f, 0.5f, 0.45f));
     		g.fillRoundRect(40, 120, 225, 50, 20, 20); //border of End Game
     		g.fillRoundRect(45, 214, 100, 30, 20, 20); //border of score
     		g.fillRoundRect(160, 214, 130, 30, 20, 20); //border of best
     		g.fillRoundRect(45, 260, 100,30,20,20); // border of playagain
     		g.fillRoundRect(45, 310, 100,30,20,20); // border of exit
     		g.setColor(Color.BLACK);
     		g.setFont(new Font("TimesRoman", Font.BOLD, 43)); 
     		g.drawString("Game Over", 48, 158);
     		g.setFont(new Font("Monospaced", Font.BOLD, 15));
     		g.drawString("Play Again", 50, 280);
     		if(refreshBounds1.contains(mouseX, mouseY))
            g.drawImage(refresh.getScaledInstance(refresh.getWidth() + 3, refresh.getHeight() + 3,
                    BufferedImage.SCALE_DEFAULT), refreshBounds1.x + 3, refreshBounds1.y + 3, null);
            else
            g.drawImage(refresh, refreshBounds1.x, refreshBounds1.y, null);

     		g.setFont(new Font("Monospaced", Font.BOLD, 15)); 
     		g.drawString("Score:",50, 234);
     		g.drawString(""+score, 104, 234);
     		g.drawString("BestScore:", 165, 234);
     		 try {
     			BufferedReader br= new BufferedReader(new FileReader("topscore.txt"));
     			 g.drawString(br.readLine()+"", 260, 234);
     		} catch (FileNotFoundException e) {
     			e.printStackTrace();
     		} catch (IOException e) {
				e.printStackTrace();
			}
     		g.drawString("Exit",70,330);
     		if(exitBound.contains(MOUSEX, MOUSEY))
                g.drawImage(exit.getScaledInstance(exit.getWidth() + 3, exit.getHeight() + 3,
                        BufferedImage.SCALE_DEFAULT), exitBound.x + 3, exitBound.y + 3, null);
                else
                g.drawImage(exit, exitBound.x, exitBound.y, null);
        }
        
        g.setColor(Color.WHITE);

       
        
        g.setFont(new Font("Serif", Font.BOLD, 16));
        g.drawString("SCORE", Window.WIDTH - 130, Window.HEIGHT/2);
        g.drawString("BESTSCORE:", Window.WIDTH -130, Window.HEIGHT/2+40);
        g.drawString(score+"", Window.WIDTH - 130, Window.HEIGHT/2 + 20);
        try {
			BufferedReader br= new BufferedReader(new FileReader("topscore.txt"));
			 try {
				g.drawString(br.readLine()+"", Window.WIDTH - 130, Window.HEIGHT/2 + 70);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

        g.drawString("LEVEL:", Window.WIDTH -130, Window.HEIGHT/2+100);
        g.drawString(level+"", Window.WIDTH - 130, Window.HEIGHT/2+120);
        g.drawString("PAUSE:", Window.WIDTH -130, Window.HEIGHT/2+150);
        Graphics2D g2d = (Graphics2D)g;

        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(0, 0, 0, 100));

        for(int i = 0; i <= boardHeight; i++)
        {
            g2d.drawLine(0, i*blockSize, boardWidth*blockSize, i*blockSize);
        }
        for(int j = 0; j <= boardWidth; j++)
        {
            g2d.drawLine(j*blockSize, 0, j*blockSize, boardHeight*30);
        }
    }
    
    // check iif the row if full=> add scores. 
    public void checkLine(){
        int size = board.length - 1;

        for(int i = board.length - 1; i > 0; i--)
        {
            int count = 0;
            for(int j = 0; j < board[0].length; j++)
            {
                if(board[i][j] != 0)
                {
                    count++;
                }
                if(count==10)
                {
                   addScore();
                }
                
                board[size][j] = board[i][j];
            }
            if(count < board[0].length)
            {
                size--;
            }
        }
    }
 
    
  
    // getter for level variable 
    public int getLevel() {
		return level;
	}
	private String format(String string, int topscore2) {
        return null;
    }

    public void setNextShape(){
        int index = (int)(Math.random()*shapes.length);
        nextShape = new Shape(shapes[index].getCoords(), shapes[index].getBlock(), this, shapes[index].getColor());
    }

    public void setCurrentShape(){
        currentShape = nextShape;
        setNextShape();

        for(int row = 0; row < currentShape.getCoords().length; row ++)
        {
            for(int col = 0; col < currentShape.getCoords()[0].length; col ++)
            {
                if(currentShape.getCoords()[row][col] != 0)
                {
                    if(board[currentShape.getY() + row][currentShape.getX() + col] != 0)
                        gameOver = true;
                }
            }
        }
    }
    public int[][] getBoard(){
        return board;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_UP)
            currentShape.rotateShape();
        if(e.getKeyCode() == KeyEvent.VK_RIGHT)
            currentShape.setDeltaX(1);
        if(e.getKeyCode() == KeyEvent.VK_LEFT)
            currentShape.setDeltaX(-1);
        if(e.getKeyCode() == KeyEvent.VK_DOWN)
            currentShape.speedUp();
    }
    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_DOWN)
            currentShape.speedDown();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
 
    public void SaveTopScore() {
    	if(topscore < score) // level 0 with topscore = 0 
    	{
    		topscore=score;
    		try {
				BufferedWriter bw= new BufferedWriter(new FileWriter("topscore.txt"));
				bw.write(topscore+"");
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	else 
    	{ // level 1,2,3 
    		topscore+=score;
    		try {
				BufferedWriter bw= new BufferedWriter(new FileWriter("topscore.txt"));
				bw.write(topscore+"");
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    public void startGame(){
        stopGame();
        setNextShape();
        setCurrentShape();
        gameOver = false;
        looper.start();
    }
    
    public void nextLevel() {
    	SaveTopScore();
    	startGame();
    }
    
    public void stopGame(){
        score=0;
        for(int row = 0; row < board.length; row++)
        {
            for(int col = 0; col < board[row].length; col ++)
            {
                board[row][col] = 0;
            }
        }
        looper.stop();
    }

    class GameLooper implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            update();
            repaint();
        }

    }


    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        MOUSEX = e.getX();
        MOUSEY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        MOUSEX = e.getX();
        MOUSEY = e.getY();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }
    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1)
            leftClick = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1)
            leftClick = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
    public int addScore(){
        return score+=10;
    }
}
