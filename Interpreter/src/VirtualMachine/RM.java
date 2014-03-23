// pati pati prad=ia
package VirtualMachine;

import java.io.File;

public class RM {
	
	File[] f = null; //padaryti ,kad open file failus saugotu cia or smt...
	
	public RM() {
	}
	

	public static void main(String[] args) {
		Interpretator VM = new Interpretator();
		boolean a = true;
		while (a) {
			System.out.println();
			a = VM.interpreting();
		}
	}
}