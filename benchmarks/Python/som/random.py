class Random:
    seed = 74755

    def next(self):
        self.seed = ((self.seed * 1309) + 13849) & 65535

        return self.seed
