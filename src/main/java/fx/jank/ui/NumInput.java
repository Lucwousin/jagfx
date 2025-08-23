package fx.jank.ui;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

class NumInput extends JPanel
{
	private final JLabel label;
	final JSpinner spinner;

	NumInput(int value, String label, ChangeListener action) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		var model = new SpinnerNumberModel(value, -65536, 65535, 1);
		this.spinner = new JSpinner(model);
		this.label = new JLabel(label);
		this.label.setLabelFor(spinner);
		this.spinner.addChangeListener(action);
		add(this.label);
		add(this.spinner);
	}

	int getValue() {
		return (int) spinner.getValue();
	}

	void setValue(int value) {
		spinner.setValue(value);
	}
}