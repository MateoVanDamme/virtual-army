package be.ugent.devops.commons.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

/**
 * This class models a Location in the game world.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Location extends Coordinate {

    private final boolean base;
    private final boolean resource;
    private final boolean fortified;
    private final Integer owner;
    private final Unit occupyingUnit;

    public Location(@JsonProperty("x") int x, @JsonProperty("y") int y, @JsonProperty("base") boolean base, @JsonProperty("resource") boolean resource,
             @JsonProperty("fortified") boolean fortified, @JsonProperty("owner") Integer owner, @JsonProperty("occupyingUnit") Unit occupyingUnit) {
        super(x, y);
        this.base = base;
        this.resource = resource;
        this.fortified = fortified;
        this.owner = owner;
        this.occupyingUnit = occupyingUnit;
    }

    /**
     * Indicates if the Location holds the base for a Faction.
     */
    public boolean isBase() {
        return base;
    }

    /**
     * Indicates if the Location holds a resource which can be extracted by a Worker for extra income.
     */
    public boolean isResource() {
        return resource;
    }

    /**
     * Indicates if the Location is fortified. Fortified territories require an extra turn to be conquered.
     */
    public boolean isFortified() {
        return fortified;
    }

    /**
     * Returns the optional owner of the Location.
     * For neutral Locations, the Optional will be empty.
     */
    public Optional<Integer> getOwner() {
        return Optional.ofNullable(owner);
    }

    /**
     * Returns the optional unit currently on the Location.
     * If no unit is present, the optional will be empty.
     */
    public Optional<Unit> getOccupyingUnit() {
        return Optional.ofNullable(occupyingUnit);
    }

    @Override
    public String toString() {
        return "Location{" +
                "x=" + x +
                ", y=" + y +
                ", base=" + base +
                ", resource=" + resource +
                ", fortified=" + fortified +
                ", owner='" + owner + '\'' +
                ", occupyingUnit=" + occupyingUnit +
                '}';
    }
}
