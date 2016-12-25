-- This benchmark is adapted to match the SOM version.
-- Ported on Lua by Francois Perrad <francois.perrad@gadz.org>
--
-- Copyright © 2004-2013 Brent Fulgham
--
-- All rights reserved.
--
-- Redistribution and use in source and binary forms, with or without
-- modification, are permitted provided that the following conditions are met:
--
--   * Redistributions of source code must retain the above copyright notice,
--     this list of conditions and the following disclaimer.
--
--   * Redistributions in binary form must reproduce the above copyright notice,
--     this list of conditions and the following disclaimer in the documentation
--     and/or other materials provided with the distribution.
--
--   * Neither the name of "The Computer Language Benchmarks Game" nor the name
--     of "The Computer Language Shootout Benchmarks" nor the names of its
--     contributors may be used to endorse or promote products derived from this
--     software without specific prior written permission.
--
-- THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
-- AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
-- IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
-- DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
-- FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
-- DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
-- SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
-- CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
-- OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
-- OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

-- The Computer Language Benchmarks Game
-- http:--benchmarksgame.alioth.debian.org
--
--  contributed by Karl von Laudermann
--  modified by Jeremy Echols
--  modified by Detlef Reichl
--  modified by Joseph LaFata
--  modified by Peter Zotov

-- http:--benchmarksgame.alioth.debian.org/u64q/program.php?test=mandelbrot&lang=yarv&id=3

local mandelbrot = {} do
setmetatable(mandelbrot, {__index = require'benchmark'})

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
local _mandelbrot
if _VERSION < 'Lua 5.3' then
    local bit = bit32 or require'bit'
    local bxor = bit.bxor
    local lshift = bit.lshift

    _mandelbrot = function (size)
        local sum      = 0
        local byte_acc = 0
        local bit_num  = 0

        local y = 0
        while y < size do
            local ci = (2.0 * y / size) - 1.0
            local x  = 0

            while x < size do
                local zrzr, zr = 0.0
                local zizi, zi = 0.0, 0.0
                local cr   = (2.0 * x / size) - 1.5

                local z = 0
                local not_done = true
                local escape = 0
                while not_done and z < 50 do
                    zr = zrzr - zizi + cr
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
else
    _mandelbrot = assert(load[[
    --  Lua 5.3 variant with bitwise operators
    return function (size)
        local sum      = 0
        local byte_acc = 0
        local bit_num  = 0

        local y = 0
        while y < size do
            local ci = (2.0 * y / size) - 1.0
            local x  = 0

            while x < size do
                local zrzr, zr = 0.0
                local zizi, zi = 0.0, 0.0
                local cr   = (2.0 * x / size) - 1.5

                local z = 0
                local not_done = true
                local escape = 0
                while not_done and z < 50 do
                    zr = zrzr - zizi + cr
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

                byte_acc = (byte_acc << 1) + escape
                bit_num = bit_num + 1

                -- Code is very similar for these cases, but using separate blocks
                -- ensures we skip the shifting when it's unnecessary,
                -- which is most cases.
                if bit_num == 8 then
                    sum = sum ~ byte_acc
                    byte_acc = 0
                    bit_num  = 0
                elseif x == size - 1 then
                    byte_acc = byte_acc << (8 - bit_num)
                    sum = sum ~ byte_acc
                    byte_acc = 0
                    bit_num  = 0
                end
                x = x + 1
            end
            y = y + 1
        end

        return sum
    end
]])()
end

function mandelbrot:inner_benchmark_loop (inner_iterations)
    return self:verify_result(_mandelbrot(inner_iterations), inner_iterations)
end

function mandelbrot:verify_result (result, inner_iterations)
    if     inner_iterations == 500 then
        return result == 191
    elseif inner_iterations == 750 then
        return result == 50
    elseif inner_iterations ==   1 then
        return result == 128
    else
        print(('No verification result for %d found'):format(inner_iterations))
        print(('Result is: %d'):format(result))
        return false
    end
end

end -- object mandelbrot

return mandelbrot
