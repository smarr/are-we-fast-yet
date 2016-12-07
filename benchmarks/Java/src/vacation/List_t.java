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

public class List_t {

  public List_Node head;
  int              size;

  public List_t() {
    head = new List_Node();
  }

  /*
   * =======================================================================
   * allocNode -- Returns null on failure
   * =======================================================================
   */
  private List_Node allocNode(final Object dataPtr) {
    List_Node nodePtr = new List_Node();

    nodePtr.dataPtr = dataPtr;
    nodePtr.nextPtr = null;

    return nodePtr;
  }

  /*
   * ===========================================================================
   * == list_alloc -- If NULL passed for 'compare' function, will compare data
   * pointer addresses -- Returns NULL on failure
   * ===========================================================================
   * == list_t* list_alloc (long (*compare)(const void*, const void*));
   *
   *
   */

  public static List_t alloc() {
    List_t listPtr = new List_t();

    listPtr.head.dataPtr = null;
    listPtr.head.nextPtr = null;
    listPtr.size = 0;

    return listPtr;
  }

  /*
   * ===========================================================================
   * == list_free -- If NULL passed for 'compare' function, will compare data
   * pointer addresses -- Returns NULL on failure
   * ===========================================================================
   * == void list_free (list_t* listPtr);
   */
  public static void free(List_t listPtr) {
    listPtr = null;
  }

  // privae freeList

  /*
   * ===========================================================================
   * == list_isEmpty -- Return TRUE if list is empty, else FALSE
   * ===========================================================================
   * == bool_t list_isEmpty (list_t* listPtr);
   */
  public boolean isEmpty() {
    return (head.nextPtr == null);
  }

  /*
   * ===========================================================================
   * == list_getSize -- Returns size of list
   * ===========================================================================
   * == long list_getSize (list_t* listPtr);
   */
  public int getSize() {
    return size;
  }

  /*
   * ===========================================================================
   * == findPrevious
   * ===========================================================================
   * == void* list_find (list_t* listPtr, void* dataPtr);
   */
  private List_Node findPrevious(final Object dataPtr) {
    List_Node prevPtr = head;
    List_Node nodePtr = prevPtr.nextPtr;

    for (; nodePtr != null; nodePtr = nodePtr.nextPtr) {
      if (compare(nodePtr.dataPtr, dataPtr) >= 0) {
        return prevPtr;
      }
      prevPtr = nodePtr;
    }

    return prevPtr;
  }

  /*
   * ===========================================================================
   * == list_find -- Returns NULL if not found, else returns pointer to data
   * ===========================================================================
   * == void* list_find (list_t* listPtr, void* dataPtr);
   */
  public Object find(final Object dataPtr) {
    List_Node nodePtr;
    List_Node prevPtr = findPrevious(dataPtr);

    nodePtr = prevPtr.nextPtr;

    if ((nodePtr == null) || (compare(nodePtr.dataPtr, dataPtr) != 0)) {
      return null;
    }

    return (nodePtr.dataPtr);
  }

  public int compare(final Object obj1, final Object obj2) {
    ReservationInfo aPtr = (ReservationInfo) obj1;
    ReservationInfo bPtr = (ReservationInfo) obj2;
    int typeDiff;

    typeDiff = aPtr.type - bPtr.type;

    return ((typeDiff != 0) ? (typeDiff) : (aPtr.id - bPtr.id));
  }

  /*
   * ===========================================================================
   * == list_insert -- Return TRUE on success, else FALSE
   * ===========================================================================
   * == bool_t list_insert (list_t* listPtr, void* dataPtr);
   */
  public boolean insert(final Object dataPtr) {
    List_Node prevPtr;
    List_Node nodePtr;
    List_Node currPtr;

    prevPtr = findPrevious(dataPtr);
    currPtr = prevPtr.nextPtr;

    nodePtr = allocNode(dataPtr);
    if (nodePtr == null) {
      return false;
    }

    nodePtr.nextPtr = currPtr;
    prevPtr.nextPtr = nodePtr;
    size++;

    return true;
  }

  /*
   * ===========================================================================
   * == list_remove -- Returns TRUE if successful, else FALSE
   * ===========================================================================
   * == bool_t list_remove (list_t* listPtr, void* dataPtr);
   */
  public boolean remove(final Object dataPtr) {
    List_Node prevPtr;
    List_Node nodePtr;

    prevPtr = findPrevious(dataPtr);

    nodePtr = prevPtr.nextPtr;

    if ((nodePtr != null) && (compare(nodePtr.dataPtr, dataPtr) == 0)) {
      prevPtr.nextPtr = nodePtr.nextPtr;
      nodePtr.nextPtr = null;
      nodePtr = null;
      size--;

      return true;
    }

    return false;
  }

  int compareObject(final Object obj1, final Object obj2) {
    return 1;
  }

  /*
   * ===========================================================================
   * == list_clear -- Removes all elements
   * ===========================================================================
   * == void list_clear (list_t* listPtr);
   */
  public void clear() {
    head = new List_Node();
    size = 0;
  }
}
