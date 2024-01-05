#include "NBodySystem.h"

using namespace std;

namespace nbody {
    NBodySystem::NBodySystem() {
        _bodiesSize = 5;
        _bodies = createBodies();
    }

    shared_ptr<Body>*  NBodySystem::createBodies() {
        shared_ptr<Body>*  bodies = new shared_ptr<Body>[5];
        bodies[0] = Body::sun();
        bodies[1] = Body::jupiter();
        bodies[2] = Body::saturn();
        bodies[3] = Body::uranus();
        bodies[4] = Body::neptune();

        double px = 0.0;
        double py = 0.0;
        double pz = 0.0;

        for (int i = 0; i < _bodiesSize; ++i) {
            px += bodies[i]->getVX() * bodies[i]->getMass();
            py += bodies[i]->getVY() * bodies[i]->getMass();
            pz += bodies[i]->getVZ() * bodies[i]->getMass();
        }

        bodies[0]->offsetMomentum(px, py, pz);

        return bodies;
    }

    void NBodySystem::advance(double dt) {
        for (int i = 0; i < _bodiesSize; ++i) {
            shared_ptr<Body> iBody = _bodies[i];

            for (int j = i + 1; j < _bodiesSize; ++j) {
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

        for (int i = 0; i < _bodiesSize; ++i) {
            _bodies[i]->setX(_bodies[i]->getX() + dt * _bodies[i]->getVX());
            _bodies[i]->setY(_bodies[i]->getY() + dt * _bodies[i]->getVY());
            _bodies[i]->setZ(_bodies[i]->getZ() + dt * _bodies[i]->getVZ());
        }
    }

    double NBodySystem::energy() {
        double e = 0.0;

        for (int i = 0; i < _bodiesSize; ++i) {
            shared_ptr<Body> iBody = _bodies[i];
            e += 0.5 * iBody->getMass()
                * (iBody->getVX() * iBody->getVX() +
                    iBody->getVY() * iBody->getVY() +
                    iBody->getVZ() * iBody->getVZ());

            for (int j = i + 1; j < _bodiesSize; ++j) {
                shared_ptr<Body> jBody = _bodies[j];
                double dx = iBody->getX() - jBody->getX();
                double dy = iBody->getY() - jBody->getY();
                double dz = iBody->getZ() - jBody->getZ();

                double distance = sqrt(dx * dx + dy * dy + dz * dz);
                e -= (iBody->getMass() * jBody->getMass()) / distance;
            }
        }
        delete[] _bodies;
        return e;
    }

}

  