package gdacarv;

import java.io.IOException;

import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.MessageEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.TeamRobot;

public abstract class LockTargetTeamRobot extends TeamRobot {

	private static final String SET_TARGET = "Set Target:";
	private String mTarget;
	private boolean leaderPicked = false;
	
	
	protected String getTarget(){
		return mTarget;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		super.onScannedRobot(event);
		if(mTarget == null && !isTeammate(event.getName()) && (leaderPicked || event.getEnergy() >= 150)){
			leaderPicked = true;
			mTarget = event.getName();
			try {
				broadcastMessage(SET_TARGET + mTarget);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("I set Target: " + mTarget);
		}
	}
	
	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		super.onRobotDeath(event);
		if(mTarget.equals(event.getName())){
			mTarget = null;
		}
	}
	
	@Override
	public void onMessageReceived(MessageEvent event) {
		super.onMessageReceived(event);
		if(event.getMessage().toString().startsWith(SET_TARGET)){
			leaderPicked = true;
			mTarget = event.getMessage().toString().substring(SET_TARGET.length());

			System.out.println("Other set Target: " + mTarget);
		}
	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
		super.onHitRobot(event);
		if (event.getBearing() > -90 && event.getBearing() <= 90)
	    {
	        setBack(100);
	    }
	    else
	    {
	        setAhead(100);
	    }
	}
}
