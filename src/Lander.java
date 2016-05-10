
// Lander.java

import ch.aplu.jgamegrid.*;
import ch.aplu.util.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Lander extends Actor implements GGKeyListener {
	
	private final 	double 		startFuel 		= 1000; 										// Amount of fuel at start
	private final 	double 		fuelFactor 		= 0.5; 											// Fuel consumption per simulation period and thrust level
	private final 	double 		amax			= 1.6; 											// Free acceleration on moon
	private 		double 		x; 																// Position in left direction
	private 		double 		y; 																// Position in downward direction
	private 		double 		velocityX;														// Speed in horizontal direction
	private 		double 		velocityY; 														// Speed in vertical direction
	private 		double 		accelerationX; 													// Acceleration in horizontal direction
	private 		double 		accelerationY; 													// Acceleration in vertical direction
	private 		int 		thrustLevel; 													// Thrust level
	private 		double 		fuel; 															// Remaining fuel
	private 		Actor 		ufoCrashed 		= new Actor("sprites/landerdebris.gif");		// Crashed Ufo Actor
	private 		Actor 		thrust 			= new Actor("sprites/thrust.gif", 9);			// Thrust Fire Actor
	private 		boolean 	isLanded 		= false;										// Boolean for checking if UFO is already landed
	private 		HiResTimer 	timer 			= new HiResTimer();								// Timer ?
	private 		SoundPlayer player 			= new SoundPlayer(this, "wav/jet.wav");			// SoundPlayer for Thrust sounds
	private 		boolean 	fuelExpired;													// Boolean for disabling controls if fuel is expired
	
	
	public Lander() {
		super("sprites/lander.gif");
	}

	
	public void reset() {
		
		GameGrid gg = gameGrid;
		setDirection(Location.SOUTH);
		x = getLocationStart().x;
		y = getLocationStart().y;
		velocityX = 0;
		velocityY = 0;
		accelerationX = 0;
		accelerationY = amax;
		fuel = startFuel;
		fuelExpired = false;
		
		show();
		setActEnabled(true);
		
		if (ufoCrashed.gameGrid == null) // not yet added to GameGrid
			gg.addActor(ufoCrashed, new Location());
		ufoCrashed.hide();
		
		if (thrust.gameGrid == null) // not yet added to GameGrid
			gg.addActor(thrust, new Location());
		
		thrust.hide();
		isLanded = false;
		gg.setBgColor(java.awt.Color.black);
		timer.start();
		player.setVolume(0);
		player.playLoop();
	}

	
	public void act() {
		
		GameGrid gg = gameGrid;
		double vdispy = (int) (100 * velocityY) / 100.0;
		double hy = 490 - y;
		String s;
		
		if (fuelExpired)
			s = String.format("hy = %10.2f m    velocityY = %10.2f m/s    accelerationY = %10.2f m/s^2    fuel = %10.0f kg (expired)", hy,
					velocityY, accelerationY, fuel);
		
		else
			s = String.format("hy = %10.2f m    velocityY = %10.2f m/s    accelerationY = %10.2f m/s^2    fuel = %10.0f kg", hy, velocityY, accelerationY,
					fuel);
		
		gg.setTitle(s);
		double dtx = 2 * gg.getSimulationPeriod() / 1000.0; // Time scaled: * 2
		double dty = 2 * gg.getSimulationPeriod() / 1000.0; // Time scaled: * 2
		velocityX = velocityX + accelerationX * dtx;
		velocityY = velocityY + accelerationY * dty;
		x = x + velocityX * dtx;
		y = y + velocityY * dty;
		
		setLocation(new Location((int) x, (int) y));
		thrust.setLocation(new Location((int) x, (int) y + 57));
		fuel = fuel - thrustLevel * fuelFactor;

		if (fuel <= 0) {
			fuel = 0;
			thrustLevel = 0;
			accelerationX = 0;
			accelerationY = amax;
			setThrust(0);
			player.setVolume(0);
			fuelExpired = true;
		}

		if (getLocation().y > 490 && !isLanded) {
			gg.setTitle("Touchdown!");
			if (velocityY > 10.0) {
				ufoCrashed.setLocation(new Location(getLocation().x, getLocation().y + 30));
				ufoCrashed.show();
				hide();
				gg.getBg().drawText("Sorry! Crashed with speed: " + vdispy + " m/s", new Point(20, 300));
				gg.playSound(this, GGSound.EXPLODE);
			}
			
			else {
				long time = timer.getTime();
				gg.getBg().drawText("Congratulation! Landed with speed: " + vdispy + " m/s", new Point(20, 300));
				gg.getBg().drawText("Time used: " + time / 1000000 + " s", new Point(20, 350));
				gg.getBg().drawText("Remaining fuel: " + (int) fuel + " kg", new Point(20, 400));
				gg.playSound(this, GGSound.FADE);
			}
			
			player.stop();
			gg.getBg().drawText("Press any key...", new Point(20, 450));
			setActEnabled(false);
			thrustLevel = 0;
			setThrust(0);
			isLanded = true;
		}
	}

	public boolean keyPressed(KeyEvent evt) {
		
		if (!gameGrid.isRunning())
			return true;

		if (isLanded) {
			reset();
			gameGrid.doRun();
			return true;
		}

		double da = 0.4;
		
		switch (evt.getKeyCode()) {
		
			case KeyEvent.VK_UP:
				accelerationY -= da;
				thrustLevel += 1;
				break;
				
			case KeyEvent.VK_DOWN:
				accelerationY += da;
				thrustLevel -= 1;
				if (accelerationY > amax) {
					accelerationY = amax;
					thrustLevel = 0;
				}
				break;
				
			case KeyEvent.VK_LEFT:
				accelerationX -= da;
				break;
				
			case KeyEvent.VK_RIGHT:
				accelerationX += da;
				break;
		}

		if (accelerationY == amax) {
			setThrust(0);
			player.setVolume(0);
		}
		
		else {
			player.setVolume((int) (-100 * accelerationY) + 820);
			setThrust(thrustLevel);
		}
		
		return true; // Consume
	}

	public boolean keyReleased(KeyEvent evt) {
		return true;
	}

	private void setThrust(int i) {
		if (i < 0) {
			i = 0;
			}
		
		if (i > 8) {
			i = 8;
		}
		
		thrust.show(i);
	}
}