#include "RBObject.h"

namespace richards {
    class WorkerTaskDataRecord : public  RBObject {
        private:
            int _destination;
            int _count;

        public:
            WorkerTaskDataRecord() {
                _destination = HANDLER_A;
                _count = 0;
            }
                
            int getCount() const {
                return _count; 
            }

            void setCount(int aCount) { 
                _count = aCount; 
            }

            int getDestination() const {
                return _destination; 
            }

            void setDestination(int aHandler) { 
                _destination = aHandler; 
            }
    };
}