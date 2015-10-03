#include <cstdio>
#include <vector>
#include <string>
#include <sstream>

/* definitions */
const int stone_size = 8;
const int field_size = 32;
const int empty_val = -1; // 障害物やzkの無いことを表す
const int filled_val = 256; // 障害物を表す
FILE* dumpout = stdout; // dump系関数の出力先

struct Position{
  /*座標を表現するクラス*/
  int y, x;
  bool operator==(const Position&obj) const {
    return y == obj.y && x == obj.x;
  }
};

struct Stone {
  /* 石 */
  int raw[stone_size][stone_size]; // empty_val -> 空き, otherwise -> うまり
  std::vector<Position> fills; // 埋まってる座標を持っておく
};

struct Field {
  /* フィールド */
  int raw[field_size][field_size];
  std::vector<std::string> answer; // 答えとなる石の置き方を持っておく
};

int number_of_stones; // 与えられる石の数
Stone stones[256 * 8]; // 石を持っておくインスタンス（回転反転を考慮して8倍とってある）
Field initial_field; // 初期フィールド状態

int rotated(int n, int deg) {
  /* n番の石をdeg度まわした石の番号を返す */
  return n + number_of_stones * (deg/90 + 1);
}
int fliped(int n) {
  /* n番の石を反転した時の石の番号を返す */
  return n + number_of_stones * 4;
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
void rotate_stone(int n, int deg) {
  /* 石を回す。deg = 90でよばれて、内部で180, 270をよぶ */
  for (int i = 0; i < stones[n].fills.size(); ++i) {
    stones[rotated(n, deg)].fills.push_back(Position{stones[n].fills[i].x, 7 - stones[n].fills[i].y});
  }

  if (deg != 270) {
    rotate_stone(n, deg+90);
  }
}

void flip_stone(int n) {
  /* 石を反転させる */
  for (int i = 0; i < stones[n].fills.size(); ++i) {
    stones[fliped(n)].fills.push_back(Position{stones[n].fills[i].y, 7 - stones[n].fills[i].x});
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



/* main */
int main() {
  get_input();
  dump_field(initial_field);
}
