
/*
 * This is a cilk FFTWS code. 
 * Copyright (c) 2000 Massachusetts Institute of Technology
 * Copyright (c) 2000 Matteo Frigo
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/*
 * Vivek Kumar: Ported to JavaTC work-asyncing.  
 */

class COMPLEX {
	public float re;
	public float im;
	public static void copy(COMPLEX to, COMPLEX from) {
		to.re = from.re;
		to.im = from.im;
	}
}

public class FFT {
	public static void compute_w_coefficients(int n, int a, int b, COMPLEX[] W) {
		if(b - a < 128) {
			final double twoPiOverN = 2.0D * 3.1415926535897932384626434D / n;
			for(int k = a; k <= b; ++k) {
				final float c = (float)Math.cos(twoPiOverN * k);
				W[k].re = W[n - k].re = c;
				final float s = (float)Math.sin(twoPiOverN * k);
				W[k].im = -s;
				W[n - k].im = s;
			}
		}
		else {
			final int ab = (a + b) / 2;
			finish {
				async {
					compute_w_coefficients(n, a, ab, W);
					compute_w_coefficients(n, ab + 1, b, W);
				}
			}
		}
	}
	public static int factor(int n) {
		if(n < 2) 
			return 1;
		if(n == 64 || n == 128 || n == 256 || n == 1024 || n == 2048 || n == 4096) 
			return 8;
		if((n & 15) == 0) 
			return 16;
		if((n & 7) == 0) 
			return 8;
		if((n & 3) == 0) 
			return 4;
		if((n & 1) == 0) 
			return 2;
		for(int r = 3; r < n; r += 2) 
			if(n % r == 0) 
				return r;
		return n;
	}
	public static void unshuffle(int a, int b, int startIndexInOut, COMPLEX[] in, COMPLEX[] out, int r, int m) {
		int j;
		final int r4 = r & (~0x3);
		if(b - a < 16) {
			int ip_start = startIndexInOut + a * r;
			for(int i = a; i < b; ++i) {
				int jp_start = startIndexInOut + i;
				for(j = 0; j < r4; j += 4) {
					COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
					COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
					COMPLEX.copy(out[jp_start + 2 * m], in[ip_start + 2]);
					COMPLEX.copy(out[jp_start + 3 * m], in[ip_start + 3]);
					jp_start += 4 * m;
					ip_start += 4;
				}
				for(; j < r; ++j) {
					COMPLEX.copy(out[jp_start], in[ip_start++]);
					jp_start += m;
				}
			}
		}
		else {
			final int ab = (a + b) / 2;
			finish {
				async {
					unshuffle(a, ab, startIndexInOut, in, out, r, m);
					unshuffle(ab, b, startIndexInOut, in, out, r, m);
				}
			}
		}
	}
	public static void fft_twiddle_gen1(int startIndexInOut, COMPLEX[] in, COMPLEX[] out, COMPLEX[] W, int r, int m, int nW, int nWdnti, int nWdntm) {
		for(int k = 0, kp_start = startIndexInOut; k < r; ++k, kp_start += m) {
			float r0;
			float i0;
			float rt;
			float it;
			float rw;
			float iw;
			int l1 = nWdnti + nWdntm * k;
			r0 = i0 = 0.0F;
			for(int j = 0, jp_start = startIndexInOut, l0 = 0; j < r; ++j, jp_start += m) {
				rw = W[l0].re;
				iw = W[l0].im;
				rt = in[jp_start].re;
				it = in[jp_start].im;
				r0 += rt * rw - it * iw;
				i0 += rt * iw + it * rw;
				l0 += l1;
				if(l0 > nW) 
					l0 -= nW;
			}
			out[kp_start].re = r0;
			out[kp_start].im = i0;
		}
	}
	public static void fft_twiddle_gen(int i, int i1, int startIndexInOut, COMPLEX[] in, COMPLEX[] out, COMPLEX[] W, int nW, int nWdn, int r, int m) {
		if(i == i1 - 1) {
			fft_twiddle_gen1(startIndexInOut + i, in, out, W, r, m, nW, nWdn * i, nWdn * m);
		}
		else {
			final int i2 = (i + i1) / 2;
			finish {
				async {
					fft_twiddle_gen(i, i2, startIndexInOut, in, out, W, nW, nWdn, r, m);
					fft_twiddle_gen(i2, i1, startIndexInOut, in, out, W, nW, nWdn, r, m);
				}
			}
		}
	}
	public static void fft_base_2(int startIndexInOut, COMPLEX[] in, COMPLEX[] out) {
		float r1_0;
		float i1_0;
		float r1_1;
		float i1_1;
		r1_0 = in[startIndexInOut].re;
		i1_0 = in[startIndexInOut].im;
		r1_1 = in[startIndexInOut + 1].re;
		i1_1 = in[startIndexInOut + 1].im;
		out[startIndexInOut].re = (r1_0 + r1_1);
		out[startIndexInOut].im = (i1_0 + i1_1);
		out[startIndexInOut + 1].re = (r1_0 - r1_1);
		out[startIndexInOut + 1].im = (i1_0 - i1_1);
	}
	public static void fft_twiddle_2(int a, int b, int startIndexInOut, COMPLEX[] in, COMPLEX[] out, COMPLEX[] W, int nW, int nWdn, int m) {
		if((b - a) < 128) {
			for(int i = a, l1 = nWdn * i, kp_start = startIndexInOut + i; i < b; i++, l1 += nWdn, kp_start++) {
				final int jp_start = startIndexInOut + i;
				{
					final float r1_0 = in[jp_start + 0 * m].re;
					final float i1_0 = in[jp_start + 0 * m].im;
					final float wr = W[1 * l1].re;
					final float wi = W[1 * l1].im;
					final float tmpr = in[jp_start + 1 * m].re;
					final float tmpi = in[jp_start + 1 * m].im;
					final float r1_1 = ((wr * tmpr) - (wi * tmpi));
					final float i1_1 = ((wi * tmpr) + (wr * tmpi));
					out[kp_start + 0 * m].re = (r1_0 + r1_1);
					out[kp_start + 0 * m].im = (i1_0 + i1_1);
					out[kp_start + 1 * m].re = (r1_0 - r1_1);
					out[kp_start + 1 * m].im = (i1_0 - i1_1);
				}
			}
		}
		else {
			final int ab = (a + b) / 2;
			finish {
				async {
					fft_twiddle_2(a, ab, startIndexInOut, in, out, W, nW, nWdn, m);
					fft_twiddle_2(ab, b, startIndexInOut, in, out, W, nW, nWdn, m);
				}
			}
		}
	}
	public static void fft_unshuffle_2(int a, int b, int startIndexInOut, COMPLEX[] in, COMPLEX[] out, int m) {
		if((b - a) < 128) {
			int ip_start = startIndexInOut + a * 2;
			for(int i = a; i < b; ++i) {
				final int jp_start = startIndexInOut + i;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
			}
		}
		else {
			final int ab = (a + b) / 2;
			finish {
				async {
					fft_unshuffle_2(a, ab, startIndexInOut, in, out, m);
					fft_unshuffle_2(ab, b, startIndexInOut, in, out, m);
				}
			}
		}
	}
	public static void fft_base_4(int startIndexInOut, COMPLEX[] in, COMPLEX[] out) {
		float r1_0;
		float i1_0;
		float r1_1;
		float i1_1;
		float r1_2;
		float i1_2;
		float r1_3;
		float i1_3;
		{
			float r2_0;
			float i2_0;
			float r2_2;
			float i2_2;
			r2_0 = in[startIndexInOut].re;
			i2_0 = in[startIndexInOut].im;
			r2_2 = in[startIndexInOut + 2].re;
			i2_2 = in[startIndexInOut + 2].im;
			r1_0 = (r2_0 + r2_2);
			i1_0 = (i2_0 + i2_2);
			r1_2 = (r2_0 - r2_2);
			i1_2 = (i2_0 - i2_2);
		}
		{
			float r2_1;
			float i2_1;
			float r2_3;
			float i2_3;
			r2_1 = in[startIndexInOut + 1].re;
			i2_1 = in[startIndexInOut + 1].im;
			r2_3 = in[startIndexInOut + 3].re;
			i2_3 = in[startIndexInOut + 3].im;
			r1_1 = (r2_1 + r2_3);
			i1_1 = (i2_1 + i2_3);
			r1_3 = (r2_1 - r2_3);
			i1_3 = (i2_1 - i2_3);
		}
		out[startIndexInOut].re = (r1_0 + r1_1);
		out[startIndexInOut].im = (i1_0 + i1_1);
		out[startIndexInOut + 2].re = (r1_0 - r1_1);
		out[startIndexInOut + 2].im = (i1_0 - i1_1);
		out[startIndexInOut + 1].re = (r1_2 + i1_3);
		out[startIndexInOut + 1].im = (i1_2 - r1_3);
		out[startIndexInOut + 3].re = (r1_2 - i1_3);
		out[startIndexInOut + 3].im = (i1_2 + r1_3);
	}
	public static void fft_twiddle_4(int a, int b, int startIndexInOut, COMPLEX[] in, COMPLEX[] out, COMPLEX[] W, int nW, int nWdn, int m) {
		float tmpr;
		float tmpi;
		float wr;
		float wi;
		if((b - a) < 128) {
			for(int i = a, l1 = nWdn * i, kp_start = startIndexInOut + i; i < b; i++, l1 += nWdn, kp_start++) {
				int jp_start = startIndexInOut + i;
				{
					float r1_0;
					float i1_0;
					float r1_1;
					float i1_1;
					float r1_2;
					float i1_2;
					float r1_3;
					float i1_3;
					{
						float r2_0;
						float i2_0;
						float r2_2;
						float i2_2;
						r2_0 = in[jp_start + 0 * m].re;
						i2_0 = in[jp_start + 0 * m].im;
						wr = W[2 * l1].re;
						wi = W[2 * l1].im;
						tmpr = in[jp_start + 2 * m].re;
						tmpi = in[jp_start + 2 * m].im;
						r2_2 = ((wr * tmpr) - (wi * tmpi));
						i2_2 = ((wi * tmpr) + (wr * tmpi));
						r1_0 = (r2_0 + r2_2);
						i1_0 = (i2_0 + i2_2);
						r1_2 = (r2_0 - r2_2);
						i1_2 = (i2_0 - i2_2);
					}
					{
						float r2_1;
						float i2_1;
						float r2_3;
						float i2_3;
						wr = W[1 * l1].re;
						wi = W[1 * l1].im;
						tmpr = in[jp_start + 1 * m].re;
						tmpi = in[jp_start + 1 * m].im;
						r2_1 = ((wr * tmpr) - (wi * tmpi));
						i2_1 = ((wi * tmpr) + (wr * tmpi));
						wr = W[3 * l1].re;
						wi = W[3 * l1].im;
						tmpr = in[jp_start + 3 * m].re;
						tmpi = in[jp_start + 3 * m].im;
						r2_3 = ((wr * tmpr) - (wi * tmpi));
						i2_3 = ((wi * tmpr) + (wr * tmpi));
						r1_1 = (r2_1 + r2_3);
						i1_1 = (i2_1 + i2_3);
						r1_3 = (r2_1 - r2_3);
						i1_3 = (i2_1 - i2_3);
					}
					out[kp_start + 0 * m].re = (r1_0 + r1_1);
					out[kp_start + 0 * m].im = (i1_0 + i1_1);
					out[kp_start + 2 * m].re = (r1_0 - r1_1);
					out[kp_start + 2 * m].im = (i1_0 - i1_1);
					out[kp_start + 1 * m].re = (r1_2 + i1_3);
					out[kp_start + 1 * m].im = (i1_2 - r1_3);
					out[kp_start + 3 * m].re = (r1_2 - i1_3);
					out[kp_start + 3 * m].im = (i1_2 + r1_3);
				}
			}
		}
		else {
			final int ab = (a + b) / 2;
			finish {
				async {
					fft_twiddle_4(a, ab, startIndexInOut, in, out, W, nW, nWdn, m);
					fft_twiddle_4(ab, b, startIndexInOut, in, out, W, nW, nWdn, m);
				}
			}
		}
	}
	public static void fft_unshuffle_4(int a, int b, int startIndexInOut, COMPLEX[] in, COMPLEX[] out, int m) {
		if((b - a) < 128) {
			int ip_start = startIndexInOut + a * 4;
			for(int i = a; i < b; ++i) {
				int jp_start = startIndexInOut + i;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
			}
		}
		else {
			final int ab = (a + b) / 2;
			finish {
				async {
					fft_unshuffle_4(a, ab, startIndexInOut, in, out, m);
					fft_unshuffle_4(ab, b, startIndexInOut, in, out, m);
				}
			}
		}
	}
	public static void fft_base_8(int startIndexInOut, COMPLEX[] in, COMPLEX[] out) {
		float tmpr;
		float tmpi;
		{
			float r1_0;
			float i1_0;
			float r1_1;
			float i1_1;
			float r1_2;
			float i1_2;
			float r1_3;
			float i1_3;
			float r1_4;
			float i1_4;
			float r1_5;
			float i1_5;
			float r1_6;
			float i1_6;
			float r1_7;
			float i1_7;
			{
				float r2_0;
				float i2_0;
				float r2_2;
				float i2_2;
				float r2_4;
				float i2_4;
				float r2_6;
				float i2_6;
				{
					float r3_0;
					float i3_0;
					float r3_4;
					float i3_4;
					r3_0 = in[startIndexInOut + 0].re;
					i3_0 = in[startIndexInOut + 0].im;
					r3_4 = in[startIndexInOut + 4].re;
					i3_4 = in[startIndexInOut + 4].im;
					r2_0 = (r3_0 + r3_4);
					i2_0 = (i3_0 + i3_4);
					r2_4 = (r3_0 - r3_4);
					i2_4 = (i3_0 - i3_4);
				}
				{
					float r3_2;
					float i3_2;
					float r3_6;
					float i3_6;
					r3_2 = in[startIndexInOut + 2].re;
					i3_2 = in[startIndexInOut + 2].im;
					r3_6 = in[startIndexInOut + 6].re;
					i3_6 = in[startIndexInOut + 6].im;
					r2_2 = (r3_2 + r3_6);
					i2_2 = (i3_2 + i3_6);
					r2_6 = (r3_2 - r3_6);
					i2_6 = (i3_2 - i3_6);
				}
				r1_0 = (r2_0 + r2_2);
				i1_0 = (i2_0 + i2_2);
				r1_4 = (r2_0 - r2_2);
				i1_4 = (i2_0 - i2_2);
				r1_2 = (r2_4 + i2_6);
				i1_2 = (i2_4 - r2_6);
				r1_6 = (r2_4 - i2_6);
				i1_6 = (i2_4 + r2_6);
			}
			{
				float r2_1;
				float i2_1;
				float r2_3;
				float i2_3;
				float r2_5;
				float i2_5;
				float r2_7;
				float i2_7;
				{
					float r3_1;
					float i3_1;
					float r3_5;
					float i3_5;
					r3_1 = in[startIndexInOut + 1].re;
					i3_1 = in[startIndexInOut + 1].im;
					r3_5 = in[startIndexInOut + 5].re;
					i3_5 = in[startIndexInOut + 5].im;
					r2_1 = (r3_1 + r3_5);
					i2_1 = (i3_1 + i3_5);
					r2_5 = (r3_1 - r3_5);
					i2_5 = (i3_1 - i3_5);
				}
				{
					float r3_3;
					float i3_3;
					float r3_7;
					float i3_7;
					r3_3 = in[startIndexInOut + 3].re;
					i3_3 = in[startIndexInOut + 3].im;
					r3_7 = in[startIndexInOut + 7].re;
					i3_7 = in[startIndexInOut + 7].im;
					r2_3 = (r3_3 + r3_7);
					i2_3 = (i3_3 + i3_7);
					r2_7 = (r3_3 - r3_7);
					i2_7 = (i3_3 - i3_7);
				}
				r1_1 = (r2_1 + r2_3);
				i1_1 = (i2_1 + i2_3);
				r1_5 = (r2_1 - r2_3);
				i1_5 = (i2_1 - i2_3);
				r1_3 = (r2_5 + i2_7);
				i1_3 = (i2_5 - r2_7);
				r1_7 = (r2_5 - i2_7);
				i1_7 = (i2_5 + r2_7);
			}
			out[startIndexInOut + 0].re = (r1_0 + r1_1);
			out[startIndexInOut + 0].im = (i1_0 + i1_1);
			out[startIndexInOut + 4].re = (r1_0 - r1_1);
			out[startIndexInOut + 4].im = (i1_0 - i1_1);
			tmpr = (0.707106781187F * (r1_3 + i1_3));
			tmpi = (0.707106781187F * (i1_3 - r1_3));
			out[startIndexInOut + 1].re = (r1_2 + tmpr);
			out[startIndexInOut + 1].im = (i1_2 + tmpi);
			out[startIndexInOut + 5].re = (r1_2 - tmpr);
			out[startIndexInOut + 5].im = (i1_2 - tmpi);
			out[startIndexInOut + 2].re = (r1_4 + i1_5);
			out[startIndexInOut + 2].im = (i1_4 - r1_5);
			out[startIndexInOut + 6].re = (r1_4 - i1_5);
			out[startIndexInOut + 6].im = (i1_4 + r1_5);
			tmpr = (0.707106781187F * (i1_7 - r1_7));
			tmpi = (0.707106781187F * (r1_7 + i1_7));
			out[startIndexInOut + 3].re = (r1_6 + tmpr);
			out[startIndexInOut + 3].im = (i1_6 - tmpi);
			out[startIndexInOut + 7].re = (r1_6 - tmpr);
			out[startIndexInOut + 7].im = (i1_6 + tmpi);
		}
	}
	public static void fft_twiddle_8(int a, int b, int startIndexInOut, COMPLEX[] in, COMPLEX[] out, COMPLEX[] W, int nW, int nWdn, int m) {
		float tmpr;
		float tmpi;
		float wr;
		float wi;
		if((b - a) < 128) {
			for(int i = a, l1 = nWdn * i, kp_start = startIndexInOut + i; i < b; i++, l1 += nWdn, kp_start++) {
				int jp_start = startIndexInOut + i;
				{
					float r1_0;
					float i1_0;
					float r1_1;
					float i1_1;
					float r1_2;
					float i1_2;
					float r1_3;
					float i1_3;
					float r1_4;
					float i1_4;
					float r1_5;
					float i1_5;
					float r1_6;
					float i1_6;
					float r1_7;
					float i1_7;
					{
						float r2_0;
						float i2_0;
						float r2_2;
						float i2_2;
						float r2_4;
						float i2_4;
						float r2_6;
						float i2_6;
						{
							float r3_0;
							float i3_0;
							float r3_4;
							float i3_4;
							r3_0 = in[jp_start + 0 * m].re;
							i3_0 = in[jp_start + 0 * m].im;
							wr = W[4 * l1].re;
							wi = W[4 * l1].im;
							tmpr = in[jp_start + 4 * m].re;
							tmpi = in[jp_start + 4 * m].im;
							r3_4 = ((wr * tmpr) - (wi * tmpi));
							i3_4 = ((wi * tmpr) + (wr * tmpi));
							r2_0 = (r3_0 + r3_4);
							i2_0 = (i3_0 + i3_4);
							r2_4 = (r3_0 - r3_4);
							i2_4 = (i3_0 - i3_4);
						}
						{
							float r3_2;
							float i3_2;
							float r3_6;
							float i3_6;
							wr = W[2 * l1].re;
							wi = W[2 * l1].im;
							tmpr = in[jp_start + 2 * m].re;
							tmpi = in[jp_start + 2 * m].im;
							r3_2 = ((wr * tmpr) - (wi * tmpi));
							i3_2 = ((wi * tmpr) + (wr * tmpi));
							wr = W[6 * l1].re;
							wi = W[6 * l1].im;
							tmpr = in[jp_start + 6 * m].re;
							tmpi = in[jp_start + 6 * m].im;
							r3_6 = ((wr * tmpr) - (wi * tmpi));
							i3_6 = ((wi * tmpr) + (wr * tmpi));
							r2_2 = (r3_2 + r3_6);
							i2_2 = (i3_2 + i3_6);
							r2_6 = (r3_2 - r3_6);
							i2_6 = (i3_2 - i3_6);
						}
						r1_0 = (r2_0 + r2_2);
						i1_0 = (i2_0 + i2_2);
						r1_4 = (r2_0 - r2_2);
						i1_4 = (i2_0 - i2_2);
						r1_2 = (r2_4 + i2_6);
						i1_2 = (i2_4 - r2_6);
						r1_6 = (r2_4 - i2_6);
						i1_6 = (i2_4 + r2_6);
					}
					{
						float r2_1;
						float i2_1;
						float r2_3;
						float i2_3;
						float r2_5;
						float i2_5;
						float r2_7;
						float i2_7;
						{
							float r3_1;
							float i3_1;
							float r3_5;
							float i3_5;
							wr = W[1 * l1].re;
							wi = W[1 * l1].im;
							tmpr = in[jp_start + 1 * m].re;
							tmpi = in[jp_start + 1 * m].im;
							r3_1 = ((wr * tmpr) - (wi * tmpi));
							i3_1 = ((wi * tmpr) + (wr * tmpi));
							wr = W[5 * l1].re;
							wi = W[5 * l1].im;
							tmpr = in[jp_start + 5 * m].re;
							tmpi = in[jp_start + 5 * m].im;
							r3_5 = ((wr * tmpr) - (wi * tmpi));
							i3_5 = ((wi * tmpr) + (wr * tmpi));
							r2_1 = (r3_1 + r3_5);
							i2_1 = (i3_1 + i3_5);
							r2_5 = (r3_1 - r3_5);
							i2_5 = (i3_1 - i3_5);
						}
						{
							float r3_3;
							float i3_3;
							float r3_7;
							float i3_7;
							wr = W[3 * l1].re;
							wi = W[3 * l1].im;
							tmpr = in[jp_start + 3 * m].re;
							tmpi = in[jp_start + 3 * m].im;
							r3_3 = ((wr * tmpr) - (wi * tmpi));
							i3_3 = ((wi * tmpr) + (wr * tmpi));
							wr = W[7 * l1].re;
							wi = W[7 * l1].im;
							tmpr = in[jp_start + 7 * m].re;
							tmpi = in[jp_start + 7 * m].im;
							r3_7 = ((wr * tmpr) - (wi * tmpi));
							i3_7 = ((wi * tmpr) + (wr * tmpi));
							r2_3 = (r3_3 + r3_7);
							i2_3 = (i3_3 + i3_7);
							r2_7 = (r3_3 - r3_7);
							i2_7 = (i3_3 - i3_7);
						}
						r1_1 = (r2_1 + r2_3);
						i1_1 = (i2_1 + i2_3);
						r1_5 = (r2_1 - r2_3);
						i1_5 = (i2_1 - i2_3);
						r1_3 = (r2_5 + i2_7);
						i1_3 = (i2_5 - r2_7);
						r1_7 = (r2_5 - i2_7);
						i1_7 = (i2_5 + r2_7);
					}
					out[kp_start + 0 * m].re = (r1_0 + r1_1);
					out[kp_start + 0 * m].im = (i1_0 + i1_1);
					out[kp_start + 4 * m].re = (r1_0 - r1_1);
					out[kp_start + 4 * m].im = (i1_0 - i1_1);
					tmpr = (0.707106781187F * (r1_3 + i1_3));
					tmpi = (0.707106781187F * (i1_3 - r1_3));
					out[kp_start + 1 * m].re = (r1_2 + tmpr);
					out[kp_start + 1 * m].im = (i1_2 + tmpi);
					out[kp_start + 5 * m].re = (r1_2 - tmpr);
					out[kp_start + 5 * m].im = (i1_2 - tmpi);
					out[kp_start + 2 * m].re = (r1_4 + i1_5);
					out[kp_start + 2 * m].im = (i1_4 - r1_5);
					out[kp_start + 6 * m].re = (r1_4 - i1_5);
					out[kp_start + 6 * m].im = (i1_4 + r1_5);
					tmpr = (0.707106781187F * (i1_7 - r1_7));
					tmpi = (0.707106781187F * (r1_7 + i1_7));
					out[kp_start + 3 * m].re = (r1_6 + tmpr);
					out[kp_start + 3 * m].im = (i1_6 - tmpi);
					out[kp_start + 7 * m].re = (r1_6 - tmpr);
					out[kp_start + 7 * m].im = (i1_6 + tmpi);
				}
			}
		}
		else {
			final int ab = (a + b) / 2;
			finish {
				async {
					fft_twiddle_8(a, ab, startIndexInOut, in, out, W, nW, nWdn, m);
					fft_twiddle_8(ab, b, startIndexInOut, in, out, W, nW, nWdn, m);
				}
			}
		}
	}
	public static void fft_unshuffle_8(int a, int b, int startIndexInOut, COMPLEX[] in, COMPLEX[] out, int m) {
		if((b - a) < 128) {
			int ip_start = startIndexInOut + a * 8;
			for(int i = a; i < b; ++i) {
				int jp_start = startIndexInOut + i;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
			}
		}
		else {
			final int ab = (a + b) / 2;
			finish {
				async {
					fft_unshuffle_8(a, ab, startIndexInOut, in, out, m);
					fft_unshuffle_8(ab, b, startIndexInOut, in, out, m);
				}
			}
		}
	}
	public static void fft_base_16(int startIndexInOut, COMPLEX[] in, COMPLEX[] out) {
		float tmpr;
		float tmpi;
		{
			float r1_0;
			float i1_0;
			float r1_1;
			float i1_1;
			float r1_2;
			float i1_2;
			float r1_3;
			float i1_3;
			float r1_4;
			float i1_4;
			float r1_5;
			float i1_5;
			float r1_6;
			float i1_6;
			float r1_7;
			float i1_7;
			float r1_8;
			float i1_8;
			float r1_9;
			float i1_9;
			float r1_10;
			float i1_10;
			float r1_11;
			float i1_11;
			float r1_12;
			float i1_12;
			float r1_13;
			float i1_13;
			float r1_14;
			float i1_14;
			float r1_15;
			float i1_15;
			{
				float r2_0;
				float i2_0;
				float r2_2;
				float i2_2;
				float r2_4;
				float i2_4;
				float r2_6;
				float i2_6;
				float r2_8;
				float i2_8;
				float r2_10;
				float i2_10;
				float r2_12;
				float i2_12;
				float r2_14;
				float i2_14;
				{
					float r3_0;
					float i3_0;
					float r3_4;
					float i3_4;
					float r3_8;
					float i3_8;
					float r3_12;
					float i3_12;
					{
						float r4_0;
						float i4_0;
						float r4_8;
						float i4_8;
						r4_0 = in[startIndexInOut + 0].re;
						i4_0 = in[startIndexInOut + 0].im;
						r4_8 = in[startIndexInOut + 8].re;
						i4_8 = in[startIndexInOut + 8].im;
						r3_0 = (r4_0 + r4_8);
						i3_0 = (i4_0 + i4_8);
						r3_8 = (r4_0 - r4_8);
						i3_8 = (i4_0 - i4_8);
					}
					{
						float r4_4;
						float i4_4;
						float r4_12;
						float i4_12;
						r4_4 = in[startIndexInOut + 4].re;
						i4_4 = in[startIndexInOut + 4].im;
						r4_12 = in[startIndexInOut + 12].re;
						i4_12 = in[startIndexInOut + 12].im;
						r3_4 = (r4_4 + r4_12);
						i3_4 = (i4_4 + i4_12);
						r3_12 = (r4_4 - r4_12);
						i3_12 = (i4_4 - i4_12);
					}
					r2_0 = (r3_0 + r3_4);
					i2_0 = (i3_0 + i3_4);
					r2_8 = (r3_0 - r3_4);
					i2_8 = (i3_0 - i3_4);
					r2_4 = (r3_8 + i3_12);
					i2_4 = (i3_8 - r3_12);
					r2_12 = (r3_8 - i3_12);
					i2_12 = (i3_8 + r3_12);
				}
				{
					float r3_2;
					float i3_2;
					float r3_6;
					float i3_6;
					float r3_10;
					float i3_10;
					float r3_14;
					float i3_14;
					{
						float r4_2;
						float i4_2;
						float r4_10;
						float i4_10;
						r4_2 = in[startIndexInOut + 2].re;
						i4_2 = in[startIndexInOut + 2].im;
						r4_10 = in[startIndexInOut + 10].re;
						i4_10 = in[startIndexInOut + 10].im;
						r3_2 = (r4_2 + r4_10);
						i3_2 = (i4_2 + i4_10);
						r3_10 = (r4_2 - r4_10);
						i3_10 = (i4_2 - i4_10);
					}
					{
						float r4_6;
						float i4_6;
						float r4_14;
						float i4_14;
						r4_6 = in[startIndexInOut + 6].re;
						i4_6 = in[startIndexInOut + 6].im;
						r4_14 = in[startIndexInOut + 14].re;
						i4_14 = in[startIndexInOut + 14].im;
						r3_6 = (r4_6 + r4_14);
						i3_6 = (i4_6 + i4_14);
						r3_14 = (r4_6 - r4_14);
						i3_14 = (i4_6 - i4_14);
					}
					r2_2 = (r3_2 + r3_6);
					i2_2 = (i3_2 + i3_6);
					r2_10 = (r3_2 - r3_6);
					i2_10 = (i3_2 - i3_6);
					r2_6 = (r3_10 + i3_14);
					i2_6 = (i3_10 - r3_14);
					r2_14 = (r3_10 - i3_14);
					i2_14 = (i3_10 + r3_14);
				}
				r1_0 = (r2_0 + r2_2);
				i1_0 = (i2_0 + i2_2);
				r1_8 = (r2_0 - r2_2);
				i1_8 = (i2_0 - i2_2);
				tmpr = (0.707106781187F * (r2_6 + i2_6));
				tmpi = (0.707106781187F * (i2_6 - r2_6));
				r1_2 = (r2_4 + tmpr);
				i1_2 = (i2_4 + tmpi);
				r1_10 = (r2_4 - tmpr);
				i1_10 = (i2_4 - tmpi);
				r1_4 = (r2_8 + i2_10);
				i1_4 = (i2_8 - r2_10);
				r1_12 = (r2_8 - i2_10);
				i1_12 = (i2_8 + r2_10);
				tmpr = (0.707106781187F * (i2_14 - r2_14));
				tmpi = (0.707106781187F * (r2_14 + i2_14));
				r1_6 = (r2_12 + tmpr);
				i1_6 = (i2_12 - tmpi);
				r1_14 = (r2_12 - tmpr);
				i1_14 = (i2_12 + tmpi);
			}
			{
				float r2_1;
				float i2_1;
				float r2_3;
				float i2_3;
				float r2_5;
				float i2_5;
				float r2_7;
				float i2_7;
				float r2_9;
				float i2_9;
				float r2_11;
				float i2_11;
				float r2_13;
				float i2_13;
				float r2_15;
				float i2_15;
				{
					float r3_1;
					float i3_1;
					float r3_5;
					float i3_5;
					float r3_9;
					float i3_9;
					float r3_13;
					float i3_13;
					{
						float r4_1;
						float i4_1;
						float r4_9;
						float i4_9;
						r4_1 = in[startIndexInOut + 1].re;
						i4_1 = in[startIndexInOut + 1].im;
						r4_9 = in[startIndexInOut + 9].re;
						i4_9 = in[startIndexInOut + 9].im;
						r3_1 = (r4_1 + r4_9);
						i3_1 = (i4_1 + i4_9);
						r3_9 = (r4_1 - r4_9);
						i3_9 = (i4_1 - i4_9);
					}
					{
						float r4_5;
						float i4_5;
						float r4_13;
						float i4_13;
						r4_5 = in[startIndexInOut + 5].re;
						i4_5 = in[startIndexInOut + 5].im;
						r4_13 = in[startIndexInOut + 13].re;
						i4_13 = in[startIndexInOut + 13].im;
						r3_5 = (r4_5 + r4_13);
						i3_5 = (i4_5 + i4_13);
						r3_13 = (r4_5 - r4_13);
						i3_13 = (i4_5 - i4_13);
					}
					r2_1 = (r3_1 + r3_5);
					i2_1 = (i3_1 + i3_5);
					r2_9 = (r3_1 - r3_5);
					i2_9 = (i3_1 - i3_5);
					r2_5 = (r3_9 + i3_13);
					i2_5 = (i3_9 - r3_13);
					r2_13 = (r3_9 - i3_13);
					i2_13 = (i3_9 + r3_13);
				}
				{
					float r3_3;
					float i3_3;
					float r3_7;
					float i3_7;
					float r3_11;
					float i3_11;
					float r3_15;
					float i3_15;
					{
						float r4_3;
						float i4_3;
						float r4_11;
						float i4_11;
						r4_3 = in[startIndexInOut + 3].re;
						i4_3 = in[startIndexInOut + 3].im;
						r4_11 = in[startIndexInOut + 11].re;
						i4_11 = in[startIndexInOut + 11].im;
						r3_3 = (r4_3 + r4_11);
						i3_3 = (i4_3 + i4_11);
						r3_11 = (r4_3 - r4_11);
						i3_11 = (i4_3 - i4_11);
					}
					{
						float r4_7;
						float i4_7;
						float r4_15;
						float i4_15;
						r4_7 = in[startIndexInOut + 7].re;
						i4_7 = in[startIndexInOut + 7].im;
						r4_15 = in[startIndexInOut + 15].re;
						i4_15 = in[startIndexInOut + 15].im;
						r3_7 = (r4_7 + r4_15);
						i3_7 = (i4_7 + i4_15);
						r3_15 = (r4_7 - r4_15);
						i3_15 = (i4_7 - i4_15);
					}
					r2_3 = (r3_3 + r3_7);
					i2_3 = (i3_3 + i3_7);
					r2_11 = (r3_3 - r3_7);
					i2_11 = (i3_3 - i3_7);
					r2_7 = (r3_11 + i3_15);
					i2_7 = (i3_11 - r3_15);
					r2_15 = (r3_11 - i3_15);
					i2_15 = (i3_11 + r3_15);
				}
				r1_1 = (r2_1 + r2_3);
				i1_1 = (i2_1 + i2_3);
				r1_9 = (r2_1 - r2_3);
				i1_9 = (i2_1 - i2_3);
				tmpr = (0.707106781187F * (r2_7 + i2_7));
				tmpi = (0.707106781187F * (i2_7 - r2_7));
				r1_3 = (r2_5 + tmpr);
				i1_3 = (i2_5 + tmpi);
				r1_11 = (r2_5 - tmpr);
				i1_11 = (i2_5 - tmpi);
				r1_5 = (r2_9 + i2_11);
				i1_5 = (i2_9 - r2_11);
				r1_13 = (r2_9 - i2_11);
				i1_13 = (i2_9 + r2_11);
				tmpr = (0.707106781187F * (i2_15 - r2_15));
				tmpi = (0.707106781187F * (r2_15 + i2_15));
				r1_7 = (r2_13 + tmpr);
				i1_7 = (i2_13 - tmpi);
				r1_15 = (r2_13 - tmpr);
				i1_15 = (i2_13 + tmpi);
			}
			out[startIndexInOut + 0].re = (r1_0 + r1_1);
			out[startIndexInOut + 0].im = (i1_0 + i1_1);
			out[startIndexInOut + 8].re = (r1_0 - r1_1);
			out[startIndexInOut + 8].im = (i1_0 - i1_1);
			tmpr = ((0.923879532511F * r1_3) + (0.382683432365F * i1_3));
			tmpi = ((0.923879532511F * i1_3) - (0.382683432365F * r1_3));
			out[startIndexInOut + 1].re = (r1_2 + tmpr);
			out[startIndexInOut + 1].im = (i1_2 + tmpi);
			out[startIndexInOut + 9].re = (r1_2 - tmpr);
			out[startIndexInOut + 9].im = (i1_2 - tmpi);
			tmpr = (0.707106781187F * (r1_5 + i1_5));
			tmpi = (0.707106781187F * (i1_5 - r1_5));
			out[startIndexInOut + 2].re = (r1_4 + tmpr);
			out[startIndexInOut + 2].im = (i1_4 + tmpi);
			out[startIndexInOut + 10].re = (r1_4 - tmpr);
			out[startIndexInOut + 10].im = (i1_4 - tmpi);
			tmpr = ((0.382683432365F * r1_7) + (0.923879532511F * i1_7));
			tmpi = ((0.382683432365F * i1_7) - (0.923879532511F * r1_7));
			out[startIndexInOut + 3].re = (r1_6 + tmpr);
			out[startIndexInOut + 3].im = (i1_6 + tmpi);
			out[startIndexInOut + 11].re = (r1_6 - tmpr);
			out[startIndexInOut + 11].im = (i1_6 - tmpi);
			out[startIndexInOut + 4].re = (r1_8 + i1_9);
			out[startIndexInOut + 4].im = (i1_8 - r1_9);
			out[startIndexInOut + 12].re = (r1_8 - i1_9);
			out[startIndexInOut + 12].im = (i1_8 + r1_9);
			tmpr = ((0.923879532511F * i1_11) - (0.382683432365F * r1_11));
			tmpi = ((0.923879532511F * r1_11) + (0.382683432365F * i1_11));
			out[startIndexInOut + 5].re = (r1_10 + tmpr);
			out[startIndexInOut + 5].im = (i1_10 - tmpi);
			out[startIndexInOut + 13].re = (r1_10 - tmpr);
			out[startIndexInOut + 13].im = (i1_10 + tmpi);
			tmpr = (0.707106781187F * (i1_13 - r1_13));
			tmpi = (0.707106781187F * (r1_13 + i1_13));
			out[startIndexInOut + 6].re = (r1_12 + tmpr);
			out[startIndexInOut + 6].im = (i1_12 - tmpi);
			out[startIndexInOut + 14].re = (r1_12 - tmpr);
			out[startIndexInOut + 14].im = (i1_12 + tmpi);
			tmpr = ((0.382683432365F * i1_15) - (0.923879532511F * r1_15));
			tmpi = ((0.382683432365F * r1_15) + (0.923879532511F * i1_15));
			out[startIndexInOut + 7].re = (r1_14 + tmpr);
			out[startIndexInOut + 7].im = (i1_14 - tmpi);
			out[startIndexInOut + 15].re = (r1_14 - tmpr);
			out[startIndexInOut + 15].im = (i1_14 + tmpi);
		}
	}
	public static void fft_twiddle_16(int a, int b, int startIndexInOut, COMPLEX[] in, COMPLEX[] out, COMPLEX[] W, int nW, int nWdn, int m) {
		float tmpr;
		float tmpi;
		float wr;
		float wi;
		if((b - a) < 128) {
			for(int i = a, l1 = nWdn * i, kp_start = startIndexInOut + i; i < b; i++, l1 += nWdn, kp_start++) {
				int jp_start = startIndexInOut + i;
				{
					float r1_0;
					float i1_0;
					float r1_1;
					float i1_1;
					float r1_2;
					float i1_2;
					float r1_3;
					float i1_3;
					float r1_4;
					float i1_4;
					float r1_5;
					float i1_5;
					float r1_6;
					float i1_6;
					float r1_7;
					float i1_7;
					float r1_8;
					float i1_8;
					float r1_9;
					float i1_9;
					float r1_10;
					float i1_10;
					float r1_11;
					float i1_11;
					float r1_12;
					float i1_12;
					float r1_13;
					float i1_13;
					float r1_14;
					float i1_14;
					float r1_15;
					float i1_15;
					{
						float r2_0;
						float i2_0;
						float r2_2;
						float i2_2;
						float r2_4;
						float i2_4;
						float r2_6;
						float i2_6;
						float r2_8;
						float i2_8;
						float r2_10;
						float i2_10;
						float r2_12;
						float i2_12;
						float r2_14;
						float i2_14;
						{
							float r3_0;
							float i3_0;
							float r3_4;
							float i3_4;
							float r3_8;
							float i3_8;
							float r3_12;
							float i3_12;
							{
								float r4_0;
								float i4_0;
								float r4_8;
								float i4_8;
								r4_0 = in[jp_start + 0 * m].re;
								i4_0 = in[jp_start + 0 * m].im;
								wr = W[8 * l1].re;
								wi = W[8 * l1].im;
								tmpr = in[jp_start + 8 * m].re;
								tmpi = in[jp_start + 8 * m].im;
								r4_8 = ((wr * tmpr) - (wi * tmpi));
								i4_8 = ((wi * tmpr) + (wr * tmpi));
								r3_0 = (r4_0 + r4_8);
								i3_0 = (i4_0 + i4_8);
								r3_8 = (r4_0 - r4_8);
								i3_8 = (i4_0 - i4_8);
							}
							{
								float r4_4;
								float i4_4;
								float r4_12;
								float i4_12;
								wr = W[4 * l1].re;
								wi = W[4 * l1].im;
								tmpr = in[jp_start + 4 * m].re;
								tmpi = in[jp_start + 4 * m].im;
								r4_4 = ((wr * tmpr) - (wi * tmpi));
								i4_4 = ((wi * tmpr) + (wr * tmpi));
								wr = W[12 * l1].re;
								wi = W[12 * l1].im;
								tmpr = in[jp_start + 12 * m].re;
								tmpi = in[jp_start + 12 * m].im;
								r4_12 = ((wr * tmpr) - (wi * tmpi));
								i4_12 = ((wi * tmpr) + (wr * tmpi));
								r3_4 = (r4_4 + r4_12);
								i3_4 = (i4_4 + i4_12);
								r3_12 = (r4_4 - r4_12);
								i3_12 = (i4_4 - i4_12);
							}
							r2_0 = (r3_0 + r3_4);
							i2_0 = (i3_0 + i3_4);
							r2_8 = (r3_0 - r3_4);
							i2_8 = (i3_0 - i3_4);
							r2_4 = (r3_8 + i3_12);
							i2_4 = (i3_8 - r3_12);
							r2_12 = (r3_8 - i3_12);
							i2_12 = (i3_8 + r3_12);
						}
						{
							float r3_2;
							float i3_2;
							float r3_6;
							float i3_6;
							float r3_10;
							float i3_10;
							float r3_14;
							float i3_14;
							{
								float r4_2;
								float i4_2;
								float r4_10;
								float i4_10;
								wr = W[2 * l1].re;
								wi = W[2 * l1].im;
								tmpr = in[jp_start + 2 * m].re;
								tmpi = in[jp_start + 2 * m].im;
								r4_2 = ((wr * tmpr) - (wi * tmpi));
								i4_2 = ((wi * tmpr) + (wr * tmpi));
								wr = W[10 * l1].re;
								wi = W[10 * l1].im;
								tmpr = in[jp_start + 10 * m].re;
								tmpi = in[jp_start + 10 * m].im;
								r4_10 = ((wr * tmpr) - (wi * tmpi));
								i4_10 = ((wi * tmpr) + (wr * tmpi));
								r3_2 = (r4_2 + r4_10);
								i3_2 = (i4_2 + i4_10);
								r3_10 = (r4_2 - r4_10);
								i3_10 = (i4_2 - i4_10);
							}
							{
								float r4_6;
								float i4_6;
								float r4_14;
								float i4_14;
								wr = W[6 * l1].re;
								wi = W[6 * l1].im;
								tmpr = in[jp_start + 6 * m].re;
								tmpi = in[jp_start + 6 * m].im;
								r4_6 = ((wr * tmpr) - (wi * tmpi));
								i4_6 = ((wi * tmpr) + (wr * tmpi));
								wr = W[14 * l1].re;
								wi = W[14 * l1].im;
								tmpr = in[jp_start + 14 * m].re;
								tmpi = in[jp_start + 14 * m].im;
								r4_14 = ((wr * tmpr) - (wi * tmpi));
								i4_14 = ((wi * tmpr) + (wr * tmpi));
								r3_6 = (r4_6 + r4_14);
								i3_6 = (i4_6 + i4_14);
								r3_14 = (r4_6 - r4_14);
								i3_14 = (i4_6 - i4_14);
							}
							r2_2 = (r3_2 + r3_6);
							i2_2 = (i3_2 + i3_6);
							r2_10 = (r3_2 - r3_6);
							i2_10 = (i3_2 - i3_6);
							r2_6 = (r3_10 + i3_14);
							i2_6 = (i3_10 - r3_14);
							r2_14 = (r3_10 - i3_14);
							i2_14 = (i3_10 + r3_14);
						}
						r1_0 = (r2_0 + r2_2);
						i1_0 = (i2_0 + i2_2);
						r1_8 = (r2_0 - r2_2);
						i1_8 = (i2_0 - i2_2);
						tmpr = (0.707106781187F * (r2_6 + i2_6));
						tmpi = (0.707106781187F * (i2_6 - r2_6));
						r1_2 = (r2_4 + tmpr);
						i1_2 = (i2_4 + tmpi);
						r1_10 = (r2_4 - tmpr);
						i1_10 = (i2_4 - tmpi);
						r1_4 = (r2_8 + i2_10);
						i1_4 = (i2_8 - r2_10);
						r1_12 = (r2_8 - i2_10);
						i1_12 = (i2_8 + r2_10);
						tmpr = (0.707106781187F * (i2_14 - r2_14));
						tmpi = (0.707106781187F * (r2_14 + i2_14));
						r1_6 = (r2_12 + tmpr);
						i1_6 = (i2_12 - tmpi);
						r1_14 = (r2_12 - tmpr);
						i1_14 = (i2_12 + tmpi);
					}
					{
						float r2_1;
						float i2_1;
						float r2_3;
						float i2_3;
						float r2_5;
						float i2_5;
						float r2_7;
						float i2_7;
						float r2_9;
						float i2_9;
						float r2_11;
						float i2_11;
						float r2_13;
						float i2_13;
						float r2_15;
						float i2_15;
						{
							float r3_1;
							float i3_1;
							float r3_5;
							float i3_5;
							float r3_9;
							float i3_9;
							float r3_13;
							float i3_13;
							{
								float r4_1;
								float i4_1;
								float r4_9;
								float i4_9;
								wr = W[1 * l1].re;
								wi = W[1 * l1].im;
								tmpr = in[jp_start + 1 * m].re;
								tmpi = in[jp_start + 1 * m].im;
								r4_1 = ((wr * tmpr) - (wi * tmpi));
								i4_1 = ((wi * tmpr) + (wr * tmpi));
								wr = W[9 * l1].re;
								wi = W[9 * l1].im;
								tmpr = in[jp_start + 9 * m].re;
								tmpi = in[jp_start + 9 * m].im;
								r4_9 = ((wr * tmpr) - (wi * tmpi));
								i4_9 = ((wi * tmpr) + (wr * tmpi));
								r3_1 = (r4_1 + r4_9);
								i3_1 = (i4_1 + i4_9);
								r3_9 = (r4_1 - r4_9);
								i3_9 = (i4_1 - i4_9);
							}
							{
								float r4_5;
								float i4_5;
								float r4_13;
								float i4_13;
								wr = W[5 * l1].re;
								wi = W[5 * l1].im;
								tmpr = in[jp_start + 5 * m].re;
								tmpi = in[jp_start + 5 * m].im;
								r4_5 = ((wr * tmpr) - (wi * tmpi));
								i4_5 = ((wi * tmpr) + (wr * tmpi));
								wr = W[13 * l1].re;
								wi = W[13 * l1].im;
								tmpr = in[jp_start + 13 * m].re;
								tmpi = in[jp_start + 13 * m].im;
								r4_13 = ((wr * tmpr) - (wi * tmpi));
								i4_13 = ((wi * tmpr) + (wr * tmpi));
								r3_5 = (r4_5 + r4_13);
								i3_5 = (i4_5 + i4_13);
								r3_13 = (r4_5 - r4_13);
								i3_13 = (i4_5 - i4_13);
							}
							r2_1 = (r3_1 + r3_5);
							i2_1 = (i3_1 + i3_5);
							r2_9 = (r3_1 - r3_5);
							i2_9 = (i3_1 - i3_5);
							r2_5 = (r3_9 + i3_13);
							i2_5 = (i3_9 - r3_13);
							r2_13 = (r3_9 - i3_13);
							i2_13 = (i3_9 + r3_13);
						}
						{
							float r3_3;
							float i3_3;
							float r3_7;
							float i3_7;
							float r3_11;
							float i3_11;
							float r3_15;
							float i3_15;
							{
								float r4_3;
								float i4_3;
								float r4_11;
								float i4_11;
								wr = W[3 * l1].re;
								wi = W[3 * l1].im;
								tmpr = in[jp_start + 3 * m].re;
								tmpi = in[jp_start + 3 * m].im;
								r4_3 = ((wr * tmpr) - (wi * tmpi));
								i4_3 = ((wi * tmpr) + (wr * tmpi));
								wr = W[11 * l1].re;
								wi = W[11 * l1].im;
								tmpr = in[jp_start + 11 * m].re;
								tmpi = in[jp_start + 11 * m].im;
								r4_11 = ((wr * tmpr) - (wi * tmpi));
								i4_11 = ((wi * tmpr) + (wr * tmpi));
								r3_3 = (r4_3 + r4_11);
								i3_3 = (i4_3 + i4_11);
								r3_11 = (r4_3 - r4_11);
								i3_11 = (i4_3 - i4_11);
							}
							{
								float r4_7;
								float i4_7;
								float r4_15;
								float i4_15;
								wr = W[7 * l1].re;
								wi = W[7 * l1].im;
								tmpr = in[jp_start + 7 * m].re;
								tmpi = in[jp_start + 7 * m].im;
								r4_7 = ((wr * tmpr) - (wi * tmpi));
								i4_7 = ((wi * tmpr) + (wr * tmpi));
								wr = W[15 * l1].re;
								wi = W[15 * l1].im;
								tmpr = in[jp_start + 15 * m].re;
								tmpi = in[jp_start + 15 * m].im;
								r4_15 = ((wr * tmpr) - (wi * tmpi));
								i4_15 = ((wi * tmpr) + (wr * tmpi));
								r3_7 = (r4_7 + r4_15);
								i3_7 = (i4_7 + i4_15);
								r3_15 = (r4_7 - r4_15);
								i3_15 = (i4_7 - i4_15);
							}
							r2_3 = (r3_3 + r3_7);
							i2_3 = (i3_3 + i3_7);
							r2_11 = (r3_3 - r3_7);
							i2_11 = (i3_3 - i3_7);
							r2_7 = (r3_11 + i3_15);
							i2_7 = (i3_11 - r3_15);
							r2_15 = (r3_11 - i3_15);
							i2_15 = (i3_11 + r3_15);
						}
						r1_1 = (r2_1 + r2_3);
						i1_1 = (i2_1 + i2_3);
						r1_9 = (r2_1 - r2_3);
						i1_9 = (i2_1 - i2_3);
						tmpr = (0.707106781187F * (r2_7 + i2_7));
						tmpi = (0.707106781187F * (i2_7 - r2_7));
						r1_3 = (r2_5 + tmpr);
						i1_3 = (i2_5 + tmpi);
						r1_11 = (r2_5 - tmpr);
						i1_11 = (i2_5 - tmpi);
						r1_5 = (r2_9 + i2_11);
						i1_5 = (i2_9 - r2_11);
						r1_13 = (r2_9 - i2_11);
						i1_13 = (i2_9 + r2_11);
						tmpr = (0.707106781187F * (i2_15 - r2_15));
						tmpi = (0.707106781187F * (r2_15 + i2_15));
						r1_7 = (r2_13 + tmpr);
						i1_7 = (i2_13 - tmpi);
						r1_15 = (r2_13 - tmpr);
						i1_15 = (i2_13 + tmpi);
					}
					out[kp_start + 0 * m].re = (r1_0 + r1_1);
					out[kp_start + 0 * m].im = (i1_0 + i1_1);
					out[kp_start + 8 * m].re = (r1_0 - r1_1);
					out[kp_start + 8 * m].im = (i1_0 - i1_1);
					tmpr = ((0.923879532511F * r1_3) + (0.382683432365F * i1_3));
					tmpi = ((0.923879532511F * i1_3) - (0.382683432365F * r1_3));
					out[kp_start + 1 * m].re = (r1_2 + tmpr);
					out[kp_start + 1 * m].im = (i1_2 + tmpi);
					out[kp_start + 9 * m].re = (r1_2 - tmpr);
					out[kp_start + 9 * m].im = (i1_2 - tmpi);
					tmpr = (0.707106781187F * (r1_5 + i1_5));
					tmpi = (0.707106781187F * (i1_5 - r1_5));
					out[kp_start + 2 * m].re = (r1_4 + tmpr);
					out[kp_start + 2 * m].im = (i1_4 + tmpi);
					out[kp_start + 10 * m].re = (r1_4 - tmpr);
					out[kp_start + 10 * m].im = (i1_4 - tmpi);
					tmpr = ((0.382683432365F * r1_7) + (0.923879532511F * i1_7));
					tmpi = ((0.382683432365F * i1_7) - (0.923879532511F * r1_7));
					out[kp_start + 3 * m].re = (r1_6 + tmpr);
					out[kp_start + 3 * m].im = (i1_6 + tmpi);
					out[kp_start + 11 * m].re = (r1_6 - tmpr);
					out[kp_start + 11 * m].im = (i1_6 - tmpi);
					out[kp_start + 4 * m].re = (r1_8 + i1_9);
					out[kp_start + 4 * m].im = (i1_8 - r1_9);
					out[kp_start + 12 * m].re = (r1_8 - i1_9);
					out[kp_start + 12 * m].im = (i1_8 + r1_9);
					tmpr = ((0.923879532511F * i1_11) - (0.382683432365F * r1_11));
					tmpi = ((0.923879532511F * r1_11) + (0.382683432365F * i1_11));
					out[kp_start + 5 * m].re = (r1_10 + tmpr);
					out[kp_start + 5 * m].im = (i1_10 - tmpi);
					out[kp_start + 13 * m].re = (r1_10 - tmpr);
					out[kp_start + 13 * m].im = (i1_10 + tmpi);
					tmpr = (0.707106781187F * (i1_13 - r1_13));
					tmpi = (0.707106781187F * (r1_13 + i1_13));
					out[kp_start + 6 * m].re = (r1_12 + tmpr);
					out[kp_start + 6 * m].im = (i1_12 - tmpi);
					out[kp_start + 14 * m].re = (r1_12 - tmpr);
					out[kp_start + 14 * m].im = (i1_12 + tmpi);
					tmpr = ((0.382683432365F * i1_15) - (0.923879532511F * r1_15));
					tmpi = ((0.382683432365F * r1_15) + (0.923879532511F * i1_15));
					out[kp_start + 7 * m].re = (r1_14 + tmpr);
					out[kp_start + 7 * m].im = (i1_14 - tmpi);
					out[kp_start + 15 * m].re = (r1_14 - tmpr);
					out[kp_start + 15 * m].im = (i1_14 + tmpi);
				}
			}
		}
		else {
			final int ab = (a + b) / 2;
			finish {
				async {
					fft_twiddle_16(a, ab, startIndexInOut, in, out, W, nW, nWdn, m);
					fft_twiddle_16(ab, b, startIndexInOut, in, out, W, nW, nWdn, m);
				}
			}
		}
	}
	public static void fft_unshuffle_16(int a, int b, int startIndexInOut, COMPLEX[] in, COMPLEX[] out, int m) {
		if((b - a) < 128) {
			int ip_start = startIndexInOut + a * 16;
			for(int i = a; i < b; ++i) {
				int jp_start = startIndexInOut + i;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
			}
		}
		else {
			final int ab = (a + b) / 2;
			finish {
				async {
					fft_unshuffle_16(a, ab, startIndexInOut, in, out, m);
					fft_unshuffle_16(ab, b, startIndexInOut, in, out, m);
				}
			}
		}
	}
	public static void fft_base_32(int startIndexInOut, COMPLEX[] in, COMPLEX[] out) {
		float tmpr;
		float tmpi;
		{
			float r1_0;
			float i1_0;
			float r1_1;
			float i1_1;
			float r1_2;
			float i1_2;
			float r1_3;
			float i1_3;
			float r1_4;
			float i1_4;
			float r1_5;
			float i1_5;
			float r1_6;
			float i1_6;
			float r1_7;
			float i1_7;
			float r1_8;
			float i1_8;
			float r1_9;
			float i1_9;
			float r1_10;
			float i1_10;
			float r1_11;
			float i1_11;
			float r1_12;
			float i1_12;
			float r1_13;
			float i1_13;
			float r1_14;
			float i1_14;
			float r1_15;
			float i1_15;
			float r1_16;
			float i1_16;
			float r1_17;
			float i1_17;
			float r1_18;
			float i1_18;
			float r1_19;
			float i1_19;
			float r1_20;
			float i1_20;
			float r1_21;
			float i1_21;
			float r1_22;
			float i1_22;
			float r1_23;
			float i1_23;
			float r1_24;
			float i1_24;
			float r1_25;
			float i1_25;
			float r1_26;
			float i1_26;
			float r1_27;
			float i1_27;
			float r1_28;
			float i1_28;
			float r1_29;
			float i1_29;
			float r1_30;
			float i1_30;
			float r1_31;
			float i1_31;
			{
				float r2_0;
				float i2_0;
				float r2_2;
				float i2_2;
				float r2_4;
				float i2_4;
				float r2_6;
				float i2_6;
				float r2_8;
				float i2_8;
				float r2_10;
				float i2_10;
				float r2_12;
				float i2_12;
				float r2_14;
				float i2_14;
				float r2_16;
				float i2_16;
				float r2_18;
				float i2_18;
				float r2_20;
				float i2_20;
				float r2_22;
				float i2_22;
				float r2_24;
				float i2_24;
				float r2_26;
				float i2_26;
				float r2_28;
				float i2_28;
				float r2_30;
				float i2_30;
				{
					float r3_0;
					float i3_0;
					float r3_4;
					float i3_4;
					float r3_8;
					float i3_8;
					float r3_12;
					float i3_12;
					float r3_16;
					float i3_16;
					float r3_20;
					float i3_20;
					float r3_24;
					float i3_24;
					float r3_28;
					float i3_28;
					{
						float r4_0;
						float i4_0;
						float r4_8;
						float i4_8;
						float r4_16;
						float i4_16;
						float r4_24;
						float i4_24;
						{
							float r5_0;
							float i5_0;
							float r5_16;
							float i5_16;
							r5_0 = in[startIndexInOut + 0].re;
							i5_0 = in[startIndexInOut + 0].im;
							r5_16 = in[startIndexInOut + 16].re;
							i5_16 = in[startIndexInOut + 16].im;
							r4_0 = (r5_0 + r5_16);
							i4_0 = (i5_0 + i5_16);
							r4_16 = (r5_0 - r5_16);
							i4_16 = (i5_0 - i5_16);
						}
						{
							float r5_8;
							float i5_8;
							float r5_24;
							float i5_24;
							r5_8 = in[startIndexInOut + 8].re;
							i5_8 = in[startIndexInOut + 8].im;
							r5_24 = in[startIndexInOut + 24].re;
							i5_24 = in[startIndexInOut + 24].im;
							r4_8 = (r5_8 + r5_24);
							i4_8 = (i5_8 + i5_24);
							r4_24 = (r5_8 - r5_24);
							i4_24 = (i5_8 - i5_24);
						}
						r3_0 = (r4_0 + r4_8);
						i3_0 = (i4_0 + i4_8);
						r3_16 = (r4_0 - r4_8);
						i3_16 = (i4_0 - i4_8);
						r3_8 = (r4_16 + i4_24);
						i3_8 = (i4_16 - r4_24);
						r3_24 = (r4_16 - i4_24);
						i3_24 = (i4_16 + r4_24);
					}
					{
						float r4_4;
						float i4_4;
						float r4_12;
						float i4_12;
						float r4_20;
						float i4_20;
						float r4_28;
						float i4_28;
						{
							float r5_4;
							float i5_4;
							float r5_20;
							float i5_20;
							r5_4 = in[startIndexInOut + 4].re;
							i5_4 = in[startIndexInOut + 4].im;
							r5_20 = in[startIndexInOut + 20].re;
							i5_20 = in[startIndexInOut + 20].im;
							r4_4 = (r5_4 + r5_20);
							i4_4 = (i5_4 + i5_20);
							r4_20 = (r5_4 - r5_20);
							i4_20 = (i5_4 - i5_20);
						}
						{
							float r5_12;
							float i5_12;
							float r5_28;
							float i5_28;
							r5_12 = in[startIndexInOut + 12].re;
							i5_12 = in[startIndexInOut + 12].im;
							r5_28 = in[startIndexInOut + 28].re;
							i5_28 = in[startIndexInOut + 28].im;
							r4_12 = (r5_12 + r5_28);
							i4_12 = (i5_12 + i5_28);
							r4_28 = (r5_12 - r5_28);
							i4_28 = (i5_12 - i5_28);
						}
						r3_4 = (r4_4 + r4_12);
						i3_4 = (i4_4 + i4_12);
						r3_20 = (r4_4 - r4_12);
						i3_20 = (i4_4 - i4_12);
						r3_12 = (r4_20 + i4_28);
						i3_12 = (i4_20 - r4_28);
						r3_28 = (r4_20 - i4_28);
						i3_28 = (i4_20 + r4_28);
					}
					r2_0 = (r3_0 + r3_4);
					i2_0 = (i3_0 + i3_4);
					r2_16 = (r3_0 - r3_4);
					i2_16 = (i3_0 - i3_4);
					tmpr = (0.707106781187F * (r3_12 + i3_12));
					tmpi = (0.707106781187F * (i3_12 - r3_12));
					r2_4 = (r3_8 + tmpr);
					i2_4 = (i3_8 + tmpi);
					r2_20 = (r3_8 - tmpr);
					i2_20 = (i3_8 - tmpi);
					r2_8 = (r3_16 + i3_20);
					i2_8 = (i3_16 - r3_20);
					r2_24 = (r3_16 - i3_20);
					i2_24 = (i3_16 + r3_20);
					tmpr = (0.707106781187F * (i3_28 - r3_28));
					tmpi = (0.707106781187F * (r3_28 + i3_28));
					r2_12 = (r3_24 + tmpr);
					i2_12 = (i3_24 - tmpi);
					r2_28 = (r3_24 - tmpr);
					i2_28 = (i3_24 + tmpi);
				}
				{
					float r3_2;
					float i3_2;
					float r3_6;
					float i3_6;
					float r3_10;
					float i3_10;
					float r3_14;
					float i3_14;
					float r3_18;
					float i3_18;
					float r3_22;
					float i3_22;
					float r3_26;
					float i3_26;
					float r3_30;
					float i3_30;
					{
						float r4_2;
						float i4_2;
						float r4_10;
						float i4_10;
						float r4_18;
						float i4_18;
						float r4_26;
						float i4_26;
						{
							float r5_2;
							float i5_2;
							float r5_18;
							float i5_18;
							r5_2 = in[startIndexInOut + 2].re;
							i5_2 = in[startIndexInOut + 2].im;
							r5_18 = in[startIndexInOut + 18].re;
							i5_18 = in[startIndexInOut + 18].im;
							r4_2 = (r5_2 + r5_18);
							i4_2 = (i5_2 + i5_18);
							r4_18 = (r5_2 - r5_18);
							i4_18 = (i5_2 - i5_18);
						}
						{
							float r5_10;
							float i5_10;
							float r5_26;
							float i5_26;
							r5_10 = in[startIndexInOut + 10].re;
							i5_10 = in[startIndexInOut + 10].im;
							r5_26 = in[startIndexInOut + 26].re;
							i5_26 = in[startIndexInOut + 26].im;
							r4_10 = (r5_10 + r5_26);
							i4_10 = (i5_10 + i5_26);
							r4_26 = (r5_10 - r5_26);
							i4_26 = (i5_10 - i5_26);
						}
						r3_2 = (r4_2 + r4_10);
						i3_2 = (i4_2 + i4_10);
						r3_18 = (r4_2 - r4_10);
						i3_18 = (i4_2 - i4_10);
						r3_10 = (r4_18 + i4_26);
						i3_10 = (i4_18 - r4_26);
						r3_26 = (r4_18 - i4_26);
						i3_26 = (i4_18 + r4_26);
					}
					{
						float r4_6;
						float i4_6;
						float r4_14;
						float i4_14;
						float r4_22;
						float i4_22;
						float r4_30;
						float i4_30;
						{
							float r5_6;
							float i5_6;
							float r5_22;
							float i5_22;
							r5_6 = in[startIndexInOut + 6].re;
							i5_6 = in[startIndexInOut + 6].im;
							r5_22 = in[startIndexInOut + 22].re;
							i5_22 = in[startIndexInOut + 22].im;
							r4_6 = (r5_6 + r5_22);
							i4_6 = (i5_6 + i5_22);
							r4_22 = (r5_6 - r5_22);
							i4_22 = (i5_6 - i5_22);
						}
						{
							float r5_14;
							float i5_14;
							float r5_30;
							float i5_30;
							r5_14 = in[startIndexInOut + 14].re;
							i5_14 = in[startIndexInOut + 14].im;
							r5_30 = in[startIndexInOut + 30].re;
							i5_30 = in[startIndexInOut + 30].im;
							r4_14 = (r5_14 + r5_30);
							i4_14 = (i5_14 + i5_30);
							r4_30 = (r5_14 - r5_30);
							i4_30 = (i5_14 - i5_30);
						}
						r3_6 = (r4_6 + r4_14);
						i3_6 = (i4_6 + i4_14);
						r3_22 = (r4_6 - r4_14);
						i3_22 = (i4_6 - i4_14);
						r3_14 = (r4_22 + i4_30);
						i3_14 = (i4_22 - r4_30);
						r3_30 = (r4_22 - i4_30);
						i3_30 = (i4_22 + r4_30);
					}
					r2_2 = (r3_2 + r3_6);
					i2_2 = (i3_2 + i3_6);
					r2_18 = (r3_2 - r3_6);
					i2_18 = (i3_2 - i3_6);
					tmpr = (0.707106781187F * (r3_14 + i3_14));
					tmpi = (0.707106781187F * (i3_14 - r3_14));
					r2_6 = (r3_10 + tmpr);
					i2_6 = (i3_10 + tmpi);
					r2_22 = (r3_10 - tmpr);
					i2_22 = (i3_10 - tmpi);
					r2_10 = (r3_18 + i3_22);
					i2_10 = (i3_18 - r3_22);
					r2_26 = (r3_18 - i3_22);
					i2_26 = (i3_18 + r3_22);
					tmpr = (0.707106781187F * (i3_30 - r3_30));
					tmpi = (0.707106781187F * (r3_30 + i3_30));
					r2_14 = (r3_26 + tmpr);
					i2_14 = (i3_26 - tmpi);
					r2_30 = (r3_26 - tmpr);
					i2_30 = (i3_26 + tmpi);
				}
				r1_0 = (r2_0 + r2_2);
				i1_0 = (i2_0 + i2_2);
				r1_16 = (r2_0 - r2_2);
				i1_16 = (i2_0 - i2_2);
				tmpr = ((0.923879532511F * r2_6) + (0.382683432365F * i2_6));
				tmpi = ((0.923879532511F * i2_6) - (0.382683432365F * r2_6));
				r1_2 = (r2_4 + tmpr);
				i1_2 = (i2_4 + tmpi);
				r1_18 = (r2_4 - tmpr);
				i1_18 = (i2_4 - tmpi);
				tmpr = (0.707106781187F * (r2_10 + i2_10));
				tmpi = (0.707106781187F * (i2_10 - r2_10));
				r1_4 = (r2_8 + tmpr);
				i1_4 = (i2_8 + tmpi);
				r1_20 = (r2_8 - tmpr);
				i1_20 = (i2_8 - tmpi);
				tmpr = ((0.382683432365F * r2_14) + (0.923879532511F * i2_14));
				tmpi = ((0.382683432365F * i2_14) - (0.923879532511F * r2_14));
				r1_6 = (r2_12 + tmpr);
				i1_6 = (i2_12 + tmpi);
				r1_22 = (r2_12 - tmpr);
				i1_22 = (i2_12 - tmpi);
				r1_8 = (r2_16 + i2_18);
				i1_8 = (i2_16 - r2_18);
				r1_24 = (r2_16 - i2_18);
				i1_24 = (i2_16 + r2_18);
				tmpr = ((0.923879532511F * i2_22) - (0.382683432365F * r2_22));
				tmpi = ((0.923879532511F * r2_22) + (0.382683432365F * i2_22));
				r1_10 = (r2_20 + tmpr);
				i1_10 = (i2_20 - tmpi);
				r1_26 = (r2_20 - tmpr);
				i1_26 = (i2_20 + tmpi);
				tmpr = (0.707106781187F * (i2_26 - r2_26));
				tmpi = (0.707106781187F * (r2_26 + i2_26));
				r1_12 = (r2_24 + tmpr);
				i1_12 = (i2_24 - tmpi);
				r1_28 = (r2_24 - tmpr);
				i1_28 = (i2_24 + tmpi);
				tmpr = ((0.382683432365F * i2_30) - (0.923879532511F * r2_30));
				tmpi = ((0.382683432365F * r2_30) + (0.923879532511F * i2_30));
				r1_14 = (r2_28 + tmpr);
				i1_14 = (i2_28 - tmpi);
				r1_30 = (r2_28 - tmpr);
				i1_30 = (i2_28 + tmpi);
			}
			{
				float r2_1;
				float i2_1;
				float r2_3;
				float i2_3;
				float r2_5;
				float i2_5;
				float r2_7;
				float i2_7;
				float r2_9;
				float i2_9;
				float r2_11;
				float i2_11;
				float r2_13;
				float i2_13;
				float r2_15;
				float i2_15;
				float r2_17;
				float i2_17;
				float r2_19;
				float i2_19;
				float r2_21;
				float i2_21;
				float r2_23;
				float i2_23;
				float r2_25;
				float i2_25;
				float r2_27;
				float i2_27;
				float r2_29;
				float i2_29;
				float r2_31;
				float i2_31;
				{
					float r3_1;
					float i3_1;
					float r3_5;
					float i3_5;
					float r3_9;
					float i3_9;
					float r3_13;
					float i3_13;
					float r3_17;
					float i3_17;
					float r3_21;
					float i3_21;
					float r3_25;
					float i3_25;
					float r3_29;
					float i3_29;
					{
						float r4_1;
						float i4_1;
						float r4_9;
						float i4_9;
						float r4_17;
						float i4_17;
						float r4_25;
						float i4_25;
						{
							float r5_1;
							float i5_1;
							float r5_17;
							float i5_17;
							r5_1 = in[startIndexInOut + 1].re;
							i5_1 = in[startIndexInOut + 1].im;
							r5_17 = in[startIndexInOut + 17].re;
							i5_17 = in[startIndexInOut + 17].im;
							r4_1 = (r5_1 + r5_17);
							i4_1 = (i5_1 + i5_17);
							r4_17 = (r5_1 - r5_17);
							i4_17 = (i5_1 - i5_17);
						}
						{
							float r5_9;
							float i5_9;
							float r5_25;
							float i5_25;
							r5_9 = in[startIndexInOut + 9].re;
							i5_9 = in[startIndexInOut + 9].im;
							r5_25 = in[startIndexInOut + 25].re;
							i5_25 = in[startIndexInOut + 25].im;
							r4_9 = (r5_9 + r5_25);
							i4_9 = (i5_9 + i5_25);
							r4_25 = (r5_9 - r5_25);
							i4_25 = (i5_9 - i5_25);
						}
						r3_1 = (r4_1 + r4_9);
						i3_1 = (i4_1 + i4_9);
						r3_17 = (r4_1 - r4_9);
						i3_17 = (i4_1 - i4_9);
						r3_9 = (r4_17 + i4_25);
						i3_9 = (i4_17 - r4_25);
						r3_25 = (r4_17 - i4_25);
						i3_25 = (i4_17 + r4_25);
					}
					{
						float r4_5;
						float i4_5;
						float r4_13;
						float i4_13;
						float r4_21;
						float i4_21;
						float r4_29;
						float i4_29;
						{
							float r5_5;
							float i5_5;
							float r5_21;
							float i5_21;
							r5_5 = in[startIndexInOut + 5].re;
							i5_5 = in[startIndexInOut + 5].im;
							r5_21 = in[startIndexInOut + 21].re;
							i5_21 = in[startIndexInOut + 21].im;
							r4_5 = (r5_5 + r5_21);
							i4_5 = (i5_5 + i5_21);
							r4_21 = (r5_5 - r5_21);
							i4_21 = (i5_5 - i5_21);
						}
						{
							float r5_13;
							float i5_13;
							float r5_29;
							float i5_29;
							r5_13 = in[startIndexInOut + 13].re;
							i5_13 = in[startIndexInOut + 13].im;
							r5_29 = in[startIndexInOut + 29].re;
							i5_29 = in[startIndexInOut + 29].im;
							r4_13 = (r5_13 + r5_29);
							i4_13 = (i5_13 + i5_29);
							r4_29 = (r5_13 - r5_29);
							i4_29 = (i5_13 - i5_29);
						}
						r3_5 = (r4_5 + r4_13);
						i3_5 = (i4_5 + i4_13);
						r3_21 = (r4_5 - r4_13);
						i3_21 = (i4_5 - i4_13);
						r3_13 = (r4_21 + i4_29);
						i3_13 = (i4_21 - r4_29);
						r3_29 = (r4_21 - i4_29);
						i3_29 = (i4_21 + r4_29);
					}
					r2_1 = (r3_1 + r3_5);
					i2_1 = (i3_1 + i3_5);
					r2_17 = (r3_1 - r3_5);
					i2_17 = (i3_1 - i3_5);
					tmpr = (0.707106781187F * (r3_13 + i3_13));
					tmpi = (0.707106781187F * (i3_13 - r3_13));
					r2_5 = (r3_9 + tmpr);
					i2_5 = (i3_9 + tmpi);
					r2_21 = (r3_9 - tmpr);
					i2_21 = (i3_9 - tmpi);
					r2_9 = (r3_17 + i3_21);
					i2_9 = (i3_17 - r3_21);
					r2_25 = (r3_17 - i3_21);
					i2_25 = (i3_17 + r3_21);
					tmpr = (0.707106781187F * (i3_29 - r3_29));
					tmpi = (0.707106781187F * (r3_29 + i3_29));
					r2_13 = (r3_25 + tmpr);
					i2_13 = (i3_25 - tmpi);
					r2_29 = (r3_25 - tmpr);
					i2_29 = (i3_25 + tmpi);
				}
				{
					float r3_3;
					float i3_3;
					float r3_7;
					float i3_7;
					float r3_11;
					float i3_11;
					float r3_15;
					float i3_15;
					float r3_19;
					float i3_19;
					float r3_23;
					float i3_23;
					float r3_27;
					float i3_27;
					float r3_31;
					float i3_31;
					{
						float r4_3;
						float i4_3;
						float r4_11;
						float i4_11;
						float r4_19;
						float i4_19;
						float r4_27;
						float i4_27;
						{
							float r5_3;
							float i5_3;
							float r5_19;
							float i5_19;
							r5_3 = in[startIndexInOut + 3].re;
							i5_3 = in[startIndexInOut + 3].im;
							r5_19 = in[startIndexInOut + 19].re;
							i5_19 = in[startIndexInOut + 19].im;
							r4_3 = (r5_3 + r5_19);
							i4_3 = (i5_3 + i5_19);
							r4_19 = (r5_3 - r5_19);
							i4_19 = (i5_3 - i5_19);
						}
						{
							float r5_11;
							float i5_11;
							float r5_27;
							float i5_27;
							r5_11 = in[startIndexInOut + 11].re;
							i5_11 = in[startIndexInOut + 11].im;
							r5_27 = in[startIndexInOut + 27].re;
							i5_27 = in[startIndexInOut + 27].im;
							r4_11 = (r5_11 + r5_27);
							i4_11 = (i5_11 + i5_27);
							r4_27 = (r5_11 - r5_27);
							i4_27 = (i5_11 - i5_27);
						}
						r3_3 = (r4_3 + r4_11);
						i3_3 = (i4_3 + i4_11);
						r3_19 = (r4_3 - r4_11);
						i3_19 = (i4_3 - i4_11);
						r3_11 = (r4_19 + i4_27);
						i3_11 = (i4_19 - r4_27);
						r3_27 = (r4_19 - i4_27);
						i3_27 = (i4_19 + r4_27);
					}
					{
						float r4_7;
						float i4_7;
						float r4_15;
						float i4_15;
						float r4_23;
						float i4_23;
						float r4_31;
						float i4_31;
						{
							float r5_7;
							float i5_7;
							float r5_23;
							float i5_23;
							r5_7 = in[startIndexInOut + 7].re;
							i5_7 = in[startIndexInOut + 7].im;
							r5_23 = in[startIndexInOut + 23].re;
							i5_23 = in[startIndexInOut + 23].im;
							r4_7 = (r5_7 + r5_23);
							i4_7 = (i5_7 + i5_23);
							r4_23 = (r5_7 - r5_23);
							i4_23 = (i5_7 - i5_23);
						}
						{
							float r5_15;
							float i5_15;
							float r5_31;
							float i5_31;
							r5_15 = in[startIndexInOut + 15].re;
							i5_15 = in[startIndexInOut + 15].im;
							r5_31 = in[startIndexInOut + 31].re;
							i5_31 = in[startIndexInOut + 31].im;
							r4_15 = (r5_15 + r5_31);
							i4_15 = (i5_15 + i5_31);
							r4_31 = (r5_15 - r5_31);
							i4_31 = (i5_15 - i5_31);
						}
						r3_7 = (r4_7 + r4_15);
						i3_7 = (i4_7 + i4_15);
						r3_23 = (r4_7 - r4_15);
						i3_23 = (i4_7 - i4_15);
						r3_15 = (r4_23 + i4_31);
						i3_15 = (i4_23 - r4_31);
						r3_31 = (r4_23 - i4_31);
						i3_31 = (i4_23 + r4_31);
					}
					r2_3 = (r3_3 + r3_7);
					i2_3 = (i3_3 + i3_7);
					r2_19 = (r3_3 - r3_7);
					i2_19 = (i3_3 - i3_7);
					tmpr = (0.707106781187F * (r3_15 + i3_15));
					tmpi = (0.707106781187F * (i3_15 - r3_15));
					r2_7 = (r3_11 + tmpr);
					i2_7 = (i3_11 + tmpi);
					r2_23 = (r3_11 - tmpr);
					i2_23 = (i3_11 - tmpi);
					r2_11 = (r3_19 + i3_23);
					i2_11 = (i3_19 - r3_23);
					r2_27 = (r3_19 - i3_23);
					i2_27 = (i3_19 + r3_23);
					tmpr = (0.707106781187F * (i3_31 - r3_31));
					tmpi = (0.707106781187F * (r3_31 + i3_31));
					r2_15 = (r3_27 + tmpr);
					i2_15 = (i3_27 - tmpi);
					r2_31 = (r3_27 - tmpr);
					i2_31 = (i3_27 + tmpi);
				}
				r1_1 = (r2_1 + r2_3);
				i1_1 = (i2_1 + i2_3);
				r1_17 = (r2_1 - r2_3);
				i1_17 = (i2_1 - i2_3);
				tmpr = ((0.923879532511F * r2_7) + (0.382683432365F * i2_7));
				tmpi = ((0.923879532511F * i2_7) - (0.382683432365F * r2_7));
				r1_3 = (r2_5 + tmpr);
				i1_3 = (i2_5 + tmpi);
				r1_19 = (r2_5 - tmpr);
				i1_19 = (i2_5 - tmpi);
				tmpr = (0.707106781187F * (r2_11 + i2_11));
				tmpi = (0.707106781187F * (i2_11 - r2_11));
				r1_5 = (r2_9 + tmpr);
				i1_5 = (i2_9 + tmpi);
				r1_21 = (r2_9 - tmpr);
				i1_21 = (i2_9 - tmpi);
				tmpr = ((0.382683432365F * r2_15) + (0.923879532511F * i2_15));
				tmpi = ((0.382683432365F * i2_15) - (0.923879532511F * r2_15));
				r1_7 = (r2_13 + tmpr);
				i1_7 = (i2_13 + tmpi);
				r1_23 = (r2_13 - tmpr);
				i1_23 = (i2_13 - tmpi);
				r1_9 = (r2_17 + i2_19);
				i1_9 = (i2_17 - r2_19);
				r1_25 = (r2_17 - i2_19);
				i1_25 = (i2_17 + r2_19);
				tmpr = ((0.923879532511F * i2_23) - (0.382683432365F * r2_23));
				tmpi = ((0.923879532511F * r2_23) + (0.382683432365F * i2_23));
				r1_11 = (r2_21 + tmpr);
				i1_11 = (i2_21 - tmpi);
				r1_27 = (r2_21 - tmpr);
				i1_27 = (i2_21 + tmpi);
				tmpr = (0.707106781187F * (i2_27 - r2_27));
				tmpi = (0.707106781187F * (r2_27 + i2_27));
				r1_13 = (r2_25 + tmpr);
				i1_13 = (i2_25 - tmpi);
				r1_29 = (r2_25 - tmpr);
				i1_29 = (i2_25 + tmpi);
				tmpr = ((0.382683432365F * i2_31) - (0.923879532511F * r2_31));
				tmpi = ((0.382683432365F * r2_31) + (0.923879532511F * i2_31));
				r1_15 = (r2_29 + tmpr);
				i1_15 = (i2_29 - tmpi);
				r1_31 = (r2_29 - tmpr);
				i1_31 = (i2_29 + tmpi);
			}
			out[startIndexInOut + 0].re = (r1_0 + r1_1);
			out[startIndexInOut + 0].im = (i1_0 + i1_1);
			out[startIndexInOut + 16].re = (r1_0 - r1_1);
			out[startIndexInOut + 16].im = (i1_0 - i1_1);
			tmpr = ((0.980785280403F * r1_3) + (0.195090322016F * i1_3));
			tmpi = ((0.980785280403F * i1_3) - (0.195090322016F * r1_3));
			out[startIndexInOut + 1].re = (r1_2 + tmpr);
			out[startIndexInOut + 1].im = (i1_2 + tmpi);
			out[startIndexInOut + 17].re = (r1_2 - tmpr);
			out[startIndexInOut + 17].im = (i1_2 - tmpi);
			tmpr = ((0.923879532511F * r1_5) + (0.382683432365F * i1_5));
			tmpi = ((0.923879532511F * i1_5) - (0.382683432365F * r1_5));
			out[startIndexInOut + 2].re = (r1_4 + tmpr);
			out[startIndexInOut + 2].im = (i1_4 + tmpi);
			out[startIndexInOut + 18].re = (r1_4 - tmpr);
			out[startIndexInOut + 18].im = (i1_4 - tmpi);
			tmpr = ((0.831469612303F * r1_7) + (0.55557023302F * i1_7));
			tmpi = ((0.831469612303F * i1_7) - (0.55557023302F * r1_7));
			out[startIndexInOut + 3].re = (r1_6 + tmpr);
			out[startIndexInOut + 3].im = (i1_6 + tmpi);
			out[startIndexInOut + 19].re = (r1_6 - tmpr);
			out[startIndexInOut + 19].im = (i1_6 - tmpi);
			tmpr = (0.707106781187F * (r1_9 + i1_9));
			tmpi = (0.707106781187F * (i1_9 - r1_9));
			out[startIndexInOut + 4].re = (r1_8 + tmpr);
			out[startIndexInOut + 4].im = (i1_8 + tmpi);
			out[startIndexInOut + 20].re = (r1_8 - tmpr);
			out[startIndexInOut + 20].im = (i1_8 - tmpi);
			tmpr = ((0.55557023302F * r1_11) + (0.831469612303F * i1_11));
			tmpi = ((0.55557023302F * i1_11) - (0.831469612303F * r1_11));
			out[startIndexInOut + 5].re = (r1_10 + tmpr);
			out[startIndexInOut + 5].im = (i1_10 + tmpi);
			out[startIndexInOut + 21].re = (r1_10 - tmpr);
			out[startIndexInOut + 21].im = (i1_10 - tmpi);
			tmpr = ((0.382683432365F * r1_13) + (0.923879532511F * i1_13));
			tmpi = ((0.382683432365F * i1_13) - (0.923879532511F * r1_13));
			out[startIndexInOut + 6].re = (r1_12 + tmpr);
			out[startIndexInOut + 6].im = (i1_12 + tmpi);
			out[startIndexInOut + 22].re = (r1_12 - tmpr);
			out[startIndexInOut + 22].im = (i1_12 - tmpi);
			tmpr = ((0.195090322016F * r1_15) + (0.980785280403F * i1_15));
			tmpi = ((0.195090322016F * i1_15) - (0.980785280403F * r1_15));
			out[startIndexInOut + 7].re = (r1_14 + tmpr);
			out[startIndexInOut + 7].im = (i1_14 + tmpi);
			out[startIndexInOut + 23].re = (r1_14 - tmpr);
			out[startIndexInOut + 23].im = (i1_14 - tmpi);
			out[startIndexInOut + 8].re = (r1_16 + i1_17);
			out[startIndexInOut + 8].im = (i1_16 - r1_17);
			out[startIndexInOut + 24].re = (r1_16 - i1_17);
			out[startIndexInOut + 24].im = (i1_16 + r1_17);
			tmpr = ((0.980785280403F * i1_19) - (0.195090322016F * r1_19));
			tmpi = ((0.980785280403F * r1_19) + (0.195090322016F * i1_19));
			out[startIndexInOut + 9].re = (r1_18 + tmpr);
			out[startIndexInOut + 9].im = (i1_18 - tmpi);
			out[startIndexInOut + 25].re = (r1_18 - tmpr);
			out[startIndexInOut + 25].im = (i1_18 + tmpi);
			tmpr = ((0.923879532511F * i1_21) - (0.382683432365F * r1_21));
			tmpi = ((0.923879532511F * r1_21) + (0.382683432365F * i1_21));
			out[startIndexInOut + 10].re = (r1_20 + tmpr);
			out[startIndexInOut + 10].im = (i1_20 - tmpi);
			out[startIndexInOut + 26].re = (r1_20 - tmpr);
			out[startIndexInOut + 26].im = (i1_20 + tmpi);
			tmpr = ((0.831469612303F * i1_23) - (0.55557023302F * r1_23));
			tmpi = ((0.831469612303F * r1_23) + (0.55557023302F * i1_23));
			out[startIndexInOut + 11].re = (r1_22 + tmpr);
			out[startIndexInOut + 11].im = (i1_22 - tmpi);
			out[startIndexInOut + 27].re = (r1_22 - tmpr);
			out[startIndexInOut + 27].im = (i1_22 + tmpi);
			tmpr = (0.707106781187F * (i1_25 - r1_25));
			tmpi = (0.707106781187F * (r1_25 + i1_25));
			out[startIndexInOut + 12].re = (r1_24 + tmpr);
			out[startIndexInOut + 12].im = (i1_24 - tmpi);
			out[startIndexInOut + 28].re = (r1_24 - tmpr);
			out[startIndexInOut + 28].im = (i1_24 + tmpi);
			tmpr = ((0.55557023302F * i1_27) - (0.831469612303F * r1_27));
			tmpi = ((0.55557023302F * r1_27) + (0.831469612303F * i1_27));
			out[startIndexInOut + 13].re = (r1_26 + tmpr);
			out[startIndexInOut + 13].im = (i1_26 - tmpi);
			out[startIndexInOut + 29].re = (r1_26 - tmpr);
			out[startIndexInOut + 29].im = (i1_26 + tmpi);
			tmpr = ((0.382683432365F * i1_29) - (0.923879532511F * r1_29));
			tmpi = ((0.382683432365F * r1_29) + (0.923879532511F * i1_29));
			out[startIndexInOut + 14].re = (r1_28 + tmpr);
			out[startIndexInOut + 14].im = (i1_28 - tmpi);
			out[startIndexInOut + 30].re = (r1_28 - tmpr);
			out[startIndexInOut + 30].im = (i1_28 + tmpi);
			tmpr = ((0.195090322016F * i1_31) - (0.980785280403F * r1_31));
			tmpi = ((0.195090322016F * r1_31) + (0.980785280403F * i1_31));
			out[startIndexInOut + 15].re = (r1_30 + tmpr);
			out[startIndexInOut + 15].im = (i1_30 - tmpi);
			out[startIndexInOut + 31].re = (r1_30 - tmpr);
			out[startIndexInOut + 31].im = (i1_30 + tmpi);
		}
	}
	public static void fft_twiddle_32(int a, int b, int startIndexInOut, COMPLEX[] in, COMPLEX[] out, COMPLEX[] W, int nW, int nWdn, int m) {
		float tmpr;
		float tmpi;
		float wr;
		float wi;
		if((b - a) < 128) {
			for(int i = a, l1 = nWdn * i, kp_start = startIndexInOut + i; i < b; i++, l1 += nWdn, kp_start++) {
				int jp_start = startIndexInOut + i;
				{
					float r1_0;
					float i1_0;
					float r1_1;
					float i1_1;
					float r1_2;
					float i1_2;
					float r1_3;
					float i1_3;
					float r1_4;
					float i1_4;
					float r1_5;
					float i1_5;
					float r1_6;
					float i1_6;
					float r1_7;
					float i1_7;
					float r1_8;
					float i1_8;
					float r1_9;
					float i1_9;
					float r1_10;
					float i1_10;
					float r1_11;
					float i1_11;
					float r1_12;
					float i1_12;
					float r1_13;
					float i1_13;
					float r1_14;
					float i1_14;
					float r1_15;
					float i1_15;
					float r1_16;
					float i1_16;
					float r1_17;
					float i1_17;
					float r1_18;
					float i1_18;
					float r1_19;
					float i1_19;
					float r1_20;
					float i1_20;
					float r1_21;
					float i1_21;
					float r1_22;
					float i1_22;
					float r1_23;
					float i1_23;
					float r1_24;
					float i1_24;
					float r1_25;
					float i1_25;
					float r1_26;
					float i1_26;
					float r1_27;
					float i1_27;
					float r1_28;
					float i1_28;
					float r1_29;
					float i1_29;
					float r1_30;
					float i1_30;
					float r1_31;
					float i1_31;
					{
						float r2_0;
						float i2_0;
						float r2_2;
						float i2_2;
						float r2_4;
						float i2_4;
						float r2_6;
						float i2_6;
						float r2_8;
						float i2_8;
						float r2_10;
						float i2_10;
						float r2_12;
						float i2_12;
						float r2_14;
						float i2_14;
						float r2_16;
						float i2_16;
						float r2_18;
						float i2_18;
						float r2_20;
						float i2_20;
						float r2_22;
						float i2_22;
						float r2_24;
						float i2_24;
						float r2_26;
						float i2_26;
						float r2_28;
						float i2_28;
						float r2_30;
						float i2_30;
						{
							float r3_0;
							float i3_0;
							float r3_4;
							float i3_4;
							float r3_8;
							float i3_8;
							float r3_12;
							float i3_12;
							float r3_16;
							float i3_16;
							float r3_20;
							float i3_20;
							float r3_24;
							float i3_24;
							float r3_28;
							float i3_28;
							{
								float r4_0;
								float i4_0;
								float r4_8;
								float i4_8;
								float r4_16;
								float i4_16;
								float r4_24;
								float i4_24;
								{
									float r5_0;
									float i5_0;
									float r5_16;
									float i5_16;
									r5_0 = in[jp_start + 0 * m].re;
									i5_0 = in[jp_start + 0 * m].im;
									wr = W[16 * l1].re;
									wi = W[16 * l1].im;
									tmpr = in[jp_start + 16 * m].re;
									tmpi = in[jp_start + 16 * m].im;
									r5_16 = ((wr * tmpr) - (wi * tmpi));
									i5_16 = ((wi * tmpr) + (wr * tmpi));
									r4_0 = (r5_0 + r5_16);
									i4_0 = (i5_0 + i5_16);
									r4_16 = (r5_0 - r5_16);
									i4_16 = (i5_0 - i5_16);
								}
								{
									float r5_8;
									float i5_8;
									float r5_24;
									float i5_24;
									wr = W[8 * l1].re;
									wi = W[8 * l1].im;
									tmpr = in[jp_start + 8 * m].re;
									tmpi = in[jp_start + 8 * m].im;
									r5_8 = ((wr * tmpr) - (wi * tmpi));
									i5_8 = ((wi * tmpr) + (wr * tmpi));
									wr = W[24 * l1].re;
									wi = W[24 * l1].im;
									tmpr = in[jp_start + 24 * m].re;
									tmpi = in[jp_start + 24 * m].im;
									r5_24 = ((wr * tmpr) - (wi * tmpi));
									i5_24 = ((wi * tmpr) + (wr * tmpi));
									r4_8 = (r5_8 + r5_24);
									i4_8 = (i5_8 + i5_24);
									r4_24 = (r5_8 - r5_24);
									i4_24 = (i5_8 - i5_24);
								}
								r3_0 = (r4_0 + r4_8);
								i3_0 = (i4_0 + i4_8);
								r3_16 = (r4_0 - r4_8);
								i3_16 = (i4_0 - i4_8);
								r3_8 = (r4_16 + i4_24);
								i3_8 = (i4_16 - r4_24);
								r3_24 = (r4_16 - i4_24);
								i3_24 = (i4_16 + r4_24);
							}
							{
								float r4_4;
								float i4_4;
								float r4_12;
								float i4_12;
								float r4_20;
								float i4_20;
								float r4_28;
								float i4_28;
								{
									float r5_4;
									float i5_4;
									float r5_20;
									float i5_20;
									wr = W[4 * l1].re;
									wi = W[4 * l1].im;
									tmpr = in[jp_start + 4 * m].re;
									tmpi = in[jp_start + 4 * m].im;
									r5_4 = ((wr * tmpr) - (wi * tmpi));
									i5_4 = ((wi * tmpr) + (wr * tmpi));
									wr = W[20 * l1].re;
									wi = W[20 * l1].im;
									tmpr = in[jp_start + 20 * m].re;
									tmpi = in[jp_start + 20 * m].im;
									r5_20 = ((wr * tmpr) - (wi * tmpi));
									i5_20 = ((wi * tmpr) + (wr * tmpi));
									r4_4 = (r5_4 + r5_20);
									i4_4 = (i5_4 + i5_20);
									r4_20 = (r5_4 - r5_20);
									i4_20 = (i5_4 - i5_20);
								}
								{
									float r5_12;
									float i5_12;
									float r5_28;
									float i5_28;
									wr = W[12 * l1].re;
									wi = W[12 * l1].im;
									tmpr = in[jp_start + 12 * m].re;
									tmpi = in[jp_start + 12 * m].im;
									r5_12 = ((wr * tmpr) - (wi * tmpi));
									i5_12 = ((wi * tmpr) + (wr * tmpi));
									wr = W[28 * l1].re;
									wi = W[28 * l1].im;
									tmpr = in[jp_start + 28 * m].re;
									tmpi = in[jp_start + 28 * m].im;
									r5_28 = ((wr * tmpr) - (wi * tmpi));
									i5_28 = ((wi * tmpr) + (wr * tmpi));
									r4_12 = (r5_12 + r5_28);
									i4_12 = (i5_12 + i5_28);
									r4_28 = (r5_12 - r5_28);
									i4_28 = (i5_12 - i5_28);
								}
								r3_4 = (r4_4 + r4_12);
								i3_4 = (i4_4 + i4_12);
								r3_20 = (r4_4 - r4_12);
								i3_20 = (i4_4 - i4_12);
								r3_12 = (r4_20 + i4_28);
								i3_12 = (i4_20 - r4_28);
								r3_28 = (r4_20 - i4_28);
								i3_28 = (i4_20 + r4_28);
							}
							r2_0 = (r3_0 + r3_4);
							i2_0 = (i3_0 + i3_4);
							r2_16 = (r3_0 - r3_4);
							i2_16 = (i3_0 - i3_4);
							tmpr = (0.707106781187F * (r3_12 + i3_12));
							tmpi = (0.707106781187F * (i3_12 - r3_12));
							r2_4 = (r3_8 + tmpr);
							i2_4 = (i3_8 + tmpi);
							r2_20 = (r3_8 - tmpr);
							i2_20 = (i3_8 - tmpi);
							r2_8 = (r3_16 + i3_20);
							i2_8 = (i3_16 - r3_20);
							r2_24 = (r3_16 - i3_20);
							i2_24 = (i3_16 + r3_20);
							tmpr = (0.707106781187F * (i3_28 - r3_28));
							tmpi = (0.707106781187F * (r3_28 + i3_28));
							r2_12 = (r3_24 + tmpr);
							i2_12 = (i3_24 - tmpi);
							r2_28 = (r3_24 - tmpr);
							i2_28 = (i3_24 + tmpi);
						}
						{
							float r3_2;
							float i3_2;
							float r3_6;
							float i3_6;
							float r3_10;
							float i3_10;
							float r3_14;
							float i3_14;
							float r3_18;
							float i3_18;
							float r3_22;
							float i3_22;
							float r3_26;
							float i3_26;
							float r3_30;
							float i3_30;
							{
								float r4_2;
								float i4_2;
								float r4_10;
								float i4_10;
								float r4_18;
								float i4_18;
								float r4_26;
								float i4_26;
								{
									float r5_2;
									float i5_2;
									float r5_18;
									float i5_18;
									wr = W[2 * l1].re;
									wi = W[2 * l1].im;
									tmpr = in[jp_start + 2 * m].re;
									tmpi = in[jp_start + 2 * m].im;
									r5_2 = ((wr * tmpr) - (wi * tmpi));
									i5_2 = ((wi * tmpr) + (wr * tmpi));
									wr = W[18 * l1].re;
									wi = W[18 * l1].im;
									tmpr = in[jp_start + 18 * m].re;
									tmpi = in[jp_start + 18 * m].im;
									r5_18 = ((wr * tmpr) - (wi * tmpi));
									i5_18 = ((wi * tmpr) + (wr * tmpi));
									r4_2 = (r5_2 + r5_18);
									i4_2 = (i5_2 + i5_18);
									r4_18 = (r5_2 - r5_18);
									i4_18 = (i5_2 - i5_18);
								}
								{
									float r5_10;
									float i5_10;
									float r5_26;
									float i5_26;
									wr = W[10 * l1].re;
									wi = W[10 * l1].im;
									tmpr = in[jp_start + 10 * m].re;
									tmpi = in[jp_start + 10 * m].im;
									r5_10 = ((wr * tmpr) - (wi * tmpi));
									i5_10 = ((wi * tmpr) + (wr * tmpi));
									wr = W[26 * l1].re;
									wi = W[26 * l1].im;
									tmpr = in[jp_start + 26 * m].re;
									tmpi = in[jp_start + 26 * m].im;
									r5_26 = ((wr * tmpr) - (wi * tmpi));
									i5_26 = ((wi * tmpr) + (wr * tmpi));
									r4_10 = (r5_10 + r5_26);
									i4_10 = (i5_10 + i5_26);
									r4_26 = (r5_10 - r5_26);
									i4_26 = (i5_10 - i5_26);
								}
								r3_2 = (r4_2 + r4_10);
								i3_2 = (i4_2 + i4_10);
								r3_18 = (r4_2 - r4_10);
								i3_18 = (i4_2 - i4_10);
								r3_10 = (r4_18 + i4_26);
								i3_10 = (i4_18 - r4_26);
								r3_26 = (r4_18 - i4_26);
								i3_26 = (i4_18 + r4_26);
							}
							{
								float r4_6;
								float i4_6;
								float r4_14;
								float i4_14;
								float r4_22;
								float i4_22;
								float r4_30;
								float i4_30;
								{
									float r5_6;
									float i5_6;
									float r5_22;
									float i5_22;
									wr = W[6 * l1].re;
									wi = W[6 * l1].im;
									tmpr = in[jp_start + 6 * m].re;
									tmpi = in[jp_start + 6 * m].im;
									r5_6 = ((wr * tmpr) - (wi * tmpi));
									i5_6 = ((wi * tmpr) + (wr * tmpi));
									wr = W[22 * l1].re;
									wi = W[22 * l1].im;
									tmpr = in[jp_start + 22 * m].re;
									tmpi = in[jp_start + 22 * m].im;
									r5_22 = ((wr * tmpr) - (wi * tmpi));
									i5_22 = ((wi * tmpr) + (wr * tmpi));
									r4_6 = (r5_6 + r5_22);
									i4_6 = (i5_6 + i5_22);
									r4_22 = (r5_6 - r5_22);
									i4_22 = (i5_6 - i5_22);
								}
								{
									float r5_14;
									float i5_14;
									float r5_30;
									float i5_30;
									wr = W[14 * l1].re;
									wi = W[14 * l1].im;
									tmpr = in[jp_start + 14 * m].re;
									tmpi = in[jp_start + 14 * m].im;
									r5_14 = ((wr * tmpr) - (wi * tmpi));
									i5_14 = ((wi * tmpr) + (wr * tmpi));
									wr = W[30 * l1].re;
									wi = W[30 * l1].im;
									tmpr = in[jp_start + 30 * m].re;
									tmpi = in[jp_start + 30 * m].im;
									r5_30 = ((wr * tmpr) - (wi * tmpi));
									i5_30 = ((wi * tmpr) + (wr * tmpi));
									r4_14 = (r5_14 + r5_30);
									i4_14 = (i5_14 + i5_30);
									r4_30 = (r5_14 - r5_30);
									i4_30 = (i5_14 - i5_30);
								}
								r3_6 = (r4_6 + r4_14);
								i3_6 = (i4_6 + i4_14);
								r3_22 = (r4_6 - r4_14);
								i3_22 = (i4_6 - i4_14);
								r3_14 = (r4_22 + i4_30);
								i3_14 = (i4_22 - r4_30);
								r3_30 = (r4_22 - i4_30);
								i3_30 = (i4_22 + r4_30);
							}
							r2_2 = (r3_2 + r3_6);
							i2_2 = (i3_2 + i3_6);
							r2_18 = (r3_2 - r3_6);
							i2_18 = (i3_2 - i3_6);
							tmpr = (0.707106781187F * (r3_14 + i3_14));
							tmpi = (0.707106781187F * (i3_14 - r3_14));
							r2_6 = (r3_10 + tmpr);
							i2_6 = (i3_10 + tmpi);
							r2_22 = (r3_10 - tmpr);
							i2_22 = (i3_10 - tmpi);
							r2_10 = (r3_18 + i3_22);
							i2_10 = (i3_18 - r3_22);
							r2_26 = (r3_18 - i3_22);
							i2_26 = (i3_18 + r3_22);
							tmpr = (0.707106781187F * (i3_30 - r3_30));
							tmpi = (0.707106781187F * (r3_30 + i3_30));
							r2_14 = (r3_26 + tmpr);
							i2_14 = (i3_26 - tmpi);
							r2_30 = (r3_26 - tmpr);
							i2_30 = (i3_26 + tmpi);
						}
						r1_0 = (r2_0 + r2_2);
						i1_0 = (i2_0 + i2_2);
						r1_16 = (r2_0 - r2_2);
						i1_16 = (i2_0 - i2_2);
						tmpr = ((0.923879532511F * r2_6) + (0.382683432365F * i2_6));
						tmpi = ((0.923879532511F * i2_6) - (0.382683432365F * r2_6));
						r1_2 = (r2_4 + tmpr);
						i1_2 = (i2_4 + tmpi);
						r1_18 = (r2_4 - tmpr);
						i1_18 = (i2_4 - tmpi);
						tmpr = (0.707106781187F * (r2_10 + i2_10));
						tmpi = (0.707106781187F * (i2_10 - r2_10));
						r1_4 = (r2_8 + tmpr);
						i1_4 = (i2_8 + tmpi);
						r1_20 = (r2_8 - tmpr);
						i1_20 = (i2_8 - tmpi);
						tmpr = ((0.382683432365F * r2_14) + (0.923879532511F * i2_14));
						tmpi = ((0.382683432365F * i2_14) - (0.923879532511F * r2_14));
						r1_6 = (r2_12 + tmpr);
						i1_6 = (i2_12 + tmpi);
						r1_22 = (r2_12 - tmpr);
						i1_22 = (i2_12 - tmpi);
						r1_8 = (r2_16 + i2_18);
						i1_8 = (i2_16 - r2_18);
						r1_24 = (r2_16 - i2_18);
						i1_24 = (i2_16 + r2_18);
						tmpr = ((0.923879532511F * i2_22) - (0.382683432365F * r2_22));
						tmpi = ((0.923879532511F * r2_22) + (0.382683432365F * i2_22));
						r1_10 = (r2_20 + tmpr);
						i1_10 = (i2_20 - tmpi);
						r1_26 = (r2_20 - tmpr);
						i1_26 = (i2_20 + tmpi);
						tmpr = (0.707106781187F * (i2_26 - r2_26));
						tmpi = (0.707106781187F * (r2_26 + i2_26));
						r1_12 = (r2_24 + tmpr);
						i1_12 = (i2_24 - tmpi);
						r1_28 = (r2_24 - tmpr);
						i1_28 = (i2_24 + tmpi);
						tmpr = ((0.382683432365F * i2_30) - (0.923879532511F * r2_30));
						tmpi = ((0.382683432365F * r2_30) + (0.923879532511F * i2_30));
						r1_14 = (r2_28 + tmpr);
						i1_14 = (i2_28 - tmpi);
						r1_30 = (r2_28 - tmpr);
						i1_30 = (i2_28 + tmpi);
					}
					{
						float r2_1;
						float i2_1;
						float r2_3;
						float i2_3;
						float r2_5;
						float i2_5;
						float r2_7;
						float i2_7;
						float r2_9;
						float i2_9;
						float r2_11;
						float i2_11;
						float r2_13;
						float i2_13;
						float r2_15;
						float i2_15;
						float r2_17;
						float i2_17;
						float r2_19;
						float i2_19;
						float r2_21;
						float i2_21;
						float r2_23;
						float i2_23;
						float r2_25;
						float i2_25;
						float r2_27;
						float i2_27;
						float r2_29;
						float i2_29;
						float r2_31;
						float i2_31;
						{
							float r3_1;
							float i3_1;
							float r3_5;
							float i3_5;
							float r3_9;
							float i3_9;
							float r3_13;
							float i3_13;
							float r3_17;
							float i3_17;
							float r3_21;
							float i3_21;
							float r3_25;
							float i3_25;
							float r3_29;
							float i3_29;
							{
								float r4_1;
								float i4_1;
								float r4_9;
								float i4_9;
								float r4_17;
								float i4_17;
								float r4_25;
								float i4_25;
								{
									float r5_1;
									float i5_1;
									float r5_17;
									float i5_17;
									wr = W[1 * l1].re;
									wi = W[1 * l1].im;
									tmpr = in[jp_start + 1 * m].re;
									tmpi = in[jp_start + 1 * m].im;
									r5_1 = ((wr * tmpr) - (wi * tmpi));
									i5_1 = ((wi * tmpr) + (wr * tmpi));
									wr = W[17 * l1].re;
									wi = W[17 * l1].im;
									tmpr = in[jp_start + 17 * m].re;
									tmpi = in[jp_start + 17 * m].im;
									r5_17 = ((wr * tmpr) - (wi * tmpi));
									i5_17 = ((wi * tmpr) + (wr * tmpi));
									r4_1 = (r5_1 + r5_17);
									i4_1 = (i5_1 + i5_17);
									r4_17 = (r5_1 - r5_17);
									i4_17 = (i5_1 - i5_17);
								}
								{
									float r5_9;
									float i5_9;
									float r5_25;
									float i5_25;
									wr = W[9 * l1].re;
									wi = W[9 * l1].im;
									tmpr = in[jp_start + 9 * m].re;
									tmpi = in[jp_start + 9 * m].im;
									r5_9 = ((wr * tmpr) - (wi * tmpi));
									i5_9 = ((wi * tmpr) + (wr * tmpi));
									wr = W[25 * l1].re;
									wi = W[25 * l1].im;
									tmpr = in[jp_start + 25 * m].re;
									tmpi = in[jp_start + 25 * m].im;
									r5_25 = ((wr * tmpr) - (wi * tmpi));
									i5_25 = ((wi * tmpr) + (wr * tmpi));
									r4_9 = (r5_9 + r5_25);
									i4_9 = (i5_9 + i5_25);
									r4_25 = (r5_9 - r5_25);
									i4_25 = (i5_9 - i5_25);
								}
								r3_1 = (r4_1 + r4_9);
								i3_1 = (i4_1 + i4_9);
								r3_17 = (r4_1 - r4_9);
								i3_17 = (i4_1 - i4_9);
								r3_9 = (r4_17 + i4_25);
								i3_9 = (i4_17 - r4_25);
								r3_25 = (r4_17 - i4_25);
								i3_25 = (i4_17 + r4_25);
							}
							{
								float r4_5;
								float i4_5;
								float r4_13;
								float i4_13;
								float r4_21;
								float i4_21;
								float r4_29;
								float i4_29;
								{
									float r5_5;
									float i5_5;
									float r5_21;
									float i5_21;
									wr = W[5 * l1].re;
									wi = W[5 * l1].im;
									tmpr = in[jp_start + 5 * m].re;
									tmpi = in[jp_start + 5 * m].im;
									r5_5 = ((wr * tmpr) - (wi * tmpi));
									i5_5 = ((wi * tmpr) + (wr * tmpi));
									wr = W[21 * l1].re;
									wi = W[21 * l1].im;
									tmpr = in[jp_start + 21 * m].re;
									tmpi = in[jp_start + 21 * m].im;
									r5_21 = ((wr * tmpr) - (wi * tmpi));
									i5_21 = ((wi * tmpr) + (wr * tmpi));
									r4_5 = (r5_5 + r5_21);
									i4_5 = (i5_5 + i5_21);
									r4_21 = (r5_5 - r5_21);
									i4_21 = (i5_5 - i5_21);
								}
								{
									float r5_13;
									float i5_13;
									float r5_29;
									float i5_29;
									wr = W[13 * l1].re;
									wi = W[13 * l1].im;
									tmpr = in[jp_start + 13 * m].re;
									tmpi = in[jp_start + 13 * m].im;
									r5_13 = ((wr * tmpr) - (wi * tmpi));
									i5_13 = ((wi * tmpr) + (wr * tmpi));
									wr = W[29 * l1].re;
									wi = W[29 * l1].im;
									tmpr = in[jp_start + 29 * m].re;
									tmpi = in[jp_start + 29 * m].im;
									r5_29 = ((wr * tmpr) - (wi * tmpi));
									i5_29 = ((wi * tmpr) + (wr * tmpi));
									r4_13 = (r5_13 + r5_29);
									i4_13 = (i5_13 + i5_29);
									r4_29 = (r5_13 - r5_29);
									i4_29 = (i5_13 - i5_29);
								}
								r3_5 = (r4_5 + r4_13);
								i3_5 = (i4_5 + i4_13);
								r3_21 = (r4_5 - r4_13);
								i3_21 = (i4_5 - i4_13);
								r3_13 = (r4_21 + i4_29);
								i3_13 = (i4_21 - r4_29);
								r3_29 = (r4_21 - i4_29);
								i3_29 = (i4_21 + r4_29);
							}
							r2_1 = (r3_1 + r3_5);
							i2_1 = (i3_1 + i3_5);
							r2_17 = (r3_1 - r3_5);
							i2_17 = (i3_1 - i3_5);
							tmpr = (0.707106781187F * (r3_13 + i3_13));
							tmpi = (0.707106781187F * (i3_13 - r3_13));
							r2_5 = (r3_9 + tmpr);
							i2_5 = (i3_9 + tmpi);
							r2_21 = (r3_9 - tmpr);
							i2_21 = (i3_9 - tmpi);
							r2_9 = (r3_17 + i3_21);
							i2_9 = (i3_17 - r3_21);
							r2_25 = (r3_17 - i3_21);
							i2_25 = (i3_17 + r3_21);
							tmpr = (0.707106781187F * (i3_29 - r3_29));
							tmpi = (0.707106781187F * (r3_29 + i3_29));
							r2_13 = (r3_25 + tmpr);
							i2_13 = (i3_25 - tmpi);
							r2_29 = (r3_25 - tmpr);
							i2_29 = (i3_25 + tmpi);
						}
						{
							float r3_3;
							float i3_3;
							float r3_7;
							float i3_7;
							float r3_11;
							float i3_11;
							float r3_15;
							float i3_15;
							float r3_19;
							float i3_19;
							float r3_23;
							float i3_23;
							float r3_27;
							float i3_27;
							float r3_31;
							float i3_31;
							{
								float r4_3;
								float i4_3;
								float r4_11;
								float i4_11;
								float r4_19;
								float i4_19;
								float r4_27;
								float i4_27;
								{
									float r5_3;
									float i5_3;
									float r5_19;
									float i5_19;
									wr = W[3 * l1].re;
									wi = W[3 * l1].im;
									tmpr = in[jp_start + 3 * m].re;
									tmpi = in[jp_start + 3 * m].im;
									r5_3 = ((wr * tmpr) - (wi * tmpi));
									i5_3 = ((wi * tmpr) + (wr * tmpi));
									wr = W[19 * l1].re;
									wi = W[19 * l1].im;
									tmpr = in[jp_start + 19 * m].re;
									tmpi = in[jp_start + 19 * m].im;
									r5_19 = ((wr * tmpr) - (wi * tmpi));
									i5_19 = ((wi * tmpr) + (wr * tmpi));
									r4_3 = (r5_3 + r5_19);
									i4_3 = (i5_3 + i5_19);
									r4_19 = (r5_3 - r5_19);
									i4_19 = (i5_3 - i5_19);
								}
								{
									float r5_11;
									float i5_11;
									float r5_27;
									float i5_27;
									wr = W[11 * l1].re;
									wi = W[11 * l1].im;
									tmpr = in[jp_start + 11 * m].re;
									tmpi = in[jp_start + 11 * m].im;
									r5_11 = ((wr * tmpr) - (wi * tmpi));
									i5_11 = ((wi * tmpr) + (wr * tmpi));
									wr = W[27 * l1].re;
									wi = W[27 * l1].im;
									tmpr = in[jp_start + 27 * m].re;
									tmpi = in[jp_start + 27 * m].im;
									r5_27 = ((wr * tmpr) - (wi * tmpi));
									i5_27 = ((wi * tmpr) + (wr * tmpi));
									r4_11 = (r5_11 + r5_27);
									i4_11 = (i5_11 + i5_27);
									r4_27 = (r5_11 - r5_27);
									i4_27 = (i5_11 - i5_27);
								}
								r3_3 = (r4_3 + r4_11);
								i3_3 = (i4_3 + i4_11);
								r3_19 = (r4_3 - r4_11);
								i3_19 = (i4_3 - i4_11);
								r3_11 = (r4_19 + i4_27);
								i3_11 = (i4_19 - r4_27);
								r3_27 = (r4_19 - i4_27);
								i3_27 = (i4_19 + r4_27);
							}
							{
								float r4_7;
								float i4_7;
								float r4_15;
								float i4_15;
								float r4_23;
								float i4_23;
								float r4_31;
								float i4_31;
								{
									float r5_7;
									float i5_7;
									float r5_23;
									float i5_23;
									wr = W[7 * l1].re;
									wi = W[7 * l1].im;
									tmpr = in[jp_start + 7 * m].re;
									tmpi = in[jp_start + 7 * m].im;
									r5_7 = ((wr * tmpr) - (wi * tmpi));
									i5_7 = ((wi * tmpr) + (wr * tmpi));
									wr = W[23 * l1].re;
									wi = W[23 * l1].im;
									tmpr = in[jp_start + 23 * m].re;
									tmpi = in[jp_start + 23 * m].im;
									r5_23 = ((wr * tmpr) - (wi * tmpi));
									i5_23 = ((wi * tmpr) + (wr * tmpi));
									r4_7 = (r5_7 + r5_23);
									i4_7 = (i5_7 + i5_23);
									r4_23 = (r5_7 - r5_23);
									i4_23 = (i5_7 - i5_23);
								}
								{
									float r5_15;
									float i5_15;
									float r5_31;
									float i5_31;
									wr = W[15 * l1].re;
									wi = W[15 * l1].im;
									tmpr = in[jp_start + 15 * m].re;
									tmpi = in[jp_start + 15 * m].im;
									r5_15 = ((wr * tmpr) - (wi * tmpi));
									i5_15 = ((wi * tmpr) + (wr * tmpi));
									wr = W[31 * l1].re;
									wi = W[31 * l1].im;
									tmpr = in[jp_start + 31 * m].re;
									tmpi = in[jp_start + 31 * m].im;
									r5_31 = ((wr * tmpr) - (wi * tmpi));
									i5_31 = ((wi * tmpr) + (wr * tmpi));
									r4_15 = (r5_15 + r5_31);
									i4_15 = (i5_15 + i5_31);
									r4_31 = (r5_15 - r5_31);
									i4_31 = (i5_15 - i5_31);
								}
								r3_7 = (r4_7 + r4_15);
								i3_7 = (i4_7 + i4_15);
								r3_23 = (r4_7 - r4_15);
								i3_23 = (i4_7 - i4_15);
								r3_15 = (r4_23 + i4_31);
								i3_15 = (i4_23 - r4_31);
								r3_31 = (r4_23 - i4_31);
								i3_31 = (i4_23 + r4_31);
							}
							r2_3 = (r3_3 + r3_7);
							i2_3 = (i3_3 + i3_7);
							r2_19 = (r3_3 - r3_7);
							i2_19 = (i3_3 - i3_7);
							tmpr = (0.707106781187F * (r3_15 + i3_15));
							tmpi = (0.707106781187F * (i3_15 - r3_15));
							r2_7 = (r3_11 + tmpr);
							i2_7 = (i3_11 + tmpi);
							r2_23 = (r3_11 - tmpr);
							i2_23 = (i3_11 - tmpi);
							r2_11 = (r3_19 + i3_23);
							i2_11 = (i3_19 - r3_23);
							r2_27 = (r3_19 - i3_23);
							i2_27 = (i3_19 + r3_23);
							tmpr = (0.707106781187F * (i3_31 - r3_31));
							tmpi = (0.707106781187F * (r3_31 + i3_31));
							r2_15 = (r3_27 + tmpr);
							i2_15 = (i3_27 - tmpi);
							r2_31 = (r3_27 - tmpr);
							i2_31 = (i3_27 + tmpi);
						}
						r1_1 = (r2_1 + r2_3);
						i1_1 = (i2_1 + i2_3);
						r1_17 = (r2_1 - r2_3);
						i1_17 = (i2_1 - i2_3);
						tmpr = ((0.923879532511F * r2_7) + (0.382683432365F * i2_7));
						tmpi = ((0.923879532511F * i2_7) - (0.382683432365F * r2_7));
						r1_3 = (r2_5 + tmpr);
						i1_3 = (i2_5 + tmpi);
						r1_19 = (r2_5 - tmpr);
						i1_19 = (i2_5 - tmpi);
						tmpr = (0.707106781187F * (r2_11 + i2_11));
						tmpi = (0.707106781187F * (i2_11 - r2_11));
						r1_5 = (r2_9 + tmpr);
						i1_5 = (i2_9 + tmpi);
						r1_21 = (r2_9 - tmpr);
						i1_21 = (i2_9 - tmpi);
						tmpr = ((0.382683432365F * r2_15) + (0.923879532511F * i2_15));
						tmpi = ((0.382683432365F * i2_15) - (0.923879532511F * r2_15));
						r1_7 = (r2_13 + tmpr);
						i1_7 = (i2_13 + tmpi);
						r1_23 = (r2_13 - tmpr);
						i1_23 = (i2_13 - tmpi);
						r1_9 = (r2_17 + i2_19);
						i1_9 = (i2_17 - r2_19);
						r1_25 = (r2_17 - i2_19);
						i1_25 = (i2_17 + r2_19);
						tmpr = ((0.923879532511F * i2_23) - (0.382683432365F * r2_23));
						tmpi = ((0.923879532511F * r2_23) + (0.382683432365F * i2_23));
						r1_11 = (r2_21 + tmpr);
						i1_11 = (i2_21 - tmpi);
						r1_27 = (r2_21 - tmpr);
						i1_27 = (i2_21 + tmpi);
						tmpr = (0.707106781187F * (i2_27 - r2_27));
						tmpi = (0.707106781187F * (r2_27 + i2_27));
						r1_13 = (r2_25 + tmpr);
						i1_13 = (i2_25 - tmpi);
						r1_29 = (r2_25 - tmpr);
						i1_29 = (i2_25 + tmpi);
						tmpr = ((0.382683432365F * i2_31) - (0.923879532511F * r2_31));
						tmpi = ((0.382683432365F * r2_31) + (0.923879532511F * i2_31));
						r1_15 = (r2_29 + tmpr);
						i1_15 = (i2_29 - tmpi);
						r1_31 = (r2_29 - tmpr);
						i1_31 = (i2_29 + tmpi);
					}
					out[kp_start + 0 * m].re = (r1_0 + r1_1);
					out[kp_start + 0 * m].im = (i1_0 + i1_1);
					out[kp_start + 16 * m].re = (r1_0 - r1_1);
					out[kp_start + 16 * m].im = (i1_0 - i1_1);
					tmpr = ((0.980785280403F * r1_3) + (0.195090322016F * i1_3));
					tmpi = ((0.980785280403F * i1_3) - (0.195090322016F * r1_3));
					out[kp_start + 1 * m].re = (r1_2 + tmpr);
					out[kp_start + 1 * m].im = (i1_2 + tmpi);
					out[kp_start + 17 * m].re = (r1_2 - tmpr);
					out[kp_start + 17 * m].im = (i1_2 - tmpi);
					tmpr = ((0.923879532511F * r1_5) + (0.382683432365F * i1_5));
					tmpi = ((0.923879532511F * i1_5) - (0.382683432365F * r1_5));
					out[kp_start + 2 * m].re = (r1_4 + tmpr);
					out[kp_start + 2 * m].im = (i1_4 + tmpi);
					out[kp_start + 18 * m].re = (r1_4 - tmpr);
					out[kp_start + 18 * m].im = (i1_4 - tmpi);
					tmpr = ((0.831469612303F * r1_7) + (0.55557023302F * i1_7));
					tmpi = ((0.831469612303F * i1_7) - (0.55557023302F * r1_7));
					out[kp_start + 3 * m].re = (r1_6 + tmpr);
					out[kp_start + 3 * m].im = (i1_6 + tmpi);
					out[kp_start + 19 * m].re = (r1_6 - tmpr);
					out[kp_start + 19 * m].im = (i1_6 - tmpi);
					tmpr = (0.707106781187F * (r1_9 + i1_9));
					tmpi = (0.707106781187F * (i1_9 - r1_9));
					out[kp_start + 4 * m].re = (r1_8 + tmpr);
					out[kp_start + 4 * m].im = (i1_8 + tmpi);
					out[kp_start + 20 * m].re = (r1_8 - tmpr);
					out[kp_start + 20 * m].im = (i1_8 - tmpi);
					tmpr = ((0.55557023302F * r1_11) + (0.831469612303F * i1_11));
					tmpi = ((0.55557023302F * i1_11) - (0.831469612303F * r1_11));
					out[kp_start + 5 * m].re = (r1_10 + tmpr);
					out[kp_start + 5 * m].im = (i1_10 + tmpi);
					out[kp_start + 21 * m].re = (r1_10 - tmpr);
					out[kp_start + 21 * m].im = (i1_10 - tmpi);
					tmpr = ((0.382683432365F * r1_13) + (0.923879532511F * i1_13));
					tmpi = ((0.382683432365F * i1_13) - (0.923879532511F * r1_13));
					out[kp_start + 6 * m].re = (r1_12 + tmpr);
					out[kp_start + 6 * m].im = (i1_12 + tmpi);
					out[kp_start + 22 * m].re = (r1_12 - tmpr);
					out[kp_start + 22 * m].im = (i1_12 - tmpi);
					tmpr = ((0.195090322016F * r1_15) + (0.980785280403F * i1_15));
					tmpi = ((0.195090322016F * i1_15) - (0.980785280403F * r1_15));
					out[kp_start + 7 * m].re = (r1_14 + tmpr);
					out[kp_start + 7 * m].im = (i1_14 + tmpi);
					out[kp_start + 23 * m].re = (r1_14 - tmpr);
					out[kp_start + 23 * m].im = (i1_14 - tmpi);
					out[kp_start + 8 * m].re = (r1_16 + i1_17);
					out[kp_start + 8 * m].im = (i1_16 - r1_17);
					out[kp_start + 24 * m].re = (r1_16 - i1_17);
					out[kp_start + 24 * m].im = (i1_16 + r1_17);
					tmpr = ((0.980785280403F * i1_19) - (0.195090322016F * r1_19));
					tmpi = ((0.980785280403F * r1_19) + (0.195090322016F * i1_19));
					out[kp_start + 9 * m].re = (r1_18 + tmpr);
					out[kp_start + 9 * m].im = (i1_18 - tmpi);
					out[kp_start + 25 * m].re = (r1_18 - tmpr);
					out[kp_start + 25 * m].im = (i1_18 + tmpi);
					tmpr = ((0.923879532511F * i1_21) - (0.382683432365F * r1_21));
					tmpi = ((0.923879532511F * r1_21) + (0.382683432365F * i1_21));
					out[kp_start + 10 * m].re = (r1_20 + tmpr);
					out[kp_start + 10 * m].im = (i1_20 - tmpi);
					out[kp_start + 26 * m].re = (r1_20 - tmpr);
					out[kp_start + 26 * m].im = (i1_20 + tmpi);
					tmpr = ((0.831469612303F * i1_23) - (0.55557023302F * r1_23));
					tmpi = ((0.831469612303F * r1_23) + (0.55557023302F * i1_23));
					out[kp_start + 11 * m].re = (r1_22 + tmpr);
					out[kp_start + 11 * m].im = (i1_22 - tmpi);
					out[kp_start + 27 * m].re = (r1_22 - tmpr);
					out[kp_start + 27 * m].im = (i1_22 + tmpi);
					tmpr = (0.707106781187F * (i1_25 - r1_25));
					tmpi = (0.707106781187F * (r1_25 + i1_25));
					out[kp_start + 12 * m].re = (r1_24 + tmpr);
					out[kp_start + 12 * m].im = (i1_24 - tmpi);
					out[kp_start + 28 * m].re = (r1_24 - tmpr);
					out[kp_start + 28 * m].im = (i1_24 + tmpi);
					tmpr = ((0.55557023302F * i1_27) - (0.831469612303F * r1_27));
					tmpi = ((0.55557023302F * r1_27) + (0.831469612303F * i1_27));
					out[kp_start + 13 * m].re = (r1_26 + tmpr);
					out[kp_start + 13 * m].im = (i1_26 - tmpi);
					out[kp_start + 29 * m].re = (r1_26 - tmpr);
					out[kp_start + 29 * m].im = (i1_26 + tmpi);
					tmpr = ((0.382683432365F * i1_29) - (0.923879532511F * r1_29));
					tmpi = ((0.382683432365F * r1_29) + (0.923879532511F * i1_29));
					out[kp_start + 14 * m].re = (r1_28 + tmpr);
					out[kp_start + 14 * m].im = (i1_28 - tmpi);
					out[kp_start + 30 * m].re = (r1_28 - tmpr);
					out[kp_start + 30 * m].im = (i1_28 + tmpi);
					tmpr = ((0.195090322016F * i1_31) - (0.980785280403F * r1_31));
					tmpi = ((0.195090322016F * r1_31) + (0.980785280403F * i1_31));
					out[kp_start + 15 * m].re = (r1_30 + tmpr);
					out[kp_start + 15 * m].im = (i1_30 - tmpi);
					out[kp_start + 31 * m].re = (r1_30 - tmpr);
					out[kp_start + 31 * m].im = (i1_30 + tmpi);
				}
			}
		}
		else {
			final int ab = (a + b) / 2;
			finish {
				async {
					fft_twiddle_32(a, ab, startIndexInOut, in, out, W, nW, nWdn, m);
					fft_twiddle_32(ab, b, startIndexInOut, in, out, W, nW, nWdn, m);
				}
			}
		}
	}
	public static void fft_unshuffle_32(int a, int b, int startIndexInOut, COMPLEX[] in, COMPLEX[] out, int m) {
		if((b - a) < 128) {
			int ip_start = startIndexInOut + a * 32;
			for(int i = a; i < b; ++i) {
				int jp_start = startIndexInOut + i;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
				jp_start += 2 * m;
				COMPLEX.copy(out[jp_start + 0], in[ip_start + 0]);
				COMPLEX.copy(out[jp_start + m], in[ip_start + 1]);
				ip_start += 2;
			}
		}
		else {
			final int ab = (a + b) / 2;
			finish {
				async {
					fft_unshuffle_32(a, ab, startIndexInOut, in, out, m);
					fft_unshuffle_32(ab, b, startIndexInOut, in, out, m);
				}
			}
		}
	}
	
	public static void fft_aux(int n, int startIndexInOut, COMPLEX[] in, COMPLEX[] out, int posFactors, int[] factors, COMPLEX[] W, int nW) {
		if(n == 32) {
			fft_base_32(startIndexInOut, in, out);
			return ;
		}
		if(n == 16) {
			fft_base_16(startIndexInOut, in, out);
			return ;
		}
		if(n == 8) {
			fft_base_8(startIndexInOut, in, out);
			return ;
		}
		if(n == 4) {
			fft_base_4(startIndexInOut, in, out);
			return ;
		}
		if(n == 2) {
			fft_base_2(startIndexInOut, in, out);
			return ;
		}
		final int r = factors[posFactors];
		final int m = n / r;
		if(r < n) {
			if(r == 32) 
				fft_unshuffle_32(0, m, startIndexInOut, in, out, m);
			else if(r == 16) 
				fft_unshuffle_16(0, m, startIndexInOut, in, out, m);
			else if(r == 8) 
				fft_unshuffle_8(0, m, startIndexInOut, in, out, m);
			else if(r == 4) 
				fft_unshuffle_4(0, m, startIndexInOut, in, out, m);
			else if(r == 2) 
				fft_unshuffle_2(0, m, startIndexInOut, in, out, m);
			else 
				unshuffle(0, m, startIndexInOut, in, out, r, m);

			finish {
				async {
					for(int k = 0; k < n; k += m) {
						fft_aux(m, startIndexInOut + k, out, in, posFactors + 1, factors, W, nW);
					}
				}
			}
		}
		if(r == 2) 
			fft_twiddle_2(0, m, startIndexInOut, in, out, W, nW, nW / n, m);
		else if(r == 4) 
			fft_twiddle_4(0, m, startIndexInOut, in, out, W, nW, nW / n, m);
		else if(r == 8) 
			fft_twiddle_8(0, m, startIndexInOut, in, out, W, nW, nW / n, m);
		else if(r == 16) 
			fft_twiddle_16(0, m, startIndexInOut, in, out, W, nW, nW / n, m);
		else if(r == 32) 
			fft_twiddle_32(0, m, startIndexInOut, in, out, W, nW, nW / n, m);
		else 
			fft_twiddle_gen(0, m, startIndexInOut, in, out, W, nW, nW / n, r, m);
	}

	public static void cilk_fft(int n, COMPLEX[] in, COMPLEX[] out, boolean correctness) {
		int l = n;
		int r;
		if(correctness)	{
			factors = new int[40];
			W = allocate(n + 1);
		}

		compute_w_coefficients(n, 0, n / 2, W);
		int i = 0;
		do {
			r = factor(l);
			factors[i++] = r;
			l /= r;
		}while(l > 1);
		fft_aux(n, 0, in, out, 0, factors, W, n);
	}
	public static void test_fft_elem(int n, int j, COMPLEX[] in, COMPLEX[] out) {
		COMPLEX sum = new COMPLEX();
		COMPLEX w = new COMPLEX();
		final double pi = 3.1415926535897932384626434D;
		sum.re = sum.im = 0.0F;
		for(int i = 0; i < n; ++i) {
			w.re = (float)Math.cos((2.0D * pi * (i * j % n)) / n);
			w.im = -(float)Math.sin((2.0D * pi * (i * j % n)) / n);
			sum.re += in[i].re * w.re - in[i].im * w.im;
			sum.im += in[i].im * w.re + in[i].re * w.im;
		}
		out[j] = sum;
	}
	public static void test_fft(int n, COMPLEX[] in, COMPLEX[] out) {
		for(int j = 0; j < n; ++j) 
			test_fft_elem(n, j, in, out);
	}
	public static void test_correctness() {
		double error;
		double a;
		COMPLEX[] in1 = allocate(MAX);
		COMPLEX[] in2 = allocate(MAX);
		COMPLEX[] out1 = allocate(MAX);
		COMPLEX[] out2 = allocate(MAX);
		for(int n = 1; n < MAX; ++n) {
			for(int i = 0; i < n; ++i) {
				in1[i].re = in2[i].re = i;
				in1[i].im = in2[i].im = 0.0F;
			}
			cilk_fft(n, in1, out1, true);
			test_fft(n, in2, out2);
			error = 0.0D;
			for(int i = 0; i < n; ++i) {
				double d;
				a = Math.sqrt((out1[i].re - out2[i].re) * (out1[i].re - out2[i].re) + (out1[i].im - out2[i].im) * (out1[i].im - out2[i].im));
				d = Math.sqrt(out2[i].re * out2[i].re + out2[i].im * out2[i].im);
				if(d < -1.0e-10D || d > 1.0e-10D) 
					a /= d;
				if(a > error) 
					error = a;
			}
			if(error > 1e-3D) {
				System.out.println("n=" + n + " error= " + error);
				System.out.println("CT:");
				for(int i = 0; i < n; ++i) 
					System.out.println(out2[i].re + " + " + out2[i].im);
				System.out.println("SEQ:");
				for(int i = 0; i < n; ++i) 
					System.out.println(out1[i].re + " + " + out1[i].im);
			}
			if(n % 10 == 0) 
				System.out.println("n=" + n + " OK");
		}
	}

	public static COMPLEX[] in, out, W;
	public static int[] factors;
	public static boolean allocation_done = false;

	public static COMPLEX[] allocate(int size) {
		final COMPLEX[] m = new COMPLEX[size];
		for(int i = 0; i < size; i++) {
			m[i] = new COMPLEX();
		}
		return m;
	}

	public static void allocate_speed(int size) {
		if(!allocation_done) {
			factors = new int[40];
			in = new COMPLEX[size];
			out = new COMPLEX[size];
			W = new COMPLEX[size+1];
			for(int i = 0; i < size; i++) {
				in[i] = new COMPLEX();
				out[i] = new COMPLEX();
				W[i] = new COMPLEX();
			}
			W[size] = new COMPLEX();
			allocation_done = true;
		}

		for(int i = 0; i < 40; i++) {
			factors[i] = 0;
		}
		for(int i = 0; i < size; i++) {
			W[i].re = 0;
			out[i].re = 0;
			W[i].im = 0;
			out[i].im = 0;
			in[i].re = 1.0F;
			in[i].im = 1.0F;
		}
		W[size].re = 0;
		W[size].im = 0;
	}

	public static void test_speed(int size) {
		allocate_speed(size);
		final long startTime = System.currentTimeMillis();
		cilk_fft(size, in, out, false);
		final long time = System.currentTimeMillis() - startTime;
		final float secs = ((float)time) / 1000.0F;
		System.out.println("Time: " + secs);
	}

	public static void usage() {
		System.out.println("This program is a highly optimized version of the classical");
		System.out.println("Cooley-Tukey Fast Fourier Transform algorithm.  Some documentation can");
		System.out.println("be found in the source code. The program is optimized for an exact");
		System.out.println("power of 2.  To test for correctness use parameter correctness=true.");
	}
	final static int MAX = 800;

	public static void main(String[] args) { 
		int n  = 1024;
		if(args.length > 0) n = Integer.parseInt(args[0]);
		System.out.println("Size = "+n+"X"+n);
		int size = n * n;
		boolean correctness = false;
		usage();
		boolean harnessStarted = false;

		int inner = 5;
		int outter = 3;
		if(args.length > 1) inner = Integer.parseInt(args[1]);
		if(args.length > 2) outter = Integer.parseInt(args[2]);
		
		final long start = System.nanoTime();
		for(int i=0;i <outter; i++) {
			if(i+1 == outter) {
				harnessStarted = true;
				org.mmtk.plan.Plan.harnessBegin();
				org.jikesrvm.scheduler.RVMThread.perfEventStart();
			}
			for(int j=0; j<inner; j++) {
				System.out.println("========================== ITERATION ("+i+"."+j+") ==================================");
				if(correctness) {
					System.out.println("Testing Correctness for Size = " + MAX);
					test_correctness();
				}
				else {
					System.out.println("Testing Speed for Size = " + size);
					test_speed(size);
					if(harnessStarted) {
						if(n==1024) {
							if(org.jikesrvm.scheduler.WS.wsTotalPush() != 46830) {
								System.out.println("EXITING DUE TO FAILURE IN HARNESS ITERATIONS");
								System.exit(-1);
							}
						}
					}
				}
				org.jikesrvm.scheduler.WS.dumpWSStatistics();
			}
		}

		System.out.println("Test Kernel under harness passed successfully....");
		
		org.jikesrvm.scheduler.RVMThread.perfEventStop();
		org.mmtk.plan.Plan.harnessEnd();

		final double duration = (((double)(System.nanoTime() - start))/((double)(1.0E9))) * 1000;
		System.out.printf("===== Test PASSED in %d msec =====\n",(int)duration);
	}
}
