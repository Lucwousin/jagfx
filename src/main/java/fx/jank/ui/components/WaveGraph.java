package fx.jank.ui.components;

import static fx.jank.rs.SoundSystem.sampleRate;
import fx.jank.rs.Tone;
import java.awt.Color;
import java.awt.Graphics;
import javax.inject.Provider;

public class WaveGraph extends Graph
{
	private final Provider<Tone> bufferProvider;
	public WaveGraph(Provider<Tone> bufferProvider) {
		this.bufferProvider = bufferProvider;
	}

	protected void paintGraph(Graphics g) {
		g.setColor(Color.GREEN);
		Tone tone = bufferProvider.get();
		//todo: true wave? idk
		int[] samples = tone.getSamples();
		int sampleCount = tone.getLen() * sampleRate / 1000;
		float xScale = getWidth() / (float)sampleCount;
		float yScale = getHeight() / 32768.f;
		int midY = getHeight() / 2;
		int prevX = 0;
		int prevY = midY;

		for (int i = 0; i < sampleCount; i++) {
			int x = (int)(i * xScale);
			int y = midY + (int)(samples[i] * yScale);
			g.drawLine(prevX, prevY, x, y);
			prevX = x; prevY = y;
		}
	}
}
