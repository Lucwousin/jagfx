package fx.jank.ui;

import fx.jank.rs.Envelope;
import javax.inject.Provider;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;

class EnvelopeSettings extends JPanel
{
	private static final String[] WAVEFORMS = {
		"Off", "Sqr", "Sin", "Saw", "Noise"
	};
	private final ButtonGroup waveSelector = new ButtonGroup();
	private final JRadioButton[] buttons;
	private final NumInput min;
	private final NumInput max;

	private final SynthPanel parent;
	// Provider, as the target will change
	private Provider<Envelope> envelope;

	private EnvelopeSettings(SynthPanel parent, Provider<Envelope> envelope, String[] labels, String unit) {
		this.parent = parent;
		this.envelope = envelope;
		this.buttons = new JRadioButton[labels.length];

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		for (int i = 0; i < labels.length; i++) {
			JRadioButton button = this.buttons[i] = new JRadioButton(labels[i]);
			button.addChangeListener(this::onButtonEvent);
			waveSelector.add(button);
			add(button);
		}

		this.buttons[envelope.get().getWaveFun()].setSelected(true);
		this.min = new NumInput(envelope.get().getMin(), "Min " + unit, this::onMinMaxEvent);
		this.max = new NumInput(envelope.get().getMax(), "Max " + unit, this::onMinMaxEvent);
		add(min);
		add(max);
	}

	static EnvelopeSettings createOscillatorSettings(SynthPanel parent, Provider<Envelope> envelope) {
		return new EnvelopeSettings(parent, envelope, WAVEFORMS, "Hz");
	}
	static EnvelopeSettings createGapSettings(SynthPanel parent, Provider<Envelope> envelope) {
		return new EnvelopeSettings(parent, envelope, new String[]{"Off", "On"}, "Gap");
	}

	private void onButtonEvent(ChangeEvent e) {
		JRadioButton button = (JRadioButton) e.getSource();
		if (!button.isSelected())
			return;

		int index = -1;
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i] == button) {
				index = i;
				break;
			}
		}
		Envelope target = envelope.get();
		if (target.getWaveFun() == index)
			return;
		target.setWaveFun(index);
		parent.update();
		parent.revalidate();
	}

	private void onMinMaxEvent(ChangeEvent e) {
		JSpinner spinner = (JSpinner) e.getSource();
		Envelope target = envelope.get();
		if (spinner == min.spinner) {
			int newValue = min.getValue();
			target.setMin(newValue);
		} else {
			int newValue = max.getValue();
			target.setMax(newValue);
			//target.setMin(Math.min(newValue, min.getValue()));
		}
		parent.update();
		parent.revalidate();
	}

	public void revalidate() {
		if (envelope == null)
			return;
		Envelope target = envelope.get();
		this.min.setValue(target.getMin());
		this.max.setValue(target.getMax());
		this.buttons[target.getWaveFun()].setSelected(true);
	}
}
