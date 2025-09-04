package fx.jank.ui;

import fx.jank.rs.Envelope;
import fx.jank.ui.components.EnvelopeGraph;
import java.awt.event.MouseEvent;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.swing.event.MouseInputAdapter;

class EnvelopeEditor extends GraphView {
	private final SynthPanel parent;
	private final Supplier<Envelope> targetProvider;
	private final BooleanSupplier checkActive;
	private final MouseInputAdapter inputListener = new MouseInputAdapter() {
		private int selectedIndex = -1;

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isControlDown() && selectedIndex == -1) {
				createPoint(e);
			} else {
				updateSelectedIndex(e);
			}
			graph.repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (selectedIndex != -1) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					deleteSelected();
				} else {
					movePoint(e.getX(), e.getY(), e.getComponent().getWidth(), e.getComponent().getHeight());
				}
				graph.repaint();
				parent.update();
				selectedIndex = -1;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (selectedIndex == -1) {
				return;
			}

			movePoint(e.getX(), e.getY(), e.getComponent().getWidth(), e.getComponent().getHeight());
			graph.repaint();
			parent.update();
		}

		private void createPoint(MouseEvent e) {
			int mx = e.getX(); int my = e.getY();
			int cw = e.getComponent().getWidth(); int ch = e.getComponent().getHeight();
			int newX = mx * 65535 / cw;
			int newY = 65535 - my * 65535 / ch;
			Envelope target = targetProvider.get();
			selectedIndex = target.insertPoint(newX, newY);
			graph.repaint();
			parent.update();
		}

		private void deleteSelected() {
			if (selectedIndex != -1)
				targetProvider.get().deletePoint(selectedIndex);
			selectedIndex = -1;
			graph.repaint();
			parent.update();
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

	EnvelopeEditor(SynthPanel daddy, String name, Supplier<Envelope> targetProvider, BooleanSupplier checkActive) {
		super(name, new EnvelopeGraph(targetProvider));
		this.parent = daddy;
		this.targetProvider = targetProvider;
		this.checkActive = checkActive;
	}

	public void update() {
		if (checkActive == null)
			return;
		boolean active = checkActive.getAsBoolean();
		graph.setShouldDraw(active);
		if (active) {
			graph.addMouseListener(this.inputListener);
		} else {
			graph.removeMouseListener(this.inputListener);
		}
		graph.repaint();
	}
}
