package fx.jank;

import fx.jank.ui.JagFXFrame;
import static java.lang.System.exit;
import java.util.Arrays;
import java.util.Iterator;

public class JagFx {
	private static int loadId = 370;
	public static void main(String[] args) {
		var it = Arrays.stream(args).iterator();
		while (it.hasNext())
			parseOption(it);
		JagFXFrame.create(loadId);
	}


	private static void parseOption(Iterator<String> i) {
		final String cmd = i.next();
		if (!i.hasNext()) {
			exit(1); // not very user friendly now is it
		}

		switch (cmd) {
			case "id":
				loadId = Integer.parseInt(i.next());
				break;
			default:
				throw new IllegalArgumentException(); // lol
		}
	}
}
