package fx.jank.rs;

import static fx.jank.rs.SoundSystem.stereo;
import java.util.Arrays;

public class Channel
{
	public static final int CHANNELS = 8;
	static int field1423;
	protected int[] samples;
	PcmStream source;
	// Minimum
	int minSamples;
	long timeMs;
	int capacity;
	int bufferOffset;
	int chBufferSize;
	long timeout;
	int remaining;
	int field1435;
	// amount of samples
	int sampleCount;
	long end;
	boolean playing;
	int available;
	PcmStream[] channels;
	PcmStream[] tails;

	protected Channel() {
		this.minSamples = 32;
		this.timeMs = System.currentTimeMillis();
		this.timeout = 0L;
		this.remaining = 0;
		this.field1435 = 0;
		this.sampleCount = 0;
		this.end = 0L;
		this.playing = true;
		this.available = 0;
		this.channels = new PcmStream[8];
		this.tails = new PcmStream[8];
	}

	protected void init() throws Exception {
	}

	protected void open(int bufferSize) throws Exception {
	}

	protected int available() throws Exception {
		return this.capacity;
	}

	protected void write() throws Exception {
	}

	protected void close() {
	}

	protected void flush() throws Exception {
	}

	public final synchronized void setSource(PcmStream src) {
		this.source = src;
	}

	public final synchronized void run() {
		if (this.samples == null)
			return;
		long time = System.currentTimeMillis();

		try {
			if (0L != this.timeout) {
				if (time < this.timeout) {
					return;
				}

				this.open(this.capacity);
				this.timeout = 0L;
				this.playing = true;
			}

			int writeIdx = this.available();
			if (this.sampleCount - writeIdx > this.remaining) {
				this.remaining = this.sampleCount - writeIdx;
			}

			int var4 = this.bufferOffset + this.chBufferSize;
			if (var4 + 256 > 16384) {
				var4 = 16128;
			}

			if (var4 + 256 > this.capacity) {
				this.capacity += 1024;
				if (this.capacity > 16384) {
					this.capacity = 16384;
				}

				this.close();
				this.open(this.capacity);
				writeIdx = 0;
				this.playing = true;
				if (var4 + 256 > this.capacity) {
					var4 = this.capacity - 256;
					this.chBufferSize = var4 - this.bufferOffset;
				}
			}

			while (writeIdx < var4) {
				this.fill(this.samples, 256);
				this.write();
				writeIdx += 256;
			}

			if (time > this.end) {
				if (this.playing) {
					this.playing = false;
				} else if (this.remaining != 0 || this.field1435 != 0) {
					this.chBufferSize = Math.min(this.field1435, this.remaining);
					this.field1435 = this.remaining;
				} else {
					this.close();
					this.timeout = time + 2000L;
					return;
				}

				this.remaining = 0;
				this.end = 2000L + time;
			}

			this.sampleCount = writeIdx;
		} catch (Exception var7) {
			this.close();
			this.timeout = 2000L + time;
		}

		try {
			if (time > 500000L + this.timeMs) {
				time = this.timeMs;
			}

			while (time > 5000L + this.timeMs) {
				this.skip(256);
				this.timeMs += (long)(256000 / SoundSystem.sampleRate);
			}
		} catch (Exception var6) {
			this.timeMs = time;
		}

	}

	public final void play() {
		this.playing = true;
	}

	public final synchronized void stop() {
		this.playing = true;

		try {
			this.flush();
		} catch (Exception var2) {
			this.close();
			this.timeout = System.currentTimeMillis() + 2000L;
		}

	}

	final void skip(int samples) {
		this.available -= samples;
		if (this.available < 0) {
			this.available = 0;
		}

		if (this.source != null) {
			this.source.skip(samples);
		}

	}

	final void fill(int[] samples, int len) {
		int nBytes = len;
		if (stereo) {
			nBytes = len * 2;
		}

		Arrays.fill(samples, 0, nBytes, 0);
		this.available -= len;
		if (this.source != null && this.available <= 0) {
			this.available += SoundSystem.sampleRate / 16;
			PcmStream.stop(this.source);
			this.addStream(this.source, this.source.available());

			PcmStream next = processStreams();

			for (int i = 0; i < 8; ++i) {
				PcmStream last = this.channels[i];
				this.tails[i] = null;

				for (this.channels[i] = null; last != null; last = next) {
					next = last.after;
					last.after = null;
				}
			}
		}

		if (this.available < 0) {
			this.available = 0;
		}

		if (this.source != null) {
			this.source.update(samples, 0, len);
		}

		this.timeMs = System.currentTimeMillis();
	}

	final void addStream(PcmStream stream, int channelId) {
		int channel = channelId >> 5;
		PcmStream last = this.tails[channel];
		if (last == null) {
			this.channels[channel] = stream;
		} else {
			last.after = stream;
		}

		this.tails[channel] = stream;
		stream.channelId = channelId;
	}

	PcmStream processStreams() {
		PcmStream var10 = null;
		int min = 0;
		int var5 = 255;

		for (int channel = 7; var5 != 0; channel--) {
			int var7;
			int mask;
			if (channel < 0) {
				var7 = channel & 3;
				mask = -(channel >> 2);
			} else {
				var7 = channel;
				mask = 0;
			}

			for (int var9 = (var5 >>> var7) & 0x11111111; var9 != 0; var9 >>>= 4) {
				if ((var9 & 1) != 0) {
					var5 &= ~(1 << var7);
					var10 = null;
					PcmStream channelStream = this.channels[var7];

					while (channelStream != null) {

						Track track = channelStream.buffer;
						if (track != null && track.pos > mask) {
							var5 |= 1 << var7;
							var10 = channelStream;
							channelStream = channelStream.after;
							continue;
						}
						channelStream.playing = true;
						int var13 = channelStream.status();
						min += var13;
						if (track != null) {
							track.pos += var13;
						}

						if (min >= this.minSamples) {
							return var10;
						}

						PcmStream var14 = channelStream.firstSubStream();
						if (var14 != null) {
							for (int var15 = channelStream.channelId; var14 != null; var14 = channelStream.nextSubStream()) {
								this.addStream(var14, var15 * var14.available() >> 8);
							}
						}

						PcmStream var18 = channelStream.after;
						channelStream.after = null;
						if (var10 == null) {
							this.channels[var7] = var18;
						} else {
							var10.after = var18;
						}

						if (var18 == null) {
							this.tails[var7] = var10;
						}

						channelStream = var18;
					}
				}

				var7 += 4; ++mask;
			}
		}
		return var10;
	}

}