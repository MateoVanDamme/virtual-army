package be.ugent.devops.services.logic;

import be.ugent.devops.commons.model.*;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;


public class FactionLogicTest {

    private static final Map<UnitType, Integer> bogusUnitBaseHealth = Arrays.stream(UnitType.values()).collect(Collectors.toMap(k -> k, v -> 100));
    private static final Map<UnitType, Integer> bogusUnitCost = Arrays.stream(UnitType.values()).collect(Collectors.toMap(k -> k, v -> 100));
    private static final Map<UnitMoveType, Integer> bogusUnitMoveCost = Arrays.stream(UnitMoveType.values()).collect(Collectors.toMap(k -> k, v -> 10));
    private static final FactionLogic logic = new FactionLogicImpl();
    private static final GameContext context = new GameContext(0, "test", 100, 50, bogusUnitBaseHealth, bogusUnitCost, bogusUnitMoveCost, Set.of());

    @Test
    public void testFactionBuildsUnit() {
        // Faction with maximum amount of gold and sufficient population capacity
        // The build slot of the faction is empty
        var input = new BaseMoveInput(context, maakFaction(Long.MAX_VALUE), Optional.empty());
        var move = logic.nextBaseMove(input);
        // With these conditions, the Faction Logic should return a START_BUILDING_UNIT move
        // Assert if this true
        assertEquals(BaseMoveType.START_BUILDING_UNIT, move.type());
        // Assert that the provided unit type should not be empty
        assertTrue(move.unitToBuild().isPresent());
    }

    @Test
    public void testClericHealingWoundedUnit() {
        var soldier = new Unit(1, 0, UnitType.SOLDIER, 3, 2, false);
        var cleric = maakCleric();
        var soldierLocation = new Location(1, 1, false, false, false, 0, soldier);
        var locationList = new ArrayList<Location>();
        locationList.add(soldierLocation);
        var clericLocation = new Location(1, 2, false, false, false, 0, cleric);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), cleric, clericLocation, locationList);
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.HEAL, move.type());
    }

    @Test
    public void testClericNotHealingHealthyUnit() {
        var soldier =maakSoldier(false,0);
        var cleric = maakCleric();
        var soldierLocation = new Location(1, 1, false, false, false, 0, soldier);
        var locationList = new ArrayList<Location>();
        locationList.add(soldierLocation);
        var clericLocation = new Location(1, 2, false, false, false, 0, cleric);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), cleric, clericLocation, locationList);
        var move = logic.nextUnitMove(input);
        // The move will be PREPARE_DEFENSE currently but that's not important.
        // Here we just test that it's not HEAL which is always wrong
        assertNotEquals(UnitMoveType.HEAL, move.type());
    }

    @Test
    public void testPioneerAttack(){

        var pioneer = maakPioneer();
        var soldierEnemy = maakSoldier(false,1);
        var soldierLocation = new Location(1, 1, false, false, false, 0, soldierEnemy);
        var locationList = new ArrayList<Location>();
        locationList.add(soldierLocation);
        var pioneerLocation = new Location(1, 2, false, false, false, 0, pioneer);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), pioneer, pioneerLocation, locationList);
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.ATTACK,move.type());
    }

    @Test
    public void testPioneerGenerateGold(){
        var square = new Location(1,2,true,false,false,0,null);
        var pioneer = maakPioneer();
        var locationList = new ArrayList<Location>();
        locationList.add(square);
        var pioneerLocation = new Location(1, 2, false, false, false, 0, pioneer);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), pioneer, pioneerLocation, locationList);
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.GENERATE_GOLD,move.type());

    }

    @Test
    public void testPioneerClaim(){
        var emptySquare = new Location(1,1,false,false,false,null,null);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), maakPioneer(), emptySquare, null);
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.CONQUER_NEUTRAL_TILE,move.type());

    }

    @Test
    public void testWorkerClaim(){
        var emptySquare = new Location(1,1,false,true,false,null,null);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), maakWorker(), emptySquare, new ArrayList<Location>());
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.CONQUER_NEUTRAL_TILE,move.type());
    }

    @Test
    public void testWorkerFortify(){
        var emptySquare = new Location(1,1,false,true,false,0,null);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), maakWorker(), emptySquare, new ArrayList<Location>());
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.FORTIFY,move.type());
    }

    @Test
    public void testWorkerGenerateGold(){
        var emptySquare = new Location(1,1,false,true,true,0,null);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), maakWorker(), emptySquare, new ArrayList<Location>());
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.GENERATE_GOLD,move.type());
    }

    @Test
    public void testSoldierAttack(){
        var soldierAlly = maakSoldier(false,0);
        var soldierEnemy = maakSoldier(false,1);
        var soldierLocationEnemy = new Location(1, 1, false, false, false, 0, soldierEnemy);
        var locationList = new ArrayList<Location>();
        locationList.add(soldierLocationEnemy);
        var soldierLocationAlly = new Location(1, 2, false, false, false, 0, soldierAlly);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), soldierAlly, soldierLocationAlly, locationList);
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.ATTACK,move.type());
    }

    @Test
    public void testSoldierDefenseBonus(){
        var soldierAlly = maakSoldier(false,0);
        var soldierLocation = new Location(1, 2, false, false, false, 0, soldierAlly);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), soldierAlly, soldierLocation, new ArrayList<Location>());
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.PREPARE_DEFENSE,move.type());
    }

    @Test
    public void testSoldierConquer(){
        var soldierAlly = maakSoldier( true,0);
        var soldierLocation = new Location(1, 2, false, false, false, null, soldierAlly);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), soldierAlly, soldierLocation, new ArrayList<Location>());
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.CONQUER_NEUTRAL_TILE,move.type());
    }

    @Test
    public void testSoldierConvert(){
        var soldierAlly = maakSoldier(true,0);
        var soldierLocation = new Location(1, 2, false, false, false, 2, soldierAlly);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), soldierAlly, soldierLocation, new ArrayList<Location>());
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.NEUTRALIZE_ENEMY_TILE,move.type());
    }

    @Test
    public void testWorkerLastResortGold(){
        var emptySquare = new Location(1,1,false,false,true,0,null);
        var input = new UnitMoveInput(context, maakFaction(200L), maakWorker(), emptySquare, new ArrayList<Location>());
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.GENERATE_GOLD,move.type());
    }

    @Test
    public void testWorkerLastResortConquer(){
        var emptySquare = new Location(1,1,false,false,false,null,null);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), maakWorker(), emptySquare, new ArrayList<Location>());
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.CONQUER_NEUTRAL_TILE,move.type());
    }

    @Test
    public void testWorkerLastResortFortify(){
        var emptySquare = new Location(1,1,false,false,false,0,null);
        var input = new UnitMoveInput(context, maakFaction(Long.MAX_VALUE), maakWorker(), emptySquare, new ArrayList<Location>());
        var move = logic.nextUnitMove(input);
        assertEquals(UnitMoveType.FORTIFY,move.type());
    }

    //hulpmethodes

    private Faction maakFaction(Long gold){
        var baseLocation = new Location(0, 0, false, false, false, 0, null);
        return new Faction(0, "TestFaction1", baseLocation, gold, 200, 0, 20, 0, 0, false);
    }

    private Unit maakWorker(){
        return new Unit(1, 0, UnitType.WORKER, 0, 5, false);
    }

    private Unit maakSoldier(boolean defense,int owner){
        return new Unit(1, owner, UnitType.SOLDIER, 3, 6, defense);
    }

    private Unit maakPioneer(){
        return new Unit(1, 0, UnitType.PIONEER, 2, 3, false);
    }

    private Unit maakCleric(){
        return  new Unit(2, 0, UnitType.CLERIC, 1, 4, false);
    }
}


