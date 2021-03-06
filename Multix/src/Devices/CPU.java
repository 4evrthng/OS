package Devices;

import Interfaces.StepByStep;
import Processes.Process;
import Registers.Reg8B;
import Registers.RegB;
import Registers.StatusFlag;

public class CPU implements StepByStep {
	
	private Process currentProcess;
	
	public Reg8B CS, DS, AX, BX, CX, IP;
	public StatusFlag SF;
	public RegB CH1, CH2, CH3, TI, SI, PI, MODE, PTR;
	
	public CPU() {
		CS = new Reg8B();
		DS = new Reg8B();
		AX = new Reg8B();
		BX = new Reg8B();
		CX = new Reg8B();
		IP = new Reg8B();
		SF = new StatusFlag();
		CH1 = new RegB();
		CH2 = new RegB();
		CH3 = new RegB();
		TI = new RegB();
		SI = new RegB();
		PI = new RegB();
		MODE = new RegB();
		PTR = new RegB();
	}
	
	public void step() {
		if (currentProcess != null)
			currentProcess.step();
	}
}
