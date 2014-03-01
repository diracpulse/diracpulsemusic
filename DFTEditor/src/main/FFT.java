package main;

public class FFT {

	static float[] runFFT(float[] data, int nn, int isign) {
        /*      Varient of Numerical Recipes code from off the internet.  It takes nn
        interleaved complex input data samples in the array data and returns nn interleaved
        complex data samples in place where the output is the FFT of input if isign==1 and it
        is nn times the IFFT of the input if isign==-1.
        (Note: it doesn't renormalize by 1/N when doing the inverse transform!!!)
        (Note: this follows physicists convention of +i, not EE of -j in forward
        transform!!!!)
         */

        /*      Press, Flannery, Teukolsky, Vettering "Numerical
         *      Recipes in C" tuned up ; Code works only when nn is
         *      a power of 2  */

        int n, mmax, m, j, i;
        double theta;
        float wtemp, wr, wpr, wpi, wi, wpin;
        float tempr, tempi, datar, datai;
        float data1r, data1i, tmp;

        n = nn * 2;

        /* bit reversal */

        j = 0;
        for (i = 0; i < n; i += 2) {
            if (j > i) {  /* could use j>i+1 to help compiler analysis */
//                              SWAP (data [j], data [i]);
                tmp = data[j];
                data[j] = data[i];
                data[i] = tmp;
//                              SWAP (data [j + 1], data [i + 1]);
                tmp = data[j + 1];
                data[j + 1] = data[i + 1];
                data[i + 1] = tmp;
            }
            m = nn;
            while ((m >= 2) && (j >= m)) {
                j -= m;
                m >>= 1;
            }
            j += m;
        }

        theta = 3.141592653589795 * 0.5;
        if (isign < 0) {
            theta = -theta;
        }
        wpin = 0;   /* sin(+-PI) */
        for (mmax = 2; n > mmax; mmax *= 2) {
            wpi = wpin;
            wpin = (float) (Math.sin(theta));
            wpr = 1 - wpin * wpin - wpin * wpin; /* cos(theta*2) */
            theta *= .5;
            wr = 1;
            wi = 0;
            for (m = 0; m < mmax; m += 2) {
                j = m + mmax;
                tempr = (float) wr * (data1r = data[j]);
                tempi = (float) wi * (data1i = data[j + 1]);
                for (i = m; i < n - mmax * 2; i += mmax * 2) {
                    /* mixed precision not significantly more
                     * accurate here; if removing float casts,
                     * tempr and tempi should be double */
                    tempr -= tempi;
                    tempi = (float) wr * data1i + (float) wi * data1r;
                    /* don't expect compiler to analyze j > i + 1 */
                    data1r = data[j + mmax * 2];
                    data1i = data[j + mmax * 2 + 1];
                    data[i] = (datar = data[i]) + tempr;
                    data[i + 1] = (datai = data[i + 1]) + tempi;
                    data[j] = datar - tempr;
                    data[j + 1] = datai - tempi;
                    tempr = (float) wr * data1r;
                    tempi = (float) wi * data1i;
                    j += mmax * 2;
                }
                tempr -= tempi;
                tempi = (float) wr * data1i + (float) wi * data1r;
                data[i] = (datar = data[i]) + tempr;
                data[i + 1] = (datai = data[i + 1]) + tempi;
                data[j] = datar - tempr;
                data[j + 1] = datai - tempi;
                wr = (wtemp = wr) * wpr - wi * wpi;
                wi = wtemp * wpi + wi * wpr;
            }
        }
        return data;
    }
}