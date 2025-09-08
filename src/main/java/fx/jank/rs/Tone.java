package fx.jank.rs;

import static fx.jank.rs.Synth.SAMPLE_RATE_SYNTH;
import java.util.Arrays;
import java.util.Random;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;

@NoArgsConstructor
@ToString
@Getter
@Setter
@Slf4j
public class Tone {
	// total resolution
	public static final int ENVELOPE_RES = 65536;
	public static final int ENVELOPE_MAG = ENVELOPE_RES / 2;
	private static final int MAX_TONE_LEN_SEC = 10;
	private static final double SEMI_RATIO = 1.0057929410678534; // 2^1/12
	private static final int[] noise = new int[ENVELOPE_MAG];
	private static final int[] sine = new int[ENVELOPE_MAG];

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


	private Envelope freqBase = new Envelope();
	private Envelope ampBase = new Envelope();

	private Envelope freqModRate = new Envelope(); // note: these are not initialized normally but im dumb
	private Envelope freqModRange = new Envelope(); // note: these are not initialized normally but im dumb

	private Envelope ampModRate = new Envelope(); // note: these are not initialized normally but im dumb
	private Envelope ampModRange = new Envelope(); // note: these are not initialized normally but im dumb

	private Envelope gapOff = new Envelope(); // note: these are not initialized normally but im dumb
	private Envelope gapOn = new Envelope(); // note: these are not initialized normally but im dumb

	private int[] volInput = new int[5];
	private int[] semiInput = new int[5];
	private int[] delInput = new int[5];

	private int reverbDel = 0;
	private int reverbVol = 100;

	private Filter filter = new Filter();
	private Envelope transitionCurve = new Envelope();

	int len = 500;
	int pos = 0;

	private int[] samples = new int[SAMPLE_RATE_SYNTH * MAX_TONE_LEN_SEC];
	private int[] oscPhase = new int[5];
	private int[] oscDelay = new int[5];
	private int[] oscVolum = new int[5];
	private int[] oscStep = new int[5];
	private int[] oscBase = new int[5];

	void readFrom(ByteStream in)
	{
		log.info("Reading Tone!");
		freqBase.readFrom(in);
		ampBase.readFrom(in);
		log.info("FreqBase {}", freqBase);
		log.info("AmpBase {}", ampBase);

		if (in.tryGet()) {
			(freqModRate = new Envelope()).readFrom(in);
			(freqModRange = new Envelope()).readFrom(in);
			log.info("FreqModRate {}", freqModRate);
			log.info("FreqModRange {}", freqModRange);
		}
		if (in.tryGet()) {
			(ampModRate = new Envelope()).readFrom(in);
			(ampModRange = new Envelope()).readFrom(in);
			log.info("AmpModRate {}", ampModRate);
			log.info("AmpModRange {}", ampModRange);
		}
		if (in.tryGet()) {
			(gapOff = new Envelope()).readFrom(in);
			(gapOn = new Envelope()).readFrom(in);
			log.info("gapOn {}", gapOff);
			log.info("gapOff {}", gapOn);
		}

		for (int i = 0; i < 10; i++) {
			int vol = in.getVarUint16();
			if (vol == 0)
				break;

			volInput[i] = vol;
			semiInput[i] = in.getVarInt16();
			delInput[i] = in.getVarUint16();
			log.info("harmonic osc {}: vol: {}, semi: {}, del: {}", i, volInput[i], (float) semiInput[i]/10, delInput[i]);
		}
		reverbDel = in.getVarUint16();
		reverbVol = in.getVarUint16();
		log.info("reverb delay: {}, volume: {}", reverbDel, reverbVol);
		len = in.getUint16();
		pos = in.getUint16();
		log.info("Duration: {}, offset: {}", len, pos);
		filter.readFrom(in, transitionCurve);
	}

	public int[] synthesize(int samples, int ms) {
		Arrays.fill(this.samples, 0, samples, 0);
		if (ms < 10) {
			return this.samples;
		}

		double sampleRate = samples / (ms + 0.0D); // mHz
		double scale = 32.768 / sampleRate;
		freqBase.reset();
		ampBase.reset();

		Pair<Integer, Integer> tmp;
		tmp = this.calcModStep(freqModRate, freqModRange, scale);
		int freqModStep = tmp.a;
		int freqModBase = tmp.b;

		tmp = this.calcModStep(ampModRate, ampModRange, scale);
		int ampModStep = tmp.a;
		int ampModBase = tmp.b;

		for (int i = 0; i < 5; ++i) {
			if (volInput[i] != 0) {
				oscPhase[i] = 0;
				oscDelay[i] = (int)(delInput[i] * sampleRate);
				oscVolum[i] = (volInput[i] * 16384) / 100;
				oscStep[i] = (int)(scale * (freqBase.max - freqBase.min) * Math.pow(SEMI_RATIO, semiInput[i]));
				oscBase[i] = (int)(scale * (freqBase.min));
			}
		}

		runOscillators(samples, freqModStep, freqModBase, ampModStep, ampModBase);

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

	private Pair<Integer, Integer> calcModStep(Envelope envelope, Envelope rangeEnvelope, double scale) {
		if (envelope == null || envelope.waveFun == WaveFun.OFF.ordinal())
			return new Pair<>(0, 0);

		envelope.reset();
		rangeEnvelope.reset();
		int step = (int)(scale * (envelope.max - envelope.min));
		int base = (int)(scale * (envelope.min));
		return new Pair<>(step, base);
	}

	public static int evaluateWave(int phase, int amplitude, int waveform) {
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

	void runOscillators(int samples, int fmodStep, int fmodBase, int amodStep, int amodBase) {
		int fmodPhase = 0;
		int amodPhase = 0;
		int frequency;
		int amplitude;

		for (int index = 0; index < samples; ++index) {
			frequency = freqBase.sample(samples);
			amplitude = ampBase.sample(samples);
			if (freqModRate != null && freqModRate.waveFun != WaveFun.OFF.ordinal())
			{
				int fmodRate = freqModRate.sample(samples);
				int fmodMult = freqModRange.sample(samples);
				frequency += evaluateWave(fmodPhase, fmodMult, freqModRate.waveFun) >> 1;
				fmodPhase = fmodPhase + fmodBase + (fmodRate * fmodStep >> 16);
			}

			if (ampModRate != null && ampModRate.waveFun != WaveFun.OFF.ordinal()) {
				int amodRate = ampModRate.sample(samples);
				int amodMult = ampModRange.sample(samples);
				amplitude = amplitude * ((evaluateWave(amodPhase, amodRate, ampModRate.waveFun) >> 1) + 32768) >> 15;
				amodPhase = amodPhase + amodBase + (amodMult * amodStep >> 16);
			}

			for (int osc = 0; osc < 5; ++osc) {
				if (volInput[osc] != 0) {
					int tgtI = oscDelay[osc] + index;
					if (tgtI < samples) {
						this.samples[tgtI] += this.evaluateWave(oscPhase[osc], amplitude * oscVolum[osc] >> 15, freqBase.waveFun);
						this.oscPhase[osc] += (frequency * oscStep[osc] >> 16) + oscBase[osc];
					}
				}
			}
		}
	}

	void gap(int samples) {
		if (gapOff == null || gapOff.waveFun == WaveFun.OFF.ordinal())
			return;

		gapOn.reset();
		gapOff.reset();
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
		if (reverbVol > 0 && reverbDel > 0) {
			int reverbSampleLen = (int)(reverbDel * sampleRate);

			for (int i = reverbSampleLen; i < samples; ++i) {
				this.samples[i] += this.samples[i - reverbSampleLen] * this.reverbVol / 100;
			}
		}
	}

	void filter(int samples) {
		if (filter.orderN[0] <= 0 && filter.orderN[1] <= 0) {
			return;
		}

		transitionCurve.reset();
		int filterMix = transitionCurve.sample(samples + 1);
		int zeroDelay = filter.compute(0, (float)filterMix / 65536.0F);
		int poleDelay = filter.compute(1, (float)filterMix / 65536.0F);
		if (samples < zeroDelay + poleDelay) {
			return;
		}

		int i;
		int n = Math.min(poleDelay, samples - zeroDelay);
		for (i = 0; i < n; i++)
		{
			int sample = (int) (((long) this.samples[i + zeroDelay] * (long) filter.linearGainInt) >> 16);

			for (int j = 0; j < zeroDelay; ++j) {
				sample += (int)((long)this.samples[i + zeroDelay - 1 - j] * (long) filter.coefficients[0][j] >> 16);
			}

			for (int j = 0; j < i; ++j) {
				sample -= (int)((long)this.samples[i - 1 - j] * (long)filter.coefficients[1][j] >> 16);
			}

			this.samples[i] = sample;
			filterMix = this.transitionCurve.sample(samples + 1);
		}

		boolean var21 = true;
		for (n = 128; var21; n += 128) {
			if (n > samples - zeroDelay) {
				n = samples - zeroDelay;
			}

			while (i < n) {
				int sample = (int)((long)this.samples[i + zeroDelay] * (long)filter.linearGainInt >> 16);

				for (int j = 0; j < zeroDelay; ++j) {
					sample += (int)((long)this.samples[i + zeroDelay - 1 - j] * (long)filter.coefficients[0][j] >> 16);
				}

				for (int j = 0; j < poleDelay; ++j) {
					sample -= (int)((long)this.samples[i - 1 - j] * (long)filter.coefficients[1][j] >> 16);
				}

				this.samples[i] = sample;
				filterMix = this.transitionCurve.sample(samples + 1);
				++i;
			}

			if (i >= samples - zeroDelay)
			{
				filterTail(samples, i, zeroDelay, poleDelay);
				break;
			}

			zeroDelay = filter.compute(0, (float) filterMix / 65536.0F);
			poleDelay = filter.compute(1, (float) filterMix / 65536.0F);
		}
	}



	private void filterTail(int samples, int i, int zeroDelay, int poleDelay) {
		while (i < samples) {
			int sample = 0;

			for (int j = i + zeroDelay - samples; j < zeroDelay; ++j) {
				sample += (int) ((long) this.samples[i + zeroDelay - 1 - j] * (long) filter.coefficients[0][j] >> 16);
			}

			for (int j = 0; j < poleDelay; ++j) {
				sample -= (int) ((long) this.samples[i - 1 - j] * (long) filter.coefficients[1][j] >> 16);
			}

			this.samples[i] = sample;
			this.transitionCurve.sample(samples + 1);
			++i;
		}
	}

	public int[] synthAll() {
		int sampleCount = len * SAMPLE_RATE_SYNTH / 1000;
		return synthesize(sampleCount, len);
	}

	public BufferedTrack getStream() {
		int[] mix = synthAll();
		byte[] bmix = new byte[mix.length];
		for (int i = 0; i < mix.length; i++) {
			int s = mix[i] >> 8;
			if ((s + 0x80 & 0xffffff00) != 0) {
				s = (s >> 31) ^ 127; // normalize to -128..
			}
			bmix[i] = (byte)s;
		}
		return new BufferedTrack(SAMPLE_RATE_SYNTH, bmix, 0, len);
	}

	public static Tone defaultTone() {
		Tone t = new Tone();
		t.getVolInput()[0] = 50;
		return t;
	}
}
