package VirtualMachine;

import java.util.LinkedList;

public class SMem {
	private static final int MAX_PAGES = 128;
	int[] pageTable = new int[MAX_PAGES];
	LinkedList<VMDesc> desc = new LinkedList<VMDesc>();
	SharedMemoryDesc shareMem = null;

}
