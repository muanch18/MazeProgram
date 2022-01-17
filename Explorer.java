import java.awt.Graphics;
import java.awt.Color;

public class Explorer extends GameObject{
	public Explorer(Location point){
		super(point);
	}

	public String toString(){
		return "explorer";
	}

	public void drawMan(Graphics g){
		int x = getLocation().getX();
		int y = getLocation().getY();
		g.setColor(Color.RED);
        g.fillRect(x, y, 20, 20);
	}


}