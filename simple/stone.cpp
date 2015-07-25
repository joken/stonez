#include "stone.h"

RawStone StoneRotate(RawStone stone, int manipulate_info) { //石回す
  RawStone rotated;
  switch (manipulate_info) {
    case ROTATE_90:
      for (int a = 0; a < 8; ++a) {
        for (int b = 0; b < 8; ++b) {
          rotated[b][7-a] = stone[a][b];
        }
      }
      return rotated;
    case ROTATE_180:
      return StoneRotate(StoneRotate(stone, ROTATE_90), ROTATE_90);
    case ROTATE_270:
      return StoneRotate(StoneRotate(stone, ROTATE_180), ROTATE_90);
    case REVERSE:
      for (int y = 0; y < 8; ++y) {
        for (int x = 0; x < 8; ++x) {
          rotated[y][x] = stone[y][7-x];
        }
      }
      return rotated;
    case REVERSE | ROTATE_90:
      return StoneRotate(StoneRotate(stone, ROTATE_90), REVERSE);
    case REVERSE | ROTATE_180:
      return StoneRotate(StoneRotate(stone, ROTATE_180), REVERSE);
    case REVERSE | ROTATE_270:
      return StoneRotate(StoneRotate(stone, ROTATE_270), REVERSE);
  }
  return stone; // 0のとき
}
