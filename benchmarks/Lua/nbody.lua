-- The Computer Language Benchmarks Game
-- http:--shootout.alioth.debian.org/
--
--     contributed by Mark C. Lewis
-- modified slightly by Chad Whipkey
--
-- Based on nbody.java ported to SOM, and then Lua by Francois Perrad.

local Body = {_CLASS = 'Body'} do

local PI            = 3.141592653589793
local SOLAR_MASS    = 4.0 * PI * PI
local DAYS_PER_YEAR = 365.24

function Body.new (x, y, z, vx, vy, vz, mass)
    local obj = {
        x = x,
        y = y,
        z = z,
        vx = vx * DAYS_PER_YEAR,
        vy = vy * DAYS_PER_YEAR,
        vz = vz * DAYS_PER_YEAR,
        mass = mass * SOLAR_MASS,
    }
    return setmetatable(obj, {__index = Body})
end

function Body:offset_momentum (px, py, pz)
    self.vx = 0.0 - (px / SOLAR_MASS)
    self.vy = 0.0 - (py / SOLAR_MASS)
    self.vz = 0.0 - (pz / SOLAR_MASS)
end

function Body.jupiter ()
    return Body.new( 4.8414314424647209,
                    -1.16032004402742839,
                    -0.103622044471123109,
                     0.00166007664274403694,
                     0.00769901118419740425,
                    -0.0000690460016972063023,
                     0.000954791938424326609)
end

function Body.saturn ()
    return Body.new( 8.34336671824457987,
                     4.12479856412430479,
                    -0.403523417114321381,
                    -0.00276742510726862411,
                     0.00499852801234917238,
                     0.0000230417297573763929,
                     0.000285885980666130812)
end

function Body.uranus ()
    return Body.new( 12.894369562139131,
                    -15.1111514016986312,
                     -0.223307578892655734,
                      0.00296460137564761618,
                      0.0023784717395948095,
                     -0.0000296589568540237556,
                      0.0000436624404335156298)
end

function Body.neptune ()
    return Body.new( 15.3796971148509165,
                    -25.9193146099879641,
                      0.179258772950371181,
                      0.00268067772490389322,
                      0.00162824170038242295,
                     -0.000095159225451971587,
                      0.0000515138902046611451)
end

function Body.sun ()
    return Body.new(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0)
end

end -- class Body

local NBodySystem = {_CLASS = 'NBodySystem'} do

local sqrt = math.sqrt

local function create_bodies ()
    local bodies = {Body.sun(),
                    Body.jupiter(),
                    Body.saturn(),
                    Body.uranus(),
                    Body.neptune()}
    local px, py, pz = 0.0, 0.0, 0.0
    for i = 1, #bodies do
        local b = bodies[i]
        px = px + b.vx * b.mass
        py = py + b.vy * b.mass
        pz = pz + b.vz * b.mass
    end
    bodies[1]:offset_momentum(px, py, pz)
    return bodies
end

function NBodySystem.new ()
    local obj = {bodies = create_bodies()}
    return setmetatable(obj, {__index = NBodySystem})
end

function NBodySystem:advance (dt)
    for i = 1, #self.bodies do
        local i_body = self.bodies[i]

        for j = i + 1, #self.bodies do
            local j_body = self.bodies[j]
            local dx = i_body.x - j_body.x
            local dy = i_body.y - j_body.y
            local dz = i_body.z - j_body.z

            local dSquared = dx * dx + dy * dy + dz * dz
            local distance = sqrt(dSquared)
            local mag = dt / (dSquared * distance)

            i_body.vx = i_body.vx - dx * j_body.mass * mag
            i_body.vy = i_body.vy - dy * j_body.mass * mag
            i_body.vz = i_body.vz - dz * j_body.mass * mag

            j_body.vx = j_body.vx + dx * i_body.mass * mag
            j_body.vy = j_body.vy + dy * i_body.mass * mag
            j_body.vz = j_body.vz + dz * i_body.mass * mag
        end
    end

    for i = 1, #self.bodies do
        local body = self.bodies[i]
        body.x = body.x + dt * body.vx
        body.y = body.y + dt * body.vy
        body.z = body.z + dt * body.vz
    end
end

function NBodySystem:energy ()
    local e = 0.0

    for i = 1, #self.bodies do
        local i_body = self.bodies[i]

        e = e + 0.5 * i_body.mass * (i_body.vx * i_body.vx +
                                     i_body.vy * i_body.vy +
                                     i_body.vz * i_body.vz)

        for j = i + 1, #self.bodies do
            local j_body = self.bodies[j]

            local dx = i_body.x - j_body.x
            local dy = i_body.y - j_body.y
            local dz = i_body.z - j_body.z

            local distance = sqrt(dx * dx + dy * dy + dz * dz)
            e = e - (i_body.mass * j_body.mass) / distance;
        end
    end
    return e
end

end -- class NBodySystem

local nbody = {} do
setmetatable(nbody, {__index = require'benchmark'})

function nbody:inner_benchmark_loop (inner_iterations)
    local system = NBodySystem.new()
    for _ = 1, inner_iterations do
        system:advance(0.01)
    end
    return self:verify_result(system:energy(), inner_iterations)
end

function nbody:verify_result (result, inner_iterations)
    if     inner_iterations == 250000 then
        return result == -0.1690859889909308
    elseif inner_iterations ==   1 then
        return result == -0.16907495402506745
    else
        print(('No verification result for %d found'):format(inner_iterations))
        print(('Result is: %.14g'):format(result))
        return false
    end
end

end -- object nbody

return nbody
