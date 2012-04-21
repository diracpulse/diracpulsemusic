
public class GraphUtils {
	
	public static int screenXToTime(double x) {
		double timeSpan = GraphEditor.maxViewTime - GraphEditor.minViewTime;
		double timesPerPixel = timeSpan / (double) GraphEditor.view.getWidth();
		return (int) Math.round(x * timesPerPixel) + GraphEditor.minViewTime;
	}
	
	public static double screenYToValue(double y) {
		if(y < GraphEditor.upperOffset) y = GraphEditor.upperOffset;
		double pixelsPerValue = 0.0;
		if(GraphView.yView == GraphView.YView.AMPLITUDE) pixelsPerValue = (double) (GraphEditor.view.getHeight() - GraphEditor.upperOffset) / (GraphEditor.maxViewLogAmplitude - GraphEditor.minViewLogAmplitude);
		if(GraphView.yView == GraphView.YView.FREQUENCY) pixelsPerValue = (double) (GraphEditor.view.getHeight() - GraphEditor.upperOffset) / (GraphEditor.maxViewNote - GraphEditor.minViewNote);
		if(GraphView.yView == GraphView.YView.AMPLITUDE) return GraphEditor.maxViewLogAmplitude - ((y - GraphEditor.upperOffset) / pixelsPerValue);
		if(GraphView.yView == GraphView.YView.FREQUENCY) return GraphEditor.maxViewNote - ((y - GraphEditor.upperOffset) / pixelsPerValue);
		return -1.0;
	}

}
