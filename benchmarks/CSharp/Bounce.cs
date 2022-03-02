using System;
using System.Collections;

public class Bounce : Benchmark
{

  public class Ball
  {
    private int x;
    private int y;
    private int xVel;
    private int yVel;

    public Ball(Random random)
    {
      x = random.Next() % 500;
      y = random.Next() % 500;
      xVel = (random.Next() % 300) - 150;
      yVel = (random.Next() % 300) - 150;
    }

    public bool bounce()
    {
      int xLimit = 500;
      int yLimit = 500;
      bool bounced = false;

      x += xVel;
      y += yVel;
      if (x > xLimit) { x = xLimit; xVel = 0 - Math.Abs(xVel); bounced = true; }
      if (x < 0) { x = 0; xVel = Math.Abs(xVel); bounced = true; }
      if (y > yLimit) { y = yLimit; yVel = 0 - Math.Abs(yVel); bounced = true; }
      if (y < 0) { y = 0; yVel = Math.Abs(yVel); bounced = true; }
      return bounced;
    }
  }

  public static void Main(string[] args)
  {
    Bounce s = new Bounce();
    Object result = s.benchmark();
    bool rr = s.verifyResult(result);
  }

  public override Object benchmark()
  {
    Random random = new Random();

    int ballCount = 100;
    int bounces = 0;
    Ball[] balls = new Ball[ballCount];

    for (int i = 0; i < ballCount; i++)
    {
      balls[i] = new Ball(random);
    }

    for (int i = 0; i < 50; i++)
    {
      foreach (Ball ball in balls)
      {
        if (ball.bounce())
        {
          bounces += 1;
        }
      }
    }
    return bounces;
  }

  public override bool verifyResult(Object result)
  {
    return 1331 == (int)result;
  }
}
