package be.ugent.devops.commons.model;

/**
 * This class defines the operations that are required for implementing the decision logic for a Faction.
 */
public interface FactionLogic {

    /**
     * This operation is called at the start of each new turn.
     * It allows the Faction Logic to decide upon the next base move (e.g. generating income, building a unit).
     * <p>
     * The operation must return a BaseMove instance.
     */
    BaseMove nextBaseMove(BaseMoveInput input);

    /**
     * This operation is called every turn for each of the units the Faction controls.
     * It allows the Faction Logic to decide upon the next move for the unit.
     * <p>
     * The operation must return a UnitMove instance.
     */
    UnitMove nextUnitMove(UnitMoveInput input);

}
