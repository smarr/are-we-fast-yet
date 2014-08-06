import time

class MethodInvocation(object):

    def __init__(self):
        self._x = 1

        self._get_one_bound   = getattr(self, "get_one")
        self._get_one_unbound = getattr(self, "get_one").__func__

        self._get_one_static_bound   = getattr(self, "get_one_static")
        self._get_one_static_unbound = getattr(self, "get_one_static") ##.__func__

    def get_one(self):
        return self._x

    @staticmethod
    def get_one_static(mi):
        return mi._x

    def bench_direct(self):
        return self._x + self.get_one()

    def bench_direct_static(self):
        return self._x + self.get_one_static(self)

    def bench_refl_bound(self):
        return self._x + self._get_one_bound()

    def bench_refl_unbound(self):
        return self._x + self._get_one_unbound(self)

    def bench_refl_static_bound(self):
        return self._x + self._get_one_static_bound(self)

    def bench_refl_static_unbound(self):
        return self._x + self._get_one_static_unbound(self)


# mi  = MethodInvocation()
# go  = mi.get_one
# go1  = mi.get_one.__func__
# gos = mi.get_one_static
#
# print go()
# print go1(mi)
# print gos(mi)
#
# mi._x = 2
# print go()
# print gos(mi)


def benchmark_direct(inner_iter):
    mi = MethodInvocation()

    result = 0
    for i in range(0, inner_iter):
        result = result + mi.bench_direct()
    return result


def benchmark_direct_static(inner_iter):
    mi = MethodInvocation()

    result = 0
    for i in range(0, inner_iter):
        result = result + mi.bench_direct_static()
    return result

def benchmark_refl_bound(inner_iter):
    mi = MethodInvocation()

    result = 0
    for i in range(0, inner_iter):
        result = result + mi.bench_refl_bound()
    return result

def benchmark_refl_unbound(inner_iter):
    mi = MethodInvocation()

    result = 0
    for i in range(0, inner_iter):
        result = result + mi.bench_refl_unbound()
    return result

def benchmark_refl_static_bound(inner_iter):
    mi = MethodInvocation()

    result = 0
    for i in range(0, inner_iter):
        result = result + mi.bench_refl_static_bound()
    return result

def benchmark_refl_static_unbound(inner_iter):
    mi = MethodInvocation()

    result = 0
    for i in range(0, inner_iter):
        result = result + mi.bench_refl_static_unbound()
    return result


def microseconds():
    return time.time() * 1000 * 1000

# inner_iter = 50000000
#
# for i in range(0, 40):
#
#     start_d = microseconds()
#     d = benchmark_refl_bound(inner_iter)
#     elapsed_d = microseconds() - start_d
#     print d
#     print "Reflective Bound: iterations=1 runtime: %dus" % elapsed_d
#
#     start_d = microseconds()
#     d = benchmark_refl_unbound(inner_iter)
#     elapsed_d = microseconds() - start_d
#     print d
#     print "Reflective Unbound: iterations=1 runtime: %dus" % elapsed_d
#
#     start_d = microseconds()
#     d = benchmark_refl_static_bound(inner_iter)
#     elapsed_d = microseconds() - start_d
#     print d
#     print "Reflective Static Bound: iterations=1 runtime: %dus" % elapsed_d
#
#     start_d = microseconds()
#     d = benchmark_refl_unbound(inner_iter)
#     elapsed_d = microseconds() - start_d
#     print d
#     print "Reflective Unbound: iterations=1 runtime: %dus" % elapsed_d


if __name__ == '__main__':
    import sys
    
    bench_name = sys.argv[1]
    if "Direct"                  == bench_name:
        bench = benchmark_direct
    elif "DirectStatic"          == bench_name:
        bench = benchmark_direct_static
    elif "ReflectiveBound"       == bench_name:
        bench = benchmark_refl_bound
    elif "ReflectiveUnbound"     == bench_name:
        bench = benchmark_refl_unbound
    elif "ReflectiveStaticBound" == bench_name:
        bench = benchmark_refl_static_bound
    elif "ReflectiveStaticUnbound" == bench_name:
        bench = benchmark_refl_static_unbound
    else:
        assert False
     
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

        if r != innerIter * 2:
            print "results are incorrect, direct: %d proxy: %d " % (r, innerIter * 2)
            import os
            os.exit(1)
