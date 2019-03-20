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
	private boolean space;

	private boolean gameStartState = true;
	private boolean gameRunningState = false;
	private boolean collisionState = false;	
	private boolean gamePausedState = false;
	private boolean exitGameState = false;
	
	public void keyPressed(KeyEvent e) {		
		int keyCode = e.getKeyCode();
		// For the arrow keys and the space key, we need to maintain when they are pressed		
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
		// For some states, the state changes when the arrow keys and/or the space key are pressed
		if(keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT || 
			 keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN || 
			 keyCode == KeyEvent.VK_SPACE) {
			if(gameStartState || gamePausedState) {
				gameStartState = false;
				gameRunningState = true;
				gamePausedState = false;
			}			
		}
	}
	
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		// For the arrow keys and the space key, we need to maintain when they are released
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
		// For some keys, we need to simply modify the state when they are released
		if(keyCode == KeyEvent.VK_R) {
			// Resetting specific values
			tick = 0;
			mapNumber = 1;
			planePosX = planeStartPosX;
			bulletExists = false;
			bulletPosY = bulletStartPosY; 
			plane = straightPlane;
			// Setting start state
			gameStartState = true;
			gameRunningState = false;
			collisionState = false;	
			gamePausedState = false;
			exitGameState = false;
		}
		// For some keys, the state changes when they are released
		if(keyCode == KeyEvent.VK_P) {
			if(gameRunningState) {
				gameRunningState = false;
				gamePausedState = true;
			} else if(gamePausedState) {
				gameRunningState = true;
				gamePausedState = false;
			}
		}
		if(keyCode == KeyEvent.VK_ESCAPE) {
			if(gameRunningState) {
				gameRunningState = false;
				gamePausedState = true;
			} 
			if(exitGameState) {
				exitGameState = false;
			} else {
				exitGameState = true;
			}
		}		
		if(keyCode == KeyEvent.VK_Y) {
			if(exitGameState) {
				dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			}			
		}
		if(keyCode == KeyEvent.VK_N) {
			exitGameState = false;
		}
	}
	
	public void keyTyped(KeyEvent e) { 
		/* Not used */ 
	}	
	
	/************ FIELDS ***************/
	private int tileWidth = 32;

	private int tilesScreenWidth = 32;
	private int tilesScreenHeight = 24;

	private int tilesDashboardHeight = 3; 
	private int tilesContentHeight = tilesScreenHeight - tilesDashboardHeight;

	private int screenWidth = tileWidth * tilesScreenWidth;
	private int screenHeight = tileWidth * tilesScreenHeight;
  private int contentHeight = tileWidth * tilesContentHeight;

	private int planeStartPosX;
	private int planePosX;
	private int planePosY;
	private int planeSpeedX;	
	private int planeSpeedYSlow;
	private int planeSpeedYMedium;
	private int planeSpeedYFast;
	private int planeSpeedY;
	private BufferedImage straightPlane;	
	private BufferedImage leftPlane;
	private BufferedImage rightPlane;		
	private BufferedImage plane;

	private boolean bulletExists;
	private int	bulletStartPosY;
	private int bulletPosY;
	private int bulletSpeed;	
	private BufferedImage bulletImage;

	private Map<Integer, BufferedImage> tileMaps = new HashMap<>();

	private BufferedImage dashBoardImage;

	private String exitString = "Do you want to exitGameState the game? [Y] or [N]";
	private int exitStringWidth;
	private BufferedImage exitScreenBGImage;	

	private int tick = 0;

	private BufferedImage mainImage = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB	);
	private Graphics mainG = (Graphics) mainImage.getGraphics();
	private int mapNumber = 1;
	private int mapPosY;

	private	Graphics frame_g;
	
	/************ CONSTRUCTOR ****************/	
	public JFrameTesting(){
		setupPlane();
		setupBullet();		
		setupMap();
		setupDashboard();
		setupExitScreen();

		setTitle("JFrameTesting");		
		setSize(screenWidth, screenHeight);
		setLocationRelativeTo(null); // Center jframe on screen
		setAlwaysOnTop(true); // Make the jframe stay on top of all other windows
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
		// Set up keylistener methods
		addKeyListener(this);

		frame_g = this.getGraphics();
		
		// Call run 
		new Thread(this).start();
	}
	
	public void run() {		
		enterGameLoop();
	}	
	
	/**************** GAME LOOP ***************/	
	private void enterGameLoop() {
		boolean running = true;
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
				if (gameRunningState) {
					tick += planeSpeedY;
				}
			} catch (Exception e) {
				running = false;
				System.out.println("Error in game loop");
				e.printStackTrace();
			}
		}
	}
	
	private void updateModel() {		
		if (gameRunningState || gameStartState) {
			if (left && !right) { 
				plane = leftPlane;
			} else if (right && !left) { 
				plane = rightPlane;
			} else {
				plane = straightPlane;
			}
			
			if (left) { 
				planePosX -= planeSpeedX;
			} 
			if (right) { 
				planePosX += planeSpeedX;
			}
			
			if (up && !down) {
				planeSpeedY = planeSpeedYFast;
			} else if (down && !up) {
				planeSpeedY = planeSpeedYSlow;
			} else {
				planeSpeedY = planeSpeedYMedium;
			}

			if(space && !bulletExists) {
				bulletExists = true;
			} else if (bulletExists) {
				bulletPosY -= bulletSpeed;
				if (bulletPosY <= bulletImage.getHeight()) {
					bulletPosY = bulletStartPosY;
					if (space) {
						bulletExists = true;
					} else {
						bulletExists = false;
					}
				}
			}
		}

		if(collisionState) {
			if (bulletExists) {
				bulletPosY -= bulletSpeed;
				if (bulletPosY < bulletImage.getHeight()) {
					bulletExists = false;
				}
			}
		}
		
		// Check for collisions with plane and landscape				
		BufferedImage currMap = tileMaps.get(mapNumber);
		int rgb;
		if (mapPosY > -1*contentHeight + 3*tileWidth) {
			// 1: Check front
			rgb = currMap.getRGB(
				planePosX+tileWidth/2, mapPosY + contentHeight - 2*tileWidth - planeSpeedY*2
			);
			if (rgb == -12482236) { // Green Color
				collisionState = true;
				gameRunningState = false;
			}		
			// 2: Check right
			rgb = currMap.getRGB(
				planePosX+tileWidth-1, mapPosY + contentHeight - 2*tileWidth + tileWidth/2
			);
			if (rgb == -12482236) { // Green Color
				collisionState = true;
				gameRunningState = false;
			}
			// 2: Check left
			rgb = currMap.getRGB(
				planePosX+1, mapPosY + contentHeight - 2*tileWidth + tileWidth/2
			);
			if (rgb == -12482236) { // Green Color
				collisionState = true;
				gameRunningState = false;
			}
		}		
	}
	
	private void renderMainImage() {		
		// Draw map section		
		if(gameStartState || gameRunningState || collisionState || gamePausedState) {
			BufferedImage tileMap = tileMaps.get(mapNumber);
			BufferedImage tileMapNext = tileMaps.get(mapNumber + 1);
			int x = 0;
			mapPosY = tileMap.getHeight() - contentHeight - tick;
			if (mapPosY <= 0) {
				int w = screenWidth;
				int h = contentHeight - Math.abs(mapPosY);
				if (h <= 0) {
					int yNext = tileMapNext.getHeight() - contentHeight;
					int hNext = contentHeight;
					BufferedImage contentImageNext = tileMapNext.getSubimage(0, yNext, w, hNext);
					mainG.drawImage(contentImageNext, 0, 0, null);					
					mapNumber += 1;
					tick = 0;
				} else {				
					int yNext = tileMapNext.getHeight() - contentHeight + h;
					int hNext = contentHeight - h;
					if (hNext > 0) {
						BufferedImage contentImageNext = tileMapNext.getSubimage(0, yNext, w, hNext);
						mainG.drawImage(contentImageNext, 0, 0, null);
					}
					BufferedImage contentImage = tileMap.getSubimage(x, 0, w, h);
					mainG.drawImage(contentImage, 0, Math.abs(mapPosY), null);
				}
			} else {
				int w = screenWidth;
				int h = contentHeight;		
				BufferedImage contentImage = tileMap.getSubimage(x, mapPosY, w, h);			
				mainG.drawImage(contentImage, 0, 0, null);
			}
		
			// Dashboard
			mainG.drawImage(dashBoardImage, 0, contentHeight, null);
		
			// Plane				
			mainG.drawImage(plane, planePosX, planePosY, null);

			// Bullet
			if (bulletExists) {
				System.out.println("affff");
				mainG.drawImage(bulletImage, planePosX + 13, bulletPosY, null);
			}
		}
		
		if(exitGameState) {
			mainG.drawImage(exitScreenBGImage, 0, 0, null);	
			mainG.setColor(Color.RED);
			mainG.drawString(exitString, (screenWidth)/2-exitStringWidth/2, (screenHeight)/2);
		}
	}
	
	private void drawMainImageToFrame() {				
		frame_g.drawImage(mainImage, 0, 0, null);
	}


	//////////////////////////////////////////////////////////////////
	///////////////////////// SETUP //////////////////////////////////
	//////////////////////////////////////////////////////////////////	

	/************* PLANE ******************/	
	private void setupPlane() {
		try {
			planeStartPosX = (screenWidth)/2-tileWidth/2;
			planePosX = planeStartPosX;
			planePosY = (contentHeight)-tileWidth*2;
			planeSpeedX = 4;	
			planeSpeedYSlow = 4;
			planeSpeedYMedium = 8;
			planeSpeedYFast = 12;
			planeSpeedY = planeSpeedYMedium;
			BufferedImage planeImage = ImageIO.read(new File("img/plane.png"));
			straightPlane = planeImage.getSubimage(0, 0, tileWidth, tileWidth);	
			leftPlane = planeImage.getSubimage(tileWidth, 0, tileWidth, tileWidth);
			rightPlane = planeImage.getSubimage(tileWidth, tileWidth, tileWidth, tileWidth);		
			plane = straightPlane;
		} catch (Exception e) {
			System.out.println("Error setting up plane images");
		}
	}
	
	/**************** BULLET ***************/
	private void setupBullet() {
		try {
			bulletExists = false;
			bulletStartPosY = planePosY;
			bulletPosY = bulletStartPosY;
			bulletSpeed = 25;	
			bulletImage = ImageIO.read(new File("img/bullet.png"));
		} catch (Exception e) {
			System.out.println("Error getting bullet.png");
		}
	}
	
	/*************** MAP ******************/	
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
	private void setupDashboard() {
		try {
			BufferedImage fullTiles = ImageIO.read(new File("img/fullTiles.png"));
			
			BufferedImage fullGray = fullTiles.getSubimage(0, 32, tileWidth, tileWidth);
			BufferedImage grayWithTopBlack = fullTiles.getSubimage(32, 32, tileWidth, tileWidth);
			
			dashBoardImage = new BufferedImage(
				screenWidth, 
				tilesDashboardHeight*tileWidth, 
				BufferedImage.TYPE_INT_RGB
			);
			Graphics g = (Graphics) dashBoardImage.getGraphics();
			
			for (int i = 0; i < tilesScreenWidth; i++) {
				g.drawImage(grayWithTopBlack, i*tileWidth, 0, null);
			}
			for (int i = 1; i < tilesDashboardHeight; i++) {
				for (int j = 0; j < tilesScreenWidth; j++) {
					g.drawImage(fullGray, j*tileWidth, i*tileWidth, null);
				}
			}
		} catch (Exception e) {
			System.out.println("Error in setup dashboards");
		}
	}
	
	/*************** EXIT SCREEN ************/	
	private void setupExitScreen() {		
		try {			
			BufferedImage fullTiles = ImageIO.read(new File("img/fullTiles.png"));			
			BufferedImage fullBlack = fullTiles.getSubimage(0, 64, tileWidth, tileWidth);
			
			exitScreenBGImage = new BufferedImage(
				screenWidth, 
				screenHeight, 
				BufferedImage.TYPE_INT_RGB
			);
			Graphics g = (Graphics) exitScreenBGImage.getGraphics();
			
			for (int i = 0; i < tilesScreenHeight; i++) {
				for (int j = 0; j < tilesScreenWidth; j++) {
					g.drawImage(fullBlack, j*tileWidth, i*tileWidth, null);
				}
			}			
			exitStringWidth = g.getFontMetrics().stringWidth(exitString);
		} catch (Exception e) {
			System.out.println("Error in setup maps");
		}
	}

}
