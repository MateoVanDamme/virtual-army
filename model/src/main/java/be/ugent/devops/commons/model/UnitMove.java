package be.ugent.devops.commons.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.reactivex.annotations.NonNull;

import java.util.Optional;

/**
 * This class defines the message format for the unit move.
 * <p>
 * The unit move defines what the next move will be for a specific unit (generate income, attack, conquer a location...).
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record UnitMove(
        @JsonProperty("type") @NonNull UnitMoveType type,
        @JsonProperty("targetLocation") @NonNull Optional<Coordinate> targetLocation,
        @JsonProperty("targetUnit") @NonNull Optional<Unit> targetUnit
) {

}
