package fx.jank.ui;

import fx.jank.ui.components.Graph;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import static javax.swing.BoxLayout.X_AXIS;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GraphView extends JPanel {
	public static final Dimension MINIMUM_SIZE = new Dimension(256, 300);
	protected Graph graph;

	public GraphView(String name, Graph graph) {
		this.graph = graph;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(new JLabel(name));

		var graphContainer = new Box(X_AXIS);
		graphContainer.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		this.graph.setMinimumSize(MINIMUM_SIZE);
		this.graph.setPreferredSize(MINIMUM_SIZE);
		graphContainer.add(this.graph);
		this.add(graphContainer);
		//todo zoom etc

	}


	public void update() {
		if (graph != null)
			graph.repaint();
	}

	public void setRaster(int[] resolution, int[] bold) {
		graph.setRasterCount(resolution);
		graph.setRasterBold(bold);
	}
}
