#ifndef FIELD_H_
#define FIELD_H_

#include "stone.h"
#include "point.h"
#include <array>
#include <set>

/*
 * フィールド
 */
typedef std::array<std::array<char, 32>, 32> RawField;

class Field {
  private:
    int score; //置かれた石の面積
  public:
    Field(): score(0) {}
    RawField raw;
    int Score() const {
      return score;
    }
    bool TryPutStone(Stone& s, int base_x, int base_y, int manipulate_info, std::set<Point>* next_candidates); //石を置いてみる処理
};

#endif
