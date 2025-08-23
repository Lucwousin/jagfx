package fx.jank.ui;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class EnvelopePanel extends JPanel
{
	private enum Type {
		FREQUENCY,
		AMPLITUDE,
		GAP
	}

	private final JPanel comboPanel = new JPanel();
	private final GraphView[] graphs = new GraphView[3];

	private EnvelopePanel(SynthPanel parent, EnvelopeSettings settings, Type type, GraphView view) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		if (view == null) {
			view = type == Type.FREQUENCY ?
				new EnvelopeEditor(parent, "Freq Base",
					() -> parent.getSelectedTone().getFreqBase(),
					() -> true) :
				new EnvelopeEditor(parent, "Amp Base",
					() -> parent.getSelectedTone().getAmpBase(),
					() -> true);
		}
		graphs[0] = view;
		comboPanel.add(view);
		comboPanel.add(settings);
		add(comboPanel);
		switch (type) {
			case FREQUENCY:
				graphs[1] = new EnvelopeEditor(parent, "Freq Modulation rate",
					() -> parent.getSelectedTone().getFreqModRate(),
					() -> parent.getSelectedTone().getFreqModRate().getWaveFun() != 0);
				graphs[2] = new EnvelopeEditor(parent, "Freq Modulation range",
					() -> parent.getSelectedTone().getFreqModRange(),
					() -> parent.getSelectedTone().getFreqModRate().getWaveFun() != 0);
				break;
			case AMPLITUDE:
				graphs[1] = new EnvelopeEditor(parent, "Amp Modulation rate",
					() -> parent.getSelectedTone().getAmpModRate(),
					() -> parent.getSelectedTone().getAmpModRate().getWaveFun() != 0);
				graphs[2] = new EnvelopeEditor(parent, "Amp Modulation range",
					() -> parent.getSelectedTone().getAmpModRange(),
					() -> parent.getSelectedTone().getAmpModRate().getWaveFun() != 0);
				break;
			case GAP:
				graphs[1] = new EnvelopeEditor(parent, "Gap off",
					() -> parent.getSelectedTone().getGapOff(),
					() -> parent.getSelectedTone().getGapOff().getWaveFun() != 0);
				graphs[2] = new EnvelopeEditor(parent, "Gap on",
					() -> parent.getSelectedTone().getGapOn(),
					() -> parent.getSelectedTone().getGapOff().getWaveFun() != 0);
				break;
		}
		add(graphs[1]);
		add(graphs[2]);
	}

	static EnvelopePanel frqEditor(SynthPanel parent, EnvelopeSettings settings) {
		return new EnvelopePanel(parent, settings, Type.FREQUENCY, null);
	}
	static EnvelopePanel ampEditor(SynthPanel parent, EnvelopeSettings settings) {
		return new EnvelopePanel(parent, settings, Type.AMPLITUDE, null);
	}
	static EnvelopePanel gapEditor(SynthPanel parent, EnvelopeSettings settings, GraphView view) {
		return new EnvelopePanel(parent, settings, Type.GAP, view);
	}

	public void revalidate() {
		if (graphs == null)
			return;
		for (GraphView v : graphs) {
			if (v == null)
				break;
			v.revalidate();
		}
	}
}
