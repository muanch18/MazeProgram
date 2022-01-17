import java.awt.Graphics;


public class Wall extends GameObject{

	public Wall(Location point){
		super(point);
	}
	
	public void draw(Graphics g){
		int x = getLocation().getX();
		int y = getLocation().getY();
		g.drawRect(x, y, 20, 20);
		
	}
	public String toString(){
		return "wall";
	}
	
}
	
	