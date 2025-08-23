package fx.jank.ui;

import fx.jank.rs.Tone;
import java.awt.Container;
import java.security.Provider;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class HarmonicSettings extends JPanel
{
	private static class HarmonicSetting extends JPanel {
		private final int index;
		private final SynthPanel parent;
		private final JSpinner semitone;
		private final JSpinner volume;
		private final JSpinner delay;
		private HarmonicSetting(int index, SynthPanel parent) {
			this.index = index;
			this.parent = parent;
			this.semitone = new JSpinner(new SpinnerNumberModel(0.0d, 0.0d, 6553.5d, 0.1d));
			this.volume = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
			this.delay = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));

			this.semitone.addChangeListener(e -> {
				Tone tone = parent.getSelectedTone();
				tone.getHarmonicSemitones()[index] = ((int)((double)semitone.getValue() * 10));
				parent.update();
			});
			this.volume.addChangeListener(e -> {
				Tone tone = parent.getSelectedTone();
				tone.getHarmonicVolumes()[index] = (int)volume.getValue();
				parent.update();
			});
			this.delay.addChangeListener(e -> {
				Tone tone = parent.getSelectedTone();
				tone.getHarmonicDelays()[index] = (int)delay.getValue();
				parent.update();
			});
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(new JLabel("Harmonic " + index));

			JPanel container = new JPanel();
			container.add(new JLabel("Semi:"));
			container.add(semitone);
			add(container);
			container = new JPanel();
			container.add(new JLabel("Vol:"));
			container.add(volume);
			add(container);
			container = new JPanel();
			container.add(new JLabel("Del:"));
			container.add(delay);
			add(container);
		}

		public void revalidate() {
			if (parent == null)
				return;
			Tone tone = parent.getSelectedTone();
			double semitones = tone.getHarmonicSemitones()[index] / 10.0d;
			if ((double)semitone.getValue() != semitones) {
				semitone.setValue(semitones);
			}
			int vol = tone.getHarmonicVolumes()[index];
			if ((int)volume.getValue() != vol) {
				volume.setValue(vol);
			}
			int del = tone.getHarmonicDelays()[index];
			if ((int)delay.getValue() != del) {
				delay.setValue(del);
			}
		}
	}


	private final HarmonicSetting[] settings = new HarmonicSetting[5];
	public HarmonicSettings(SynthPanel parent) {
		//setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		for (int i = 0; i < settings.length; i++) {
			settings[i] = new HarmonicSetting(i, parent);
			add(settings[i]);
		}
	}

	public void revalidate() {
		if (settings == null)
			return;
		for (HarmonicSetting setting : settings) {
			if (setting == null)
				return;
			setting.revalidate();
		}
	}
}
