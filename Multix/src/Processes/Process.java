package Processes;

import Devices.CPU;
import Interfaces.StepByStep;
import Registers.Reg8B;
import Registers.RegB;
import Registers.StatusFlag;

public abstract class Process implements StepByStep {
	
	private final int maxNumberOfBlockingStuff = 1;
	private Reg8B CS, DS, AX, BX, CX, IP;
	private StatusFlag SF;
	private RegB CH1, CH2, CH3, TI, SI, PI, MODE, PTR;
	private CPU cpu = new CPU();
	
	public abstract void step();
}