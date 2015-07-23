#include "stone.h"
#include "field.h"
#include "dump.h"


#include<iostream>
#include <cstdlib>
#include <cstdio>
#include <cassert>



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
