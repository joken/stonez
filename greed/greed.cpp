#include <cstdio>
#include <vector>
#include <map>
#include <deque>
#include <string>
#include <algorithm>
#include <sstream>

/* definitions */
const int stone_size = 8;
const int field_size = 32;
const int empty_val = 257; // 障害物やzkの無いことを表す
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

struct OperatedStone {
  int n;
  bool flip;
  int deg;
};

struct Field {
  /* フィールド */
  int raw[field_size][field_size];
  std::deque<std::string> answer; // 答えとなる石の置き方を持っておく
  std::map<Position, std::deque<OperatedStone>> placable_stones;
};

int number_of_stones; // 与えられる石の数
Stone stones[256 * 8]; // 石を持っておくインスタンス（回転反転を考慮して8倍とってある）
Field initial_field; // 初期フィールド状態
std::deque<Position> initial_empties; // 初期フィールドで空いている場所を探す

int rotated(int deg) {
  /* 石をdeg度まわした石の番号のバイアスを返す */
  return number_of_stones * (deg/90);
}
int fliped() {
  /* 石を反転した時の石の番号のバイアスを返す */
  return number_of_stones * 4;
}
int operated(bool flip, int deg) {
  /* 石を操作した時の石の番号のバイアスを返す */
  if (flip) {
    if (deg == 0) {
      return fliped();
    }
    return fliped() + rotated(deg);
  }
  return rotated(deg);
}
int rotated(int n, int deg) {
  /* 石をdeg度まわした石の番号を返す */
  return n + rotated(deg);
}
int fliped(int n) {
  /* 石を反転した時の石の番号を返す */
  return n + fliped();
}
int operated(int n, bool flip, int deg) {
  /* 石を操作した時の石の番号のバイアスを返す */
  return n + operated(flip, deg);
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

void dump_field(Field& f, std::deque<Position>& ps) {
  for (int i = 0; i < field_size; ++i) {
    for (int j = 0; j < field_size; ++j) {
      if (std::find(ps.begin(), ps.end(), Position{i, j}) != ps.end()) {
        /* 次の候補 */
        putc('_', dumpout);
      } else if (f.raw[i][j] == empty_val) {
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
        initial_empties.push_back(Position{i, j});
      } else {
        initial_field.raw[i][j] = filled_val;
      }
    }
    read_br();
  }
}

void get_placables(Field& f, int m, bool flip, int deg) {
  int n = operated(m, flip, deg);

  int y = stones[n].fills.begin()->y;
  int x = stones[n].fills.begin()->x;

  for (auto e : initial_empties) {
    bool flag = true;
    for (auto it = stones[n].fills.begin(); it != stones.fills.end(); ++it) {
      if (!(f.raw[it->y + e.y - y][it->x + e.x + x] == empty_val)) {
        flag = false;
        break;
      }
    }
    initial_field.placable_stones[e] = OperatedStone{m, flip, deg};
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

  /* フィールドにおけるのかを調べる */
  for (int deg = 0; deg <= 270; deg += 90) {
    for (bool flip = false; !flip; flip = true) {
        get_placables(initial_field, index, flip, deg);
    }
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
  // next_candidates.resize(empties.size() * stones[n].fills.size());
  for (auto p1 : empties) {
    for (auto p2 : stones[n].fills) {
      next_candidates.push_back(Position{ p1.y - p2.y, p1.x - p2.x });
    }
  }
}

bool pos_check(int y, int x) {
  /* 与えられた座標が、fieldからはみ出ていたらfalseを返す */
  return (0 <= y && y < field_size) && (0 <= x < field_size);
}

std::string create_answer_format(int n, bool flip, int deg, Position p) {
  /* 石を置いた様子から回答フォーマットに沿った文字列を返す */
  return to_s(p.x) + " " + to_s(p.y) + " " + (flip ? "T" : "H") + " " + to_s(deg);
}

void print_anser(Field& f) {
  for (auto l : f.answer) {
    printf("%s\r\n", l.c_str());
  }
}

/* solver */
int put_stone(int m, bool flip, int deg, Position base, Field& f, std::deque<Position>& next_candidates) {
  /* 石を置く処理。ついでに次に石を置けそうな場所を探す */
  int score = 0;
  int n = operated(m, flip, deg);
  bool flag = false; // となりに石があるかフラグ。本来不要
  Field backup = f;
  for (auto p : stones[n].fills) { // zkごとにおけるかどうかを判定する
    int y = base.y + p.y;
    int x = base.x + p.x;
    if (!pos_check(y, x) || f.raw[y][x] != empty_val) { // 置きたい場所が空いてないと置けない
      next_candidates.clear();
      f = backup;
      return 0;
    }


    score += 1;
    f.raw[y][x] = m;

    /* 置いたところは候補から外す */
    auto exists = std::find(next_candidates.begin(), next_candidates.end(), Position{y, x});
    if (exists != next_candidates.end()) {
      next_candidates.erase(exists);
    }

    /* 隣接している空きマスを次の候補に */
    int dy[] = {-1, 0, 0, 1};
    int dx[] = {0, -1, 1, 0};
    for (int i = 0; i < 4; ++i) {
      if (!flag &&
          pos_check(y + dy[i], x + dx[i]) &&
          f.raw[y + dy[i]][x + dx[i]] > n) {
        flag = true;
      }
      if (pos_check(y + dy[i], x + dx[i]) &&
          f.raw[y + dy[i]][x + dx[i]] == empty_val) {
        Position newpos{y+dy[i], x+dx[i]};
        if (std::find(next_candidates.begin(), next_candidates.end(), newpos) == next_candidates.end()) {
          next_candidates.push_back(newpos);
        }
      }
    }
  }

  if (!flag) {
      next_candidates.clear();
      f = backup;
      return 0;
  }

  return score;
}
void dfs(int nowscore, int n, bool fliped, int deg, Position p, Field f, std::deque<Position> next_empties) {
  /* n番の石をfliped + degの状態で、f上のpに置くところから深く */
  if (n >= number_of_stones) {
    return;
  }

  int score;

  if ((score = put_stone(n, fliped, deg, p, f, next_empties)) > 0) {
    score += nowscore;
    fprintf(stderr, "score - %d\n", score);
    /* 石を置いた時の処理 */
    f.answer[n] = create_answer_format(n, fliped, deg, p);

    printf("score: %d\n", score);
    dump_field(f, next_empties);
    print_anser(f);
    fflush(dumpout);
  }

  /* 次の石を置く処理 */
  std::deque<Position> next_candidates;

  for (int j = 0; j <= 270; j += 90) { // 回転
    for (bool k = false; !k; k = true) { // 反転
      for (int i = n + 1; i < number_of_stones; ++i) {
        create_candidates(next_candidates, operated(i, k, j), next_empties); // 石を置く場所の候補を生成
        for (auto p : next_candidates) {
         dfs(score, i, k, j, p, f, next_empties);
        }
      }
    }
  }
}
void solve() {
  /* とりあえずこれを呼んでsolveする */
  std::deque<Position> first_candidates; // 最初の石を置く場所
  std::deque<Position> empty_positions;
  empty_positions.empty();

  for (int j = 0; j <= 270; j += 90) { // 回転
    for (bool k = false; !k; k = true) { // 反転
      for (int i = 0; i < number_of_stones; ++i) {
        create_candidates(first_candidates, operated(i, k, j), initial_empties); // 一個目の石を置く場所の候補を生成
        for (auto p : first_candidates) {
          dfs(0, i, k, j, p, initial_field, empty_positions);
        }
      }
    }
  }
}

/* main */
int main() {
  get_input();
  initial_field.answer.resize(number_of_stones);
  solve();
}
