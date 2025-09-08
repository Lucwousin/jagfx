package fx.jank;

import fx.jank.rs.DEnvelope;
import fx.jank.rs.DFilter;
import fx.jank.rs.DTone;
import fx.jank.rs.Tone;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ToneTest {
	private static final int TEST_SIZE = 150;
	private static final int SAMPLE_N = (int)((TEST_SIZE * 22050) / 1000.0f);

	@Test public void testWave() {
		int sample_step = 28; // idk why
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 32768; j += sample_step) {
				assertEquals(
					Tone.evaluateWave(j, 32768, i),
					DTone.evaluateWave(j, 32768, i));
			}
		}
	}

	@Test public void testTone() {
		// things to test:
		// all waveforms (x)
		// harmonic settings
		// freq/amp base (x)
		// fmod/amod (x)
		// gap
		// filter
		// (delay? loop?)

		var a = new Tone();
		var b = new DTone();
		a.getFreqBase().setWaveFun(1);
		(b.pitch = new DEnvelope()).form = 1;
		a.getAmpBase().setWaveFun(2);
		(b.volume = new DEnvelope()).form = 2;
		a.getFreqModRate().setWaveFun(3);
		(b.pitchModifier = new DEnvelope()).form = 3;
		b.pitchModifierAmplitude = new DEnvelope();
		a.getAmpModRate().setWaveFun(4);
		(b.volumeMultiplier = new DEnvelope()).form = 4;
		b.volumeMultiplierAmplitude = new DEnvelope();
		a.getGapOff().setWaveFun(1);
		(b.release = new DEnvelope()).form = 1;
		b.attack = new DEnvelope();
		Arrays.fill(a.getVolInput(), 20);
		Arrays.fill(b.oscillatorVolume, 20);
		int hDel[][] = {a.getDelInput(), b.oscillatorDelays};
		int hSemi[][] = {a.getSemiInput(), b.oscillatorPitch};
		for (int i = 0; i < 5; i++) {
			hDel[0][i] = hDel[1][i] = i;
			hSemi[0][i] = hSemi[1][i] = i;
		}
		a.setReverbDel(1);
		b.delayTime = 1;
		a.setReverbVol(50);
		b.delayDecay = 50;
		a.setLen(TEST_SIZE);
		b.duration = TEST_SIZE;
		var filters = FilterTest.createS1();
		a.setFilter(filters.a);
		b.filter = filters.b;
		b.filterEnvelope = new DEnvelope();

		int[][] res = {
			a.synthesize(SAMPLE_N, TEST_SIZE), b.synthesize(SAMPLE_N, TEST_SIZE)
		};
		// overshoot just to see if it ended at the right time
		for (int i = 0; i < SAMPLE_N + 10; i++) {
			assertEquals(res[0][i], res[1][i]);
		}
	}
}
