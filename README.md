### What is this?

Have you ever wondered how the sound effects in old school runescape were (or more specifically, continually are) generated? This project is a (wip) attempt to recreate the original GUI of the JagFx synthesizer. [Scroll down to screenshots](#Screenshots)

### Why?

I wanted to play around with it, and more importantly understand how everything was done.

### Is it functional?

A lot of things are functional, but some aren't. Feel free to contribute, just make a pull request, or contact me `@luc_ass.` on discord.

There is no way to:
* Save a synth config or export to .wav
* Enable the filter, or change filter parameters
* View a filters' frequency response
* Not resynthesize all samples way too often because of shoddy vibe coding (performance "issue")

If you search for `todo` in this project you'll probably also find more stuff to do, if that's your thing.

### Can you explain how to use it?

Every synth is made up of up to 10 tones, each with their own synth/filter parameters. Each tone consists of up to 5 harmonic oscillators generating a waveform. Each oscillator's volume and pitch can be set relative to the base amplitude and frequency, and the phase can be controlled with a delay setting. The base frequency is determined using an envelope with a configurable minimum and maximum. The base amplitude is determined similarly. Both frequency and amplitude can be modulated by another wave, these waves are configured the same way as the base frequency/amplitude are. The final 2 envelopes on the main panel, labeled "gap on" and "gap off" are a bit different. These don't configure a wave that modulates the output, but instead turn the output on and off intermittently. This can be used for frequency synthesis, by choosing a square wave with a frequency of 0Hz a constant positive "voltage" can be generated for this purpose. Reverb can also be enabled, with a configurable delay and volume. Finally, each tone has an optional configurable filter.

### Screenshots

![A screenshot of the original gui (cow)](references/cow_screenshot.jpeg)
_Screenshot from Ian Taylor on twitter (The cow death sound, annoyingly with some parameters changed from the actual sound)_
![A screenshot of my interpretation (cow too)](screenshots/cow_screenshot.png)
_Comparison, same parameters modified_
![A screenshot of actual cow](screenshots/actual_cow.png)
_This is what it actually is like!_

## Credits

Andrew Gower, mostly, I think. Absolute legend.

Runelite, for the osrs cache library

The three pieces of reference media!!!!!1