#ifndef TASKSTATE
#define TASKSTATE

#include "RBObject.h"

namespace richards {
    class TaskState : public RBObject {
        private: 

            bool _packetPending;
            bool _taskWaiting;
            bool _taskHolding;
        
        public: 
        
            bool isPacketPending() const;
            bool isTaskHolding() const;
            bool isTaskWaiting() const;
            void setTaskHolding(bool b);
            void setTaskWaiting(bool b);
            void setPacketPending(bool b);
            void packetPending();
            void running();
            void waiting();
            void waitingWithPacket();
            bool isTaskHoldingOrWaiting() const;
            bool isWaitingWithPacket() const;
            
            static std::shared_ptr<TaskState> createRunning();
            static std::shared_ptr<TaskState> createWaiting();
            static std::shared_ptr<TaskState> createWaitingWithPacket();

    };
}

#endif //TASKSTATE