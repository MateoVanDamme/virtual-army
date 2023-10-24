package be.ugent.devops.commons.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.reactivex.annotations.NonNull;

import java.util.List;

/**
 * This class defines the message format of the UnitMove request (input).
 * <p>
 * It contains all the basic information necessary for the FactionLogic implementation to make its decision
 * on what the next move should be for a specific Unit.
 */
public record UnitMoveInput(
        @JsonProperty("context") @NonNull GameContext context,
        @JsonProperty("faction") @NonNull Faction faction,
        @JsonProperty("unit") @NonNull Unit unit,
        @JsonProperty("unitLocation") @NonNull Location unitLocation,
        @JsonProperty("neighbouringLocations") @NonNull List<Location> neighbouringLocations
) {
}
