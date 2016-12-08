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
 *
 * =============================================================================
 */
import vacation.Barrier;
import vacation.Client;
import vacation.Defines;
import vacation.Manager;
import vacation.RBTree;
import vacation.Random;

public class Vacation extends Benchmark {

  @Override
  public boolean innerBenchmarkLoop(final int problemSize, final int numThreads) {
    Manager manager = initializeManager();
    Client[] clients = initializeClients(manager, numThreads);

    /* Run transactions */
    Barrier.setBarrier(numThreads + 1);

    for (int i = 0; i < numThreads; i++) {
      clients[i].start();
    }

    Barrier.enterBarrier();
    Barrier.enterBarrier();
    Barrier.assertIsClear();

    return checkTables(manager);
  }

  private static Manager initializeManager() {
    Random  random  = new Random();
    Manager manager = new Manager();

    int numRelation = Defines.PARAM_DEFAULT_RELATIONS;
    int[] ids = new int[numRelation];
    for (int i = 0; i < numRelation; i++) {
      ids[i] = i + 1;
    }

    for (int t = 0; t < 4; t++) {
      /* Shuffle ids */
      for (int i = 0; i < numRelation; i++) {
        int x = random.posrandom_generate() % numRelation;
        int y = random.posrandom_generate() % numRelation;
        int tmp = ids[x];
        ids[x] = ids[y];
        ids[y] = tmp;
      }

      /* Populate table */
      for (int i = 0; i < numRelation; i++) {
        int id = ids[i];
        int num = ((random.posrandom_generate() % 5) + 1) * 100;
        int price = ((random.posrandom_generate() % 5) * 10) + 50;
        if (t == 0) {
          manager.addCar(id, num, price);
        } else if (t == 1) {
          manager.addFlight(id, num, price);
        } else if (t == 2) {
          manager.addRoom(id, num, price);
        } else if (t == 3) {
          manager.addCustomer(id);
        }
      }
    }
    return manager;
  }

  private static Client[] initializeClients(final Manager manager, final int numClients) {
    int numTransaction = Defines.PARAM_DEFAULT_TRANSACTIONS;
    int numRelation = Defines.PARAM_DEFAULT_RELATIONS;
    int percentQuery = Defines.PARAM_DEFAULT_QUERIES;
    Client[] clients = new Client[numClients];

    int numTransactionPerClient = (int) ((double) numTransaction / (double) numClients + 0.5);
    int queryRange = (int) (percentQuery / 100.0 * numRelation + 0.5);

    for (int i = 0; i < numClients; i++) {
      clients[i] = new Client(i, manager, numTransactionPerClient,
          Defines.PARAM_DEFAULT_NUMBER, queryRange, Defines.PARAM_DEFAULT_USER);
    }

    return clients;
  }

  private static boolean checkTables(final Manager manager) {
    int numRelation = Defines.PARAM_DEFAULT_RELATIONS;
    RBTree customerTablePtr = manager.getCustomers();
    RBTree[] tables = new RBTree[] {
        manager.getCars(), manager.getFlights(), manager.getRooms()};

    /* Check for unique customer IDs */
    int percentQuery = Defines.PARAM_DEFAULT_QUERIES;
    int queryRange = (int) (percentQuery / 100.0 * numRelation + 0.5);
    int maxCustomerId = queryRange + 1;
    for (int i = 1; i <= maxCustomerId; i++) {
      if (customerTablePtr.find(i) != null) {
        if (!customerTablePtr.remove(i)) {
          return false;
        }
      }
    }

    /* Check reservation tables for consistency and unique ids */
    for (int t = 0; t < tables.length; t++) {
      RBTree tablePtr = tables[t];
      for (int i = 1; i <= numRelation; i++) {
        if (tablePtr.find(i) != null) {
          if (t == 0) {
            if (!manager.addCar(i, 0, 0)) {
              return false;
            }
          } else if (t == 1) {
            if (!manager.addFlight(i, 0, 0)) {
              return false;
            }
          } else if (t == 2) {
            if (!manager.addRoom(i, 0, 0)) {
              return false;
            }
          }
          if (!tablePtr.remove(i)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public Object benchmark() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean verifyResult(final Object result) {
    throw new UnsupportedOperationException();
  }
}
