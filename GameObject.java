import java.awt.Graphics;

public class GameObject{
    Location location;
    
    public GameObject(Location location){
        this.location = location;
    }
    
    
    public Location getLocation(){
        return location;
    }
    public void draw(Graphics g){
        int x = getLocation().getX();
        int y = getLocation().getY();
        g.drawRect(x, y, 20, 20);
    }
}
    