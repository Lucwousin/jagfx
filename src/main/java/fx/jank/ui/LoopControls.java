package fx.jank.ui;

import fx.jank.rs.Synth;
import fx.jank.rs.Tone;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;

class LoopControls extends JPanel
{
	private final SynthPanel parent;
	private final NumInput len = new NumInput(1200, "Len ms", this::onChange);
	private final NumInput pos = new NumInput(0, "Pos ms", this::onChange);
	private final NumInput l1 = new NumInput(0, "L1 ms", this::onChange);
	private final NumInput l2 = new NumInput(0, "L2 ms", this::onChange);

	LoopControls(SynthPanel parent) {
		this.parent = parent;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(len);
		add(pos);
		add(l1);
		add(l2);
	}

	private void onChange(ChangeEvent e) {
		var b = (JSpinner) e.getSource();
		Tone t = parent.getSelectedTone();
		Synth s = parent.getSynth();
		int value = (int)b.getValue();
		if (b == len.spinner) {
			if (t.getLen() != value) {
				t.setLen(value);
			}
		} else if (b == pos.spinner) {
			if (t.getPos() != value) {
				t.setPos(value);
			}
		} else if (b == l1.spinner) {
			if (s.getL1() != value) {
				s.setL1(value);
			}
		} else if (b == l2.spinner) {
			if (s.getL2() != value) {
				s.setL2(value);
			}
		}
		parent.update();
	}

	public void revalidate() {
		if (parent == null)
			return;
		Tone t = parent.getSelectedTone();
		Synth s = parent.getSynth();
		if (t == null || s == null)
			return;

		if (len.getValue() != t.getLen()) {
			len.setValue(t.getLen());
		}
		if (pos.getValue() != t.getPos()) {
			pos.setValue(t.getPos());
		}
		if (l1.getValue() != s.getL1()) {
			l1.setValue(s.getL1());
		}
		if (l2.getValue() != s.getL2()) {
			l2.setValue(s.getL2());
		}
	}
}
