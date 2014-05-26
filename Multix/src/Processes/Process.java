package Processes;

import Devices.CPU;
import Interfaces.StepByStep;

public abstract class Process implements StepByStep {
	private int ip;
	private final int maxNumberOfBlockingStuff = 1;
	private CPU cpu = new CPU();
	
	public void step()
	{
		if (ip != maxNumberOfBlockingStuff)
		{
			ip ++;
		}
		this.chooseStep(ip);
		//go back to cpu
	}
	private void chooseStep(int ip)
	{
		switch (ip)
		{
		case 1:  
			//do stuff with cpu?
			break;
		default:
			break;
		}
		
	}
	

}