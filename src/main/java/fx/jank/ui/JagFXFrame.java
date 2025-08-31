package fx.jank.ui;

import fx.jank.rs.Channel;
import fx.jank.rs.Mixer;
import fx.jank.rs.SoundSystem;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.UIManager;
import lombok.Getter;

public class JagFXFrame extends JFrame {

	public static void create() {
		UIManager.put("Panel.background", Color.DARK_GRAY);
		UIManager.put("Button.foreground", Color.WHITE);
		UIManager.put("ToggleButton.background", Color.GRAY);
		UIManager.put("ToggleButton.foreground", Color.WHITE);
		UIManager.put("ToggleButton.select", Color.GREEN);
		UIManager.put("Label.foreground", Color.WHITE);
		UIManager.put("Label.border", BorderFactory.createEmptyBorder(3, 2, 2, 2));
		UIManager.put("ToggleButton.border", BorderFactory.createEmptyBorder(5, 5, 5, 5));
		UIManager.put("FormattedTextField.background", Color.BLACK);
		UIManager.put("FormattedTextField.foreground", Color.WHITE);
		UIManager.put("FormattedTextField.border", Color.WHITE);

		var frame = new JagFXFrame();
		frame.init();
		SynthLoader.loadCache(frame.panel, 4211);
		frame.open();
	}


	private Channel channel;
	private Mixer mixer;

	@Getter
	private SynthPanel panel;

    private JagFXFrame() {
		setSize(1920, 1080);

		initSoundSystem();

    }

	public void init() {

		this.panel = new SynthPanel(mixer);
		this.panel.init();

		//MetalLookAndFeel.setCurrentTheme(new HackermanTheme());
		add(this.panel);

		pack();

		setDefaultCloseOperation(EXIT_ON_CLOSE);
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
