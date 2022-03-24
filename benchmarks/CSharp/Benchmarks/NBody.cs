namespace AreWeFastYet;

public sealed class NBody : Benchmark
{
    public override bool InnerBenchmarkLoop(int innerIterations)
    {
        var system = new NBodySystem();
        for (var i = 0; i < innerIterations; i++)
        {
            system.Advance(0.01);
        }

        return VerifyResult(system.Energy(), innerIterations);
    }
    
    private static bool VerifyResult(double result, int innerIterations)
    {
        if (innerIterations == 250000)
        {
            return result == -0.1690859889909308;
        }
        if (innerIterations == 1)
        {
            return result == -0.16907495402506745;
        }
        Console.WriteLine("No verification result for " + innerIterations + " found");
        Console.WriteLine("Result is: " + result);
        return false;
    }

    public override object Execute()
    {
        throw new NotImplementedException();
    }

    public override bool VerifyResult(object result)
    {
        throw new NotImplementedException();
    }
}

public sealed class NBodySystem
{
    private readonly Body[] bodies;

    public NBodySystem()
    {
        bodies = CreateBodies();
    }

    public static Body[] CreateBodies()
    {
        Body[] bodies = new[] {Body.Sun,
                            Body.Jupiter,
                            Body.Saturn,
                            Body.Uranus,
                            Body.Neptune};

        var px = 0.0;
        var py = 0.0;
        var pz = 0.0;

        foreach (var b in bodies)
        {
            px += b.Vx * b.Mass;
            py += b.Vy * b.Mass;
            pz += b.Vz * b.Mass;
        }

        bodies[0].OffsetMomentum(px, py, pz);

        return bodies;
    }

    public void Advance(double dt)
    {
        for (int i = 0; i < bodies.Length; i++)
        {
            var iBody = bodies[i];
            for (int j = i + 1; j < bodies.Length; j++)
            {
                var jBody = bodies[j];
                double dx = iBody.X - jBody.X;
                double dy = iBody.Y - jBody.Y;
                double dz = iBody.Z - jBody.Z;

                double dSquared = (dx * dx) + (dy * dy) + (dz * dz);
                double distance = Math.Sqrt(dSquared);
                double mag = dt / (dSquared * distance);

                iBody.Vx -= dx * jBody.Mass * mag;
                iBody.Vy -= dy * jBody.Mass * mag;
                iBody.Vz -= dz * jBody.Mass * mag;
                jBody.Vx += dx * iBody.Mass * mag;
                jBody.Vy += dy * iBody.Mass * mag;
                jBody.Vz += dz * iBody.Mass * mag;
            }
        }

        foreach (var body in bodies)
        {
            body.X += dt * body.Vx;
            body.Y += dt * body.Vy;
            body.Z += dt * body.Vz;
        }
    }

    public double Energy()
    {
        var e = 0.0;

        for (var i = 0; i < bodies.Length; i++)
        {
            var iBody = bodies[i];
            e += 0.5 * iBody.Mass
                * ((iBody.Vx * iBody.Vx) +
                   (iBody.Vy * iBody.Vy) +
                   (iBody.Vz * iBody.Vz));

            for (var j = i + 1; j < bodies.Length; j++)
            {
                var jBody = bodies[j];
                var dx = iBody.X - jBody.X;
                var dy = iBody.Y - jBody.Y;
                var dz = iBody.Z - jBody.Z;

                var distance = Math.Sqrt((dx * dx) + (dy * dy) + (dz * dz));
                e -= iBody.Mass * jBody.Mass / distance;
            }
        }
        return e;
    }
}

public sealed class Body
{
    private const double PI = 3.141592653589793;
    private const double SOLAR_MASS = 4 * PI * PI;
    private const double DAYS_PER_YER = 365.24;

    public Body(double x, double y, double z, double vx, double vy, double vz, double mass)
    {
        X = x;
        Y = y;
        Z = z;
        Vx = vx * DAYS_PER_YER;
        Vy = vy * DAYS_PER_YER;
        Vz = vz * DAYS_PER_YER;
        Mass = mass * SOLAR_MASS;
    }

    public double X { get; set; }
    public double Y { get; set; }
    public double Z { get; set; }
    public double Vx { get; set; }
    public double Vy { get; set; }
    public double Vz { get; set; }
    public double Mass { get; init; }

    public static Body Jupiter => new(
         4.84143144246472090e+00,
        -1.16032004402742839e+00,
        -1.03622044471123109e-01,
         1.66007664274403694e-03,
         7.69901118419740425e-03,
        -6.90460016972063023e-05,
         9.54791938424326609e-04);
    public static Body Saturn => new(
         8.34336671824457987e+00,
         4.12479856412430479e+00,
        -4.03523417114321381e-01,
        -2.76742510726862411e-03,
         4.99852801234917238e-03,
         2.30417297573763929e-05,
         2.85885980666130812e-04);

    public static Body Uranus => new(
         1.28943695621391310e+01,
        -1.51111514016986312e+01,
        -2.23307578892655734e-01,
         2.96460137564761618e-03,
         2.37847173959480950e-03,
        -2.96589568540237556e-05,
         4.36624404335156298e-05);

    public static Body Neptune => new(
         1.53796971148509165e+01,
        -2.59193146099879641e+01,
         1.79258772950371181e-01,
         2.68067772490389322e-03,
         1.62824170038242295e-03,
        -9.51592254519715870e-05,
         5.15138902046611451e-05);

    public static Body Sun => new(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);

    internal void OffsetMomentum(double px, double py, double pz)
    {
        Vx = 0.0 - (px / SOLAR_MASS);
        Vy = 0.0 - (py / SOLAR_MASS);
        Vz = 0.0 - (pz / SOLAR_MASS);
    }
}
