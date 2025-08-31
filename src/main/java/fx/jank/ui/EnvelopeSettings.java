package fx.jank.ui;

import fx.jank.rs.Envelope;
import fx.jank.ui.util.Buttons;
import java.awt.Dimension;
import java.util.function.Supplier;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;

class EnvelopeSettings extends JPanel
{
	private static final String[] WAVEFORMS = {
		"Off", "Sqr", "Sin", "Saw", "Noise"
	};
	private final Buttons buttons;
	private final NumInput min;
	private final NumInput max;
	private final NumInput avg;

	private final SynthPanel parent;
	private final Supplier<Envelope> envelope;

	private EnvelopeSettings(SynthPanel parent, Supplier<Envelope> envelope, String[] labels, String unit) {
		this.parent = parent;
		this.envelope = envelope;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.buttons = new Buttons(labels, 0, b -> {
			b.setAlignmentX(.5f);
			this.add(b);
			this.add(Box.createVerticalStrut(1));
		}, this::onButton);

		this.min = new NumInput(envelope.get().getMin(), "Min " + unit, this::onMinMaxEvent);
		this.max = new NumInput(envelope.get().getMax(), "Max " + unit, this::onMinMaxEvent);
		this.avg = new NumInput(max.getValue() + min.getValue() / 2, "Av " + unit, (e) -> {});
		avg.spinner.setEditor(new JSpinner.DefaultEditor(avg.spinner));
		add(min);
		this.add(Box.createVerticalStrut(1));
		add(max);
		this.add(Box.createVerticalStrut(1));
		add(avg);
		add(Box.createVerticalGlue());
		updateSize();
	}

	static EnvelopeSettings createOscillatorSettings(SynthPanel parent, Supplier<Envelope> envelope) {
		return new EnvelopeSettings(parent, envelope, WAVEFORMS, "Hz");
	}
	static EnvelopeSettings createGapSettings(SynthPanel parent, Supplier<Envelope> envelope) {
		return new EnvelopeSettings(parent, envelope, new String[]{"Off", "On"}, "Gap");
	}

	private void onButton(int index) {
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
		}
		int avg = min.getValue() + max.getValue() / 2;
		this.avg.setValue(avg);
		parent.update();
		parent.revalidate();
	}

	public void revalidate() {
		if (envelope == null)
			return;
		Envelope target = envelope.get();
		this.min.setValue(target.getMin());
		this.max.setValue(target.getMax());
		this.buttons.select(target.getWaveFun());
	}

	private void updateSize() {
		int width = 0;
		int height = 0;
		var buttons = this.buttons.getButtons();

		for (var b : buttons) {
			width = Math.max(width, b.getWidth());
			height += b.getHeight();
		}
		var s = min.getMinimumSize();
		width = Math.max(width, s.width);
		height += s.height;
		s = max.getMinimumSize();
		width = Math.max(width, s.width);
		height += s.height;
		s = avg.getMinimumSize();
		width = Math.max(width, s.width);
		height += s.height;

		var bSize = new Dimension(width, buttons.get(0).getHeight());
		for (var b : buttons) {
			b.setMaximumSize(bSize);
			b.setMinimumSize(bSize);
		}
		var nSize = new Dimension(width, min.getMinimumSize().height);
		min.setMaximumSize(nSize);
		min.setMinimumSize(nSize);
		max.setMaximumSize(nSize);
		max.setMinimumSize(nSize);
		avg.setMaximumSize(nSize);
		avg.setMinimumSize(nSize);
		this.setMinimumSize(new Dimension(width, height));
	}
}
