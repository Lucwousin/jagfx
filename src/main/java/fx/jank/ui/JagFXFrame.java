package fx.jank.ui;

import fx.jank.rs.Channel;
import fx.jank.rs.Mixer;
import fx.jank.rs.RawPcmStream;
import fx.jank.rs.SoundSystem;
import fx.jank.rs.Synth;
import fx.jank.rs.Tone;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JFrame;
import lombok.Getter;

public class JagFXFrame extends JFrame {
	private Channel channel;
	private Mixer mixer;

	@Getter
	private SynthPanel panel;

    public JagFXFrame() {
		setBackground(Color.DARK_GRAY);

		initSoundSystem();

    }

	public void init() {

		this.panel = new SynthPanel(mixer);
		this.panel.init();

		add(this.panel);

		pack();
	}

	private void initSoundSystem() {
		channel = SoundSystem.openChannel(0, 2048);
		channel.setSource((mixer = new Mixer()));
	}


	public void open()
	{
		setVisible(true);
		toFront();
		repaint();
	}

}
