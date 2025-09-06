package fx.jank.ui;

import fx.jank.rs.Synth;
import java.awt.Container;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SpinnerNumberModel;
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
			store = new Store(new File(System.getProperty("user.home") + "\\.runelite\\jagexcache\\oldschool\\LIVE"));
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

	private static void init(SynthPanel parent, boolean show) {
		if (instance == null) instance = new SynthLoader(parent);
		if (show) instance.createPopup();
	}

	public static void activate(SynthPanel parent) {
		init(parent, true);
		instance.sourceChoice.setVisible(true);
		instance.popupContainer.revalidate();
	}

	public static void loadCache(SynthPanel parent, int id) {
		init(parent, false);
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
	private final Container cacheSelector;
	private final JPanel fileSelector;
	private final PopupFactory factory = new PopupFactory();
	private Popup popup;
	private boolean showingPopup = false;

	public SynthLoader(SynthPanel parent) {
		this.parent = parent;
		this.popupContainer = new JPanel();
		this.popupContainer.setLayout(new BoxLayout(popupContainer, BoxLayout.X_AXIS));
		this.sourceChoice = initSourceSelector();
		this.cacheSelector = initCacheSelector();
		this.fileSelector = new JPanel();
		this.cacheSelector.setVisible(false);
		this.fileSelector.setVisible(false);
		this.popupContainer.add(sourceChoice);
		this.popupContainer.add(cacheSelector);
		this.popupContainer.add(fileSelector);
	}

	private JPanel initSourceSelector() {
		var p = new JPanel();
		p.add(new JLabel("Choose synth source"));
		var button = new JButton("Cache");
		button.addActionListener(e -> {
			this.sourceChoice.setVisible(false);
			this.cacheSelector.setVisible(true);
			popupContainer.revalidate();
		});
		p.add(button);
		button = new JButton("File");
		button.addActionListener(e -> {
			this.sourceChoice.setVisible(false);
			this.fileSelector.setVisible(true);
			popupContainer.revalidate();
		});
		p.add(button);
		return p;
	}

	private Container initCacheSelector() {
		var container = Box.createVerticalBox();
		var grid = new JPanel(new GridLayout(0, 2));
		var idSpinner = new JSpinner(new SpinnerNumberModel(370, 0, soundIndex.getArchives().size(), 1));
		grid.add(new JLabel("Enter synth id"));
		grid.add(idSpinner);

		JButton button;

		button = new JButton("Load");
		button.addActionListener(e -> {
			this.loadCache((int)idSpinner.getValue());
			this.cacheSelector.setVisible(false);
			this.sourceChoice.setVisible(true);
			if (this.showingPopup) {
				this.popup.hide();
				this.showingPopup = false;
			}
		});
		grid.add(button);
		button = new JButton("Cancel");
		button.addActionListener(e -> {
			this.cacheSelector.setVisible(false);
			this.sourceChoice.setVisible(true);
			if (this.showingPopup) {
				this.popup.hide();
				this.showingPopup = false;
			}
		});
		grid.add(button);
		container.add(grid);
		return container;
	}

	private void createPopup() {
		if (showingPopup)
			return;
		this.popup = factory.getPopup(parent, popupContainer,
			parent.getWidth() / 2 - popupContainer.getWidth() / 2,
			parent.getHeight() / 2 - popupContainer.getWidth() / 2);
		this.popup.show();
		this.showingPopup = true;
	}
}
