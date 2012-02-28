import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;


public class DisplayPlayTime implements ActionListener {
	
	int currentOffsetInMillis;
	int refreshRateInMillis;
	int endTimeInMillis;
	Timer timer;
	DFTEditor parent;
	
	DisplayPlayTime(DFTEditor parent, int refreshRateInMillis, int endTimeInMillis) {
		this.parent = parent;
		this.currentOffsetInMillis = 0;
		this.refreshRateInMillis = refreshRateInMillis;
		this.endTimeInMillis = endTimeInMillis;
		timer = new Timer(refreshRateInMillis, this);
        timer.setInitialDelay(0);
        timer.start(); 
	}

	public void actionPerformed(ActionEvent e) {
		System.out.println(currentOffsetInMillis);
		parent.drawPlayTime(currentOffsetInMillis, refreshRateInMillis);
		currentOffsetInMillis += refreshRateInMillis;
		if(currentOffsetInMillis >= endTimeInMillis) timer.stop();
	}
	
}
