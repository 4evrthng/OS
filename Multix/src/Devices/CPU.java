package Devices;

import Interfaces.StepByStep;
import Processes.Process;

public class CPU implements StepByStep {
	
	private Process currentProcess;
	
	public void step() {
		if (currentProcess != null)
			currentProcess.step();
	}
}
