#include "stone.h"
#include "field.h"
#include "dump.h"
#include "input.h"


#include <iostream>

int number_of_stone = 0;
Stone reserved_stones[256];
Field max_score_field; // 最終的にscoreが最大になっているfieldが入る

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
