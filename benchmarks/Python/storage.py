class Random:
    seed = 74755;

    def next(self):
        self.seed = ((self.seed * 1309) + 13849) & 65535

        return self.seed

class Storage:
    count = 0

    def buildTreeDepth(self, depth, random):
        self.count += 1
        if (depth == 1):
            s = random.next() % 10 + 1
            a = []
            for i in range(0, s):
                a.append(True)
            return a
        else:
          arr = []
          for i in range(0, 4):
              arr.append(self.buildTreeDepth(depth - 1, random))
          return arr

r = Random()
s = Storage()
s.buildTreeDepth(7, r)

print(s.count)
