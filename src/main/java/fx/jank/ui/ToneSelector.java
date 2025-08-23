package fx.jank.ui;

import java.awt.Button;
import javax.swing.BoxLayout;
import static javax.swing.BoxLayout.X_AXIS;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;

//todo: copy button
public class ToneSelector extends JPanel {
	private final SynthPanel parent;
	private final ButtonGroup bg = new ButtonGroup();
	private final JRadioButton[] buttons = new JRadioButton[10];
	private final JPanel container = new JPanel();

	ToneSelector(SynthPanel parent) {
		this.parent = parent;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		container.setLayout(new BoxLayout(container, X_AXIS));
		for (int i = 0; i < 10; i++) {
			buttons[i] = new JRadioButton(Integer.toString(i));
			buttons[i].addChangeListener(this::changeTone);
			bg.add(buttons[i]);
			container.add(buttons[i]);
		}
		buttons[0].setSelected(true);
		add(new JLabel("Editing tone:"));
		add(container);
	}

	private void changeTone(ChangeEvent event) {
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i].isSelected()) {
				parent.setSelectedTone(i);
				return;
			}
		}
	}

	public void revalidate() {
		if (buttons == null || parent == null)
			return;
		if (!buttons[parent.getSelectedToneIdx()].isSelected()) {
			buttons[parent.getSelectedToneIdx()].setSelected(true);
		}
	}
}
