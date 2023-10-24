package be.ugent.devops.commons.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.reactivex.annotations.NonNull;

import java.util.Optional;

/**
 * This class defines the message format of the BaseMove request (input).
 * <p>
 * It contains all the basic information necessary for the FactionLogic implementation to make its decision
 * on what the next BaseMove should be.
 *
 * @param context        Basic context information about the Game
 * @param faction        Up-to-date information about the Faction doing the move
 * @param buildSlotState Optional information about the current build slot for the Faction (only present if the building of a unit is still in progress for the Faction)
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record BaseMoveInput(
        @JsonProperty("context") @NonNull GameContext context,
        @JsonProperty("faction") @NonNull Faction faction,
        @JsonProperty("buildSlotState") @NonNull Optional<BuildSlotState> buildSlotState
) {
}
