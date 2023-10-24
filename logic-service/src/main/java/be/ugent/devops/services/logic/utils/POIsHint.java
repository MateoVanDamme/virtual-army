package be.ugent.devops.services.logic.utils;

import be.ugent.devops.commons.model.Location;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.reactivex.annotations.NonNull;

import java.util.List;

public record POIsHint(
        @JsonProperty("gameId") @NonNull String gameId,
        @JsonProperty("locations") @NonNull List<Location> locations
)
{
    //nothing
}
