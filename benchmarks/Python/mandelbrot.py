def mandelbrot(size):
    sum     = 0
    byteAcc = 0
    bitNum  = 0

    y = 0

    while (y < size):
        ci = (2.0 * y / size) - 1.0
        x = 0

        while (x < size):
            zr   = 0.0
            zrzr = 0.0
            zi   = 0.0
            zizi = 0.0
            cr = (2.0 * x / size) - 1.5

            z = 0
            notDone = True
            escape = 0
            while (notDone and z < 50):
                zr = zrzr - zizi + cr
                zi = 2.0 * zr * zi + ci

                zrzr = zr * zr
                zizi = zi * zi

                if (zrzr + zizi > 4.0):
                    notDone = False
                    escape  = 1
                z = z+1

            byteAcc = (byteAcc << 1) + escape
            bitNum = bitNum + 1

            if (bitNum == 8):
                sum = sum ^ byteAcc
                byteAcc = 0
                bitNum  = 0
            elif (x == size - 1):
               byteAcc = byteAcc << (8 - bitNum)
               sum = sum ^ byteAcc
               byteAcc = 0
               bitNum  = 0
            x = x + 1
        y = y + 1

    return sum

size = 500

# print(mandelbrot(size))

import dis
dis.dis(mandelbrot)
