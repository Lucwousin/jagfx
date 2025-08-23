package fx.jank.rs;

public class BufferedTrack extends Track
{
	public int sampleRate;
	public byte[] samples;
	public int l1;
	public int l2;
	public boolean loop;

	BufferedTrack(int sampleRate, byte[] samples, int start, int end) {
		this.sampleRate = sampleRate;
		this.samples = samples;
		this.l1 = start;
		this.l2 = end;
	}

	BufferedTrack(int sampleRate, byte[] samples, int start, int end, boolean loop) {
		this.sampleRate = sampleRate; 
		this.samples = samples; 
		this.l1 = start;
		this.l2 = end;
		this.loop = loop;
	} 

	public BufferedTrack resample(Resampler r) {
		this.samples = r.resample(this.samples); 
		this.sampleRate = r.scaleRate(this.sampleRate); 
		if (this.l1 == this.l2) {
			this.l1 = this.l2 = r.scalePosition(this.l1);
		} else {
			this.l1 = r.scalePosition(this.l1);
			this.l2 = r.scalePosition(this.l2);
			if (this.l1 == this.l2) {
				--this.l1;
			}
		}

		return this; 
	}
}
