package be.ugent.devops.commons.model;

/**
 * Enum describing the type of BaseMove
 */
public enum BaseMoveType {
    /**
     * Use the turn to generate income.
     */
    RECEIVE_INCOME,
    /**
     * Use the turn to start building a unit. In this case the unitToBuild parameter is NOT optional.
     */
    START_BUILDING_UNIT,
    /**
     * Use the turn to continue building the unit (all units take at least two turns before they are completed!).
     */
    CONTINUE_BUILDING_UNIT,
    /**
     * Use the turn to move the location of your base to a different conquered base location.
     */
    MOVE_BASE,
    /**
     * Use the turn to redeem and activate a code for a Bonus.
     * (Bonus codes will be introduced in Lab session 3)
     */
    REDEEM_CODE,
    /**
     * Do Nothing
     */
    IDLE
}
