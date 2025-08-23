package fx.jank.ui;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;

class MediaControls extends JPanel
{
	private final SynthPanel parent;
	private JButton play;
	private JButton playAll;
	private JCheckBox loop;
	private NumInput loopCount;
	private JButton stop;
	private JButton load;
	private JButton save;


	MediaControls(SynthPanel parent) {
		this.parent = parent;
		this.play = new JButton("Play");
		this.playAll = new JButton("Play all");
		this.loop = new JCheckBox("Loop");
		this.loopCount = new NumInput(5, "Loops", this::onChange);
		this.stop = new JButton("Stop");
		this.load = new JButton("Load");
		this.save = new JButton("Save");

		this.play.addActionListener(a -> parent.play());
		this.playAll.addActionListener(a -> parent.playAll());
		this.stop.addActionListener(a -> parent.stop());
		this.load.addActionListener(a -> {});

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(play);
		add(playAll);
		add(loop);
		add(loopCount);
		add(stop);
		add(load);
		add(save);
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


}
