package Processes;

import Devices.CPU;
import Interfaces.StepByStep;

public abstract class Process implements StepByStep {
	private int ip;
	private final int maxNumberOfBlockingStuff = 1;
	private CPU cpu = new CPU();
	
	public abstract void step();
}