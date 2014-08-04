Basic Information on the Benchmarking System
============================================

Hardware
--------

`$ cat /proc/cpuinfo | grep "model name" | uniq`

```
model name	: Intel(R) Xeon(R) CPU           E5520  @ 2.27GHz
```

`$ lscpu`

```
Architecture:          x86_64
CPU op-mode(s):        32-bit, 64-bit
Byte Order:            Little Endian
CPU(s):                16
On-line CPU(s) list:   0-15
Thread(s) per core:    2
Core(s) per socket:    4
Socket(s):             2
NUMA node(s):          1
Vendor ID:             GenuineIntel
CPU family:            6
Model:                 26
Stepping:              5
CPU MHz:               1596.000
BogoMIPS:              4521.81
Virtualization:        VT-x
L1d cache:             32K
L1i cache:             32K
L2 cache:              256K
L3 cache:              8192K
NUMA node0 CPU(s):     0-15
```

`$ cat /proc/meminfo | grep MemTotal`

```
MemTotal:        8164188 kB
```

Software
--------

`$ uname -a`

```
Linux Infinity 3.11.0-26-generic #45-Ubuntu SMP Tue Jul 15 04:02:06 UTC 2014 x86_64 x86_64 x86_64 GNU/Linux
```

`$ cat /etc/lsb-release`

```
DISTRIB_ID=Ubuntu
DISTRIB_RELEASE=13.10
DISTRIB_CODENAME=saucy
DISTRIB_DESCRIPTION="Ubuntu 13.10"
```

`$ cc --version`

```
cc (Ubuntu/Linaro 4.8.1-10ubuntu9) 4.8.1
Copyright (C) 2013 Free Software Foundation, Inc.
This is free software; see the source for copying conditions.  There is NO
warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

```

`$ java -version`

```
java version "1.8.0_11"
Java(TM) SE Runtime Environment (build 1.8.0_11-b12)
Java HotSpot(TM) 64-Bit Server VM (build 25.11-b03, mixed mode)
```

