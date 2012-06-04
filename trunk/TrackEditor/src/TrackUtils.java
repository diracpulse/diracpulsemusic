
public class TrackUtils {
	
	public static int screenXToTime(double x) {
		//double timeSpan = TrackEditor.maxViewTime - TrackEditor.minViewTime;
		//double timesPerPixel = timeSpan / (double) TrackEditor.view.getWidth();
		//int returnVal = (int) Math.round(x * timesPerPixel) + TrackEditor.minViewTime;
		//System.out.println("screenXToTime " + returnVal);
		return 0; //returnVal;
	}
	
	public static double screenYToValue(double y) {
		if(y < 0) y = 0;
		if(y > TrackEditor.view.getHeight()) y = TrackEditor.view.getHeight();
		//double pixelsPerValue = 0.0;
		//if(TrackView.yView == TrackView.YView.AMPLITUDE) pixelsPerValue = (double) TrackEditor.view.getHeight() / (TrackEditor.maxViewLogAmplitude - TrackEditor.minViewLogAmplitude);
		//if(TrackView.yView == TrackView.YView.FREQUENCY) pixelsPerValue = (double) TrackEditor.view.getHeight() / (TrackEditor.maxViewNote - TrackEditor.minViewNote);
		//if(TrackView.yView == TrackView.YView.AMPLITUDE) return TrackEditor.maxViewLogAmplitude - (y / pixelsPerValue);
		//if(TrackView.yView == TrackView.YView.FREQUENCY) return TrackEditor.maxViewNote - (y / pixelsPerValue);
		return -1.0;
	}

}
