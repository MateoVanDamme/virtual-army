package be.ugent.devops.commons.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.reactivex.annotations.NonNull;

/**
 * This class defines a Unit in the game world.
 */
public record Unit(
        @JsonProperty("id") int id,
        @JsonProperty("owner") int owner,
        @JsonProperty("type") @NonNull UnitType type,
        @JsonProperty("damage") int damage,
        @JsonProperty("health") int health,
        @JsonProperty("defenseBonus") boolean defenseBonus
) {
}
