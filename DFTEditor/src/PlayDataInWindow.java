import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.Timer;


public class PlayDataInWindow implements ActionListener {
	
	public enum SynthType {
		Linear,
		LinearCubicSpline,
		LinearNoise;
	}

	int currentOffsetInMillis;
	int refreshRateInMillis;
	int endTimeInMillis;
	Timer timer;
	DFTEditor parent;
	boolean startPlay = true;
	public static SynthType synthType = SynthType.Linear;
	
	PlayDataInWindow(DFTEditor parent, int refreshRateInMillis, int endTimeInMillis) {
		this.parent = parent;
		this.currentOffsetInMillis = 0;
		this.refreshRateInMillis = refreshRateInMillis;
		this.endTimeInMillis = endTimeInMillis;
		timer = new Timer(refreshRateInMillis, this);
		if(synthType == SynthType.Linear) SynthTools.createPCMDataLinear();
		if(synthType == SynthType.LinearCubicSpline) SynthTools.createPCMDataLinearCubicSpline();
		if(synthType == SynthType.LinearNoise) SynthTools.createPCMDataLinearNoise();		
		JOptionPane.showMessageDialog(parent, "Ready To Play");
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
