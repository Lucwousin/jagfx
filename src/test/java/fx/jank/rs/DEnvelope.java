package fx.jank.rs;

public class DEnvelope
{
	int segments;
	int[] durations;
	int[] phases;
	int start = 0; // different from original
	int end = 100; // different from original
	public int form;
	int ticks;
	int phaseIndex;
	int max;
	int step;
	int amplitude;

	public DEnvelope() {
		this.segments = 2; // L: 21
		this.durations = new int[2]; // L: 22
		this.phases = new int[2]; // L: 23
		this.durations[0] = 0; // L: 24
		this.durations[1] = 65535; // L: 25
		this.phases[0] = 0; // L: 26
		this.phases[1] = 65535; // L: 27
	} // L: 28

	final void reset() {
		this.ticks = 0; // L: 48
		this.phaseIndex = 0; // L: 49
		this.step = 0; // L: 50
		this.amplitude = 0; // L: 51
		this.max = 0; // L: 52
	} // L: 53

	public int doStep(int var1) {
		if (this.max >= this.ticks) { // L: 56
			this.amplitude = this.phases[this.phaseIndex++] << 15; // L: 57
			if (this.phaseIndex >= this.segments) { // L: 58
				this.phaseIndex = this.segments - 1;
			}

			this.ticks = (int)((double)this.durations[this.phaseIndex] / 65536.0D * (double)var1); // L: 59
			if (this.ticks > this.max) { // L: 60
				this.step = ((this.phases[this.phaseIndex] << 15) - this.amplitude) / (this.ticks - this.max);
			}
		}

		this.amplitude += this.step; // L: 62
		++this.max; // L: 63
		return this.amplitude - this.step >> 15; // L: 64
	}
}