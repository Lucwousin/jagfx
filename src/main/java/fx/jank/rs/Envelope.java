package fx.jank.rs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class Envelope
{
	private int bands = 2;
	private int[] x = {0, 65535};
	private int[] y = {0, 65535};

	// ie minHz
	int min = 0;
	// ie maxHz
	int max = 100;
	// waveform (off, sqr, sin, saw, noise)
	int waveFun = 0;
	private int sampleLimit;
	private int band;
	private int sampleIndex;
	private int velocity;
	private int amplitude;

	public Envelope(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public Envelope(int min, int max, int[][] data) {
		this(min, max);
		this.x = data[0];
		this.y = data[1];
		this.bands = Math.min(x.length, y.length);
	}

	void readFrom(ByteStream in)
	{
		waveFun = in.getUint8();

		min = in.getInt();
		max = in.getInt();

		readStages(in);

		//log.info("env {}", this);
	}

	void readStages(ByteStream in)
	{
		bands = in.getUint8();

		x = new int[bands];
		y = new int[bands];

		for (int i = 0; i < bands; i++)
		{
			x[i] = in.getUint16();
			y[i] = in.getUint16();
		}
	}

	public void reset()
	{
		sampleLimit = 0;
		band = 0;
		velocity = 0;
		amplitude = 0;
		sampleIndex = 0;
	}

	public int sample(int n)
	{
		if (sampleIndex >= sampleLimit)
		{
			amplitude = y[band++] * 32768;
			if (band >= bands)
			{
				band = bands - 1;
			}

			sampleLimit = (int) (((double) x[band] / 65536.0D) * (double) n);
			if (sampleLimit > sampleIndex)
			{
				velocity = ((y[band] << 15) - this.amplitude) / (sampleLimit - sampleIndex);
			}
		}

		amplitude += velocity;
		++sampleIndex;
		return (amplitude - velocity) / 32768;
	}

	public int getMagnitude() {
		int max = 0;
		for (int mag : y) {
			max = Math.max(max, mag);
		}
		return max;
	}

	@Override
	public String toString() {
		StringBuilder strb = new StringBuilder();
		strb.append("Envelope (").append(bands).append("), ")
			.append("min: ").append(min).append(", ")
			.append("max: ").append(max).append('\n');
		for (int i = 0; i < bands; i++) {
			strb.append(i).append(") ")
				.append("x: ").append(x[i]).append("; ")
				.append("y: ").append(y[i]).append('\n');
		}
		return strb.toString();
	}
	
	public int insertPoint(int newX, int newY) {
		int i = 0;
		while (i++ < bands - 1)
			if (x[i] > newX)
				break;
		int[] newXs = new int[bands + 1];
		int[] newYs = new int[bands + 1];
		System.arraycopy(x, 0, newXs, 0, i);
		System.arraycopy(y, 0, newYs, 0, i);
		newXs[i] = newX;
		newYs[i] = newY;
		System.arraycopy(x, i, newXs, i + 1, bands - i);
		System.arraycopy(y, i, newYs, i + 1, bands - i);
		x = newXs;
		y = newYs;
		bands++;
		return i;
	}

	public void deletePoint(int index) {
		if (bands == 2)
			return;
		if (index == 0) {
			y[0] = y[1];
			index = 1;
		} else if (index == bands - 1) {
			y[bands - 1] = y[bands - 2];
			index = bands - 1;
		}
		int[] newX = new int[bands - 1];
		int[] newY = new int[bands - 1];
		System.arraycopy(x, 0, newX, 0, index);
		System.arraycopy(y, 0, newY, 0, index);
		System.arraycopy(x, index + 1, newX, index, bands - index - 1);
		System.arraycopy(y, index + 1, newY, index, bands - index - 1);
		x = newX;
		y = newY;
		bands--;
	}
}
