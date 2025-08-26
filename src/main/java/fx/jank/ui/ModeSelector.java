package fx.jank.ui;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;

public class ModeSelector extends JPanel {
	enum Mode {
		MAIN("Main"),
		FILTER("Filter"),
		BOTH("Both");
		public final String label;
		Mode(String label) {
			this.label = label;
		}
	}

	private final SynthPanel parent;
	private final JRadioButton[] buttons = new JRadioButton[3];
	private final ButtonGroup bgroup = new ButtonGroup();
	private final JPanel buttonContainer = new JPanel();

	private Mode mode = Mode.MAIN;


	ModeSelector(SynthPanel parent) {
		this.parent = parent;
		for (int i = 0; i < buttons.length; i++) {
			var button = buttons[i] = new JRadioButton(Mode.values()[i].label, i == 0);
			button.addChangeListener(this::onButton);
			bgroup.add(button);
			buttonContainer.add(button);
		}
		add(new JLabel("Mode:"));
		add(buttonContainer);
	}

	private void onButton(ChangeEvent e) {
		var button = (JRadioButton)e.getSource();

		if (!button.isSelected())
			return;

		int i = 0;
		for (; i < buttons.length; i++) {
			if (buttons[i] == button) {
				break;
			}
		}

		mode = Mode.values()[i];
		parent.revalidate();
	}

	boolean displayMain() {
		return mode == Mode.MAIN || mode == Mode.BOTH;
	}
	boolean displayFilter() {
		return mode == Mode.FILTER || mode == Mode.BOTH;
	}
}
