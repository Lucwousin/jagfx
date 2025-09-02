package fx.jank.ui.components;

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
		g.setColor(Color.PINK);
		for (int i = 25; i < getHeight() - 15; i += 25)
			for (int j = 25; j < getWidth() - 40; j += 50)
				g.drawString("TODO!", j, i); // todo!
	}
}
