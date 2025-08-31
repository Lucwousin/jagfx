package fx.jank.ui.components;

import fx.jank.rs.Envelope;
import fx.jank.rs.Filter;
import static fx.jank.rs.SoundSystem.sampleRate;
import fx.jank.rs.Tone;
import fx.jank.ui.SynthPanel;
import java.awt.Color;
import java.awt.Graphics;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FrequencyResponseGraph extends Graph {
	private final SynthPanel parent;
	public FrequencyResponseGraph(SynthPanel parent) {
		this.parent = parent;
		this.rasterCount = new int[]{80, 20};
		this.rasterBold = new int[]{10, 2};
	}
/*
// Java
int nPoints = 512;
double[] freq = new double[nPoints];
double[] magnitude = new double[nPoints];
double[] phase = new double[nPoints];

// Assume coefficients[0] contains a0, a1, a2, ... and coefficients[1] contains b0, b1, b2, ...
// You may need to adjust this based on your filter structure

for (int i = 0; i < nPoints; i++) {
    double omega = Math.PI * i / nPoints;
    freq[i] = omega * sampleRate / Math.PI / 2.0; // Convert to Hz

    // Example for biquad (order 2)
    double reNum = coefficients[1][0] + coefficients[1][1] * Math.cos(-omega) + coefficients[1][2] * Math.cos(-2 * omega);
    double imNum = coefficients[1][1] * Math.sin(-omega) + coefficients[1][2] * Math.sin(-2 * omega);

    double reDen = coefficients[0][0] + coefficients[0][1] * Math.cos(-omega) + coefficients[0][2] * Math.cos(-2 * omega);
    double imDen = coefficients[0][1] * Math.sin(-omega) + coefficients[0][2] * Math.sin(-2 * omega);

    double numMag = Math.sqrt(reNum * reNum + imNum * imNum);
    double denMag = Math.sqrt(reDen * reDen + imDen * imDen);

    magnitude[i] = numMag / denMag;
    phase[i] = Math.atan2(imNum, reNum) - Math.atan2(imDen, reDen);
}
 */
	protected void paintGraph(Graphics g) {
		Tone t = parent.getSelectedTone();
		Envelope tCurve = t.getTransitionCurve();
		tCurve.reset();

		g.setColor(Color.CYAN);
		double prev = getMagnitude(0);// / 100.0d;// Filter.toDecibel((float)getMagnitude(0)) / 100.d;
		log.info("0: mag = {}", prev);
		for (int x = 0; x < getWidth() - 1; x++) {
			double mag = getMagnitude(x + 1);// / 100.0d;//Filter.toDecibel((float)getMagnitude(x + 1)) / 100.d;;
			log.info("{}: mag = {}", x + 1, mag);
			int y1 = (int)((prev) * getHeight());// + (int)(t.getFilter().getGain()[0] / 65535.f * getHeight());
			int y2 = (int)((mag) * getHeight()) ;//+ (int)(t.getFilter().getGain()[0] / 65535.f * getHeight());
			g.drawLine(x, y1, x + 1, y2);
			prev = mag;
		}
	}

	private double getMagnitude(int x)
	{
		Filter filter = parent.getSelectedTone().getFilter();
		final float octaveCount = 8;//(float)(Math.log(sampleRate / 2.0 / 32.703197) / Math.log(2));

		float omega = Filter.octaveToFrequency((float)(x * octaveCount / (double)getWidth()));
		//float omega = (float)(Math.PI * x / (float) getWidth());
		float G = filter.getLinearGain();

		// Numerator
		double reNum = 0, imNum = 0;
		for (int j = 0; j < filter.getOrderN()[0]; j++)
		{
			reNum += filter.getCoefficients()[0][j] * Math.cos(-j * omega);
			imNum += filter.getCoefficients()[0][j] * Math.sin(-j * omega);
		}
		reNum *= G;
		imNum *= G;

		// Denominator
		double reDen = 1, imDen = 0;
		for (int j = 0; j < filter.getOrderN()[1]; j++)
		{
			reDen += filter.getCoefficients()[1][j] * Math.cos(-(j + 1) * omega);
			imDen += filter.getCoefficients()[1][j] * Math.sin(-(j + 1) * omega);
		}

		double numMag = reNum + imNum;
		double denMag = reDen + imDen;

		return Filter.toDecibel((float)(denMag == 0 ? 0 : numMag / denMag));
	}
}
