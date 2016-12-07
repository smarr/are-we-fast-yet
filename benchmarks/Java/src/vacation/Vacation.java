package vacation;

/* =============================================================================
 *
 * vacation.c
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

public class Vacation {

  /*
   * ===========================================================================
   * == displayUsage
   * ===========================================================================
   * ==
   */
  public Vacation() {}

  public static void displayUsage(final String appName) {
    System.out.println("Usage: %s [options]\n" + appName);
    System.out.println(
        "\nOptions:                                             (defaults)\n");
    System.out
        .println("    c <UINT>   Number of [c]lients                   (%i)\n"
            + Defines.PARAM_DEFAULT_CLIENTS);
    System.out
        .println("    n <UINT>   [n]umber of user queries/transaction  (%i)\n"
            + Defines.PARAM_DEFAULT_NUMBER);
    System.out
        .println("    q <UINT>   Percentage of relations [q]ueried     (%i)\n"
            + Defines.PARAM_DEFAULT_QUERIES);
    System.out
        .println("    r <UINT>   Number of possible [r]elations        (%i)\n"
            + Defines.PARAM_DEFAULT_RELATIONS);
    System.out
        .println("    t <UINT>   Number of [t]ransactions              (%i)\n"
            + Defines.PARAM_DEFAULT_TRANSACTIONS);
    System.out
        .println("    u <UINT>   Percentage of [u]ser transactions     (%i)\n"
            + Defines.PARAM_DEFAULT_USER);
    System.exit(1);
  }

  /*
   * ===========================================================================
   * == setDefaultParams
   * ===========================================================================
   * ==
   */

  int CLIENTS;
  int NUMBER;
  int QUERIES;
  int RELATIONS;
  int TRANSACTIONS;
  int USER;

  public void setDefaultParams() {
    CLIENTS = Defines.PARAM_DEFAULT_CLIENTS;
    NUMBER = Defines.PARAM_DEFAULT_NUMBER;
    QUERIES = Defines.PARAM_DEFAULT_QUERIES;
    RELATIONS = Defines.PARAM_DEFAULT_RELATIONS;
    TRANSACTIONS = Defines.PARAM_DEFAULT_TRANSACTIONS;
    USER = Defines.PARAM_DEFAULT_USER;
  }

  /*
   * ===========================================================================
   * == parseArgs
   * ===========================================================================
   * ==
   */
  public void parseArgs(final String argv[]) {
    int opterr = 0;

    setDefaultParams();
    for (int i = 0; i < argv.length; i++) {
      String arg = argv[i];
      if (arg.equals("-c")) {
        CLIENTS = Integer.parseInt(argv[++i]);
      } else if (arg.equals("-n")) {
        NUMBER = Integer.parseInt(argv[++i]);
      } else if (arg.equals("-q")) {
        QUERIES = Integer.parseInt(argv[++i]);
      } else if (arg.equals("-r")) {
        RELATIONS = Integer.parseInt(argv[++i]);
      } else if (arg.equals("-t")) {
        TRANSACTIONS = Integer.parseInt(argv[++i]);
      } else if (arg.equals("-u")) {
        USER = Integer.parseInt(argv[++i]);
      } else {
        opterr++;
      }
    }

    if (opterr > 0) {
      displayUsage(argv[0]);
    }
  }

  /*
   * ===========================================================================
   * == addCustomer -- Wrapper function
   * ===========================================================================
   * ==
   */
  public static boolean addCustomer(final Manager managerPtr, final int id,
      final int num, final int price) {
    return managerPtr.manager_addCustomer(id);
  }

  /*
   * ===========================================================================
   * == initializeManager
   * ===========================================================================
   * ==
   */
  public Manager initializeManager() {
    int i;
    int t;
    System.out.println("Initializing manager... ");

    Random randomPtr = new Random();
    randomPtr.random_alloc();
    Manager managerPtr = new Manager();

    int numRelation = RELATIONS;
    int ids[] = new int[numRelation];
    for (i = 0; i < numRelation; i++) {
      ids[i] = i + 1;
    }

    for (t = 0; t < 4; t++) {

      /* Shuffle ids */
      for (i = 0; i < numRelation; i++) {
        int x = randomPtr.posrandom_generate() % numRelation;
        int y = randomPtr.posrandom_generate() % numRelation;
        int tmp = ids[x];
        ids[x] = ids[y];
        ids[y] = tmp;
      }

      /* Populate table */
      for (i = 0; i < numRelation; i++) {
        boolean status;
        int id = ids[i];
        int num = ((randomPtr.posrandom_generate() % 5) + 1) * 100;
        int price = ((randomPtr.posrandom_generate() % 5) * 10) + 50;
        if (t == 0) {
          status = managerPtr.manager_addCar(id, num, price);
        } else if (t == 1) {
          status = managerPtr.manager_addFlight(id, num, price);
        } else if (t == 2) {
          status = managerPtr.manager_addRoom(id, num, price);
        } else if (t == 3) {
          status = managerPtr.manager_addCustomer(id);
        }
        // assert(status);
      }

    } /* for t */

    System.out.println("done.");
    return managerPtr;
  }

  /*
   * ===========================================================================
   * == initializeClients
   * ===========================================================================
   * ==
   */
  public Client[] initializeClients(final Manager managerPtr) {
    Random randomPtr;
    Client clients[];
    int i;
    int numClient = CLIENTS;
    int numTransaction = TRANSACTIONS;
    int numTransactionPerClient;
    int numQueryPerTransaction = NUMBER;
    int numRelation = RELATIONS;
    int percentQuery = QUERIES;
    int queryRange;
    int percentUser = USER;

    System.out.println("Initializing clients... ");

    randomPtr = new Random();
    randomPtr.random_alloc();

    clients = new Client[numClient];

    numTransactionPerClient = (int) ((double) numTransaction
        / (double) numClient + 0.5);
    queryRange = (int) (percentQuery / 100.0 * numRelation + 0.5);

    for (i = 0; i < numClient; i++) {
      clients[i] = new Client(i, managerPtr, numTransactionPerClient,
          numQueryPerTransaction, queryRange, percentUser);
    }

    System.out.println("done.");
    System.out.println("    Transactions        = " + numTransaction);
    System.out.println("    Clients             = " + numClient);
    System.out.println("    Transactions/client = " + numTransactionPerClient);
    System.out.println("    Queries/transaction = " + numQueryPerTransaction);
    System.out.println("    Relations           = " + numRelation);
    System.out.println("    Query percent       = " + percentQuery);
    System.out.println("    Query range         = " + queryRange);
    System.out.println("    Percent user        = " + percentUser);

    return clients;
  }

  /*
   * ===========================================================================
   * == main
   * ===========================================================================
   * ==
   */
  public static void main(final String[] argv) throws Exception {
    Manager managerPtr;
    Client[] clients;
    long start;
    long stop;

    /* Initialization */
    Vacation vac = new Vacation();
    vac.parseArgs(argv);
    managerPtr = vac.initializeManager();
    clients = vac.initializeClients(managerPtr);
    int numThread = vac.CLIENTS;

    /* Run transactions */
    System.out.println("Running clients... ");

    Barrier.setBarrier(numThread + 1);

    for (int i = 0; i < numThread; i++) {
      clients[i].start();
    }

    Barrier.enterBarrier();
    start = System.currentTimeMillis();
    Barrier.enterBarrier();
    stop = System.currentTimeMillis();

    Barrier.assertIsClear();

    System.out.print("done.");
    long diff = stop - start;
    System.out.println("TIME=" + diff);
    vac.checkTables(managerPtr);

    /* Clean up */
    System.out.println("Deallocating memory... ");
    /*
     * TODO: The contents of the manager's table need to be deallocated.
     */
    System.out.println("done.");
  }

  void checkTables(final Manager managerPtr) {
    int i;
    int numRelation = RELATIONS;
    RBTree customerTablePtr = managerPtr.customerTablePtr;
    RBTree tables[] = new RBTree[3];
    tables[0] = managerPtr.carTablePtr;
    tables[1] = managerPtr.flightTablePtr;
    tables[2] = managerPtr.roomTablePtr;
    int numTable = 3;

    int t;

    System.out.println("Checking tables... ");

    /* Check for unique customer IDs */
    int percentQuery = QUERIES;
    int queryRange = (int) (percentQuery / 100.0 * numRelation + 0.5);
    int maxCustomerId = queryRange + 1;
    for (i = 1; i <= maxCustomerId; i++) {
      if (customerTablePtr.find(i) != null) {
        customerTablePtr.remove(i);
      }
    }

    /* Check reservation tables for consistency and unique ids */
    for (t = 0; t < numTable; t++) {
      RBTree tablePtr = tables[t];
      for (i = 1; i <= numRelation; i++) {
        if (tablePtr.find(i) != null) {
          if (t == 0) {
            managerPtr.manager_addCar(i, 0, 0);
          } else if (t == 1) {
            managerPtr.manager_addFlight(i, 0, 0);
          } else if (t == 2) {
            managerPtr.manager_addRoom(i, 0, 0);
          }
          tablePtr.remove(i);
        }
      }
    }
    System.out.println("done.");
  }
}
