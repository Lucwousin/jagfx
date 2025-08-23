package fx.jank.ui.components;

import fx.jank.rs.Envelope;
import java.awt.Color;
import java.awt.Graphics;
import javax.inject.Provider;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EnvelopeGraph extends Graph {
	final Provider<Envelope> targetProvider;

	@Override
	protected void paintGraph(Graphics g) {
		Envelope envelope = targetProvider.get();
		int bands = envelope.getBands();
		int[] xs = envelope.getX();
		int[] ys = envelope.getY();
		int[] screenX = new int[bands];
		int[] screenY = new int[bands];

		int w = this.getWidth();
		int h = this.getHeight();
		for (int i = 0; i < bands; i++) {
			screenX[i] = (int)(xs[i] / (65536.f * zoom) * w);
			screenY[i] = h - (int)(ys[i] / (65536.f * zoom) * h);
		}

		g.setColor(Color.YELLOW);
		g.drawPolyline(screenX, screenY, bands);

		g.setColor(Color.WHITE);
		for (int i = 0; i < bands; i++) {
			drawCross(g, screenX[i], screenY[i]);
		}
	}

	private void drawCross(Graphics g, int x, int y) {
		if (x < -5 || y < -5 || x > getWidth() + 5 || y > getHeight() + 5)
			return;
		g.drawLine(x - 2, y - 2, x + 2, y + 2);
		g.drawLine(x - 2, y + 2, x + 2, y - 2);
	}
}
