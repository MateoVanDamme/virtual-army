package be.ugent.devops.services.logic;

import be.ugent.devops.commons.model.Location;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GameState {

    private List<POI> pointsOfInterest;
    private int hash = 0;

    public List<POI> getPointsOfInterest() {
        return pointsOfInterest;
    }

    public void setPointsOfInterest(List<POI> pointsOfInterest) {
        this.pointsOfInterest = pointsOfInterest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GameState gameState = (GameState) o;
        return Objects.equals(pointsOfInterest, gameState.pointsOfInterest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointsOfInterest);
    }

    @JsonIgnore
    public boolean isChanged() {
        boolean result = hash != hashCode();
        hash = hashCode();
        return result;
    }
}
