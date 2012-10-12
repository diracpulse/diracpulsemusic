
public class GraphUtils {
	
	public static int screenXToTime(double x) {
		double timeSpan = GraphEditor.maxViewTime - GraphEditor.minViewTime;
		double timesPerPixel = timeSpan / (double) GraphEditor.view.getWidth();
		int returnVal = (int) Math.round(x * timesPerPixel) + GraphEditor.minViewTime;
		//System.out.println("screenXToTime " + returnVal);
		return returnVal;
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
