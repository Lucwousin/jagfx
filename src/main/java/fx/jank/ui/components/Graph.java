package fx.jank.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import lombok.Setter;

public abstract class Graph extends Component {
	@Setter
	private boolean shouldDraw = true;
	int zoom = 1;

	public void paint(Graphics g) {
		paintBackground(g);
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

	protected abstract void paintGraph(Graphics g);
}
