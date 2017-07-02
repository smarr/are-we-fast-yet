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

local _mandelbrot
if _VERSION < 'Lua 5.3' then
    _mandelbrot = require 'mandelbrot-fn'
else
    _mandelbrot = require 'mandelbrot-fn-53'
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
