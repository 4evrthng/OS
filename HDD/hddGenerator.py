import sys

hdd = open(sys.argv[1], 'wb')
hddBytes = bytearray()

for i in range(0, 512 * 255):
	hddBytes.append(0x00)
	
hdd.write(hddBytes)
hdd.close()