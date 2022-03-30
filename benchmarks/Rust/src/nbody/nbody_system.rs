use crate::nbody::body::Body;

pub struct NBodySystem {
    bodies: [Body; 5],
}

impl Default for NBodySystem {
    fn default() -> Self {
        Self {
            bodies: Self::create_bodies(),
        }
    }
}

impl NBodySystem {
    pub fn create_bodies() -> [Body; 5] {
        let mut bodies = [
            Body::sun(),
            Body::jupiter(),
            Body::saturn(),
            Body::uranus(),
            Body::neptune(),
        ];

        let mut px = 0.0;
        let mut py = 0.0;
        let mut pz = 0.0;

        for b in &bodies {
            px += b.get_vx() * b.get_mass();
            py += b.get_vy() * b.get_mass();
            pz += b.get_vz() * b.get_mass();
        }

        bodies[0].offset_momentum(px, py, pz);

        bodies
    }

    pub fn advance(&mut self, dt: f64) {
        let mut bodies = self.bodies.as_mut_slice();
        // This dance is because we are wanting to access multiple parts of the array mutably
        // simultaneously.  Would be more simple if we relied on the officials `itertools` crate,
        // but doing it manually here for simplicity.
        while let Some((i_body, j_bodies)) = bodies.split_first_mut() {
            for j_body in j_bodies.iter_mut() {
                let dx = i_body.get_x() - j_body.get_x();
                let dy = i_body.get_y() - j_body.get_y();
                let dz = i_body.get_z() - j_body.get_z();

                let d_squared = dx * dx + dy * dy + dz * dz;
                let distance = d_squared.sqrt();
                let mag = dt / (d_squared * distance);

                i_body.set_vx(i_body.get_vx() - dx * j_body.get_mass() * mag);
                i_body.set_vy(i_body.get_vy() - dy * j_body.get_mass() * mag);
                i_body.set_vz(i_body.get_vz() - dz * j_body.get_mass() * mag);

                j_body.set_vx(j_body.get_vx() + dx * i_body.get_mass() * mag);
                j_body.set_vy(j_body.get_vy() + dy * i_body.get_mass() * mag);
                j_body.set_vz(j_body.get_vz() + dz * i_body.get_mass() * mag);
            }
            bodies = j_bodies;
        }

        for body in &mut self.bodies {
            body.set_x(body.get_x() + dt * body.get_vx());
            body.set_y(body.get_y() + dt * body.get_vy());
            body.set_z(body.get_z() + dt * body.get_vz());
        }
    }

    pub fn energy(&self) -> f64 {
        let mut e = 0.0;

        for (i, i_body) in self.bodies.iter().enumerate() {
            e += 0.5
                * i_body.get_mass()
                * (i_body.get_vx() * i_body.get_vx()
                    + i_body.get_vy() * i_body.get_vy()
                    + i_body.get_vz() * i_body.get_vz());

            for j_body in self.bodies.iter().skip(i + 1) {
                let dx = i_body.get_x() - j_body.get_x();
                let dy = i_body.get_y() - j_body.get_y();
                let dz = i_body.get_z() - j_body.get_z();

                let distance = dx * dx + dy * dy + dz * dz;
                e -= (i_body.get_mass() * j_body.get_mass()) / distance.sqrt();
            }
        }

        e
    }
}
