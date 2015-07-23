#include<iostream>
#include <cstdlib>
#include <cstdio>
#include <array>
#include <cassert>

typedef std::array<std::array<char, 32>, 32> RawField;
typedef std::array<std::array<char, 8>, 8> RawStone;

const int ROTATE_90 = 1,
          ROTATE_180 = 2,
          ROTATE_270 = 4,
          REVERSE = 8;

class Stone {
  public:
    RawStone raw;
};

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


  int number_of_stone = 0;
  Stone reserved_stones[256];
  Field max_score_field; // 最終的にscoreが最大になっているfieldが入る

int Parse(Field* f, Stone* reserved) { // 今後、標準入力以外の場所から入力を受け付けるのかしら
  int number_of_stone;
  for (int i = 0; i < 32; ++i) {
    fread(f->raw[i].data(), sizeof(char[32]), 1, stdin);
    getchar(); //CR
    getchar(); //LF
  }
  scanf("\n%d\n", &number_of_stone);
  for (int i = 0; i < number_of_stone; ++i) {
    for (int j = 0; j < 8; ++j) {
      fread(reserved[i].raw[j].data(), sizeof(char[8]), 1, stdin);
      getchar(); //CR
      getchar(); //LF
    }
    getchar(); //CR
    getchar(); //LF
  }

  return number_of_stone;
}
void DumpField(const Field& f) {
  for (int i = 0; i < 32; ++i) {
    for (int j = 0; j < 32; ++j) {
      putc(f.raw[i][j], stdout);
    }
    puts("");
  }
}
void DumpStone(const Stone& s) {
  for (int j = 0; j < 8; ++j) {
    for (int k = 0; k < 8; ++k) {
      putc(s.raw[j][k], stdout);
    }
    puts("");
  }
}
void DumpStones() {
  for (int i = 0; i < number_of_stone; ++i) {
    DumpStone(reserved_stones[i]);
    puts("");
  }
}

void Solve(Field f, Field& max_score_field, const int look_nth_stone) {
  if (look_nth_stone > number_of_stone) { //終了判定
    if (f.Score() > max_score_field.Score()) {
      max_score_field = f;
    }
      return;
  }

  Solve(f, max_score_field, look_nth_stone + 1); // 石を置かない場合
  Field backup = f;

  for (int x = -7; x < 32; ++x) {
    for (int y = -7; y < 32; ++y) {
      for (int i = 0; i < 8; ++i) {
        if (f.TryPutStone(reserved_stones[look_nth_stone], x, y, i)) { //置いてみておけたら
          Solve(f, max_score_field, look_nth_stone+1);
          f = backup;
        }
      }
    }
  }
}



void test();
int main() {
  test();
  Field reserved_field;
  // get_problemfile();
  number_of_stone = Parse(&reserved_field, reserved_stones);
  Solve(reserved_field, max_score_field, 0);
  DumpField(max_score_field);
  // solve();
  // submit_answer();
  return 0;
}

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

//
void test() { //テスト走らせる
  RawStone rawstone;
  for (int x = 0; x < 8; ++x) {
    rawstone[0][x] = '1';
  }
  for (int y = 1; y < 8; ++y) {
    for (int x = 0; x < 8; ++x) {
      rawstone[y][x] = '0';
    }
  }
  RawStone rotated_90;
  for (int y = 0; y < 8; ++y) {
    rotated_90[y][7] = '1';
  }
  for (int y = 0; y < 8; ++y) {
    for (int x = 0; x < 7; ++x) {
      rotated_90[y][x] = '0';
    }
  }
  RawStone rotated_by_f = StoneRotate(rawstone, ROTATE_270|REVERSE);

  assert(rotated_by_f == rotated_90);
}
