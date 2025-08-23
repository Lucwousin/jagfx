package fx.jank.rs;

import static fx.jank.rs.SoundSystem.sampleRate;
import lombok.ToString;

@ToString
public class Filter
{
	float[][] field413 = new float[2][8];
	int[][] coefficients = new int[2][8];

	// current gain, as a float
	float pregainFloat;
	// current gain, as an int
	int pregainInt;

	int[] bands;
	int[][][] field406;
	int[][][] field407;
	int[] pregain;

	Filter() {
		// 0-15
		this.bands = new int[2];

		this.field406 = new int[2][2][4];
		this.field407 = new int[2][2][4];
		// idx 0 start, idx 2 end?
		this.pregain = new int[2];
	}

	private void calculateGain(float mix) {
		float gain = (float)this.pregain[0] + (float)(this.pregain[1] - this.pregain[0]) * mix;
		gain *= 100.f * 65536.f / (float) Integer.MAX_VALUE;
		pregainFloat = (float)Math.pow(0.1D, (gain / 20.0F)); 
		pregainInt = (int)(pregainFloat * 65536.0F); 
	}
	float getMagnitude(int f, int i, float mix) {
		float db = (float)this.field407[f][0][i] + mix * (float)(this.field407[f][1][i] - this.field407[f][0][i]); 
		db *= 100.f / 65536.f; 
		return 1.0F - (float)Math.pow(10.0D, -db / 20.0F); 
	}

	float getPhaseShift(int f, int i, float mix) {
		float var4 = (float)this.field406[f][0][i] + mix * (float)(this.field406[f][1][i] - this.field406[f][0][i]); 
		var4 *= 8.f / 65536.f; 
		return normalize(var4); 
	}

	int compute(int f, float mix) {
		float var3;
		if (f == 0) {
			calculateGain(mix);
		}

		if (this.bands[f] == 0) { 
			return 0;
		}

		var3 = this.getMagnitude(f, 0, mix); 
		field413[f][0] = -2.0F * var3 * (float)Math.cos((double)this.getPhaseShift(f, 0, mix)); 
		field413[f][1] = var3 * var3; 

		int var4;
		for (var4 = 1; var4 < this.bands[f]; ++var4) { 
			var3 = this.getMagnitude(f, var4, mix); 
			float var5 = -2.0F * var3 * (float)Math.cos((double)this.getPhaseShift(f, var4, mix)); 
			float var6 = var3 * var3; 
			field413[f][var4 * 2 + 1] = field413[f][var4 * 2 - 1] * var6; 
			field413[f][var4 * 2] = field413[f][var4 * 2 - 1] * var5 + field413[f][var4 * 2 - 2] * var6; 

			for (int var7 = var4 * 2 - 1; var7 >= 2; --var7) { 
				field413[f][var7] += field413[f][var7 - 1] * var5 + field413[f][var7 - 2] * var6;
			}

			field413[f][1] += field413[f][0] * var5 + var6;
			field413[f][0] += var5;
		}

		if (f == 0) { 
			for (var4 = 0; var4 < this.bands[0] * 2; ++var4) {
				field413[0][var4] *= pregainFloat;
			}
		}

		for (var4 = 0; var4 < this.bands[f] * 2; ++var4) { 
			coefficients[f][var4] = (int)(field413[f][var4] * 65536.0F);
		}

		return this.bands[f] * 2; 
	}


	final void readFrom(ByteStream in, Envelope envelope) {
		int var3 = in.getUint8();
		// 0 - 15...
		this.bands[0] = var3 >> 4;
		this.bands[1] = var3 & 15;
		if (var3 == 0)
		{
			this.pregain[0] = this.pregain[1] = 0;
			return;
		}

		this.pregain[0] = in.getUint16();
		this.pregain[1] = in.getUint16();
		int var7 = in.getUint8();

		for (int i = 0; i < 2; ++i) {
			for (int var6 = 0; var6 < this.bands[i]; ++var6) {
				this.field406[i][0][var6] = in.getUint16();
				this.field407[i][0][var6] = in.getUint16();
			}
		}

		for (int i = 0; i < 2; ++i) {
			for (int var6 = 0; var6 < this.bands[i]; ++var6) {
				if ((var7 & (1 << (i * 4) << var6)) != 0) {
					this.field406[i][1][var6] = in.getUint16();
					this.field407[i][1][var6] = in.getUint16();
				} else {
					this.field406[i][1][var6] = this.field406[i][0][var6];
					this.field407[i][1][var6] = this.field407[i][0][var6];
				}
			}
		}

		if (var7 != 0 || this.pregain[1] != this.pregain[0])
			envelope.readStages(in);
	}

	static float normalize(float var0) {
		final float C0Hz = 32.703197F;
		float var1 = C0Hz * (float)Math.pow(2.0D, (double)var0); 
		return var1 * (float) Math.PI / (sampleRate / 2.f);
	}

}
