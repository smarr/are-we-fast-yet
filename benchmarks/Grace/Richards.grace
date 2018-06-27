import "harness" as harness

def NoTask: Done = Done
def NoWork: Done = Done

def            Idler: Number = 1.asInteger
def           Worker: Number = 2.asInteger
def   WorkPacketKind: Number = 2.asInteger
def         HandlerA: Number = 3.asInteger
def         HandlerB: Number = 4.asInteger
def          DeviceA: Number = 5.asInteger
def          DeviceB: Number = 6.asInteger
def DevicePacketKind: Number = 1.asInteger

def tracing: Boolean = false

type RBObject = interface {
  append (_) head (_)
}

type Packet = interface {
  link
  identity
  kind
  datum
  data
  asString
}

type Scheduler = interface {
  start
}

type TaskControlBlock = interface {
  packetPending(_)
  taskWaiting(_)
  taskHolding(_)
}

type DeviceTaskDataRecord = interface {
  pending
}

type TaskState = interface {
  append (packet) head (queueHead)
  packetPending'
  taskWaiting'
  taskHolding'
  isPacketPending
  isTaskHolding
  isTaskWaiting
  taskHolding (aBoolean)
  taskWaiting (aBoolean)
  packetPending (aBoolean)
  packetPending
  running
  waiting
  waitingWithPacket
  isRunning
  isTaskHoldingOrWaiting
  isWaiting
  isWaitingWithPacket
}

type HandlerTaskDataRecord = interface {
  workIn
  deviceIn
  deviceInAdd(packet)
  workInAdd(packet)
  asString
}

type IdleTaskDataRecord = interface {
  control
  count
}

type WorkerTaskDataRecord = interface {
  destination
  count
}

class newRichards -> Benchmark {
  inherit harness.newBenchmark

  method benchmark() -> Boolean {
    newScheduler.start
  }

  method verifyResult(result: Boolean) -> Boolean {
    result
  }
}

class RBObject -> Done {
  method append (packet: Packet) head (queueHead: Packet) -> Packet {
    packet.link(NoWork)
    (NoWork == queueHead).ifTrue { return packet }

    var mouse: Packet := queueHead
    var link: Packet
    {
      link := mouse.link
      NoWork == link
    }. whileFalse { mouse := link }

    mouse.link(packet)
    return queueHead
  }
}

class newScheduler -> Scheduler {
  var taskList: TaskControlBlock    := NoTask
  var currentTask: TaskControlBlock := NoTask
  var currentTaskIdentity: Number   := 0.asInteger
  var taskTable: List               := platform.kernel.Array.new(6.asInteger) withAll (NoTask)
  var layout: Number                := 0.asInteger
  var queuePacketCount: Number      := 0.asInteger
  var holdCount: Number             := 0.asInteger

  method createDevice (identity: Number) priority (priority: Number)
                 work (work: Packet) state (state: TaskState) -> Done {
      var data: DeviceTaskDataRecord := DeviceTaskDataRecord

      createTask (identity) priority (priority) work (work) state (state)
             function { work: Packet, word: RBObject ->
          var data: DeviceTaskDataRecord := word
          var functionWork: Packet := work

          (NoWork == functionWork). ifTrue {
            functionWork := data.pending

            (NoWork == functionWork). ifTrue {
              wait
            } ifFalse {
              data.pending := NoWork
              queuePacket (functionWork)
            }
          } ifFalse {
              data.pending := functionWork
              tracing.ifTrue {
                trace (functionWork.datum)
              }
              holdSelf
            }
          }
          data (data)
  }

  method createHandler (identity: Number) priority (priority: Number)
                  work (work: Packet) state (state: TaskState) -> Done {
    var data: HandlerTaskDataRecord := HandlerTaskDataRecord

    createTask (identity) priority (priority) work (work) state (state)
         function { work: Packet, word: RBObject ->
            var data: HandlerTaskDataRecord := word

            (NoWork == work).ifFalse {
              (WorkPacketKind == work.kind).ifTrue {
                data.workInAdd(work)
              } ifFalse {
                data.deviceInAdd(work)
              }
            }

            var workPacket: Packet := data.workIn
            (NoWork == workPacket).ifTrue {
              wait
            } ifFalse {
              var count: Number := workPacket.datum
              (count > 4.asInteger).ifTrue {
                data.workIn(workPacket.link)
                queuePacket(workPacket)
              } ifFalse {
                var devicePacket: Packet := data.deviceIn
                (NoWork == devicePacket).ifTrue {
                  wait
                } ifFalse {
                  data.deviceIn(devicePacket.link)
                  devicePacket.datum(workPacket.data.at(count))
                  workPacket.datum(count + 1.asInteger)
                  queuePacket(devicePacket)
                }
              }
            }
          }
          data(data)
  }

  method createIdler (identity: Number) priority (priority: Number)
                work (work: Packet) state (state: TaskState) -> Done {
    var data: IdleTaskDataRecord := IdleTaskDataRecord
    createTask(identity) priority(priority) work(work) state(state)
         function { work: Packet, word: RBObject ->
           var data: RBObject := word
           data.count(data.count - 1.asInteger)
           (0.asInteger == data.count).ifTrue {
            holdSelf
           } ifFalse {
             (0.asInteger == (data.control & 1.asInteger)).ifTrue {
               data.control((data.control / 2.asInteger))
               release(DeviceA)
             } ifFalse {
               data.control(((data.control / 2.asInteger)).bitXor(53256.asInteger))
               release(DeviceB)
             }
           }
         }
         data(data)
  }

  method createPacket (link: Packet) identity (identity: Number) kind (kind: Number) -> Packet {
    Packet (link) identity (identity) kind (kind)
  }

  method createTask (identity: Number) priority (priority: Number)
               work (work: Packet) state (state: TaskState)
           function (aBlock: Invokable) data (data: RBObject) -> Done {
    var t: TaskControlBlock := newTaskControlBlock (taskList) create(identity)
                                           priority(priority) initialWorkQueue(work)
                                       initialState(state) function(aBlock)
                                        privateData(data)
    taskList := t
    taskTable.at(identity) put(t)
  }

  method createWorker (identity: Number) priority (priority: Number)
                 work (work: Packet) state (state: TaskState) -> Done {
    var data: WorkerTaskDataRecord := WorkerTaskDataRecord
    createTask (identity) priority(priority) work(work) state(state)
          function { work: Packet, word: RBObject ->
            var data: WorkerTaskDataRecord := word

            (NoWork == work).ifTrue {
              wait
            } ifFalse {
              data.destination := (HandlerA == data.destination).ifTrue { HandlerB } ifFalse { HandlerA }

              work.identity(data.destination)
              work.datum(1.asInteger)
              1.asInteger.to(4.asInteger) do { i: Number ->
               data.count (data.count + 1.asInteger)
               (data.count > 26.asInteger).ifTrue { data.count(1.asInteger) }
               work.data.at(i)put(65.asInteger + data.count - 1.asInteger)
              }

              queuePacket(work)
            }
          }
          data (data)
  }

  method start -> Boolean {
    var workQ: Packet

    createIdler(Idler) priority(0.asInteger) work(NoWork) state(newTaskStateRunning)
    workQ := createPacket(NoWork) identity(Worker) kind(WorkPacketKind)
    workQ := createPacket(workQ) identity(Worker) kind(WorkPacketKind)
    createWorker(Worker) priority(1000.asInteger) work(workQ) state(newTaskStateWaitingWithPacket)

    workQ := createPacket(NoWork) identity(DeviceA) kind(DevicePacketKind)
    workQ := createPacket(workQ) identity(DeviceA) kind(DevicePacketKind)
    workQ := createPacket(workQ) identity(DeviceA) kind(DevicePacketKind)

    createHandler(HandlerA) priority(2000.asInteger) work(workQ) state(newTaskStateWaitingWithPacket)
    workQ := createPacket(NoWork) identity(DeviceB) kind(DevicePacketKind)
    workQ := createPacket(workQ) identity(DeviceB) kind(DevicePacketKind)
    workQ := createPacket(workQ) identity(DeviceB) kind(DevicePacketKind)

    createHandler(HandlerB) priority(3000.asInteger) work(workQ) state (newTaskStateWaitingWithPacket)
    createDevice(DeviceA) priority(4000.asInteger) work(NoWork) state (newTaskStateWaiting)
    createDevice(DeviceB) priority(5000.asInteger) work(NoWork) state (newTaskStateWaiting)

    schedule

    (queuePacketCount == 23246.asInteger) && (holdCount == 9297.asInteger)
  }

  method findTask (identity: Number) -> TaskControlBlock {
    var t: TaskControlBlock := taskTable.at(identity)
    (NoTask == t).ifTrue {error("findTask failed")}
    t
  }

  method holdSelf -> TaskControlBlock {
    holdCount := holdCount + 1.asInteger
    currentTask.taskHolding(true)
    currentTask.link
  }

  method queuePacket (packet: Packet) -> Done {
    var t: TaskControlBlock := findTask(packet.identity)
    (NoTask == t).ifTrue { return NoTask }

    queuePacketCount := queuePacketCount + 1.asInteger
    packet.link(NoWork)
    packet.identity(currentTaskIdentity)
    return t.addInput(packet) checkPriority(currentTask)
  }

  method release (identity: Number) -> Done {
    var t: TaskControlBlock := findTask (identity)
    (NoTask == t). ifTrue { return NoTask }
    t.taskHolding (false)
    (t.priority > currentTask.priority).ifTrue  { return t } ifFalse { return currentTask }
  }

  method trace (id: Number) -> Done {
    layout := layout - 1.asInteger
    (0.asInteger >= layout).ifTrue {
      print("")
      layout := 50.asInteger
    }
    print(id.asString)
  }

  method wait -> TaskControlBlock {
    currentTask.taskWaiting(true)
    currentTask
  }

  method schedule -> Done {
    currentTask := taskList

    { NoTask == currentTask }.whileFalse {

      currentTask.isTaskHoldingOrWaiting.ifTrue {
        currentTask := currentTask.link

      } ifFalse {
        currentTaskIdentity := currentTask.identity
        tracing.ifTrue { trace(currentTaskIdentity) }
        currentTask := currentTask.runTask
      }
    }
  }
}

class Packet (link': Packet) identity (identity': Number) kind (kind': Number) -> Packet {
  var     link: Packet := link'
  var identity: Number := identity'
  var     kind: Number := kind'
  var    datum: Number := 1.asInteger
  var     data: List := platform.kernel.Array.new(4.asInteger)withAll(0.asInteger)

  method asString -> String {
    "Packet({link.asString}, {identity.asString}, {kind.asString}, {datum.asString}, {data.asString})"
  }
}

class DeviceTaskDataRecord -> DeviceTaskDataRecord {
  inherit RBObject
  var pending: Packet := NoWork
}

class HandlerTaskDataRecord -> HandlerTaskDataRecord {
  inherit RBObject
  var workIn: Packet := NoWork
  var deviceIn: Packet := NoWork

  method deviceInAdd (packet: Packet) -> Packet {
    deviceIn := append (packet) head (deviceIn)
  }

  method workInAdd (packet: Packet) -> Packet {
    workIn := append (packet) head (workIn)
  }

  method asString -> String {
    "HandlerTaskDataRecord({workIn.asString}, {deviceIn.asString})"
  }
}

class IdleTaskDataRecord -> IdleTaskDataRecord {
  inherit RBObject

  var control: Number := 1.asInteger
  var count: Number := 10000.asInteger
}

class TaskState -> TaskState {
  inherit RBObject

  var packetPending': Boolean
  var taskWaiting': Boolean
  var taskHolding': Boolean

  method isPacketPending -> Boolean { packetPending' }
  method isTaskHolding   -> Boolean { taskHolding' }
  method isTaskWaiting   -> Boolean { taskWaiting' }

  method taskHolding (aBoolean: Boolean) -> Done { taskHolding' := aBoolean }
  method taskWaiting (aBoolean: Boolean) -> Done { taskWaiting' := aBoolean }
  method packetPending (aBoolean: Boolean) -> Done { packetPending' := aBoolean }

  method packetPending -> Done {
    packetPending' := true
    taskWaiting' := false
    taskHolding' := false
    Done
  }

  method running -> Done {
    packetPending' := false
    taskWaiting' := false
    taskHolding' := false
    Done
  }

  method waiting -> Done {
    packetPending' := false
    taskHolding' := false
    taskWaiting' :=  true
    Done
  }

  method waitingWithPacket -> Done {
    taskHolding' := false
    taskWaiting' := true
    packetPending' := true
    Done
  }

  method isRunning -> Boolean {
    !packetPending' && !taskWaiting' && !taskHolding'
  }

  method isTaskHoldingOrWaiting -> Boolean {
    taskHolding' || (!packetPending' && taskWaiting')
  }

  method isWaiting -> Boolean {
    !packetPending' && taskWaiting' && !taskHolding'
  }

  method isWaitingWithPacket -> Boolean {
    packetPending' && taskWaiting' && !taskHolding'
  }
}

method newTaskStatePacketPending -> TaskState {
  var ret: TaskState := TaskState
  ret.packetPending
  ret
}

method newTaskStateRunning -> TaskState {
  var ret: TaskState := TaskState
  ret.running
  ret
}

method newTaskStateWaiting -> TaskState {
  var ret: TaskState := TaskState
  ret.waiting
  ret
}

method newTaskStateWaitingWithPacket -> TaskState {
  var ret: TaskState := TaskState
  ret.waitingWithPacket
  ret
}

class newTaskControlBlock (link: TaskControlBlock) create (identity: Number) priority (priority: Number) initialWorkQueue (initialWorkQueue: Packet) initialState (initialState: TaskState) function (aBlock: Invokable) privateData (privateData: RBObject) -> TaskControlBlock {
  inherit TaskState

  var input: Packet := initialWorkQueue
  var handle: RBObject := privateData
  var function: Invokable := aBlock
  packetPending(initialState.isPacketPending)
  taskWaiting(initialState.isTaskWaiting)
  taskHolding(initialState.isTaskHolding)

  method addInput (packet: Packet) checkPriority (oldTask: TaskControlBlock) -> TaskControlBlock {
    (NoWork == input).ifTrue {
      input := packet
      packetPending(true)
      (priority > oldTask.priority).ifTrue { return self }
    } ifFalse {
      input := append (packet) head (input)
    }

    oldTask
  }

  method runTask -> TaskControlBlock {
    var message: Packet

    isWaitingWithPacket.ifTrue {
      message := input
      input := message.link
      (NoWork == input).ifTrue {
        running
      } ifFalse {
        packetPending
      }
    } ifFalse {
      message := NoWork
    }

    function.apply (message, handle)
  }
}

class WorkerTaskDataRecord -> WorkerTaskDataRecord {
  inherit RBObject

  var destination: Number := HandlerA
  var count: Number := 0.asInteger
}

method newInstance -> Benchmark { newRichards }
