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
from benchmark import Benchmark

_NO_TASK = None
_NO_WORK = None

_IDLER = 0
_WORKER = 1
_HANDLER_A = 2
_HANDLER_B = 3
_DEVICE_A = 4
_DEVICE_B = 5

_NUM_TYPES = 6

_DEVICE_PACKET_KIND = 0
_WORK_PACKET_KIND = 1

_DATA_SIZE = 4

_TRACING = False


class Richards(Benchmark):
    def benchmark(self):
        return _Scheduler().start()

    def verify_result(self, result):
        return result


class _RBObject:
    @staticmethod
    def append(packet, queue_head):
        packet.link = _NO_WORK
        if _NO_WORK == queue_head:
            return packet

        mouse = queue_head

        while True:
            link = mouse.link
            if link == _NO_WORK:
                break
            mouse = link

        mouse.link = packet
        return queue_head


class _Scheduler(_RBObject):
    def __init__(self):
        # init tracing
        self._layout = 0

        # init scheduler
        self._task_list = _NO_TASK
        self._current_task = _NO_TASK
        self._current_task_identity = 0

        self._task_table = [_NO_TASK] * _NUM_TYPES

        self._queue_count = 0
        self._hold_count = 0

    def create_device(self, identity, priority, work, state):
        data = _DeviceTaskDataRecord()

        def fn(function_work, data_record):
            if _NO_WORK == function_work:
                function_work = data_record.pending
                if _NO_WORK == function_work:
                    return self.mark_waiting()

                data_record.pending = _NO_WORK
                return self.queue_packet(function_work)

            data_record.pending = function_work
            if _TRACING:
                self._trace(function_work.datum)
            return self._hold_self()

        self.create_task(identity, priority, work, state, data, fn)

    def create_handler(self, identity, priority, work, state):
        data = _HandlerTaskDataRecord()

        def fn(work_arg, data_record):
            if _NO_WORK != work_arg:
                if _WORK_PACKET_KIND == work_arg.kind:
                    data_record.work_in_add(work_arg)
                else:
                    data_record.device_in_add(work_arg)

            work_packet = data_record.work_in
            if _NO_WORK == work_packet:
                return self.mark_waiting()

            count = work_packet.datum
            if count >= _DATA_SIZE:
                data_record.work_in = work_packet.link
                return self.queue_packet(work_packet)

            device_packet = data_record.device_in
            if _NO_WORK == device_packet:
                return self.mark_waiting()

            data_record.device_in = device_packet.link
            device_packet.datum = work_packet.data[count]
            work_packet.datum = count + 1
            return self.queue_packet(device_packet)

        self.create_task(identity, priority, work, state, data, fn)

    def create_idler(self, identity, priority, work, state):
        data = _IdleTaskDataRecord()

        def fn(_work, data_record):
            data_record.count = data_record.count - 1
            if 0 == data_record.count:
                return self._hold_self()

            if 0 == (data_record.control & 1):
                data_record.control = data_record.control // 2
                return self.release(_DEVICE_A)

            data_record.control = (data_record.control // 2) ^ 53_256
            return self.release(_DEVICE_B)

        self.create_task(identity, priority, work, state, data, fn)

    def create_packet(self, link, identity, kind):
        return _Packet(link, identity, kind)

    def create_task(self, identity, priority, work, state, data, fn):
        t = _TaskControlBlock(
            self._task_list, identity, priority, work, state, data, fn
        )
        self._task_list = t
        self._task_table[identity] = t

    def create_worker(self, identity, priority, work, state):
        data = _WorkerTaskDataRecord()

        def fn(work, data_record):
            if _NO_WORK == work:
                return self.mark_waiting()

            data_record.destination = (
                _HANDLER_B if _HANDLER_A == data_record.destination else _HANDLER_A
            )

            work.identity = data_record.destination
            work.datum = 0
            for i in range(_DATA_SIZE):
                data_record.count = data_record.count + 1
                if data_record.count > 26:
                    data_record.count = 1
                work.data[i] = 65 + data_record.count - 1

            return self.queue_packet(work)

        self.create_task(identity, priority, work, state, data, fn)

    def start(self):
        self.create_idler(_IDLER, 0, _NO_WORK, _TaskState.with_running())
        wkq = self.create_packet(_NO_WORK, _WORKER, _WORK_PACKET_KIND)
        wkq = self.create_packet(wkq, _WORKER, _WORK_PACKET_KIND)

        self.create_worker(_WORKER, 1000, wkq, _TaskState.with_waiting_with_packet())
        wkq = self.create_packet(_NO_WORK, _DEVICE_A, _DEVICE_PACKET_KIND)
        wkq = self.create_packet(wkq, _DEVICE_A, _DEVICE_PACKET_KIND)
        wkq = self.create_packet(wkq, _DEVICE_A, _DEVICE_PACKET_KIND)

        self.create_handler(
            _HANDLER_A, 2000, wkq, _TaskState.with_waiting_with_packet()
        )
        wkq = self.create_packet(_NO_WORK, _DEVICE_B, _DEVICE_PACKET_KIND)
        wkq = self.create_packet(wkq, _DEVICE_B, _DEVICE_PACKET_KIND)
        wkq = self.create_packet(wkq, _DEVICE_B, _DEVICE_PACKET_KIND)

        self.create_handler(
            _HANDLER_B, 3000, wkq, _TaskState.with_waiting_with_packet()
        )
        self.create_device(_DEVICE_A, 4000, _NO_WORK, _TaskState.with_waiting())
        self.create_device(_DEVICE_B, 5000, _NO_WORK, _TaskState.with_waiting())

        self.schedule()

        return self._queue_count == 23_246 and self._hold_count == 9297

    def find_task(self, identity):
        t = self._task_table[identity]
        if _NO_TASK == t:
            raise Exception("find_task failed")
        return t

    def _hold_self(self):
        self._hold_count += 1
        self._current_task.set_task_holding(True)
        return self._current_task.link

    def queue_packet(self, packet):
        task = self.find_task(packet.identity)
        if _NO_TASK == task:
            return _NO_TASK

        self._queue_count += 1

        packet.link = _NO_WORK
        packet.identity = self._current_task_identity
        return task.add_input_and_check_priority(packet, self._current_task)

    def release(self, identity):
        task = self.find_task(identity)
        if _NO_TASK == task:
            return _NO_TASK

        task.set_task_holding(False)

        if task.priority > self._current_task.priority:
            return task
        return self._current_task

    def _trace(self, msg):
        self._layout -= 1
        if 0 >= self._layout:
            print("")
            self._layout = 50

        print(msg)

    def mark_waiting(self):
        self._current_task.set_task_waiting(True)
        return self._current_task

    def schedule(self):
        self._current_task = self._task_list
        while _NO_TASK is not self._current_task:
            if self._current_task.is_task_holding_or_waiting():
                self._current_task = self._current_task.link
            else:
                self._current_task_identity = self._current_task.identity
                if _TRACING:
                    self._trace(self._current_task_identity)
                self._current_task = self._current_task.run_task()


class _DeviceTaskDataRecord(_RBObject):
    def __init__(self):
        self.pending = _NO_WORK


class _HandlerTaskDataRecord(_RBObject):
    def __init__(self):
        self.work_in = _NO_WORK
        self.device_in = _NO_WORK

    def device_in_add(self, packet):
        self.device_in = self.append(packet, self.device_in)

    def work_in_add(self, packet):
        self.work_in = self.append(packet, self.work_in)


class _IdleTaskDataRecord(_RBObject):
    def __init__(self):
        self.control = 1
        self.count = 10_000


class _Packet(_RBObject):
    def __init__(self, link, identity, kind):
        self.link = link
        self.kind = kind
        self.identity = identity
        self.datum = 0
        self.data = [0] * _DATA_SIZE


class _TaskState(_RBObject):
    def __init__(self):
        self._task_holding = False
        self._task_waiting = False
        self._packet_pending = False

    def is_packet_pending(self):
        return self._packet_pending

    def is_task_waiting(self):
        return self._task_waiting

    def is_task_holding(self):
        return self._task_holding

    def set_task_holding(self, task):
        self._task_holding = task

    def set_task_waiting(self, task):
        self._task_waiting = task

    def packet_pending(self):
        self._packet_pending = True
        self._task_waiting = False
        self._task_holding = False
        return self

    def running(self):
        self._packet_pending = self._task_waiting = self._task_holding = False
        return self

    def waiting(self):
        self._packet_pending = self._task_holding = False
        self._task_waiting = True
        return self

    def waiting_with_packet(self):
        self._task_holding = False
        self._task_waiting = self._packet_pending = True
        return self

    def is_task_holding_or_waiting(self):
        return self._task_holding or (not self._packet_pending and self._task_waiting)

    def is_waiting_with_packet(self):
        return self._packet_pending and self._task_waiting and not self._task_holding

    @classmethod
    def with_running(cls):
        return cls().running()

    @classmethod
    def with_waiting(cls):
        return cls().waiting()

    @classmethod
    def with_waiting_with_packet(cls):
        return cls().waiting_with_packet()


class _TaskControlBlock(_TaskState):
    def __init__(
        self,
        link,
        identity,
        priority,
        initial_work_queue,
        initial_state,
        private_data,
        fn,
    ):
        super().__init__()
        self.link = link
        self.identity = identity
        self.function = fn
        self.priority = priority
        self._input = initial_work_queue
        self._handle = private_data

        self._packet_pending = initial_state.is_packet_pending()
        self._task_waiting = initial_state.is_task_waiting()
        self._task_holding = initial_state.is_task_holding()

    def add_input_and_check_priority(self, packet, old_task):
        if _NO_WORK == self._input:
            self._input = packet
            self._packet_pending = True
            if self.priority > old_task.priority:
                return self
        else:
            self._input = self.append(packet, self._input)

        return old_task

    def run_task(self):
        if self.is_waiting_with_packet():
            message = self._input
            self._input = message.link
            if _NO_WORK == self._input:
                self.running()
            else:
                self.packet_pending()
        else:
            message = _NO_WORK

        return self.function(message, self._handle)


class _WorkerTaskDataRecord(_RBObject):
    def __init__(self):
        self.destination = _HANDLER_A
        self.count = 0
