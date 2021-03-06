package Master;


// Rishab: TODO clean up the travelling code. I can't read it, nor work with it!

import lejos.nxt.*;
import Support.Communicator;
import java.util.LinkedList;

public class Navigation {
	// put your navigation code here 
	// NAVIGATION USED AS A MIX FROM LAST LAB AND FROM THE GIVEN CODE AND SOME EXTRA METHODS ADDED
	
	private Odometer odo;
	private UltrasonicSensor us;
	private TwoWheeledRobot robot;
	private Grid grid;
	
	private int ROTATE_SPEED = 50;
	
	private double leftRadius = 2.9;
	private double rightRadius = 2.9;
	private double width = 12.61;
	
	public double[] position = new double[3];
	
	public int ROTATION_SPEED = 150;
	final static int FAST = 200, SLOW = 100;
	final static double DEG_ERR = 1.0, CM_ERR = 1;
	
	private static double finalX = 5.0, finalY = 5.0;
	
	private boolean objectCollected = false;
	
	private int i = 1;

	
	public Navigation(Odometer odo, UltrasonicSensor us) {
		this.odo = odo;
		this.robot = odo.getRobot();
		this.grid = odo.getGrid();
		this.us = us;
	}
	
	/*
	*  Navigation is done by generating a path of points, and then sequentially
	*  following these steps:
	*    - check if there is a block to close. If there is, report the location
	*      of the block
	*    - if there was a block, restart, otherwise travel to point
	*/
	
	public void navigateTo(Point destination) {
		Point location = new Point((int) odo.getX(), (int) odo.getY());
		LinkedList<Point> path = grid.getDirections(location, destination);
		
		while (path.size() > 0 && navigate(path));
		
		// recursively try again if we didn't make it
		if (path.size() > 0) navigateTo(destination);
	}
	
	private boolean navigate(LinkedList<Point> path) {
		Point collision = collisionPoint();
		
		if (collision == null) {
			travelTo(path.remove(0));
			return true;
		} else {
			grid.report(collision.x, collision.y);
			return false;
		}
	}
		
	private Point collisionPoint() {
		int distance = getDistance();
		
		Point point = null;
		
		// Rishab: the distance for a collision must be determined experimentally
		
		if (distance < 40) {
			double angle = Math.toRadians(odo.getTheta());
		
			int x = (int) (distance * Math.cos(angle) + odo.getX());
			int y = (int) (distance * Math.sin(angle) + odo.getY());
			
			point = new Point(x, y);
		}
		
		return point;
	}
	
	private int getDistance() {
		
		// Rishab: possibly need to do multiple readings here
		
		return us.getDistance();
	}
	
	/*
	*  Travelling simply moves the robot to (x, y) assuming the path is safe. It
	*  must only be used for short journeys. 
	*/
		
	public void travelTo(Point point) {
		travelTo(point.x, point.y);
	}
		
	public void travelTo(double x, double y) {
		// USE THE FUNCTIONS setForwardSpeed and setRotationalSpeed from TwoWheeledRobot!
		
		double requiredAngle;
		
		boolean obstacle = false;
		
		requiredAngle = Math.toDegrees((Math.atan2(x - odo.getX(), y - odo.getY())));
		
//		if (requiredAngle > Math.PI)
//			requiredAngle = requiredAngle - 2*Math.PI; 
		
		turnTo(requiredAngle);
		
		while (Math.abs(x - odo.getX()) > CM_ERR || Math.abs(y - odo.getY()) > CM_ERR) {
			
			/*
			if(us.getDistance() <= 25 && objectCollected == false){
				obstacleAvoider(x, y);					
				break;
				} 
			
			else if(us.getDistance() <= 25 && objectCollected == true){
				objectDeliver(finalX, finalY);
				break;
			}
			*/
				

			robot.setForwardSpeed(10);
		}
		
		robot.setForwardSpeed(0);

		
	}
	
	public double minTheta(double angle){
		if (angle < -180)
			angle += 360;
		
		else if(angle > 180)
			angle -= 360;
		
		return angle;
	}
	
	public void turnTo(double angle){
		
		Motor.A.setSpeed(ROTATE_SPEED);
		Motor.B.setSpeed(ROTATE_SPEED);			
				
		double correctedAngle = angle - odo.getTheta();
		
		correctedAngle = minTheta(correctedAngle);
		
		Motor.A.rotate(convertAngle(leftRadius, width, correctedAngle), true);
		Motor.B.rotate(-convertAngle(rightRadius, width, correctedAngle), false);
		
		Sound.beep();
		
		Motor.A.stop();
		Motor.B.stop();
	}
	
	
	public int convertDistance(double radius, double distance) {							//Copied same from Lab 2 
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	public int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	

	
	public void goForward(){
		Motor.A.setSpeed(100);
		Motor.B.setSpeed(100);
		
		Motor.A.forward();
		Motor.B.forward();
	}
	
	/*
	public void obstacleAvoider(double x, double y){
		
		Sound.beepSequence();
		
		int filter = 0;
		
		try {
			if (ObjectDetector.detector() == false){
				if (i % 2 != 0){
					turnTo(odo.getTheta() + 90);
					Sound.beepSequenceUp();
				}
				
				else
					turnTo(odo.getTheta() - 90);
				
				
				moveBy(35);
				
				
				if (i % 2 != 0)
					turnTo(odo.getTheta() - 90);
				
				else
					turnTo(odo.getTheta() + 100);
				
				
				while (true){
				
					if(us.getDistance() >= 55){
						filter ++;
					}
					
					else
						turnTo(odo.getTheta() + 15);
					
					if(filter >= 10){
						moveBy(45);
						travelTo(x,y);
						
						moveBy(-10);
						
						break;
					}
				}
				
			}
			
			
			if (true) {
				
				// tell the slave brick to lift the block
				comm.bluetoothSend("lift");
			}
			
			
		} 
		
		catch (Exception e) {}
		
		i++;
		
	}
	*/
		
	public void objectDeliver(double x, double y){
		Sound.beepSequenceUp();
		
		int filter = 0;
		
		if(i % 2 != 0){
			turnTo(odo.getTheta() + 90);
		}
		
		else
			turnTo(odo.getTheta() - 90);
		
		moveBy(35);
		
		if(i % 2 != 0){
			turnTo(odo.getTheta() - 90);
		}
		
		else
			turnTo(odo.getTheta() + 90);
		
		
		while (true){
			
			if(us.getDistance() >= 55){
				filter ++;
			}
			
			else
				turnTo(odo.getTheta() + 15);
			
			if(filter >= 10){
				moveBy(45);
				travelTo(x,y);
				break;
			}
		}
		
		i++;
		
	}
	
	public void moveBy(int distance){
		
		Motor.A.setSpeed(100);
		Motor.B.setSpeed(100);
		
		Motor.A.rotate(convertDistance(leftRadius, distance), true);
		Motor.B.rotate(convertDistance(rightRadius, distance), false);
	
	}
	
	public void stop() {
		robot.stop();
	}
	
	
	
	
	/*
	*  Simple getters for internal objects. Passing in Navigation object gives
	*  access to Odometer, Robot, UltrasonicSensor, and Grid.
	*/
	
	public TwoWheeledRobot getRobot(){
		return this.robot;
	}
	
	public Odometer getOdometer() {
		return this.odo;
	}
	
	public Grid getGrid() {
		return this.grid;
	}
	
	public UltrasonicSensor getUltrasonicSensor() {
		return this.us;
	}
	
	
	
//	public void startRotating(){											//Some helper methods created
//		robot.setRotationSpeed(ROTATE_SPEED);
//	}
//	
//	public void startRotatingCounter(){
//		robot.setRotationSpeed(-10);
//	}
	
}
