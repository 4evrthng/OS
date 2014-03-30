package VirtualMachine;

public class SharedMemoryDesc {
	byte PTR;
	boolean[] semafors = new boolean[16];
	
	public SharedMemoryDesc(byte firstPage) {
		PTR = firstPage;
	}
	
	public void setSemafor(int i) {
		semafors[i] = true;
	}
	
	public void clearSemafor(int i) {
		semafors[i] = false;
	}
	public boolean getSemafor(int i) {
		return semafors[i];
	}
}
