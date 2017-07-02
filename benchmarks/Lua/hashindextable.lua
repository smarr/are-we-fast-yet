local HashIndexTable = {_CLASS = 'HashIndexTable'} do

local bit = bit32 or require'bit'
local band = bit.band

function HashIndexTable.new ()
    local obj = {
        hash_table = {length = 32;
                      0, 0, 0, 0, 0, 0, 0, 0,
                      0, 0, 0, 0, 0, 0, 0, 0,
                      0, 0, 0, 0, 0, 0, 0, 0,
                      0, 0, 0, 0, 0, 0, 0, 0},
    }
    return setmetatable(obj, {__index = HashIndexTable})
end

function HashIndexTable:add (name, index)
    local slot = self:hash_slot_for(name)
    if index < 255 then
        -- increment by 1, 0 stands for empty
        self.hash_table[slot] = band(index + 1, 0xFF)
    else
        self.hash_table[slot] = 0
    end
end

function HashIndexTable:get (name)
    local slot = self:hash_slot_for(name)
    -- subtract 1, 0 stands for empty
    return band(self.hash_table[slot], 0xFF) - 1
end

function HashIndexTable:string_hash (s)
    -- this is not a proper hash, but sufficient for the benchmark,
    -- and very portable!
    return #s * 1402589
end

function HashIndexTable:hash_slot_for (element)
    return band(self:string_hash(element), self.hash_table.length - 1) + 1
end

end -- class HashIndexTable

return HashIndexTable
