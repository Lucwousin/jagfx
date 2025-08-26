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

	void reset()
	{
		sampleLimit = 0;
		band = 0;
		velocity = 0;
		amplitude = 0;
		sampleIndex = 0;
	}

	int sample(int n)
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
}
