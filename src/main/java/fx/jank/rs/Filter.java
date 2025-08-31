package fx.jank.rs;

import static fx.jank.rs.SoundSystem.sampleRate;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Filter
{
	float[][] coefficientsFloat = new float[2][8];
	int[][] coefficients = new int[2][8];

	// current gain, as a float
	float linearGain;
	// current gain, as an int
	int linearGainInt;

	// idx 0 pole count, idx 1 zero count
	int[] orderN;
	int[][][] pzXs;
	int[][][] pzYs;
	int[] gain;

	Filter() {
		// 0-15
		this.orderN = new int[2];

		this.pzXs = new int[2][2][4];
		this.pzYs = new int[2][2][4];
		// idx 0 start, idx 2 end?
		this.gain = new int[2];
	}

	private void linearizeRefGain(float mix) {
		float gainDb = (float)this.gain[0] + (float)(this.gain[1] - this.gain[0]) * mix;
		gainDb *= 100.f * 65536.f / (float) Integer.MAX_VALUE;
		linearGain = fromDecibel(gainDb);
		linearGainInt = (int)(linearGain * 65536.0F);
	}
	float getBand(int f, int i, float mix) {
		float db = (float)this.pzYs[f][0][i] + mix * (float)(this.pzYs[f][1][i] - this.pzYs[f][0][i]);
		db *= 100.f / 65536.f; 
		return 1.0F - fromDecibel(db);
	}


	float getCenterFrequency(int f, int i, float mix) {
		float var4 = (float)this.pzXs[f][0][i] + mix * (float)(this.pzXs[f][1][i] - this.pzXs[f][0][i]);
		var4 *= 8.f / 65536.f; 
		return octaveToFrequency(var4);
	}

	int compute(int f, float mix) {
		float var3;
		if (f == 0) {
			linearizeRefGain(mix);
		}

		if (this.orderN[f] == 0) {
			return 0;
		}

		var3 = this.getBand(f, 0, mix);
		coefficientsFloat[f][0] = -2.0F * var3 * (float)Math.cos((double)this.getCenterFrequency(f, 0, mix));
		coefficientsFloat[f][1] = var3 * var3;

		int var4;
		for (var4 = 1; var4 < this.orderN[f]; ++var4) {
			var3 = this.getBand(f, var4, mix);
			float var5 = -2.0F * var3 * (float)Math.cos((double)this.getCenterFrequency(f, var4, mix));
			float var6 = var3 * var3;
			coefficientsFloat[f][var4 * 2 + 1] = coefficientsFloat[f][var4 * 2 - 1] * var6;
			coefficientsFloat[f][var4 * 2] = coefficientsFloat[f][var4 * 2 - 1] * var5 + coefficientsFloat[f][var4 * 2 - 2] * var6;

			for (int var7 = var4 * 2 - 1; var7 >= 2; --var7) { 
				coefficientsFloat[f][var7] += coefficientsFloat[f][var7 - 1] * var5 + coefficientsFloat[f][var7 - 2] * var6;
			}

			coefficientsFloat[f][1] += coefficientsFloat[f][0] * var5 + var6;
			coefficientsFloat[f][0] += var5;
		}

		if (f == 0) { 
			for (var4 = 0; var4 < this.orderN[0] * 2; ++var4) {
				coefficientsFloat[0][var4] *= linearGain;
			}
		}

		for (var4 = 0; var4 < this.orderN[f] * 2; ++var4) {
			coefficients[f][var4] = (int)(coefficientsFloat[f][var4] * 65536.0F);
		}

		return this.orderN[f] * 2;
	}


	final void readFrom(ByteStream in, Envelope envelope) {
		int pzCountPacked = in.getUint8();
		// 0 - 15...
		this.orderN[0] = pzCountPacked >> 4;
		this.orderN[1] = pzCountPacked & 15;
		if (pzCountPacked == 0)
		{
			this.gain[0] = this.gain[1] = 0;
			return;
		}

		this.gain[0] = in.getUint16();
		this.gain[1] = in.getUint16();
		int var7 = in.getUint8();

		for (int i = 0; i < 2; ++i) {
			for (int ptIdx = 0; ptIdx < this.orderN[i]; ++ptIdx) {
				this.pzXs[i][0][ptIdx] = in.getUint16();
				this.pzYs[i][0][ptIdx] = in.getUint16();
			}
		}

		for (int i = 0; i < 2; ++i) {
			for (int var6 = 0; var6 < this.orderN[i]; ++var6) {
				if ((var7 & (1 << (i * 4) << var6)) != 0) {
					this.pzXs[i][1][var6] = in.getUint16();
					this.pzYs[i][1][var6] = in.getUint16();
				} else {
					this.pzXs[i][1][var6] = this.pzXs[i][0][var6];
					this.pzYs[i][1][var6] = this.pzYs[i][0][var6];
				}
			}
		}

		if (var7 != 0 || this.gain[1] != this.gain[0])
			envelope.readStages(in);
	}

	public static float fromDecibel(float db) {
		return (float) Math.pow(0.1f, db / 20.f);
	}
	public static float toDecibel(float mag) {
		return 20.f * (float) Math.log10(mag);
	}

	public static float octaveToFrequency(float var0) {
		final float C0Hz = 32.703197F;
		float var1 = C0Hz * (float)Math.pow(2.0D, (double)var0); 
		return var1 * (float) Math.PI / (sampleRate / 2.f);
	}
	public static float frequencyToOctave(float var0) {
		final float C0Hz = 32.703197F;
		float var1 = C0Hz * (float)Math.pow(2.0D, (double)var0);
		return var1;
	}
}
