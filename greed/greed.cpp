#include <cstdio>
#include <vector>
#include <deque>
#include <string>
#include <sstream>

/* definitions */
const int stone_size = 8;
const int field_size = 32;
const int empty_val = -1; // 障害物やzkの無いことを表す
const int filled_val = 256; // 障害物を表す
FILE* dumpout = stdout; // dump系関数の出力先

struct Position {
  /*座標を表現するクラス*/
  int y, x;
  bool operator==(const Position&obj) const {
    return y == obj.y && x == obj.x;
  }
  bool operator<(const Position&obj) const {
    if (y == obj.y) {
      return x < obj.x;
    }
    return y < obj.y;
  }
};

struct Stone {
  /* 石 */
  int raw[stone_size][stone_size]; // empty_val -> 空き, otherwise -> うまり
  std::deque<Position> fills; // 埋まってる座標を持っておく
};

struct Field {
  /* フィールド */
  int raw[field_size][field_size];
  std::deque<std::string> answer; // 答えとなる石の置き方を持っておく
};

int number_of_stones; // 与えられる石の数
Stone stones[256 * 8]; // 石を持っておくインスタンス（回転反転を考慮して8倍とってある）
Field initial_field; // 初期フィールド状態
std::deque<Position> initial_empties; // 初期フィールドで空いている場所を探す

int rotated(int n, int deg) {
  /* n番の石をdeg度まわした石の番号を返す */
  return n + number_of_stones * (deg/90);
}
int fliped(int n) {
  /* n番の石を反転した時の石の番号を返す */
  return n + number_of_stones * 4;
}
int all_stones_num() {
  /* 全部で石が何個になるか */
  return 8*number_of_stones;
}

/* util */
std::string to_s(int n) {
  /* std::to_stringとかぶるけど、本番環境では必要になりそう*/
	std::stringstream ss;
	ss << n;
	return ss.str();
}
int get() {
  return getc(stdin) - '0';
}
void read_br() {
  /* CRLFを読み飛ばす */
  get();
  get();
};

/* dump */
void dump_stone(int n) {
  for (int i = 0; i < 8; ++i) {
    for (int j = 0; j < 8; ++j) {
      if (stones[n].raw[i][j] == empty_val) {
        /* 空き */
        putc('.', dumpout);
      } else {
        /* うまり */
        putc('@', dumpout);
      }
    }
    putc('\n', dumpout);
  }
}

void dump_field(Field& f) {
  for (int i = 0; i < field_size; ++i) {
    for (int j = 0; j < field_size; ++j) {
      if (f.raw[i][j] == empty_val) {
        /* あき */
        putc('.', dumpout);
      } else if (f.raw[i][j] == filled_val) {
        /* 障害物 */
        putc('#', dumpout);
      } else {
        /* 石 */
        putc('@', dumpout);
      }
    }
    putc('\n', dumpout);
  }
}

/* stone manipurates */
void init_stone(int n) {
  /* 石を初期化しておく */
  for (int i = 0; i < 8; ++i) {
    for (int j = 0; j < 8; ++j) {
      stones[n].raw[i][j] = empty_val;
    }
  }
}
void rotate_stone(int n, int deg) {
  /* 石を回す。deg = 90でよばれて、内部で180, 270をよぶ */
  init_stone(rotated(n, deg));
  for (int i = 0; i < stones[n].fills.size(); ++i) {
    int newy = stones[n].fills[i].x;
    int newx = 7 - stones[n].fills[i].y;
    stones[rotated(n, deg)].fills.push_back(Position{newy, newx});
    stones[rotated(n, deg)].raw[newy][newx] = stones[n].raw[stones[n].fills[i].y][stones[n].fills[i].x];
  }

  if (deg != 270) {
    rotate_stone(n, deg+90);
  }
}

void flip_stone(int n) {
  /* 石を反転させる */
  init_stone(fliped(n));
  for (int i = 0; i < stones[n].fills.size(); ++i) {
    int newy = stones[n].fills[i].y;
    int newx = 7 - stones[n].fills[i].x;
    stones[fliped(n)].fills.push_back(Position{newy, newx});
    stones[fliped(n)].raw[newy][newx] = stones[n].raw[stones[n].fills[i].y][stones[n].fills[i].x];
  }
}

/* parsing */
void get_field() {
  /* 初期フィールドを得る */
  for (int i = 0; i < field_size; ++i) {
    for (int j = 0; j < field_size; ++j) {
      if (get() == 0) {
        initial_field.raw[i][j] = empty_val;
      } else {
        initial_field.raw[i][j] = filled_val;
      }
    }
    read_br();
  }
}

void get_stone(int index) {
  /* 石をひとつ読む */
  for (int i = 0; i < stone_size; ++i) {
    for (int j = 0; j < stone_size; ++j) {
      if (get() == 0) {
        stones[index].raw[i][j] = empty_val;
      } else {
        stones[index].raw[i][j] = index;
        stones[index].fills.push_back(Position{i, j});
      }
    }
    read_br();
  }
}
void get_stones() {
  /* 石をnumber_of_stones個よむ */
  for (int i = 0; i < number_of_stones; ++i) {
    get_stone(i);
    read_br();

    flip_stone(i);
    rotate_stone(i, 90);
    rotate_stone(fliped(i), 90);
  }
}
void get_input() {
  /* 入力を読む */
  get_field(); read_br();
  scanf("%d\n", &number_of_stones);
  get_stones();
}

/* helper */
void create_candidates(std::deque<Position>& next_candidates, int n, std::deque<Position>& empties) {
  /* 石の埋まっている場所とフィールド上の候補から、次に石を置く可能性のある場所に置く*/
  next_candidates.resize(empties.size() * stones[n].fills.size());
  for (auto p1 : empties) {
    for (auto p2 : stones[n].fills) {
      next_candidates.push_back(Position{ p1.y - p2.y, p1.x - p2.x });
    }
  }
}

/* solver */
void solve() {
  /* とりあえずこれを呼んでsolveする */
  std::deque<Position> first_candidates;
  for (int i = 0; i < all_stones_num(); ++i) {
    create_candidates(first_candidates, i, initial_empties); // 一個目の石を置く場所の候補を生成
  }
}

/* main */
int main() {
  initial_empties.resize(1024);
  get_input();
  // dump_field(initial_field);
  // solve();
}
