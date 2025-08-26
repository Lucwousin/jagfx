package fx.jank.ui;

import fx.jank.rs.Envelope;
import fx.jank.rs.WaveFun;
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
					() -> parent.getSelectedTone() == null ? null : parent.getSelectedTone().getFreqModRate(),
					() -> parent.getSelectedTone() != null && envelopeActive(parent.getSelectedTone().getFreqModRate()));
				graphs[2] = new EnvelopeEditor(parent, "Freq Modulation range",
					() -> parent.getSelectedTone() == null ? null : parent.getSelectedTone().getFreqModRange(),
					() -> parent.getSelectedTone() != null && envelopeActive(parent.getSelectedTone().getFreqModRate()));
				break;
			case AMPLITUDE:
				graphs[1] = new EnvelopeEditor(parent, "Amp Modulation rate",
					() -> parent.getSelectedTone() == null ? null : parent.getSelectedTone().getAmpModRate(),
					() -> parent.getSelectedTone() != null && envelopeActive(parent.getSelectedTone().getAmpModRate()));
				graphs[2] = new EnvelopeEditor(parent, "Amp Modulation range",
					() -> parent.getSelectedTone() == null ? null : parent.getSelectedTone().getAmpModRange(),
					() -> parent.getSelectedTone() != null && envelopeActive(parent.getSelectedTone().getAmpModRate()));
				break;
			case GAP:
				graphs[1] = new EnvelopeEditor(parent, "Gap off",
					() -> parent.getSelectedTone() == null ? null : parent.getSelectedTone().getGapOff(),
					() -> parent.getSelectedTone() != null && envelopeActive(parent.getSelectedTone().getGapOn()));
				graphs[2] = new EnvelopeEditor(parent, "Gap on",
					() -> parent.getSelectedTone() == null ? null : parent.getSelectedTone().getGapOn(),
					() -> parent.getSelectedTone() != null && envelopeActive(parent.getSelectedTone().getGapOn()));
				break;
		}
		add(graphs[1]);
		add(graphs[2]);
	}

	private static boolean envelopeActive(Envelope e) {
		if (e == null)
			return false;
		return e.getWaveFun() != WaveFun.OFF.ordinal();
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
