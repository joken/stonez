#include<iostream>
#include <cstdlib>
#include <cstdio>
#include <array>
#include <utility>

#include <boost/test/unit_test.hpp>

using RawField = std::array<std::array<char, 32>, 32>;
using RawStone = std::array<std::array<char, 8>, 8>;

constexpr int ROTATE_90 = 1,
          ROTATE_180 = 2,
          ROTATE_270 = 4,
          REVERSED = 8;

class Field {
  private:
    int score = 0;
  public:
    RawField raw = {};
    int Score() const {
      return score;
    }
    bool TryPutStone(int stone_number, int base_x, int base_y, int manipulate_info);
};

class Stone {
  public:
    RawStone raw = {};
};

Field max_score_field;
Stone reserved_stones[256];
int number_of_stone = 0;


void Parse(Field* f) {
  for (int i = 0; i < 32; ++i) {
    fread(f->raw[i].data(), sizeof(char[32]), 1, stdin);
    getchar(); //CR
    getchar(); //LF
  }
  scanf("\n%d\n", &number_of_stone);
  for (int i = 0; i < number_of_stone; ++i) {
    for (int j = 0; j < 8; ++j) {
      fread(reserved_stones[i].raw[j].data(), sizeof(char[8]), 1, stdin);
      getchar(); //CR
      getchar(); //LF
    }
    getchar(); //CR
    getchar(); //LF
  }

}
void DumpField(const Field& f) {
  for (int i = 0; i < 32; ++i) {
    for (int j = 0; j < 32; ++j) {
      putc(f.raw[i][j], stdout);
    }
    puts("");
  }
}


void Solve(Field f, const int look_nth_stone) {
  if (look_nth_stone > number_of_stone) {
    if (f.Score() > max_score_field.Score()) {
      max_score_field = f;
    }
      return;
  }

  Solve(f, look_nth_stone + 1);
  Field backup = f;

  for (int x = -7; x < 32; ++x) {
    for (int y = -7; y < 32; ++y) {
      if (f.TryPutStone(look_nth_stone, x, y, 0)) {
        Solve(f, look_nth_stone+1);
        f = backup;
      }
    }
  }
}

void DumpStones() {
  for (int i = 0; i < number_of_stone; ++i) {
    for (int j = 0; j < 8; ++j) {
      for (int k = 0; k < 8; ++k) {
        putc(reserved_stones[i].raw[j][k], stdout);
      }
      puts("");
    }
    puts("");
  }
}
int main() {
  Field reserved_field;
  // get_problemfile();
  Parse(&reserved_field);
  Solve(reserved_field, 0);
  DumpField(max_score_field);
  // solve();
  // submit_answer();
  return 0;
}

RawStone StoneRotate(RawStone& stone, int manipulate_info) {
  RawStone rotated;
  switch (manipulate_info) {
    case ROTATE_90:
      for (int y = 0; y < 8; ++y) {
        for (int x = 0; x < 8; ++x){
          rotated[y][x] = stone[x][7-y];
        }
      }
      return rotated;
    case ROTATE_180:
      return StoneRotate(std::move(StoneRotate(stone, ROTATE_90)), ROTATE_90);
    case ROTATE_270:
      return StoneRotate(std::move(StoneRotate(stone, ROTATE_180)), ROTATE_90);
    case REVERSED:
      for (int y = 0; y < 8; ++y) {
        for (int x = 0; x < 8; ++x) {
          rotated[y][x] = stone[y][7-x];
        }
      }
      return rotated;
    case REVERSED | ROTATE_90:
      return StoneRotate(std::move(StoneRotate(stone, ROTATE_90), REVERSED);
    case REVERSED | ROTATE_180:
      return StoneRotate(std::move(StoneRotate(stone, ROTATE_180), REVERSED);
    case REVERSED | ROTATE_270:
      return StoneRotate(std::move(StoneRotate(stone, ROTATE_270), REVERSED);
  }
  return stone;
}

bool Field::TryPutStone(int stone_number, int base_x, int base_y, int manipulate_info) {
  // 書き換えるので、もどせるようにしておく
  RawField backup_field = raw;
  int backup_score = score;
  RawStone& sraw = reserved_stones[stone_number].raw;
  for (int x = 0; x < 8; ++x) {
    for (int y = 0; y < 8; ++y) {
      if (y + base_y < 0 || y + base_y >= 32 || x + base_x < 0 || x + base_x >= 32) {
        continue;
      }
      if (sraw[y][x] == '1') {
        if(backup_score != 0 && raw[y + base_y][x + base_x] == '1') { //score = 1、つまり最初に置く石なら判定しない
          raw = backup_field;
          score = backup_score;
          return false;
        }
        raw[y + base_y][x + base_x] = '1';
        ++score;
      }
    }
  }
  return true;
}

//
BOOST_AUTO_TEST_CASE(StoneRotate_Test) {
  RawStone rawstone = {
    "11111111",
    "00000000",
    "00000000",
    "00000000",
    "00000000",
    "00000000",
    "00000000",
    "00000000"
  };
  RawStone rotated_90 = {
    "00000001",
    "00000001",
    "00000001",
    "00000001",
    "00000001",
    "00000001",
    "00000001",
    "00000001"
  };
  RawStone rotated_by_f = std::move(StoneRotate(rawstone));

  BOOST_CHECK_EQUAL(rotated_90, rotated_by_f);
}
