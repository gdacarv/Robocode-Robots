package gdacarv;

import robocode.*;
import be.ac.ulg.montefiore.run.jadti.*;
import be.ac.ulg.montefiore.run.jadti.io.*;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.awt.Color;


// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * DecisionTreeRobot - a robot by (your name here)
 */
public class DecisionTreeTeamRobot extends LockTargetTeamRobot
{

	final static String dbFileName = "arguments.db";
	public static final double ROBOT_SIZE = 36;
	
	private final static String INPUT_PREFIX = "arguments-database\nrobotscanned symbolic distancetorobot numerical enemyenergy numerical gunaimedtoenemy symbolic hitbybullet symbolic hitwall symbolic hitrobot symbolic energy numerical heat symbolic positionx numerical positiony numerical moving symbolic gunturning symbolic robotturning symbolic action symbolic\n";

	private ScannedRobotEvent scannedRobot;
	private HitByBulletEvent hitByBulletEvent;
	private HitWallEvent hitWallEvent;
	private HitRobotEvent hitRobotEvent;

	private DecimalFormat decimalFormat;
	private double maxDistance;
	
	/**
	 * run: DecisionTreeRobot's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		setColors(Color.green,Color.red,Color.red); // body,gun,radar
		
		ItemSet learningSet = null;
		try {
			File file = getDataFile(dbFileName);
			System.err.println("Path: " + file.getAbsolutePath());
		    learningSet = ItemSetReader.read(new FileReader(file));
		}
		catch(FileFormatException | IOException e) {
			System.err.println("Exception: " + e.toString());
			System.err.println("Exception: " + e.getMessage());
		    
		    System.exit(-1);
		}
		AttributeSet attributeSet = learningSet.attributeSet();
		
		Vector testAttributesVector = new Vector();
		testAttributesVector.add(attributeSet.findByName("robotscanned"));
		testAttributesVector.add(attributeSet.findByName("distancetorobot"));
		testAttributesVector.add(attributeSet.findByName("enemyenergy"));
		testAttributesVector.add(attributeSet.findByName("gunaimedtoenemy"));
		testAttributesVector.add(attributeSet.findByName("hitbybullet"));
		testAttributesVector.add(attributeSet.findByName("hitwall"));
		testAttributesVector.add(attributeSet.findByName("hitrobot"));
		testAttributesVector.add(attributeSet.findByName("energy"));
		testAttributesVector.add(attributeSet.findByName("heat"));
		testAttributesVector.add(attributeSet.findByName("positionx"));
		testAttributesVector.add(attributeSet.findByName("positiony"));
		testAttributesVector.add(attributeSet.findByName("moving")); 
		testAttributesVector.add(attributeSet.findByName("gunturning"));
		testAttributesVector.add(attributeSet.findByName("robotturning"));
		
		AttributeSet testAttributes = new AttributeSet(testAttributesVector);
		SymbolicAttribute goalAttribute =
		    (SymbolicAttribute) attributeSet.findByName("action");
	
		DecisionTree tree = buildTree(learningSet, testAttributes,
				      goalAttribute);
		
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
		decimalFormatSymbols.setDecimalSeparator('.');
		decimalFormat = new DecimalFormat("##0.00", decimalFormatSymbols);
		
		maxDistance = Math.sqrt(getBattleFieldHeight()*getBattleFieldHeight() + getBattleFieldWidth()*getBattleFieldWidth());

		System.err.println("Tree done. Begin main loop...");
		//System.err.println(new DecisionTreeToDot(tree).produce());
		// Robot main loop
		while(true) {
			// Replace the next 4 lines with any behavior you would like
			/*ahead(100);
			turnGunRight(360);
			back(100);
			setTurnGunRight(360);*/
			
			
			String guess = "nothing";
			try {
				String itemDatabaseString = getItemDatabaseString();
				System.err.println("Input: " + itemDatabaseString);
				guess = getGuess(ItemSetReader.read(new StringReader(itemDatabaseString), attributeSet).item(0), tree);
				System.err.println("Guess: " + guess);
			} catch (FileFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if("fireweak".equals(guess)){
				doActionFire(1);
			} else if("firemedium".equals(guess)){
				doActionFire(2);
			} else if("firestrong".equals(guess)){
				doActionFire(3);
			} else if("turngun".equals(guess)){
				setTurnGunRight(360);
			} else if("aim".equals(guess)){
				setTurnGunRight((scannedRobot.getBearing() + getHeading() + 360)%360 - getGunHeading());
			} else if("movesoutheast".equals(guess)){
				setTurnRight(135 - getHeading());
				setAhead(Math.sqrt(Math.pow(getBattleFieldWidth()*0.7d - getX(), 2) + Math.pow(getBattleFieldHeight()*0.3 - getY(), 2)));
			} else if("movenortheast".equals(guess)){
				setTurnRight(45 - getHeading());
				setAhead(Math.sqrt(Math.pow(getBattleFieldWidth()*0.7d - getX(), 2) + Math.pow(getBattleFieldHeight()*0.7 - getY(), 2)));
			} else if("movenorthwest".equals(guess)){
				setTurnRight(315 - getHeading());
				setAhead(Math.sqrt(Math.pow(getBattleFieldWidth()*0.3d - getX(), 2) + Math.pow(getBattleFieldHeight()*0.7 - getY(), 2)));
			} else if("movesouthwest".equals(guess)){
				setTurnRight(225 - getHeading());
				setAhead(Math.sqrt(Math.pow(getBattleFieldWidth()*0.3d - getX(), 2) + Math.pow(getBattleFieldHeight()*0.3 - getY(), 2)));
			}else if("turnaround".equals(guess)){
				setTurnRight(180);
			} 
			resetEvents();
			onEndLoop();
		}
	}

	private void doActionFire(double power) {
		//setTurnGunRight(0);
		setFire(power);
	}

	private String getItemDatabaseString() {
		StringBuilder builder = new StringBuilder(INPUT_PREFIX);
		
		scan();
		if(scannedRobot != null && scannedRobot.getName().equals(getTarget())){
			builder.append("yes ");
			
			builder.append(decimalFormat.format(scannedRobot.getDistance()/maxDistance)).append(' ');
			
			builder.append(decimalFormat.format(scannedRobot.getEnergy())).append(' ');
			
			builder.append(Math.abs((scannedRobot.getBearing() + getHeading() + 360)%360 - getGunHeading()) <= Math.abs(2*180*Math.atan(ROBOT_SIZE*0.5d/scannedRobot.getDistance()/Math.PI)) ? "yes " : "no ");
			System.err.println("Enemy bearing: " + scannedRobot.getBearing() + " robot heading: " + getHeading() + " gunHeading: "+ getGunHeading() + " enemyDistance: " + scannedRobot.getDistance());
		} else {
			builder.append("no ");
			
			builder.append("? ");
			
			builder.append("? ");
			
			builder.append("no ");
		}
		
		builder.append(hitByBulletEvent != null ? "yes " : "no ");

		builder.append(hitWallEvent != null ? "yes " : "no ");

		builder.append(hitRobotEvent != null ? "yes " : "no ");
		
		builder.append(decimalFormat.format(getEnergy())).append(' ');
		
		builder.append(getGunHeat() > 0 ? "yes " : "no ");
		
		builder.append(decimalFormat.format(getX()/getBattleFieldWidth())).append(' ');
		
		builder.append(decimalFormat.format(getY()/getBattleFieldHeight())).append(' ');
		
		builder.append(getDistanceRemaining() != 0 ? "yes " : "no ");
		
		builder.append(getGunTurnRemaining() != 0 ? "yes " : "no ");
		
		builder.append(getTurnRemaining() != 0 ? "yes " : "no ");
		
		return builder.append('?').append('\n').toString();
	}
	
	private void resetEvents(){
		scannedRobot = null;
		hitByBulletEvent = null;
		hitWallEvent = null;
		hitRobotEvent = null;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		super.onScannedRobot(e);
		scannedRobot = e;
		//stop();
	}

	@Override
	public void onHitByBullet(HitByBulletEvent e) {
		hitByBulletEvent = e;
	}
	
	@Override
	public void onHitWall(HitWallEvent e) {
		super.onHitWall(e);
		hitWallEvent = e;
	}
	
	@Override
	public void onHitRobot(HitRobotEvent event) {
		super.onHitRobot(event);
		hitRobotEvent = event;
	}
	
	/*
     * Build the decision tree.
     */
    static private DecisionTree buildTree(ItemSet learningSet, AttributeSet testAttributes, SymbolicAttribute goalAttribute) {
		DecisionTreeBuilder builder = new DecisionTreeBuilder(learningSet, testAttributes,
				    goalAttribute);
	
	return builder.build().decisionTree();
    }

    /*
     * Prints an item's guessed goal attribute value.
     */
    static private String getGuess(Item item, DecisionTree tree) {
	SymbolicAttribute goalAttribute = tree.getGoalAttribute();
	
	KnownSymbolicValue guessedGoalAttributeValue = 
	    tree.guessGoalAttribute(item);
		
	return goalAttribute.valueToString(guessedGoalAttributeValue);
    }	
    
    protected void onEndLoop(){
    	
    }
}
