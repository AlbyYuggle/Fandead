package ho4;
import battlecode.common.*;
import java.util.*;

public strictfp class RobotPlayer {
    static RobotController rc;
    static int cDir = 0, ecid = 0, flag = 0, oFlag = 0, next = 0, ESCORT = 15, DEF = 14, sz = 0, turns = 0, turnsFromDiscover = 0;
    static boolean nATK = false, atkEC = false, atBase = false, starting = true, buff = false;
    static MapLocation slA;
    static int[] rbid = new int[3000];
    static int[][] ec = new int[20][3], nec = new int[20][3];
    static final int DEFENDERFLAG = 1;
    static final int NEEDDEFENDFLAG = 2;
    static final int DEFENDOVERFLAG = 3;
    static int[] defenderID = new int[16], startingArray = {0, 1, 2, 3, 4, 5, 6, 7};
    static int startTurnCount=0;
    static boolean defending = false, cantVote = false, needDelay2 = false, arrived = false, isPad = false;;
    static boolean[] defendDirs = new boolean[8];
    static int prevVote = 0, prevInf = 0;
    static int[] goodValues = {41, 63, 85, 107, 130, 154, 178, 203, 229, 255, 282, 310, 339, 369, 400, 431, 463, 497, 532, 568, 605, 644, 683, 724, 766, 810, 855, 902, 949};
    static double minBid = 2;
    static MapLocation home, ene, sland;
    static int mPadID = 0, lowMLoc = 0;
    static int[] innerDefenderID = new int[8];
    static int baseRadSq = 144;
    static int maxDistFromBase = 0;
    static MapLocation spawnLoc, dest;
    static int nextPadLocIndex = 0;
    static double[][] graphWeights = new double[7][7];
	static double[][] sqrtDists = new double[7][7];
	static MapLocation dest2;

    static final RobotType[] spawnableRobot = {
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

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
    
    static final int[][] innerPadLocs = {
		{0,1},
		{0,2},
		{1,0},
		{2,0},
		{0,-1},
		{0,-2},
		{-1,0},
		{-2,0}
	};

    static int turnCount;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;
        for(int i = 0; i < 20; i++) {
            ec[i][0] = -1;
            ec[i][1] = -1;
            nec[i][0] = -1;
            nec[i][1] = -1;
        }

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

	static void runEnlightenmentCenter() throws GameActionException {
		/*int index = 0;
		while(ec[index++][0] != -1) System.out.println(Arrays.toString(ec[index-1]));
		index = 0;
		while(nec[index++][0] != -1) System.out.println("n" + Arrays.toString(nec[index-1]));
		System.out.println(Arrays.deepToString(nec));
		*/
        if(turnCount == 1) {
            prevVote = rc.getTeamVotes();
            prevInf = rc.getInfluence();
        }
        // if(rc.getInfluence()==prevInf){ //not important really
        //     if(rc.getTeamVotes()==prevVote){
        //         minBid = (minBid*1.2);
        //     }
        // }else if(rc.getInfluence()==prevInf-minBid){ //successful
        //     minBid= Math.max(2,(minBid/1.05));
        // }else{ //lost
        //     minBid = (minBid*1.2);
        // }
        if(rc.getRoundNum() > 200 && !cantVote){
            if(rc.getTeamVotes()==prevVote){
                    minBid = (minBid*1.1);
            }else{
                minBid= Math.max(2,(minBid/1.05));
            }
        }
        cantVote = false;
		flag = 0;
        
        for (RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam().opponent())){
            starting = false;
            defending = true;
            break;
        }
        if(rc.getRoundNum() > 50) starting = false;
        if(starting){
            if(startTurnCount==9){
                startTurnCount++;
            }
            else if(startTurnCount==10){
                starting = false;
            }
            if(startTurnCount==0){
                for(int k = 7; k<15; k++){
                    if(rc.canBuildRobot(RobotType.SLANDERER, directions[startingArray[k%8]], 130)){
                        rc.buildRobot(RobotType.SLANDERER, directions[startingArray[k%8]], 130);
						rbid[sz++] = rc.senseRobotAtLocation(rc.adjacentLocation(directions[startingArray[k%8]])).getID();
                        startTurnCount++;
                        break;
                    }
                }
            }
            else if(1<=startTurnCount && startTurnCount<=8){
                int loopCounter = 0;
                boolean notMap = false;
                while(startTurnCount<=8&&!rc.onTheMap(rc.adjacentLocation(directions[startTurnCount-1]))){
                    startTurnCount++;
                    notMap = true;
                }
                if(notMap){
                    while(rc.isLocationOccupied(rc.adjacentLocation(directions[startingArray[startTurnCount-1]]))){
                        int temp = startingArray[startTurnCount-1];
                        for(int i = startTurnCount; i<8; i++){
                            startingArray[i-1] = startingArray[i];
                        }
                        startingArray[7] = temp;
                        loopCounter++;
                        if(loopCounter>8){
                            break;
                        }                    
                    }
                    }
                if(rc.canBuildRobot(RobotType.MUCKRAKER, directions[startingArray[startTurnCount-1]], 1)){
                    rc.buildRobot(RobotType.MUCKRAKER, directions[startingArray[startTurnCount-1]], 1);
						rbid[sz++] = rc.senseRobotAtLocation(rc.adjacentLocation(directions[startingArray[startTurnCount-1]])).getID();
                    startTurnCount++;
                }
            }
        }
        
        if(rc.getInfluence() > 1000 && turnCount%30 == 0) buff = true;
        
        if(turnCount%200 == 50) buff = true;

        if(buff && rc.getInfluence() > 300 && rc.getCooldownTurns()==0) {
            buildAtkMuckraker();
            buff = false;
        }

        if(((ec[0][0] == -1 && nec[0][0] == -1) || rc.getRoundNum() < 100) && rc.getInfluence() > 50 && !searchEnemy(RobotType.MUCKRAKER, -1)) {
            if(sz%5 == 0) buildSlanderer(rc.getInfluence());
            if(sz%5 == 1 || sz%4 == 2) buildMuckraker();
            if(sz%5 == 3 || sz%5 == 4) buildDefPolitician();
        }
        
        if(searchEnemy(RobotType.MUCKRAKER, -1)) {
            if(sz%2 == 0) buildMuckraker();
            if(sz%2 == 1) buildDefPolitician();
        }
        
        else if(rc.getInfluence() > 50) {
            if(rc.getInfluence() > 200) {
                if(sz%6 == 0) buildSlanderer(rc.getInfluence()/2);
                if(sz%6 == 1 || sz%6 == 2) buildMuckraker();
                if(sz%6 == 3) buildAtkPolitician();
                if(sz%6 == 4 || sz%6 == 5) buildDefPolitician();
            } else {
                if(sz%6 == 0 || sz%6 == 1) buildSlanderer(rc.getInfluence()/2);
                if(sz%6 == 2 || sz%6 == 3) buildMuckraker();
                if(sz%6 == 4 || sz%6 == 5) buildDefPolitician();
            }
        }
        
        buildMuckraker();
        // else if(rc.getInfluence() > 50) {
        //     if(rc.getInfluence() > 200) {
        //         if(sz%6 == 0) buildSlanderer(rc.getInfluence()/2);
        //         if(sz%6 == 1 || sz%6 == 2) buildMuckraker();
        //         if(sz%6 == 3) buildAtkPolitician();
        //         if(sz%6 == 4 || sz%6 == 5) buildDefPolitician();
        //     } else {
        //         if(sz%6 == 0 || sz%6 == 1) buildSlanderer(rc.getInfluence()/2);
        //         if(sz%6 == 2 || sz%6 == 3) buildMuckraker();
        //         if(sz%6 == 4 || sz%6 == 5) buildDefPolitician();
        //     }
        // }
        // else buildMuckraker();
		
        getFlagInfo();
        putFlagInfo();
        rc.setFlag(flag);
        if(rc.getTeamVotes() > 750) return;
        if(rc.canBid((int)minBid) && rc.getRoundNum() > 250) {
			if(rc.getRoundNum()%3 == 0 && rc.canBid((int) (minBid/10))) rc.bid((int) (minBid/10));
            else rc.bid((int)minBid);
        }else{
            cantVote = true;
            if(rc.getRoundNum() > 250 && rc.canBid(2)) rc.bid(2);
        }
        prevVote = rc.getTeamVotes();
        prevInf = rc.getInfluence();
    }
    
    static int minDistToEC() throws GameActionException {
		int min = 1000000000;
		MapLocation m = rc.getLocation();
		for(int i = 0; i < 20; i++) {
			if(ec[i][0] == -1) return min;
			min = Math.min(min, m.distanceSquaredTo(convertLocation(ec[i])));
		}
		
		return min;
	}
    
    static void buildSlanderer(int inf) throws GameActionException {
		int amt = 0;
		for(int slan : goodValues) if(slan < inf) amt = slan;
		for(int i = 0; i < 8; i++) {
			Direction dir = directions[i];
			if(rc.canBuildRobot(RobotType.SLANDERER, dir, amt)) {
				rc.buildRobot(RobotType.SLANDERER, dir, amt);
				rbid[sz++] = rc.senseRobotAtLocation(rc.adjacentLocation(dir)).getID();
				break;
			}
		}
	}
	
	static void buildMuckraker() throws GameActionException {
		for(int i = 0; i < 8; i++) {
			Direction dir = directions[i];
			if(rc.canBuildRobot(RobotType.MUCKRAKER, dir, 1+rc.getInfluence()/1000)) {
				rc.buildRobot(RobotType.MUCKRAKER, dir, 1+rc.getInfluence()/1000);
				rbid[sz++] = rc.senseRobotAtLocation(rc.adjacentLocation(dir)).getID();
				break;
			}
		}
	}
	
	static void buildAtkMuckraker() throws GameActionException {
		for(int i = 0; i < 8; i++) {
			Direction dir = directions[i];
			if(rc.canBuildRobot(RobotType.MUCKRAKER, dir, 40)) {
				rc.buildRobot(RobotType.MUCKRAKER, dir, 40);
				rbid[sz++] = rc.senseRobotAtLocation(rc.adjacentLocation(dir)).getID();
				break;
			}
		}
	}
	
	static void buildDefPolitician() throws GameActionException {
		for(int i = 0; i < 8; i++) {
			Direction dir = directions[i];
			if(rc.canBuildRobot(RobotType.POLITICIAN, dir, Math.min(40, DEF + ((rc.getInfluence()/100)/2)*2))){
				rc.buildRobot(RobotType.POLITICIAN, dir, Math.min(40, DEF + ((rc.getInfluence()/100)/2)*2));
				rbid[sz++] = rc.senseRobotAtLocation(rc.adjacentLocation(dir)).getID();
				break;
			}
		}
	}
	
	static void buildEscortPolitician() throws GameActionException {
		for(int i = 0; i < 8; i++) {
			Direction dir = directions[i];
			if(rc.canBuildRobot(RobotType.POLITICIAN, dir, ESCORT+((rc.getInfluence()/50)/2)*2)) {
				rc.buildRobot(RobotType.POLITICIAN, dir, ESCORT+((rc.getInfluence()/50)/2)*2);
				rbid[sz++] = rc.senseRobotAtLocation(rc.adjacentLocation(dir)).getID();
				break;
			}
		}
	}
	
	static void buildAtkPolitician() throws GameActionException {
		for(int i = 0; i < 8; i++) {
			Direction dir = directions[i];
			if(rc.canBuildRobot(RobotType.POLITICIAN, dir, 200+((rc.getInfluence()/10)))) {
				rc.buildRobot(RobotType.POLITICIAN, dir, 200+(rc.getInfluence()/10));
				rbid[sz++] = rc.senseRobotAtLocation(rc.adjacentLocation(dir)).getID();
				break;
			}
		}
	}
    
    static void getFlagInfo() throws GameActionException {
        for(int j = 0; j < sz; j++){
			if(Clock.getBytecodesLeft() < 1800) return;
            int i = rbid[j];
            if (i != 0 && rc.canGetFlag(i)){
                oFlag = rc.getFlag(i);
                if(oFlag%32==1){
                    addECFlag(oFlag);
                    removeNEC(oFlag);
                }else if(oFlag%32==2){
                    addNECFlag(oFlag);
                }else if(oFlag%32==3){
                    removeNEC(oFlag);
                    removeEC(oFlag);
                } else if(oFlag%32 == 4) {
					int[] enemyLocation = {oFlag/(1 << 17), (oFlag/(1 << 10))%(1 << 7)};
					sland = convertLocation(enemyLocation);
				} else if(oFlag%32 == 5) {
					int[] enemyLocation = {oFlag/(1 << 17), (oFlag/(1 << 10))%(1 << 7)};
					ene = convertLocation(enemyLocation);
				} else if(oFlag%32 == 6) {
					int[] enemyLocation = {oFlag/(1 << 17), (oFlag/(1 << 10))%(1 << 7)};
					MapLocation t = convertLocation(enemyLocation);
					if(t.equals(sland)) sland = null;
				}
            }
            
            else rbid[j] = 0;
        }
    }
    static void removeEC(int f) throws GameActionException {
        for(int i = 0; i < 20; i++) {
            if(ec[i][0] == f/(1 << 17) && ec[i][1] == (f%(1 << 17))/(1 << 10)) {
                for(int j = i+1; j < 19; j++) {
                    ec[j-1][0] = ec[j][0];
                    ec[j-1][1] = ec[j][1];
                    ec[j-1][2] = ec[j][2];
                }
            }
        }
    }
    static void removeNEC(int f) throws GameActionException {
		//if(f%32 == 3) System.out.println(f/(1 << 17) + " " + (f%(1 << 17))/(1 << 10) + " " + f);
        for(int i = 0; i < 20; i++) {
            if(nec[i][0] == f/(1 << 17) && nec[i][1] == (f%(1 << 17))/(1 << 10)) {
                for(int j = i+1; j < 19; j++) {
                    nec[j-1][0] = nec[j][0];
                    nec[j-1][1] = nec[j][1];
                    nec[j-1][2] = nec[j][2];
                }
            }
        }
    }
    static void putFlagInfo() throws GameActionException {
        if(starting){
            flag = 17; 
        }
        
        else {
			int ind = 0;
            double mini = 10000000;
			boolean neutral = false;
			int index = 0;
			while(nec[index][0] != -1 && turnCount%2 == 0) {
				if(nec[index][2]*(1+rc.getLocation().distanceSquaredTo(convertLocation(nec[index]))/100.0) < mini) {
					mini = nec[index][2]*(1+rc.getLocation().distanceSquaredTo(convertLocation(nec[index]))/100.0);
					ind = index;
					neutral = true;
				}
				
				index++;
			}
			
			index = 0;
			while(ec[index][0] != -1 && turnCount%2 == 0) {
				if(2*ec[index][2]*(1+rc.getLocation().distanceSquaredTo(convertLocation(ec[index]))/100.0) < mini) {
					mini = 2*ec[index][2]*(1+rc.getLocation().distanceSquaredTo(convertLocation(ec[index]))/100.0);
					ind = index;
					neutral = false;
				}
				
				index++;
			}
			
			if(mini != 10000000) {
				if(neutral) flag(2, convertLocation(nec[ind]));
				else flag(1, convertLocation(ec[ind]));
			} else if(sland != null) flag(4, sland);
			else if(ene != null) flag(5, ene);
		}
    }

    static void runPolitician() throws GameActionException {
		if(rc.getRoundNum() > 1490 && rc.canEmpower(-1)) rc.empower(-1);
        flag = 0;
        oFlag = 0;
        Team enemy = rc.getTeam().opponent();
        if(turnCount == 1) cDir = ((int) (Math.random()*100000))%8;
        int actionRadius = rc.getType().actionRadiusSquared;
        getECID();
        if(rc.canGetFlag(ecid)){
            oFlag = rc.getFlag(ecid);
        }

        neutral();
        if(flag == 0) addEC();
        if(flag == 0) checkConquered();

        if(rc.getInfluence()%2==1 && rc.getInfluence()<100){
            escort();
        } 
        else if(rc.getInfluence()%2==0 && rc.getInfluence()<100){ //this prob wont be the condition but make it somehow realize its defensive
            patrol(30);
        }
        else {
            if(oFlag%32 == 1) addECFlag(oFlag);
            if(oFlag%32 == 2) addNECFlag(oFlag);
            
            if(searchNeutral(5)||searchEnemy(RobotType.ENLIGHTENMENT_CENTER, 5)) turns++;
			
			if(turnCount > 500 && rc.canEmpower(9)) rc.empower(9);
			
            if(turns > 20 || (rc.getRoundNum() > 1000 && turns > 10)) 
                if(rc.canEmpower(5)) rc.empower(5);

            for(int i = 0; i < 4; i++) {
                if(rc.onTheMap(rc.adjacentLocation(directions[2*i])) && 
                    rc.isLocationOccupied(rc.adjacentLocation(directions[2*i])) &&
                    !rc.senseRobotAtLocation(rc.adjacentLocation(directions[2*i])).getTeam().equals(rc.getTeam()) && 
                    rc.senseRobotAtLocation(rc.adjacentLocation(directions[2*i])).type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                    next++;
                    turns = 0;
                    flag(16, rc.getLocation());
                    int tot = 0;
                    for(int j = 0; j < 4; j++) 
                        if(!rc.onTheMap(rc.adjacentLocation(directions[2*j])) || !rc.isLocationOccupied(rc.adjacentLocation(directions[2*j]))) tot++;
                    
                    if((next > 30 || tot == 3) && rc.canEmpower(1)) rc.empower(1);
                    if(tot == 2 && next > 10 && rc.canEmpower(1)) rc.empower(1);
                }
            }
            
            rc.setFlag(flag);
            if(next > 0) return;
			
            if(nec[0][0] != -1) moveGood(convertLocation(nec[0]));
            else if(ec[0][0] != -1) moveGood(convertLocation(ec[0]));
            else moveGood(cDir);
        }
    }
    //Stay within a certain radius of base
            //When attack flag, start heading to coords
            //Signal if found base or found nuke
            //Explode if >4 muckrakers nearby or surrounded, ignore otherwise
            //Once at enemy base, wait next to it and explode if a politician is seen
            //When nuke is in range, it signals its target spot, explodes the three relevant
    static void escort() throws GameActionException {
        if(oFlag%32==2){
            //neutral
            nATK = true;
            atkEC = false;
            addNECFlag(oFlag);
        }
        if(oFlag%32==1){
            //enemy
            atkEC = true;
            addECFlag(oFlag);
        }
        if(noEnemies()){ 
            patrol(30);
            return;
        } 
        if(searchNeutral(9)||searchEnemy(RobotType.ENLIGHTENMENT_CENTER, 9)){
            atBase = true; 
        }
        
        if(atBase){
            if(searchEnemy(RobotType.POLITICIAN, -1)||searchSignal(16)){
                if(rc.canEmpower(-1)){
                    rc.empower(-1);
                }
            }
        }
        
        if(searchEnemyCount(RobotType.MUCKRAKER, 9) > 2){
            if(rc.canEmpower(9)) rc.empower(9);
        }

        if(!atBase) {
            if(ec[0][0]>-1){
                move(convertLocation(ec[0]));
            }
        }else{
            cDir = ((int) (Math.random()*100000))%8;
            move();
        }
    }

    static void patrol(int r2) throws GameActionException {
        
        if(turnCount == 1){
            getSpawnLoc();
            getECID();
        }
        setDefensiveFlag();
        defendBase();
        circleBase(RobotType.POLITICIAN, 1, 49);
        /*
        if(home==null){
            if(rc.canEmpower(1))
                rc.empower(1);
            return;
        }
        //System.out.println(home.x + " " + home.y);
        if(searchEnemy(RobotType.MUCKRAKER, 4)){
            if(rc.canEmpower(4))
                rc.empower(4);
        }
        for(RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam().opponent())){
            if(robot.type.equals(RobotType.MUCKRAKER))
                move(robot.getLocation());
        }
        int minDist = 100000;
        int bestDir = 0;
        int radiusMin = 30, radiusMax = 50;
        int[] bestChoices = {0, 1 , -1, 2, -2, 3, -3, 4};
        
        for(int i = 0; i < 8; i++){
            MapLocation temp = rc.adjacentLocation(directions[(8+cDir+bestChoices[i])%8]);
            if(rc.onTheMap(temp)){
                int distToHome = distFromRange(temp, home, radiusMin, radiusMax);
                if(distToHome != 0){
                    if(distToHome < minDist){
                        minDist = distToHome;
                        bestDir = (8+cDir+bestChoices[i])%8;
                    }
                    //System.out.println(home.x + " " + home.y);
                }
                else{
                    bestDir = (8+cDir+bestChoices[i])%8;
                    break;
                }
            }
        }
        cDir = bestDir;
        move();*/
        
        
        //includes circling
        //if they're lost they should pathfind back to base
    }

    static int distFromRange(MapLocation consider, MapLocation loc, int min, int max) throws GameActionException {
        int num = consider.distanceSquaredTo(loc);
        if(num < min) return min-num;
        if(num > max) return num-max;
        return 0;
    }
    static boolean searchSignal(int signal) throws GameActionException {
        for(RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam())){
            if(rc.canGetFlag(robot.getID())){
                if(rc.getFlag(robot.getID())%32==signal)
                    return true;
            }
        }
        return false;
    }
    static int searchEnemyCount(RobotType type, int radius) throws GameActionException {
        int ccount = 0;
        for(RobotInfo robot : rc.senseNearbyRobots(radius, rc.getTeam().opponent())){
            if(robot.type.equals(type))
                ccount++;
        }
        return ccount;
    }
    static boolean searchSignal(int signal, int radius) throws GameActionException {
        for(RobotInfo robot : rc.senseNearbyRobots(radius, rc.getTeam())){
            if(rc.canGetFlag(robot.getID())){
                if(rc.getFlag(robot.getID())%32==signal)
                    return true;
            }
        }
        return false;
    } 
    static boolean searchEnemy(RobotType type, int radius) throws GameActionException {
        for(RobotInfo robot : rc.senseNearbyRobots(radius, rc.getTeam().opponent())){
            if(robot.type.equals(type))
                return true;
        }
        return false;
    }
    static boolean searchNeutral(int radius) throws GameActionException {
        for(RobotInfo robot : rc.senseNearbyRobots(radius, Team.NEUTRAL)){
            return true;
        }
        return false;
    }
    static int searchEC(int radius) throws GameActionException {
        for(RobotInfo robot : rc.senseNearbyRobots(radius, rc.getTeam())){
            if(robot.type.equals(RobotType.ENLIGHTENMENT_CENTER))
                return robot.getID();
        }
        for(RobotInfo robot : rc.senseNearbyRobots(radius, rc.getTeam().opponent())){
            if(robot.type.equals(RobotType.ENLIGHTENMENT_CENTER))
                return robot.getID();
        }
        for(RobotInfo robot : rc.senseNearbyRobots(radius, Team.NEUTRAL)){
            if(robot.type.equals(RobotType.ENLIGHTENMENT_CENTER))
                return robot.getID();
        }
        return 0;
    }
    static void addECFlag(int f) throws GameActionException {
        for(int i = 0; i<20; i++){
            if(ec[i][0] == f/(1 << 17) && ec[i][1] == (f%(1 << 17))/(1 << 10)) {
                ec[i][2] = (f/(1 << 5))%(1 << 5);
                break;
            }
            if(ec[i][0]==-1&&ec[i][1]==-1){
                ec[i][0] = f/(1 << 17);
                ec[i][1] = (f/(1<<10))%(1 << 7);
                ec[i][2] = (f/(1 << 5))%(1 << 5);
                break;
            }
        }
    }
    static void addNECFlag(int f) throws GameActionException {
        for(int i = 0; i<20; i++){
            if(nec[i][0] == f/(1 << 17) && nec[i][1] == (f%(1 << 17))/(1 << 10)) {
                nec[i][2] = (f/(1 << 5))%(1 << 5);
                break;
            }
            if(nec[i][0]==-1&&nec[i][1]==-1){
                nec[i][0] = f/(1 << 17);
                nec[i][1] = (f/(1<<10))%(1 << 7);
                nec[i][2] = (f/(1 << 5))%(1 << 5);
                break;
            }
        }
    }
    static boolean noEnemies() throws GameActionException {
        return (ec[0][0]== -1 && ec[0][1] == -1);
    }

    static void runSlanderer() throws GameActionException {/*
		if(turnCount == 1) cDir = ((int) (Math.random()*100000))%8;
        int actionRadius = rc.getType().actionRadiusSquared;
        getECID();
		int minDist = 100000;
        int bestDir = 0;
        int radiusMin = 0, radiusMax = 15;
        int[] bestChoices = {0, 1 , -1, 2, -2, 3, -3, 4};
        
        for(int i = 0; i < 8; i++){
            MapLocation temp = rc.adjacentLocation(directions[(8+cDir+bestChoices[i])%8]);
            int distToHome = distFromRange(temp, home, radiusMin, radiusMax);
            if(distToHome != 0){
                if(distToHome < minDist){
					minDist = distToHome;
					bestDir = (8+cDir+bestChoices[i])%8;
                }
                //System.out.println(home.x + " " + home.y);
            }
            else{
                bestDir = (8+cDir+bestChoices[i])%8;
                break;
            }
        }
        cDir = bestDir;
        move();*/
        if(turnCount == 1){
    		getECID();
    		getSpawnLoc();
    	}
        moveSlanderer();
        if(rc.getLocation().distanceSquaredTo(spawnLoc) > maxDistFromBase){
        	maxDistFromBase = rc.getLocation().distanceSquaredTo(spawnLoc);
        }
    }

    static void runMuckraker() throws GameActionException {
        flag = 0;
        if(turnCount == 1) cDir = ((int) (Math.random()*100000))%8;
        if(turnCount == 1) atkEC = (rc.getID()%3 == 0);
        if(rc.getInfluence() > 1) atkEC = true;
        if(rc.getRoundNum() > 200) atkEC = true;
        if(rc.canGetFlag(ecid)) oFlag = rc.getFlag(ecid);
        
        if(!rc.canGetFlag(ecid)) ecid = 0;
        getECID();
        if(turnCount == 1 && rc.canGetFlag(ecid) && rc.getFlag(ecid)%(1 << 5) == 17) 
            for(int i = 0; i < 8; i++)  
                if(rc.onTheMap(rc.adjacentLocation(directions[i])) && 
                rc.isLocationOccupied(rc.adjacentLocation(directions[i])) &&
                rc.senseRobotAtLocation(rc.adjacentLocation(directions[i])).getTeam().equals(rc.getTeam()) && 
                rc.senseRobotAtLocation(rc.adjacentLocation(directions[i])).type.equals(RobotType.ENLIGHTENMENT_CENTER))
                    cDir = (i+4)%8;

        boolean found = false;
        for(RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam().opponent())) {
            if(robot.type.canBeExposed()) {
                if(rc.canExpose(robot.location)) rc.expose(robot.location);
                else {
                    found = true;
                    cDir = dirToInt(rc.getLocation().directionTo(robot.location));
                }
            }
        }
        
        if(oFlag%32 == 1) addECFlag(oFlag);
        if(oFlag%32 == 4) {
			int[] arr = {oFlag/(1 << 17), (oFlag/(1 << 10))%(1 << 7)};
			sland = convertLocation(arr);
		}
		
		if(oFlag%32 == 5) {
			int[] arr = {oFlag/(1 << 17), (oFlag/(1 << 10))%(1 << 7)};
			ene = convertLocation(arr);
		}
		
		if(ene != null && rc.getLocation().distanceSquaredTo(ene) < 20) ene = null;
		if(sland != null && rc.getLocation().distanceSquaredTo(sland) < 20 && !found) {
			flag(6, sland);
			sland = null;
		}

        neutral();
        if(flag == 0) addEC();
        if(flag == 0) checkConquered();
        if(flag == 0) enemies();
		
        if(found) moveGood(cDir);
        
        if(rc.senseNearbyRobots(10, rc.getTeam()).length > 10) {
			ene = null;
			sland = null;
			separate();
		}
        

        if(atkEC && sland != null) moveGood(sland);
        //else if(atkEC && ene != null) move(ene);
        /*else if(atkEC && ec[0][0] != -1 && !searchEnemy(RobotType.ENLIGHTENMENT_CENTER, 20)) move(convertLocation(ec[0]));
		else if(searchEnemy(RobotType.ENLIGHTENMENT_CENTER, 20)) {
			cDir = ((int) (Math.random()*100000))%8;
			move();
		}*/ else if(rc.getRoundNum() > 100) separate2();
		else moveGood(cDir);
        rc.setFlag(flag);
    }
    
    static void separate2() throws GameActionException {
		int[] min = {0, 0, 0};
		for(RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam())) {
			for(int i = -1; i < 2; i++) {
				if(!rc.onTheMap(rc.adjacentLocation(directions[(i+cDir+8)%8]))) continue;
				min[i+1] += rc.adjacentLocation(directions[(i+cDir+8)%8]).distanceSquaredTo(robot.getLocation());
			}
		}
		
		for(int i = -1; i < 2; i++) 
			if(!rc.onTheMap(rc.adjacentLocation(directions[(i+cDir+8)%8]).add(directions[(i+cDir+8)%8]).add(directions[(i+cDir+8)%8]))) min[i+1] -= 100;
		
		if(rc.senseNearbyRobots(-1, rc.getTeam()).length == 0) {
			moveGood(cDir);
			return;
		}
		
		int mini = min[0];
		int railgun = 0;
		for(int i = 0; i < 3; i++) {
			if(mini < min[i] && rc.onTheMap(rc.adjacentLocation(directions[(i+cDir+8)%8]))) {
				railgun = i;
				mini = min[i];
			}
		}
		
		cDir = (cDir+railgun+7)%8;
		moveGood(cDir);
	}
    
    static void separate() throws GameActionException {
		int[] min = {0, 0, 0, 0, 0, 0, 0, 0};
		for(RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam())) {
			for(int i = 0; i < 8; i++) {
				if(!rc.onTheMap(rc.adjacentLocation(directions[i]))) continue;
				min[i] += rc.adjacentLocation(directions[i]).distanceSquaredTo(robot.getLocation());
			}
		}
		
		for(int i = 0; i < 8; i++) 
			if(!rc.onTheMap(rc.adjacentLocation(directions[i]).add(directions[i]).add(directions[i]))) min[i] -= 100;
		
		if(rc.senseNearbyRobots(-1, rc.getTeam()).length == 0) {
			moveGood(cDir);
			return;
		}
		
		int mini = min[0];
		int railgun = 0;
		for(int i = 0; i < 8; i++) {
			if(mini < min[i] && rc.onTheMap(rc.adjacentLocation(directions[i]))) {
				railgun = i;
				mini = min[i];
			}
		}
		
		cDir = railgun;
		moveGood(cDir);
	}

    static void addEC() throws GameActionException {
        for(RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam().opponent())) {
            if(robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                for(int i = 0; i < 20; i++) {
                    if(ec[i][0] == -1) {
                        ec[i][0] = robot.location.x%128;
                        ec[i][1] = robot.location.y%128;
                        flag(1, robot.conviction, robot.location);
                        break;
                    }

                    else if(ec[i][0] == robot.location.x%128 && ec[i][1] == robot.location.y%128) 
                        break;
                }
            }
        }
    }

    static void checkConquered() throws GameActionException {
        for(RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam())) {
            if(robot.type.equals(RobotType.ENLIGHTENMENT_CENTER) && robot.getID() != ecid) {
                flag(3, robot.location);
                for(int i = 0; i < 20; i++) {
                    if(ec[i][0] == robot.location.x%128 && ec[i][1] == robot.location.y%128) {
                        for(int j = i+1; j < 19; j++) {
                            ec[j-1][0] = ec[j][0];
                            ec[j-1][1] = ec[j][1];
                        }
                    }
                    
                    if(nec[i][0] == robot.location.x%128 && nec[i][1] == robot.location.y%128) {
						for(int j = i+1; j < 19; j++) {
                            nec[j-1][0] = nec[j][0];
                            nec[j-1][1] = nec[j][1];
                        }
                    }
                }
            }
        }               
    }

    static void neutral() throws GameActionException {
        for(RobotInfo robot : rc.senseNearbyRobots(-1, Team.NEUTRAL))
            if(robot.type.equals(RobotType.ENLIGHTENMENT_CENTER))
                flag(2, robot.conviction, robot.location);
    }
    
    static void enemies() throws GameActionException {
		for(RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam().opponent())) {
			if(robot.getType().equals(RobotType.SLANDERER)) {
				flag(4, robot.getLocation());
				break;
			} else if(robot.getType().equals(RobotType.POLITICIAN) && robot.getInfluence() < 50) flag(5, robot.getLocation());
		}
	}
    
    static void getECID() throws GameActionException {
        for(RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam())) {
            if(ecid == 0) {
                if(robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) {
                    ecid = robot.getID();
                    home = robot.location;
                }
            }
        }
    }
    
    static MapLocation convertLocation(int[] arr) throws GameActionException {
		int modcoord[] = {arr[0], arr[1]};
        int modmycoords[] = {rc.getLocation().x % 128, rc.getLocation().y % 128};
        if (Math.abs(modmycoords[0] - modcoord[0]) >= 64) {
            if (modmycoords[0] > modcoord[0])
                modcoord[0] += 128;
            else if (modmycoords[0] < modcoord[0])
                modcoord[0] -= 128;
        }
        
        if (Math.abs(modmycoords[1] - modcoord[1]) >= 64) {
            if (modmycoords[1] > modcoord[1])
                modcoord[1] += 128;
            else if (modmycoords[1] < modcoord[1])
                modcoord[1] -= 128;
        }
        
        return new MapLocation(rc.getLocation().x + modcoord[0] - modmycoords[0], rc.getLocation().y + modcoord[1] - modmycoords[1]);
    }
    
    static void move() throws GameActionException {
        int d = cDir;
        
        if(!rc.onTheMap(rc.adjacentLocation(directions[cDir]))) cDir = ((int) (Math.random()*100000))%8;
        
        if(rc.canMove(directions[d]))
            rc.move(directions[d]);
        else if(rc.canMove(directions[(d+1)%8]))
            rc.move(directions[(d+1)%8]);
        else if(rc.canMove(directions[(d+7)%8]))
            rc.move(directions[(d+7)%8]);
        else if(rc.canMove(directions[(d+2)%8]))
            rc.move(directions[(d+2)%8]);
        else if(rc.canMove(directions[(d+6)%8]))
            rc.move(directions[(d+6)%8]);
        else if(rc.canMove(directions[(d+3)%8]))
            rc.move(directions[(d+3)%8]);
        else if(rc.canMove(directions[(d+5)%8]))
            rc.move(directions[(d+5)%8]);
        else if(rc.canMove(directions[(d+6)%8]))
            rc.move(directions[(d+6)%8]);
        else if(rc.canMove(directions[(d+4)%8]))
            rc.move(directions[(d+4)%8]);
    }

    static void move(MapLocation m) throws GameActionException { //also add a pathfind variant
        int d = dirToInt(rc.getLocation().directionTo(m));
        
        if(rc.canMove(directions[d]))
            rc.move(directions[d]);
        else if(rc.canMove(directions[(d+1)%8]))
            rc.move(directions[(d+1)%8]);
        else if(rc.canMove(directions[(d+7)%8]))
            rc.move(directions[(d+7)%8]);
        else if(rc.canMove(directions[(d+2)%8]))
            rc.move(directions[(d+2)%8]);
        else if(rc.canMove(directions[(d+6)%8]))
            rc.move(directions[(d+6)%8]);
        else if(rc.canMove(directions[(d+3)%8]))
            rc.move(directions[(d+3)%8]);
        else if(rc.canMove(directions[(d+5)%8]))
            rc.move(directions[(d+5)%8]);
        else if(rc.canMove(directions[(d+6)%8]))
            rc.move(directions[(d+6)%8]);
        else if(rc.canMove(directions[(d+4)%8]))
            rc.move(directions[(d+4)%8]);
    }

    static void flag(int m, int health, MapLocation l) throws GameActionException {
        flag = m + (1 << 5)*Math.min(health/20, (1 << 5) - 1) + (1 << 10)*(l.y%128) + (1 << 17)*(l.x%128);
    }

    static void flag(int m, MapLocation l) throws GameActionException {
        flag = m + (1 << 10)*(l.y%128) + (1 << 17)*(l.x%128);
    }
    
    static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /*
    static void findRunPath(MapLocation enemyLoc) throws GameActionException{
        HashMap<MapLocation, Integer> hueristic = new HashMap<MapLocation, Integer>();

    }

    static void updateGraph() throws GameActionException{
    	int curx = rc.getLocation().x;
    	int cury = rc.getLocation().y;
    	for(int i = -7; i < 7; i++){
    		for(int j = -7; j < 7; j++){
    			MapLocation newloc = new MapLocation(curx + i, cury + j);
    			if(rc.canSenseLocation(newloc) && !graph.containsKey(newloc)){
    				graph.put(newloc, new Node(newloc.x, newloc.y));
    			}
    		}
    	}
    	for(int i = -7; i < 7; i++){
    		for(int j = -7; j < 7; j++){
    			MapLocation newloc = new MapLocation(rc.getLocation().x + i, rc.getLocation().y + j);
                Node cur = graph.get(newloc);
                double acd = (double)(rc.getType().actionCooldown);
                for(int k = 0; k < 8; k++){
                    MapLocation newlocadj = newloc.add(directions[k]);
                    if(cur.edges[k] == null && rc.canSenseLocation(newlocadj)){   				
                        cur.edges[k] = new Edge(acd/rc.sensePassability(newlocadj), graph.get(newlocadj));
                    }
                }
    		}
    	}
    }*/
    
    static int dirToInt(Direction d) {
        for (int i = 0; i < 8; i++) {
            if (directions[i].equals(d)) {
                return i;
            }
        }
        return -1;
    }

        static void hirePolice()  throws GameActionException {
    	int stLoc = (int)(Math.random()*8);
		for(int i = 0; i < 8; i++){
			if(rc.canBuildRobot(RobotType.POLITICIAN, directions[(stLoc+i)%8], DEF)){
				rc.buildRobot(RobotType.POLITICIAN, directions[(stLoc+i)%8], DEF);
				return;
			}
		}
	}	
	static void hireFarmer()  throws GameActionException {
    	int stLoc = (int)(Math.random()*8);
		for(int i = 0; i < 8; i++){
			if(rc.canBuildRobot(RobotType.SLANDERER, directions[(stLoc+i)%8], 130)){
				rc.buildRobot(RobotType.SLANDERER, directions[(stLoc+i)%8], 130);
				return;
			}
		}
	}
	
	static void hireMook()  throws GameActionException {
    	int stLoc = (int)(Math.random()*8);
		for(int i = 0; i < 8; i++){
			if(rc.canBuildRobot(RobotType.MUCKRAKER, directions[(stLoc+i)%8], 2)){
				rc.buildRobot(RobotType.MUCKRAKER, directions[(stLoc+i)%8], 2);
				return;
			}
		}
	}

    static void flag2(int m, int dir) throws GameActionException {
        flag = m + (1 << 5)*dir;
    }
	

    static void getSpawnLoc() throws GameActionException {
        for(RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) 
            if(spawnLoc == null) 
                if(robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) spawnLoc = robot.getLocation();
    }

    static boolean buildInnerPad() throws GameActionException {
        if (rc.canBuildRobot(RobotType.MUCKRAKER, directions[nextPadLocIndex], 1)){
            rc.buildRobot(RobotType.MUCKRAKER, directions[nextPadLocIndex], 1);
            innerDefenderID[nextPadLocIndex] = rc.senseRobotAtLocation(rc.getLocation().add(directions[nextPadLocIndex])).getID();
            flag2(12, nextPadLocIndex);
            rc.setFlag(flag);
            return true;
        }
        if (!rc.canSenseLocation(rc.getLocation().add(directions[nextPadLocIndex])) && rc.canBuildRobot(RobotType.MUCKRAKER, directions[nextPadLocIndex].rotateLeft().rotateLeft(), 1)){
            rc.buildRobot(RobotType.MUCKRAKER, directions[nextPadLocIndex].rotateLeft().rotateLeft(), 1);
            innerDefenderID[nextPadLocIndex] = rc.senseRobotAtLocation(rc.getLocation().add(directions[nextPadLocIndex].rotateLeft().rotateLeft())).getID();
            flag2(12, nextPadLocIndex);
            rc.setFlag(flag);
            return true;
        }
        return false;
    }

    static boolean isInnerPadIncomplete() throws GameActionException {
    	outerloop:
    	for(int i = 0; i < 8; i+=2){
    		MapLocation potentialChg = new MapLocation(rc.getLocation().x + innerPadLocs[i][0], rc.getLocation().y + innerPadLocs[i][1]);
    		if(rc.canSenseLocation(potentialChg) && rc.senseRobotAtLocation(potentialChg) != null && rc.senseRobotAtLocation(potentialChg).getID() != innerDefenderID[i] && rc.getFlag(rc.senseRobotAtLocation(potentialChg).getID())%32 == 11 && rc.senseRobotAtLocation(potentialChg).getTeam().equals(rc.getTeam())){
    			int newID = rc.senseRobotAtLocation(potentialChg).getID();
    			for(int j = 0; j < 8; j++){
    				if(innerDefenderID[j] == newID){
    					innerDefenderID[j] = 0;
    					break;
    				}
    			}
    			innerDefenderID[i] = newID;
    			
    		}
    	}
        Team allied = rc.getTeam();
        RobotInfo[] inRange = rc.senseNearbyRobots(-1, allied);
        for (int i = 0; i < 8; i++) {
            if (innerDefenderID[i] == 0) {
                MapLocation nextPadLoc = new MapLocation(rc.getLocation().x + innerPadLocs[i][0],
             rc.getLocation().y + innerPadLocs[i][1]);
                if (rc.canSenseLocation(nextPadLoc) && (rc.canBuildRobot(RobotType.MUCKRAKER, directions[i], 1) || rc.canBuildRobot(RobotType.MUCKRAKER, directions[(i+6)%8], 1))){
                    nextPadLocIndex = i;
                    return true;
                }
                else {
                    continue;
                }
            }
            else {
                boolean alive = false;
                for (RobotInfo robot : inRange) {
                    if (robot.getID() == innerDefenderID[i]) {
                        alive = true;
                        break;
                    }
                }
                if (!alive && (rc.canBuildRobot(RobotType.MUCKRAKER, directions[i], 1) || rc.canBuildRobot(RobotType.MUCKRAKER, directions[(i+6)%8], 1))) {
                    nextPadLocIndex = i;
                    return true;
                }
            }
        }
        return false;
    }

    static void getInnerPadDest() throws GameActionException {
        MapLocation curLoc = rc.getLocation();
        for (int i = 0; i < 8; i++){
        	MapLocation potentialECLoc = curLoc.subtract(directions[i]); 
            if(rc.canSenseLocation(potentialECLoc) && rc.senseRobotAtLocation(potentialECLoc) != null && rc.senseRobotAtLocation(potentialECLoc).getType().canBid()){
                mPadID = i;
                dest = new MapLocation(spawnLoc.x + innerPadLocs[mPadID][0],
             spawnLoc.y + innerPadLocs[mPadID][1]);
                return;
            }
        }
    }

    
    static void circleBase(RobotType rt, int penalty, int minDist) throws GameActionException {
    	MapLocation currentLoc = rc.getLocation();
        Direction dir = currentLoc.directionTo(spawnLoc);
        int largestMin = 0;
        Direction bestOption = null;
        RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
        int numfriendlies = 0;
        int stLoc = (int)(Math.random()*8);
        for(int i = 0; i < 8; i++){
        	MapLocation newLoc = rc.getLocation().add(directions[(stLoc+i)%8]);
        	numfriendlies = 0;
        	if(!rc.canSenseLocation(newLoc) || !rc.canMove(directions[(stLoc+i)%8])){
        		continue;
        	}
        	int minDistanceFromPolitician = 1000;
            int minDistanceFromAlly = 1000;
            
		    for (RobotInfo robot : allies) {
                int distToAlly = newLoc.distanceSquaredTo(robot.getLocation());
		        if (robot.type.equals(rt) && distToAlly < minDistanceFromPolitician) {
		                minDistanceFromPolitician = newLoc.distanceSquaredTo(robot.getLocation());
		        }
		        if (robot.type.equals(rt) && ((robot.getInfluence()%2 == 0 && robot.getInfluence() < 100) || robot.getInfluence() == 2)) {
		                numfriendlies++;
		        }
                if(distToAlly < minDistanceFromAlly){
                    minDistanceFromAlly = distToAlly;
                }
		    }
		    if(minDistanceFromPolitician > largestMin && minDistanceFromAlly <= 20){
		    	largestMin = minDistanceFromPolitician;
		    	bestOption = directions[(stLoc+i)%8];
		    }
		}
        if(bestOption == null){
        	move(spawnLoc);
        	return;
        }
        MapLocation newLoc = rc.getLocation().add(bestOption);
        if (rc.canMove(bestOption) && (spawnLoc.distanceSquaredTo(newLoc) < ((int)Math.sqrt(rc.getLocation().distanceSquaredTo(spawnLoc)) + numfriendlies - penalty)*((int)Math.sqrt(rc.getLocation().distanceSquaredTo(spawnLoc))+1) || spawnLoc.distanceSquaredTo(newLoc) < minDist)) {
            rc.move(bestOption);
            return;
        }
        bestOption = bestOption.rotateLeft();
        newLoc = rc.getLocation().add(bestOption);
        if (rc.canMove(bestOption) && (spawnLoc.distanceSquaredTo(newLoc) < ((int)Math.sqrt(rc.getLocation().distanceSquaredTo(spawnLoc)) + numfriendlies - penalty)*((int)Math.sqrt(rc.getLocation().distanceSquaredTo(spawnLoc))+1) || spawnLoc.distanceSquaredTo(newLoc) < minDist)) {
            rc.move(bestOption);
            return;
        }
        bestOption = bestOption.rotateRight();
        bestOption = bestOption.rotateRight();
        newLoc = rc.getLocation().add(bestOption);
        if (rc.canMove(bestOption) && (spawnLoc.distanceSquaredTo(newLoc) < ((int)Math.sqrt(rc.getLocation().distanceSquaredTo(spawnLoc)) + numfriendlies - penalty)*((int)Math.sqrt(rc.getLocation().distanceSquaredTo(spawnLoc))+1) || spawnLoc.distanceSquaredTo(newLoc) < minDist)) {
            rc.move(bestOption);
            return;
        }
        move(spawnLoc);
    }

    static void defendBase() throws GameActionException {
        MapLocation currentLoc = rc.getLocation();
        for (int r = 1; r < 8; r++) {
            int total = rc.senseNearbyRobots(r).length;
            int enemyPoliCount = 0;
            for (RobotInfo robot : rc.senseNearbyRobots(r, rc.getTeam().opponent())){
                if (robot.type.equals(RobotType.POLITICIAN)){
                    enemyPoliCount += 1;
                }
            }
            
            
            if ((enemyPoliCount > 1) && rc.canEmpower(r)) {
                rc.empower(r);
                return;
            }
        }
        for (int r = 10; r > 0; r--) {
            int total = rc.senseNearbyRobots(r).length;
            int minEnemyMuckrakerHp = 10000000;
            int enemyMuckrakerCount = 0;
            for (RobotInfo robot : rc.senseNearbyRobots(r, rc.getTeam().opponent())){
                if (robot.type.equals(RobotType.MUCKRAKER)){
                    enemyMuckrakerCount++;
                    if(robot.getConviction() < minEnemyMuckrakerHp){
                    	minEnemyMuckrakerHp = robot.getConviction();
                    }
                }
            }
            if (enemyMuckrakerCount > 0 && (rc.getInfluence() - 10) / total > minEnemyMuckrakerHp && rc.canEmpower(r)) {
				rc.empower(r);
                return;
            }
        }
        
        for (int r = 6; r > 0; r--) {
            int total = rc.senseNearbyRobots(r).length;
            int enemyMuckrakerCount = 0;
            for (RobotInfo robot : rc.senseNearbyRobots(r, rc.getTeam().opponent())){
                if (robot.type.equals(RobotType.MUCKRAKER)){
                    enemyMuckrakerCount++;
                }
            }
            if (enemyMuckrakerCount > 0 && (rc.getInfluence() - 10) / total > 0 && rc.canEmpower(r)) {
                rc.empower(r);
                return;
            }
        }
        for (int r = 1; r < 10; r++) {
            for (RobotInfo robot : rc.senseNearbyRobots(r, rc.getTeam().opponent())) {
                Direction dir = currentLoc.directionTo(robot.getLocation());
                if (robot.type.equals(RobotType.MUCKRAKER) && rc.canMove(dir) && spawnLoc.distanceSquaredTo(currentLoc.add(dir)) < (int)(1.1D*rc.getLocation().distanceSquaredTo(spawnLoc))) {
                    rc.move(dir);
                    return;
                }
                else if (robot.type.equals(RobotType.MUCKRAKER) && rc.canMove(dir.rotateLeft()) && spawnLoc.distanceSquaredTo(currentLoc.add(dir.rotateLeft())) < (int)(1.1D*rc.getLocation().distanceSquaredTo(spawnLoc))) {
                    rc.move(dir.rotateLeft());
                    return;
                }
                else if (robot.type.equals(RobotType.MUCKRAKER) && rc.canMove(dir.rotateRight()) && spawnLoc.distanceSquaredTo(currentLoc.add(dir.rotateRight())) < (int)(1.1D*rc.getLocation().distanceSquaredTo(spawnLoc))) {
                    rc.move(dir.rotateRight());
                    return;
                }
            }
        }
    }

    static boolean detectMuck() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        for (RobotInfo robot : rc.senseNearbyRobots(-1, enemy)) {
            if (robot.type.canExpose()) {
                return true;
            }
        }
        return false;
    }
    
    static boolean detectPoli() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        for (RobotInfo robot : rc.senseNearbyRobots(-1, enemy)) {
            if (robot.type.canEmpower()) {
                return true;
            }
        }
        return false;
    }
    
    static boolean detectEnemy() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        RobotInfo[] ri = rc.senseNearbyRobots(-1, enemy);
		return ri.length != 0;
    }
    
    static void block() throws GameActionException {
    	MapLocation currentLoc = rc.getLocation();
    	RobotInfo closest = null;
    	int minDS = 10000;
        for (RobotInfo robot : rc.senseNearbyRobots(-1, rc.getTeam().opponent())){
            if (robot.type.equals(RobotType.POLITICIAN) && currentLoc.distanceSquaredTo(robot.getLocation()) < minDS){
                closest  = robot;
                minDS = currentLoc.distanceSquaredTo(robot.getLocation());
            }
        }
        if(closest == null){
        	return;
        }
        MapLocation enemyLoc = closest.getLocation();
        int initDir = dirToInt(enemyLoc.directionTo(spawnLoc)) + 8;
		for(int i = 1; i < 9; i++){
		    MapLocation tryLoc = enemyLoc.add(directions[(initDir + (i/2)*(int)Math.pow(-1, i))%8]);
		    if(rc.canSenseLocation(tryLoc) && rc.senseRobotAtLocation(tryLoc) == null && tryLoc.distanceSquaredTo(spawnLoc) < maxDistFromBase){
		    	move(tryLoc);
		    }
		}
    }
    
    static boolean detectHotDog() throws GameActionException {
        Team ally = rc.getTeam();
        RobotInfo[] friendlies = rc.senseNearbyRobots(-1, ally);
        for(RobotInfo robot : friendlies){
            if((robot.getInfluence() % 2 == 0 || robot.getType().canBeExposed()) && rc.getFlag(robot.getID())/32 >= 13){
                return true;
            }
        }
        return false;
    }
    
    static void flagMP(int m, int p) throws GameActionException {
        flag = m + (1 << 5)*p;
    }
    
    static void flagMPL(int m, int p, MapLocation l) throws GameActionException {
        flag = m + (1 << 5)*p + (1 << 10)*(l.y%128) + (1 << 17)*(l.x%128);
    }
    
    static void staySafe(RobotType rt, int HP, int penalty, int minDist) throws GameActionException {
    	MapLocation currentLoc = rc.getLocation();
        Direction dir = currentLoc.directionTo(spawnLoc);
        int largestMin = 0;
        Direction bestOption = null;
        RobotInfo[] allies = rc.senseNearbyRobots(-1, rc.getTeam());
        int numfriendlies = 0;
        int stLoc = (int)(Math.random()*8);
        for(int i = 0; i < 8; i++){
        	MapLocation newLoc = rc.getLocation().add(directions[(stLoc+i)%8]);
        	numfriendlies = 0;
        	if(!rc.canSenseLocation(newLoc) || !rc.canMove(directions[(stLoc+i)%8])){
        		continue;
        	}
        	int minDistanceFromPolitician = 1000;
            int minDistanceFromAlly = 1000;
            
		    for (RobotInfo robot : allies) {
                int distToAlly = newLoc.distanceSquaredTo(robot.getLocation());
		        if (robot.type.equals(rt) && (robot.getInfluence() % 2 == 1 || robot.getInfluence() > 100) && distToAlly < minDistanceFromPolitician) {
		                minDistanceFromPolitician = newLoc.distanceSquaredTo(robot.getLocation());
		        }
		        if (robot.type.equals(rt) && (robot.getInfluence() % 2 == 1 || robot.getInfluence() > 100)) {
		                numfriendlies++;
		        }
                if(distToAlly < minDistanceFromAlly){
                    minDistanceFromAlly = distToAlly;
                }
		    }
		    if(minDistanceFromPolitician > largestMin && minDistanceFromAlly <= 20){
		    	largestMin = minDistanceFromPolitician;
		    	bestOption = directions[(stLoc+i)%8];
		    }
		}
        if(bestOption == null){
        	move(spawnLoc);
        	return;
        }
        MapLocation newLoc = rc.getLocation().add(bestOption);
        if (rc.canMove(bestOption) && (spawnLoc.distanceSquaredTo(newLoc) < ((int)Math.sqrt(rc.getLocation().distanceSquaredTo(spawnLoc)) + numfriendlies - penalty)*((int)Math.sqrt(rc.getLocation().distanceSquaredTo(spawnLoc))+1) || spawnLoc.distanceSquaredTo(newLoc) < minDist)) {
            rc.move(bestOption);
            return;
        }
        bestOption = bestOption.rotateLeft();
        newLoc = rc.getLocation().add(bestOption);
        if (rc.canMove(bestOption) && (spawnLoc.distanceSquaredTo(newLoc) < ((int)Math.sqrt(rc.getLocation().distanceSquaredTo(spawnLoc)) + numfriendlies - penalty)*((int)Math.sqrt(rc.getLocation().distanceSquaredTo(spawnLoc))+1) || spawnLoc.distanceSquaredTo(newLoc) < minDist)) {
            rc.move(bestOption);
            return;
        }
        bestOption = bestOption.rotateRight();
        bestOption = bestOption.rotateRight();
        newLoc = rc.getLocation().add(bestOption);
        if (rc.canMove(bestOption) && (spawnLoc.distanceSquaredTo(newLoc) < ((int)Math.sqrt(rc.getLocation().distanceSquaredTo(spawnLoc)) + numfriendlies - penalty)*((int)Math.sqrt(rc.getLocation().distanceSquaredTo(spawnLoc))+1) || spawnLoc.distanceSquaredTo(newLoc) < minDist)) {
            rc.move(bestOption);
            return;
        }
        move(spawnLoc);
    }

    static void moveSlanderer() throws GameActionException { //fix run away code
    	setDefensiveFlag();
        Team hotdog = rc.getTeam().opponent(); //nnnaaaaaa! my enemy is hotto doggu. brrrrrrrrrrrrrrrrrrrrrrrrrrrrrr
        int[][] foundHotDogs = new int[100][5]; //1st: 0 = muckraker, 1 = politician, 2nd: distSquaredTo, 3rd: direction, 4th,5th = x,y
        int index = 0;
        for (RobotInfo robot : rc.senseNearbyRobots(-1, hotdog)){
            if(robot.type.canExpose()){
                foundHotDogs[index][0] = 0;
                foundHotDogs[index][1] = rc.getLocation().distanceSquaredTo(robot.getLocation());
                foundHotDogs[index][2] = dirToInt(rc.getLocation().directionTo(robot.getLocation()));
                foundHotDogs[index][3] = robot.getLocation().x;
                foundHotDogs[index][4] = robot.getLocation().y;
                index++;
            }
        }
        Team nothotdog = rc.getTeam(); //nnnaaaaaa! my enemy is hotto doggu. brrrrrrrrrrrrrrrrrrrrrrrrrrrrrr
        for (RobotInfo robot : rc.senseNearbyRobots(-1, nothotdog)){
            if(rc.canGetFlag(robot.getID()) && rc.getFlag(robot.getID())%32 >= 13 && rc.getFlag(robot.getID())%32 <= 15){
            	int xLoc = rc.getFlag(robot.getID()) / 131072;
            	int yLoc = (rc.getFlag(robot.getID())/1024) % 128;
            	int[] arr = {xLoc, yLoc};
            	MapLocation mookLoc = convertLocation(arr);
                foundHotDogs[index][0] = 1;
                foundHotDogs[index][1] = rc.getLocation().distanceSquaredTo(mookLoc);
                foundHotDogs[index][2] = dirToInt(rc.getLocation().directionTo(mookLoc));
                foundHotDogs[index][3] = mookLoc.x;
                foundHotDogs[index][4] = mookLoc.y;
                index++;
            }
        }
        if(index != 0){
		    double[] weights = new double[8];
		    for(int i = 0; i < 8; i++){
		        MapLocation adj = rc.getLocation().add(directions[i]);
		        double weight = 1000.0D;
		        for(int j = 0; j < index; j++){
		            int hddir = foundHotDogs[j][2];
		            int hddist = foundHotDogs[j][1];
		            MapLocation hdloc = new MapLocation(foundHotDogs[j][3], foundHotDogs[j][4]);
		            weight *= ((double)adj.distanceSquaredTo(hdloc)*1.0D)/((double)hddist*1.0D);
		        }
		        weights[i] = weight;
		    }
		    for(int k = 0; k < 8; k++){
		        double max = 0;
		        int bestDir = -1;
		        for(int l = 0; l < 8; l++){
		            if(weights[l] > max){
		                max = weights[l];
		                bestDir = l;
		            }
		        }
		        if(bestDir == -1){
		            return;
		        }
		        if(rc.canMove(directions[bestDir])){
		            rc.move(directions[bestDir]);
		            return;
		        }
		        else{
		            weights[bestDir] = -1;
		        }
		    }
		}
		if(!detectFriendlyPoli()){
			move(spawnLoc);
		}
        staySafe(RobotType.POLITICIAN, 130, 6, 16);
	}
    
    static boolean detectFriendlyPoli() throws GameActionException {
        Team ally = rc.getTeam();
        int ct = 0;
        if(rc.canSenseLocation(spawnLoc)){
        	ct = 2;
        }	
        for (RobotInfo robot : rc.senseNearbyRobots(7, ally)) {
        	boolean isSlanderer = false;
        	for(int i : goodValues){
        		if(robot.getInfluence() == i){
        			isSlanderer = true;
        			break;
        		}
        	}
            if (robot.type.canEmpower() && isSlanderer) {
                ct++;
            }
        }
        return ct > rc.getLocation().distanceSquaredTo(spawnLoc)/72 + 1;
    }
    
	static void setDefensiveFlag() throws GameActionException {
		boolean foundM = detectMuck();
		boolean foundP = detectPoli();
		int lowP = findLowP();
		int lowM = findLowM();
		if(foundM && foundP){
			MapLocation closestMook = getMLoc();
			flagMPL(13, 13, closestMook);
			rc.setFlag(flag);
		}
		else if(foundM){
			MapLocation closestMook = getMLoc();
			flagMPL(13, (lowP+1), closestMook);
			rc.setFlag(flag);
		}
		else if(foundP){
			rc.setFlag((lowM+1) + 32*13 + lowMLoc*1024);
		}
		else{
			rc.setFlag((lowM+1) + 32*(lowP+1) + lowMLoc*1024);
		}
	}    
    
    static int findLowP() throws GameActionException {
    	Team ally = rc.getTeam();
    	int min = 10000;
    	int myFlag = rc.getFlag(rc.getID())/32;
    	for(RobotInfo robot : rc.senseNearbyRobots(-1, ally)){
    		int allyFlag = rc.getFlag(robot.getID())/32;
    		if(allyFlag >= 13 && allyFlag < min && (allyFlag < myFlag || myFlag == 0)){
    			min = allyFlag;
    		}
    	}
    	if(min != 10000 && min <= 17){
    		return min;
    	}
    	return -1;
    }
    
    
    static int findLowM() throws GameActionException {
    	Team ally = rc.getTeam();
    	int min = 10000;
    	int myFlag = rc.getFlag(rc.getID())%32;
    	lowMLoc = 0;
    	for(RobotInfo robot : rc.senseNearbyRobots(-1, ally)){
    		int allyFlag = rc.getFlag(robot.getID())%32;
    		if(allyFlag >= 13 && allyFlag < min && (allyFlag < myFlag || myFlag == 0)){
    			min = allyFlag;
    			lowMLoc = rc.getFlag(robot.getID())/1024;
    		}
    	}
    	if(min != 10000 && min <= 17){
    		return min;
    	}
    	return -1;
    }
    
    static MapLocation getMLoc() throws GameActionException {
    	Team enemy = rc.getTeam().opponent();
    	int minDS = 10000;
    	MapLocation enemyLoc = null;
    	MapLocation myLoc = rc.getLocation();
        for (RobotInfo robot : rc.senseNearbyRobots(-1, enemy)) {
            if (robot.type.canExpose() && robot.getLocation().distanceSquaredTo(myLoc) < minDS) {
                minDS = robot.getLocation().distanceSquaredTo(myLoc);
                enemyLoc = robot.getLocation();
                
            }
        }
        if(enemyLoc != null){
        	return enemyLoc;
        }
        return null;
    }
    
    static void moveGood(MapLocation dest) throws GameActionException {
    	if(rc.getCooldownTurns() >= 1.0000000D){
    		return;
    	}
    	fillTable(dest, 2);
    	MapLocation curLoc = rc.getLocation();
    	double minWeight = 10000000;
    	MapLocation firstStep, secondStep, thirdStep;
    	int bestFirstStep = 7;
    	double curweight = 0;
    	int curx = curLoc.x;
		int cury = curLoc.y;
		int a, b, c;
		int q = ((int)(Math.random()*2))*2 - 1;
    	int w = ((int)(Math.random()*2))*2 - 1;
    	int e = ((int)(Math.random()*2))*2 - 1;
		int dirTo = dirToInt(curLoc.directionTo(dest));
    	for(int i = -1; i <= 1; i++){
    		a = (dirTo + 8 + q*i)%8;
    		firstStep = curLoc.add(directions[a]);
    		boolean senseLoc = rc.canSenseLocation(firstStep);
    		if(!senseLoc || (senseLoc && rc.senseRobotAtLocation(firstStep) != null)){
    			continue;
    		}
    		for(int j = -1; j <= 1; j++){
    			b = (dirTo + 8 + w*j)%8;
				secondStep = firstStep.add(directions[b]);
				if(!rc.canSenseLocation(secondStep)){
					continue;
				}
    			for(int k = -1; k <= 1; k++){
    				c = (dirTo + 8 + e*k)%8;
    				thirdStep = secondStep.add(directions[c]);
    				if(!rc.canSenseLocation(thirdStep)){
    					continue;
    				}
					curweight = graphWeights[cury - firstStep.y + 3][firstStep.x - curx + 3] + graphWeights[cury - secondStep.y + 3][secondStep.x - curx + 3] + graphWeights[cury - thirdStep.y + 3][thirdStep.x - curx + 3] + sqrtDists[cury - thirdStep.y + 3][thirdStep.x - curx + 3];
					if(curweight < minWeight){
						minWeight = curweight;
						bestFirstStep = a;
					}
    			}
    		}
    	}
    	if(rc.canMove(directions[bestFirstStep])){
    		rc.move(directions[bestFirstStep]);
    	}	
    	
    	else move(dest);
    }
    
    static void moveGood(int d) throws GameActionException {
    	if(rc.getCooldownTurns() >= 1.0000000D){
    		return;
    	}
    	MapLocation curLoc = rc.getLocation();
    	int curx = curLoc.x;
		int cury = curLoc.y;
    	if(dest2 == null || turnCount % 10 == 0 || dest2.distanceSquaredTo(curLoc) <= 5){	
    		dest2 = new MapLocation(curx + 20*directions[d].dx, cury + 20*directions[d].dy);
    	}
    	fillTable(dest2, 2);
    	int q = ((int)(Math.random()*2))*2 - 1;
    	int w = ((int)(Math.random()*2))*2 - 1;
    	int e = ((int)(Math.random()*2))*2 - 1;
    	double minWeight = 10000000;
    	MapLocation firstStep, secondStep, thirdStep;
    	int bestFirstStep = 7;
    	double curweight = 0;
    	
		int a, b, c;
		int dirTo = d;
    	for(int i = -1; i <= 1; i++){
    		a = (dirTo + 8 + q*i)%8;
    		firstStep = curLoc.add(directions[a]);
    		boolean senseLoc = rc.canSenseLocation(firstStep);
    		if(!senseLoc || (senseLoc && rc.senseRobotAtLocation(firstStep) != null)){
    			continue;
    		}
    		for(int j = -1; j <= 1; j++){
    			b = (dirTo + 8 + w*j)%8;
				secondStep = firstStep.add(directions[b]);
				if(!rc.canSenseLocation(secondStep)){
					continue;
				}
    			for(int k = -1; k <= 1; k++){
    				c = (dirTo + 8 + e*k)%8;
    				thirdStep = secondStep.add(directions[c]);
    				if(!rc.canSenseLocation(thirdStep)){
    					continue;
    				}
					curweight = graphWeights[cury - firstStep.y + 3][firstStep.x - curx + 3] + graphWeights[cury - secondStep.y + 3][secondStep.x - curx + 3] + graphWeights[cury - thirdStep.y + 3][thirdStep.x - curx + 3] + sqrtDists[cury - thirdStep.y + 3][thirdStep.x - curx + 3];
					if(curweight < minWeight){
						minWeight = curweight;
						bestFirstStep = a;
					}
    			}
    		}
    	}
    	if(rc.canMove(directions[bestFirstStep])){
    		rc.move(directions[bestFirstStep]);
    	}	
    	
    	else {
			cDir = d;
			move();
		}
    }
	
	static void fillTable(MapLocation dest, int penalty) throws GameActionException {
		MapLocation curLoc = rc.getLocation();
		double acd = (double)rc.getType().actionCooldown;
		int curx = curLoc.x;
		int cury = curLoc.y;
		for(int i = 0; i < 7; i++){
			for(int j = 0; j < 7; j++){
				MapLocation target = new MapLocation(curx + j - 3, cury - i + 3);
				if(!rc.canSenseLocation(target)){
					continue;
				}
				graphWeights[i][j] = acd/rc.sensePassability(target);
				sqrtDists[i][j] = penalty*Math.sqrt(target.distanceSquaredTo(dest));
			}
		}
	}
    
    /* Builds Cross (EC)
        if(detectEnemy() || needDelay2){
        	System.out.println("enemy spotted");
        	if(isInnerPadIncomplete()){
        		System.out.println("incomplete");
                boolean built = buildInnerPad();
                if(built){
                    rc.setFlag(12);
                    needDelay2 = true;
                }
            }
            else if(needDelay2){
                needDelay2 = false;
            }
        }
    */

    /* Activate Def Politician
        if(turnCount == 1){
            getSpawnLoc();
            getECID();
        }
        setDefensiveFlag();
        defendBase();
        circleBase(RobotType.POLITICIAN, DEF, 2, 49);
    */

    /* Slanderer Runaway
        if(turnCount == 1){
    		getECID();
    		getSpawnLoc();
    	}
        moveSlanderer();
        if(rc.getLocation().distanceSquaredTo(spawnLoc) > maxDistFromBase){
        	maxDistFromBase = rc.getLocation().distanceSquaredTo(spawnLoc);
        }
    */

    /* Active wall + defensive muckrakers
        if(turnCount == 1){
            getSpawnLoc();
            getECID();
        }
        if(turnCount == 1 && rc.getFlag(ecid)%32 == 12){
        	isPad = true;
        	rc.setFlag(11);
        	getInnerPadDest();
        }
        if(isPad){
        	if(rc.getLocation().distanceSquaredTo(dest) != 0 && !arrived){
				if(rc.senseRobotAtLocation(dest) != null && rc.getFlag(rc.senseRobotAtLocation(dest).getID())%32 == 11){
					dest = rc.getLocation().subtract(rc.getLocation().directionTo(spawnLoc).rotateRight().rotateRight());
				}
				move(dest);
			}
			else{
				arrived = true;
			}
		}
		else{
			setDefensiveFlag();
			Team enemy = rc.getTeam().opponent();
			RobotInfo[] enemies = rc.senseNearbyRobots(-1, enemy);
		    for (RobotInfo robot : enemies) {
		        if (robot.type.canBeExposed()) {
		            if (rc.canExpose(robot.location)) {
		                rc.expose(robot.location);
		                return;
		            }
		        }
		    }
			if(detectPoli()){
				block();
			}
			else if(findLowP() >= 13 && findLowP() <= 16){
				Team ally = rc.getTeam();
				for(RobotInfo robot : rc.senseNearbyRobots(-1, ally)){
					int allyFlag = rc.getFlag(robot.getID())/32;
					if(allyFlag == findLowP()){
						move(robot.getLocation());
					}
				}
			}
			circleBase(RobotType.MUCKRAKER, 2, 2, 49);
			if(rc.getLocation().distanceSquaredTo(spawnLoc) > maxDistFromBase){
				maxDistFromBase = rc.getLocation().distanceSquaredTo(spawnLoc);
			}
		}
    */

}

/*
static void defendBase() throws GameActionException {
        MapLocation currentLoc = rc.getLocation();
        for (int r = 1; r < 8; r++) {
            int total = rc.senseNearbyRobots(r).length;
            int enemyPoliCount = 0;
            for (RobotInfo robot : rc.senseNearbyRobots(r, rc.getTeam().opponent())){
                if (robot.type.equals(RobotType.POLITICIAN)){
                    enemyPoliCount += 1;
                }
            }
            
            
            if ((enemyPoliCount > 1) && rc.canEmpower(r)) {
                rc.empower(r);
                return;
            }
        }
        for (int r = 5; r > 0; r--) {
            int total = rc.senseNearbyRobots(r).length;
            int enemyMuckrakerCount = 0;
            for (RobotInfo robot : rc.senseNearbyRobots(r, rc.getTeam().opponent())){
                if (robot.type.equals(RobotType.MUCKRAKER)){
                    enemyMuckrakerCount += 1;
                }
            }
            if (enemyMuckrakerCount > 0 && (rc.getInfluence() - 10) / total > 0 && rc.canEmpower(r)) {
                rc.empower(r);
                return;
            }
        }
        for (int r = 1; r < 10; r++) {
            for (RobotInfo robot : rc.senseNearbyRobots(r, rc.getTeam().opponent())) {
                Direction dir = currentLoc.directionTo(robot.getLocation());
                if (robot.type.equals(RobotType.MUCKRAKER) && rc.canMove(dir) && spawnLoc.distanceSquaredTo(currentLoc.add(dir)) < (int)(1.1D*rc.getLocation().distanceSquaredTo(spawnLoc))) {
                    rc.move(dir);
                    return;
                }
                else if (robot.type.equals(RobotType.MUCKRAKER) && rc.canMove(dir.rotateLeft()) && spawnLoc.distanceSquaredTo(currentLoc.add(dir.rotateLeft())) < (int)(1.1D*rc.getLocation().distanceSquaredTo(spawnLoc))) {
                    rc.move(dir.rotateLeft());
                    return;
                }
                else if (robot.type.equals(RobotType.MUCKRAKER) && rc.canMove(dir.rotateRight()) && spawnLoc.distanceSquaredTo(currentLoc.add(dir.rotateRight())) < (int)(1.1D*rc.getLocation().distanceSquaredTo(spawnLoc))) {
                    rc.move(dir.rotateRight());
                    return;
                }
            }
        }
    }
*/
