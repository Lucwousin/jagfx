package fx.jank.ui.components;

import fx.jank.rs.Filter;
import fx.jank.rs.Synth;
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

	protected void paintGraph(Graphics g) {
		g.setColor(Color.GREEN);
		var filter = parent.getSelectedTone().getFilter();
		int nPoints = getWidth();
		double[] freq = new double[nPoints];
		double[] magnitude = new double[nPoints];

		int N1 = filter.getOrderN()[0];
		int N2 = filter.getOrderN()[1];
		double G = filter.getLinearGain();

		for (int i = 0; i < nPoints; i++) {
			double omega = Filter.octaveToRad(8.0f * i / nPoints);
			freq[i] = omega * Synth.SAMPLE_RATE_SYNTH / (2 * Math.PI);

			double reNum = 0, imNum = 0;
			for (int j = 0; j < N1; j++) {
				reNum += filter.getCoefff()[1][j] * Math.cos(-j * omega);
				imNum += filter.getCoefff()[1][j] * Math.sin(-j * omega);
			}
			reNum *= G;
			imNum *= G;

			double reDen = 1, imDen = 0;
			for (int j = 0; j < N2; j++) {
				reDen += filter.getCoefff()[0][j] * Math.cos(-(j + 1) * omega);
				imDen += filter.getCoefff()[0][j] * Math.sin(-(j + 1) * omega);
			}

			double numMag = Math.sqrt(reNum * reNum + imNum * imNum);
			double denMag = Math.sqrt(reDen * reDen + imDen * imDen);

			magnitude[i] = denMag == 0 ? 0 : numMag / denMag;
		}
		for (int i = 1; i < magnitude.length; i++) {
			g.drawLine(i - 1, (int)(magnitude[i - 1] * getHeight()), i, (int)(magnitude[i] * getHeight()));
		}
	}
}
