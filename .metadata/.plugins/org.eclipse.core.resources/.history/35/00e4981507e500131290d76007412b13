package VirtualMachine;

public class StatusFlag extends RegB {
	byte value;
	
	public StatusFlag() {
		super();
	}
	
	public void setTF() {
		value = (byte)(value | 1 << 7);
	}
	
	public void clearTF() {
		value = (byte)(value & ~(1 << 7));
	}
	
	public boolean getTF() {
		return 0x1 == ((value >>> 7)&0x1);
	}
	
	public void setCF() {
		value = (byte)(value | 1 << 6);
	}
	
	public void clearCF() {
		value = (byte)(value & ~(1 << 6));
	}
	
	public boolean getCF() {
		return 0x1 == ((value >>> 6)&0x1);
	}

	public void setZF() {
		value = (byte)(value | 1 << 4);
	}
	
	public void clearZF() {
		value = (byte)(value & ~(1 << 4));
	}
	
	public boolean getZF() {
		return 0x1 == ((value >>> 4)&0x1);
	}

	public void setSF() {
		value = (byte)(value | 1 << 2);
	}
	
	public void clearSF() {
		value = (byte)(value & ~(1 << 2));
	}
	
	public boolean getSF() {
		return 0x1 == ((value >>> 2)&0x1);
	}

	public void setOF() {
		value = (byte)(value | 1 << 0);
	}
	
	public void clearOF() {
		value = (byte)(value & ~(1 << 0));
	}
	
	public boolean getOF() {
		return 0x1 == ((value >>> 6)&0x1);
	}
	
}