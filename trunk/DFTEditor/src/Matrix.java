
class Matrix {
	
	static void disentangleWithMatrix() {
		// create responseOfWaveletToNote
		int xDimension = FDData.noteBase + 2;
		int yDimension = FDData.noteBase + 1;
		double[][] responseOfWaveletToNote = new double[xDimension][yDimension];
		double noteRatio = Math.pow(2.0, -1.0 / (double) FDData.noteBase);
		double maxFreqHz = SynthTools.sampleRate / 16.0;
		double minFreqHz = (SynthTools.sampleRate / 32.0);
		for(double freqInHz = maxFreqHz ; Math.round(freqInHz * 10000.0) / 10000.0 >= minFreqHz; freqInHz *= noteRatio) {
			double samplesPerCycle = SynthTools.sampleRate / freqInHz;
			double noteBase = FDData.noteBase; Math.sqrt(freqInHz / (SynthTools.sampleRate / 8.0));
			if(noteBase > FDData.noteBase) noteBase = FDData.noteBase;
			double bins = DFT2.maxBinStep / (Math.pow(2.0, 1.0 / (double) noteBase) - 1.0);
			double currentPhase = 0.0;
			double deltaPhase = (freqInHz / SynthTools.sampleRate) * SynthTools.twoPI;
			double[] mono = new double[(int) Math.ceil(bins * samplesPerCycle * 2.0)]; // MAXWINDOWLENGTH eventually
			for(int index = 0; index < mono.length; index++) {
				mono[index] = Math.sin(currentPhase);
				currentPhase += deltaPhase;
			}
			for(double innerFreqInHz = maxFreqHz; Math.round(innerFreqInHz * 10000.0) / 10000.0 >= minFreqHz; innerFreqInHz *= noteRatio) {
				double innerNoteBase = FDData.noteBase * Math.sqrt(innerFreqInHz / (SynthTools.sampleRate / 8.0));
				if(innerNoteBase > FDData.noteBase) innerNoteBase = FDData.noteBase;
				bins = DFT2.maxBinStep / (Math.pow(2.0, 1.0 / (double) innerNoteBase) - 1.0);
				DFT2.Wavelet currentWavelet = DFT2.createWavelet(innerFreqInHz, bins);
				int xIndex = DFT2.frequencyToNote(maxFreqHz) - DFT2.frequencyToNote(innerFreqInHz);
				int yIndex = DFT2.frequencyToNote(maxFreqHz) - DFT2.frequencyToNote(freqInHz);
				responseOfWaveletToNote[xIndex][yIndex] = DFT2.singleDFTTest(currentWavelet, mono);
			}
		}
		// create test signal
		double maxSamplesPerCycle = SynthTools.sampleRate / minFreqHz;
		double bins = DFT2.maxBinStep / (Math.pow(2.0, 1.0 / (double) FDData.noteBase) - 1.0);
		double[] mono = new double[(int) Math.ceil(bins * maxSamplesPerCycle * 2.0)]; // MAXWINDOWLENGTH eventually
		double[] testAmplitudes = new double[FDData.noteBase + 1];
		for(int index = 0; index < testAmplitudes.length; index++) testAmplitudes[index] = Math.random();
		for(double freqInHz = maxFreqHz ; Math.round(freqInHz * 10000.0) / 10000.0 >= minFreqHz; freqInHz *= noteRatio) {
			double currentPhase = Math.random();
			double deltaPhase = (freqInHz / SynthTools.sampleRate) * SynthTools.twoPI;
			int noteIndex = DFT2.frequencyToNote(maxFreqHz) - DFT2.frequencyToNote(freqInHz);
			if(noteIndex % 1 != 0) {
				testAmplitudes[noteIndex] = 0.0;
				continue;
			}
			for(int index = 0; index < mono.length; index++) {
				mono[index] += Math.sin(currentPhase) * testAmplitudes[noteIndex];
				currentPhase += deltaPhase;
			}
		}
		// analyse test signal
		for(double freqInHz = maxFreqHz ; Math.round(freqInHz * 10000.0) / 10000.0 >= minFreqHz; freqInHz *= noteRatio) {
			double innerNoteBase = FDData.noteBase * Math.sqrt(freqInHz / (SynthTools.sampleRate / 8.0));
			if(innerNoteBase > FDData.noteBase) innerNoteBase = FDData.noteBase;
			bins = DFT2.maxBinStep / (Math.pow(2.0, 1.0 / (double) innerNoteBase) - 1.0);
			DFT2.Wavelet currentWavelet = DFT2.createWavelet(freqInHz, bins);
			int index = DFT2.frequencyToNote(maxFreqHz) - DFT2.frequencyToNote(freqInHz);
			responseOfWaveletToNote[xDimension - 1][index] = DFT2.singleDFTTest(currentWavelet, mono);
		}
		// print system to solve
		for(double freqInHz = maxFreqHz ; Math.round(freqInHz * 10000.0) / 10000.0 >= minFreqHz; freqInHz *= noteRatio) {
			System.out.print("[");
			int yIndex = DFT2.frequencyToNote(maxFreqHz) - DFT2.frequencyToNote(freqInHz);
			for(double innerFreqInHz = maxFreqHz; Math.round(innerFreqInHz * 10000.0) / 10000.0 >= minFreqHz; innerFreqInHz *= noteRatio) {
				int xIndex = DFT2.frequencyToNote(maxFreqHz) - DFT2.frequencyToNote(innerFreqInHz);
				System.out.printf("%.3f", responseOfWaveletToNote[xIndex][yIndex]);
				System.out.print(",");
			}
			System.out.printf("%.6f] = ", responseOfWaveletToNote[xDimension - 1][yIndex]);
			System.out.printf("%.6f", testAmplitudes[yIndex]);
		}
		double[][] A = responseOfWaveletToNote;
		for(int x = 0; x < yDimension; x++) {
			double divisor = A[x][x];
			for(int x2 = x; x2 < xDimension; x2++) A[x2][x] /= divisor;
			for(int y = 0; y < yDimension; y++) {
				if(y == x) continue;
				double multiplier = A[x][y];
				for(int x2 = x; x2 < xDimension; x2++) {
					A[x2][y] -= A[x2][x] * multiplier;
				}
			}
			System.out.println(x);
		}
		responseOfWaveletToNote = A;
		for(double freqInHz = maxFreqHz ; Math.round(freqInHz * 10000.0) / 10000.0 >= minFreqHz; freqInHz *= noteRatio) {
			System.out.print("[");
			int yIndex = DFT2.frequencyToNote(maxFreqHz) - DFT2.frequencyToNote(freqInHz);
			for(double innerFreqInHz = maxFreqHz; Math.round(innerFreqInHz * 10000.0) / 10000.0 >= minFreqHz; innerFreqInHz *= noteRatio) {
				int xIndex = DFT2.frequencyToNote(maxFreqHz) - DFT2.frequencyToNote(innerFreqInHz);
				System.out.printf("%.3f", responseOfWaveletToNote[xIndex][yIndex]);
				System.out.print(",");
			}
			System.out.printf("%.10f] = ", responseOfWaveletToNote[xDimension - 1][yIndex]);
			System.out.printf("%.10f", testAmplitudes[yIndex]);
		}
		System.out.println();
		System.out.println();
	}
}
