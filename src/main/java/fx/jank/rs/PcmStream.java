package fx.jank.rs;

import lombok.Getter;

public abstract class PcmStream extends Node {
	@Getter
	volatile boolean playing;
	PcmStream after;
	int channelId;
	Track buffer;

	protected PcmStream() {
		this.playing = true;
	}

	protected abstract PcmStream firstSubStream();

	protected abstract PcmStream nextSubStream();

	protected abstract int status();

	protected abstract void fill(int[] samples, int offset, int length);

	protected abstract void skip(int length);

	int available() {
		return 255;
	}

	final void update(int[] samples, int offset, int length) {
		if (this.playing) {
			this.fill(samples, offset, length);
		} else {
			this.skip(length);
		}

	}

	public static void stop(PcmStream stream) {
		stream.playing = false; // L: 270
		if (stream.buffer != null) { // L: 271
			stream.buffer.pos = 0;
		}

		for (PcmStream var1 = stream.firstSubStream(); var1 != null; var1 = stream.nextSubStream()) {
			stop(var1); // L: 272
		}

	}
}
