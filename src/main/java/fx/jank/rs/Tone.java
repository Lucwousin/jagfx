package fx.jank.rs;

import static fx.jank.rs.SoundSystem.sampleRate;
import java.util.Arrays;
import java.util.Random;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor
@ToString
@Getter
@Setter
@Slf4j
public class Tone
{
	private static final int[] noise = new int[32768];
	private static final int[] sine = new int[32768];

	static {
		Random r = new Random(0L);
		for (int i = 0; i < noise.length; i++) {
			noise[i] = (r.nextInt() & 2) - 1;
		}
		//5215.1903 == 32768 / 2 PI
		for (int i = 0; i < sine.length; i++) {
			sine[i] = (int)(Math.sin((double) i / 5215.1903) * 16384.0D);
		}
	}

	private static int[][] defaultEnvelopeData() {
		return new int[][] {
			{0 * 65535 / 5, 1 * 65535 / 5, 2 * 65535 / 5, 3 * 65535 / 5, 4 * 65535 / 5, 5 * 65535 / 5},
			{32768, 32768, 32768, 32768, 32768, 32768}
		};
	}

	private Envelope freqBase = new Envelope(50, 100, defaultEnvelopeData());
	private Envelope ampBase = new Envelope(0, 100, defaultEnvelopeData());

	private Envelope freqModRate = new Envelope(50, 100, defaultEnvelopeData()); // note: these are not initialized normally but im dumb
	private Envelope freqModRange = new Envelope(0, 100, defaultEnvelopeData()); // note: these are not initialized normally but im dumb

	private Envelope ampModRate = new Envelope(50, 100, defaultEnvelopeData()); // note: these are not initialized normally but im dumb
	private Envelope ampModRange = new Envelope(0, 100, defaultEnvelopeData()); // note: these are not initialized normally but im dumb

	private Envelope gapOff = new Envelope(0, 0, defaultEnvelopeData()); // note: these are not initialized normally but im dumb
	private Envelope gapOn = new Envelope(0, 0, defaultEnvelopeData()); // note: these are not initialized normally but im dumb

	private int[] harmonicVolumes = new int[5];
	private int[] harmonicSemitones = new int[5];
	private int[] harmonicDelays = new int[5];

	private int reverbDelay = 0;
	private int reverbVolume = 100;

	private Filter filter = new Filter();
	private Envelope transitionCurve = new Envelope();

	int duration = 500;
	int offset = 0;

	private int[] samples = new int[sampleRate * 10];
	private int[] phases = new int[5];
	private int[] delays = new int[5];
	private int[] ampSteps = new int[5];
	private int[] freqSteps = new int[5];
	private int[] freqBaseSteps = new int[5];

	void readFrom(ByteStream in)
	{
		log.info("Reading Tone!");
		freqBase.readFrom(in);
		ampBase.readFrom(in);

		log.info("FreqBase {}", freqBase);
		log.info("AmpBase {}", ampBase);

		if (in.tryGet())
		{
			freqModRate = new Envelope();
			freqModRate.readFrom(in);
			freqModRange = new Envelope();
			freqModRange.readFrom(in);
			log.info("FreqModRate {}", freqModRate);
			log.info("FreqModRange {}", freqModRange);
		}
		if (in.tryGet())
		{
			ampModRate = new Envelope();
			ampModRate.readFrom(in);
			ampModRange = new Envelope();
			ampModRange.readFrom(in);
			log.info("AmpModRate {}", ampModRate);
			log.info("AmpModRange {}", ampModRange);
		}
		if (in.tryGet())
		{
			gapOn = new Envelope();
			gapOn.readFrom(in);
			gapOff = new Envelope();
			gapOff.readFrom(in);
			log.info("gapOn {}", gapOn);
			log.info("gapOff {}", gapOff);
		}


		for (int i = 0; i < 10; i++) {
			int vol = in.getVarUint16();
			if (vol == 0)
				break;

			harmonicVolumes[i] = vol;
			harmonicSemitones[i] = in.getVarInt16();
			harmonicDelays[i] = in.getVarUint16();
			log.info("harmonic osc {}: vol: {}, semi: {}, del: {}", i, harmonicVolumes[i], (float)harmonicSemitones[i]/10, harmonicDelays[i]);
		}
		reverbDelay = in.getVarUint16();
		reverbVolume = in.getVarUint16();
		log.info("reverb delay: {}, volume: {}", reverbDelay, reverbVolume);
		duration = in.getUint16();
		offset = in.getUint16();
		log.info("Duration: {}, offset: {}", duration, offset);
		filter.readFrom(in, transitionCurve);
	}

	public int[] synthesize(int samples, int ms) {
		Arrays.fill(this.samples, 0, samples, 0);
		if (ms < 10) {
			return this.samples;
		}

		double sampleRate = samples / (ms + 0.0D); // mHz
		freqBase.reset();
		ampBase.reset();
		int freqModStep = 0;
		int freqModBase = 0;
		int freqModPhase = 0;
		if (freqModRate != null && freqModRate.waveFun != WaveFun.OFF.ordinal()) {
			freqModRate.reset();
			freqModRange.reset();
			freqModStep = (int)((freqModRate.max - freqModRate.min) * 32.768D / sampleRate);
			freqModBase = (int)(freqModRate.min * 32.768D / sampleRate);
		}

		int ampModStep = 0;
		int ampModBase = 0;
		int ampModPhase = 0;
		if (ampModRate != null && ampModRate.waveFun != WaveFun.OFF.ordinal()) {
			ampModRate.reset();
			ampModRange.reset();
			ampModStep = (int)((ampModRate.max - ampModRate.min) * 32.768D / sampleRate);
			ampModBase = (int)(ampModRate.min * 32.768D / sampleRate);
		}

		for (int i = 0; i < 5; ++i) {
			if (harmonicVolumes[i] != 0) {
				phases[i] = 0;
				delays[i] = (int)(harmonicDelays[i] * sampleRate);
				ampSteps[i] = (harmonicVolumes[i] * 16384) / 100;
				freqSteps[i] = (int)((freqBase.max - freqBase.min) * 32.768D * Math.pow(1.0057929410678534D, harmonicSemitones[i]) / sampleRate);
				freqBaseSteps[i] = (int)(freqBase.min * 32.768D / sampleRate);
			}
		}

		runOscillators(samples, freqModStep, freqModBase, freqModPhase, ampModStep, ampModBase, ampModPhase);

		gap(samples);

		reverb(samples, sampleRate);

		filter(samples);

		for (int i = 0; i < samples; ++i) {
			if (this.samples[i] < -32768) {
				this.samples[i] = -32768;
			}

			if (this.samples[i] > 32767) {
				this.samples[i] = 32767;
			}
		}

		return this.samples;
	}

	int evaluateWave(int phase, int amplitude, int waveform) {
		if (waveform == 1) {
			return (phase & 32767) < 16384 ? amplitude
				: -amplitude;
		} else if (waveform == 2) {
			return sine[phase & 32767] * amplitude >> 14;
		} else if (waveform == 3) {
			return (amplitude * (phase & 32767) >> 14) - amplitude;
		} else if (waveform == 4) {
			return amplitude * noise[phase / 2607 & 32767];
		} else {
			return 0;
		}
	}

	void runOscillators(int samples,
						int freqModStep, int freqModBase, int freqModPhase,
						int ampModStep, int ampModBase, int ampModPhase)
	{
		int frequency;
		int amplitude;

		for (int index = 0; index < samples; ++index) {
			frequency = freqBase.sample(samples);
			amplitude = ampBase.sample(samples);
			if (freqModRate != null && freqModRate.waveFun != WaveFun.OFF.ordinal())
			{
				int fmodRate = freqModRate.sample(samples);
				int fmodMult = freqModRange.sample(samples);
				frequency += evaluateWave(freqModPhase, fmodMult, freqModRate.waveFun) >> 1;
				freqModPhase = freqModPhase + freqModBase + (fmodRate * freqModStep >> 16);
			}

			if (ampModRate != null && ampModRate.waveFun != WaveFun.OFF.ordinal()) {
				int amodRate = ampModRate.sample(samples);
				int amodMult = ampModRange.sample(samples);
				amplitude = amplitude * ((evaluateWave(ampModPhase, amodRate, ampModRate.waveFun) >> 1) + 32768) >> 15;
				ampModPhase = ampModPhase + ampModBase + (amodMult * ampModStep >> 16);
			}

			for (int osc = 0; osc < 5; ++osc) {
				if (harmonicVolumes[osc] != 0) {
					int tgtI = delays[osc] + index;
					if (tgtI < samples) {
						this.samples[tgtI] += this.evaluateWave(phases[osc], amplitude * ampSteps[osc] >> 15, freqBase.waveFun);
						this.phases[osc] += (frequency * freqSteps[osc] >> 16) + freqBaseSteps[osc];
					}
				}
			}
		}
	}

	void gap(int samples) {
		if (gapOff == null || gapOff.waveFun == WaveFun.OFF.ordinal())
			return;

		gapOff.reset();
		gapOn.reset();
		int progress = 0;
		int threshold;
		boolean first = true;

		for (int i = 0; i < samples; ++i) {
			int release = gapOff.sample(samples);
			int attack = gapOn.sample(samples);
			if (first) {
				threshold = (release * (gapOff.max - gapOff.min) >> 8) + gapOff.min;
			} else {
				threshold = (attack * (gapOff.max - gapOff.min) >> 8) + gapOff.min;
			}

			progress += 256;
			if (progress >= threshold) {
				progress = 0;
				first = !first;
			}

			if (first) {
				this.samples[i] = 0;
			}
		}
	}

	void reverb(int samples, double sampleRate) {
		if (reverbVolume > 0 && reverbDelay > 0) {
			int reverbSampleLen = (int)(reverbDelay * sampleRate);

			for (int i = reverbSampleLen; i < samples; ++i) {
				this.samples[i] += this.samples[i - reverbSampleLen] * this.reverbVolume / 100;
			}
		}
	}

	void filter(int samples) {
		if (filter.bands[0] <= 0 && filter.bands[1] <= 0) {
			return;
		}

		transitionCurve.reset();
		int filterMix = transitionCurve.sample(samples + 1);
		int filterStart = filter.compute(0, (float)filterMix / 65536.0F);
		int filterLength = filter.compute(1, (float)filterMix / 65536.0F);
		if (samples < filterStart + filterLength) {
			return;
		}

		int i;
		int n = Math.min(filterLength, samples - filterStart);
		for (i = 0; i < n; i++)
		{
			int sample = (int)((long)this.samples[i + filterStart] * (long)filter.pregainInt >> 16);

			for (int j = 0; j < filterStart; ++j) {
				sample += (int)((long)this.samples[i + filterStart - 1 - j] * (long) filter.coefficients[0][j] >> 16);
			}

			for (int j = 0; j < i; ++j) {
				sample -= (int)((long)this.samples[i - 1 - j] * (long)filter.coefficients[1][j] >> 16);
			}

			this.samples[i] = sample;
			filterMix = this.transitionCurve.sample(samples + 1);
		}

		boolean var21 = true;
		for (n = 128; var21; n += 128) {
			if (n > samples - filterStart) {
				n = samples - filterStart;
			}

			while (i < n) {
				int sample = (int)((long)this.samples[i + filterStart] * (long)filter.pregainInt >> 16);

				for (int j = 0; j < filterStart; ++j) {
					sample += (int)((long)this.samples[i + filterStart - 1 - j] * (long)filter.coefficients[0][j] >> 16);
				}

				for (int j = 0; j < filterLength; ++j) {
					sample -= (int)((long)this.samples[i - 1 - j] * (long)filter.coefficients[1][j] >> 16);
				}

				this.samples[i] = sample;
				filterMix = this.transitionCurve.sample(samples + 1);
				++i;
			}

			if (i >= samples - filterStart) {
				break;
			}
			filterStart = filter.compute(0, (float) filterMix / 65536.0F);
			filterLength = filter.compute(1, (float) filterMix / 65536.0F);
		}

		while (i < samples)
		{
			int j = 0;

			for (int var18 = i + filterStart - samples; var18 < filterStart; ++var18)
			{
				j += (int) ((long) this.samples[i + filterStart - 1 - var18] * (long) filter.coefficients[0][var18] >> 16);
			}

			for (int var18 = 0; var18 < filterLength; ++var18)
			{
				j -= (int) ((long) this.samples[i - 1 - var18] * (long) filter.coefficients[1][var18] >> 16);
			}

			this.samples[i] = j;
			this.transitionCurve.sample(samples + 1);
			++i;
		}
	}

	public int[] synthAll(int sampleRate) {
		int sampleCount = duration * sampleRate / 1000;
		return synthesize(sampleCount, duration);
	}

	public BufferedTrack getStream() {
		int[] mix = synthAll(sampleRate);
		byte[] bmix = new byte[mix.length];
		for (int i = 0; i < mix.length; i++) {
			int s = mix[i] >> 8;
			if ((s + 0x80 & 0xffffff00) != 0) {
				s = (s >> 31) ^ 127; // normalize to -128..
			}
			bmix[i] = (byte)s;
		}
		return new BufferedTrack(sampleRate, bmix, 0, duration);
	}
}
