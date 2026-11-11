package Simulation;
public class Coordinate<T> {
    //Helper class that contains two ints referring to a position in a two-dimensional grid.
    int x, y;
    T value;
    public Coordinate(){
        x = 0; y = 0;
        value = null;
    }

    public Coordinate(int X, int Y, T value){
        x = X; y = Y;
        this.value = value;
    }

    public Coordinate(Coordinate<T> c){
        this.x = c.x;
        this.y = c.y;
        this.value = c.value;
    }

    @Override
    public String toString(){
        return "x = " + x + ", y = " + y + ", value = " + value;
    }
}
