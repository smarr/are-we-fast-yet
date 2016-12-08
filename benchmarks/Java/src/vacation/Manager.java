package vacation;

import cd.RedBlackTree;

/* =============================================================================
 *
 * Travel reservation resource manager
 *
 * =============================================================================
 *
 * Copyright (C) Stanford University, 2006.  All Rights Reserved.
 * Author: Chi Cao Minh
 *
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

public class Manager {

  private final RedBlackTree<Integer, Reservation> cars;
  private final RedBlackTree<Integer, Reservation> rooms;
  private final RedBlackTree<Integer, Reservation> flights;
  private final RedBlackTree<Integer, Customer>    customers;

  /*
   * ===========================================================================
   * == manager_alloc
   * ===========================================================================
   * ==
   */
  public Manager() {
    cars      = new RedBlackTree<>();
    rooms     = new RedBlackTree<>();
    flights   = new RedBlackTree<>();
    customers = new RedBlackTree<>();
  }

  public RedBlackTree<Integer, Reservation> getCars() {
    return cars;
  }

  public RedBlackTree<Integer, Reservation> getRooms() {
    return rooms;
  }

  public RedBlackTree<Integer, Reservation> getFlights() {
    return flights;
  }

  public RedBlackTree<Integer, Customer> getCustomers() {
    return customers;
  }

  /**
   * If 'num' > 0 then add, if < 0 remove.
   * Adding 0 seats is error if does not exist.
   * If 'price' < 0, do not update price.
   *
   * @return true on success, else false
   */
  private boolean addReservation(final RedBlackTree<Integer, Reservation> reservations, final int id,
      final int num, final int price) {
    synchronized (reservations) {
      Reservation reservation = reservations.get(id);
      if (reservation == null) {
        /* Create new reservation */
        if (num < 1 || price < 0) {
          return false;
        }
        reservation = new Reservation(id, num, price);
        reservations.put(id, reservation);
      } else {
        /* Update existing reservation */
        if (!reservation.addToTotal(num)) {
          return false;
        }
        if (reservation.numTotal == 0) {
          reservations.remove(id);
        } else {
          reservation.updatePrice(price);
        }
      }
    }

    return true;
  }

  /**
   * Add cars to a city.
   * Adding to an existing car overwrite the price if 'price' >= 0.
   *
   * @return true on success, else false
   */
  public boolean addCar(final int carId, final int numCars, final int price) {
    return addReservation(cars, carId, numCars, price);
  }

  /**
   * Delete cars from a city.
   *
   * Decreases available car count (those not allocated to a customer).
   * Fails if would make available car count negative.
   * If decreased to 0, deletes entire entry.
   *
   * @return true on success, else false
   */
  boolean deleteCar(final int carId, final int numCar) {
    /* -1 keeps old price */
    return addReservation(cars, carId, -numCar, -1);
  }

  /**
   * Add rooms to a city.
   *
   * Adding to an existing room overwrite the price if 'price' >= 0.
   *
   * @return true on success, else false
   */
  public boolean addRoom(final int roomId, final int numRoom,
      final int price) {
    return addReservation(rooms, roomId, numRoom, price);
  }

  /**
   * Delete rooms from a city.
   *
   * Decreases available room count (those not allocated to a customer).
   * Fails if would make available room count negative.
   * If decreased to 0, deletes entire entry.
   *
   * @return true on success, else false
   */
  boolean deleteRoom(final int roomId, final int numRoom) {
    /* -1 keeps old price */
    return addReservation(rooms, roomId, -numRoom, -1);
  }

  /**
   * Add seats to a flight.
   *
   * Adding to an existing flight overwrite the price if 'price' >= 0
   *
   * @return true on success, else false
   */
  public boolean addFlight(final int flightId, final int numSeat, final int price) {
    return addReservation(flights, flightId, numSeat, price);
  }

  /**
   * Delete an entire flight.
   *
   * Fails if customer has reservation on this flight.
   *
   * @return true on success, else false
   */
  boolean deleteFlight(final int flightId) {
    Reservation reservation = flights.get(flightId);
    if (reservation == null) {
      return false;
    }

    if (reservation.numUsed > 0) {
      return false; /* somebody has a reservation */
    }

    return addReservation(flights, flightId, -reservation.numTotal, -1 /* -1 keeps old price */);
  }

  /**
   * If customer already exists, returns failure.
   *
   * @return true on success, else false
   */
  public boolean addCustomer(final int customerId) {
    synchronized (customers) {
      if (customers.contains(customerId)) {
        return false;
      }

      Customer customer = new Customer(customerId);
      customers.put(customerId, customer);
    }

    return true;
  }

  /**
   * Delete this customer and associated reservations.
   *
   * If customer does not exist, returns success.
   *
   * @return true on success, else false
   */
  synchronized boolean deleteCustomer(final int customerId) {
    Customer customer = customers.get(customerId);
    if (customer == null) {
      return false;
    }

    @SuppressWarnings("unchecked")
    RedBlackTree<Integer, Reservation>[] reservations = new RedBlackTree[Defines.NUM_RESERVATION_TYPE];

    reservations[Defines.RESERVATION_CAR]    = cars;
    reservations[Defines.RESERVATION_ROOM]   = rooms;
    reservations[Defines.RESERVATION_FLIGHT] = flights;

    /* Cancel this customer's reservations */
    List reservationList = customer.reservations;
    ListNode it = reservationList.head;
    while (it.next != null) {
      it = it.next;
      ReservationInfo reservation = it.data;
      Reservation resrv = reservations[reservation.type].get(reservation.id);
      resrv.cancel();
    }

    customers.remove(customerId);
    return true;
  }

  /**
   * @return numFree of a reservation, -1 if failure
   */
  int queryNumFree(final RedBlackTree<Integer, Reservation> table, final int id) {
    int numFree = -1;
    Reservation reservation = table.get(id);
    if (reservation != null) {
      numFree = reservation.numFree;
    }

    return numFree;
  }

  /**
   * @return price of a reservation, -1 if failure
   */
  int queryPrice(final RedBlackTree<Integer, Reservation> table, final int id) {
    int price = -1;
    Reservation reservation = table.get(id);
    if (reservation != null) {
      price = reservation.price;
    }

    return price;
  }

  /**
   * @return the number of empty seats on a car, returns -1 if the car does not exist
   */
  int queryCar(final int carId) {
    return queryNumFree(cars, carId);
  }

  /**
   * @return the price of the car, returns -1 if the car does not exist
   */
  int queryCarPrice(final int carId) {
    return queryPrice(cars, carId);
  }

  /**
   * @return the number of empty seats on a room, returns -1 if the room does not exist
   */
  int queryRoom(final int roomId) {
    return queryNumFree(rooms, roomId);
  }

  /**
   * @return the price of the room, returns -1 if the room does not exist
   */
  int queryRoomPrice(final int roomId) {
    return queryPrice(rooms, roomId);
  }

  /**
   * @return the number of empty seats on a flight, returns -1 if the flight does not exist
   */
  int queryFlight(final int flightId) {
    return queryNumFree(flights, flightId);
  }

  /**
   * @return the price of the flight, returns -1 if the flight does not exist
   */
  int queryFlightPrice(final int flightId) {
    return queryPrice(flights, flightId);
  }

  /**
   * @return the total price of all reservations held for a customer, returns -1 if the customer does not exist
   */
  int queryCustomerBill(final int customerId) {
    int bill = -1;
    Customer customer = customers.get(customerId);

    if (customer != null) {
      bill = customer.getBill();
    }

    return bill;
  }

  /**
   * Customer is not allowed to reserve same (type, id) multiple times.
   *
   * @returns true on success, else false
   */
  static boolean reserve(final RedBlackTree<Integer, Reservation> table,
      final RedBlackTree<Integer, Customer> customers, final int customerId, final int id, final int type) {
    Customer customer = customers.get(customerId);
    if (customer == null) {
      return false;
    }

    Reservation reservation = table.get(id);
    if (reservation == null) {
      return false;
    }

    if (!reservation.makeReservation()) {
      return false;
    }

    if (!customer.addReservationInfo(type, id, reservation.price)) {
      /* Undo previous successful reservation */
      reservation.cancel();
      return false;
    }

    return true;
  }

  /**
   * @return failure if the car or customer does not exist
   */
  boolean reserveCar(final int customerId, final int carId) {
    return reserve(cars, customers, customerId, carId,
        Defines.RESERVATION_CAR);
  }

  /**
   * @return failure if the room or customer does not exist
   */
  boolean reserveRoom(final int customerId, final int roomId) {
    return reserve(rooms, customers, customerId, roomId,
        Defines.RESERVATION_ROOM);
  }

  /**
   * @return failure if the flight or customer does not exist
   */
  boolean reserveFlight(final int customerId, final int flightId) {
    return reserve(flights, customers, customerId, flightId,
        Defines.RESERVATION_FLIGHT);
  }
}
