package fx.jank.ui;

import fx.jank.ui.components.FrequencyResponseGraph;
import fx.jank.ui.components.PolesZeroesGraph;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class FilterPanel extends JPanel {
	private final SynthPanel parent;

	private GraphView pzGraph;
	private EnvelopeEditor transitionCurve;
	private GraphView freqResponse;

	public FilterPanel(SynthPanel parent) {
		this.parent = parent;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.pzGraph = new GraphView("Graph of poles and zeroes", new PolesZeroesGraph(parent));
		this.transitionCurve = new EnvelopeEditor(parent, "Transition curve",
			() -> parent.getSelectedTone().getTransitionCurve(),
			() -> true); // todo en/disable on on/off
		this.transitionCurve.setRaster(new int[]{0, 50}, new int[]{0, 5});
		this.freqResponse  = new GraphView("Frequency response", new FrequencyResponseGraph(parent));
		add(pzGraph);
		add(transitionCurve);
		add(freqResponse);
	}

	public void update() {
		if (parent == null)
			return;
		pzGraph.update();
		transitionCurve.update();
		freqResponse.update();
	}
}
