import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
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
	AbstractEditor parent;
	boolean startPlay = true;
	public static SynthType synthType = SynthType.Linear;
	
	PlayDataInWindow(AbstractEditor parent, int refreshRateInMillis, int endTimeInMillis) {
		this.parent = parent;
		this.currentOffsetInMillis = DFTEditor.getMinViewTimeInMillis();
		this.refreshRateInMillis = refreshRateInMillis;
		this.endTimeInMillis = endTimeInMillis;
		timer = new Timer(refreshRateInMillis, this);
		if(synthType == SynthType.Linear) parent.createPCMDataLinear();
		if(synthType == SynthType.LinearCubicSpline) parent.createPCMDataLinearCubicSpline();
		if(synthType == SynthType.LinearNoise) parent.createPCMDataLinearNoise();		
		JOptionPane.showMessageDialog((JFrame) parent, "Ready To Play");
        timer.setInitialDelay(0);
        timer.start();
	}

	public void actionPerformed(ActionEvent e) {
		//System.out.println(currentOffsetInMillis);
		if(startPlay) {
			parent.playPCMData();
			startPlay = false;
		}
		parent.drawPlayTime(currentOffsetInMillis);
		currentOffsetInMillis += refreshRateInMillis;
		if(currentOffsetInMillis >= endTimeInMillis) {
			parent.drawPlayTime(parent.getMaxViewTimeInMillis());
			timer.stop();
		}
	}
	
}
