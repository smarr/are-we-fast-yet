def sieve(flags, size):
    primeCount = 0

    for i in range(2, size+1):
      if (flags[i - 1]):
        primeCount += 1
        k = i + i
        while (k <= size):
          flags[k - 1] = False
          k += i

    return primeCount

flags = []
for i in range(0, 5000):
    flags.append(True)

print(sieve(flags, 5000))
