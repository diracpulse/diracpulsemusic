
public class GraphUtils {
	
	public static int screenXToTime(int x) {
		double timeOffset =  x - GraphEditor.minViewTime;
		double timeSpan = GraphEditor.maxViewTime - GraphEditor.minViewTime;
		double timesPerPixel = (double) GraphEditor.view.getWidth() / timeSpan;
		return (int) Math.round(timeOffset * timesPerPixel) + GraphEditor.minViewTime;	
	}
	
	public static int screenYToValue(int x) {
		double timeOffset =  x - GraphEditor.minViewTime;
		double timeSpan = GraphEditor.maxViewTime - GraphEditor.minViewTime;
		double timesPerPixel = (double) GraphEditor.view.getWidth() / timeSpan;
		return (int) Math.round(timeOffset * timesPerPixel) + GraphEditor.minViewTime;	
	}
	
}
