#ifndef STONE_H_
#define STONE_H_

#include <array>

/*
 * 石
 */
typedef std::array<std::array<char, 8>, 8> RawStone;


/*
 * 回転・反転を示す定数
 */
const int ROTATE_90 = 1,
          ROTATE_180 = 2,
          ROTATE_270 = 4,
          REVERSE = 8;

class Stone {
  public:
    RawStone raw;
};

RawStone StoneRotate(RawStone stone, int manipulate_info); //石を回す関数
#endif
