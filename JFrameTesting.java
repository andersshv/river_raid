import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.Map;
import java.util.HashMap;

import java.awt.Component;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.Canvas;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Color;

public class JFrameTesting extends JFrame implements KeyListener, Runnable {
	
	/*************** MAIN **************/	
	public static void main(String[] args){	
		SwingUtilities.invokeLater(new Runnable() {				
			public void run() {
				new JFrameTesting();
			}
		});
	}
	
	/************** KEYLISTENER *************/	
	private boolean left;
	private boolean right;
	private boolean up;
	private boolean down;	
	private boolean escape;	
	private boolean space;
	private boolean reset;
	
	public void keyPressed(KeyEvent e) {		
		int keyCode = e.getKeyCode();
		if(keyCode == KeyEvent.VK_ESCAPE) {	
			escape = true;
		}
		if(keyCode == KeyEvent.VK_LEFT) {			
			left = true;
		}
		if(keyCode == KeyEvent.VK_RIGHT) {			
			right = true;
		}
		if(keyCode == KeyEvent.VK_UP) {			
			up = true;
		}
		if(keyCode == KeyEvent.VK_DOWN) {			
			down = true;
		}
		if(keyCode == KeyEvent.VK_SPACE) {			
			space = true;
		}		
		if(escape && keyCode == KeyEvent.VK_Y) {
			dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
		if(escape && keyCode == KeyEvent.VK_N) {
			escape = false;
		}
		if(keyCode == KeyEvent.VK_R) {
			reset = true;
		}
	}
	
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if(keyCode == KeyEvent.VK_LEFT) {			
			left = false;
		}
		if(keyCode == KeyEvent.VK_RIGHT) {			
			right = false;
		}
		if(keyCode == KeyEvent.VK_UP) {			
			up = false;
		}
		if(keyCode == KeyEvent.VK_DOWN) {			
			down = false;
		}
		if(keyCode == KeyEvent.VK_SPACE) {			
			space = false;
		}
		if(keyCode == KeyEvent.VK_R) {
			reset = false;
		}
	}
	
	public void keyTyped(KeyEvent e) { 
		/* Not used */ 
	}	
	
	/************ FIELDS ***************/
	private int tileWidth = 32;
	
	private int screenWidth = 32;
	private int screenHeight = 24;
	private int dashboardHeight = 3; 
	private int contentHeight = screenHeight - dashboardHeight;
	
	/************ CONSTRUCTOR ****************/	
	public JFrameTesting(){
		setTitle("JFrameTesting");		
		setSize(screenWidth*tileWidth, screenHeight*tileWidth);
		setLocationRelativeTo(null); // Center jframe on screen
		setAlwaysOnTop(true); // Make the jframe stay on top of all other windows
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
		// Set up keylistener methods
		addKeyListener(this);
		
		// Call run 
		new Thread(this).start();
	}
	
	public void run() {
		setupPlane();
		setupBullet();		
		setupMap();
		setupDashboard();
		setupExitScreen();
		
		enterGameLoop();
	}
	
	/************* PLANE ******************/	
	private int planeStartPosX;
	private int planePosX;
	private int planePosY = (contentHeight*tileWidth)-tileWidth*2;
	private int planeSpeedX = 4;	
	private int planeSpeedY = 12;
	private BufferedImage straightPlane;	
	private BufferedImage leftPlane;
	private BufferedImage rightPlane;		
	private BufferedImage plane;
	private void setupPlane() {
		try {
			BufferedImage planeImage = ImageIO.read(new File("img/plane2.png"));
			straightPlane = planeImage.getSubimage(0, 0, tileWidth, tileWidth);	
			leftPlane = planeImage.getSubimage(tileWidth, 0, tileWidth, tileWidth);
			rightPlane = planeImage.getSubimage(tileWidth, tileWidth, tileWidth, tileWidth);		
			plane = straightPlane;
			planeStartPosX = (screenWidth*tileWidth)/2-tileWidth/2;
			planePosX = planeStartPosX;
		} catch (Exception e) {
			System.out.println("Error setting up plane images");
		}
	}
	
	/**************** BULLET ***************/
	boolean bulletExists = false;
	int	bulletStartPosY = planePosY;
	int bulletPosY = bulletStartPosY;
	int bulletSpeed = 25;	
	BufferedImage bulletImage;
	private void setupBullet() {
		try {
			bulletImage = ImageIO.read(new File("img/bullet.png"));
		} catch (Exception e) {
			System.out.println("Error getting bullet.png");
		}
	}
	
	/*************** MAP ******************/
	private Map<Integer, BufferedImage> tileMaps = new HashMap<>();
	private void setupMap() {
		try {
			BufferedImage fullTiles = ImageIO.read(new File("img/fullTiles.png"));
			BufferedImage fullLandscapeTile = fullTiles.getSubimage(0, 0, tileWidth, tileWidth);
			BufferedImage fullWaterTile = fullTiles.getSubimage(tileWidth, 0, tileWidth, tileWidth);
			
			BufferedImage allMapsImage = ImageIO.read(new File("img/maps.png"));
			
			int mapWidth = 32; // Pixels
			int mapHeight = 128; // Pixels
			int numberOfMapsPrRow = 7;
			int numberOfRows = 2;
			for (int i = 0; i < numberOfRows; i++) {
				for (int j = 0; j < numberOfMapsPrRow; j++) {
					BufferedImage map = 
						allMapsImage.getSubimage(j*mapWidth, i*mapHeight, mapWidth, mapHeight);
					BufferedImage tileMap = 
						new BufferedImage(mapWidth*tileWidth, mapHeight*tileWidth, BufferedImage.TYPE_INT_RGB);
					Graphics g = (Graphics) tileMap.getGraphics();
					for (int mapY = 0; mapY < mapHeight; mapY++) {
						for (int mapX = 0; mapX < mapWidth; mapX++) {
							int pixel_color_map = map.getRGB(mapX, mapY);
							if (pixel_color_map < -10) { // Color is black => add land tile to screen
								g.drawImage(fullLandscapeTile, mapX*tileWidth, mapY*tileWidth, null);
							} else { // Color is white => add water tile to screen
								g.drawImage(fullWaterTile, mapX*tileWidth, mapY*tileWidth, null);
							}
						}
					}
					int mapNumber = i * numberOfMapsPrRow + j + 1;
					tileMaps.put(mapNumber, tileMap);
				}
			}			
		} catch (Exception e) {
			System.out.println("Error in setup maps");
		}
	}
	
	/**************** DASHBOARD ***********/
	private BufferedImage dashBoardImage;
	private void setupDashboard() {
		try {
			BufferedImage fullTiles = ImageIO.read(new File("img/fullTiles.png"));
			
			BufferedImage fullGray = fullTiles.getSubimage(0, 32, tileWidth, tileWidth);
			BufferedImage grayWithTopBlack = fullTiles.getSubimage(32, 32, tileWidth, tileWidth);
			
			dashBoardImage = new BufferedImage(
				screenWidth*tileWidth, 
				dashboardHeight*tileWidth, 
				BufferedImage.TYPE_INT_RGB
			);
			Graphics g = (Graphics) dashBoardImage.getGraphics();
			
			for (int i = 0; i < screenWidth; i++) {
				g.drawImage(grayWithTopBlack, i*tileWidth, 0, null);
			}
			for (int i = 1; i < dashboardHeight; i++) {
				for (int j = 0; j < screenWidth; j++) {
					g.drawImage(fullGray, j*tileWidth, i*tileWidth, null);
				}
			}
		} catch (Exception e) {
			System.out.println("Error in setup dashboards");
		}
	}
	
	/*************** EXIT SCREEN ************/
	String exitString = "Do you want to quit the game? [Y] or [N]";
	int exitStringWidth;
	private BufferedImage exitScreenBGImage;	
	private void setupExitScreen() {		
		try {			
			BufferedImage fullTiles = ImageIO.read(new File("img/fullTiles.png"));			
			BufferedImage fullBlack = fullTiles.getSubimage(0, 64, tileWidth, tileWidth);
			
			exitScreenBGImage = new BufferedImage(
				screenWidth*tileWidth, 
				screenHeight*tileWidth, 
				BufferedImage.TYPE_INT_RGB
			);
			Graphics g = (Graphics) exitScreenBGImage.getGraphics();
			
			for (int i = 0; i < screenHeight; i++) {
				for (int j = 0; j < screenWidth; j++) {
					g.drawImage(fullBlack, j*tileWidth, i*tileWidth, null);
				}
			}			
			exitStringWidth = g.getFontMetrics().stringWidth(exitString);
		} catch (Exception e) {
			System.out.println("Error in setup maps");
		}
	}
	
	/**************** GAME LOOP ***************/
	int tick = 0;
	boolean running = true;
	private void enterGameLoop() {
		long startTime, endTime;		
		long FPS = 30;
		long timePrFrame_ms = 1000 / FPS;
		long timeSpend_ms;
		long sleepingTime;
		while(running) {			
			try {	
				startTime = System.nanoTime();
				updateModel();
				renderMainImage(); 	
				drawMainImageToFrame();
				endTime = System.nanoTime();
				timeSpend_ms = (endTime - startTime) / 1000000;
				sleepingTime = timePrFrame_ms - timeSpend_ms;
				System.out.println(sleepingTime + "______" + startTime);
				if (sleepingTime > 0) { Thread.sleep(sleepingTime); }
				if (!collision) {
					tick += 1;
				}
			} catch (Exception e) {
				running = false;
				System.out.println("Error in game loop");
				e.printStackTrace();
			}
		}
	}
	
	private boolean collision;
	private void updateModel() {
		if (reset) {
			tick = 0;
			collision = false;
			planePosX = planeStartPosX;
			bulletExists = false;
		}
		if (!collision) {
			if (left) { 
				planePosX -= planeSpeedX;
				plane = leftPlane;
			} else if (right) { 
				planePosX += planeSpeedX;
				plane = rightPlane;
			} else if (up) {
				plane = straightPlane;
			} else if (down) { 
				plane = straightPlane;
			} else {
				plane = straightPlane;
			}
		}
		
		
		if (space && !bulletExists && !collision) {
			bulletExists = true;					
		} else if (bulletExists) {
			bulletPosY -= bulletSpeed;
			if (bulletPosY - bulletSpeed <= 0) {
				bulletPosY = bulletStartPosY;
				if (!space) {
					bulletExists = false;
				}
			}
		}
		
		// Check for collisions with plane and landscape				
		BufferedImage currMap = tileMaps.get(mapNumber);
		int rgb;
		if (mapPosY > -1*contentHeight*tileWidth + 3*tileWidth) {
			// 1: Check front
			rgb = currMap.getRGB(
				planePosX+tileWidth/2, mapPosY + contentHeight*tileWidth - 2*tileWidth - planeSpeedY*2
			);
			if (rgb == -12482236) { // Green Color
				collision = true;
			}		
			// 2: Check right
			rgb = currMap.getRGB(
				planePosX+tileWidth-1, mapPosY + contentHeight*tileWidth - 2*tileWidth + tileWidth/2
			);
			if (rgb == -12482236) { // Green Color
				collision = true;
			}
			// 2: Check left
			rgb = currMap.getRGB(
				planePosX+1, mapPosY + contentHeight*tileWidth - 2*tileWidth + tileWidth/2
			);
			if (rgb == -12482236) { // Green Color
				collision = true;
			}
		}
		
	}
	
	private BufferedImage mainImage = 
		new BufferedImage(screenWidth*tileWidth, screenHeight*tileWidth, BufferedImage.TYPE_INT_RGB	);
	private Graphics mainG = (Graphics) mainImage.getGraphics();
	int mapNumber = 1;
	int mapPosY;
	private void renderMainImage() {		
		// Draw map section		
		BufferedImage tileMap = tileMaps.get(mapNumber);
		BufferedImage tileMapNext = tileMaps.get(mapNumber + 1);
		int x = 0;
		mapPosY = tileMap.getHeight() - contentHeight*tileWidth - tick*planeSpeedY;
		if (mapPosY <= 0) {
			int w = screenWidth*tileWidth;
			int h = contentHeight*tileWidth - Math.abs(mapPosY);
			if (h <= 0) {
				int yNext = tileMapNext.getHeight() - contentHeight*tileWidth;
				int hNext = contentHeight*tileWidth;
				BufferedImage contentImageNext = tileMapNext.getSubimage(0, yNext, w, hNext);
				mainG.drawImage(contentImageNext, 0, 0, null);					
				mapNumber += 1;
				tick = 0;
			} else {				
				int yNext = tileMapNext.getHeight() - contentHeight*tileWidth + h;
				int hNext = contentHeight*tileWidth - h;
				if (hNext > 0) {
					BufferedImage contentImageNext = tileMapNext.getSubimage(0, yNext, w, hNext);
					mainG.drawImage(contentImageNext, 0, 0, null);
				}
				BufferedImage contentImage = tileMap.getSubimage(x, 0, w, h);
				mainG.drawImage(contentImage, 0, Math.abs(mapPosY), null);
			}
		} else {
			int w = screenWidth*tileWidth;
			int h = contentHeight*tileWidth;		
			BufferedImage contentImage = tileMap.getSubimage(x, mapPosY, w, h);			
			mainG.drawImage(contentImage, 0, 0, null);
		}
		
		// Dashboard
		mainG.drawImage(dashBoardImage, 0, contentHeight*tileWidth, null);
		
		// Plane				
		mainG.drawImage(plane, planePosX, planePosY, null);

		// Bullet
		if (bulletExists) {
			mainG.drawImage(bulletImage, planePosX + 13, bulletPosY, null);
		}
		
		if(escape) {
			mainG.drawImage(exitScreenBGImage, 0, 0, null);	
			mainG.setColor(Color.RED);
			mainG.drawString(
				exitString, 
				(screenWidth*tileWidth)/2-exitStringWidth/2, 
				(screenHeight*tileWidth)/2
			);
		}
	}
	
	private void drawMainImageToFrame() {
		Graphics frame_g = this.getGraphics();				
		frame_g.drawImage(mainImage, 0, 0, null);
		frame_g.dispose();
	}
}