package fx.jank.ui;

import fx.jank.ui.util.Buttons;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import static javax.swing.BoxLayout.X_AXIS;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

//todo: copy button
public class ToneSelector extends JPanel {
	private static final String[] NUMBERS;
	static {
		NUMBERS = new String[10];
		for (int i = 0; i < 10; i++) {
			NUMBERS[i] = String.valueOf(i + 1);
		}
	}

	private final SynthPanel parent;
	private final Buttons buttons;
	private final JPanel container = new JPanel();

	ToneSelector(SynthPanel parent) {
		this.parent = parent;
		this.buttons = new Buttons(NUMBERS, 0, this::addButton, this::changeTone);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		container.setLayout(new BoxLayout(container, X_AXIS));
		add(new JLabel("Editing tone:"));
		add(container);
		var butts = buttons.getButtons();
		var minS = butts.get(9).getPreferredSize();
		for (var b : butts) {
			b.setMinimumSize(minS);
		}
	}

	private void addButton(JToggleButton button) {
		container.add(button);
		container.add(Box.createHorizontalStrut(1));
	}

	private void changeTone(int index) {
		if (parent.getSelectedToneIdx() != index)
			parent.setSelectedTone(index);
	}

	public void revalidate() {
		if (buttons == null || parent == null)
			return;
		buttons.select(parent.getSelectedToneIdx());
	}
}
