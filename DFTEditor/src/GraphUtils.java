
public class GraphUtils {
	
	public static double screenXToValue(double x) {
		if(x < 0) x = 0;
		if(x > GraphEditor.view.getHeight()) x = GraphEditor.view.getHeight();
		double pixelsPerValue = 0.0;
		if(GraphView.xView == GraphView.XView.TIME) pixelsPerValue = (double) GraphEditor.view.getWidth() / (GraphEditor.maxViewTime - GraphEditor.minViewTime);
		if(GraphView.xView == GraphView.XView.FREQUENCY) pixelsPerValue = (double) GraphEditor.view.getWidth() / (GraphEditor.maxViewNote - GraphEditor.minViewNote);
		if(GraphView.xView == GraphView.XView.TIME) return GraphEditor.minViewTime + (x / pixelsPerValue);
		if(GraphView.xView == GraphView.XView.FREQUENCY) return GraphEditor.maxViewNote - (x / pixelsPerValue);
		return -1.0;
	}
	
	public static double screenYToValue(double y) {
		if(y < 0) y = 0;
		if(y > GraphEditor.view.getHeight()) y = GraphEditor.view.getHeight();
		double pixelsPerValue = 0.0;
		if(GraphView.yView == GraphView.YView.AMPLITUDE) pixelsPerValue = (double) GraphEditor.view.getHeight() / (GraphEditor.maxViewLogAmplitude - GraphEditor.minViewLogAmplitude);
		if(GraphView.yView == GraphView.YView.FREQUENCY) pixelsPerValue = (double) GraphEditor.view.getHeight() / (GraphEditor.maxViewNote - GraphEditor.minViewNote);
		if(GraphView.yView == GraphView.YView.AMPLITUDE) return GraphEditor.maxViewLogAmplitude - (y / pixelsPerValue);
		if(GraphView.yView == GraphView.YView.FREQUENCY) return GraphEditor.maxViewNote - (y / pixelsPerValue);
		return -1.0;
	}

}
