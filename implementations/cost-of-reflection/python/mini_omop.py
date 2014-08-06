import time

class DomainObj(object):
    
    def __init__(self):
        self._owner = None


class Domain(object):
    
    def request_exec_on_with(self, method_name, target, *args):
        fun = getattr(target, method_name)
        return fun(*args)


class AddDomain(object):
    
    def request_exec_on_with(self, method_name, target, *args):
        fun = getattr(target, method_name)
        return 1 + fun(*args)


class _IntercessionProxy(object):
    
    def __init__(self, target, method_name):
        self._target      = target
        self._method_name = method_name

    def __call__(self, *args):
        return self._target._owner.request_exec_on_with(self._method_name, self._target, *args)


class DomainRef(object):
    
    def __init__(self, obj, domain):
        obj._owner = domain
        self._target = obj
        
        # self._cache = {
        #     'get' : _IntercessionProxy(self._target, 'get'),
        #     'inc' : _IntercessionProxy(self._target, 'inc')
        # }
    
    def __getattr__(self, method_name):
        return _IntercessionProxy(self._target, method_name)


class Adder(DomainObj):
    
    def __init__(self):
        self._value = 0
    
    def get(self):
        return self._value
    
    def inc(self):
        self._value += 1
        return self._value
    
a = Adder()
print a.get()
a.inc()
print a.get()

b = DomainRef(Adder(), Domain())
print b.get()
b.inc()
print b.get()

b = DomainRef(Adder(), AddDomain())
print b.get()
b.inc()
print b.get()



def benchmark_direct(inner_iter):
    add = Adder()
    result = 0
    for i in range(0, inner_iter):
        result = 1 + add.inc()

    return result


def benchmark_proxy(inner_iter):
    add = DomainRef(Adder(), AddDomain())
    result = 0
    for i in range(0, inner_iter):
        result = add.inc()
    return result


def microseconds():
    return time.time() * 1000 * 1000

# inner_iter = 50000000
#
# for i in range(0, 100):
#
#     start_d = microseconds()
#     d = benchmark_direct(inner_iter)
#     elapsed_d = microseconds() - start_d
#     print "Direct: iterations=1 runtime: %dus" % elapsed_d
#
#     start_p = microseconds()
#     p = benchmark_proxy(inner_iter)
#     elapsed_p = microseconds() - start_p
#     print "Proxy:  iterations=1 runtime: %dus" % elapsed_p
#
#     if d != p:
#         print "results are incorrect, direct: %d proxy: %d " % (d, p)
#         import os
#         os.exit(1)

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
    innerIter     = int(sys.argv[4]) * 10000
    
    for i in range(warmUp):
        bench(innerIter)
                    
    for i in range(numIterations):
        start_p = microseconds()
        r = bench(innerIter)
        elapsed_p = microseconds() - start_p
        print "%s: iterations=1 runtime: %dus" % (bench_name, elapsed_p)

        if r != innerIter + 1:
            print "results are incorrect, direct: %d proxy: %d " % (r, innerIter + 1)
            import os
            os.exit(1)
    
    
