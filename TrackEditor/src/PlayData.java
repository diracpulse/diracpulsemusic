import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;


public class PlayData implements ActionListener {
	
	public enum DataType {
		LOOP,
		TRACK,
	}
	
	int currentOffsetInMillis;
	int refreshRateInMillis;
	int endTimeInMillis;
	Timer timer;
	TrackEditor parent;
	boolean startPlay = true;
	
	PlayData(TrackEditor parent, int refreshRateInMillis, int endTimeInMillis, DataType type) {
		this.parent = parent;
		this.currentOffsetInMillis = 0;
		this.refreshRateInMillis = refreshRateInMillis;
		this.endTimeInMillis = endTimeInMillis;
		timer = new Timer(this.refreshRateInMillis, this);
		if(type == DataType.LOOP) SynthTools.createPCMLoopDataLinear();
		if(type == DataType.TRACK) SynthTools.createPCMTrackDataLinear();
		//SynthTools.createPCMData(parent, GraphEditor.leftX, endTimeInMillis / GraphEditor.timeStepInMillis);
        timer.setInitialDelay(0);
        timer.start();
	}

	public void actionPerformed(ActionEvent e) {
		//System.out.println(currentOffsetInMillis);
		if(startPlay) {
			SynthTools.playWindow();
			startPlay = false;
		}
		TrackEditor.drawPlayTime(currentOffsetInMillis);
		currentOffsetInMillis += refreshRateInMillis;
		if(currentOffsetInMillis >= endTimeInMillis) timer.stop();
	}
	
}
