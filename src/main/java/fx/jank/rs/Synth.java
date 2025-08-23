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
	public static Synth loadSynth(byte[] data)
	{
		log.debug("Loading synth from {} bytes of data", data.length);
		ByteStream in = new ByteStream(data);
		return new Synth(in);
	}

	private Tone[] tones = new Tone[10];
	@Setter
	int start;
	@Setter
	int end;

	public Synth() {};

	Synth(ByteStream in) {
		for (int i = 0; i < 10; i++) {
			if (in.tryGet()) {
				tones[i] = new Tone();
				tones[i].readFrom(in);
			}
		}

		start = in.getUint16();
		end = in.getUint16();
		log.info("Synth start {} end {}", start, end);
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

		for (int i = 0; i < 10; ++i)
		{
			mixTone(i, samples);
		}

		return samples;
	}

	private int calculateDuration() {
		int max = 0;

		int var2;
		for (var2 = 0; var2 < 10; ++var2)
		{
			if (this.tones[var2] != null && this.tones[var2].duration + this.tones[var2].offset > max)
			{
				max = this.tones[var2].duration + this.tones[var2].offset;
			}
		}
		return max;
	}

	public final int calculateDelay() {
		int minDelay = 9999999; 

		for (int i = 0; i < 10; ++i) { 
			if (this.tones[i] != null && this.tones[i].offset / 20 < minDelay) { 
				minDelay = this.tones[i].offset / 20;
			}
		}

		if (this.start < this.end && this.start / 20 < minDelay) { 
			minDelay = this.start / 20;
		}

		if (minDelay != 9999999 && minDelay != 0) { 
			for (int i = 0; i < 10; ++i) { 
				if (this.tones[i] != null) {
					this.tones[i].offset -= minDelay * 20; 
				}
			}

			if (this.start < this.end) { 
				this.start -= minDelay * 20; 
				this.end -= minDelay * 20; 
			}

			return minDelay; 
		} else {
			return 0;
		}
	}

	public void mixTone(int t, byte[] samples) {
		if (this.tones[t] == null) return;

		int sampleCount = this.tones[t].duration * 22050 / 1000;
		int sampleOffset = this.tones[t].offset * 22050 / 1000;
		int[] wave = this.tones[t].synthesize(sampleCount, this.tones[t].duration);
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
		return new BufferedTrack(22050, mix, start * 22050 / 1000, end * 22050 / 1000);
	}
}
