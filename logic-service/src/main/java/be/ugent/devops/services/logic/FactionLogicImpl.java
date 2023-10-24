package be.ugent.devops.services.logic;

import be.ugent.devops.commons.model.*;
import be.ugent.devops.services.logic.utils.BonusCode;
import be.ugent.devops.services.logic.utils.POIsHint;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.HTTPServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static be.ugent.devops.services.logic.utils.Constants.GAMESTATE_PATH;

public class FactionLogicImpl implements FactionLogic {

    private static final Logger logger = LoggerFactory.getLogger(FactionLogicImpl.class);
    private static final Random rg = new Random();
    private static final double PIONEER_GENERATE_GOLD_CHANCE = 0.15;

    String bonuscode;

    private final HTTPServer prometheusServer;

    static final Gauge territory = Gauge.build()
            .name("faction_territory")
            .help("Amount of territory hold by the Faction.")
            .register();

    static final Gauge population = Gauge.build()
            .name("faction_population")
            .help("Population of the Faction.")
            .register();

    static final Gauge score = Gauge.build()
            .name("faction_score")
            .help("Score of the Faction.")
            .register();
    static final Gauge kills = Gauge.build()
            .name("faction_kills")
            .help("Kills of the Faction.")
            .register();
    static final Gauge gold = Gauge.build()
            .name("faction_gold")
            .help("Gold of the Faction.")
            .register();

    static final Counter heals = Counter.build()
            .name("cleric_heals")
            .help("How much units have been healed by the cleric")
            .register();
    static final Counter fortifications = Counter.build()
            .name("worker_fortifications")
            .help("How much tiles have the workers fortified")
            .register();

    private final JsonObject config;
    private String currentGameId;
    private List<POI> pointsOfInterest;

    private static Path statePath = Path.of(GAMESTATE_PATH); // Define a path for your state here. Make it configurable!
    GameState gameState;

    private void saveState() {
        // Only try to save the state if it has changed!
        if (gameState.isChanged()) {
            logger.info("Creating state snapshot!");
            try {
                if ((statePath).toFile().exists()) {
                    if (statePath.toFile().delete()) {
                        logger.info("verwijderd");
                    }
                }
                Files.createFile(statePath);
                Files.writeString(statePath, Json.encode(gameState), StandardCharsets.UTF_8);

            } catch (IOException e) {
                logger.warn("Could not write state!", e);
            }
        }
    }

    private void restoreState() {
        // Only try to restore the state if a state file exists!
        if (statePath.toFile().exists()) {
            try {
                gameState = Json.decodeValue(Files.readString(statePath, StandardCharsets.UTF_8), GameState.class);
                pointsOfInterest = gameState.getPointsOfInterest();

            } catch (Exception e) {
                logger.warn("Could not restore state!", e);
            }
        }
        // If this line is reached and gameState is still 'null', initialize a new instance
        if (gameState == null) {
            logger.info("Initialized new gamestate");
            gameState = new GameState();
        }
    }


    public FactionLogicImpl() {
        this(new JsonObject());
    }

    public FactionLogicImpl(JsonObject config) {
        this.config = config;
        pointsOfInterest = new ArrayList<>();
        logger.info("New FactionLogicImplementation created");
        try {
            prometheusServer = new HTTPServer(1234);
        } catch (IOException ex) {
            // Wrap the IO Exception as a Runtime Exception so HttpBinding can remain unchanged.
            throw new RuntimeException("The HTTPServer required for Prometheus could not be created!", ex);
        }
        //Restore from state if needed
        restoreState();
    }

    @Override
    public BaseMove nextBaseMove(BaseMoveInput input) {
        if (!input.context().gameId().equals(currentGameId)) {
            currentGameId = input.context().gameId();
            gameState = new GameState();
            logger.info("Start running game with id {}...", currentGameId);
            logger.info("Reset gamestate");
        }
        territory.set(input.faction().territorySize()); //aanpassing van tuto --> hier :  .getFaction().getTerritorySize() --> .faction().territorySize()
        population.set(input.faction().territorySize());
        score.set(input.faction().score());
        kills.set(input.faction().kills());
        gold.set(input.faction().gold());

        saveState();

        //Check if we need to redeem a bonus code
        if (bonuscode != null) {
            String kopie = new String(bonuscode);
            bonuscode = null;
            logger.info("Redeeming this bonuscode: {}", kopie);
            return MoveFactory.redeemBonusCode(kopie);
        }

        return nextUnit(input.faction())
                .filter(type -> input.faction().gold() >= input.context().unitCost().get(type) && input.buildSlotState().isEmpty())
                .map(MoveFactory::baseBuildUnit)
                .orElseGet(() -> input.buildSlotState()
                        .map(it -> MoveFactory.baseContinueBuilding())
                        .orElseGet(MoveFactory::baseReceiveIncome)
                );
    }

    @Override
    public UnitMove nextUnitMove(UnitMoveInput input) {
        return switch (input.unit().type()) {
            case PIONEER -> pioneerLogic(input);
            case SOLDIER -> soldierLogic(input);
            case WORKER -> workerLogic(input);
            case CLERIC -> clericLogic(input);
        };
    }

    private Optional<UnitType> nextUnit(Faction faction) {
        if (faction.population() < faction.populationCap()) {
            return Optional.of(randomListItem(List.of(UnitType.PIONEER, UnitType.WORKER, UnitType.SOLDIER, UnitType.CLERIC)));
        } else {
            return Optional.empty();
        }
    }

    private UnitMove workerLogic(UnitMoveInput input) {
        var worker = input.unit();
        var workerLocation = input.unitLocation();

        // Always try to move away from our own base location and enemy locations
        if (workerLocation.isBase() || isHostileLocation(workerLocation, worker.owner())) {
            return travel(input).orElse(MoveFactory.unitIdle()); // 0G
        }

        // If not on resource, try moving to a resource (that is not in enemy territory)
        var resourceLocation = input.neighbouringLocations().stream()
                .filter(loc -> loc.isResource() && !isHostileLocation(loc, worker.owner()))
                .findAny();
        if (!workerLocation.isResource() && resourceLocation.isPresent() && resourceLocation.get().getOccupyingUnit().isEmpty()) {
            return MoveFactory.unitTravelTo(resourceLocation.get()); // 0G
        }


        // If on a neutral or owned resource
        if (workerLocation.isResource() && !isHostileLocation(workerLocation, worker.owner())) {
            // First capture if neutral
            if (workerLocation.getOwner().isEmpty()) {
                return MoveFactory.unitConquerLocation(); //75G
            } else if (!workerLocation.isFortified()) {
                // Fortify this strategic location
                fortifications.inc();
                return MoveFactory.unitFortifyLocation(); // 150G
            } else {
                // Profit!
                return MoveFactory.unitGenerateGold(); // 0G
            }
        }

        for (POI p : pointsOfInterest) {
            if (p.getResource()) {
                if (Math.abs(p.getX() - workerLocation.getX()) <= 15) {
                    if (p.getX() > workerLocation.getX()) {
                        return MoveFactory.unitTravelTo(new Coordinate(workerLocation.getX() + 1, workerLocation.getY()));
                    } else {
                        return MoveFactory.unitTravelTo(new Coordinate(workerLocation.getX() - 1, workerLocation.getY()));
                    }

                }
                if (Math.abs(p.getY() - workerLocation.getY()) <= 15) {
                    if (p.getY() > workerLocation.getY()) {
                        return MoveFactory.unitTravelTo(new Coordinate(workerLocation.getX(), workerLocation.getY() + 1));
                    } else {
                        return MoveFactory.unitTravelTo(new Coordinate(workerLocation.getX(), workerLocation.getY() - 1));
                    }
                }
            }
        }
        // Otherwise: do random action and hope for the best!
        //var action = randomListItem(List.of(UnitMoveType.TRAVEL, UnitMoveType.FORTIFY, UnitMoveType.CONQUER_NEUTRAL_TILE, UnitMoveType.GENERATE_GOLD));
        if (/*action.equals(UnitMoveType.FORTIFY) &&*/ workerLocation.getOwner().equals(Optional.of(worker.owner())) && input.faction().gold() > 1000L) {
            return MoveFactory.unitFortifyLocation(); // 150G
        } else if (/*action.equals(UnitMoveType.CONQUER_NEUTRAL_TILE) &&*/ workerLocation.getOwner().isEmpty() && input.faction().gold() > 500L) {
            return MoveFactory.unitConquerLocation(); // 75G
        } else if (/*action.equals(UnitMoveType.GENERATE_GOLD) &&*/ Optional.of(worker.owner()).equals(workerLocation.getOwner())) {
            return MoveFactory.unitGenerateGold(); // 0G
        } else {
            // Travel
            return travel(input).orElse(MoveFactory.unitIdle()); // 2x 0G
        }
    }

    private UnitMove pioneerLogic(UnitMoveInput input) {
        //logger.info("Pioneer executing a move");
        var pioneer = input.unit();
        var pioneerLocation = input.unitLocation();

        // If possible, conquer territory
        if (!Optional.of(pioneer.owner()).equals(pioneerLocation.getOwner())) {
            if (pioneerLocation.getOwner().isEmpty()) {
                //logger.info("Pioneer with id {} conquered territory",pioneer.id());
                return MoveFactory.unitConquerLocation(); // 75G
            } else {
                //logger.info("Pioneer with id {} neutralized territory",pioneer.id());
                return MoveFactory.unitNeutralizeLocation(); // 25G
            }
        }

        // Attack enemies in range
        var enemyInRange = input.neighbouringLocations().stream()
                .flatMap(loc -> loc.getOccupyingUnit().stream())
                .filter(occupyingUnit -> occupyingUnit.owner() != pioneer.owner())
                .findAny();
        if (enemyInRange.isPresent()) {
            //logger.info("Pioneer with id {} attacked an enemy in range",pioneer.id());
            return MoveFactory.unitAttack(enemyInRange.get()); // 25G
        }

        // Otherwise, generate income a percentage of the time, else travel around
        if (rg.nextDouble() <= PIONEER_GENERATE_GOLD_CHANCE) {
            //logger.info("Pioneer with id {} generated gold",pioneer.id());
            return MoveFactory.unitGenerateGold(); // 0G
        } else {
            //logger.info("Pioneer with id {} travelled",pioneer.id());
            return travel(input).orElse(MoveFactory.unitGenerateGold()); // 2x 0G
        }
    }

    private UnitMove soldierLogic(UnitMoveInput input) {
        var soldier = input.unit();
        var soldierLocation = input.unitLocation();

        // Attack if enemy unit is near (should have priority as the soldier is the strongest unit)
        var enemyInRange = input.neighbouringLocations().stream()
                .flatMap(loc -> loc.getOccupyingUnit().stream())
                .filter(occupyingUnit -> occupyingUnit.owner() != soldier.owner())
                .findAny();


        if (enemyInRange.isPresent()) {
            return MoveFactory.unitAttack(enemyInRange.get()); // 25G
        }

        for (POI p : pointsOfInterest) {
            if (p.getUnit() != null) {
                if (Math.abs(p.getX() - soldierLocation.getX()) <= 15) {
                    if (p.getX() > soldierLocation.getX()) {
                        return MoveFactory.unitTravelTo(new Coordinate(soldierLocation.getX() + 1, soldierLocation.getY()));
                    } else {
                        return MoveFactory.unitTravelTo(new Coordinate(soldierLocation.getX() - 1, soldierLocation.getY()));
                    }

                }
                if (Math.abs(p.getY() - soldierLocation.getY()) <= 15) {
                    if (p.getY() > soldierLocation.getY()) {
                        return MoveFactory.unitTravelTo(new Coordinate(soldierLocation.getX(), soldierLocation.getY() + 1));
                    } else {
                        return MoveFactory.unitTravelTo(new Coordinate(soldierLocation.getX(), soldierLocation.getY() - 1));
                    }
                }
            }
        }

        // Prepare defences for next encounter
        if (!soldier.defenseBonus()) {
            return MoveFactory.unitPrepareDefense(); // 15G
        }

        // If possible, conquer territory
        if (!Optional.of(soldier.owner()).equals(soldierLocation.getOwner())) {
            if (soldierLocation.getOwner().isEmpty()) {
                return MoveFactory.unitConquerLocation(); // 75G
            } else {
                return MoveFactory.unitNeutralizeLocation(); // 25G
            }
        }

        // Else try to travel
        return travel(input).orElse(MoveFactory.unitPrepareDefense()); // 0G of 15G
    }

    /* Moves cleric
     * TRAVEL
     * ATTACK
     * HEAL
     * CONVERT
     * PREPARE_DEFENSE
     * CONVERT
     * IDLE
     * */
    private UnitMove clericLogic(UnitMoveInput input) {
        var cleric = input.unit();
        var clericLocation = input.unitLocation();
        logger.info("Cleric with id {} is making a move", cleric.id());
        logger.info("This cleric has his defensebonus on {}", cleric.defenseBonus());

        //Voor aanvallen
        var enemyInRange = input.neighbouringLocations().stream()
                .flatMap(loc -> loc.getOccupyingUnit().stream())
                .filter(occupyingUnit -> occupyingUnit.owner() != cleric.owner())
                .findAny();

        logger.info("Cleric with id {} has an enemy in range", cleric.id());

        //Voor healen
        var woundedAllyInRange = input.neighbouringLocations().stream()
                .flatMap(loc -> loc.getOccupyingUnit().stream())
                .filter(occupyingUnit -> occupyingUnit.owner() == cleric.owner() && isWounded(occupyingUnit))
                .findAny();

        logger.info("Cleric with id {} has an ally in range", cleric.id());

        //focus op healing van allies
        if (woundedAllyInRange.isPresent()) {
            logger.info("Cleric with id {} chose to heal an ally\n", cleric.id());
            heals.inc();
            return MoveFactory.unitHeal(woundedAllyInRange.get()); // 25G
        }

        //Converteer enemy unit
        if (enemyInRange.isPresent() && cleric.defenseBonus()) {
            logger.info("Cleric with id {} chose to convert an enemy\n", cleric.id());
            return MoveFactory.unitConvert(enemyInRange.get()); // 150G

        }
        if (enemyInRange.isPresent()) {
            logger.info("Cleric with id {} chose attack an enemy\n", cleric.id());
            return MoveFactory.unitAttack(enemyInRange.get()); // 25G
        }

        logger.info("Cleric with id {} will travel or prepare defense", cleric.id());


        return travel(input).orElse(MoveFactory.unitPrepareDefense()); // 0 of 15G
    }

    private <T> T randomListItem(List<T> input) {
        return input.get(rg.nextInt(input.size()));
    }

    private boolean isHostileLocation(Location location, Integer faction) {
        return location.getOwner().map(owner -> !owner.equals(faction)).orElse(false);
    }

    private Optional<UnitMove> travel(UnitMoveInput input) {
        var possibleMoves = input.neighbouringLocations().stream()
                .filter(loc -> !loc.isBase() || !loc.getOwner().equals(Optional.of(input.unit().owner()))) // Don't go back to own base.
                .filter(loc -> loc.getOccupyingUnit().isEmpty()) // The target location should not be occupied.
                .collect(Collectors.toList());
        return possibleMoves.isEmpty() ? Optional.empty() : Optional.of(MoveFactory.unitTravelTo(randomListItem(possibleMoves)));
    }

    private boolean isWounded(Unit unit) {
        return
                switch (unit.type()) {
                    case PIONEER -> unit.health() < 3;
                    case WORKER -> unit.health() < 5;
                    case SOLDIER -> unit.health() < 6;
                    case CLERIC -> unit.health() < 4;
                };
    }

    public Object registerPOIs(POIsHint input) {
        logger.info("Received POI list for game: {}", input.gameId());

        logger.info("Old POI list was {} long", pointsOfInterest.size());
        for (Location l : input.locations()) {
            logger.info("Location: [{},{}] is a {}", l.getX(), l.getY(), l.isResource() ? "Resource" : "Enemy base");
            pointsOfInterest.add(new POI(l.getX(), l.getY(), l.isResource(), l.getOccupyingUnit().isEmpty() ? null : l.getOccupyingUnit().get()));
        }

        logger.info("New POI list is {} long", pointsOfInterest.size());
        gameState.setPointsOfInterest(pointsOfInterest);
        return "POI list received";
    }

    public Object registerBonusCodes(BonusCode input) {
        logger.info("Received BonusCode: {}, expires on: {}", input.type(), input.validUntil());
        bonuscode = input.code();
        return "Bonuscode received";
    }
}
