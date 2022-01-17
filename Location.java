
public class Location{
	private int x;
	private int y;
	
	public Location(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	public Location(Location point)
	{
		this.x = point.getX();
		this.y = point.getY();
	}

	public double getDistance(Location loc) {
        int distRow = this.y - loc.getY();
        int distCol = this.x - loc.getX();
        return Math.sqrt((distRow * distRow) + (distCol * distCol));
    }

	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public void setX(int x){
		this.x = x;
	}
	public void setY(int y){
		this.y = y;
	}
	public String toString(){
		return "x: "+x+"\ny: "+y;
	}
}