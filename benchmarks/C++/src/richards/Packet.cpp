#include "Packet.h"
#include <utility>


namespace richards {
    Packet::Packet(shared_ptr<Packet> link, int identity, int kind) {
        _link = link;
        _identity = identity;
        _kind = kind;
        _datum = 0;
        _data = new int[DATA_SIZE];
    }

    int* Packet::getData() { 
        return _data; 
    }

    int Packet::getDatum() const {
        return _datum; 
    }

    void Packet::setDatum(int someData) { 
        _datum = someData; 
    }


    int Packet::getIdentity() const {
        return _identity; 
    }
            
    void Packet::setIdentity(int anIdentity) { 
        _identity = anIdentity; 
    }

    int Packet::getKind() const {
        return _kind; 
    }
            
    shared_ptr<Packet> Packet::getLink() { 
        return _link; 
    }
            
    void Packet::setLink(shared_ptr<Packet> aLink) { 
        _link = aLink;
    }

    string Packet::toString() const {
        return "Packet id: " + to_string(_identity) + " kind: " + to_string(_kind);
    }
}
