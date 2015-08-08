#include "stone.h"
#include "field.h"
#include "dump.h"
#include "input.h"
#include "point.h"

#include <iostream>
#include <set>
#include <exception>

int number_of_stone = 0;
Stone reserved_stones[256];
Field max_score_field; // 最終的にscoreが最大になっているfieldが入る

void DumpStones() {
  for (int i = 0; i < number_of_stone; ++i) {
    DumpStone(reserved_stones[i]);
    puts("");
  }
}

void Solve(Field f, Field& max_score_field, const int look_nth_stone, std::set<Point>& candidate_points) {
  if ((f.Score() != 0 && candidate_points.empty()) ||
    (look_nth_stone > number_of_stone)) { // 最後の石まで行ったら
    if (f.Score() > max_score_field.Score()) { // より良いフィールドなら更新
      max_score_field = f;
    }
      return;
  }

  if (f.Score() == 0) { // 最初の石の時は全部候補にしちゃおう
    for (int y = -7; y < 32; ++y) {
      for (int x = -7; x < 32; ++x) {
        candidate_points.insert(Point(y, x));
      }
    }
  }

  Solve(f, max_score_field, look_nth_stone + 1, candidate_points); // 石を置かない場合
  Field backup = f;

  for (std::set<Point>::iterator it = candidate_points.begin(); it != candidate_points.end();) {
    for (int i = 0; i < 8; ++i) { // あらゆる向きで
      std::set<Point> next_candidates; // itの位置に置いた時、新しく候補となる位置
      if (f.TryPutStone(reserved_stones[look_nth_stone], it->x, it->y, i, &next_candidates)) { //置いてみておけたら
        next_candidates.insert(candidate_points.begin(), candidate_points.end()); // next_candidatesにcandidate_pointsをmerge
        next_candidates.erase(*it); // itの位置はもう置いたので削除する
        // 出力処理をここに書く
        Solve(f, max_score_field, look_nth_stone+1, next_candidates); // 置いた状態で次の石を探す
        f = backup;
      } else { // おけないなら候補から外そう
        it = candidate_points.erase(it);
        goto noinclement;
      }
    }
    ++it;
noinclement:;
  }
}

int main() {
  Field reserved_field;
  std::set<Point> candidate_points;
  // get_problemfile();
  number_of_stone = Parse(&reserved_field, reserved_stones);
  Solve(reserved_field, max_score_field, 0, candidate_points);
  DumpField(max_score_field);
  // solve();
  // submit_answer();
  return 0;
}
