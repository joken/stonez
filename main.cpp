/*
 * MEMO
 *  - 障害物はスコアに入らないので、スコアと障害物の両方を1として表すことはできない
 */

// -- Constant Values
enum STONE_MANIPULATION { // 石操作情報
  REVERSED = 1,     // これが立ってたら反転してる
  ROTATED_90 = 2,   // これが立ってたら90度回転
  ROTATED_180 = 4,  // これがry
  ROTATED_270 = 6,
};

// Type Declarations
using RawStone = std::array<std::array<char, 8>, 8>;
using RawField = std::array<std::array<char, 32, 32>;

class Stone;
class Field;

// Variable Declarations
Field max_score_field; // 最適な解があるField
Stone stones[256]; // 渡されるstoneを格納
uint8_t number_of_stones; // 渡されるstoneの数

// Type Definitions
class Stone {
public:
  RawStone raw;
};

class Field {
private:
  const Stone* put_stones[256];
  STONE_MANIPULATION[256] manipurations;
public:
  RawField raw;
public:
  void put_stone(const uint8_t n, STONE_MANIPURATION m) {
    put_stones[n] = &stones[n];
  }
  const uint16_t score() const;
};



// -- Function Declarations
void solve(Field f, uint8_t look_nth_stone);

void parse(Field*);
void parse_field(Field* f);
void parse_stone();

int main() {
  Field f;
  get_problem_file();
  parse(&f);
  solve(field, 0);
  submit();
  return 0;
}

// -- Function Definitions

/**
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

/*
 * parse_field
 *
 * 入力からField部分をパースする
 * -- Args
 *  Field* f: パースした結果を格納するfield
 *
 * -- 副作用
 *  stdin
 */
void parse_field(Field* f) {
  for (int i = 0; i < 32; ++i) {
    fgets(*field[i], 32, stdin);
    get();
  }
}

/*
 * parse_stone
 *
 * 入力からStone部分をパースする　
 *  
 * -- 副作用
 *  stdin
 *  stones
 */
void parse_stone() {
  for (int i = 0; i < n; ++i) for (int j = 0; j < 8; ++j) {
    fgets(stones[i][j], 8, stdin);
    get();
  }
}

/**
 * parse
 *
 * 入力をパースする
 *
 * -- Args
 *  Field* f: パースした結果を格納するFieldのポインタ
 *
 * -- 副作用
 *  stdin
 *  stones
 */
void parse(Field* f) {
  parse_field(&f);
  scanf("%d¥n", &n);
  for (int i = 0; i < n; ++i) {
    parse_stone();
    get(); // 改行読み捨て
  }
}
