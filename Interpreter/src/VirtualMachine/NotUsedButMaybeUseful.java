
	public void IN(byte cmdB) {
		if (CX.value/8+(cmdB&0xFF) >= 256) {
			//daugiau nei atmintis reiktu error
			//break;
		}
		char c;
		String input = "";
		AX.value = 0;
		BufferedReader br = new	BufferedReader(new InputStreamReader(System.in));
		for(int i=0; i<CX.value;i++) {
			try {
				c = (char) br.read();
			} catch (IOException e) {
				break;         //TODO kazkaip apdoroti ar ka?
			}
			input +=c;
			AX.value++;
		}
		writeToMemoryAX(cmdB, input);
	}
	
	public void writeToMemoryAX(byte cmdB, String input) {
		long mem = 0;
		byte b;
		int words = (int) AX.value/8;
		int chars = (int) AX.value%8;
		for(int i=0; i<words; i++) {
			mem = 0;
			for(int j=0; j<8; j++) {
				b = (byte) input.charAt(i*8+j);
				mem = mem | (b << (7-j)*8);
			}
			memory[block(cmdB+i)][word(cmdB+i)] = mem;
		}
		for(int i=0; i<chars; i++) {
			b = (byte) input.charAt(words*8+i);
			mem = mem | (b << (7-i)*8);
		}
		memory[block(cmdB+words)][word(cmdB+words)] = mem;
	}
	
	
	public String readFromMemoryCX(byte cmdB) {
		String s = "";
		if (CX.value/8+(cmdB&0xFF) >= 256) {
			//TODO daugiau nei atmintis reiktu error
			//break;
		}
		long word = 0;
		int words =(int) CX.value/8;
		byte cha = 0;
		for(int i=0; i<words; i++) {
			word = memory[block(cmdB+i)][word(cmdB+i)];
			for(int j=0;j<8;j++) {
				cha = (byte) ((word >>>(7-j)*8)&0xFF);
				s+=((char)cha);
			}
		}
		int chars = (int) (CX.value%8);
		if (chars != 0) {
			word = memory[block(cmdB+words)][word(cmdB+words)];
			for(int i=0;i<chars;i++) {
				cha = (byte) ((word >>>(7-i)*8)&0xFF);
				s+=(char)cha;
			}
		}
		return s;
	}