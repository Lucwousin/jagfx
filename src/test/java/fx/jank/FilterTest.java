package fx.jank;

import fx.jank.rs.DFilter;
import fx.jank.rs.Filter;
import org.antlr.v4.runtime.misc.Pair;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class FilterTest {
	public static Pair<Filter, DFilter> createSubjects(int[] order, int[] gain, int[][][] real, int[][][] imag) {
		var f = new Filter();
		var d = new DFilter();
		f.setOrderN(order);
		d.setPairs(order);
		f.setReal(real);
		d.setField406(real);
		f.setImag(imag);
		d.setField407(imag);
		f.setGain(gain);
		d.setField408(gain);
		return new Pair<>(f, d);
	}

	public static Pair<Filter, DFilter> createS1() {
		return createSubjects(new int[]{1, 2}, new int[]{39321, 0},
			new int[][][]{ // [pole/zero][complex/real][index]
				{{19808, 0, 0, 0}, {10522, 0, 0 ,0}},
				{{65535, 41839, 0, 0}, {34619, 41839, 0, 0}}},
			new int[][][]{
				{{10250, 0, 0, 0}, {16972, 0, 0, 0}},
				{{11762, 7897, 0, 0}, {13947, 7897, 0, 0}}});
	}

	@Test public void testCompute() {
		var subj = createS1();
		int a[] = {subj.a.compute(0, 1.f), subj.a.compute(1, 1.f)};
		int b[] = {subj.b.compute(0, 1.f), subj.b.compute(1, 1.f)};
		assertEquals(a.length, b.length);
		for (int i = 0; i < a.length; i++) {
			assertEquals(a[i], b[i]);
			for (int j = 0; j < a[i]; j++) {
				assertEquals(subj.a.getCoefficients()[i][j], subj.b.coefficients[i][j]);
			}
		}
	}

}
