package fx.jank.ui;

import fx.jank.rs.BufferedTrack;
import fx.jank.rs.Mixer;
import fx.jank.rs.PcmStream;
import fx.jank.rs.RawPcmStream;
import fx.jank.rs.Resampler;
import static fx.jank.rs.SoundSystem.sampleRate;
import fx.jank.rs.Synth;
import fx.jank.rs.Tone;
import static fx.jank.ui.EnvelopeSettings.createGapSettings;
import static fx.jank.ui.EnvelopeSettings.createOscillatorSettings;
import fx.jank.ui.components.WaveGraph;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import lombok.Getter;

public class  SynthPanel extends JPanel {
	private static final int PAD = 8;

	@Getter
	private Synth synth;
	private int selectedTone = 0;

	private boolean initialized = false;

	private final Mixer mixer;

	// main oscillator waveform and frequency selectors
	private EnvelopeSettings freqBaseCfg;
	private EnvelopeSettings freqModCfg;
	private EnvelopeSettings ampModCfg;
	private EnvelopeSettings gapCfg;
	private LoopControls loopControls;
	private MediaControls mediaControls;
	private EnvelopePanel frqEditor;
	private EnvelopePanel ampEditor;
	private EnvelopePanel gapEditor;
	private ModeSelector modeSelector;
	private Box centerContainer;
	private Container mainPanel;
	private Container filterPanel;
	private HarmonicSettings harmonicSettings = new HarmonicSettings(this);

	@Getter
	private BufferedTrack buffer = null;
	private BufferedTrack soloBuffer = null;
	private RawPcmStream stream;
	private GraphView waveGraph;

	SynthPanel(Mixer mixer) {
		this.mixer = mixer;
		this.synth = new Synth();

		final Supplier<Tone> toneProvider = this::getSelectedTone;
		this.freqBaseCfg = createOscillatorSettings(this, () -> toneProvider.get() == null ? null : toneProvider.get().getFreqBase());
		this.freqModCfg = createOscillatorSettings(this, () -> toneProvider.get() == null ? null : toneProvider.get().getFreqModRate());
		this.ampModCfg = createOscillatorSettings(this, () -> toneProvider.get() == null ? null : toneProvider.get().getAmpModRate());
		this.gapCfg = createGapSettings(this, () -> toneProvider.get() == null ? null : toneProvider.get().getGapOff());
		this.loopControls = new LoopControls(this);
		this.mediaControls = new MediaControls(this);

		this.waveGraph = new GraphView("Final Output", new WaveGraph(this::getSelectedTone));
		this.frqEditor = EnvelopePanel.frqEditor(this, freqModCfg);
		this.ampEditor = EnvelopePanel.ampEditor(this, ampModCfg);
		this.gapEditor = EnvelopePanel.gapEditor(this, gapCfg, waveGraph);
		setLayout(new BorderLayout());
		add(leftContainer(), BorderLayout.WEST);

		this.modeSelector = new ModeSelector(this);
		this.mainPanel = buildMainPanel();

		// todo: filter needs input! (also frequency response)
		this.filterPanel = new FilterPanel(this);
		this.filterPanel.setVisible(false);

		add(buildCenter(), BorderLayout.CENTER);

		add(bottom(), BorderLayout.SOUTH);

	}

	void init() {
		initialized = true;
		this.update(); // lol
		this.revalidate();
	}

	private Container leftContainer() {
		JPanel leftContainer = new JPanel();
		leftContainer.setLayout(new BoxLayout(leftContainer, BoxLayout.Y_AXIS));
		leftContainer.add(freqBaseCfg);
		leftContainer.add(loopControls);
		leftContainer.add(mediaControls);

		leftContainer.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
		int w = 0;
		for (var c : leftContainer.getComponents()) {
			w = Math.max(w, c.getWidth());
		}
		leftContainer.setSize(w, leftContainer.getHeight());
		//todo: truewave? wtf is that
		return leftContainer;
	}

	private static GridBagConstraints chelper(int x, int y, int w) {
		return new GridBagConstraints(
			x, y, w, 1, 1.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(0, 0, 0, 0), 50, 5);
	}

	private Container buildMainPanel() {
		Box center = Box.createVerticalBox();
		center.add(frqEditor, chelper(0, 0, 3));
		center.add(ampEditor, chelper(0, 1, 3));
		center.add(gapEditor, chelper(0, 2, 3));
		return center;
	}

	private Container buildCenter() {
		centerContainer = Box.createHorizontalBox();
		centerContainer.add(mainPanel);
		centerContainer.add(filterPanel);
		centerContainer.setBorder(BorderFactory.createLineBorder(Color.white));
		return centerContainer;
	}

	private Container bottom() {
		Container bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		bottom.add(new ToneSelector(this));
		bottom.add(harmonicSettings);
		bottom.add(modeSelector);
		return bottom;
	}

	public Tone getSelectedTone() {
		if (synth == null) return null;
		if (synth.getTones()[selectedTone] == null) {
			synth.getTones()[selectedTone] = new Tone();
		}
		return synth.getTones()[selectedTone];
	}

	int getSelectedToneIdx() {
		return this.selectedTone;
	}

	void setSelectedTone(int idx) {
		this.selectedTone = idx;
		this.freqBaseCfg.revalidate();
		this.freqModCfg.revalidate();
		this.ampModCfg.revalidate();
		this.gapCfg.revalidate();
		this.harmonicSettings.revalidate();
		this.update();
	}

	void play() {
		if (stream != null && stream.isPlaying()) {
			PcmStream.stop(stream);
			stream = null;
		}
		if (soloBuffer == null) {
			soloBuffer = this.getSelectedTone().getStream();
		}
		soloBuffer.loop = mediaControls.shouldLoop();
		//soloBuffer.start = loopControls.getL1();
		//soloBuffer.end = loopControls.getL2();
		this.stream = RawPcmStream.createRawPcmStream(soloBuffer,100, 255);
		stream.setNumLoops(mediaControls.getLoopCount());
		mixer.addInput(stream);
	}

	void playAll() {
		if (stream != null && stream.isPlaying()) {
			PcmStream.stop(stream);
			stream = null;
		}
		if (buffer == null) {
			Resampler rs = new Resampler(22050, sampleRate);
			buffer = synth.getStream().resample(rs);
		}
		buffer.loop = mediaControls.shouldLoop();
		this.stream = RawPcmStream.createRawPcmStream(buffer, 100, 255);
		this.stream.setNumLoops(mediaControls.getLoopCount());
		mixer.addInput(stream);
	}

	void stop() {
		mixer.removeInput(stream);
	}

	public void setSynth(Synth synth) {
		this.synth = synth;
		setSelectedTone(0);
	}

/*	public void revalidate() {
		Tone t = getSelectedTone();
		if (t == null)
			return;
		this.buffer = this.synth.getStream();
		remove(graphPanel);
		this.graphPanel = new ToneGraphPanel(getSelectedTone());
		add(graphPanel, BorderLayout.CENTER);
		super.revalidate();
	}*/

	public void update() {
		Resampler rs = new Resampler(22050, sampleRate);
		this.buffer = this.synth.getStream().resample(rs);
		this.frqEditor.revalidate();
		this.ampEditor.revalidate();
		this.gapEditor.revalidate();
		this.loopControls.revalidate();
		this.filterPanel.revalidate();
	}

	public void revalidate() {
		if (!initialized)
			return;
		mainPanel.setVisible(modeSelector.displayMain());
		filterPanel.setVisible(modeSelector.displayFilter());
		this.filterPanel.revalidate();;
	}
}
