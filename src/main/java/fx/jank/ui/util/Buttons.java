package fx.jank.ui.util;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import lombok.Getter;

public class Buttons {
	@Getter
	private final ArrayList<JToggleButton> buttons;
	private final ButtonGroup group = new ButtonGroup();
	private final IntConsumer onChange;

	public Buttons(String[] labels, int selected, Consumer<JToggleButton> configurer, IntConsumer onChange) {
		this.onChange = onChange;
		this.buttons = new ArrayList<>(labels.length);

		for (int i = 0; i < labels.length; i++) {
			var b = new JToggleButton(labels[i], i == selected);
			group.add(b);
			b.addChangeListener(this::onChange);
			configurer.accept(b);
			buttons.add(b);
		}
	}

	private void onChange(ChangeEvent e) {
		var button = (JToggleButton) e.getSource();
		int index = this.buttons.indexOf(button);
		if (button.isSelected()) {
			this.onChange.accept(index);
		}
	}

	public void select(int index) {
		this.buttons.get(index).setSelected(true);
	}
}
