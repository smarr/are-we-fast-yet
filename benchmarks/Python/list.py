class Element:
    val = 0
    next = None

    def __init__(self, v):
        self.val = v

    def length(self):
        if (self.next == None):
            return 1
        else:
            return 1 + self.next.length()


def isShorterThan(x, y):
    xTail = x
    yTail = y

    while (yTail != None):
       if (xTail == None):
         return True


       xTail = xTail.next
       yTail = yTail.next


    return False


def tail(x, y, z):
    if (isShorterThan(y, x)):
      return tail(
        tail(x.next, y, z),
        tail(y.next, z, x),
        tail(z.next, x, y)
      )
    else:
      return z

def makeList(length):
    if (length == 0):
        return None
    else:
        e = Element(length)
        e.next = makeList(length - 1)

        return e

result = tail(makeList(15), makeList(10), makeList(6))
print(result.length())
