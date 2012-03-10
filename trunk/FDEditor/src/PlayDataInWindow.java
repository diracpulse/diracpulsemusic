import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;


public class PlayDataInWindow implements ActionListener {
	
	int currentOffsetInMillis;
	int refreshRateInMillis;
	int endTimeInMillis;
	Timer timer;
	FDEditor parent;
	boolean startPlay = true;
	
	PlayDataInWindow(FDEditor parent, int startTime, int endTime, int refreshRateInMillis, int endTimeInMillis) {
		this.parent = parent;
		this.currentOffsetInMillis = 0;
		this.refreshRateInMillis = refreshRateInMillis;
		this.endTimeInMillis = endTimeInMillis;
		timer = new Timer(refreshRateInMillis, this);
		SynthTools.createPCMData(parent, FDEditor.leftX, endTimeInMillis / FDEditor.timeStepInMillis);
        timer.setInitialDelay(0);
        timer.start();
	}

	public void actionPerformed(ActionEvent e) {
		//System.out.println(currentOffsetInMillis);
		if(startPlay) {
			SynthTools.playWindow();
			startPlay = false;
		}
		FDEditor.drawPlayTime(currentOffsetInMillis, refreshRateInMillis);
		currentOffsetInMillis += refreshRateInMillis;
		if(currentOffsetInMillis >= endTimeInMillis) timer.stop();
	}
	
}
