package fx.jank.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import lombok.Setter;

@Setter
public abstract class Graph extends Component {
	private boolean shouldDraw = true;
	protected int zoom = 1;
	protected int[] rasterCount = {0, 0};
	protected int[] rasterBold = {0, 0};

	public void paint(Graphics g) {
		paintBackground(g);
		paintRaster(g);
		if (shouldDraw)
			paintGraph(g);
	}

	protected void paintBackground(Graphics g) {
		g.setColor(Color.BLACK);
		Dimension dims = getSize();
		g.fillRect(0, 0, dims.width, dims.height);

		g.setColor(Color.WHITE);
		g.drawLine(0, dims.height / 2, dims.width, dims.height / 2);
	}

	private void paintRaster(Graphics g) {
		if (rasterCount[0] > 0) {
			for (int i = 0; i < rasterCount[0]; i++) {
				int x = (i + 1) * getWidth() / rasterCount[0];
				g.setColor((i + 1) % rasterBold[0] == 0 ? Color.LIGHT_GRAY : Color.DARK_GRAY);
				g.drawLine(x, 0, x, getHeight());
			}
		}
		if (rasterCount[1] > 0) {
			for (int i = 0; i < rasterCount[1]; i++) {
				int y = (i + 1) * getHeight() / rasterCount[1];
				g.setColor((i + 1) % rasterBold[1] == 0 ? Color.LIGHT_GRAY : Color.DARK_GRAY);
				g.drawLine(0, y, getWidth(), y);
			}
		}
	}

	protected abstract void paintGraph(Graphics g);
}
