package be.ugent.devops.services.logic;

import be.ugent.devops.commons.model.Unit;

public class POI {
    private int x;
    private int y;
    private boolean resource;
    private Unit unit;

    public POI(int lx, int ly, boolean res, Unit unit){
        this.x = lx;
        this.y = ly;
        this.resource = res;
        this.unit = unit;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean getResource(){
        return  resource;
    }

    public Unit getUnit(){return unit;}
}
