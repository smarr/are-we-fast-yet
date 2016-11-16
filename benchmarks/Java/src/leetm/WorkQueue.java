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

public class WorkQueue {

  public int x1;
  public int y1;
  public int x2;
  public int y2;
  public int nn;

  public WorkQueue next;

  WorkQueue() {
    next = null;
  }

  WorkQueue(final int xx1, final int yy1, final int xx2, final int yy2, final int n) {
    x1 = xx1;
    y1 = yy1;
    x2 = xx2;
    y2 = yy2;
    nn = n;
  }

  public WorkQueue enQueue(final int x1, final int y1, final int x2, final int y2, final int n) {
    WorkQueue q = new WorkQueue(x1, y1, x2, y2, n);
    q.next = this.next;
    return q;
  }

  public WorkQueue deQueue() {
    WorkQueue q = this.next;
    this.next = this.next.next;
    return q;
  }

  public boolean less(final int xx1, final int yy1, final int xx2, final int yy2) {
    return (x2 - x1) * (x2 - x1)
        + (y2 - y1) * (y2 - y1) > (xx2 - xx1) * (xx2 - xx1)
            + (yy2 - yy1) * (yy2 - yy1);
  }

  public boolean pass() {
    boolean done = true;
    WorkQueue ent = this;
    WorkQueue a = ent.next;
    while (a.next != null) {
      WorkQueue b = a.next;
      if (a.less(b.x1, b.y1, b.x2, b.y2)) {
        ent.next = b;
        a.next = b.next;
        b.next = a;
        done = false;
      }
      ent = a;
      a = b;
      b = b.next;
      // System.out.print("#");
    }
    return done;
  }

  public void sort() {
    while (!pass()) {
      ;
    }
  }

  public WorkQueue enQueue(final WorkQueue q) {
    WorkQueue n = new WorkQueue(q.x1, q.y1, q.x2, q.y2, q.nn);
    n.next = this.next;
    return n;
  }

  public int length() {
    WorkQueue curr = this.next;
    int retval = 0;

    while (curr != null) {
      retval++;
      curr = curr.next;
    }
    return retval;
  }
}
