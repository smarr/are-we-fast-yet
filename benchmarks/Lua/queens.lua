-- This code is derived from the SOM benchmarks, see AUTHORS.md file.
--
-- Copyright (c) 2016 Francois Perrad <francois.perrad@gadz.org>
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

local queens = {} do
setmetatable(queens, {__index = require'benchmark'})

function queens:benchmark ()
    local result = true
    for _ = 1, 10 do
        result = result and self:queens()
    end
    return result
end

function queens:verify_result (result)
    return result
end

function queens:queens ()
    self.free_rows  = {true, true, true, true, true, true, true, true}
    self.free_maxs  = {true, true, true, true, true, true, true, true,
                       true, true, true, true, true, true, true, true}
    self.free_mins  = {true, true, true, true, true, true, true, true,
                       true, true, true, true, true, true, true, true}
    self.queen_rows = {-1,   -1,   -1,   -1,   -1,   -1,   -1,   -1}
    return self:place_queen(1)
end

function queens:place_queen (c)
    for r = 1, 8 do
        if self:get_row_column(r, c) then
            self.queen_rows[r] = c
            self:set_row_column(r, c, false)
            if c == 8 then
                return true
            end
            if self:place_queen(c + 1) then
                return true
            end
            self:set_row_column(r, c, true)
        end
    end
    return false
end

function queens:get_row_column (r, c)
    return self.free_rows[r] and self.free_maxs[c + r] and self.free_mins[c - r + 8]
end

function queens:set_row_column (r, c, v)
    self.free_rows[r        ] = v
    self.free_maxs[c + r    ] = v
    self.free_mins[c - r + 8] = v
end

end -- object queens

return queens
