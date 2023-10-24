package be.ugent.devops.commons.model;

import java.util.Optional;

/**
 * Use this class to construct Base and Unit moves.
 */
public class MoveFactory {

    /**
     * Start building a new unit.
     *
     * @param unitToBuild The type of unit to build.
     * @return A BaseMove instance
     */
    public static BaseMove baseBuildUnit(UnitType unitToBuild) {
        return new BaseMove(BaseMoveType.START_BUILDING_UNIT, Optional.of(unitToBuild), Optional.empty(), Optional.empty());
    }

    /**
     * Continue building the unit currently in the Base building slot.
     *
     * @return A BaseMove instance
     */
    public static BaseMove baseContinueBuilding() {
        return new BaseMove(BaseMoveType.CONTINUE_BUILDING_UNIT, Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * Use the Base to receive additional income for the turn.
     *
     * @return A BaseMove instance
     */
    public static BaseMove baseReceiveIncome() {
        return new BaseMove(BaseMoveType.RECEIVE_INCOME, Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * Relocate the Base during this turn. WARNING: this resets the building slot.
     * The target location must be a Location having a Base and belong to the Faction doing the move.
     *
     * @param coordinate The coordinate of the Location to move to.
     * @return A BaseMove instance
     */
    public static BaseMove baseRelocate(Coordinate coordinate) {
        return new BaseMove(BaseMoveType.MOVE_BASE, Optional.empty(), Optional.of(coordinate), Optional.empty());
    }


    /**
     * Redeem a bonus code at the start of this turn.
     * (Bonus codes will be introduced in Lab session 3)
     *
     * @param code The bonus code
     * @return A BaseMove instance
     */
    public static BaseMove redeemBonusCode(String code) {
        return new BaseMove(BaseMoveType.REDEEM_CODE, Optional.empty(), Optional.empty(), Optional.of(code));
    }

    /**
     * Move the Unit to a Location.
     *
     * @param coordinate The coordinate fo the Location to travel to.
     * @return A UnitMove instance
     */
    public static UnitMove unitTravelTo(Coordinate coordinate) {
        return new UnitMove(UnitMoveType.TRAVEL, Optional.of(coordinate), Optional.empty());
    }

    /**
     * Use the Unit to neutralize a Location currently occupied by an enemy Faction.
     * (Fortified Locations will need to be neutralized twice!)
     *
     * @return A UnitMove instance
     */
    public static UnitMove unitNeutralizeLocation() {
        return new UnitMove(UnitMoveType.NEUTRALIZE_ENEMY_TILE, Optional.empty(), Optional.empty());
    }

    /**
     * Use the Unit to conquer a Location.
     * You can only conquer neutral Locations.
     *
     * @return A UnitMove instance
     */
    public static UnitMove unitConquerLocation() {
        return new UnitMove(UnitMoveType.CONQUER_NEUTRAL_TILE, Optional.empty(), Optional.empty());
    }

    /**
     * Use the Unit to fortify a Location.
     *
     * @return A UnitMove instance
     */
    public static UnitMove unitFortifyLocation() {
        return new UnitMove(UnitMoveType.FORTIFY, Optional.empty(), Optional.empty());
    }

    /**
     * Use the unit to generate additional income.
     *
     * @return A UnitMove instance
     */
    public static UnitMove unitGenerateGold() {
        return new UnitMove(UnitMoveType.GENERATE_GOLD, Optional.empty(), Optional.empty());
    }

    /**
     * Use the Unit to attack an enemy Unit.
     *
     * @param target The unit to attack
     * @return A UnitMove instance
     */
    public static UnitMove unitAttack(Unit target) {
        return new UnitMove(UnitMoveType.ATTACK, Optional.empty(), Optional.of(target));
    }

    /**
     * Use the Unit to setup defenses.
     * A Unit with prepared defenses will only take half of the damage on the next attack.
     *
     * @return A UnitMove instance
     */
    public static UnitMove unitPrepareDefense() {
        return new UnitMove(UnitMoveType.PREPARE_DEFENSE, Optional.empty(), Optional.empty());
    }

    /**
     * Do nothing.
     *
     * @return A UnitMove instance
     */
    public static UnitMove unitIdle() {
        return new UnitMove(UnitMoveType.IDLE, Optional.empty(), Optional.empty());
    }

    /**
     * Use the Unit to heal a friendly unit.
     *
     * @param target The unit to heal.
     * @return A UnitMove instance
     */
    public static UnitMove unitHeal(Unit target) {
        return new UnitMove(UnitMoveType.HEAL, Optional.empty(), Optional.of(target));
    }

    /**
     * Use the Unit to convert an enemy Unit.
     * The unit doing the conversion must have defenses prepared for this to work!
     *
     * @param target The unit to convert
     * @return A UnitMove instance
     */
    public static UnitMove unitConvert(Unit target) {
        return new UnitMove(UnitMoveType.CONVERT, Optional.empty(), Optional.of(target));
    }


}
