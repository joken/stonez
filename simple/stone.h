#ifndef STONE_H_
#define STONE_H_

#include <array>

typedef std::array<std::array<char, 8>, 8> RawStone;

const int ROTATE_90 = 1,
          ROTATE_180 = 2,
          ROTATE_270 = 4,
          REVERSE = 8;

class Stone {
  public:
    RawStone raw;
};

RawStone StoneRotate(RawStone stone, int manipulate_info);
#endif
