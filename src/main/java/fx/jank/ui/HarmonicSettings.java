package fx.jank.ui;

import fx.jank.rs.Tone;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class HarmonicSettings extends JPanel
{
	private static final int SPINNER_WIDTH = 2;

	private static abstract class ASetting extends JPanel {
		abstract void update();
	}
	private static class HarmonicSetting extends ASetting
	{
		private final int index;
		private final SynthPanel parent;
		private final JSpinner semitone;
		private final JSpinner volume;
		private final JSpinner delay;
		private HarmonicSetting(int index, SynthPanel parent) {
			this.index = index;
			this.parent = parent;
			this.semitone = new JSpinner(new SpinnerNumberModel(0.0d, 0.0d, 6553.6d, 0.1d));
			this.volume = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
			this.delay = new JSpinner(new SpinnerNumberModel(0, 0, 65536, 1));

			this.semitone.addChangeListener(e -> {
				Tone tone = parent.getSelectedTone();
				tone.getSemiInput()[index] = ((int)((double)semitone.getValue() * 10));
				parent.update();
			});
			this.volume.addChangeListener(e -> {
				Tone tone = parent.getSelectedTone();
				tone.getVolInput()[index] = (int)volume.getValue();
				parent.update();
			});
			this.delay.addChangeListener(e -> {
				Tone tone = parent.getSelectedTone();
				tone.getDelInput()[index] = (int)delay.getValue();
				parent.update();
			});
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(new JLabel("Harmonic " + (index + 1)));

			JPanel container = new JPanel(new GridLayout(0, 2));
			JLabel label;
			container.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			label = new JLabel("Semi:");
//			final Dimension labelSize = label.getMinimumSize();
			container.add(label);
			setSpinnerWidth(semitone);
			semitone.setAlignmentX(.5f);
			container.add(semitone);
			label = new JLabel("Vol:");
			//label.setMaximumSize(labelSize);
			container.add(label);
			setSpinnerWidth(volume);
			container.add(volume);
			label = new JLabel("Del:");
//label.setMaximumSize(labelSize);
container.add(label);
			setSpinnerWidth(delay);
			container.add(delay);
			add(container);
		}

		public void update() {
			if (parent == null || parent.getSelectedTone() == null)
				return;
			Tone tone = parent.getSelectedTone();
			double semitones = tone.getSemiInput()[index] / 10.0d;
			if ((double)semitone.getValue() != semitones) {
				semitone.setValue(semitones);
			}
			int vol = tone.getVolInput()[index];
			if ((int)volume.getValue() != vol) {
				volume.setValue(vol);
			}
			int del = tone.getDelInput()[index];
			if ((int)delay.getValue() != del) {
				delay.setValue(del);
			}
		}
	}

	private static class ReverbSettings extends ASetting
	{
		private final JSpinner reverbVol = new JSpinner(new SpinnerNumberModel(100, 0, 100, 1));
		private final JSpinner reverbDel = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
		private final SynthPanel parent;

		private ReverbSettings(SynthPanel parent) {
			this.parent = parent;
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(new JLabel("Reverb"));
			JPanel container = new JPanel(new GridLayout(0, 2));
			reverbVol.addChangeListener(e -> {
				parent.getSelectedTone().setReverbVolume((int)reverbVol.getValue());
				parent.update();
			});
			container.add(new JLabel("Reverb vol:"));
			setSpinnerWidth(reverbVol);
			container.add(reverbVol);
			reverbDel.addChangeListener(e -> {
				parent.getSelectedTone().setReverbDelay((int)reverbDel.getValue());
				parent.update();
			});
			container.add(new JLabel("Reverb del:"));
			setSpinnerWidth(reverbDel);
			container.add(reverbDel);
			add(container);
		}

		public void update() {
			if (parent == null || parent.getSelectedTone() == null)
				return;
			Tone t = parent.getSelectedTone();
			int vol = t.getReverbVolume();
			if ((int)reverbVol.getValue() != vol) {
				reverbVol.setValue(vol);
			}
			int del = t.getReverbDelay();
			if ((int)reverbDel.getValue() != del) {
				reverbDel.setValue(del);
			}
		}
	}

	private final ASetting[] settings = new ASetting[6];
	public HarmonicSettings(SynthPanel parent) {
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
		//add(Box.createHorizontalGlue());
		for (int i = 0; i < 5; i++) {
			settings[i] = new HarmonicSetting(i, parent);
			settings[i].setBorder(BorderFactory.createLineBorder(Color.WHITE));
			container.add(settings[i]);
			container.add(Box.createHorizontalStrut(1));
		}
		settings[5] = new ReverbSettings(parent);
		settings[5].setBorder(BorderFactory.createLineBorder(Color.WHITE));
		container.add(settings[5]);
		add(container);
		//add(Box.createHorizontalGlue());
	}
	public void update() {
		if (settings == null)
			return;
		for (ASetting setting : settings) {
			if (setting == null)
				return;
			setting.update();
		}
	}

	private static void setSpinnerWidth(JSpinner spinner) {
		var editor = ((JSpinner.NumberEditor)spinner.getEditor());
		editor.getTextField().setColumns(SPINNER_WIDTH);
	}
}
