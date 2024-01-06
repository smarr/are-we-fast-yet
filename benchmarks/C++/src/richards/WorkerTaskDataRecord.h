#include "RBObject.h"

namespace richards {
    class WorkerTaskDataRecord : public  RBObject {
        private:
            int _destination;
            int _count;

        public:
            WorkerTaskDataRecord();
                
            int getCount() const;
            void setCount(int aCount);
            int getDestination() const;
            void setDestination(int aHandler);
    };
}