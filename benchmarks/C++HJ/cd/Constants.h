#ifndef CONSTANTS
#define CONSTANTS

namespace CD {
    class Constants {
        public: 
            constexpr static const double MIN_X = 0.0;
            constexpr static const double MIN_Y = 0.0;
            constexpr static const double MAX_X = 1000.0;
            constexpr static const double MAX_Y = 1000.0;
            constexpr static const double MIN_Z = 0.0;
            constexpr static const double MAX_Z = 10.0;
            constexpr static const double PROXIMITY_RADIUS = 1.0;
            constexpr static const double GOOD_VOXEL_SIZE  = PROXIMITY_RADIUS * 2.0;
    };
};

#endif //CONSTANTS