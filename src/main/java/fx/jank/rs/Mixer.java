package fx.jank.rs;

public class Mixer extends PcmStream {
	Deque inputStreams;
	Deque outputStreams;
	int dataAvailable;
	int available;

	public Mixer() {
		this.inputStreams = new Deque(); // L: 9
		this.outputStreams = new Deque(); // L: 10
		this.dataAvailable = 0; // L: 11
		this.available = -1; // L: 12
	}

	public final synchronized void addInput(PcmStream var1) {
		this.inputStreams.addLast(var1); // L: 15
	} // L: 16

	public final synchronized void removeInput(PcmStream var1) {
		var1.remove(); // L: 19
	} // L: 20

	void updateAvailability() {
		if (this.dataAvailable > 0) { // L: 23
			for (MixerBuffer var1 = (MixerBuffer)this.outputStreams.last(); var1 != null; var1 = (MixerBuffer)this.outputStreams.previous()) { // L: 24
				var1.available -= this.dataAvailable; // L: 25
			}

			this.available -= this.dataAvailable; // L: 27
			this.dataAvailable = 0; // L: 28
		}

	} // L: 30

	void addBuffer(Node prev, MixerBuffer listener) {
		while (this.outputStreams.sentinel != prev && ((MixerBuffer)prev).available <= listener.available) { // L: 33
			prev = prev.previous;
		}

		Deque.insertBefore(listener, prev); // L: 34
		this.available = ((MixerBuffer)this.outputStreams.sentinel.previous).available; // L: 35
	} // L: 36

	void removeBuffer(MixerBuffer var1) {
		var1.remove(); // L: 39
		var1.remove2(); // L: 40
		Node var2 = this.outputStreams.sentinel.previous; // L: 41
		if (var2 == this.outputStreams.sentinel) {
			this.available = -1; // L: 42
		} else {
			this.available = ((MixerBuffer)var2).available; // L: 43
		}

	} // L: 44


	protected PcmStream firstSubStream() {
		return (PcmStream)this.inputStreams.last(); // L: 47
	}

	protected PcmStream nextSubStream() {
		return (PcmStream)this.inputStreams.previous(); // L: 51
	}

	protected int status() {
		return 0; // L: 55
	}

	public final synchronized void fill(int[] samples, int offset, int length) {
		do {
			if (this.available < 0) { // L: 60
				this.updateChannels(samples, offset, length); // L: 61
				return; // L: 62
			}

			if (length + this.dataAvailable < this.available) { // L: 64
				this.dataAvailable += length; // L: 65
				this.updateChannels(samples, offset, length); // L: 66
				return; // L: 67
			}

			int freeSpace = this.available - this.dataAvailable; // L: 69
			this.updateChannels(samples, offset, freeSpace); // L: 70
			offset += freeSpace; // L: 71
			length -= freeSpace; // L: 72
			this.dataAvailable += freeSpace; // L: 73
			this.updateAvailability(); // L: 74
			MixerBuffer listener = (MixerBuffer)this.outputStreams.last(); // L: 75
			synchronized(listener) { // L: 76
				int var7 = listener.update(); // L: 77
				if (var7 < 0) { // L: 78
					listener.available = 0; // L: 79
					this.removeBuffer(listener); // L: 80
				} else {
					listener.available = var7; // L: 83
					this.addBuffer(listener.previous, listener); // L: 84
				}
			}
		} while(length != 0); // L: 87

	}

	void updateChannels(int[] var1, int var2, int var3) {
		for (PcmStream var4 = (PcmStream)this.inputStreams.last(); var4 != null; var4 = (PcmStream)this.inputStreams.previous()) { // L: 92
			var4.update(var1, var2, var3); // L: 93
		}

	} // L: 95

	public final synchronized void skip(int length) {
		do {
			if (this.available < 0) { // L: 99
				this.skipSubStreams(length); // L: 100
				return; // L: 101
			}

			if (this.dataAvailable + length < this.available) { // L: 103
				this.dataAvailable += length; // L: 104
				this.skipSubStreams(length); // L: 105
				return; // L: 106
			}

			int var2 = this.available - this.dataAvailable; // L: 108
			this.skipSubStreams(var2); // L: 109
			length -= var2; // L: 110
			this.dataAvailable += var2; // L: 111
			this.updateAvailability(); // L: 112
			MixerBuffer var3 = (MixerBuffer)this.outputStreams.last(); // L: 113
			synchronized(var3) { // L: 114
				int var5 = var3.update(); // L: 115
				if (var5 < 0) { // L: 116
					var3.available = 0; // L: 117
					this.removeBuffer(var3); // L: 118
				} else {
					var3.available = var5; // L: 121
					this.addBuffer(var3.previous, var3); // L: 122
				}
			}
		} while(length != 0); // L: 125

	}

	void skipSubStreams(int var1) {
		for (PcmStream var2 = (PcmStream)this.inputStreams.last(); var2 != null; var2 = (PcmStream)this.inputStreams.previous()) { // L: 130
			var2.skip(var1); // L: 131
		}

	} // L: 133

	public abstract static class MixerBuffer extends Node {
		int available;

		abstract void remove2();
		abstract int update();
	}
}