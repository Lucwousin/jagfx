package fx.jank.ui;

import fx.jank.rs.Synth;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import lombok.extern.slf4j.Slf4j;
import net.runelite.cache.IndexType;
import net.runelite.cache.fs.Archive;
import net.runelite.cache.fs.Index;
import net.runelite.cache.fs.Store;

@Slf4j
public class SynthLoader {
	private static final Store store;
	private static final Index soundIndex;
	private static SynthLoader instance;

	static {
		try {
			store = new Store(new File("C:\\Users\\Lucas\\.runelite\\jagexcache\\oldschool-beta\\LIVE"));
			store.load();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		soundIndex = store.getIndex(IndexType.SOUNDEFFECTS);
	}

	private static byte[] getCache(int id) throws IOException {
		Archive ar = soundIndex.getArchive(id);
		return ar.decompress(store.getStorage().loadArchive(ar));
	}

	private static void init(SynthPanel parent) {
		if (instance == null)
			instance = new SynthLoader(parent);
	}

	public static void activate(SynthPanel parent) {
		init(parent);
		instance.popup.show();
		instance.sourceChoice.setVisible(true);
	}

	public static void loadCache(SynthPanel parent, int id) {
		init(parent);
		instance.loadCache(id);
	}

	private void loadCache(int id) {
		byte[] synth;
		try {
			log.info("Loading {} from cache", id);
			synth = getCache(id);
			log.info("Loaded {} bytes from cache", synth.length);
		} catch (Exception ee) {
			return;
		}
		Synth s = Synth.loadSynth(synth);
		parent.setSynth(s);
	}

	private final SynthPanel parent;
	private final JPanel popupContainer;
	private final JPanel sourceChoice;
	private final JPanel cacheSelector;
	private final JPanel fileSelector;
	private Popup popup;

	private Runnable next = null;

	public SynthLoader(SynthPanel parent) {
		this.parent = parent;
		this.popupContainer = new JPanel();
		this.sourceChoice = initSourceSelector();
		this.cacheSelector = initCacheSelector();
		this.fileSelector = new JPanel();
		this.cacheSelector.setVisible(false);
		this.fileSelector.setVisible(false);
		this.popupContainer.add(sourceChoice);
		this.popupContainer.add(cacheSelector);
		this.popupContainer.add(fileSelector);

		var pf = new PopupFactory();
		this.popup = pf.getPopup(parent, popupContainer,
			parent.getWidth() / 2 - popupContainer.getWidth() / 2,
			parent.getHeight() / 2 - popupContainer.getWidth() / 2);
	}

	private JPanel initSourceSelector() {
		var p = new JPanel();
		p.add(new JLabel("Choose synth source"));
		var button = new JButton("Cache");
		button.addActionListener(e -> {
			this.sourceChoice.setVisible(false);
			this.cacheSelector.setVisible(true);
		});
		p.add(button);
		button = new JButton("File");
		button.addActionListener(e -> {
			this.sourceChoice.setVisible(false);
			this.fileSelector.setVisible(true);
		});
		p.add(button);
		return p;
	}

	private JPanel initCacheSelector() {
		var p = new JPanel();
		p.add(new JLabel("Enter synth id"));
		var idInput = new NumInput(370, "synth id", e -> {});
		p.add(idInput);
		var submitButton = new JButton("Load");
		submitButton.addActionListener(e -> {
			this.loadCache(idInput.getValue());
			this.cacheSelector.setVisible(false);
			this.sourceChoice.setVisible(true);
			this.popup.hide();
		});
		p.add(submitButton);
		return p;
	}
}
