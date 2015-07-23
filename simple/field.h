#ifndef FIELD_H_
#define FIELD_H_

#include "stone.h"
#include <array>

typedef std::array<std::array<char, 32>, 32> RawField;

class Field {
  private:
    int score;
  public:
    Field(): score(0) {}
    RawField raw;
    int Score() const {
      return score;
    }
    bool TryPutStone(Stone& s, int base_x, int base_y, int manipulate_info);
};

#endif
