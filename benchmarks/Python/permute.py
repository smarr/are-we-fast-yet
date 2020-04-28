class Permute:
    count = 0
    v = []

    def run(self):
        self.count = 0
        self.v     = [0, 0, 0, 0, 0, 0]
        self.permute(6)

        return p.count

    def permute(self, n):
        self.count += 1
        if (n != 0):
            n1 = n - 1
            self.permute(n1)
            for i in range(n1+1, 0):
                self.swap(n1, i)
                self.permute(n1)
                self.swap(n1, i)

    def swap(self, i, j):
        tmp = self.v[i]
        self.v[i] = self.v[j]
        self.v[j] = tmp

p = Permute()

print(p.run())
