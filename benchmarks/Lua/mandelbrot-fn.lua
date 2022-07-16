--[[
    The module 'bit' is available with:
      * LuaJIT
      * LuaBitOp extension which is available for:
          * Lua 5.1
          * Lua 5.2
    The module 'bit32' is available with:
      * Lua 5.2
      * Lua 5.3 when compiled with LUA_COMPAT_5_2
    The bitwise operators are added to Lua 5.3 as new lexemes (there causes
    lexical error in older version)
--]]

local bit    = bit32 or require'bit'
local bxor   = bit.bxor
local lshift = bit.lshift

local function mandelbrot (size)
    local sum      = 0
    local byte_acc = 0
    local bit_num  = 0

    local y = 0
    while y < size do
        local ci = (2.0 * y / size) - 1.0
        local x  = 0

        while x < size do
            local zrzr = 0.0
            local zizi, zi = 0.0, 0.0
            local cr   = (2.0 * x / size) - 1.5

            local z = 0
            local not_done = true
            local escape = 0
            while not_done and z < 50 do
                local zr = zrzr - zizi + cr
                zi = 2.0 * zr * zi + ci

                -- preserve recalculation
                zrzr = zr * zr
                zizi = zi * zi
                if zrzr + zizi > 4.0 then
                    not_done = false
                    escape   = 1
                end
                z = z + 1
            end

            byte_acc = (byte_acc * 2) + escape
            bit_num = bit_num + 1

            -- Code is very similar for these cases, but using separate blocks
            -- ensures we skip the shifting when it's unnecessary,
            -- which is most cases.
            if bit_num == 8 then
                sum = bxor(sum, byte_acc)
                byte_acc = 0
                bit_num  = 0
            elseif x == size - 1 then
                byte_acc = lshift(byte_acc, (8 - bit_num))
                sum = bxor(sum,byte_acc)
                byte_acc = 0
                bit_num  = 0
            end
            x = x + 1
        end
        y = y + 1
    end

    return sum
end

return mandelbrot
