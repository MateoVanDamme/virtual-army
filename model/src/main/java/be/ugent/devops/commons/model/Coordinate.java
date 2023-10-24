package be.ugent.devops.commons.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Base class for representing a Coordinate in the Game. Is extended by the class Location.
 * You can use this class in your Faction Logic implementation code to model positions on the map!
 */
public class Coordinate {

    protected final int x;
    protected final int y;

    public Coordinate(@JsonProperty("x") int x, @JsonProperty("y") int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * The X coordinate of the Location.
     * X increases from 0 to the width of the world map (from the left of the screen to the right).
     */
    public int getX() {
        return x;
    }

    /**
     * The Y coordinate of the Location
     * Y increases from 0 to the height of the world map (from the top of the screen to the bottom).
     */
    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return x == that.x &&
                y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Coordinate[" +
                "x=" + x +
                ", y=" + y +
                ']';
    }
}
