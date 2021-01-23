-- This code is derived from the SOM benchmarks, see AUTHORS.md file.
--
-- Copyright (c) 2016-2017 Francois Perrad <francois.perrad@gadz.org>
--
-- Permission is hereby granted, free of charge, to any person obtaining a copy
-- of this software and associated documentation files (the 'Software'), to deal
-- in the Software without restriction, including without limitation the rights
-- to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
-- copies of the Software, and to permit persons to whom the Software is
-- furnished to do so, subject to the following conditions:
--
-- The above copyright notice and this permission notice shall be included in
-- all copies or substantial portions of the Software.
--
-- THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
-- IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
-- FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
-- AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
-- LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
-- OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
-- THE SOFTWARE.

--[[
    Alternate implementation of som which does not follow the rules.

    Usage: lua -lluasom harness.lua benchmark ...
--]]

local pairs = pairs
local setmetatable = setmetatable

local LuaVector = {_CLASS = 'LuaVector'} do

local tremove = table.remove
local tsort = table.sort

function LuaVector.new ()
    local obj = {
        seq = {},
    }
    return setmetatable(obj, {__index = LuaVector})
end

function LuaVector.with (elem)
    local v = LuaVector.new()
    v:append(elem)
    return v
end

function LuaVector:at (idx)
    return self.seq[idx]
end

function LuaVector:at_put (idx, val)
    self.seq[idx] = val
end

function LuaVector:append (elem)
    self.seq[#self.seq+1] = elem
end

function LuaVector:is_empty ()
    return 0 == #self.seq
end

function LuaVector:each (fn)
    for i = 1, #self.seq do
        fn(self.seq[i])
    end
end

function LuaVector:has_some (fn)
    for i = 1, #self.seq do
        if fn(self.seq[i]) then
            return true
        end
    end
    return false
end

function LuaVector:get_one (fn)
    for i = 1, #self.seq do
        local e = self.seq[i]
        if fn(e) then
            return e
        end
    end
    return nil
end

function LuaVector:remove_first ()
    return tremove(self.seq, 1)
end

function LuaVector:remove (obj)
    for i = 1, #self.seq do
        if self.seq[i] == obj then
            tremove(self.seq, i)
            return true
        end
    end
    return false
end

function LuaVector:remove_all ()
    self.seq = {}
end

function LuaVector:size ()
    return #self.seq
end

function LuaVector:sort (fn)
    tsort(self.seq, fn)
end

end -- class LuaVector

local LuaSet = {_CLASS = 'LuaSet'} do

function LuaSet.new ()
    local obj = {
        dict = {},
        _size = 0,
    }
    return setmetatable(obj, {__index = LuaSet})
end

function LuaSet:size ()
    return self._size
end

function LuaSet:each (fn)
    for k in pairs(self.dict) do
        fn(k)
    end
end

function LuaSet:has_some (fn)
    for k in pairs(self.dict) do
        if fn(k) then
            return true
        end
    end
    return false
end

function LuaSet:get_one (fn)
    for k in pairs(self.dict) do
        if fn(k) then
            return k
        end
    end
    return nil
end

function LuaSet:add (obj)
    if not self.dict[obj] then
        self._size = self._size + 1
    end
    self.dict[obj] = true
end

function LuaSet:remove (obj)
    if self.dict[obj] then
        self._size = self._size - 1
    end
    self.dict[obj] = nil
end

function LuaSet:remove_all ()
    self.dict = {}
    self._size = 0
end

function LuaSet:collect (fn)
    local coll = LuaVector.new()
    self:each(function (it)
        coll:append(fn(it))
    end)
    return coll
end

function LuaSet:contains (obj)
    return self.dict[obj]
end

end -- class LuaSet

local LuaDictionary = {_CLASS = 'LuaDictionary'} do

function LuaDictionary.new ()
    local obj = {
        size = 0,
        dict = {},
    }
    return setmetatable(obj, {__index = LuaDictionary})
end

function LuaDictionary:is_empty ()
    return self.size == 0
end

function LuaDictionary:at (key)
    return self.dict[key]
end

function LuaDictionary:at_put (key, value)
    if not self.dict[key] then
        self.size = self.size + 1
    end
    self.dict[key] = value
end

function LuaDictionary:remove_all ()
    self.dict = {}
    self.size = 0
end

function LuaDictionary:keys ()
    local keys = LuaVector.new()
    for k in pairs(self.dict) do
        keys:append(k)
    end
    return keys
end

function LuaDictionary:values ()
    local values = LuaVector.new()
    for _, v in pairs(self.dict) do
        values:append(v)
    end
    return values
end

end -- class LuaDictionary

local LuaRandom = {_CLASS = 'LuaRandom'} do

function LuaRandom.new ()
    local obj = {seed = 74755}
    return setmetatable(obj, {__index = LuaRandom})
end

function LuaRandom:next ()
  self.seed = ((self.seed * 1309) + 13849) % 65536;
  return self.seed;
end

end -- class LuaRandom

local som = {
    Vector = LuaVector,
    Set = LuaSet,
    IdentitySet = LuaSet,
    Dictionary = LuaDictionary,
    IdentityDictionary = LuaDictionary,
    Random = LuaRandom,
}
package.loaded.som = som
return som
