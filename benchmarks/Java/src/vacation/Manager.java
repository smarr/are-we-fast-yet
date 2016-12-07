package vacation;

/* =============================================================================
 *
 * manager.c
 * -- Travel reservation resource manager
 *
 * =============================================================================
 *
 * Copyright (C) Stanford University, 2006.  All Rights Reserved.
 * Author: Chi Cao Minh
 *
 * =============================================================================
 *
 * For the license of bayes/sort.h and bayes/sort.c, please see the header
 * of the files.
 *
 * ------------------------------------------------------------------------
 *
 * For the license of kmeans, please see kmeans/LICENSE.kmeans
 *
 * ------------------------------------------------------------------------
 *
 * For the license of ssca2, please see ssca2/COPYRIGHT
 *
 * ------------------------------------------------------------------------
 *
 * For the license of lib/mt19937ar.c and lib/mt19937ar.h, please see the
 * header of the files.
 *
 * ------------------------------------------------------------------------
 *
 * For the license of lib/rbtree.h and lib/rbtree.c, please see
 * lib/LEGALNOTICE.rbtree and lib/LICENSE.rbtree
 *
 * ------------------------------------------------------------------------
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

/* =============================================================================
 * DECLARATION OF TM_CALLABLE FUNCTIONS
 * =============================================================================
 */
public class Manager {

  RBTree carTablePtr;
  RBTree roomTablePtr;
  RBTree flightTablePtr;
  RBTree customerTablePtr;

  /*
   * ===========================================================================
   * == manager_alloc
   * ===========================================================================
   * ==
   */
  public Manager() {
    carTablePtr = new RBTree();
    roomTablePtr = new RBTree();
    flightTablePtr = new RBTree();
    customerTablePtr = new RBTree();
  }

  /*
   * ===========================================================================
   * == addReservation -- If 'num' > 0 then add, if < 0 remove -- Adding 0 seats
   * is error if does not exist -- If 'price' < 0, do not update price --
   * Returns TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean addReservation(final RBTree tablePtr, final int id, final int num,
      final int price) {
    Reservation reservationPtr;

    reservationPtr = (Reservation) tablePtr.find(id);
    if (reservationPtr == null) {
      /* Create new reservation */
      if (num < 1 || price < 0) {
        return false;
      }
      reservationPtr = new Reservation(id, num, price);
      // assert(reservationPtr != NULL);
      tablePtr.insert(id, reservationPtr);
    } else {
      /* Update existing reservation */
      if (!reservationPtr.reservation_addToTotal(num)) {
        return false;
      }
      if (reservationPtr.numTotal == 0) {
        boolean status = tablePtr.remove(id);
      } else {
        reservationPtr.reservation_updatePrice(price);
      }
    }

    return true;
  }

  /*
   * ===========================================================================
   * == manager_addCar -- Add cars to a city -- Adding to an existing car
   * overwrite the price if 'price' >= 0 -- Returns TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean manager_addCar(final int carId, final int numCars, final int price) {
    return addReservation(carTablePtr, carId, numCars, price);
  }

  /*
   * ===========================================================================
   * == manager_deleteCar -- Delete cars from a city -- Decreases available car
   * count (those not allocated to a customer) -- Fails if would make available
   * car count negative -- If decresed to 0, deletes entire entry -- Returns
   * TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean manager_deleteCar(final int carId, final int numCar) {
    /* -1 keeps old price */
    return addReservation(carTablePtr, carId, -numCar, -1);
  }

  /*
   * ===========================================================================
   * == manager_addRoom -- Add rooms to a city -- Adding to an existing room
   * overwrite the price if 'price' >= 0 -- Returns TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean manager_addRoom(final int roomId, final int numRoom,
      final int price) {
    return addReservation(roomTablePtr, roomId, numRoom, price);
  }

  /*
   * ===========================================================================
   * == manager_deleteRoom -- Delete rooms from a city -- Decreases available
   * room count (those not allocated to a customer) -- Fails if would make
   * available room count negative -- If decresed to 0, deletes entire entry --
   * Returns TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean manager_deleteRoom(final int roomId, final int numRoom) {
    /* -1 keeps old price */
    return addReservation(roomTablePtr, roomId, -numRoom, -1);
  }

  /*
   * ===========================================================================
   * == manager_addFlight -- Add seats to a flight -- Adding to an existing
   * flight overwrite the price if 'price' >= 0 -- Returns TRUE on success,
   * FALSE on failure
   * ===========================================================================
   * ==
   */
  boolean manager_addFlight(final int flightId, final int numSeat,
      final int price) {
    return addReservation(flightTablePtr, flightId, numSeat, price);
  }

  /*
   * ===========================================================================
   * == manager_deleteFlight -- Delete an entire flight -- Fails if customer has
   * reservation on this flight -- Returns TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean manager_deleteFlight(final int flightId) {
    Reservation reservationPtr = (Reservation) flightTablePtr.find(flightId);
    if (reservationPtr == null) {
      return false;
    }

    if (reservationPtr.numUsed > 0) {
      return false; /* somebody has a reservation */
    }

    return addReservation(flightTablePtr, flightId, -reservationPtr.numTotal,
        -1 /* -1 keeps old price */);
  }

  /*
   * ===========================================================================
   * == manager_addCustomer -- If customer already exists, returns failure --
   * Returns TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean manager_addCustomer(final int customerId) {
    Customer customerPtr;
    boolean status;

    if (customerTablePtr.contains(customerId)) {
      return false;
    }

    customerPtr = new Customer(customerId);
    // assert(customerPtr != null);
    status = customerTablePtr.insert(customerId, customerPtr);

    return true;
  }

  /*
   * ===========================================================================
   * == manager_deleteCustomer -- Delete this customer and associated
   * reservations -- If customer does not exist, returns success -- Returns TRUE
   * on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean manager_deleteCustomer(final int customerId) {
    Customer customerPtr;
    RBTree reservationTables[] = new RBTree[Defines.NUM_RESERVATION_TYPE];
    List_t reservationInfoListPtr;
    List_Node it;
    boolean status;

    customerPtr = (Customer) customerTablePtr.find(customerId);
    if (customerPtr == null) {
      return false;
    }

    reservationTables[Defines.RESERVATION_CAR] = carTablePtr;
    reservationTables[Defines.RESERVATION_ROOM] = roomTablePtr;
    reservationTables[Defines.RESERVATION_FLIGHT] = flightTablePtr;

    /* Cancel this customer's reservations */
    reservationInfoListPtr = customerPtr.reservationInfoListPtr;
    it = reservationInfoListPtr.head;
    while (it.nextPtr != null) {
      ReservationInfo reservationInfoPtr;
      Reservation reservationPtr;
      it = it.nextPtr;
      reservationInfoPtr = (ReservationInfo) it.dataPtr;
      reservationPtr = (Reservation) reservationTables[reservationInfoPtr.type]
          .find(reservationInfoPtr.id);
      status = reservationPtr.reservation_cancel();
    }

    status = customerTablePtr.remove(customerId);
    return true;
  }

  /*
   * ===========================================================================
   * == QUERY INTERFACE
   * ===========================================================================
   * ==
   */

  /*
   * ===========================================================================
   * == queryNumFree -- Return numFree of a reservation, -1 if failure
   * ===========================================================================
   * ==
   */
  int queryNumFree(final RBTree tablePtr, final int id) {
    int numFree = -1;
    Reservation reservationPtr = (Reservation) tablePtr.find(id);
    if (reservationPtr != null) {
      numFree = reservationPtr.numFree;
    }

    return numFree;
  }

  /*
   * ===========================================================================
   * == queryPrice -- Return price of a reservation, -1 if failure
   * ===========================================================================
   * ==
   */
  int queryPrice(final RBTree tablePtr, final int id) {
    int price = -1;
    Reservation reservationPtr = (Reservation) tablePtr.find(id);
    if (reservationPtr != null) {
      price = reservationPtr.price;
    }

    return price;
  }

  /*
   * ===========================================================================
   * == manager_queryCar -- Return the number of empty seats on a car -- Returns
   * -1 if the car does not exist
   * ===========================================================================
   * ==
   */
  int manager_queryCar(final int carId) {
    return queryNumFree(carTablePtr, carId);
  }

  /*
   * ===========================================================================
   * == manager_queryCarPrice -- Return the price of the car -- Returns -1 if
   * the car does not exist
   * ===========================================================================
   * ==
   */
  int manager_queryCarPrice(final int carId) {
    return queryPrice(carTablePtr, carId);
  }

  /*
   * ===========================================================================
   * == manager_queryRoom -- Return the number of empty seats on a room --
   * Returns -1 if the room does not exist
   * ===========================================================================
   * ==
   */
  int manager_queryRoom(final int roomId) {
    return queryNumFree(roomTablePtr, roomId);
  }

  /*
   * ===========================================================================
   * == manager_queryRoomPrice -- Return the price of the room -- Returns -1 if
   * the room does not exist
   * ===========================================================================
   * ==
   */
  int manager_queryRoomPrice(final int roomId) {
    return queryPrice(roomTablePtr, roomId);
  }

  /*
   * ===========================================================================
   * == manager_queryFlight -- Return the number of empty seats on a flight --
   * Returns -1 if the flight does not exist
   * ===========================================================================
   * ==
   */
  int manager_queryFlight(final int flightId) {
    return queryNumFree(flightTablePtr, flightId);
  }

  /*
   * ===========================================================================
   * == manager_queryFlightPrice -- Return the price of the flight -- Returns -1
   * if the flight does not exist
   * ===========================================================================
   * ==
   */
  int manager_queryFlightPrice(final int flightId) {
    return queryPrice(flightTablePtr, flightId);
  }

  /*
   * ===========================================================================
   * == manager_queryCustomerBill -- Return the total price of all reservations
   * held for a customer -- Returns -1 if the customer does not exist
   * ===========================================================================
   * ==
   */
  int manager_queryCustomerBill(final int customerId) {
    int bill = -1;
    Customer customerPtr;

    customerPtr = (Customer) customerTablePtr.find(customerId);

    if (customerPtr != null) {
      bill = customerPtr.customer_getBill();
    }

    return bill;
  }

  /*
   * ===========================================================================
   * == RESERVATION INTERFACE
   * ===========================================================================
   * ==
   */

  /*
   * ===========================================================================
   * == reserve -- Customer is not allowed to reserve same (type, id) multiple
   * times -- Returns TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  static boolean reserve(final RBTree tablePtr, final RBTree customerTablePtr,
      final int customerId, final int id, final int type) {
    Customer customerPtr;
    Reservation reservationPtr;

    customerPtr = (Customer) customerTablePtr.find(customerId);

    if (customerPtr == null) {
      return false;
    }

    reservationPtr = (Reservation) tablePtr.find(id);
    if (reservationPtr == null) {
      return false;
    }

    if (!reservationPtr.reservation_make()) {
      return false;
    }

    if (!customerPtr.customer_addReservationInfo(type, id,
        reservationPtr.price)) {
      /* Undo previous successful reservation */
      boolean status = reservationPtr.reservation_cancel();
      return false;
    }

    return true;
  }

  /*
   * ===========================================================================
   * == manager_reserveCar -- Returns failure if the car or customer does not
   * exist -- Returns TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean manager_reserveCar(final int customerId, final int carId) {
    return reserve(carTablePtr, customerTablePtr, customerId, carId,
        Defines.RESERVATION_CAR);
  }

  /*
   * ===========================================================================
   * == manager_reserveRoom -- Returns failure if the room or customer does not
   * exist -- Returns TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean manager_reserveRoom(final int customerId, final int roomId) {
    return reserve(roomTablePtr, customerTablePtr, customerId, roomId,
        Defines.RESERVATION_ROOM);
  }

  /*
   * ===========================================================================
   * == manager_reserveFlight -- Returns failure if the flight or customer does
   * not exist -- Returns TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean manager_reserveFlight(final int customerId, final int flightId) {
    return reserve(flightTablePtr, customerTablePtr, customerId, flightId,
        Defines.RESERVATION_FLIGHT);
  }

  /*
   * ===========================================================================
   * == cancel -- Customer is not allowed to cancel multiple times -- Returns
   * TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  static boolean cancel(final RBTree tablePtr, final RBTree customerTablePtr,
      final int customerId, final int id, final int type) {
    Customer customerPtr;
    Reservation reservationPtr;

    customerPtr = (Customer) customerTablePtr.find(customerId);
    if (customerPtr == null) {
      return false;
    }

    reservationPtr = (Reservation) tablePtr.find(id);
    if (reservationPtr == null) {
      return false;
    }

    if (!reservationPtr.reservation_cancel()) {
      return false;
    }

    if (!customerPtr.customer_removeReservationInfo(type, id)) {
      /* Undo previous successful cancellation */
      boolean status = reservationPtr.reservation_make();
      return false;
    }

    return true;
  }

  /*
   * ===========================================================================
   * == manager_cancelCar -- Returns failure if the car, reservation, or
   * customer does not exist -- Returns TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean manager_cancelCar(final int customerId, final int carId) {
    return cancel(carTablePtr, customerTablePtr, customerId, carId,
        Defines.RESERVATION_CAR);
  }

  /*
   * ===========================================================================
   * == manager_cancelRoom -- Returns failure if the room, reservation, or
   * customer does not exist -- Returns TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean manager_cancelRoom(final int customerId, final int roomId) {
    return cancel(roomTablePtr, customerTablePtr, customerId, roomId,
        Defines.RESERVATION_ROOM);
  }

  /*
   * ===========================================================================
   * == manager_cancelFlight -- Returns failure if the flight, reservation, or
   * customer does not exist -- Returns TRUE on success, else FALSE
   * ===========================================================================
   * ==
   */
  boolean manager_cancelFlight(final int customerId, final int flightId) {
    return cancel(flightTablePtr, customerTablePtr, customerId, flightId,
        Defines.RESERVATION_FLIGHT);
  }
}
