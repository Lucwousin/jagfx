package fx.jank.ui;

import fx.jank.rs.Envelope;
import fx.jank.ui.components.EnvelopeGraph;
import java.awt.event.MouseEvent;
import java.util.function.BooleanSupplier;
import javax.inject.Provider;
import javax.swing.event.MouseInputAdapter;

class EnvelopeEditor extends GraphView {
	private final SynthPanel parent;
	private final Provider<Envelope> targetProvider;
	private final BooleanSupplier checkActive;
	private final MouseInputAdapter inputListener = new MouseInputAdapter() {
		private int selectedIndex = -1;

		// todo: ui scaling bs
		//todo : lock first and last index to edges of envelope
		// todo: Update parent on change!
		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isControlDown()) {
				// todo: insert new point
			} else if (e.getButton() == MouseEvent.BUTTON2) {
				updateSelectedIndex(e);
				// todo: delete point
			} else {
				updateSelectedIndex(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (selectedIndex != -1) {
				movePoint(e.getX(), e.getY(), e.getComponent().getWidth(), e.getComponent().getHeight());
				parent.update();
				graph.repaint();
				selectedIndex = -1;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (selectedIndex == -1) {
				return;
			}

			movePoint(e.getX(), e.getY(), e.getComponent().getWidth(), e.getComponent().getHeight());
			parent.update();
			graph.repaint();
		}

		private void movePoint(int mx, int my, int cw, int ch) {
			int newX = mx * 65535 / cw;
			int newY = 65535 - my * 65535 / ch;
			Envelope target = targetProvider.get();
			if (selectedIndex == 0) {
				newX = 0;
			} else if (selectedIndex == target.getBands() - 1) {
				newX = 65535;
			}
			target.getX()[selectedIndex] = newX;
			target.getY()[selectedIndex] = newY;
			if (selectedIndex != 0 && selectedIndex != target.getBands() - 1)
				sortEnvelope(target, newX, newY);

		}

		private void sortEnvelope(Envelope target, int x, int y) {
			int direction = target.getX()[selectedIndex - 1] > x ? -1 : 1;

			int[] xs = target.getX();
			int[] ys = target.getY();
			// +1 and -1 because first and last are locked to edge
			while (selectedIndex + direction > 1 && selectedIndex + direction < target.getBands() - 1) {
				if ((direction < 0 && xs[selectedIndex + direction] <= x) ||
					(direction > 0 && x <= xs[selectedIndex + direction])) {
					break;
				}
				xs[selectedIndex] = xs[selectedIndex + direction];
				ys[selectedIndex] = ys[selectedIndex + direction];
				selectedIndex += direction;
				xs[selectedIndex] = x;
				ys[selectedIndex] = y;
			}
		}

		private void updateSelectedIndex(MouseEvent e) {
			Envelope target = targetProvider.get();
			int rx = e.getX() * 65535 / e.getComponent().getHeight();
			int ry = 65535 - e.getY() * 65535 / e.getComponent().getHeight();
			selectedIndex = findClosestIndex(target, rx, ry);
		}

		private int findClosestIndex(Envelope envelope, int x, int y) {
			int[] xs = envelope.getX();
			int[] ys = envelope.getY();
			long minDist = Long.MAX_VALUE;
			int closestIndex = -1;
			for (int i = 0; i < envelope.getBands(); i++) {
				long dist = (long)(xs[i] - x) * (long)(xs[i] - x) + (long)(ys[i] - y) * (long)(ys[i] - y);
				if (dist < (long) minDist) {
					minDist = dist;
					closestIndex = i;
				}
			}
			return closestIndex;
		}

	};

	EnvelopeEditor(SynthPanel daddy, String name, Provider<Envelope> targetProvider, BooleanSupplier checkActive) {
		super(name, new EnvelopeGraph(targetProvider));
		this.parent = daddy;
		this.targetProvider = targetProvider;
		this.checkActive = checkActive;
	}

	@Override
	public void revalidate() {
		if (checkActive == null)
			return;
		boolean active = checkActive.getAsBoolean();
		graph.setShouldDraw(active);
		if (active) {
			graph.addMouseListener(this.inputListener);
		} else {
			graph.removeMouseListener(this.inputListener);
		}
		super.revalidate();
	}
}
