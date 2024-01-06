#ifndef IDLETASKDATARECORD
#define IDLETASKDATARECORD

#include "RBObject.h"

namespace richards {
    class IdleTaskDataRecord : public RBObject {
        private: 

            int _control;
            int _count;

        public:
            IdleTaskDataRecord();
            
            int getControl() const;
            void setControl(int aNumber);
            int getCount() const;
            void setCount(int aCount);
    };
}

#endif //IDLETASKDATARECORD