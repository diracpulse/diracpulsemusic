import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;


public class PlayDataInWindow implements ActionListener {
	
	int currentOffsetInMillis;
	int refreshRateInMillis;
	int endTimeInMillis;
	Timer timer;
	GraphEditor parent;
	boolean startPlay = true;
	
	PlayDataInWindow(GraphEditor parent, int refreshRateInMillis, int endTimeInMillis) {
		this.parent = parent;
		this.currentOffsetInMillis = 0;
		this.refreshRateInMillis = refreshRateInMillis;
		this.endTimeInMillis = endTimeInMillis;
		timer = new Timer(refreshRateInMillis, this);
		SynthTools.createPCMData(parent);
		//SynthTools.createPCMData(parent, GraphEditor.leftX, endTimeInMillis / GraphEditor.timeStepInMillis);
        timer.setInitialDelay(2000);
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
