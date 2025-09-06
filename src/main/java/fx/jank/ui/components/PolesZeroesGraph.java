package fx.jank.ui.components;

import fx.jank.rs.Filter;
import fx.jank.ui.SynthPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

public class PolesZeroesGraph extends Graph
{
	private static final Color[] BAND_COLORS = {
		Color.YELLOW, Color.GREEN
	};
	private final SynthPanel parent;
	public PolesZeroesGraph(SynthPanel parent) {
		this.parent = parent;
		this.rasterCount = new int[]{80, 20};
		this.rasterBold = new int[]{10, 2};
	}

	protected void paintGraph(Graphics g) {
		Filter f = parent.getSelectedTone().getFilter();

		drawIndex(g, 0);
		drawIndex(g, 1);
	}

	private void drawIndex(Graphics g, int index) {
		Filter f = parent.getSelectedTone().getFilter();
		g.setColor(BAND_COLORS[index]);
		if (f.getGain()[index] != 0) {
			int gainY = getHeight() - (f.getGain()[index] * getHeight() / 65535);
			g.drawLine(0, gainY, getWidth(), gainY);
		}

		int[][][] xs = f.getReal();
		int[][][] ys = f.getImag();
		int order = f.getOrderN()[index];
		if (order <= 0) return;

		for (int i = 0; i < order; i ++) {
			Point p1 = new Point(xs[index][0][i], ys[index][0][i]);
			Point p2 = new Point(xs[index][1][i], ys[index][1][i]);
			boolean complex = !p1.equals(p2);
			if (complex) {
				this.drawline(g, p1.x, p1.y, p2.x, p2.y);
				g.setColor(BAND_COLORS[index]);
				this.drawPoint(g, p2, index);
			}
			this.drawPoint(g, p1, index);
		}
	}

	private void drawPoint(Graphics g, Point p, int index) {
		int x = p.x * getWidth() / 65535;
		int y = getHeight() - (p.y * getHeight() / 65535);
		if (index == 0)
			drawZero(g, x, y);
		else
			drawPole(g, x, y);
	}

	private void drawPole(Graphics g, int x, int y) {
		if (x < -5 || y < -5 || x > getWidth() + 5 || y > getHeight() + 5)
			return;
		g.drawLine(x - 2, y - 2, x + 2, y + 2);
		g.drawLine(x - 2, y + 2, x + 2, y - 2);
	}

	private void drawZero(Graphics g, int x, int y) {
		if (x < -5 || y < -5 || x > getWidth() + 5 || y > getHeight() + 5)
			return;
		g.drawArc(x - 3, y - 3, 5, 5, 0, 360);
	}

	private void drawline(Graphics g, int x1, int y1, int x2, int y2) {
		g.setColor(Color.RED);
		x1 *= getWidth(); x2 *= getWidth();
		x1 /= 65535; x2 /= 65535;
		y1 *= getHeight(); y2 *= getHeight();
		y1 /= 65535; y2 /= 65535;
		y1 = getHeight() - y1; y2 = getHeight() - y2;
		g.drawLine(x1, y1, x2, y2);
	}
}
