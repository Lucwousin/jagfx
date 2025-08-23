package fx.jank.rs;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SoundSystem implements Runnable {
	public static SoundSystem instance;
	public static int sampleRate = 22050;
	protected static boolean stereo;
	private static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

	volatile Channel[] players;

	public SoundSystem() {
		this.players = new Channel[2]; // L: 6
		executor.scheduleAtFixedRate(this, 0L, 10L, TimeUnit.MILLISECONDS); // L: 70
	} // L: 8

	public void run() {
		try {
			for (int var1 = 0; var1 < 2; ++var1) { // L: 12
				Channel var2 = this.players[var1]; // L: 13
				if (var2 != null) { // L: 14
					var2.run();
				}
			}
		} catch (Exception e) { // L: 17
			log.error("JagFxRl: Error running soundsystem: ", e);
		}

	} // L: 20

/*	public final synchronized void shutdown() {
		boolean var1 = true;

		for (int var2 = 0; var2 < 2; ++var2) {
			if (this == players[var2]) {
				players[var2] = null;
			}
			if (players[var2] != null) {
				var1 = false;
			}
		}
		if (var1) {
			class14.soundSystemExecutor.shutdownNow();
			class14.soundSystemExecutor = null;
			SecureRandomFuture.soundSystem = null;
		}
		this.close();
		this.samples = null;
	}*/


	public static void setRate(int sampleRate, boolean stereo) {
		if (sampleRate < 8000 || sampleRate > 48000)
			throw new IllegalArgumentException("Although, just remove the check and see what happens!");
		SoundSystem.stereo = stereo;
		SoundSystem.sampleRate = sampleRate;
	}

	public static final Channel openChannel(int index, int bufferSize) {
		if (index < 0 || index >= 2) {
			throw new IllegalArgumentException();
		}

		if (bufferSize < 256) { // L: 58
			bufferSize = 256;
		}

		try {
			Channel var3 = new DeviceChannel(); // L: 60
			var3.samples = new int[256 * (SoundSystem.stereo ? 2 : 1)]; // L: 61
			var3.bufferOffset = bufferSize; // L: 62
			var3.init(); // L: 63
			var3.capacity = (bufferSize & -1024) + 1024; // L: 64
			if (var3.capacity > 16384) { // L: 65
				var3.capacity = 16384;
			}

			var3.open(var3.capacity); // L: 66
			if (SoundSystem.sampleRate > 0 && SoundSystem.instance == null) { // L: 67
				SoundSystem.instance = new SoundSystem(); // L: 68
			}

			if (SoundSystem.instance != null) { // L: 72
				if (SoundSystem.instance.players[index] != null) { // L: 73
					throw new IllegalArgumentException();
				}

				SoundSystem.instance.players[index] = var3; // L: 74
			}

			return var3; // L: 76
		} catch (Throwable var4) { // L: 78
			return new Channel(); // L: 79
		}
	}

}