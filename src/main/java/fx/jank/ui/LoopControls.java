package fx.jank.ui;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
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

	int getLen() {
		return len.getValue();
	}
	int getPos() {
		return pos.getValue();
	}
	int getL1() {
		return l1.getValue();
	}
	int getL2() {
		return l2.getValue();
	}

	private void onChange(ChangeEvent ignore) {
		// nop
	}


}
