package vacation;

/* =============================================================================
 *
 * reservation.c
 * -- Representation of car, flight, and hotel relations
 *
 * =============================================================================
 *
 * Copyright (C) Stanford University, 2006.  All Rights Reserved.
 * Author: Chi Cao Minh
 *
 * Unless otherwise noted, the following license applies to STAMP files:
 *
 * Copyright (c) 2007, Stanford University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *     * Neither the name of Stanford University nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY STANFORD UNIVERSITY ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL STANFORD UNIVERSITY BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 * =============================================================================
 */

public class Reservation {

  final int id;
  int numUsed;
  int numFree;
  int numTotal;
  int price;

  public Reservation(final int id, final int numTotal, final int price) {
    this.id = id;
    this.numUsed = 0;
    this.numFree = numTotal;
    this.numTotal = numTotal;
    this.price = price;
    checkReservation();
  }

  /**
   * Check if consistent.
   */
  // TODO: unclear how this works, is that an STM thing?
  public void checkReservation() {
    int numUsed = this.numUsed;
    int numFree = this.numFree;
    int numTotal = this.numTotal;
    int price = this.price;
  }

  /**
   * Adds if 'num' > 0, removes if 'num' < 0.
   *
   * @return true on success, else false
   */
  synchronized boolean addToTotal(final int num) {
    if (numFree + num < 0) {
      return false;
    }

    numFree += num;
    numTotal += num;
    checkReservation();
    return true;
  }

  /**
   * @return true on success, else false
   */
  public synchronized boolean makeReservation() {
    if (numFree < 1) {
      return false;
    }
    numUsed++;
    numFree--;
    checkReservation();
    return true;
  }

  /**
   * @return true on success, else false
   */
  synchronized boolean cancel() {
    if (numUsed < 1) {
      return false;
    }
    numUsed--;
    numFree++;
    checkReservation();
    return true;
  }

  /**
   * Failure if 'price' < 0.
   *
   *  @return true on success, else false
   */
  synchronized boolean updatePrice(final int newPrice) {
    if (newPrice < 0) {
      return false;
    }

    this.price = newPrice;
    checkReservation();
    return true;
  }

  /**
   * @return -1 if A < B, 0 if A = B, 1 if A > B
   */
  int compare(final Reservation a, final Reservation b) {
    return a.id - b.id;
  }

  int hash() {
    return id;
  }
}
