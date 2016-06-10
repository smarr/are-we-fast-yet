/*
 * This benchmark is derived from Mario Wolczko's Java and Smalltalk version of
 * Richards.
 *
 * It is modified based on the SOM version and to use Java 8 features.
 * License details:
 *   http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html
 */
package richards;

import java.util.Arrays;

public class Scheduler extends RBObject {
  private TaskControlBlock taskList;
  private TaskControlBlock currentTask;
  private int currentTaskIdentity;
  private final TaskControlBlock[] taskTable;

  private int queuePacketCount;
  private int holdCount;

  private int layout;

  private static final boolean TRACING = false;

  public Scheduler() {
    // init tracing
    layout  = 0;

    // init scheduler
    queuePacketCount = 0;
    holdCount = 0;
    taskTable = new TaskControlBlock[NUM_TYPES];
    Arrays.fill(taskTable, NO_TASK);
    taskList = NO_TASK;
  }


  void createDevice(final int identity, final int priority,
      final Packet workPacket, final TaskState state) {
    DeviceTaskDataRecord data = new DeviceTaskDataRecord();

    createTask(identity, priority, workPacket, state,
       (final Packet workArg, final RBObject wordArg) -> {
         DeviceTaskDataRecord dataRecord = (DeviceTaskDataRecord) wordArg;
         Packet functionWork = workArg;
         if (NO_WORK == functionWork) {
           if (NO_WORK == (functionWork = dataRecord.getPending())) {
             return markWaiting();
           } else {
             dataRecord.setPending(NO_WORK);
             return queuePacket(functionWork);
           }
         } else {
           dataRecord.setPending(functionWork);
           if (TRACING) {
             trace(functionWork.getDatum());
           }
           return holdSelf();
         }},
      data);
  }

  void createHandler(final int identity, final int priority,
      final Packet workPaket, final TaskState state) {
    HandlerTaskDataRecord data = new HandlerTaskDataRecord();
    createTask(identity, priority, workPaket, state,
        (final Packet work, final RBObject word) -> {
          HandlerTaskDataRecord dataRecord = (HandlerTaskDataRecord) word;
          if (NO_WORK != work) {
            if (WORK_PACKET_KIND == work.getKind()) {
              dataRecord.workInAdd(work);
            } else {
              dataRecord.deviceInAdd(work);
            }
          }

          Packet workPacket;
          if (NO_WORK == (workPacket = dataRecord.workIn())) {
            return markWaiting();
          } else {
            int count = workPacket.getDatum();
            if (count >= Packet.DATA_SIZE) {
              dataRecord.workIn(workPacket.getLink());
              return queuePacket(workPacket);
            } else {
              Packet devicePacket;
              if (NO_WORK == (devicePacket = dataRecord.deviceIn())) {
                return markWaiting();
              } else {
                dataRecord.deviceIn(devicePacket.getLink());
                devicePacket.setDatum(workPacket.getData()[count]);
                workPacket.setDatum(count + 1);
                return queuePacket(devicePacket);
              }
            }
          }
      }, data);
  }

  void createIdler(final int identity, final int priority, final Packet work,
      final TaskState state) {
        IdleTaskDataRecord data = new IdleTaskDataRecord();
        createTask(identity, priority, work, state,
            (final Packet workArg, final RBObject wordArg) -> {
              IdleTaskDataRecord dataRecord = (IdleTaskDataRecord) wordArg;
              dataRecord.setCount(dataRecord.getCount() - 1);
              if (0 == dataRecord.getCount()) {
                return holdSelf();
              } else {
                if (0 == (dataRecord.getControl() & 1)) {
                  dataRecord.setControl(dataRecord.getControl() / 2);
                  return release(DEVICE_A);
                } else {
                  dataRecord.setControl((dataRecord.getControl() / 2) ^ 53256);
                  return release(DEVICE_B);
                }
              }
            }, data);
  }

  Packet createPacket(final Packet link, final int identity, final int kind) {
    return new Packet(link, identity, kind);
  }

  void createTask(final int identity, final int priority,
      final Packet work, final TaskState state,
      final ProcessFunction aBlock, final RBObject data) {

    TaskControlBlock t = new TaskControlBlock(taskList, identity,
        priority, work, state, aBlock, data);
    taskList = t;
    taskTable[identity] = t;
  }

  void createWorker(final int identity, final int priority,
      final Packet workPaket, final TaskState state) {
    WorkerTaskDataRecord dataRecord = new WorkerTaskDataRecord();
    createTask(identity, priority, workPaket, state,

        (final Packet work, final RBObject word) -> {
          WorkerTaskDataRecord data = (WorkerTaskDataRecord) word;
          if (NO_WORK == work) {
            return markWaiting();
          } else {
            data.setDestination((HANDLER_A == data.getDestination()) ? HANDLER_B : HANDLER_A);
            work.setIdentity(data.getDestination());
            work.setDatum(0);
            for (int i = 0; i < Packet.DATA_SIZE; i++) {
              data.setCount(data.getCount() + 1);
              if (data.getCount() > 26) { data.setCount(1); }
              work.getData()[i] = 65 + data.getCount() - 1;
            }
            return queuePacket(work);
          }
        }, dataRecord);
  }

  public boolean start() {
    Packet workQ;

    createIdler(IDLER, 0, NO_WORK, TaskState.createRunning());
    workQ = createPacket(NO_WORK, WORKER, WORK_PACKET_KIND);
    workQ = createPacket(workQ,   WORKER, WORK_PACKET_KIND);

    createWorker(WORKER, 1000, workQ, TaskState.createWaitingWithPacket());
    workQ = createPacket(NO_WORK, DEVICE_A, DEVICE_PACKET_KIND);
    workQ = createPacket(workQ,   DEVICE_A, DEVICE_PACKET_KIND);
    workQ = createPacket(workQ,   DEVICE_A, DEVICE_PACKET_KIND);

    createHandler(HANDLER_A, 2000, workQ, TaskState.createWaitingWithPacket());
    workQ = createPacket(NO_WORK, DEVICE_B, DEVICE_PACKET_KIND);
    workQ = createPacket(workQ, DEVICE_B, DEVICE_PACKET_KIND);
    workQ = createPacket(workQ, DEVICE_B, DEVICE_PACKET_KIND);

    createHandler(HANDLER_B, 3000, workQ, TaskState.createWaitingWithPacket());
    createDevice(DEVICE_A, 4000, NO_WORK, TaskState.createWaiting());
    createDevice(DEVICE_B, 5000, NO_WORK, TaskState.createWaiting());

    schedule();

    return queuePacketCount == 23246 && holdCount == 9297;
  }

  TaskControlBlock findTask(final int identity) {
    TaskControlBlock t = taskTable[identity];
    if (NO_TASK == t) { throw new RuntimeException("findTask failed"); }
    return t;
  }

  TaskControlBlock holdSelf() {
    holdCount = holdCount + 1;
    currentTask.setTaskHolding(true);
    return currentTask.getLink();
  }

  TaskControlBlock queuePacket(final Packet packet) {
    TaskControlBlock t = findTask(packet.getIdentity());
    if (NO_TASK == t) { return NO_TASK; }

    queuePacketCount = queuePacketCount + 1;

    packet.setLink(NO_WORK);
    packet.setIdentity(currentTaskIdentity);
    return t.addInputAndCheckPriority(packet, currentTask);
  }

  TaskControlBlock release(final int identity) {
    TaskControlBlock t = findTask(identity);
    if (NO_TASK == t) { return NO_TASK; }
    t.setTaskHolding(false);
    if (t.getPriority() > currentTask.getPriority()) {
      return t;
    } else {
      return currentTask;
    }
  }

  void trace(final int id) {
    layout = layout - 1;
    if (0 >= layout) {
      // Checkstyle: stop
      System.out.println();
      // Checkstyle: resume
      layout = 50;
    }
    // Checkstyle: stop
    System.out.print(id);
    // Checkstyle: resume
  }

  TaskControlBlock markWaiting() {
    currentTask.setTaskWaiting(true);
    return currentTask;
  }

  void schedule() {
    currentTask = taskList;
    while (NO_TASK != currentTask) {
      if (currentTask.isTaskHoldingOrWaiting()) {
        currentTask = currentTask.getLink();
      } else {
        currentTaskIdentity = currentTask.getIdentity();
        if (TRACING) { trace(currentTaskIdentity); }
        currentTask = currentTask.runTask();
      }
    }
  }
}
