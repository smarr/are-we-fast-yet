# This code is derived from the SOM benchmarks, see AUTHORS.md file.
#
# Copyright (c) 2023 Valentin Churavy <v.churavy@gmail.com>

# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the 'Software'), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.
module Bounce

include(joinpath(@__DIR__, "benchmark.jl"))
include(joinpath(@__DIR__, "som", "random.jl"))

mutable struct Ball
    x::Int32
    y::Int32
    xVel::Int32
    yVel::Int32
end

function Ball(rand)
    x = next!(rand) % 500
    y = next!(rand) % 500
    xVel = (next!(rand) % 300) - 150
    yVel = (next!(rand) % 300) - 150
    return Ball(x, y, xVel, yVel)
end

function bounce!(b::Ball)
    xLimit = Int32(500)
    yLimit = Int32(500)
    bounced = false

    b.x += b.xVel
    b.y += b.yVel

    if b.x > xLimit
        b.x = xLimit
        b.xVel = 0 - abs(b.xVel)
        bounced = true
    end
    if b.x < 0
        b.x = 0
        b.xVel = abs(b.xVel)
        bounced = true
    end
    if b.y > yLimit
        b.y = yLimit
        b.yVel = 0 - abs(b.yVel)
        bounced = true
    end
    if b.y < 0
        b.y = 0
        b.yVel = abs(b.yVel)
        bounced = true
    end
    return bounced
end

struct Benchmark end

function benchmark(::Benchmark)
    ballCount = 100
    bounces = 0
    rand = Random()

    balls = [Ball(rand) for _ in 1:ballCount]

    for _ in 1:50
        for ball in balls
            if bounce!(ball)
                bounces += 1
            end
        end
    end
    return bounces
end

function verify_result(::Benchmark, result)
    result == 1331
end

end # module
