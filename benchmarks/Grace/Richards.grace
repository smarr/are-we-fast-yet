method asString {"richards.grace"}

def NoTask           = Done
def Idler            = 1.asInteger
def NoWork           = Done
def Worker           = 2.asInteger
def WorkPacketKind   = 2.asInteger
def HandlerA         = 3.asInteger
def HandlerB         = 4.asInteger
def DeviceA          = 5.asInteger
def DeviceB          = 6.asInteger
def DevicePacketKind = 1.asInteger

type Number = interface {
  + (other)
  - (other)
  * (other)
  / (other)
  < (other)
  > (other)
}

type String = interface {
  ++ (other)
}

type Boolean = interface {
  || (other)
  && (other)
}

type Packet = interface {
  link
  identity
  kind
  datum
  data
  asString
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
  newPacketPending
  newRunning
  newWaiting
  newWaitingWithPacket
  isRunning
  isTaskHoldingOrWaiting
  isWaiting
  isWaitingWithPacket
}

type TaskDataRecord = interface {
  pending
}

class Transcript {

  method cr {
    platform.system.printNewline
  }

  method show(text: String) {
    platform.system.printString(text)
  }

}

class DeviceTaskDataRecord {
  var pending := NoWork
}

class RBObject {

  method append (packet: Packet) head (queueHead) {
    packet.link(NoWork)
    (NoWork == queueHead).ifTrue { 
      return packet
    }

    var mouse := queueHead
    var link
    {
      link := mouse.link
      NoWork == link
    }. whileFalse { mouse := link }

    mouse.link(packet)
    return queueHead
  }
  
}

class HandlerTaskDataRecord {
  inherit RBObject
  var workIn   := NoWork
  var deviceIn := NoWork 

  method deviceInAdd (packet: Packet) {
    deviceIn := self.append(packet) head (deviceIn)
  }

  method workInAdd (packet: Packet) {
    workIn := self.append(packet) head (workIn)
  }

  method asString {
    "HandlerTaskDataRecord(" + workIn.asString + ", " + deviceIn.asString + ")"
  }
}

class IdleTaskDataRecord {
  var control := 1.asInteger
  var count   := 10000.asInteger
}

class Packet (link': Packet) identity (identity': Number) kind (kind': Number) {
  var link      := link'
  var identity  := identity'
  var kind      := kind'
  var datum     := 1.asInteger
  var data      := platform.kernel.Array.new(4.asInteger)withAll(0.asInteger)

  method asString {
    "Packet(" + link.asString + ", " + identity.asString + ", " + kind.asString + ", " + datum.asString + ", " + data.asString + ")"
  }
}

class TaskState {
  inherit RBObject
  
  var packetPending'
  var taskWaiting'
  var taskHolding' 

  method isPacketPending { packetPending' }
  method isTaskHolding   { taskHolding' }
  method isTaskWaiting   { taskWaiting' }

  method taskHolding (aBoolean: Boolean) { taskHolding' := aBoolean }
  method taskWaiting (aBoolean: Boolean) { taskWaiting' := aBoolean }
  method packetPending (aBoolean: Boolean) { packetPending' := aBoolean }

  method packetPending {
    packetPending' := true
    taskWaiting' := false
    taskHolding' := false
  }

  method running {
    packetPending' := false
    taskWaiting' := false 
    taskHolding' := false
  }

  method waiting {
    packetPending' := false
    taskHolding' := false
    taskWaiting' :=  true
  }

  method waitingWithPacket {
    taskHolding' := false
    taskWaiting' := true
    packetPending' := true
  }

  method newPacketPending {
    var ret := TaskState
    ret.packetPending
    ret
  }

  method newRunning {
    var ret := TaskState
    ret.running
    ret
  }

  method newWaiting {
    var ret := TaskState
    ret.waiting
    ret
  }

  method newWaitingWithPacket {
    var ret := TaskState
    ret.waitingWithPacket
    ret
  }

  method isRunning {
    !packetPending' && !taskWaiting' && !taskHolding'
  }

  method isTaskHoldingOrWaiting {
    taskHolding' || (!packetPending' && taskWaiting')
  }

  method isWaiting {
    !packetPending' && taskWaiting' && !taskHolding'
  }

  method isWaitingWithPacket {
    packetPending' && taskWaiting' && !taskHolding'
  }
}

class TaskControlBlock (link) create (identity) priority (priority) initialWorkQueue (initialWorkQueue) initialState (initialState) function (aBlock) privateData (privateData) {
  inherit TaskState
  var input := initialWorkQueue
  var handle := privateData
  var function := aBlock
  self.packetPending(initialState.isPacketPending)
  self.taskWaiting(initialState.isTaskWaiting)
  self.taskHolding(initialState.isTaskHolding)

  method addInput (packet: Packet) checkPriority (oldTask) {
    (NoWork == input).ifTrue {
      input := packet
      self.packetPending(true)
      (priority > oldTask.priority).ifTrue { return self }
    } ifFalse {
      input := self.append (packet) head (input)
    }
    
    oldTask
  }

  method runTask {
    var message

    self.isWaitingWithPacket.ifTrue {
      message := input
      input := message.link
      (NoWork == input).ifTrue {
        self.running
      } ifFalse {
        self.packetPending
      }

    } ifFalse {
      message := NoWork

    }

    function.apply (message, handle)
  }
}



class WorkerTaskDataRecord {
  var destination := HandlerA
  var count       := 0.asInteger
}


class Richards {

  var taskList
  var currentTask
  var currentTaskIdentity
  var taskTable
  var tracing
  var layout
  var queuePacketCount
  var holdCount

  method createDevice (identity: Number) priority (priority: Number) work (work: Packet) state (state: TaskState) {
      var data := DeviceTaskDataRecord
      self.createTask (identity) priority (priority) work (work) state (state)
             function { work, word ->
          var data := word
          var functionWork := work
          
          (NoWork == functionWork). ifTrue {
            functionWork := data.pending

            (NoWork == functionWork). ifTrue {
              self.wait
            } ifFalse {
              data.pending(NoWork)
              self.queuePacket (functionWork)
            }

          } ifFalse {
              data.pending (functionWork)
              tracing.ifTrue {
                self.trace (functionWork.datum)
              }
              self.holdSelf
            }
          }
          data (data)
  }

  method createHandler (identity: Number) priority (priority: Number) work (work: Packet) state (state: TaskState) {
    var data := HandlerTaskDataRecord
    self.createTask (identity) priority (priority) work (work) state (state)
         function { work, word ->
            var data := word
            var workPacket
            

            (NoWork == work).ifFalse {
              (WorkPacketKind == work.kind).ifTrue {
                data.workInAdd(work)                
              } ifFalse {
                data.deviceInAdd(work)                
              }
            }

            workPacket := data.workIn
            (NoWork == workPacket).ifTrue {
              self.wait
            } ifFalse {
              var count := workPacket.datum
              (count > 4.asInteger).ifTrue {
                data.workIn(workPacket.link)
                self.queuePacket(workPacket)
              } ifFalse {
                var devicePacket := data.deviceIn
                (NoWork == devicePacket).ifTrue {
                  self.wait
                } ifFalse {
                  data.deviceIn(devicePacket.link)
                  devicePacket.datum(workPacket.data.at(count))
                  workPacket.datum(count + 1.asInteger)
                  self.queuePacket(devicePacket)
                }
              } 
            }
          }
          data(data)
  }

  method createIdler (identity: Number) priority (priority: Number) work (work: Packet) state (state: TaskState) {
    var data := IdleTaskDataRecord
    self.createTask(identity) priority(priority) work(work) state(state)
         function { work, word ->
           var data := word
           data.count(data.count - 1.asInteger)
           (0.asInteger == data.count).ifTrue {
            self.holdSelf
           } ifFalse {

             (0.asInteger == (data.control & 1.asInteger)).ifTrue {
               data.control((data.control / 2.asInteger).asInteger)
               self.release(DeviceA)
             } ifFalse {
               data.control(((data.control / 2.asInteger).asInteger).bitXor(53256.asInteger))
               self.release(DeviceB)
             }
           }
         }
         data(data)
  }

  method createPacket (link: Packet) identity (identity: Number) kind (kind: Number) {
    Packet (link) identity(identity) kind (kind)
  }

  method createTask (identity: Number) priority (priority: Number) work (work: Packet) state (state: TaskState) function (aBlock) data (data) {
    var t := TaskControlBlock (taskList) create(identity) priority(priority) initialWorkQueue(work) initialState(state) function(aBlock) privateData(data)
    taskList := t
    taskTable.at(identity) put(t)
  }

  method createWorker (identity: Number) priority (priority: Number) work (work: Packet) state (state: TaskState) {
    var data := WorkerTaskDataRecord
    self.createTask (identity) priority(priority) work(work) state(state)
          function { work, word ->
            var data := word

            (NoWork == work).ifTrue {
              self.wait
            } ifFalse {

              data.destination((HandlerA == data.destination).ifTrue { HandlerB } ifFalse { HandlerA })

              work.identity(data.destination)
              work.datum(1.asInteger)
              1.asInteger.to(4.asInteger) do { i ->
               data.count (data.count + 1.asInteger)
               (data.count > 26.asInteger).ifTrue { data.count(1.asInteger) }
               work.data.at(i)put(65.asInteger + data.count - 1.asInteger)
              }

              self.queuePacket(work)
            }
          }
          data (data)
  }

  method start {
    var workQ
    self.initTrace
    self.initScheduler

    self.createIdler(Idler) priority(0.asInteger) work(NoWork) state(TaskState.newRunning)
    workQ := self.createPacket(NoWork) identity(Worker) kind(WorkPacketKind)
    workQ := self.createPacket(workQ) identity(Worker) kind(WorkPacketKind)
    self.createWorker(Worker) priority(1000.asInteger) work(workQ) state(TaskState.newWaitingWithPacket)

    workQ := self.createPacket(NoWork) identity(DeviceA) kind(DevicePacketKind)
    workQ := self.createPacket(workQ) identity(DeviceA) kind(DevicePacketKind)
    workQ := self.createPacket(workQ) identity(DeviceA) kind(DevicePacketKind)

    self.createHandler(HandlerA) priority(2000.asInteger) work(workQ) state(TaskState.newWaitingWithPacket)
    workQ := self.createPacket(NoWork) identity(DeviceB) kind(DevicePacketKind)
    workQ := self.createPacket(workQ) identity(DeviceB) kind(DevicePacketKind)
    workQ := self.createPacket(workQ) identity(DeviceB) kind(DevicePacketKind)

    self.createHandler(HandlerB) priority(3000.asInteger) work(workQ) state (TaskState.newWaitingWithPacket)
    self.createDevice(DeviceA) priority(4000.asInteger) work(NoWork) state (TaskState.newWaiting)
    self.createDevice(DeviceB) priority(5000.asInteger) work(NoWork) state (TaskState.newWaiting)

    schedule

    (queuePacketCount == 23246.asInteger) && (holdCount == 9297.asInteger)
  }


  method findTask (identity: Number) {
    var t := taskTable.at(identity)
    (NoTask == t).ifTrue {self.error("findTask failed")}
    t
  }

  method holdSelf {
    holdCount := holdCount + 1.asInteger
    currentTask.taskHolding(true)
    currentTask.link
  }

  method initScheduler {
    queuePacketCount := 0.asInteger
    holdCount := 0.asInteger
    taskTable := platform.kernel.Array.new(6.asInteger) withAll (NoTask)
    taskList := NoTask
  }

  method initTrace {
    tracing := false
    layout := 0.asInteger
  }

  method queuePacket (packet: Packet) {
    var t := self.findTask(packet.identity)
    (NoTask == t).ifTrue { return NoTask }

    queuePacketCount := queuePacketCount + 1.asInteger
    packet.link(NoWork)
    packet.identity(currentTaskIdentity)
    t.addInput(packet) checkPriority(currentTask)
  }

  method release (identity: Number) {
    var t := self.findTask (identity)
    (NoTask == t). ifTrue { return NoTask }
    t.taskHolding (false)
    (t.priority > currentTask.priority).ifTrue  { return t } ifFalse { return currentTask }
  }

  method trace (id: Number) {
    layout := layout - 1.asInteger
    (0.asInteger >= layout).ifTrue {
      Transcript.cr
      layout := 50.asInteger
    }
    Transcript.show (id.asString)
  }

  method wait {
    currentTask.taskWaiting(true)
    currentTask
  }

  method schedule {
    currentTask := taskList

    { NoTask == currentTask }.whileFalse {

      currentTask.isTaskHoldingOrWaiting.ifTrue {
        currentTask := currentTask.link

      } ifFalse {
        currentTaskIdentity := currentTask.identity
        tracing.ifTrue { self.trace(currentTaskIdentity) }
        currentTask := currentTask.runTask
      }
    }
  }
}


method benchmark(innerIterations) {
  Richards.start
}
