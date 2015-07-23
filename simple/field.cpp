#include "field.h"
#include "stone.h"


bool Field::TryPutStone(Stone& stone, int base_x, int base_y, int manipulate_info) {
  int dx[] = {-1, 0, 0, 1}, // 隣接判定の上下左右
      dy[] = {0, -1, 1, 0};
  // 書き換えるので、もどせるようにしておく
  RawField backup_field = raw;
  int backup_score = score;
  // 石回す
  RawStone sraw = StoneRotate(stone.raw, manipulate_info);
  // score = 0なら隣接判定しない
  bool exist_neighbor = (score == 0);
  for (int x = 0; x < 8; ++x) {
    for (int y = 0; y < 8; ++y) {
      if (y + base_y < 0 || y + base_y >= 32 || x + base_x < 0 || x + base_x >= 32) { // 範囲チェック
        continue;
      }
      if (sraw[y][x] == '1') {
        if(raw[y + base_y][x + base_x] != '0') { //かぶるとき
          raw = backup_field;
          score = backup_score;
          return false;
        }
        if (! exist_neighbor) { //隣接判定
          for (int i = 0; i < 4; ++i) {
            if (y + base_y + dy[i] < 0 || y + base_y + dy[i] >= 32 || x + base_x + dx[i] < 0 || x + base_x + dx[i] >= 32) {
              continue;
            }
            if (raw[y + base_y + dy[i]][x + base_x + dx[i]] == '2') {
              exist_neighbor = true;
            }
          }
        }
        // 更新
        raw[y + base_y][x + base_x] = '2';
        ++score;
      }
    }
  }
  return true;
}
