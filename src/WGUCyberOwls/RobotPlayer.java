package WGUCyberOwls;

import battlecode.common.*;

import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Random;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    static int HP;

    /**
     * Shared Array Index
     * 0 - Archon count
     * 1 - Miner count
     * 2 - Watchtower count
     * 3 - Soldier count
     * 4 - Builder count
     * 5 - Lab count
     */

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());
        HP = rc.getHealth();
        // You can also use indicators to save debug notes in replays.
        //rc.setIndicatorString("Hello world!");

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount++;  // We have now been alive for one more turn!
            System.out.println("Age: " + turnCount + "; Location: " + rc.getLocation());

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case ARCHON:     ArchonStrategy.runArchon(rc);  break;
                    case MINER:      MinerStrategy.runMiner(rc);   break;
                    case SOLDIER:    runSoldier(rc); break;
                    case WATCHTOWER:  runWatchtower(rc); break;
                    case BUILDER:     BuildStrategy.runBuilder(rc); break;
                    case LABORATORY: // stretch goal!
                    case SAGE:       break;
                }
            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for an Archon.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runArchon(RobotController rc) throws GameActionException {   
        // Production Costs
        // Archon	Laboratory	Watchtower
        // 250 Au (nominal)	800 Pb	180 Pb
        // Miner	Builder	Soldier	Sage
        // 50 Pb	40 Pb	75 Pb	50 Au

        int minersBuilt = 0;
        int soldiersBuilt = 0;
        int buildersBuilt = 0;
        
        int currentMiners;
        int currentBuilders;

        Direction dir = directions[rng.nextInt(directions.length)];

        if((currentMiners = rc.readSharedArray(1)) == 0) { // if we have no miners, we need some
            minersBuilt += buildMiner(rc, dir);
        }
        
        if((currentBuilders = rc.readSharedArray(4)) == 0) { // if we have no builders, we need some
            buildersBuilt += buildBuilder(rc, dir);
        } 

        if (rng.nextBoolean()) {
            minersBuilt += buildMiner(rc, dir);
        } else {
            soldiersBuilt += buildSoldier(rc, dir);
        }
        
        if(minersBuilt > 0)
            rc.writeSharedArray(1, currentMiners + minersBuilt);
        if(soldiersBuilt > 0)
            rc.writeSharedArray(3, rc.readSharedArray(3) + soldiersBuilt);
        if(buildersBuilt > 0)
            rc.writeSharedArray(4, currentBuilders + buildersBuilt);
    }

    static int buildMiner(RobotController rc, Direction dir) throws GameActionException {
        rc.setIndicatorString("Trying to build a miner");
        if (rc.canBuildRobot(RobotType.MINER, dir)) {
            rc.buildRobot(RobotType.MINER, dir);
            return 1;
        }
        return 0;
    }

    static int buildBuilder(RobotController rc, Direction dir) throws GameActionException {
        rc.setIndicatorString("Trying to build a builder");
        if (rc.canBuildRobot(RobotType.BUILDER, dir)) {
            rc.buildRobot(RobotType.BUILDER, dir);
            return 1;
        }
        return 0;
    }

    static int buildSoldier(RobotController rc, Direction dir) throws GameActionException {
        rc.setIndicatorString("Trying to build a soldier");
        if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
            rc.buildRobot(RobotType.SOLDIER, dir);
            return 1;
        }
        return 0;
    }

    /**
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runSoldier(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if (enemies.length > 0) {
            MapLocation toAttack = enemies[0].location;
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
        }

        // Also try to move randomly.
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
            System.out.println("I moved!");
        }
    }

    static void runLaboratory(RobotController rc) throws GameActionException {
        // mix it up, literally - mix stuff in the lab
    }

    /**
     * Controls both sentry and portable modes of operation for watchtowers.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     * @param rc - The RobotController for this bot
     * @throws GameActionException
     */
    static void runWatchtower(RobotController rc) throws GameActionException {

        int myRadius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(myRadius, opponent);
        
        RobotMode mode = rc.getMode();
        switch(mode)
        {
            case TURRET:
                watchtowerSentryMode(rc, enemies, myRadius);
                break;
            case PORTABLE:
                watchtowerPortableMode(rc, enemies, myRadius);
                break;
            default: // we're either transforming or super confused
                System.out.println("I'm a " + rc.getType() + " and I'm in the watchtower runner for some reason with an odd mode!");
                break;
        }
    }


    static void watchtowerSentryMode(RobotController rc, RobotInfo[] enemies, int myRadius) throws GameActionException 
    {
        if(enemies.length == 0) {
            RobotInfo[] allies = rc.senseNearbyRobots(myRadius, rc.getTeam());
            boolean isAllyNearby = allies.length > 0;
            // if we're near an ally we want to keep covering them just in case enemies arrive,
            // so we drop out of the logic tree otherwise and just return;
            if(isAllyNearby == false && rc.isTransformReady()) {
                rc.transform();
            }
            return;
        }
        
        RobotInfo closestEnemy = enemies[0];
        int closestDistance = rc.getLocation().distanceSquaredTo(closestEnemy.location);
        for(int i = 1; i < enemies.length; i++) {
            int tmpDistance = rc.getLocation().distanceSquaredTo(enemies[i].location);
            if(tmpDistance < closestDistance) {
                closestEnemy = enemies[i];
                closestDistance = tmpDistance;
            }
        }
        
        if(rc.canAttack(closestEnemy.location))
            rc.attack(closestEnemy.location);
    }

    static void watchtowerPortableMode(RobotController rc, RobotInfo[] enemies,int myRadius) throws GameActionException {
        // if there are no enemies nearby and no allies nearby, continue to roam.
        if(enemies.length == 0) {
            RobotInfo[] allies = rc.senseNearbyRobots(myRadius, rc.getTeam());
            boolean isAllyNearby = allies.length > 0;
            if(isAllyNearby) { // there are allies nearby, check for an archon

                boolean isArchon = false;
                int botNum = 0;
                for(int i = 0; i < allies.length; i++) {
                    if(allies[i].getType() == RobotType.ARCHON) {
                        isArchon = true;
                        botNum = i;
                        break;
                    }
                }
                if(isArchon) { // if this is an archon, we should try to stay close.
                    RobotInfo archon = allies[botNum];
                    MapLocation archonLoc = archon.getLocation();
                    MapLocation curLoc = rc.getLocation();
                    if(curLoc.isAdjacentTo(archonLoc)) {
                        rc.transform();
                    } else {
                        Direction targetDir = curLoc.directionTo(archonLoc);
                        if(rc.canMove(targetDir)) {
                            rc.move(targetDir);
                        } else {
                            // just transform here, it's fine... everything is fine.
                            if(rc.isTransformReady())
                                rc.transform();
                        }
                    }
                } else { // we're near a bot, but not an archon. 
                    Direction targetDir = rc.getLocation().directionTo(allies[0].location);
                    if(rc.canMove(targetDir)) {
                        rc.move(targetDir);
                    } else {
                        // just transform here, it's fine... everything is fine.
                        if(rc.isTransformReady())
                            rc.transform();
                    }
                }
            }
            else {
                // this is where we roam around looking for a fight   
                Direction dir = directions[rng.nextInt(directions.length)];
                if (rc.canMove(dir)) {
                    rc.move(dir);
                }       
             }
        }
        // Here, we want to make sure we're guarding the nearest archon. If they are moving, we should move.
    }
}
