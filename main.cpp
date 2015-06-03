
using RawStone = std::array<std::array<char, 8>, 8>;
using RawField = std::array<std::array<char, 32, 32>;

class Field {
  RawField raw;
};

class Stone {
  RawStone raw;
};

class StoneManipurator {
};


enum STONE_MANIPURATION { // 石操作情報
  REVERSED = 1,     // これが立ってたら反転してる
  ROTATED_90 = 2,   // これが立ってたら90度回転
  ROTATED_180 = 4,  // これがry
  ROTATED_270 = 6,
};

Field max_score_field; // 最適な解があるField

/*
 * solve
 *
 * 問題を解くぞい
 *
 * -- Args
 *  Field f: 現在のフィールドの状態
 *  uint8_t look_nth_stone: 何番の石を置こうとしているか
 *
 * -- Return
 *  void
 *
 * -- 副作用
 *  max_score_field
 */
void solve(Field f, uint8_t look_nth_stone) {
  if (look_nth_stone >= number_of_stones) { // 一番深いところでスコア更新
    if (f.score() > max_score_field.score()) { // より良い結果が出るならそれを最適解フィールドとして登録
      max_score_field = f;
    }
    return;
  }

  solve(f, look_nth_stone + 1); // 今回は石を置かなかった 
  Field field_backup = f;

  for (int y = 0; y < 32; ++y) for (int x = 0; x < 32; ++x) { // 32 * 32のフィールドのどこかに置く
    Position p(x, y);
    for (int i = 0; i < 8; ++i) { // 4回転 * 反転
      if (f.try_put_stone(look_nth_stone, p, i)) { // 置いてみる。おけなかったらfalse
        solve(f, look_nth_stone + 1); // 置く
        f = field_backup;
      }
    }
  }
}

