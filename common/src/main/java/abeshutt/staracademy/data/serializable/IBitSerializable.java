package abeshutt.staracademy.data.serializable;

import abeshutt.staracademy.data.bit.BitBuffer;

public interface IBitSerializable {

	void writeBits(BitBuffer buffer);

	void readBits(BitBuffer buffer);

}
