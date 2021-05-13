# The Computer Language Benchmarks Game
# http://shootout.alioth.debian.org/
#
# Based on nbody.java and adapted based on the SOM version.
from math import sqrt

from benchmark import Benchmark

PI = 3.141592653589793
SOLAR_MASS = 4 * PI * PI
DAYS_PER_YER = 365.24


class Body:
    def __init__(self, x, y, z, vx, vy, vz, mass):
        self._x = x
        self._y = y
        self._z = z
        self._vx = vx * DAYS_PER_YER
        self._vy = vy * DAYS_PER_YER
        self._vz = vz * DAYS_PER_YER
        self._mass = mass * SOLAR_MASS

    def get_x(self):
        return self._x

    def get_y(self):
        return self._y

    def get_z(self):
        return self._z

    def get_vx(self):
        return self._vx

    def get_vy(self):
        return self._vy

    def get_vz(self):
        return self._vz

    def get_mass(self):
        return self._mass

    def set_x(self, val):
        self._x = val

    def set_y(self, val):
        self._y = val

    def set_z(self, val):
        self._z = val

    def set_vx(self, val):
        self._vx = val

    def set_vy(self, val):
        self._vy = val

    def set_vz(self, val):
        self._vz = val

    def offset_momentum(self, px, py, pz):
        self._vx = 0.0 - (px / SOLAR_MASS)
        self._vy = 0.0 - (py / SOLAR_MASS)
        self._vz = 0.0 - (pz / SOLAR_MASS)


def jupiter():
    return Body(
        4.84143144246472090e00,
        -1.16032004402742839e00,
        -1.03622044471123109e-01,
        1.66007664274403694e-03,
        7.69901118419740425e-03,
        -6.90460016972063023e-05,
        9.54791938424326609e-04,
    )


def saturn():
    return Body(
        8.34336671824457987e00,
        4.12479856412430479e00,
        -4.03523417114321381e-01,
        -2.76742510726862411e-03,
        4.99852801234917238e-03,
        2.30417297573763929e-05,
        2.85885980666130812e-04,
    )


def uranus():
    return Body(
        1.28943695621391310e01,
        -1.51111514016986312e01,
        -2.23307578892655734e-01,
        2.96460137564761618e-03,
        2.37847173959480950e-03,
        -2.96589568540237556e-05,
        4.36624404335156298e-05,
    )


def neptune():
    return Body(
        1.53796971148509165e01,
        -2.59193146099879641e01,
        1.79258772950371181e-01,
        2.68067772490389322e-03,
        1.62824170038242295e-03,
        -9.51592254519715870e-05,
        5.15138902046611451e-05,
    )


def sun():
    return Body(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0)


class NBodySystem:
    def __init__(self):
        self._bodies = self._create_bodies()

    @staticmethod
    def _create_bodies():
        bodies = [sun(), jupiter(), saturn(), uranus(), neptune()]

        px = 0.0
        py = 0.0
        pz = 0.0

        for b in bodies:
            px += b.get_vx() * b.get_mass()
            py += b.get_vy() * b.get_mass()
            pz += b.get_vz() * b.get_mass()

        bodies[0].offset_momentum(px, py, pz)
        return bodies

    def advance(self, dt):
        for i in range(len(self._bodies)):
            i_body = self._bodies[i]

            for j in range(i + 1, len(self._bodies)):
                j_body = self._bodies[j]

                dx = i_body.get_x() - j_body.get_x()
                dy = i_body.get_y() - j_body.get_y()
                dz = i_body.get_z() - j_body.get_z()

                d_squared = dx * dx + dy * dy + dz * dz
                distance = sqrt(d_squared)
                mag = dt / (d_squared * distance)

                i_body.set_vx(i_body.get_vx() - (dx * j_body.get_mass() * mag))
                i_body.set_vy(i_body.get_vy() - (dy * j_body.get_mass() * mag))
                i_body.set_vz(i_body.get_vz() - (dz * j_body.get_mass() * mag))

                j_body.set_vx(j_body.get_vx() + (dx * i_body.get_mass() * mag))
                j_body.set_vy(j_body.get_vy() + (dy * i_body.get_mass() * mag))
                j_body.set_vz(j_body.get_vz() + (dz * i_body.get_mass() * mag))

        for body in self._bodies:
            body.set_x(body.get_x() + dt * body.get_vx())
            body.set_y(body.get_y() + dt * body.get_vy())
            body.set_z(body.get_z() + dt * body.get_vz())

    def energy(self):
        e = 0.0

        for i in range(len(self._bodies)):
            i_body = self._bodies[i]
            e += (
                0.5
                * i_body.get_mass()
                * (
                    i_body.get_vx() * i_body.get_vx()
                    + i_body.get_vy() * i_body.get_vy()
                    + i_body.get_vz() * i_body.get_vz()
                )
            )

            for j in range(i + 1, len(self._bodies)):
                j_body = self._bodies[j]
                dx = i_body.get_x() - j_body.get_x()
                dy = i_body.get_y() - j_body.get_y()
                dz = i_body.get_z() - j_body.get_z()

                distance = sqrt(dx * dx + dy * dy + dz * dz)
                e -= (i_body.get_mass() * j_body.get_mass()) / distance
        return e


class NBody(Benchmark):
    def inner_benchmark_loop(self, inner_iterations):
        system = NBodySystem()
        for _ in range(inner_iterations):
            system.advance(0.01)

        return self._verify_result(system.energy(), inner_iterations)

    @staticmethod
    def _verify_result(result, inner_iterations):
        if inner_iterations == 250000:
            return result == -0.1690859889909308

        if inner_iterations == 1:
            return result == -0.16907495402506745

        print("No verification result for " + str(inner_iterations) + " found")
        print("Result is: " + str(result))
        return False

    def benchmark(self):
        raise Exception("Should never be reached")

    def verify_result(self, result):
        raise Exception("Should never be reached")
