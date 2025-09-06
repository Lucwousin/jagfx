package fx.jank.ui.components;

import static fx.jank.rs.Synth.SAMPLE_RATE_SYNTH;
import fx.jank.rs.Tone;
import java.awt.Color;
import java.awt.Graphics;
import java.util.function.Supplier;

public class WaveGraph extends Graph
{
	private final Supplier<Tone> bufferProvider;
	public WaveGraph(Supplier<Tone> bufferProvider) {
		this.bufferProvider = bufferProvider;
	}

	protected void paintGraph(Graphics g) {
		g.setColor(Color.GREEN);
		Tone tone = bufferProvider.get();
		//todo: true wave? idk
		int[] samples = tone.getSamples();
		int sampleCount = tone.getLen() * SAMPLE_RATE_SYNTH / 1000;
		float xScale = getWidth() / (float)sampleCount;
		float yScale = getHeight() / 65536.f;
		int midY = getHeight() / 2;
		int prevX = 0;
		int prevY = midY;

		for (int i = 0; i < sampleCount; i++) {
			int x = (int)(i * xScale);
			int y = midY - (int)(samples[i] * yScale);
			g.drawLine(prevX, prevY, x, y);
			prevX = x; prevY = y;
		}
	}
}
