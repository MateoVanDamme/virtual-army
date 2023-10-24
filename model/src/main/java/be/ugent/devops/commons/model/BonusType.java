package be.ugent.devops.commons.model;

/**
 * Defines the various type of bonuses that can be applied by redeeming valid bonus codes as a BaseMove.
 * (Bonus codes will be introduced in Lab session 3)
 */
public enum BonusType {
    /**
     * If this bonus is active, a fixed amount of gold is added to your stash during this turn
     */
    EXTRA_GOLD,
    /**
     * If this bonus is active, a multiplier is applied to all income
     */
    INCOME_MULTIPLIER,
    /**
     * If this bonus is active, units are cheaper to produce
     */
    CHEAPER_UNITS,
    /**
     * If this bonus is active, pioneers and soldiers can conquer locations in a single turn (as opposed to first performing NEUTRALIZE and then CONQUER).
     */
    RUSH_ATTACK;
}
