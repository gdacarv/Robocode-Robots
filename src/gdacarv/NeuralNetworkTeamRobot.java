package gdacarv;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.util.TransferFunctionType;

import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;


// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * FuzzyRobot - a robot by Gustavo Carvalho
 */
public class NeuralNetworkTeamRobot extends LockTargetTeamRobot
{
	
	public static final double ROBOT_SIZE = 36;

	private static final int TICKS_AHEAD_PREDICTION = 60;

	private static final int SAMPLE_RATE = 60;

	//private static DataSet movementDataSet = new DataSet(4, 2);

	private MultiLayerPerceptron firePerceptron;

	//private MultiLayerPerceptron enemyMovementPerceptron;

	//private double[][] dataBuffer = new double[TICKS_AHEAD_PREDICTION][4];
	//private int bufferIndex = 0;
	private long lastTime = -1, nextSample = 0;

	private double enX, enY;
	private long enTime;
	private String enName;
	
	private static Map<String, NeuralNetworkDataItem> mNN = new HashMap<String, NeuralNetworkDataItem>(3);
	


	/**
	 * run: DecisionTreeRobot's default behavior
	 */
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		setColors(Color.green,Color.red,Color.red); // body,gun,radar

		// create multi layer perceptron
		firePerceptron = new MultiLayerPerceptron(TransferFunctionType.TANH, 3, 6, 1);
		// learn the training set
		firePerceptron.learn(NeuralNetworkDataset.firePowerTraining);

		for(NeuralNetworkDataItem nn : mNN.values()){
			nn.clear();
			if(nn.dataSet.size() > 10){
				nn.network = new MultiLayerPerceptron(TransferFunctionType.TANH, 4, /*12, 2*/4, 2); // 4, 12, 2 WORKS TANH
				MomentumBackpropagation learningRule = new MomentumBackpropagation();
				nn.network.setLearningRule(learningRule);
				learningRule.setLearningRate(0.01);
				System.err.println("Building movement perceptron with " + nn.dataSet.size() + " training registers. Learning rate = " + learningRule.getLearningRate() + " momentum = " + learningRule.getMomentum());
				nn.network.learn(nn.dataSet);
			}
		}
		
		System.err.println("Loading done. Begin main loop...");

		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		// Robot main loop

		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
		boolean goAhead = true;
		while(true) {

			if(getDistanceRemaining() == 0 && enTime < getTime()){
				double x = 0, y = 0;
				x = Math.random()*getBattleFieldWidth();
				y = Math.random()*getBattleFieldHeight();
				// SET TURN
				double angle = (Math.toDegrees(Math.atan((x-getX())/(y-getY())))+(y < getY() ? 180 : 360))%360;
				if(Double.isNaN(angle))
					angle = x > getX() ? 90 : 270;

					//System.err.println("At x: " + getX() + " y: " + getY() + " Move to x: " + x + " y: " + y + " Angle: " + angle);
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
			
			if(getRadarTurnRemaining() == 0)
				setTurnRadarRight(Double.POSITIVE_INFINITY);
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		super.onScannedRobot(e);
		if(!e.getName().equals(getTarget()))
			return;
		
		NeuralNetworkDataItem nn;
		if(mNN.containsKey(e.getName()))
			nn = mNN.get(e.getName());
		else{
			nn = new NeuralNetworkDataItem(new double[TICKS_AHEAD_PREDICTION][4], new DataSet(4, 2));
			mNN.put(e.getName(), nn);
		}
		double degree = e.getBearing();
		double heading = getHeading();
		double gunHeading = getGunHeading();

		//Traces the enemy with gun
		if(nn.network == null){
			double turn = heading - gunHeading + degree;
			if(Math.abs(turn) > 180)
				turn = (360 - Math.abs(turn))*Math.signum(turn)*-1;
			setTurnGunRight(turn);
		}

		double radarTurn = getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians();

		setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));

		double absBearing = getHeadingRadians() + e.getBearingRadians();
		double eX = (getX() + Math.sin(absBearing) * e.getDistance())/getBattleFieldWidth();
		double eY = (getY() + Math.cos(absBearing) * e.getDistance())/getBattleFieldHeight();

		if(nn.network == null && isAim(e)){
			firePerceptron.setInput(Math.min(1d, e.getDistance()/1000d), Math.min(1d, e.getEnergy()/120d), Math.min(1d, getEnergy()/120d));

			firePerceptron.calculate();
			double power = firePerceptron.getOutput()[0]*3d;
			setFire(power);
			//System.err.println("Power: " + power + " Distance: " + e.getDistance() + " EnemyEnergy: " + e.getEnergy() + " MyEnergy: " + getEnergy());
		}

		long time = getTime();
		if(nn.network != null){
			if((enX + enY == 0 || time > enTime) && getGunHeat() == 0){
				nn.network.setInput(e.getHeading()/360, e.getVelocity()/Rules.MAX_VELOCITY, eX, eY);
				nn.network.calculate();
				double[] pos = nn.network.getOutput();

				enX = Math.max(0, Math.min(getBattleFieldWidth(), pos[0]*getBattleFieldWidth()));
				enY = Math.max(0, Math.min(getBattleFieldHeight(), pos[1]*getBattleFieldHeight()));
				
				if(!predictionIsPossible(eX, eY, enX, enY, TICKS_AHEAD_PREDICTION)){
					enX = enY = 0;
				}
				
				enTime = time + TICKS_AHEAD_PREDICTION;

				double turn = normalizeBearing(absoluteBearing(getX(), getY(), enX, enY) - getGunHeading());
				setTurnGunRight(turn);
				System.err.println("Enemy current x = " + eX*getBattleFieldWidth() + " y = " + eY*getBattleFieldHeight() + " future x = " + enX + " y = " + enY + " turning gun by " + turn);
			}else if(enTime >= time && getGunTurnRemaining() == 0){
				double turn = normalizeBearing(absoluteBearing(getX(), getY(), enX, enY) - getGunHeading());
				setTurnGunRight(turn);
				double distance = Math.sqrt(Math.pow(enX-getX(), 2) + Math.pow(enY-getY(), 2));
				long remainingTime = enTime-time-1;
				double firePower = (20-distance/remainingTime)/3;
				System.err.println("Firing to x = " + enX + " y = " + enY + " with remainingTime = " + remainingTime + " distance = " + distance + " firePower = " + firePower);
				if(firePower >= Rules.MIN_BULLET_POWER && firePower <= Rules.MAX_BULLET_POWER){
					setFire(firePower);
					enX = enY = enTime = 0;
				}
			}
		}


		// Collect enemy's movement data
		if(time > lastTime){
			//System.err.println("Entrou collect. time = " + time + " bufferIndex = " + bufferIndex + " lastTime = " + lastTime + " nextSample = " + nextSample);
			if(nn.dataBuffer[nn.bufferIndex][2] + nn.dataBuffer[nn.bufferIndex][3] > 0){ // Exist previously data
				System.err.println("Saved data: [" + nn.dataBuffer[nn.bufferIndex][0] + ", " + nn.dataBuffer[nn.bufferIndex][1] + ", " + nn.dataBuffer[nn.bufferIndex][2] + ", " + nn.dataBuffer[nn.bufferIndex][3] + ", " + eX + ", " + eY + "]");
				nn.dataSet.addRow(Arrays.copyOf(nn.dataBuffer[nn.bufferIndex], 4), new double[]{ eX, eY});
				nn.dataBuffer[nn.bufferIndex][2] = nn.dataBuffer[nn.bufferIndex][3] = 0;
			}
			if(time >= nextSample){
				nn.dataBuffer[nn.bufferIndex][0] = e.getHeading()/360;
				nn.dataBuffer[nn.bufferIndex][1] = e.getVelocity()/Rules.MAX_VELOCITY;
				nn.dataBuffer[nn.bufferIndex][2] = eX;
				nn.dataBuffer[nn.bufferIndex][3] = eY;

				
				nextSample += SAMPLE_RATE; 
			}
			nn.bufferIndex = (int) ((nn.bufferIndex+1)%TICKS_AHEAD_PREDICTION);
			lastTime = time;
			//System.err.println("Saiu collect. time = " + time + " bufferIndex = " + bufferIndex + " lastTime = " + lastTime + " nextSample = " + nextSample);
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
	
	double absoluteBearing(double x1, double y1, double x2, double y2) {
		double xo = x2-x1;
		double yo = y2-y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xo / hyp));
		double bearing = 0;

		if (xo > 0 && yo > 0) { // both pos: lower-Left
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
			bearing = 360 + arcSin; // arcsin is negative here, actuall 360 - ang
		} else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { // both neg: upper-right
			bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
		}

		return bearing;
	}
	
	// normalizes a bearing to between +180 and -180
	double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
	
	public static boolean predictionIsPossible(double xo, double yo, double x, double y, int ticks){
		return Rules.MAX_VELOCITY*ticks >= (Math.sqrt(Math.pow(x-xo, 2) + Math.pow(y-yo, 2))-ROBOT_SIZE);
	}
}
