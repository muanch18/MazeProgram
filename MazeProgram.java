import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.awt.Polygon;

public class MazeProgram extends JPanel implements KeyListener, MouseListener {
	// Add ons: treasure, easter egg, and fog of war
	JFrame frame;
	int x = 100, y = 100;
	GameObject[][] mazeArray = new GameObject[20][32]; // figure out variables //can this be initialized in a later
														// place?
	// GameObject[][]mazeArray = new GameObject[23][109];
	Explorer thatPlayer = new Explorer(new Location(40, 60));

	boolean play = true;
	Graphics g;
	int moves;
	int rotate = 0;
	String direction = "East";
	boolean wallFoundL = false;
	boolean wallFoundR = false;
	boolean wallFoundB = false;
	ArrayList<Polygon> polygons = new ArrayList<>();
	ArrayList<Polygon> backWallPolygons = new ArrayList<>();
	ArrayList<Polygon> floorPolygons = new ArrayList<>();
	ArrayList<Polygon> roofPolygons = new ArrayList<>();
	ArrayList<Polygon> rectPolygons = new ArrayList<>();
	ArrayList<Polygon> bottomTriPolygons = new ArrayList<>();
	ArrayList<Polygon> topTriPolygons = new ArrayList<>();

	boolean treasureFound = false; // turns true when player is in front of it
	boolean openGateway = false; // when treasureFound is collected, sets white wall to light grey
	boolean breakWall = false; // turns true after treasureFound and openGateway are true, only when player is
								// in front of it
	boolean foundHidden = false; // turns true when the hidden treasure is found
	boolean collectHidden = false; // after the hidden treasure is collected
	boolean endInSight = false; // when player is able to see the end, made to turn the 3D a different color
	boolean launchTeleport = false; //

	int seeHowFar = 4;
	// boolean wallFoundB = false;

	public MazeProgram() {
		setBoard();
		frame = new JFrame();
		frame.add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(2500, 2500);
		frame.setVisible(true);
		frame.addKeyListener(this);
		// this.addMouseListener(this); //in case you need mouse clicking
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 1500, 1000);
		g.setColor(Color.WHITE);

		// this will set the background color
		if (play) {
			for (int i = 0; i < mazeArray.length; i++) {
				for (int j = 0; j < mazeArray[i].length; j++) {
					if (mazeArray[i][j] != null) {

					}
					if (mazeArray[i][j] instanceof Wall
							&& thatPlayer.getLocation().getDistance(mazeArray[i][j].getLocation()) < seeHowFar * 20) { // &&
						g.setColor(Color.WHITE);
						mazeArray[i][j].draw(g);
					}
					if (mazeArray[i][j] instanceof Explorer) {
						g.setColor(Color.RED);
						thatPlayer.drawMan(g);
					}
					if (mazeArray[i][j] instanceof End
							&& thatPlayer.getLocation().getDistance(mazeArray[i][j].getLocation()) < seeHowFar * 20) {
						g.setColor(Color.BLUE);
						g.fillRect(mazeArray[i][j].getLocation().getX(), mazeArray[i][j].getLocation().getY(), 20, 20);
					}
					if (mazeArray[i][j] instanceof Treasure
							&& thatPlayer.getLocation().getDistance(mazeArray[i][j].getLocation()) < seeHowFar * 20) {
						if (!treasureFound) {
							g.setColor(Color.YELLOW);
							g.fillRect(mazeArray[i][j].getLocation().getX(), mazeArray[i][j].getLocation().getY(), 20,
									20);
						} else {
							g.setColor(Color.WHITE);
							mazeArray[i][j].draw(g);
						}
					}
					if (mazeArray[i][j] instanceof Gateway
							&& thatPlayer.getLocation().getDistance(mazeArray[i][j].getLocation()) < seeHowFar * 20) {
						if (!treasureFound) {
							g.setColor(Color.WHITE);
							mazeArray[i][j].draw(g);
						} else if (treasureFound && !breakWall) {
							g.setColor(Color.DARK_GRAY);
							mazeArray[i][j].draw(g);
						}
						if (breakWall) {
							mazeArray[i][j] = null;
						}
					}
					if (mazeArray[i][j] instanceof Hidden
							&& thatPlayer.getLocation().getDistance(mazeArray[i][j].getLocation()) < seeHowFar * 20) {
						if (!foundHidden) {
							g.setColor(Color.PINK);
							mazeArray[i][j].draw(g);
						} else {
							g.setColor(Color.WHITE);
							mazeArray[i][j].draw(g);
							collectHidden = true;
						}
					}
					if (mazeArray[i][j] instanceof Fake
							&& thatPlayer.getLocation().getDistance(mazeArray[i][j].getLocation()) < seeHowFar * 20) {
						if (!breakWall) {
							g.setColor(Color.WHITE);
							mazeArray[i][j].draw(g);
						}
						if (breakWall) {
							mazeArray[i][j] = null;
						}
						/*
						 * else{ g.setColor(Color.GREEN); //just to test to see if anything changes
						 * mazeArray[i][j].draw(g);
						 * 
						 * }
						 */
					}

				}
			}
			g.setColor(Color.PINK);
			g.drawString("Rotate: " + rotate, 500, 500);
			g.drawString("Direction: " + direction, 500, 530);
			g.drawString("Moves: " + moves, 500, 560);
			if (!treasureFound) {
				g.drawString("Treasure Found: No", 500, 590);
			} else
				g.drawString("Treasure Found: Yes", 500, 590);
			if (collectHidden) {
				g.drawString("Hidden Treasure Found: Yes", 500, 620);
			}
			// 3D
			rectPolygons = makeRects();
			bottomTriPolygons = makeBottomTriangle();
			topTriPolygons = makeTopTriangle();
			for (Polygon p : rectPolygons) {
				Color thisColor = new Color(255, 245, 235);
				g.setColor(thisColor);
				g.fillPolygon(p);
				// System.out.println("asfsdfsdfsd"); //tag to debug and see if this loop is
				// reached
			}
			for (Polygon p : bottomTriPolygons) { // split into top and bottom triangles
				g.setColor(Color.BLUE);
				g.fillPolygon(p);
			}
			for (Polygon p : topTriPolygons) { // split into top and bottom triangles
				g.setColor(Color.LIGHT_GRAY);
				g.fillPolygon(p);
			}

			// make triangle lists and rect lists actually pop by changing color scheme
			// make two new ArrayLists for roof and floor to change color make it look dope
			// g.setColor(220-(i*10), 220-(i*10), 220-(i*10)) for a darker gradient effect
			for (Polygon p : floorPolygons) {
				g.setColor(Color.BLUE);
				g.fillPolygon(p);
			}
			for (Polygon p : roofPolygons) {
				g.setColor(Color.LIGHT_GRAY);
				g.fillPolygon(p);
			}

			for (Polygon p : polygons) { // make new class that will take a polygon and color

				g.setColor(Color.LIGHT_GRAY); // .getColor()
				g.drawPolygon(p); // .getShape()?
				Color thisColor = new Color(255, 245, 235);
				g.setColor(thisColor);
				g.fillPolygon(p);
			}
			for (Polygon p : backWallPolygons) { // use booleans to switch the color drawn?
				g.setColor(Color.LIGHT_GRAY); // .getColor()
				g.drawPolygon(p); // .getShape()?

				Color thisColor = new Color(255, 245, 235);
				g.setColor(thisColor);
				if (endInSight) { // could use similar booleans to get into array
					g.setColor(Color.BLUE);
				} else
					endInSight = false;
				g.fillPolygon(p);

			}

		} else {
			g.clearRect(0, 0, 3000, 3000);
			g.setColor(Color.BLACK);
			g.drawString("You finished! You have completed the maze in " + moves + " moves!", 300, 300);
		}

		// playable character
		// values would be set below
		// call the x & y values from your Explorer class
		// explorer.getX() and explorer.getY()

		// other commands that might come in handy
		// g.setFont("Times New Roman",Font.PLAIN,18);
		// you can also use Font.BOLD, Font.ITALIC, Font.BOLD|Font.Italic
		// g.drawOval(x,y,10,10);
		// g.fillRect(x,y,100,100);
		// g.fillOval(x,y,10,10);
	}

	public void setBoard() {
		// choose your maze design

		File name = new File("maze1.txt");

		try {
			BufferedReader input = new BufferedReader(new FileReader(name));
			String text;
			int counter = 0;

			while ((text = input.readLine()) != null) // assigns elements to special places in the maze while the text
														// file is read
			{
				String[] elements = text.split("");
				for (int x = 0; x < elements.length; x++) {
					if (elements[x].equals("#")) {
						mazeArray[counter][x] = new Wall(new Location(x * 20, counter * 20));
					}
					if (elements[x].equals("S")) {
						mazeArray[counter][x] = thatPlayer;
					}
					if (elements[x].equals("E")) {
						mazeArray[counter][x] = new End(new Location(x * 20, counter * 20));
					}
					if (elements[x].equals("T")) {
						mazeArray[counter][x] = new Treasure(new Location(x * 20, counter * 20));
					}
					if (elements[x].equals("G")) {
						mazeArray[counter][x] = new Gateway(new Location(x * 20, counter * 20));
					}
					if (elements[x].equals("H")) {
						mazeArray[counter][x] = new Hidden(new Location(x * 20, counter * 20));
					}
					if (elements[x].equals("F")) {
						mazeArray[counter][x] = new Fake(new Location(x * 20, counter * 20));
					}

				}
				counter++;
			}
		} catch (IOException io) {
			System.err.println("File error");
		}

		setWalls();
	}
	// make a public void draw method and use nested for loops to initialize the
	// Walls (parameter of outer forloop is the getWidt
	// Wall filea and in that have another draw method that actually draws the walls
	// then a move method with a switch statement saving every position as a
	// Location
	// Location file sets x and y and a boolean method to determine if there is a
	// wall in front.
	// Explorer

	public void setWalls() {
		polygons = new ArrayList<Polygon>();
		rectPolygons = new ArrayList<Polygon>();
		floorPolygons = new ArrayList<Polygon>();
		backWallPolygons = new ArrayList<Polygon>();
		int currentCol = thatPlayer.getLocation().getX() / 20;
		int currentRow = thatPlayer.getLocation().getY() / 20;

		// keep in mind that you are walking through an array (not the maze...even
		// though you are walking through a maze)
		// System.out.println(rotate);

		// This does the right walls!
		// System.out.println("dir: "+direction);
		// System.out.println("loc: "+currentRow+", "+currentCol);

		for (int i = 0; i < seeHowFar; i++) // 5 is completely arbitrary...It's how far I can see in 3D.
		{ // 5 steps in front? //RIGHT WALLS
			int[] xVals = { 1420 - (50 * i), 1370 - (50 * i), 1370 - (50 * i), 1420 - (50 * i) };
			int[] yVals = { 0 + (50 * i), 50 + (50 * i), 450 - (50 * i), 500 - (50 * i) };
			boolean wallFoundR = false;
			switch (direction) // you need to know what direction you are facing to draw the walls
			{ // left is different if I am facing north vs south vs east vs west

				case "East":
					// draw out the walls on a piece of paper and see how the numbers change

					// if I am facing east, then the left wall would fall one row above me
					// And then I look out one column forward via the loop
					// if there is no wall there, then add a trapezoid to the walls list
					try {
						if (mazeArray[currentRow + 1][currentCol + i] instanceof Wall) {
							wallFoundR = true;
						}
						if (mazeArray[currentRow + 1][currentCol + i] instanceof Gateway) {
							wallFoundR = true;
						}

					} catch (ArrayIndexOutOfBoundsException e) {
					}
					break;
				case "West":
					try {
						if (mazeArray[currentRow - 1][currentCol - i] instanceof Wall) {
							wallFoundR = true;

						}
						if (mazeArray[currentRow + 1][currentCol - i] instanceof Gateway) {
							wallFoundR = true;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
					}
					break;
				case "North":
					try {
						if (mazeArray[currentRow - i][currentCol + 1] instanceof Wall) {
							wallFoundR = true;

						}
						if (mazeArray[currentRow - i][currentCol + 1] instanceof Gateway) {
							wallFoundR = true;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
					}
					break;
				case "South":
					try {
						if (mazeArray[currentRow + i][currentCol - 1] instanceof Wall) {
							wallFoundR = true;

						}
						if (mazeArray[currentRow + i][currentCol - 1] instanceof Gateway) {
							wallFoundR = true;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
					}
					break;
			}

			if (wallFoundR) {
				polygons.add(new Polygon(xVals, yVals, 4));

			}
		}

		for (int i = 0; i < seeHowFar; i++) { // left walls //change these coordinates
			int[] xVals = { 920 + (50 * i), 970 + (50 * i), 970 + (50 * i), 920 + (50 * i) };
			int[] yVals = { 0 + (50 * i), 50 + (50 * i), 450 - (50 * i), 500 - (50 * i) };
			wallFoundL = false;
			switch (direction) { // left is different if I am facing north vs south vs east vs west

				case "East":
					// draw out the walls on a piece of paper and see how the numbers change

					// if I am facing east, then the left wall would fall one row above me
					// And then I look out one column forward via the loop
					// if there is no wall there, then add a trapezoid to the walls list
					try {
						if (mazeArray[currentRow - 1][currentCol + i] instanceof Wall) {
							wallFoundL = true;
						}
						if (mazeArray[currentRow - 1][currentCol + i] instanceof Gateway) {
							wallFoundL = true;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
					}
					break;
				case "West":
					try {
						if (mazeArray[currentRow + 1][currentCol - i] instanceof Wall) {
							wallFoundL = true;
						}
						if (mazeArray[currentRow + 1][currentCol - i] instanceof Gateway) {
							wallFoundL = true;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
					}

					break;
				case "North":
					try {
						if (mazeArray[currentRow - i][currentCol - 1] instanceof Wall) {
							wallFoundL = true;
						}
						if (mazeArray[currentRow - i][currentCol - 1] instanceof Gateway) {
							wallFoundL = true;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
					}

					break;
				case "South":
					try {
						if (mazeArray[currentRow + i][currentCol + 1] instanceof Wall) {
							wallFoundL = true;
						}
						if (mazeArray[currentRow + i][currentCol + 1] instanceof Gateway) {
							wallFoundL = true;
						}
					} catch (ArrayIndexOutOfBoundsException e) {
					}

					break;
			}
			if (wallFoundL) {
				polygons.add(new Polygon(xVals, yVals, 4));

			}
		}

		for (int i = 0; i < seeHowFar; i++) { // roof
			int[] xVals = { 920 + (50 * i), 970 + (50 * i), 1370 - (50 * i), 1420 - (50 * i) };
			int[] yVals = { 0 + (50 * i), 50 + (50 * i), 50 + (50 * i), 0 + (50 * i) };
			roofPolygons.add(new Polygon(xVals, yVals, 4));
		}
		for (int i = 0; i < seeHowFar; i++) { // floor
			int[] xVals = { 920 + (50 * i), 970 + (50 * i), 1370 - (50 * i), 1420 - (50 * i) };
			int[] yVals = { 500 - (50 * i), 450 - (50 * i), 450 - (50 * i), 500 - (50 * i) };
			floorPolygons.add(new Polygon(xVals, yVals, 4));
		}
		for (int i = seeHowFar; i >= 0; i--) {
			int[] xVals = { 920 + (50 * i), 1420 - (50 * i), 1420 - (50 * i), 920 + (50 * i) };
			int[] yVals = { 0 + (50 * i), 0 + (50 * i), 500 - (50 * i), 500 - (50 * i) };
			wallFoundB = false;
			switch (direction) {
				case "East":
					try {
						if (mazeArray[currentRow][currentCol + i] instanceof Wall)
							wallFoundB = true;
						if (mazeArray[currentRow][currentCol + i] instanceof Gateway)
							wallFoundB = true;

						// if (mazeArray[currentRow][currentCol + i] instanceof Fake)
						// threeDFake = true;

					} catch (ArrayIndexOutOfBoundsException e) {
					}

					break;
				case "West":
					try {
						if (mazeArray[currentRow][currentCol - i] instanceof Wall)
							wallFoundB = true;
						if (mazeArray[currentRow][currentCol - i] instanceof Gateway)
							wallFoundB = true;

					} catch (ArrayIndexOutOfBoundsException e) {
					}

					break;
				case "North":
					try {
						if (mazeArray[currentRow - i][currentCol] instanceof Wall)
							wallFoundB = true;

					} catch (ArrayIndexOutOfBoundsException e) {
					}

					break;
				case "South":
					try {
						if (mazeArray[currentRow + i][currentCol] instanceof Wall)
							wallFoundB = true;

					} catch (ArrayIndexOutOfBoundsException e) {
					}

					break;

			}

			if (wallFoundB) {
				backWallPolygons.add(new Polygon(xVals, yVals, 4));

			}
			// make sure to set booleans false to start of each loop
		}

	}

	public void keyPressed(KeyEvent e) {
		int playerX = Integer.MAX_VALUE;
		int playerY = Integer.MAX_VALUE;

		for (int x = 0; x < mazeArray.length; x++) {
			for (int y = 0; y < mazeArray[x].length; y++) {
				if (mazeArray[x][y] instanceof Explorer) {
					playerX = x;
					playerY = y;
				}
			}
		}

		switch (e.getKeyCode()) {
			case (68):
				rotate += 1;
				rotate = rotate % 4;
				if (rotate == 0)
					direction = "East";
				if (rotate == -2 || rotate == 2)
					direction = "West";
				if (rotate == -3 || rotate == 1)
					direction = "South";
				if (rotate == 3 || rotate == -1)
					direction = "North";
				break;
			case (65):
				rotate -= 1;
				rotate = rotate % 4;
				if (rotate == 0)
					direction = "East";
				if (rotate == -2 || rotate == 2)
					direction = "West";
				if (rotate == -3 || rotate == 1)
					direction = "South";
				if (rotate == 3 || rotate == -1)
					direction = "North";
				break;
			case (87): // forward
				System.out.println("X location: " + thatPlayer.location.getX());
				System.out.println("Y location: " + thatPlayer.location.getY());
				if (rotate == 0) {
					if (mazeArray[playerX][playerY + 1] == null) {
						mazeArray[playerX][playerY + 1] = mazeArray[playerX][playerY];
						mazeArray[playerX][playerY] = null;
						thatPlayer.location.setX((playerY + 1) * 20);
						thatPlayer.location.setY(playerX * 20);

						moves++;

						// mazeArray[playerX][playerY].getLocation().setX(playerX*20);
					}

					if (mazeArray[playerX][playerY + 1] instanceof End) {
						play = false;
					}
					if (mazeArray[playerX][playerY + 1] instanceof Treasure) {
						treasureFound = true;
						openGateway = true;
					}
					if (mazeArray[playerX][playerY + 1] instanceof Gateway) {
						if (treasureFound)
							breakWall = true;
					}
					if (mazeArray[playerX][playerY + 1] instanceof Hidden) {
						foundHidden = true;
					}
					if (mazeArray[playerX][playerY + seeHowFar] instanceof End) {
						endInSight = true;

					} // commented out these parts at one point because the whole line of GameObjects
				}
				if (rotate == -2 || rotate == 2) {
					if (mazeArray[playerX][playerY - 1] == null) {
						mazeArray[playerX][playerY - 1] = mazeArray[playerX][playerY];
						mazeArray[playerX][playerY] = null;
						thatPlayer.location.setX((playerY - 1) * 20);
						thatPlayer.location.setY(playerX * 20);

						moves++;
					}
					if (mazeArray[playerX][playerY - 1] instanceof End) {
						play = false;
					}
					if (mazeArray[playerX][playerY - 1] instanceof Treasure) {
						treasureFound = true;
						openGateway = true;
					}
					if (mazeArray[playerX][playerY - 1] instanceof Gateway) {
						if (treasureFound)
							breakWall = true;
					}
					if (mazeArray[playerX][playerY - 1] instanceof Hidden) {
						foundHidden = true;
					}

					try {
						if (mazeArray[playerX][playerY - seeHowFar] instanceof End) {
							endInSight = true;
						}
					} catch (ArrayIndexOutOfBoundsException a) {
					}

				}
				if (rotate == -3 || rotate == 1) {
					if (mazeArray[playerX + 1][playerY] == null) {
						mazeArray[playerX + 1][playerY] = mazeArray[playerX][playerY];
						mazeArray[playerX][playerY] = null;
						thatPlayer.location.setX((playerY) * 20);
						thatPlayer.location.setY((playerX + 1) * 20);

						moves++;
					}
					if (mazeArray[playerX + 1][playerY] instanceof End) {
						play = false;
					}
					if (mazeArray[playerX + 1][playerY] instanceof Treasure) {
						treasureFound = true;
						openGateway = true;
					}
					if (mazeArray[playerX + 1][playerY] instanceof Gateway) {
						if (treasureFound)
							breakWall = true;
					}
					if (mazeArray[playerX + 1][playerY] instanceof Hidden) {
						foundHidden = true;
					}
					if (mazeArray[playerX + seeHowFar][playerY] instanceof End) {
						endInSight = true;
					}

				}
				if (rotate == 3 || rotate == -1) {
					if (mazeArray[playerX - 1][playerY] == null) {
						mazeArray[playerX - 1][playerY] = mazeArray[playerX][playerY];
						mazeArray[playerX][playerY] = null;
						thatPlayer.location.setX((playerY) * 20);
						thatPlayer.location.setY((playerX - 1) * 20);

						moves++;
					}
					if (mazeArray[playerX - 1][playerY] instanceof End) {
						play = false;
					}
					if (mazeArray[playerX - 1][playerY] instanceof Treasure) {
						treasureFound = true;
						openGateway = true;
					}
					if (mazeArray[playerX - 1][playerY] instanceof Gateway) {
						if (treasureFound)
							breakWall = true;
					}
					if (mazeArray[playerX - 1][playerY] instanceof Hidden) {
						foundHidden = true;
					}

					if (mazeArray[playerX - seeHowFar][playerY] instanceof End) {
						endInSight = true;
					}

				}
				break;
		}
		setWalls();
		repaint();

		// System.out.println(e.getKeyCode());

	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public ArrayList<Polygon> makeRects() {
		ArrayList<Polygon> list = new ArrayList<>();
		for (int i = 0; i < seeHowFar; i++) { // left wall rects
			int[] x = { 920 + (i * 50), 970 + (i * 50), 970 + (i * 50), 920 + (i * 50) };
			int[] y = { 50 + (i * 50), 50 + (i * 50), 450 - (i * 50), 450 - (i * 50) };
			list.add(new Polygon(x, y, 4));
		}

		for (int i = 0; i < seeHowFar; i++) { // right wall rects
			int[] x = { 1420 - (i * 50), 1370 - (i * 50), 1370 - (i * 50), 1420 - (i * 50) };
			int[] y = { 50 + (i * 50), 50 + (i * 50), 450 - (i * 50), 450 - (i * 50) };
			list.add(new Polygon(x, y, 4));
		}

		return list;
	}

	public ArrayList<Polygon> makeBottomTriangle() {
		ArrayList<Polygon> list = new ArrayList<>();
		for (int i = 0; i < seeHowFar; i++) { // bottom triangles right
			int[] x = { 1420 - (i * 50), 1370 - (i * 50), 1420 - (i * 50) };
			int[] y = { 500 - (50 * i), 450 - (50 * i), 450 - (50 * i) };
			list.add(new Polygon(x, y, 3));
		}

		for (int i = 0; i < seeHowFar; i++) { // bottom triangles left
			int[] x = { 920 + (i * 50), 970 + (i * 50), 920 + (i * 50) };
			int[] y = { 500 - (50 * i), 450 - (50 * i), 450 - (50 * i) };
			list.add(new Polygon(x, y, 3));
		}

		return list;
	}

	public ArrayList<Polygon> makeTopTriangle() {
		ArrayList<Polygon> list = new ArrayList<>();
		for (int i = 0; i < seeHowFar; i++) { // top triangles right
			int[] x = { 1420 - (i * 50), 1420 - (i * 50), 1370 - (i * 50) };
			int[] y = { 0 + (50 * i), 50 + (50 * i), 50 + (50 * i) };
			list.add(new Polygon(x, y, 3));
		}

		for (int i = 0; i < seeHowFar; i++) { // top triangles left
			int[] x = { 920 + (i * 50), 970 + (i * 50), 920 + (i * 50) };
			int[] y = { 0 + (50 * i), 50 + (50 * i), 50 + (50 * i) };
			list.add(new Polygon(x, y, 3));
		}

		return list;

	}

	public static void main(String args[]) {
		MazeProgram app = new MazeProgram();
	}
}