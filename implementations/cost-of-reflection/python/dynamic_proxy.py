import time


class _InvocationHandler(object):

    def __init__(self, target, method_name):
        self._target      = target
        self._method_name = method_name

    def __call__(self, *args):
        method = getattr(self._target, self._method_name)
        return method(*args)


class Proxy(object):
    
    def __init__(self, target):
        self._target = target

    def __getattr__(self, method_name):
        return _InvocationHandler(self._target, method_name)


class Calculator(object):

    def __init__(self):
        self._x = 5

    def add(self, y):
        return self._x + y


def benchmark_direct(inner_iter):
    calc = Calculator()
    result = 0

    for i in range(0, inner_iter):
        result += calc.add(1)

    return result


def benchmark_proxy(inner_iter):
    calc = Proxy(Calculator())
    result = 0

    for i in range(0, inner_iter):
        result += calc.add(1)

    return result


def microseconds():
    return time.time() * 1000 * 1000


if __name__ == '__main__':
    import sys
    
    bench_name = sys.argv[1]
    if "Direct" == bench_name:
        bench = benchmark_direct
    else:
        assert "Proxy" == bench_name
        bench = benchmark_proxy
     
    numIterations = int(sys.argv[2])
    warmUp        = int(sys.argv[3])
    innerIter     = int(sys.argv[4]) * 1000
    
    for i in range(warmUp):
        bench(innerIter)
                    
    for i in range(numIterations):
        start_p = microseconds()
        r = bench(innerIter)
        elapsed_p = microseconds() - start_p
        print "%s: iterations=1 runtime: %dus" % (bench_name, elapsed_p)

        if r != innerIter * 6:
            print "results are incorrect, direct: %d proxy: %d " % (r, innerIter * 6)
            import os
            os.exit(1)
