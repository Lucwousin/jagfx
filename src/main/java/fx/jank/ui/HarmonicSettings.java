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
			add(new JLabel("Harmonic " + (index + 1)));

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

	private static class ReverbSettings extends JPanel {
		private final JSpinner reverbVol = new JSpinner(new SpinnerNumberModel(100, 0, 100, 1));
		private final JSpinner reverbDel = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
		private final SynthPanel parent;

		private ReverbSettings(SynthPanel parent) {
			this.parent = parent;
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(new JLabel("Reverb"));
			JPanel container = new JPanel();
			reverbVol.addChangeListener(e -> {
				parent.getSelectedTone().setReverbVolume((int)reverbVol.getValue());
				parent.update();
			});
			container.add(new JLabel("Reverb vol:"));
			container.add(reverbVol);
			add(container);
			container = new JPanel();
			reverbDel.addChangeListener(e -> {
				parent.getSelectedTone().setReverbDelay((int)reverbDel.getValue());
				parent.update();
			});
			container.add(new JLabel("Reverb del:"));
			container.add(reverbDel);
			add(container);
		}

		public void revalidate() {
			if (parent == null)
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

	private final Container[] settings = new Container[6];
	public HarmonicSettings(SynthPanel parent) {
		//setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		for (int i = 0; i < 5; i++) {
			settings[i] = new HarmonicSetting(i, parent);
			add(settings[i]);
		}
		settings[5] = new ReverbSettings(parent);
		add(settings[5]);
	}
	public void revalidate() {
		if (settings == null)
			return;
		for (Container setting : settings) {
			if (setting == null)
				return;
			setting.revalidate();
		}
	}
}
