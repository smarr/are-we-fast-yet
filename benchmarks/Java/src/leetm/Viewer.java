/*
 * BSD License
 *
 * Copyright (c) 2007, The University of Manchester (UK)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     - Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     - Neither the name of the University of Manchester nor the names
 *       of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written
 *       permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package leetm;

/*
 * Viewer.java
 */


import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class Viewer extends Frame {
	private static final long serialVersionUID = 9191414219415062372L;

	private BufferedImage image;

    public Viewer() {
        image = new BufferedImage(600,600,1); //TYPE_INT_RGB
	image.flush();
	addWindowListener(new WindowAdapter() {
	    @Override
		public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});
	setSize(image.getWidth(null), image.getHeight(null)+30);
	setTitle("Picture");
    }

    public void drawSquare(int x1,int y1,int x2,int y2,int col){
        for (int i=x1; i<=x2; i++)
            for(int j=y1; j<=y2; j++)
                image.setRGB(i,j,col);
    }

    @Override
	public void paint(Graphics graphics) {
	graphics.drawImage(image, 0, 30, null);
    }

    public void display(){
      setVisible(true);
    }

    public void pad(int x,int y,int col){
      drawSquare(x-1,y-1,x+1,y+1,col);
    }

    public void point(int x,int y,int col){
      image.setRGB(x,y,col);
    }
}
