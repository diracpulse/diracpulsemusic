import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;


public class PlayData implements ActionListener {
	
	public enum DataType {
		WINDOW,
		SEQUENCER;
	}
	
	int currentOffsetInMillis;
	int refreshRateInMillis;
	int endTimeInMillis;
	Timer timer;
	GraphEditor parent;
	boolean startPlay = true;
	
	PlayData(GraphEditor parent, int refreshRateInMillis, int endTimeInMillis, DataType type) {
		this.parent = parent;
		this.currentOffsetInMillis = 0;
		this.refreshRateInMillis = refreshRateInMillis;
		this.endTimeInMillis = endTimeInMillis;
		timer = new Timer(refreshRateInMillis, this);
		if(type == DataType.WINDOW) SynthTools.createPCMWindowData(parent);
		if(type == DataType.SEQUENCER) SynthTools.createPCMSequencerData(parent);
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
		GraphEditor.drawPlayTime(currentOffsetInMillis);
		currentOffsetInMillis += refreshRateInMillis;
		if(currentOffsetInMillis >= endTimeInMillis) timer.stop();
	}
	
}
