package be.ugent.devops.commons.model;

/**
 * Enum describing the type of Unit.
 */
public enum UnitType {
    /**
     * A Pioneer is the general purpose unit of a Faction's army.
     * Soldiers are more effective at combat and workers more effective at generating resources,
     * but Pioneers can do a bit of both.
     */
    PIONEER,
    /**
     * A Worker is a utility unit. It specializes in generating additional income (a Worker can occupy a resource location).
     * However, Worker units cannot perform hostile actions (conquering locations, attacking enemy units).
     */
    WORKER,
    /**
     * A Soldier specializes in combat. It has additional health and can get defensive bonuses.
     * However, Soldier units cannot generate income.
     */
    SOLDIER,
    /**
     * A CLERIC is an additional utilitarian Unit. Its role is supporting your Faction by healing friendly units and
     * trying to convert enemy units to your Faction’s side.
     */
    CLERIC;
}
