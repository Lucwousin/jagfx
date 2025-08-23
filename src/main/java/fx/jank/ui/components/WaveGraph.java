package fx.jank.ui.components;

import fx.jank.rs.BufferedTrack;
import java.awt.Color;
import java.awt.Graphics;
import javax.inject.Provider;

public class WaveGraph extends Graph
{
	private final Provider<BufferedTrack> bufferProvider;
	public WaveGraph(Provider<BufferedTrack> bufferProvider) {
		this.bufferProvider = bufferProvider;
	}

	protected void paintGraph(Graphics g) {
		g.setColor(Color.GREEN);
		//todo: true wave? idk
		byte[] samples = bufferProvider.get().samples;
		int sampleCount = samples.length;
		float samplesPerPixel = sampleCount / (float)getWidth();
		int prevX = 0;
		int prevY = getHeight() / 2;
/*		for (int i = 0; i < getWidth(); i++) {
			int sampleIndex = (int)(i * samplesPerPixel);
			int sampleValue = samples[sampleIndex];
			int y = getHeight() - (int)((sampleValue + 128) * getHeight() / 256.0);
			g.drawLine(i, prevY, i, y);
			prevY = y;
		}*/

		for (int i = 0; i < sampleCount; i++) {
			int x = (int)(i / samplesPerPixel);
			int y = getHeight() - (int)((samples[i] + 128) * getHeight() / 256.0);
			g.drawLine(prevX, prevY, x, y);
			prevX = x; prevY = y;
		}
	}
}
