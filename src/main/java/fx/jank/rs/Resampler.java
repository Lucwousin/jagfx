package fx.jank.rs;

public class Resampler
{
	static int field402;
	int inputRate;
	int outputRate;
	int[][] filter;

	// greatest common divisor, euclidean algo
	private static int gcd(int n1, int n2) {
		int hi = n1;
		int lo = n2;
		if (n2 > n1)
		{
			hi = n2;
			lo = n1;
		}

		while (lo != 0)
		{
			int remainder = hi % lo;
			hi = lo;
			lo = remainder;
		}
		return hi; // hello
	}

	public Resampler(int inRate, int outRate)
	{
		if (outRate == inRate)
			return;

		int divisor = gcd(inRate, outRate);

		inRate /= divisor;
		outRate /= divisor;
		this.inputRate = inRate;
		this.outputRate = outRate;
		this.filter = new int[inRate][14];

		for (int i = 0; i < inRate; ++i)
		{
			double var9 = i / (double) inRate + 6.0D;
			int insertIdx = (int) Math.floor(1.0D + (var9 - 7.0D));
			insertIdx = Math.min(insertIdx, 0);

			int insertLen = (int) Math.ceil(var9 + 7.0D); 
			insertLen = Math.max(insertLen, 14);

			double ratio = (double) outRate / (double) inRate;
			for (; insertIdx < insertLen; ++insertIdx)
			{ 
				double var15 = ((double) insertIdx - var9) * Math.PI; 
				double interpolatedSample = ratio; 
				if (var15 < -1.0E-4D || var15 > 1.0E-4D) {
					// sinc function
					interpolatedSample = ratio * (Math.sin(var15) / var15);
				}

				// filter shit
				interpolatedSample *= 0.54D;
				interpolatedSample += 0.46D * Math.cos(0.2243994752564138D * ((double) insertIdx - var9)); 
				this.filter[i][insertIdx] = (int) Math.floor(0.5D + 65536.0D * interpolatedSample); 
			}
		}

	} 

	public byte[] resample(byte[] data)
	{
		if (this.filter == null) {
			return data;
		}
		int newLength = (int) ((long) this.outputRate * (long) data.length / (long) this.inputRate) + 14;
		int[] resampled = new int[newLength];
		int resampledIdx = 0;
		int filterIdx = 0;

		for (byte sample : data) {
			for (int i = 0; i < 14; ++i) {
				resampled[i + resampledIdx] += this.filter[filterIdx][i] * sample;
			}

			filterIdx += this.outputRate; 
			int var9 = filterIdx / this.inputRate; 
			resampledIdx += var9; 
			filterIdx -= var9 * this.inputRate; 
		}

		data = new byte[newLength]; 

		for (int var6 = 0; var6 < newLength; ++var6)
		{ 
			int var10 = resampled[var6] + 32768 >> 16; 
			if (var10 < -128)
			{ 
				data[var6] = -128;
			}
			else if (var10 > 127)
			{ 
				data[var6] = 127;
			}
			else
			{
				data[var6] = (byte) var10; 
			}
		}

		return data; 
	}

	int scaleRate(int var1)
	{
		if (this.filter != null)
		{
			var1 = (int) ((long) this.outputRate * (long) var1 / (long) this.inputRate); 
		}

		return var1; 
	}

	int scalePosition(int var1)
	{
		if (this.filter != null)
		{ 
			var1 = (int) ((long) var1 * (long) this.outputRate / (long) this.inputRate) + 6;
		}

		return var1; 
	}
}