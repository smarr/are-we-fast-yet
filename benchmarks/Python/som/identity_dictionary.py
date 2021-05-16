from som.dictionary import Entry, Dictionary


class _IdEntry(Entry):
    def __init__(self, hash_, key, value, next_):
        super().__init__(hash_, key, value, next_)

    def match(self, hash_, key):
        return self.hash == hash_ and self.key is key


class IdentityDictionary(Dictionary):
    def _new_entry(self, key, value, hash_):
        return _IdEntry(hash_, key, value, None)
