package gdacarv;

import robocode.*;
import robocode.util.Utils;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;


// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * FuzzyRobot - a robot by Gustavo Carvalho
 */
public class FuzzyTeamRobot extends LockTargetTeamRobot
{

	final static String fclFileName = "Robot.fcl";
	public static final double ROBOT_SIZE = 36;
	private FunctionBlock shotPowerFunction, movementFunction;


	/**
	 * run: DecisionTreeRobot's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		setColors(Color.green,Color.red,Color.red); // body,gun,radar

		FIS fis = null;
		try {
			fis = FIS.load(new FileInputStream(getDataFile(fclFileName)), true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if( fis == null ) { // Error while loading?
			System.err.println("Can't load file: '" + fclFileName + "'");
			return;
		}
		
		shotPowerFunction = fis.getFunctionBlock("shot_power");
		movementFunction = fis.getFunctionBlock("movement");
	

		System.err.println("Loading done. Begin main loop...");
		
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		// Robot main loop

		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		boolean goAhead = true;
		while(true) {
		
			if(getDistanceRemaining() == 0){
				double x = 0, y = 0;
				movementFunction.setVariable("x_init", getX()/getBattleFieldWidth());
				movementFunction.setVariable("y_init", getY()/getBattleFieldHeight());
				movementFunction.evaluate();
				x = movementFunction.getVariable("x_end").getValue()*getBattleFieldWidth();
				y = movementFunction.getVariable("y_end").getValue()*getBattleFieldHeight();
				// SET TURN
				double angle = (Math.toDegrees(Math.atan((x-getX())/(y-getY())))+(y < getY() ? 180 : 360))%360;
				if(Double.isNaN(angle))
					angle = x > getX() ? 90 : 270;
				
				System.err.println("At x: " + getX() + " y: " + getY() + " Move to x: " + x + " y: " + y + " Angle: " + angle);
				double turn = angle - getHeading();
				if(Math.abs(turn) > 180)
			    	turn = (360 - Math.abs(turn))*Math.signum(turn)*-1;
				if(Math.abs(turn) > 90){
					turn = Math.signum(turn)*-1*(180-Math.abs(turn));
					goAhead = false;
				} else
					goAhead = true;
				setTurnRight(turn);
				do{
					scan();
				} while(getTurnRemaining() != 0);
				double distance = Math.sqrt(Math.pow(x-getX(),2) + Math.pow(y-getY(), 2));
				if(goAhead)
					setAhead(distance);
				else
					setBack(distance);
				// SET MOVEMENT TO POINT
			}
			
			scan();
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		super.onScannedRobot(e);
		if(!e.getName().equals(getTarget()))
			return;
		double degree = e.getBearing();
	    double heading = getHeading();
	    double gunHeading = getGunHeading();
	    
	    //Traces the enemy with gun
	    double turn = heading - gunHeading + degree;
	    if(Math.abs(turn) > 180)
	    	turn = (360 - Math.abs(turn))*Math.signum(turn)*-1;
		setTurnGunRight(turn);
		
		double radarTurn = getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians();
		 
	    setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));
		
		if(isAim(e)){
			shotPowerFunction.setVariable("distancetorobot", e.getDistance());
			shotPowerFunction.setVariable("enemyenergy", e.getEnergy());
			shotPowerFunction.setVariable("myenergy", getEnergy());
			
			shotPowerFunction.evaluate();
			double power = shotPowerFunction.getVariable("power").getValue();
			setFire(power);
			//System.err.println("Power: " + power + " Distance: " + e.getDistance() + " EnemyEnergy: " + e.getEnergy() + " MyEnergy: " + getEnergy());
		}
		
	}
	
	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		super.onRobotDeath(event);
		if(getTarget() == null)
			setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
	}
	
	private boolean isAim(ScannedRobotEvent scannedRobot){
		return Math.abs((scannedRobot.getBearing() + getHeading() + 360)%360 - getGunHeading()) <= Math.abs(2*180*Math.atan(ROBOT_SIZE*0.5d/scannedRobot.getDistance()/Math.PI));
	}
}
