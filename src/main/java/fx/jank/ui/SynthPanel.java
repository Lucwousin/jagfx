package fx.jank.ui;

import fx.jank.rs.BufferedTrack;
import fx.jank.rs.Mixer;
import fx.jank.rs.PcmStream;
import fx.jank.rs.RawPcmStream;
import fx.jank.rs.Resampler;
import fx.jank.rs.Synth;
import fx.jank.rs.Tone;
import static fx.jank.ui.EnvelopeSettings.createGapSettings;
import static fx.jank.ui.EnvelopeSettings.createOscillatorSettings;
import fx.jank.ui.components.WaveGraph;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import lombok.Getter;

public class  SynthPanel extends JPanel {
	private static final int PAD = 8;

	@Getter
	private Synth synth = new Synth();
	private int selectedTone = 0;

	private boolean initialized = false;

	private final Mixer mixer;

	// main oscillator waveform and frequency selectors
	private final EnvelopeSettings freqBaseCfg = createOscillatorSettings(this, Tone::getFreqBase);
	private final EnvelopeSettings freqModCfg = createOscillatorSettings(this, Tone::getFreqModRate);
	private final EnvelopeSettings ampModCfg = createOscillatorSettings(this, Tone::getAmpModRate);
	private final EnvelopeSettings gapCfg = createGapSettings(this, Tone::getGapOff);
	private final LoopControls loopControls = new LoopControls(this);
	private final MediaControls mediaControls = new MediaControls(this);
	private final GraphView waveGraph = new GraphView("Final Output", new WaveGraph(this::getSelectedTone));
	private final EnvelopePanel frqEditor = EnvelopePanel.frqEditor(this, freqModCfg);
	private final EnvelopePanel ampEditor = EnvelopePanel.ampEditor(this, ampModCfg);
	private final EnvelopePanel gapEditor = EnvelopePanel.gapEditor(this, gapCfg, waveGraph);
	private final ModeSelector modeSelector = new ModeSelector(this);
	private final Container mainPanel = buildMainPanel();
	private final FilterPanel filterPanel = new FilterPanel(this);
	private final Container centerContainer = buildCenter();
	private final ToneSelector toneSelector = new ToneSelector(this);
	private final HarmonicSettings harmonicSettings = new HarmonicSettings(this);

	@Getter
	private BufferedTrack buffer = null;
	private BufferedTrack soloBuffer = null;
	private RawPcmStream stream;

	private boolean toneChanged = false;

	SynthPanel(Mixer mixer) {
		this.mixer = mixer;
		setLayout(new BorderLayout());
		add(leftContainer(), BorderLayout.WEST);

		// todo: filter needs input! (also frequency response)
		this.filterPanel.setVisible(false);

		add(centerContainer, BorderLayout.CENTER);

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
		var centerContainer = Box.createHorizontalBox();
		centerContainer.add(mainPanel);
		centerContainer.add(filterPanel);
		centerContainer.setBorder(BorderFactory.createLineBorder(Color.white));
		return centerContainer;
	}

	private Container bottom() {
		Container bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		bottom.add(toneSelector);
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
		this.update();
		this.synthTone();
	}

	void play() {
		stop();
		soloBuffer = this.getSelectedTone().getStream();
		soloBuffer = soloBuffer.resample(Resampler.instance);
		soloBuffer.loop = mediaControls.shouldLoop();
		soloBuffer.l1 = loopControls.getL1();
		soloBuffer.l2 = loopControls.getL2();
		this.stream = RawPcmStream.createRawPcmStream(soloBuffer,100, 255);
		stream.setNumLoops(mediaControls.getLoopCount());
		mixer.addInput(stream);
	}

	void playAll() {
		stop();
		buffer = synth.getStream();
		buffer = buffer.resample(Resampler.instance);
		buffer.loop = mediaControls.shouldLoop();
		this.stream = RawPcmStream.createRawPcmStream(buffer, 100, 255);
		this.stream.setNumLoops(mediaControls.getLoopCount());
		mixer.addInput(stream);
	}

	void stop() {
		if (stream != null && stream.isPlaying()) {
			mixer.removeInput(stream);
			PcmStream.stop(stream);
			stream = null;
		}
	}

	public void setSynth(Synth synth) {
		stop();
		this.synth = synth;
		this.toneChanged = true;
		setSelectedTone(0);
	}

	public void update() {
		this.harmonicSettings.update();
		this.freqBaseCfg.update();
		this.frqEditor.update();
		this.ampEditor.update();
		this.gapEditor.update();
		this.loopControls.update();
		this.filterPanel.update();
		this.toneChanged = true; // lol
		synthTone();

	}

	public void revalidate() {
		if (!initialized)
			return;
		mainPanel.setVisible(modeSelector.displayMain());
		filterPanel.setVisible(modeSelector.displayFilter());
		this.filterPanel.revalidate();
	}

	private void synthTone() {
		if (!toneChanged) {
			return;
		}
		toneChanged = false;
		getSelectedTone().synthAll();
		waveGraph.repaint();
	}
}
