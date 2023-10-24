package be.ugent.devops.commons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.reactivex.annotations.NonNull;

import java.util.Map;
import java.util.Set;

/**
 * This class is used to represent basic information for a Game Session.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GameContext(
        @JsonProperty("turnNumber") long turnNumber,
        @JsonProperty("gameId") @NonNull String gameId,
        @JsonProperty("mapWidth") int mapWidth,
        @JsonProperty("mapHeight") int mapHeight,
        @JsonProperty("unitBaseHealth") Map<UnitType, Integer> unitBaseHealth,
        @JsonProperty("unitCost") Map<UnitType, Integer> unitCost,
        @JsonProperty("unitMoveCost") Map<UnitMoveType, Integer> unitMoveCost,
        @JsonProperty("activeBonuses") Set<BonusType> activeBonuses
) {
}
