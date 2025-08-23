package fx.jank.rs;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum WaveFun
{
	OFF("Off"),
	SQR("Sqr"),
	SIN("Sin"),
	SAW("Saw"),
	NOISE("Noise");

	public final String text;
}
