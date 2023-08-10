mutable struct Random
    _seed::Int32
    Random() = new(74755)
end

function next!(self::Random)
    self._seed = ((self._seed * Int32(1309)) + Int32(13849)) & Int32(65535)

    return self._seed
end