pub struct Body {
    x: f64,
    y: f64,
    z: f64,
    vx: f64,
    vy: f64,
    vz: f64,
    mass: f64,
}

impl Body {
    #[allow(clippy::unreadable_literal, clippy::approx_constant)]
    const PI: f64 = 3.141592653589793;
    const SOLAR_MASS: f64 = 4.0 * Self::PI * Self::PI;
    const DAYS_PER_YEAR: f64 = 365.24;

    pub fn get_x(&self) -> f64 {
        self.x
    }

    pub fn get_y(&self) -> f64 {
        self.y
    }

    pub fn get_z(&self) -> f64 {
        self.z
    }

    pub fn get_vx(&self) -> f64 {
        self.vx
    }

    pub fn get_vy(&self) -> f64 {
        self.vy
    }

    pub fn get_vz(&self) -> f64 {
        self.vz
    }

    pub fn get_mass(&self) -> f64 {
        self.mass
    }

    pub fn set_x(&mut self, x: f64) {
        self.x = x;
    }

    pub fn set_y(&mut self, y: f64) {
        self.y = y;
    }

    pub fn set_z(&mut self, z: f64) {
        self.z = z;
    }

    pub fn set_vx(&mut self, vx: f64) {
        self.vx = vx;
    }

    pub fn set_vy(&mut self, vy: f64) {
        self.vy = vy;
    }

    pub fn set_vz(&mut self, vz: f64) {
        self.vz = vz;
    }

    pub fn offset_momentum(&mut self, px: f64, py: f64, pz: f64) {
        self.vx = 0.0 - (px / Self::SOLAR_MASS);
        self.vy = 0.0 - (py / Self::SOLAR_MASS);
        self.vz = 0.0 - (pz / Self::SOLAR_MASS);
    }

    fn new(x: f64, y: f64, z: f64, vx: f64, vy: f64, vz: f64, mass: f64) -> Body {
        Body {
            x,
            y,
            z,
            vx: vx * Self::DAYS_PER_YEAR,
            vy: vy * Self::DAYS_PER_YEAR,
            vz: vz * Self::DAYS_PER_YEAR,
            mass: mass * Self::SOLAR_MASS,
        }
    }

    pub fn jupiter() -> Body {
        #[allow(clippy::unreadable_literal, clippy::excessive_precision)]
        Body::new(
            4.84143144246472090e+00,
            -1.16032004402742839e+00,
            -1.03622044471123109e-01,
            1.66007664274403694e-03,
            7.69901118419740425e-03,
            -6.90460016972063023e-05,
            9.54791938424326609e-04,
        )
    }

    pub fn saturn() -> Body {
        #[allow(clippy::unreadable_literal, clippy::excessive_precision)]
        Body::new(
            8.34336671824457987e+00,
            4.12479856412430479e+00,
            -4.03523417114321381e-01,
            -2.76742510726862411e-03,
            4.99852801234917238e-03,
            2.30417297573763929e-05,
            2.85885980666130812e-04,
        )
    }

    pub fn uranus() -> Body {
        #[allow(clippy::unreadable_literal, clippy::excessive_precision)]
        Body::new(
            1.28943695621391310e+01,
            -1.51111514016986312e+01,
            -2.23307578892655734e-01,
            2.96460137564761618e-03,
            2.37847173959480950e-03,
            -2.96589568540237556e-05,
            4.36624404335156298e-05,
        )
    }

    pub fn neptune() -> Body {
        #[allow(clippy::unreadable_literal, clippy::excessive_precision)]
        Body::new(
            1.53796971148509165e+01,
            -2.59193146099879641e+01,
            1.79258772950371181e-01,
            2.68067772490389322e-03,
            1.62824170038242295e-03,
            -9.51592254519715870e-05,
            5.15138902046611451e-05,
        )
    }

    pub fn sun() -> Body {
        Body::new(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0)
    }
}
