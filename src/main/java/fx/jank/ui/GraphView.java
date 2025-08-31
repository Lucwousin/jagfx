package fx.jank.ui;

import fx.jank.ui.components.Graph;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GraphView extends JPanel {
	public static final Dimension GRAPH_SIZE = new Dimension(256, 300);
	protected Graph graph;

	public GraphView(String name, Graph graph) {
		this.graph = graph;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(new JLabel(name));

		this.graph.setPreferredSize(GRAPH_SIZE);
		//todo zoom etc

		add(this.graph);
	}


	public void revalidate() {
		if (graph != null)
			graph.repaint();
	}

	public void setRaster(int[] resolution, int[] bold) {
		graph.setRasterCount(resolution);
		graph.setRasterBold(bold);
	}
}
