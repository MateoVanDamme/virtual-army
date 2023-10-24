package be.ugent.devops.commons.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class models information about a Faction.
 */
public record Faction(
        @JsonProperty("id") int id,
        @JsonProperty("name") String name,
        @JsonProperty("base") Location base,
        @JsonProperty("gold") long gold,
        @JsonProperty("territorySize") int territorySize,
        @JsonProperty("population") int population,
        @JsonProperty("populationCap") int populationCap,
        @JsonProperty("kills") int kills,
        @JsonProperty("score") long score,
        @JsonProperty("defeated") boolean defeated
) {
}
