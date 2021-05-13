def sieve(flags, size):
    prime_count = 0

    for i in range(2, size + 1):
        if flags[i - 1]:
            prime_count += 1
            k = i + i
            while k <= size:
                flags[k - 1] = False
                k += i

    return prime_count


flags = []
for i in range(0, 5000):
    flags.append(True)

print(sieve(flags, 5000))
