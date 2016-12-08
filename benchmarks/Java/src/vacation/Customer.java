package vacation;

/*
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
 */

public class Customer {

  private final int id;
  final List reservations;

  public Customer(final int id) {
    this.id = id;
    reservations = new List();
  }

  boolean addReservationInfo(final int type, final int id,
      final int price) {
    ReservationInfo reservation = new ReservationInfo(type, id, price);
    return reservations.insert(reservation);
  }

  boolean removeReservationInfo(final int type, final int id) {
    ReservationInfo findReservationInfo = new ReservationInfo(type, id, 0);
    ReservationInfo reservation = (ReservationInfo) reservations.find(findReservationInfo);

    if (reservation == null) {
      return false;
    }

    reservations.remove(findReservationInfo);
    return true;
  }

  /**
   * @return total cost of reservations
   */
  synchronized int getBill() {
    int bill = 0;
    ListNode it;

    it = reservations.head;
    while (it.next != null) {
      it = it.next;
      ReservationInfo reservation = it.data;
      bill += reservation.price;
    }

    return bill;
  }
}
