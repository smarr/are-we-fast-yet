# The benchmark in its current state is a derivation from the SOM version,
# which is derived from Mario Wolczko's Smalltalk version of DeltaBlue.
#
# The original license details are availble here:
# http://web.archive.org/web/20050825101121/http://www.sunlabs.com/people/mario/java_benchmarking/index.html

# This file itself, and its souce control history is however based on the
# following. It is unclear whether this still bears any relevance since the
# nature of the code was essentially reverted back to the Smalltalk version.
#
# Derived from http://pws.prserv.net/dlissett/ben/bench1.htm
# Licensed CC BY-NC-SA 1.0
require "./benchmark"

NO_TASK = nil
NO_WORK = nil

IDLER     = 0
WORKER    = 1
HANDLER_A = 2
HANDLER_B = 3
DEVICE_A  = 4
DEVICE_B  = 5

NUM_TYPES = 6

DEVICE_PACKET_KIND = 0
WORK_PACKET_KIND   = 1

DATA_SIZE = 4

TRACING = false

class Richards < Benchmark
  def benchmark
    RichardsScheduler.new.start
  end

  def verify_result(result)
    result
  end
end

class RBObject
  def append(packet, queue_head)
    packet.link = NO_WORK
    if NO_WORK == queue_head
      return packet
    end

    mouse = queue_head

    while NO_WORK != (link = mouse.not_nil!.link)
      mouse = link
    end
    mouse.not_nil!.link = packet
    queue_head
  end
end

class RichardsScheduler < RBObject

  @task_list    : TaskControlBlock?
  @current_task : TaskControlBlock?

  def initialize
    # init tracing
    @layout = 0

    # init scheduler
    @task_list    = NO_TASK
    @current_task = NO_TASK
    @current_task_identity = 0

    @task_table = Array(TaskControlBlock?).new(NUM_TYPES, NO_TASK)

    @queue_count = 0
    @hold_count  = 0
  end

  def create_device(identity, priority, work, state)
    data = DeviceTaskDataRecord.new
    create_task(identity, priority, work, state, data) { | work, word |
      data_record = word.as(DeviceTaskDataRecord)
      function_work = work
      if function_work # NO_WORK ==
        data_record.pending = function_work
        if TRACING
          trace(function_work.datum)
        end
        hold_self
      else
        function_work = data_record.pending
        if function_work ## NO_WORK ==
          data_record.pending = NO_WORK
          queue_packet(function_work)
        else
          wait
        end
      end
    }
  end

  def create_handler(identity, priority, work, state)
    data = HandlerTaskDataRecord.new
    create_task(identity, priority, work, state, data) { | work, word |
      data_record = word.as(HandlerTaskDataRecord)
      unless NO_WORK == work
        w = work.not_nil!
        if WORK_PACKET_KIND == w.kind
          data_record.work_in_add(w)
        else
          data_record.device_in_add(w)
        end
      end

      work_packet = data_record.work_in
      if work_packet
        count = work_packet.datum
        if count >= DATA_SIZE
          data_record.work_in = work_packet.link
          queue_packet(work_packet)
        else
          device_packet = data_record.device_in
          if device_packet # NO_WORK ==
            data_record.device_in = device_packet.link
            device_packet.datum = work_packet.data[count]
            work_packet.datum = count + 1
            queue_packet(device_packet)
          else
            wait
          end
        end
      else
        # NO_WORK ==
        wait
      end
    }
  end

  def create_idler(identity, priority, work, state)
    data = IdleTaskDataRecord.new
    create_task(identity, priority, work, state, data) { | work, word |
      data_record = word.as(IdleTaskDataRecord)
      data_record.count -= 1
      if 0 == data_record.count
        hold_self
      else
        if 0 == (data_record.control & 1)
          data_record.control = (data_record.control / 2).to_i32
          release(DEVICE_A)
        else
          data_record.control = (data_record.control / 2).to_i32 ^ 53256
          release(DEVICE_B)
        end
      end
    }
  end

  def create_packet(link, identity, kind)
    Packet.new(link, identity, kind)
  end

  def create_task(identity : Int32, priority : Int32, work : Packet?,
                  state : TaskState, data, &block : Packet?, RBObject -> TaskControlBlock?)
    t = TaskControlBlock.new(@task_list, identity, priority, work, state, data, &block)
    @task_list = t
    @task_table[identity] = t
  end

  def create_worker(identity, priority, work, state)
    data_record = WorkerTaskDataRecord.new
    create_task(identity, priority, work, state, data_record) { | work, word |
      data = word.as(WorkerTaskDataRecord)
      if work
        data.destination = HANDLER_A == data.destination ? HANDLER_B : HANDLER_A
        work.identity = data.destination
        work.datum = 0
        DATA_SIZE.times { | i |
          data.count += 1
          if data.count > 26
            data.count = 1
          end
          work.data[i] = 65 + data.count - 1
        }
        queue_packet(work)
      else # NO_WORK ==
        wait
      end
    }
  end

  def start
    create_idler(IDLER, 0, NO_WORK, TaskState.running)
    wkq = create_packet(NO_WORK, WORKER, WORK_PACKET_KIND)
    wkq = create_packet(wkq,     WORKER, WORK_PACKET_KIND)

    create_worker(WORKER, 1000, wkq, TaskState.waiting_with_packet)
    wkq = create_packet(NO_WORK, DEVICE_A, DEVICE_PACKET_KIND)
    wkq = create_packet(wkq,     DEVICE_A, DEVICE_PACKET_KIND)
    wkq = create_packet(wkq,     DEVICE_A, DEVICE_PACKET_KIND)

    create_handler(HANDLER_A, 2000, wkq, TaskState.waiting_with_packet)
    wkq = create_packet(NO_WORK, DEVICE_B, DEVICE_PACKET_KIND)
    wkq = create_packet(wkq,     DEVICE_B, DEVICE_PACKET_KIND)
    wkq = create_packet(wkq,     DEVICE_B, DEVICE_PACKET_KIND)

    create_handler(HANDLER_B, 3000, wkq, TaskState.waiting_with_packet)
    create_device(DEVICE_A, 4000, NO_WORK, TaskState.waiting)
    create_device(DEVICE_B, 5000, NO_WORK, TaskState.waiting)

    schedule

    @queue_count == 23246 && @hold_count == 9297
  end

  def find_task(identity)
    t = @task_table[identity]
    if NO_TASK == t
      raise "find_task failed"
    end
    t
  end

  def hold_self
    @hold_count += 1
    current_task = @current_task.not_nil!
    current_task.task_holding = true
    current_task.link
  end

  def queue_packet(packet : Packet)
    task_or_no_task = find_task(packet.identity)
    if NO_TASK == task_or_no_task
      return NO_TASK
    end

    task = task_or_no_task.not_nil!

    @queue_count += 1

    packet.link     = NO_WORK
    packet.identity = @current_task_identity
    task.add_input_and_check_priority(packet, @current_task.not_nil!)
  end

  def release(identity) : TaskControlBlock?
    task_or_no_task = find_task(identity)
    if NO_TASK == task_or_no_task
      return NO_TASK
    end

    task = task_or_no_task.not_nil!

    task.task_holding = false
    current_task = @current_task.not_nil!

    if task.priority > current_task.priority
      task
    else
      current_task
    end
  end

  def trace(id)
    @layout -= 1
    if 0 >= @layout
      puts ""
      @layout = 50
    end
    print id
  end

  def wait
    current_task = @current_task.not_nil!
    current_task.task_waiting = true
    current_task
  end

  def schedule
    @current_task = @task_list
    while NO_TASK != @current_task
      current_task = @current_task.not_nil!
      if current_task.is_task_holding_or_waiting
        @current_task = current_task.link
      else
        @current_task_identity = current_task.identity
        if TRACING
          trace(@current_task_identity)
        end
        @current_task = current_task.run_task
      end
    end
  end
end

class DeviceTaskDataRecord < RBObject

  @pending : Packet?

  property :pending
   def initialize
    @pending = NO_WORK
   end
end

class HandlerTaskDataRecord < RBObject
  property :work_in, :device_in

  @work_in   : Packet?
  @device_in : Packet?

  def initialize
    @work_in   = NO_WORK
    @device_in = NO_WORK
   end

  def device_in_add(packet)
    @device_in = append(packet, @device_in)
  end

  def work_in_add(packet)
    @work_in = append(packet, @work_in)
  end
end

class IdleTaskDataRecord < RBObject
  property control : Int32
  property count : Int32

  def initialize
    @control = 1
    @count   = 10000
  end
end

class Packet < RBObject
  property :link, :kind, :identity, :datum, :data

  @data : Array(Int32)

  def initialize(link : Packet?, identity : Int32, kind : Int32)
    @link     = link
    @kind     = kind
    @identity = identity
    @datum    = 0
    @data     = Array.new(4, 0)
  end
end

class TaskState < RBObject
  property :task_holding, :task_waiting, :packet_pending

  def initialize
    @task_holding = false
    @task_waiting = false
    @packet_pending = false
  end

  def is_packet_pending
    @packet_pending
  end

  def is_task_waiting
    @task_waiting
  end

  def is_task_holding
    @task_holding
  end

  def packet_pending
    @packet_pending = true
    @task_waiting   = false
    @task_holding   = false
    self
  end

  def running
    @packet_pending = @task_waiting = @task_holding = false
    self
  end

  def waiting
    @packet_pending = @task_holding = false
    @task_waiting = true
    self
  end

  def waiting_with_packet
    @task_holding = false
    @task_waiting = @packet_pending = true
    self
  end

  def is_task_holding_or_waiting
    @task_holding || (!@packet_pending && @task_waiting)
  end

  def is_waiting_with_packet
    @packet_pending && @task_waiting && !@task_holding
  end

  def self.running
    self.new.running
  end

  def self.waiting
    self.new.waiting
  end

  def self.waiting_with_packet
    self.new.waiting_with_packet
  end
end

class TaskControlBlock < TaskState
  property :link, :identity, :function, :priority

  def initialize(link : TaskControlBlock?, identity : Int32, priority : Int32,
                 initial_work_queue : Packet?,
                 initial_state, private_data : RBObject, &block : Packet?, RBObject -> TaskControlBlock?)
    super()
    @link = link
    @identity = identity
    @function = block
    @priority = priority
    @input = initial_work_queue
    @handle = private_data

    self.packet_pending = initial_state.is_packet_pending
    self.task_waiting   = initial_state.is_task_waiting
    self.task_holding   = initial_state.is_task_holding
  end

  def add_input_and_check_priority(packet, old_task : TaskControlBlock)
    if NO_WORK == @input
      @input = packet
      self.packet_pending = true
      if @priority > old_task.priority
        return self
      end
    else
      @input = append(packet, @input)
    end
    old_task
  end

  def run_task
    if is_waiting_with_packet
      message = @input.not_nil!
      @input = message.link
      if @input
        packet_pending
      else  # NO_WORK
        running
      end
    else
      message = NO_WORK
    end

    function.call(message, @handle)
  end
end

class WorkerTaskDataRecord < RBObject
  property :destination, :count

  def initialize
    @destination = HANDLER_A
    @count = 0
  end
end
