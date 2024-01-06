#ifndef PACKET
#define PACKET

#include <string>
#include <memory>
#include "RBObject.h"

using namespace std;

namespace richards {

    class Packet : public RBObject {
        private:
            shared_ptr<Packet> _link;
            int _identity;
            int _kind;
            int _datum;
            int* _data;

        public:

            Packet(shared_ptr<Packet> link, int identity, int kind);

            int* getData();
            int getDatum() const;
            void setDatum(int someData);
            int getIdentity() const;
            void setIdentity(int anIdentity);
            int getKind() const;
            shared_ptr<Packet> getLink();
            void setLink(shared_ptr<Packet> aLink);
            string toString() const;

            static const int DATA_SIZE = 4;
    };
}

#endif //PACKET