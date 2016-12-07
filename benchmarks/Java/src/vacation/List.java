package vacation;

/* =============================================================================
 *
 * List_t.java
 * -- Sorted singly linked list
 * -- Options: duplicate allowed
 *    (DLIST_NO_DUPLICATES) is no implemented yet (default: allow duplicates)
 *
 * =============================================================================
 *
 * Copyright (C) Stanford University, 2006.  All Rights Reserved.
 * Author: Chi Cao Minh
 * * Unless otherwise noted, the following license applies to STAMP files:
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

public class List {

  public ListNode head;
  int             size;

  public List() {
    head = new ListNode(null);
  }

  public boolean isEmpty() {
    return head.next == null;
  }

  public int getSize() {
    return size;
  }

  private ListNode findPrevious(final ReservationInfo data) {
    ListNode prev = head;
    ListNode node = prev.next;

    for (; node != null; node = node.next) {
      if (ReservationInfo.compare(node.data, data) >= 0) {
        return prev;
      }
      prev = node;
    }

    return prev;
  }

  public Object find(final ReservationInfo data) {
    ListNode prev = findPrevious(data);
    ListNode node = prev.next;

    if (node == null || ReservationInfo.compare(node.data, data) != 0) {
      return null;
    }

    return node.data;
  }

  public boolean insert(final ReservationInfo data) {
    ListNode prev = findPrevious(data);
    ListNode node = new ListNode(data);
    ListNode curr = prev.next;

    node.next = curr;
    prev.next = node;
    size++;

    return true;
  }

  public boolean remove(final ReservationInfo data) {
    ListNode prev = findPrevious(data);
    ListNode node = prev.next;

    if (node != null && ReservationInfo.compare(node.data, data) == 0) {
      prev.next = node.next;
      node.next = null;
      node = null;
      size--;

      return true;
    }
    return false;
  }
}
