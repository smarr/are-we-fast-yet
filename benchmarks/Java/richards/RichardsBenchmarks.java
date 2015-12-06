package som.richards;

import java.util.Arrays;

public class RichardsBenchmarks extends RBObject {
  private TaskControlBlock taskList;
  private TaskControlBlock currentTask;
  private int currentTaskIdentity;
  private TaskControlBlock[] taskTable;
  private boolean tracing;
  private int layout;
  private int queuePacketCount;
  private int holdCount;

  void createDevice(final int identity, final int priority,
      final Packet workPacket, final TaskState state) {
    DeviceTaskDataRecord data = DeviceTaskDataRecord.create();

    createTask(identity, priority, workPacket, state,
       (final Packet workArg, final RBObject wordArg) -> {
         DeviceTaskDataRecord dataRecord = (DeviceTaskDataRecord) wordArg;
         Packet functionWork = workArg;
         if (RBObject.noWork() == functionWork) {
           if (RBObject.noWork() == (functionWork = dataRecord.getPending())) {
             return markWaiting();
           } else {
             dataRecord.setPending(RBObject.noWork());
             return queuePacket(functionWork);
           }
         } else {
           dataRecord.setPending(functionWork);
           if (tracing) {
             trace(functionWork.getDatum());
           }
           return holdSelf();
         }},
      data);
  }

  void createHandler(final int identity, final int priority,
      final Packet workPaket, final TaskState state) {
    HandlerTaskDataRecord data = HandlerTaskDataRecord.create();
    createTask(identity, priority, workPaket, state,
        (final Packet work, final RBObject word) -> {
          HandlerTaskDataRecord dataRecord = (HandlerTaskDataRecord) word;
          if (RBObject.noWork() != work) {
            if (RBObject.workPacketKind() == work.getKind()) {
              dataRecord.workInAdd(work);
            } else {
              dataRecord.deviceInAdd(work);
            }
          }

          Packet workPacket;
          if (RBObject.noWork() == (workPacket = dataRecord.workIn())) {
            return markWaiting();
          } else {
            int count = workPacket.getDatum();
            if (count > 4) {
              dataRecord.workIn(workPacket.getLink());
              return queuePacket(workPacket);
            } else {
              Packet devicePacket;
              if (RBObject.noWork() == (devicePacket = dataRecord.deviceIn())) {
                return markWaiting();
              } else {
                dataRecord.deviceIn(devicePacket.getLink());
                devicePacket.setDatum(workPacket.getData()[count - 1]);  // -1 for Java indexing????
                workPacket.setDatum(count + 1);
                return queuePacket(devicePacket);
              }
            }
          }
      }, data);
  }

  void createIdler(final int identity, final int priority, final Packet work,
      final TaskState state) {
        IdleTaskDataRecord data = IdleTaskDataRecord.create();
        createTask(identity, priority, work, state,
            (final Packet workArg, final RBObject wordArg) -> {
              IdleTaskDataRecord dataRecord = (IdleTaskDataRecord) wordArg;
              dataRecord.setCount(dataRecord.getCount() - 1);
              if (0 == dataRecord.getCount()) {
                return holdSelf();
              } else {
                if (0 == (dataRecord.getControl() & 1)) {
                  dataRecord.setControl(dataRecord.getControl() / 2);
                  return release(RBObject.deviceA());
                } else {
                  dataRecord.setControl((dataRecord.getControl() / 2) ^ 53256);
                  return release(RBObject.deviceB());
                }
              }
            }, data);
  }

  Packet createPacket(final Packet link, final int identity, final int kind) {
    return Packet.create(link, identity, kind);
  }

  void createTask(final int identity, final int priority,
      final Packet work, final TaskState state,
      final ProcessFunction aBlock, final RBObject data) {

    TaskControlBlock t = TaskControlBlock.create(taskList, identity,
        priority, work, state, aBlock, data);
    taskList = t;
    taskTable[identity - 1] = t;  // Java indexing -1
  }

  void createWorker(final int identity, final int priority,
      final Packet workPaket, final TaskState state) {
    WorkerTaskDataRecord dataRecord = WorkerTaskDataRecord.create();
    createTask(identity, priority, workPaket, state,

        (final Packet work, final RBObject word) -> {
          WorkerTaskDataRecord data = (WorkerTaskDataRecord) word;
          if (RBObject.noWork() == work) {
            return markWaiting();
          } else {
            data.setDestination(
                (RBObject.handlerA() == data.getDestination()) ?
                    RBObject.handlerB() : RBObject.handlerA());
            work.setIdentity(data.getDestination());
            work.setDatum(1);
            for (int i = 0; i < 4; i++) {
              data.setCount(data.getCount() + 1);
              if (data.getCount() > 26) { data.setCount(1); }
              work.getData()[i] = 65 + data.getCount() - 1;
            }
            return queuePacket(work);
          }
        }, dataRecord);
  }

  public boolean reBenchStart() {
    Packet workQ;
    initTrace();
    initScheduler();

    createIdler(RBObject.idler(), 0, RBObject.noWork(),
        TaskState.createRunning());
    workQ = createPacket(RBObject.noWork(), RBObject.worker(),
        RBObject.workPacketKind());
    workQ = createPacket(workQ, RBObject.worker(),
        RBObject.workPacketKind());

    createWorker(RBObject.worker(), 1000, workQ,
        TaskState.createWaitingWithPacket());
    workQ = createPacket(RBObject.noWork(), RBObject.deviceA(),
        RBObject.devicePacketKind());
    workQ = createPacket(workQ, RBObject.deviceA(), RBObject.devicePacketKind());
    workQ = createPacket(workQ, RBObject.deviceA(), RBObject.devicePacketKind());

    createHandler(RBObject.handlerA(), 2000, workQ,
        TaskState.createWaitingWithPacket());
    workQ = createPacket(RBObject.noWork(), RBObject.deviceB(),
        RBObject.devicePacketKind());
    workQ = createPacket(workQ, RBObject.deviceB(), RBObject.devicePacketKind());
    workQ = createPacket(workQ, RBObject.deviceB(), RBObject.devicePacketKind());

    createHandler(RBObject.handlerB(), 3000, workQ,
        TaskState.createWaitingWithPacket());
    createDevice(RBObject.deviceA(), 4000, RBObject.noWork(),
        TaskState.createWaiting());
    createDevice(RBObject.deviceB(), 5000, RBObject.noWork(),
        TaskState.createWaiting());

    schedule();

    return queuePacketCount == 23246 && holdCount == 9297;
  }

  TaskControlBlock findTask(final int identity) {
    TaskControlBlock t = taskTable[identity - 1]; // java indexing -1
    if (RBObject.noTask() == t) { throw new RuntimeException("findTask failed"); }
    return t;
  }

  TaskControlBlock holdSelf() {
    holdCount = holdCount + 1;
    currentTask.setTaskHolding(true);
    return currentTask.getLink();
  }

  void initScheduler() {
    queuePacketCount = 0;
    holdCount = 0;
    taskTable = new TaskControlBlock[6];
    Arrays.setAll(taskTable, v -> RBObject.noTask());
    taskList = RBObject.noTask();
  }

  void initTrace() {
    tracing = false;
    layout  = 0;
  }

  TaskControlBlock queuePacket(final Packet packet) {
    TaskControlBlock t = findTask(packet.getIdentity());
    if (RBObject.noTask() == t) { return RBObject.noTask(); }

    queuePacketCount = queuePacketCount + 1;

    packet.setLink(RBObject.noWork());
    packet.setIdentity(currentTaskIdentity);
    return t.addInputAndCheckPriority(packet, currentTask);
  }

  TaskControlBlock release(final int identity) {
    TaskControlBlock t = findTask(identity);
    if (RBObject.noTask() == t) { return RBObject.noTask(); }
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
      Transcript.cr();
      layout = 50;
    }
    Transcript.show("" + id);
  }

  TaskControlBlock markWaiting() {
    currentTask.setTaskWaiting(true);
    return currentTask;
  }

  void schedule() {
    currentTask = taskList;
    while (RBObject.noTask() != currentTask) {
      if (currentTask.isTaskHoldingOrWaiting()) {
        currentTask = currentTask.getLink();
      } else {
        currentTaskIdentity = currentTask.getIdentity();
        if (tracing) { trace(currentTaskIdentity); }
        currentTask = currentTask.runTask();
      }
    }
  }
}
