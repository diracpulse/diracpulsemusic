import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;


public class PlayDataInWindow implements ActionListener {
	
	int currentOffsetInMillis;
	int refreshRateInMillis;
	int endTimeInMillis;
	Timer timer;
	DFTEditor parent;
	boolean startPlay = true;
	
	PlayDataInWindow(DFTEditor parent, int refreshRateInMillis, int endTimeInMillis, boolean linear) {
		this.parent = parent;
		this.currentOffsetInMillis = 0;
		this.refreshRateInMillis = refreshRateInMillis;
		this.endTimeInMillis = endTimeInMillis;
		timer = new Timer(refreshRateInMillis, this);
		if(linear) {
			SynthTools.createPCMDataLinear();
		} else {
			SynthTools.createPCMData();
		}
        timer.setInitialDelay(0);
        timer.start();
	}

	public void actionPerformed(ActionEvent e) {
		//System.out.println(currentOffsetInMillis);
		if(startPlay) {
			SynthTools.playPCMData();
			startPlay = false;
		}
		parent.drawPlayTime(currentOffsetInMillis, refreshRateInMillis);
		currentOffsetInMillis += refreshRateInMillis;
		if(currentOffsetInMillis >= endTimeInMillis) timer.stop();
	}
	
}
