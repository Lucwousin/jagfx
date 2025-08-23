package fx.jank.rs;

public class ByteStream
{
	private final byte[] data;
	private int off;

	public ByteStream(byte[] data) {
		assert data != null;
		this.data = data;
	}

	boolean tryGet() {
		if (peek() == 0)
		{
			skip();
			return false;
		}
		return true;
	}

	int peek() {
		return data[off] & 0xFF;
	}

	void skip() {
		++off;
	}

	int getUint8() {
		return data[off++] & 0xFF;
	}

	int getUint16() {
		return ((data[off++] & 0xFF) << 8) + (data[off++] & 0xFF);
	}

	int getInt() {
		return ((data[off++] & 0xFF) << 24) + ((data[off++] & 0xFF) << 16) + ((data[off++] & 0xFF) << 8) + (data[off++] & 0xFF);
	}

	// 6 bits or 14 bits of data,
	int getVarInt16() {
		return peek() < 0x80 ? getUint8() - 0x40 : getUint16() - 0xc000;
	}

	// 7 bits or 15 bits of data, 32767 max
	int getVarUint16() {
		return peek() < 0x80 ? getUint8() : getUint16() - 0x8000;
	}

}
