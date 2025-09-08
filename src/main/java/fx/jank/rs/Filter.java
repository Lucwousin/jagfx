package fx.jank.rs;

import static fx.jank.rs.Synth.SAMPLE_RATE_SYNTH;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@ToString
@Getter
@Setter
@Slf4j
public class Filter
{
	private static final float NYQUIST_FREQ = SAMPLE_RATE_SYNTH / 2.f;
	private static final float FREQ_STEP = (float)Math.PI / NYQUIST_FREQ;
	private static final float BASE_OCTAVE = 32.703197F;
	private static final float OCTAVE_SCALE = 8.0f / (float)Envelope.SCALE;
	private static final float MAG_SCALE = 200.0f / (float)Envelope.SCALE;

	float[][] coefff = new float[2][8];
	int[][] coefficients = new int[2][8];

	// current gain, as a float
	float linearGain;
	// current gain, as an int
	int linearGainInt;

	// idx 0 pole count, idx 1 zero count
	int[] orderN;
	int[][][] real;
	int[][][] imag;
	int[] gain;

	public Filter() {
		this.orderN = new int[2];
		this.real = new int[2][2][4];
		this.imag = new int[2][2][4];
		this.gain = new int[2];
	}

	private static float interpolate(float start, float end, float ratio) {
		return ratio * (end - start) + start;
	}
	private static float interpolatePoint(int[][][] ps, int f, int i, float mix) {
		return interpolate(ps[f][0][i], ps[f][1][i], mix);
	}
	private void linearizeRefGain(float mix) {
		float gainDb = interpolate(this.gain[0], this.gain[1], mix) * MAG_SCALE;
		linearGain = fromDecibel(gainDb);
		linearGainInt = (int)linearGain * Envelope.SCALE;
	}

	//
	float getStrength(int f, int i, float mix) {
		float db = interpolatePoint(imag, f, i, mix) * (MAG_SCALE / 2.0f);
		return 1.0F - fromDecibel(db);
	}


	float getFrequency(int f, int i, float mix) {
		float var4 = interpolatePoint(real, f, i, mix) * OCTAVE_SCALE;
		return octaveToRad(var4);
	}

	public int compute(int f, float mix) {
		if (f == 0) {
			linearizeRefGain(mix);
		}

		if (this.orderN[f] == 0) {
			return 0;
		}

		float y = this.getStrength(f, 0, mix);
		coefff[f][0] = -2.0F * y * (float)Math.cos(this.getFrequency(f, 0, mix));
		coefff[f][1] = y * y;

		for (int i = 1; i < this.orderN[f]; ++i) {
			float var3 = this.getStrength(f, i, mix);
			float var5 = -2.0F * var3 * (float)Math.cos(this.getFrequency(f, i, mix));
			float var6 = var3 * var3;
			coefff[f][i * 2 + 1] = coefff[f][(i - 1) * 2 + 1] * var6;
			coefff[f][i * 2] = coefff[f][i * 2 - 1] * var5 + coefff[f][i * 2 - 2] * var6;

			for (int var7 = i * 2 - 1; var7 >= 2; --var7) {
				coefff[f][var7] += coefff[f][var7 - 1] * var5 + coefff[f][var7 - 2] * var6;
			}

			coefff[f][1] += coefff[f][0] * var5 + var6;
			coefff[f][0] += var5;
		}

		if (f == 0) { 
			for (int i = 0; i < this.orderN[0] * 2; ++i) {
				coefff[0][i] *= linearGain;
			}
		}

		StringBuilder strb = new StringBuilder();
		strb.append(f == 0 ? "zero" : "pole")
			.append(" coefficients:");
		for (int i = 0; i < this.orderN[f] * 2; ++i) {
			coefficients[f][i] = (int)(coefff[f][i] * Envelope.SCALE);
			strb.append(String.format("%d): %.6f; ", i, coefff[f][i]));
		}
		log.info(strb.toString());
		log.info("{}", getStrength(1, 0, 1));

		return this.orderN[f] * 2;
	}


	final void readFrom(ByteStream in, Envelope envelope) {
		int pzCountPacked = in.getUint8();
		this.orderN[0] = pzCountPacked >> 4;
		this.orderN[1] = pzCountPacked & 15;
		if (pzCountPacked == 0) {
			this.gain[0] = this.gain[1] = 0;
			return;
		}
		log.info("Reading filter; zeros ({}) poles ({})", orderN[0], orderN[1]);

		this.gain[0] = in.getUint16();
		this.gain[1] = in.getUint16();
		log.info("gain[0] = {}, gain[1] = {}", gain[0], gain[1]);

		int var7 = in.getUint8();
		log.info("Conjungate mask; zero ({}) pole ({})", var7 >> 4, var7 & 0xf);

		for (int i = 0; i < 2; ++i) {
			for (int ptIdx = 0; ptIdx < this.orderN[i]; ++ptIdx) {
				this.real[i][0][ptIdx] = in.getUint16();
				this.imag[i][0][ptIdx] = in.getUint16();
			}
		}

		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < this.orderN[i]; ++j) {
				boolean complex = (var7 & (1 << (i * 4) << j)) != 0;
				if (complex) {
					this.real[i][1][j] = in.getUint16();
					this.imag[i][1][j] = in.getUint16();
				} else {
					this.real[i][1][j] = this.real[i][0][j];
					this.imag[i][1][j] = this.imag[i][0][j];
				}
				final String ptFormat = " x(%5d) y(%5d);";
				StringBuilder strb = new StringBuilder();
				strb.append(i == 0 ? "zero:" : "pole:")
					.append(String.format(ptFormat, this.real[i][0][j], this.imag[i][0][j]));
				if (complex)
					strb.append(String.format(ptFormat, this.real[i][1][j], this.imag[i][1][j]));
				log.info(strb.toString());
			}
		}

		if (var7 != 0 || this.gain[1] != this.gain[0]) {
			envelope.readStages(in);
			log.info("Trans curve: {}", envelope);
		}
	}

	public static float fromDecibel(float db) {
		return (float) Math.pow(0.1f, db / 20.f);
	}
	public static float toDecibel(float mag) {
		return 20.f * (float) Math.log10(mag);
	}

	public static float octaveToRad(float var0) {
		float var1 = BASE_OCTAVE * (float)Math.pow(2.0D, var0);
		return var1 * FREQ_STEP;
	}
}
