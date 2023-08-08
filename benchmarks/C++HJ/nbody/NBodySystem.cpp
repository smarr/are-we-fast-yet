/* The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Based on nbody.java and adapted basde on the SOM version.
 */

#include <vector>
#include "Body.h"
#include <cmath>

namespace nbody {
    class NBodySystem {
        private:
            vector<shared_ptr<Body>> _bodies;

        public: 
            NBodySystem() {
                _bodies = createBodies();
            }

            vector<shared_ptr<Body>>  createBodies() {
                vector<shared_ptr<Body>>  bodies = {Body::sun(),
                                            Body::jupiter(),
                                            Body::saturn(),
                                            Body::uranus(),
                                            Body::neptune()};

                double px = 0.0;
                double py = 0.0;
                double pz = 0.0;

                for (shared_ptr<Body> b : bodies) {
                    px += b->getVX() * b->getMass();
                    py += b->getVY() * b->getMass();
                    pz += b->getVZ() * b->getMass();
                }

                bodies[0]->offsetMomentum(px, py, pz);

                return bodies;
            }

            void advance(double dt) {
                for (int i = 0; i < _bodies.size(); ++i) {
                    shared_ptr<Body> iBody = _bodies[i];

                    for (int j = i + 1; j < _bodies.size(); ++j) {
                        shared_ptr<Body> jBody = _bodies[j];
                        double dx = iBody->getX() - jBody->getX();
                        double dy = iBody->getY() - jBody->getY();
                        double dz = iBody->getZ() - jBody->getZ();

                        double dSquared = dx * dx + dy * dy + dz * dz;
                        double distance = sqrt(dSquared);
                        double mag = dt / (dSquared * distance);

                        iBody->setVX(iBody->getVX() - (dx * jBody->getMass() * mag));
                        iBody->setVY(iBody->getVY() - (dy * jBody->getMass() * mag));
                        iBody->setVZ(iBody->getVZ() - (dz * jBody->getMass() * mag));

                        jBody->setVX(jBody->getVX() + (dx * iBody->getMass() * mag));
                        jBody->setVY(jBody->getVY() + (dy * iBody->getMass() * mag));
                        jBody->setVZ(jBody->getVZ() + (dz * iBody->getMass() * mag));
                    }
                }

                for (shared_ptr<Body> body : _bodies) {
                    body->setX(body->getX() + dt * body->getVX());
                    body->setY(body->getY() + dt * body->getVY());
                    body->setZ(body->getZ() + dt * body->getVZ());
                }
            }

            double energy() {
                double e = 0.0;

                for (int i = 0; i < _bodies.size(); ++i) {
                    shared_ptr<Body> iBody = _bodies[i];
                    e += 0.5 * iBody->getMass()
                        * (iBody->getVX() * iBody->getVX() +
                            iBody->getVY() * iBody->getVY() +
                            iBody->getVZ() * iBody->getVZ());

                    for (int j = i + 1; j < _bodies.size(); ++j) {
                        shared_ptr<Body> jBody = _bodies[j];
                        double dx = iBody->getX() - jBody->getX();
                        double dy = iBody->getY() - jBody->getY();
                        double dz = iBody->getZ() - jBody->getZ();

                        double distance = sqrt(dx * dx + dy * dy + dz * dz);
                        e -= (iBody->getMass() * jBody->getMass()) / distance;
                    }
                }
                return e;
            }

    };
}

  