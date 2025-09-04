package fx.jank.rs;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@ToString
@Slf4j
public class Synth
{
	public static int MAX_TONES = 10;
	public static Synth loadSynth(byte[] data)
	{
		log.debug("Loading synth from {} bytes of data", data.length);
		ByteStream in = new ByteStream(data);
		return new Synth(in);
	}

	private Tone[] tones = new Tone[MAX_TONES];
	@Setter
	int l1;
	@Setter
	int l2;

	public Synth() {};

	Synth(ByteStream in) {
		for (int i = 0; i < MAX_TONES; i++) {
			if (in.tryGet()) {
				tones[i] = new Tone();
				tones[i].readFrom(in);
			}
		}

		l1 = in.getUint16();
		l2 = in.getUint16();
		log.info("Synth start {} end {}", l1, l2);
	}

	public byte[] mix()
	{
		int totalDuration = calculateDuration();

		if (totalDuration == 0)
		{
			return new byte[0];
		}
		int sampleCount = totalDuration * 22050 / 1000;
		byte[] samples = new byte[sampleCount];

		for (int i = 0; i < MAX_TONES; ++i)
		{
			mixTone(i, samples);
		}

		return samples;
	}

	private int calculateDuration() {
		int max = 0;

		int var2;
		for (var2 = 0; var2 < MAX_TONES; ++var2)
		{
			if (this.tones[var2] != null && this.tones[var2].len + this.tones[var2].pos > max)
			{
				max = this.tones[var2].len + this.tones[var2].pos;
			}
		}
		return max;
	}

	public final int calculateDelay() {
		int minDelay = 9999999; 

		for (int i = 0; i < MAX_TONES; ++i) {
			if (this.tones[i] != null && this.tones[i].pos / 20 < minDelay) {
				minDelay = this.tones[i].pos / 20;
			}
		}

		if (this.l1 < this.l2 && this.l1 / 20 < minDelay) {
			minDelay = this.l1 / 20;
		}

		if (minDelay != 9999999 && minDelay != 0) { 
			for (int i = 0; i < MAX_TONES; ++i) {
				if (this.tones[i] != null) {
					this.tones[i].pos -= minDelay * 20;
				}
			}

			if (this.l1 < this.l2) {
				this.l1 -= minDelay * 20;
				this.l2 -= minDelay * 20;
			}

			return minDelay; 
		} else {
			return 0;
		}
	}

	public void mixTone(int t, byte[] samples) {
		if (this.tones[t] == null) return;

		int sampleCount = this.tones[t].len * 22050 / 1000;
		int sampleOffset = this.tones[t].pos * 22050 / 1000;
		int[] wave = this.tones[t].synthesize(sampleCount, this.tones[t].len);
		blend(samples, wave, sampleOffset, sampleCount);
	}

	void blend(byte[] samples, int[] wave, int off, int len) {
		for (int i = 0; i < len; ++i)
		{
			int mixedSample = (wave[i] >> 8) + samples[i + off];
			if ((mixedSample + 128 & 0xffffff00) != 0)
			{
				mixedSample = (mixedSample >> 31) ^ 127;
			}

			samples[i + off] = (byte) mixedSample;
		}
	}

	public BufferedTrack getStream() {
		final byte[] mix = mix();
		return new BufferedTrack(22050, mix, l1 * 22050 / 1000, l2 * 22050 / 1000);
	}
}
