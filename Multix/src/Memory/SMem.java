package Memory;

import java.util.ArrayList;
import Processes.Process;

public class SMem {
	ArrayList <Process> blocked = new ArrayList <Process>();
	ArrayList <Process> running = new ArrayList <Process>();
	ArrayList <Process> ready = new ArrayList <Process>();
	ArrayList <Process> suspended = new ArrayList <Process>();
	ArrayList <Process> blockedSuspended = new ArrayList <Process>();
	
	ArrayList <Resource> example;
	
}
