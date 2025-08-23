package fx.jank.ui;

import fx.jank.rs.Envelope;
import fx.jank.rs.Resampler;
import fx.jank.rs.Tone;
import fx.jank.ui.components.Graph;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.inject.Provider;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphView extends JPanel
{
	protected Graph graph;

	public GraphView(String name, Graph graph) {
		this.graph = graph;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(new JLabel(name));

		this.graph.setPreferredSize(new Dimension(400, 300));
		//todo zoom etc

		add(this.graph);
	}


	public void revalidate() {
		if (graph != null)
			graph.repaint();
	}
}
