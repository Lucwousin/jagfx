package fx.jank.rs;

import static fx.jank.rs.SoundSystem.stereo;

public class RawPcmStream extends PcmStream {
	public static final int CHUNK_SIZE = 256;

	int pos;
	int speed;
	int volume;
	int field1483;
	int field1484;
	int field1489;
	int field1486;
	int numLoops;
	int loopStart;
	int loopEnd;
	boolean bounce;
	int available;
	int field1485;
	int field1492;
	int field1494;

	RawPcmStream(BufferedTrack source, int var2, int var3, int var4) {
		super.buffer = source;
		this.loopStart = source.l1;
		this.loopEnd = source.l2;
		this.bounce = source.loop;
		this.speed = var2;
		this.volume = var3;
		this.field1483 = var4;
		this.pos = 0;
		this.calculateMagnitudes();
	}

	RawPcmStream(BufferedTrack var1, int speed, int volume) {
		super.buffer = var1;
		this.loopStart = var1.l1;
		this.loopEnd = var1.l2;
		this.bounce = var1.loop;
		this.speed = speed;
		this.volume = volume;
		this.field1483 = 8192;
		this.pos = 0;
		this.calculateMagnitudes();
	}

	void calculateMagnitudes() {
		this.field1484 = this.volume;
		this.field1489 = getPosMag(this.volume, this.field1483);
		this.field1486 = getNegMag(this.volume, this.field1483);
	}

	protected PcmStream firstSubStream() {
		return null;
	}

	protected PcmStream nextSubStream() {
		return null;
	}

	protected int status() {
		return this.volume == 0 && this.available == 0 ? 0 : 1;
	}

	public synchronized void fill(int[] samples, int offset, int length) {
		if (status() == 0) {
			this.skip(length);
			return;
		}

		BufferedTrack var4 = (BufferedTrack) super.buffer;
		int start = this.loopStart * CHUNK_SIZE;
		int end = this.loopEnd * CHUNK_SIZE;
		int bufLen = var4.samples.length * CHUNK_SIZE;
		int len = end - start;
		if (len <= 0) {
			this.numLoops = 0;
		}

		int posChunk = offset;
		int endChunk = offset + length;
		if (this.pos < 0) {
			if (this.speed <= 0) {
				this.stop();
				this.remove();
				return;
			}

			this.pos = 0;
		}

		if (this.pos >= bufLen) {
			if (this.speed >= 0) {
				this.stop();
				this.remove();
				return;
			}

			this.pos = bufLen - 1;
		}

		if (this.numLoops < 0) { // loop infinitely..
			if (this.bounce) {
				infiniteBounce(samples, offset, posChunk, start, endChunk, var4, end);
			} else {
				infiniteLoop(samples, posChunk, start, endChunk, var4, end, len);
			}
			return;
		}
		if (this.numLoops > 0) {
			if (this.bounce) {
				posChunk = finiteBounce(samples, offset, posChunk, start, endChunk, var4, end);
			} else {
				posChunk = finiteLoop(samples, posChunk, start, endChunk, var4, end, len);
			}
			if (posChunk == -1)
				return;
		}

		if (this.speed < 0) {
			this.fillReverse(samples, posChunk, 0, endChunk, 0);
			if (this.pos < 0) {
				this.pos = -1;
				this.stop();
				this.remove();
			}
		} else {
			this.fillForward(samples, posChunk, bufLen, endChunk, 0);
			if (this.pos >= bufLen) {
				this.pos = bufLen;
				this.stop();
				this.remove();
			}
		}

	}

	private void infiniteLoop(int[] samples, int posChunk, int start, int endChunk, BufferedTrack var4, int end, int len) {
		if (this.speed < 0) {
			while (true) {
				posChunk = this.fillReverse(samples, posChunk, start, endChunk, var4.samples[this.loopEnd - 1]);
				if (this.pos >= start) {
					return;
				}

				this.pos = end - 1 - (end - 1 - this.pos) % len;
			}
		} else {
			while (true) {
				posChunk = this.fillForward(samples, posChunk, end, endChunk, var4.samples[this.loopStart]);
				if (this.pos < end) {
					return;
				}

				this.pos = start + (this.pos - start) % len;
			}
		}
	}

	private void infiniteBounce(int[] samples, int offset, int posChunk, int start, int endChunk, BufferedTrack var4, int end) {
		while (true) {
			if (this.speed < 0) {
				posChunk = this.fillReverse(samples, offset, start, endChunk, var4.samples[this.loopStart]);
				if (this.pos >= start) {
					return;
				}

				this.pos = start + start - 1 - this.pos;
			} else {
				posChunk = this.fillForward(samples, posChunk, end, endChunk, var4.samples[this.loopEnd - 1]);
				if (this.pos < end) {
					return;
				}

				this.pos = end + end - 1 - this.pos;
			}
			this.speed = -this.speed;
		}
	}

	private int finiteLoop(int[] samples, int posChunk, int start, int endChunk, BufferedTrack var4, int end, int len) {
		int var10;
		if (this.speed < 0) {
			while (true) {
				posChunk = this.fillReverse(samples, posChunk, start, endChunk, var4.samples[this.loopEnd - 1]);
				if (this.pos >= start) {
					return -1;
				}

				var10 = (end - 1 - this.pos) / len;
				if (var10 >= this.numLoops) {
					this.pos += len * this.numLoops;
					this.numLoops = 0;
					break;
				}

				this.pos += len * var10;
				this.numLoops -= var10;
			}
		} else {
			while (true) {
				posChunk = this.fillForward(samples, posChunk, end, endChunk, var4.samples[this.loopStart]);
				if (this.pos < end) {
					return -1;
				}

				var10 = (this.pos - start) / len;
				if (var10 >= this.numLoops) {
					this.pos -= len * this.numLoops;
					this.numLoops = 0;
					break;
				}

				this.pos -= len * var10;
				this.numLoops -= var10;
			}
		}
		return posChunk;
	}

	private int finiteBounce(int[] samples, int offset, int posChunk, int start, int endChunk, BufferedTrack var4, int end) {
		do {
			if (this.speed < 0) {
				posChunk = this.fillReverse(samples, offset, start, endChunk, var4.samples[this.loopStart]);
				if (this.pos >= start) {
					return -1;
				}

				this.pos = start + start - 1 - this.pos;
			} else {
				posChunk = this.fillForward(samples, posChunk, end, endChunk, var4.samples[this.loopEnd - 1]);
				if (this.pos < end) {
					return -1;
				}

				this.pos = end + end - 1 - this.pos;
			}
			this.speed = -this.speed;
		} while (--this.numLoops != 0);
		return posChunk;
	}

	public synchronized void setNumLoops(int var1) {
		this.numLoops = var1;
	}

	public synchronized void skip(int length) {
		if (this.available > 0) {
			if (length >= this.available) {
				if (this.volume == Integer.MIN_VALUE) {
					this.volume = 0;
					this.field1486 = 0;
					this.field1489 = 0;
					this.field1484 = 0;
					this.remove();
					length = this.available;
				}

				this.available = 0;
				this.calculateMagnitudes();
			} else {
				this.field1484 += this.field1485 * length;
				this.field1489 += this.field1492 * length;
				this.field1486 += this.field1494 * length;
				this.available -= length;
			}
		}

		BufferedTrack var2 = (BufferedTrack)super.buffer;
		int var3 = this.loopStart << 8;
		int var4 = this.loopEnd << 8;
		int var5 = var2.samples.length << 8;
		int var6 = var4 - var3;
		if (var6 <= 0) {
			this.numLoops = 0;
		}

		if (this.pos < 0) {
			if (this.speed <= 0) {
				this.stop();
				this.remove();
				return;
			}

			this.pos = 0;
		}

		if (this.pos >= var5) {
			if (this.speed >= 0) {
				this.stop();
				this.remove();
				return;
			}

			this.pos = var5 - 1;
		}

		this.pos += this.speed * length;
		if (this.numLoops < 0) {
			if (!this.bounce) {
				if (this.speed >= 0) {
					if (this.pos >= var4) {
						this.pos = var3 + (this.pos - var3) % var6;
					}
				} else {
					if (this.pos < var3) {
						this.pos = var4 - 1 - (var4 - 1 - this.pos) % var6;
					}
				}
				return;
			}
			if (this.speed < 0) {
				if (this.pos >= var3) {
					return;
				}

				this.pos = var3 + var3 - 1 - this.pos;
				this.speed = -this.speed;
			}

			while (this.pos >= var4) {
				this.pos = var4 + var4 - 1 - this.pos;
				this.speed = -this.speed;
				if (this.pos >= var3) {
					return;
				}

				this.pos = var3 + var3 - 1 - this.pos;
				this.speed = -this.speed;
			}
			return;
		}
		if (this.numLoops > 0) {
			if (this.bounce) {
				label123: {
					if (this.speed < 0) {
						if (this.pos >= var3) {
							return;
						}

						this.pos = var3 + var3 - 1 - this.pos;
						this.speed = -this.speed;
						if (--this.numLoops == 0) {
							break label123;
						}
					}

					do {
						if (this.pos < var4) {
							return;
						}

						this.pos = var4 + var4 - 1 - this.pos;
						this.speed = -this.speed;
						if (--this.numLoops == 0) {
							break;
						}

						if (this.pos >= var3) {
							return;
						}

						this.pos = var3 + var3 - 1 - this.pos;
						this.speed = -this.speed;
					} while(--this.numLoops != 0);
				}
			} else {
				int var7;
				if (this.speed < 0) {
					if (this.pos >= var3) {
						return;
					}

					var7 = (var4 - 1 - this.pos) / var6;
					if (var7 < this.numLoops) {
						this.pos += var6 * var7;
						this.numLoops -= var7;
						return;
					}

					this.pos += var6 * this.numLoops;
					this.numLoops = 0;
				} else {
					if (this.pos < var4) {
						return;
					}

					var7 = (this.pos - var3) / var6;
					if (var7 < this.numLoops) {
						this.pos -= var6 * var7;
						this.numLoops -= var7;
						return;
					}

					this.pos -= var6 * this.numLoops;
					this.numLoops = 0;
				}
			}
		}

		if (this.speed < 0) {
			if (this.pos < 0) {
				this.pos = -1;
				this.stop();
				this.remove();
			}
		} else if (this.pos >= var5) {
			this.pos = var5;
			this.stop();
			this.remove();
		}

	}

	public synchronized void method2659(int var1) {
		this.method2661(var1 << 6, this.getSign());
	}

	synchronized void method2682(int var1) {
		this.method2661(var1, this.getSign());
	}

	synchronized void method2661(int var1, int var2) {
		this.volume = var1;
		this.field1483 = var2;
		this.available = 0;
		this.calculateMagnitudes();
	}

	public synchronized int method2662() {
		return this.volume == Integer.MIN_VALUE ? 0 : this.volume;
	}

	public synchronized int getSign() {
		return this.field1483 < 0 ? -1 : this.field1483;
	}

	public synchronized void seek(int pos) {
		int var2 = ((BufferedTrack)super.buffer).samples.length << 8;
		if (pos < -1) {
			pos = -1;
		}

		if (pos > var2) {
			pos = var2;
		}

		this.pos = pos;
	}

	public synchronized void method2655() {
		this.speed = (this.speed ^ this.speed >> 31) + (this.speed >>> 31);
		this.speed = -this.speed;
	}

	void stop() {
		if (this.available == 0) {
			return;
		}
		if (this.volume == Integer.MIN_VALUE) {
			this.volume = 0;
		}

		this.available = 0;
		this.calculateMagnitudes();

	}

	public synchronized void method2666(int var1, int var2) {
		this.method2704(var1, var2, this.getSign());
	}

	public synchronized void method2704(int var1, int var2, int var3) {
		if (var1 == 0) {
			this.method2661(var2, var3);
			return;
		}
		int var4 = getPosMag(var2, var3);
		int var5 = getNegMag(var2, var3);
		if (var4 == this.field1489 && var5 == this.field1486) {
			this.available = 0;
			return;
		}
		int var6 = var2 - this.field1484;
		if (this.field1484 - var2 > var6) {
			var6 = this.field1484 - var2;
		}

		if (var4 - this.field1489 > var6) {
			var6 = var4 - this.field1489;
		}

		if (this.field1489 - var4 > var6) {
			var6 = this.field1489 - var4;
		}

		if (var5 - this.field1486 > var6) {
			var6 = var5 - this.field1486;
		}

		if (this.field1486 - var5 > var6) {
			var6 = this.field1486 - var5;
		}

		if (var1 > var6) {
			var1 = var6;
		}

		this.available = var1;
		this.volume = var2;
		this.field1483 = var3;
		this.field1485 = (var2 - this.field1484) / var1;
		this.field1492 = (var4 - this.field1489) / var1;
		this.field1494 = (var5 - this.field1486) / var1;
	}

	public synchronized void method2706(int var1) {
		if (var1 == 0) {
			this.method2682(0);
			this.remove();
		} else if (this.field1489 == 0 && this.field1486 == 0) {
			this.available = 0;
			this.volume = 0;
			this.field1484 = 0;
			this.remove();
		} else {
			int var2 = -this.field1484;
			if (this.field1484 > var2) {
				var2 = this.field1484;
			}

			if (-this.field1489 > var2) {
				var2 = -this.field1489;
			}

			if (this.field1489 > var2) {
				var2 = this.field1489;
			}

			if (-this.field1486 > var2) {
				var2 = -this.field1486;
			}

			if (this.field1486 > var2) {
				var2 = this.field1486;
			}

			if (var1 > var2) {
				var1 = var2;
			}

			this.available = var1;
			this.volume = Integer.MIN_VALUE;
			this.field1485 = -this.field1484 / var1;
			this.field1492 = -this.field1489 / var1;
			this.field1494 = -this.field1486 / var1;
		}
	}

	public synchronized void setSpeed(int var1) {
		if (this.speed < 0) {
			this.speed = -var1;
		} else {
			this.speed = var1;
		}

	}

	public synchronized int getSpeed() {
		return this.speed < 0 ? -this.speed : this.speed;
	}

	public boolean passedEnd() {
		return this.pos < 0 || this.pos >= ((BufferedTrack)super.buffer).samples.length << 8;
	}

	public boolean method2672() {
		return this.available != 0;
	}

	int fillForward(int[] var1, int var2, int var3, int var4, int var5) {
		while (this.available > 0) {
			int var6 = var2 + this.available;
			if (var6 > var4) {
				var6 = var4;
			}

			this.available += var2;
			if (this.speed == 256 && (this.pos & 255) == 0) {
				if (stereo) {
					var2 = method2753(0, ((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1489, this.field1486, this.field1492, this.field1494, 0, var6, var3, this);
				} else {
					var2 = method2710(((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1484, this.field1485, 0, var6, var3, this);
				}
			} else {
				if (stereo) {
					var2 = method2694(0, 0, ((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1489, this.field1486, this.field1492, this.field1494, 0, var6, var3, this, this.speed, var5);
				} else {
					var2 = method2693(0, 0, ((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1484, this.field1485, 0, var6, var3, this, this.speed, var5);
				}
			}

			this.available -= var2;
			if (this.available != 0) {
				return var2;
			}

			if (this.method2786()) {
				return var4;
			}
		}

		if (this.speed == 256 && (this.pos & 255) == 0) {
			if (stereo) {
				return method2718(0, ((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1489, this.field1486, 0, var4, var3, this);
			} else {
				return method2690(((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1484, 0, var4, var3, this);
			}
		} else {
			if (stereo) {
				return method2686(0, 0, ((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1489, this.field1486, 0, var4, var3, this, this.speed, var5);
			} else {
				return method2691(0, 0, ((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1484, 0, var4, var3, this, this.speed, var5);
			}
		}
	}

	int fillReverse(int[] var1, int var2, int var3, int var4, int var5) {
		while (this.available > 0) {
			int var6 = var2 + this.available;
			if (var6 > var4) {
				var6 = var4;
			}

			this.available += var2;
			if (this.speed == -256 && (this.pos & 255) == 0) {
				if (stereo) {
					var2 = method2719(0, ((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1489, this.field1486, this.field1492, this.field1494, 0, var6, var3, this);
				} else {
					var2 = method2683(((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1484, this.field1485, 0, var6, var3, this);
				}
			} else {
				if (stereo) {
					var2 = method2781(0, 0, ((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1489, this.field1486, this.field1492, this.field1494, 0, var6, var3, this, this.speed, var5);
				} else {
					var2 = method2738(0, 0, ((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1484, this.field1485, 0, var6, var3, this, this.speed, var5);
				}
			}

			this.available -= var2;
			if (this.available != 0) {
				return var2;
			}

			if (this.method2786()) {
				return var4;
			}
		}

		if (this.speed == -256 && (this.pos & 255) == 0) {
			if (stereo) {
				return method2748(0, ((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1489, this.field1486, 0, var4, var3, this);
			} else {
				return method2681(((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1484, 0, var4, var3, this);
			}
		} else {
			if (stereo) {
				return method2688(0, 0, ((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1489, this.field1486, 0, var4, var3, this, this.speed, var5);
			} else {
				return method2687(0, 0, ((BufferedTrack) super.buffer).samples, var1, this.pos, var2, this.field1484, 0, var4, var3, this, this.speed, var5);
			}
		}
	}

	boolean method2786() {
		int var1 = this.volume;
		int var2;
		int var3;
		if (var1 == Integer.MIN_VALUE) {
			var3 = 0;
			var2 = 0;
			var1 = 0;
		} else {
			var2 = getPosMag(var1, this.field1483);
			var3 = getNegMag(var1, this.field1483);
		}

		if (var1 == this.field1484 && var2 == this.field1489 && var3 == this.field1486) {
			if (this.volume == Integer.MIN_VALUE) {
				this.volume = 0;
				this.field1486 = 0;
				this.field1489 = 0;
				this.field1484 = 0;
				this.remove();
				return true;
			} else {
				this.calculateMagnitudes();
				return false;
			}
		}
		if (this.field1484 < var1) {
			this.field1485 = 1;
			this.available = var1 - this.field1484;
		} else if (this.field1484 > var1) {
			this.field1485 = -1;
			this.available = this.field1484 - var1;
		} else {
			this.field1485 = 0;
		}

		if (this.field1489 < var2) {
			this.field1492 = 1;
			if (this.available == 0 || this.available > var2 - this.field1489) {
				this.available = var2 - this.field1489;
			}
		} else if (this.field1489 > var2) {
			this.field1492 = -1;
			if (this.available == 0 || this.available > this.field1489 - var2) {
				this.available = this.field1489 - var2;
			}
		} else {
			this.field1492 = 0;
		}

		if (this.field1486 < var3) {
			this.field1494 = 1;
			if (this.available == 0 || this.available > var3 - this.field1486) {
				this.available = var3 - this.field1486;
			}
		} else if (this.field1486 > var3) {
			this.field1494 = -1;
			if (this.available == 0 || this.available > this.field1486 - var3) {
				this.available = this.field1486 - var3;
			}
		} else {
			this.field1494 = 0;
		}

		return false;
	}

	int available() {
		int var1 = this.field1484 * 3 >> 6;
		var1 = (var1 ^ var1 >> 31) + (var1 >>> 31);
		if (this.numLoops == 0) {
			var1 -= var1 * this.pos / (((BufferedTrack)super.buffer).samples.length << 8);
		} else if (this.numLoops >= 0) {
			var1 -= var1 * this.loopStart / ((BufferedTrack)super.buffer).samples.length;
		}

		return var1 > 255 ? 255 : var1;
	}

	static int getPosMag(int var0, int var1) {
		return var1 < 0 ? var0 : (int)((double)var0 * Math.sqrt((double)(16384 - var1) * 0x1.0p-13) + 0.5D);
	}

	static int getNegMag(int var0, int var1) {
		return var1 < 0 ? -var0 : (int)((double)var0 * Math.sqrt((double)var1 * 0x1.0p-13) + 0.5D);
	}

	public static RawPcmStream createRawPcmStream(BufferedTrack src, int speed, int volume) {
		return src.samples != null && src.samples.length != 0 ? new RawPcmStream(src, (int)((long)src.sampleRate * 256L * (long)speed / (SoundSystem.sampleRateOut * 100L)), volume << 6) : null;
	}

	public static RawPcmStream createRawPcmStream(BufferedTrack src, int var1, int var2, int var3) {
		return src.samples != null && src.samples.length != 0 ? new RawPcmStream(src, var1, var2, var3) : null;
	}

	static int method2690(byte[] var0, int[] var1, int var2, int var3, int var4, int var5, int var6, int var7, RawPcmStream var8) {
		var2 >>= 8;
		var7 >>= 8;
		var4 <<= 2;
		if ((var5 = var3 + var7 - var2) > var6) {
			var5 = var6;
		}

		for (var5 -= 3; var3 < var5; var1[var3++] += var0[var2++] * var4) {
			var1[var3++] += var0[var2++] * var4;
			var1[var3++] += var0[var2++] * var4;
			var1[var3++] += var0[var2++] * var4;
		}

		for (var5 += 3; var3 < var5; var1[var3++] += var0[var2++] * var4) {
		}

		var8.pos = var2 << 8;
		return var3;
	}

	static int method2718(int var0, byte[] var1, int[] var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, RawPcmStream var10) {
		var3 >>= 8;
		var9 >>= 8;
		var5 <<= 2;
		var6 <<= 2;
		if ((var7 = var4 + var9 - var3) > var8) {
			var7 = var8;
		}

		var4 <<= 1;
		var7 <<= 1;

		int var10001;
		byte var11;
		for (var7 -= 6; var4 < var7; var2[var10001] += var11 * var6) {
			var11 = var1[var3++];
			var2[var4++] += var11 * var5;
			var2[var4++] += var11 * var6;
			var11 = var1[var3++];
			var2[var4++] += var11 * var5;
			var2[var4++] += var11 * var6;
			var11 = var1[var3++];
			var2[var4++] += var11 * var5;
			var2[var4++] += var11 * var6;
			var11 = var1[var3++];
			var2[var4++] += var11 * var5;
			var10001 = var4++;
		}

		for (var7 += 6; var4 < var7; var2[var10001] += var11 * var6) {
			var11 = var1[var3++];
			var2[var4++] += var11 * var5;
			var10001 = var4++;
		}

		var10.pos = var3 << 8;
		return var4 >> 1;
	}

	static int method2681(byte[] var0, int[] var1, int var2, int var3, int var4, int var5, int var6, int var7, RawPcmStream var8) {
		var2 >>= 8;
		var7 >>= 8;
		var4 <<= 2;
		if ((var5 = var3 + var2 - (var7 - 1)) > var6) {
			var5 = var6;
		}

		int var10001;
		for (var5 -= 3; var3 < var5; var1[var10001] += var0[var2--] * var4) {
			var10001 = var3++;
			var1[var10001] += var0[var2--] * var4;
			var10001 = var3++;
			var1[var10001] += var0[var2--] * var4;
			var10001 = var3++;
			var1[var10001] += var0[var2--] * var4;
			var10001 = var3++;
		}

		for (var5 += 3; var3 < var5; var1[var10001] += var0[var2--] * var4) {
			var10001 = var3++;
		}

		var8.pos = var2 << 8;
		return var3;
	}

	static int method2748(int var0, byte[] var1, int[] var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, RawPcmStream var10) {
		var3 >>= 8;
		var9 >>= 8;
		var5 <<= 2;
		var6 <<= 2;
		if ((var7 = var3 + var4 - (var9 - 1)) > var8) {
			var7 = var8;
		}

		var4 <<= 1;
		var7 <<= 1;

		int var10001;
		byte var11;
		for (var7 -= 6; var4 < var7; var2[var10001] += var11 * var6) {
			var11 = var1[var3--];
			var2[var4++] += var11 * var5;
			var2[var4++] += var11 * var6;
			var11 = var1[var3--];
			var2[var4++] += var11 * var5;
			var2[var4++] += var11 * var6;
			var11 = var1[var3--];
			var2[var4++] += var11 * var5;
			var2[var4++] += var11 * var6;
			var11 = var1[var3--];
			var2[var4++] += var11 * var5;
			var10001 = var4++;
		}

		for (var7 += 6; var4 < var7; var2[var10001] += var11 * var6) {
			var11 = var1[var3--];
			var2[var4++] += var11 * var5;
			var10001 = var4++;
		}

		var10.pos = var3 << 8;
		return var4 >> 1;
	}

	static int method2691(int var0, int var1, byte[] var2, int[] var3, int var4, int var5, int var6, int var7, int var8, int var9, RawPcmStream var10, int var11, int var12) {
		if (var11 == 0 || (var7 = var5 + (var11 + (var9 - var4) - 257) / var11) > var8) {
			var7 = var8;
		}

		byte var13;
		int var10001;
		while (var5 < var7) {
			var1 = var4 >> 8;
			var13 = var2[var1];
			var10001 = var5++;
			var3[var10001] += ((var13 << 8) + (var2[var1 + 1] - var13) * (var4 & 255)) * var6 >> 6;
			var4 += var11;
		}

		if (var11 == 0 || (var7 = var5 + (var11 + (var9 - var4) - 1) / var11) > var8) {
			var7 = var8;
		}

		for (var1 = var12; var5 < var7; var4 += var11) {
			var13 = var2[var4 >> 8];
			var10001 = var5++;
			var3[var10001] += ((var13 << 8) + (var1 - var13) * (var4 & 255)) * var6 >> 6;
		}

		var10.pos = var4;
		return var5;
	}

	static int method2686(int var0, int var1, byte[] var2, int[] var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, RawPcmStream var11, int var12, int var13) {
		if (var12 == 0 || (var8 = var5 + (var10 - var4 + var12 - 257) / var12) > var9) {
			var8 = var9;
		}

		var5 <<= 1;

		byte var14;
		int var10001;
		for (var8 <<= 1; var5 < var8; var4 += var12) {
			var1 = var4 >> 8;
			var14 = var2[var1];
			var0 = (var14 << 8) + (var4 & 255) * (var2[var1 + 1] - var14);
			var10001 = var5++;
			var3[var10001] += var0 * var6 >> 6;
			var10001 = var5++;
			var3[var10001] += var0 * var7 >> 6;
		}

		if (var12 == 0 || (var8 = (var5 >> 1) + (var10 - var4 + var12 - 1) / var12) > var9) {
			var8 = var9;
		}

		var8 <<= 1;

		for (var1 = var13; var5 < var8; var4 += var12) {
			var14 = var2[var4 >> 8];
			var0 = (var14 << 8) + (var1 - var14) * (var4 & 255);
			var10001 = var5++;
			var3[var10001] += var0 * var6 >> 6;
			var10001 = var5++;
			var3[var10001] += var0 * var7 >> 6;
		}

		var11.pos = var4;
		return var5 >> 1;
	}

	static int method2687(int var0, int var1, byte[] var2, int[] var3, int var4, int var5, int var6, int var7, int var8, int var9, RawPcmStream var10, int var11, int var12) {
		if (var11 == 0 || (var7 = var5 + (var11 + (var9 + 256 - var4)) / var11) > var8) {
			var7 = var8;
		}

		int var10001;
		while (var5 < var7) {
			var1 = var4 >> 8;
			byte var13 = var2[var1 - 1];
			var10001 = var5++;
			var3[var10001] += ((var13 << 8) + (var2[var1] - var13) * (var4 & 255)) * var6 >> 6;
			var4 += var11;
		}

		if (var11 == 0 || (var7 = var5 + (var11 + (var9 - var4)) / var11) > var8) {
			var7 = var8;
		}

		var0 = var12;

		for (var1 = var11; var5 < var7; var4 += var1) {
			var10001 = var5++;
			var3[var10001] += ((var0 << 8) + (var2[var4 >> 8] - var0) * (var4 & 255)) * var6 >> 6;
		}

		var10.pos = var4;
		return var5;
	}

	static int method2688(int var0, int var1, byte[] var2, int[] var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, RawPcmStream var11, int var12, int var13) {
		if (var12 == 0 || (var8 = var5 + (var10 + 256 - var4 + var12) / var12) > var9) {
			var8 = var9;
		}

		var5 <<= 1;

		int var10001;
		for (var8 <<= 1; var5 < var8; var4 += var12) {
			var1 = var4 >> 8;
			byte var14 = var2[var1 - 1];
			var0 = (var2[var1] - var14) * (var4 & 255) + (var14 << 8);
			var10001 = var5++;
			var3[var10001] += var0 * var6 >> 6;
			var10001 = var5++;
			var3[var10001] += var0 * var7 >> 6;
		}

		if (var12 == 0 || (var8 = (var5 >> 1) + (var10 - var4 + var12) / var12) > var9) {
			var8 = var9;
		}

		var8 <<= 1;

		for (var1 = var13; var5 < var8; var4 += var12) {
			var0 = (var1 << 8) + (var4 & 255) * (var2[var4 >> 8] - var1);
			var10001 = var5++;
			var3[var10001] += var0 * var6 >> 6;
			var10001 = var5++;
			var3[var10001] += var0 * var7 >> 6;
		}

		var11.pos = var4;
		return var5 >> 1;
	}

	static int method2710(byte[] var0, int[] var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, RawPcmStream var9) {
		var2 >>= 8;
		var8 >>= 8;
		var4 <<= 2;
		var5 <<= 2;
		if ((var6 = var3 + var8 - var2) > var7) {
			var6 = var7;
		}

		var9.field1489 += var9.field1492 * (var6 - var3);
		var9.field1486 += var9.field1494 * (var6 - var3);

		int var10001;
		for (var6 -= 3; var3 < var6; var4 += var5) {
			var10001 = var3++;
			var1[var10001] += var0[var2++] * var4;
			var4 += var5;
			var10001 = var3++;
			var1[var10001] += var0[var2++] * var4;
			var4 += var5;
			var10001 = var3++;
			var1[var10001] += var0[var2++] * var4;
			var4 += var5;
			var10001 = var3++;
			var1[var10001] += var0[var2++] * var4;
		}

		for (var6 += 3; var3 < var6; var4 += var5) {
			var10001 = var3++;
			var1[var10001] += var0[var2++] * var4;
		}

		var9.field1484 = var4 >> 2;
		var9.pos = var2 << 8;
		return var3;
	}

	static int method2753(int var0, byte[] var1, int[] var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, RawPcmStream var12) {
		var3 >>= 8;
		var11 >>= 8;
		var5 <<= 2;
		var6 <<= 2;
		var7 <<= 2;
		var8 <<= 2;
		if ((var9 = var11 + var4 - var3) > var10) {
			var9 = var10;
		}

		var12.field1484 += var12.field1485 * (var9 - var4);
		var4 <<= 1;
		var9 <<= 1;

		byte var13;
		int var10001;
		for (var9 -= 6; var4 < var9; var6 += var8) {
			var13 = var1[var3++];
			var2[var4++] += var13 * var5;
			var5 += var7;
			var2[var4++] += var13 * var6;
			var6 += var8;
			var13 = var1[var3++];
			var2[var4++] += var13 * var5;
			var5 += var7;
			var2[var4++] += var13 * var6;
			var6 += var8;
			var13 = var1[var3++];
			var2[var4++] += var13 * var5;
			var5 += var7;
			var2[var4++] += var13 * var6;
			var6 += var8;
			var13 = var1[var3++];
			var2[var4++] += var13 * var5;
			var5 += var7;
			var2[var4++] += var13 * var6;
		}

		for (var9 += 6; var4 < var9; var6 += var8) {
			var13 = var1[var3++];
			var2[var4++] += var13 * var5;
			var5 += var7;
			var2[var4++] += var13 * var6;
		}

		var12.field1489 = var5 >> 2;
		var12.field1486 = var6 >> 2;
		var12.pos = var3 << 8;
		return var4 >> 1;
	}

	static int method2683(byte[] var0, int[] var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, RawPcmStream var9) {
		var2 >>= 8;
		var8 >>= 8;
		var4 <<= 2;
		var5 <<= 2;
		if ((var6 = var3 + var2 - (var8 - 1)) > var7) {
			var6 = var7;
		}

		var9.field1489 += var9.field1492 * (var6 - var3);
		var9.field1486 += var9.field1494 * (var6 - var3);

		int var10001;
		for (var6 -= 3; var3 < var6; var4 += var5) {
			var10001 = var3++;
			var1[var10001] += var0[var2--] * var4;
			var4 += var5;
			var10001 = var3++;
			var1[var10001] += var0[var2--] * var4;
			var4 += var5;
			var10001 = var3++;
			var1[var10001] += var0[var2--] * var4;
			var4 += var5;
			var10001 = var3++;
			var1[var10001] += var0[var2--] * var4;
		}

		for (var6 += 3; var3 < var6; var4 += var5) {
			var10001 = var3++;
			var1[var10001] += var0[var2--] * var4;
		}

		var9.field1484 = var4 >> 2;
		var9.pos = var2 << 8;
		return var3;
	}

	static int method2719(int var0, byte[] var1, int[] var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, RawPcmStream var12) {
		var3 >>= 8;
		var11 >>= 8;
		var5 <<= 2;
		var6 <<= 2;
		var7 <<= 2;
		var8 <<= 2;
		if ((var9 = var3 + var4 - (var11 - 1)) > var10) {
			var9 = var10;
		}

		var12.field1484 += var12.field1485 * (var9 - var4);
		var4 <<= 1;
		var9 <<= 1;

		var9 -= 6;
		while (var4 < var9) {
			byte var13 = var1[var3--];
			var2[var4++] += var13 * var5;
			var5 += var7;
			var2[var4++] += var13 * var6;
			var6 += var8;
		}

		var9 += 6;
		while (var4 < var9) {
			byte var13 = var1[var3--];
			var2[var4++] += var13 * var5;
			var5 += var7;
			var2[var4++] += var13 * var6;
			var6 += var8;
		}

		var12.field1489 = var5 >> 2;
		var12.field1486 = var6 >> 2;
		var12.pos = var3 << 8;
		return var4 >> 1;
	}

	static int method2693(int var0, int var1, byte[] var2, int[] var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, RawPcmStream var11, int var12, int var13) {
		var11.field1489 -= var11.field1492 * var5;
		var11.field1486 -= var11.field1494 * var5;
		if (var12 == 0 || (var8 = var5 + (var10 - var4 + var12 - 257) / var12) > var9) {
			var8 = var9;
		}

		byte var14;
		while (var5 < var8) {
			var1 = var4 >> 8;
			var14 = var2[var1];
			var3[var5++] += ((var14 << 8) + (var2[var1 + 1] - var14) * (var4 & 255)) * var6 >> 6;
			var6 += var7;
			var4 += var12;
		}

		if (var12 == 0 || (var8 = var5 + (var10 - var4 + var12 - 1) / var12) > var9) {
			var8 = var9;
		}

		for (var1 = var13; var5 < var8; var4 += var12) {
			var14 = var2[var4 >> 8];
			var3[var5++] += ((var14 << 8) + (var1 - var14) * (var4 & 255)) * var6 >> 6;
			var6 += var7;
		}

		var11.field1489 += var11.field1492 * var5;
		var11.field1486 += var11.field1494 * var5;
		var11.field1484 = var6;
		var11.pos = var4;
		return var5;
	}

	static int method2694(int var0, int var1, byte[] var2, int[] var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, RawPcmStream var13, int var14, int var15) {
		var13.field1484 -= var5 * var13.field1485;
		if (var14 == 0 || (var10 = var5 + (var12 - var4 + var14 - 257) / var14) > var11) {
			var10 = var11;
		}

		var5 <<= 1;

		byte var16;
		for (var10 <<= 1; var5 < var10; var4 += var14) {
			var1 = var4 >> 8;
			var16 = var2[var1];
			var0 = (var16 << 8) + (var4 & 255) * (var2[var1 + 1] - var16);
			var3[var5++] += var0 * var6 >> 6;
			var6 += var8;
			var3[var5++] += var0 * var7 >> 6;
			var7 += var9;
		}

		if (var14 == 0 || (var10 = (var5 >> 1) + (var12 - var4 + var14 - 1) / var14) > var11) {
			var10 = var11;
		}

		var10 <<= 1;

		for (var1 = var15; var5 < var10; var4 += var14) {
			var16 = var2[var4 >> 8];
			var0 = (var16 << 8) + (var1 - var16) * (var4 & 255);
			var3[var5++] += var0 * var6 >> 6;
			var6 += var8;
			var3[var5++] += var0 * var7 >> 6;
			var7 += var9;
		}

		var5 >>= 1;
		var13.field1484 += var13.field1485 * var5;
		var13.field1489 = var6;
		var13.field1486 = var7;
		var13.pos = var4;
		return var5;
	}

	static int method2738(int var0, int var1, byte[] var2, int[] var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, RawPcmStream var11, int var12, int var13) {
		var11.field1489 -= var11.field1492 * var5;
		var11.field1486 -= var11.field1494 * var5;
		if (var12 == 0 || (var8 = var5 + (var10 + 256 - var4 + var12) / var12) > var9) {
			var8 = var9;
		}

		while (var5 < var8) {
			var1 = var4 >> 8;
			byte var14 = var2[var1 - 1];
			var3[var5++] += ((var14 << 8) + (var2[var1] - var14) * (var4 & 255)) * var6 >> 6;
			var6 += var7;
			var4 += var12;
		}

		if (var12 == 0 || (var8 = var5 + (var10 - var4 + var12) / var12) > var9) {
			var8 = var9;
		}

		var0 = var13;

		for (var1 = var12; var5 < var8; var4 += var1) {
			var3[var5++] += ((var0 << 8) + (var2[var4 >> 8] - var0) * (var4 & 255)) * var6 >> 6;
			var6 += var7;
		}

		var11.field1489 += var11.field1492 * var5;
		var11.field1486 += var11.field1494 * var5;
		var11.field1484 = var6;
		var11.pos = var4;
		return var5;
	}

	static int method2781(int var0, int var1, byte[] var2, int[] var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, RawPcmStream var13, int var14, int var15) {
		var13.field1484 -= var5 * var13.field1485;
		if (var14 == 0 || (var10 = var5 + (var12 + 256 - var4 + var14) / var14) > var11) {
			var10 = var11;
		}

		var5 <<= 1;

		for (var10 <<= 1; var5 < var10; var4 += var14) {
			var1 = var4 >> 8;
			byte var16 = var2[var1 - 1];
			var0 = (var2[var1] - var16) * (var4 & 255) + (var16 << 8);
			var3[var5++] += var0 * var6 >> 6;
			var6 += var8;
			var3[var5++] += var0 * var7 >> 6;
			var7 += var9;
		}

		if (var14 == 0 || (var10 = (var5 >> 1) + (var12 - var4 + var14) / var14) > var11) {
			var10 = var11;
		}

		var10 <<= 1;

		for (var1 = var15; var5 < var10; var4 += var14) {
			var0 = (var1 << 8) + (var4 & 255) * (var2[var4 >> 8] - var1);
			var3[var5++] += var0 * var6 >> 6;
			var6 += var8;
			var3[var5++] += var0 * var7 >> 6;
			var7 += var9;
		}

		var5 >>= 1;
		var13.field1484 += var13.field1485 * var5;
		var13.field1489 = var6;
		var13.field1486 = var7;
		var13.pos = var4;
		return var5;
	}

}