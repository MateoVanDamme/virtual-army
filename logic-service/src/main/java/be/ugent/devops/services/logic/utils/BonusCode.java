package be.ugent.devops.services.logic.utils;

import be.ugent.devops.commons.model.Location;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.reactivex.annotations.NonNull;

import java.util.List;

public record BonusCode(
        @JsonProperty("type") @NonNull String type,
        @JsonProperty("code") @NonNull String code,
        @JsonProperty("validUntil") @NonNull String validUntil
) {
    //nothing
}
