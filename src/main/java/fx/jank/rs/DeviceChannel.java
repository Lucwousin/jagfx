package fx.jank.rs;

import static fx.jank.rs.SoundSystem.stereo;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import static javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DeviceChannel extends Channel
{
	AudioFormat format;
	SourceDataLine line;
	int availableFrames;
	byte[] buffer;

	protected void init() {
		this.format = new AudioFormat(SoundSystem.sampleRateOut, 16, stereo ? 2 : 1, true, false);
		this.buffer = new byte[256 << (stereo ? 2 : 1)]; // L: 23
	}

	protected void open(int bufferSize) throws LineUnavailableException {
		try {
			Info info = new Info(SourceDataLine.class, this.format, bufferSize << (stereo ? 2 : 1)); // L: 29
			this.line = (SourceDataLine) AudioSystem.getLine(info); // L: 30
			this.line.open(); // L: 31
			this.line.start(); // L: 32
			this.availableFrames = bufferSize; // L: 33
		} catch (LineUnavailableException var3) { // L: 35
			if (countSetBits(bufferSize) != 1) { // L: 36
				this.open(nextPowerOfTwo(bufferSize)); // L: 37
			} else {
				this.line = null; // L: 40
				throw var3; // L: 41
			}
		}
	} // L: 38 43

	protected int available() {
		return this.availableFrames - (this.line.available() >> (stereo ? 2 : 1)); // L: 47
	}

	protected void write() {
		int sampleLen = 256; // L: 53
		if (stereo) { // L: 54
			sampleLen <<= 1;
		}

		for (int i = 0; i < sampleLen; ++i) { // L: 55
			int s = super.samples[i]; // L: 56
			if ((s + 0x800000 & 0xff000000) != 0) { // L: 57
				s = 0x7fffff ^ s >> 31; // L: 58
			}

			this.buffer[i * 2] = (byte)(s >> 8); // L: 60
			this.buffer[i * 2 + 1] = (byte)(s >> 16); // L: 61
		}

		this.line.write(this.buffer, 0, sampleLen << 1); // L: 63
	} // L: 64

	protected void close() {
		if (this.line != null) { // L: 68
			this.line.close(); // L: 69
			this.line = null; // L: 70
		}

	} // L: 72

	protected void flush() {
		this.line.flush(); // L: 76
	} // L: 77

	public static int countSetBits(int n) {
		n = (n & 0x55555555) + (n >>> 1 & 0x55555555); // L: 28
		n = (n & 0x33333333) + (n >>> 2 & 0x33333333); // L: 29
		n = n + (n >>> 4) & 0xf0f0f0f; // L: 30
		n += n >>> 8; // L: 31
		n += n >>> 16; // L: 32
		return n & 255; // L: 33
	}
	public static int nextPowerOfTwo(int n) {
		--n; // L: 50
		n |= n >>> 1; // L: 51
		n |= n >>> 2; // L: 52
		n |= n >>> 4; // L: 53
		n |= n >>> 8; // L: 54
		n |= n >>> 16; // L: 55
		return n + 1; // L: 56
	}
}
