package fx.jank;

import fx.jank.rs.BufferedTrack;
import fx.jank.rs.Channel;
import fx.jank.rs.Mixer;
import fx.jank.rs.RawPcmStream;
import fx.jank.rs.Resampler;
import fx.jank.rs.SoundSystem;
import static fx.jank.rs.SoundSystem.sampleRate;
import fx.jank.rs.Synth;
import fx.jank.ui.JagFXFrame;
import java.io.File;
import net.runelite.cache.IndexType;
import net.runelite.cache.fs.Archive;
import net.runelite.cache.fs.Index;
import net.runelite.cache.fs.Store;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class JagFXTest
{
	public static void main(String[] args) throws Exception
	{
		Store store = new Store(new File("C:\\Users\\Lucas\\.runelite\\jagexcache\\oldschool-beta\\LIVE"));
		store.load();
		var s = store.getStorage();
		Index index = store.getIndex(IndexType.SOUNDEFFECTS);
		Archive ar = index.getArchive(370);
		byte[] cowData = ar.decompress(s.loadArchive(ar));


		JagFXFrame frame = new JagFXFrame();
		frame.init();
		frame.getPanel().setSynth(Synth.loadSynth(cowData));
/*		Channel ch = SoundSystem.openChannel(0, 2048);
		Mixer m = new Mixer();
		ch.setInput(m);
		//Thread.sleep(5000);
		Resampler rs = new Resampler(22050, sampleRate);
		Synth syn = Synth.loadSynth(cowData);
		syn.getTones()[0].setReverbDelay(20);
		syn.getTones()[0].setReverbVolume(50);
		BufferedTrack t = syn.getStream().resample(rs);
		t.loop = true;
		RawPcmStream str = RawPcmStream.createRawPcmStream(t, 100, 255);
		str.setNumLoops(0);
		m.addInput(str);
		Thread.sleep(1000);*/



		frame.open();

//		SoundSystem.setRate(44100, true);



	}
}