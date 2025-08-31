package fx.jank.ui;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;

class MediaControls extends JPanel
{
	private final SynthPanel parent;
	private final JButton play = new JButton("Play");
	private final JButton playAll = new JButton("Play All");
	private final JButton loop = new JButton("Loop");
	private final JButton loopAll = new JButton("Loop All");
	private final NumInput loopCount = new NumInput(5, "Loops", this::onChange);
	private final JButton stop = new JButton("Stop");
	private final JButton load = new JButton("Load");
	private final JButton save = new JButton("Save");
	private final JComponent[] children = {
		play, playAll, loop, loopAll, loopCount, stop, load, save
	};

	MediaControls(SynthPanel parent) {
		this.parent = parent;

		this.play.addActionListener(a -> parent.play());
		this.playAll.addActionListener(a -> parent.playAll());
		this.stop.addActionListener(a -> parent.stop());
		this.load.addActionListener(a -> SynthLoader.activate(parent));

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(play);
		add(playAll);
		add(loop);
		add(loopAll);
		add(loopCount);
		add(stop);
		add(load);
		add(save);
		add(Box.createVerticalGlue());

		updateSize();
	}

	boolean shouldLoop() {
		return loop.isSelected();
	}
	int getLoopCount() {
		return loopCount.getValue();
	}

	private void onChange(ChangeEvent ignore) {
		// nop
	}

	private void updateSize() {
		int width = 0;
		int height = 0;

		for (var c : children) {
			var s = c.getMinimumSize();
			width = Math.max(width, s.width);
			height += s.height;
		}
		for (var c : children) {
			var s = c.getMinimumSize();
			var newSize = new Dimension(width, s.height);
			c.setMaximumSize(newSize);
			c.setMinimumSize(newSize);
			c.setAlignmentX(.5f);
		}
		this.setMinimumSize(new Dimension(width, height));
	}
}
