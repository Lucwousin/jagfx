package fx.jank;

import fx.jank.rs.DEnvelope;
import fx.jank.rs.Envelope;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class EnvelopeTest {
	@Test public void testEnvelope() {
		// the waveform, min, and max fields are not used in the sampling method
		// why did i even make this file
		var a = new Envelope();
		var b = new DEnvelope();

		for (int i = 0; i < 100; i++) {
			assertEquals(a.sample(i), b.doStep(i));
		}
	}
}
