package fx.jank.ui;

import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
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
		setBorder(BorderFactory.createLineBorder(Color.WHITE));
		var model = new SpinnerNumberModel(value, -65536, 65535, 1);
		this.spinner = new JSpinner(model);

		this.label = new JLabel(label);
		this.label.setLabelFor(spinner);
		this.spinner.addChangeListener(action);

		var container = new JPanel();
		container.setLayout(new GridLayout(0, 1, 2, 2));
		container.add(this.label);
		container.add(this.spinner);
		add(container);

		// and width
		var editor = (JSpinner.NumberEditor) this.spinner.getEditor();
		editor.getTextField().setColumns(3);
	}

	int getValue() {
		return (int) spinner.getValue();
	}

	void setValue(int value) {
		spinner.setValue(value);
	}
}