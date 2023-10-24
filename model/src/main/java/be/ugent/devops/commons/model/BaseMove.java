package be.ugent.devops.commons.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.reactivex.annotations.NonNull;

import java.util.Optional;

/**
 * This class defines the message format for the base move.
 * <p>
 * The base move determines if the Faction uses the turn to generate income or to start building or continue
 * building a unit.
 *
 * @param type        The type of BaseMove
 * @param unitToBuild Optional field representing the type of unit to build. Only contains a value if type == START_BUILDING_UNIT.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record BaseMove(
        @JsonProperty("type") @NonNull BaseMoveType type,
        @JsonProperty("unitToBuild") @NonNull Optional<UnitType> unitToBuild,
        @JsonProperty("baseLocation") @NonNull Optional<Coordinate> baseLocation,
        @JsonProperty("code") @NonNull Optional<String> code
) {
}