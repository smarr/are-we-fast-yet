using Random = Benchmarks.Som.Random;

namespace Benchmarks;

internal sealed class Bounce : Benchmark
{
    private sealed class Ball
    {
        private int _x;
        private int _y;
        private int _xVel;
        private int _yVel;

        public Ball(Random random)
        {
            _x = random.Next() % 500;
            _y = random.Next() % 500;
            _xVel = random.Next() % 300 - 150;
            _yVel = random.Next() % 300 - 150;
        }

        public bool Bounce()
        {
            const int XLimit = 500;
            const int YLimit = 500;
            bool bounced = false;

            _x += _xVel;
            _y += _yVel;
            if (_x > XLimit)
            {
                _x = XLimit;
                _xVel = 0 - Math.Abs(_xVel);
                bounced = true;
            }

            if (_x < 0)
            {
                _x = 0;
                _xVel = Math.Abs(_xVel);
                bounced = true;
            }

            if (_y > YLimit)
            {
                _y = YLimit;
                _yVel = 0 - Math.Abs(_yVel);
                bounced = true;
            }

            if (_y < 0)
            {
                _y = 0;
                _yVel = Math.Abs(_yVel);
                bounced = true;
            }

            return bounced;
        }
    }

    public override object Execute()
    {
        Random random = new();

        const int BallCount = 100;
        int bounces = 0;
        Ball[] balls = new Ball[BallCount];

        for (int i = 0; i < BallCount; i++)
        {
            balls[i] = new Ball(random);
        }

        for (int i = 0; i < 50; i++)
        {
            foreach (Ball ball in balls)
            {
                if (ball.Bounce())
                {
                    bounces += 1;
                }
            }
        }

        return bounces;
    }

    public override bool VerifyResult(object result) => 1331 == (int)result;
}
