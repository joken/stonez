#include <cstdio>
#include <cstdlib>
#include <vector>
#include <queue>
#include <deque>
#include <string>
#include <sstream>

const int stone_size = 8;
const int field_size = 32;

struct Position{
  int y, x;
};
struct Stone {
  int raw[stone_size][stone_size];
  std::vector<Position> fills;
};
struct Field {
  int raw[field_size][field_size];
  std::vector<Position> empties;
  std::vector<std::string> answer;
  // フィールドの評価とか、石をおいた記録とかも変数で持つつもり
};

struct Argument {
  int i;
  Position pos;
  Field f;
  int score;
};

Stone stones[256 * 8 + 1];
/*
 * rot90  256 - 511
 * rot180 512 - 767
 * rot270 768 - 1023
 * fliped 1024 - 1279
 * rot90  + fliped 1280 - 1535
 * rot180 + fliped 1536 - 1791
 * rot270 + fliped 1792 - 2047
 */
const int fliped = 1024;
const int rot[] = {0, 256, 512, 768};
Field initial_field;
int number_of_stones;
const int empty_val = -1;
const int filled_val = 256;

std::string to_string(int n) {
	std::stringstream ss;
	ss << n;
	return ss.str();
}
int get() {
  return getc(stdin) - '0';
}

void read_br() {
  get();
  get();
};

void get_field() {
  for (int i = 0; i < field_size; ++i) {
    for (int j = 0; j < field_size; ++j) {
      if (get() == 0) {
        initial_field.raw[i][j] = empty_val;
        initial_field.empties.push_back(Position{i, j});
      } else {
        initial_field.raw[i][j] = filled_val;
      }
    }
    read_br();
  }
}

void rotate_stone(int n, int deg) {
  for (int i = 0; i < stones[n].fills.size(); ++i) {
    stones[n + rot[deg/90]].fills.push_back(Position{stones[n].fills[i].x, 7 - stones[n].fills[i].y});
  }

  if (deg != 270) {
    rotate_stone(n, deg+90);
  }
}

void flip_stone(int n) {
  for (int i = 0; i < stones[n].fills.size(); ++i) {
    stones[n + fliped].fills.push_back(Position{stones[n].fills[i].y, 7 - stones[n].fills[i].x});
  }
}
void get_stone(int index) {
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
  for (int i = 0; i < number_of_stones; ++i) {
    get_stone(i);
    read_br();

    flip_stone(i);
    rotate_stone(i, 90);
    rotate_stone(i + fliped, 90);
  }
}
void get_input() {
  get_field(); read_br();
  scanf("%d\n", &number_of_stones);
  get_stones();
}

void dump_field(Field &f) {
  for (int i = 0; i < field_size; ++i) {
    for (int j = 0; j < field_size; ++j) {
      if (f.raw[i][j] == empty_val) {
        putc('.', stdout);
      } else if (f.raw[i][j] == filled_val) {
        putc('#', stdout);
      } else {
        putc('@', stdout);
      }
      // printf("%d", f.raw[i][j] != empty_val);
    }
    printf("\n");
  }
}

void dump_stone(Stone &s) {
  for (int i = 0; i < stone_size; ++i) {
    for (int j = 0; j < stone_size; ++j) {
      printf("%d", s.raw[i][j] != empty_val);
    }
    printf("\n");
  }
}

Field max_score_field;
int max_score = -1;

int put_stone(Field& f, Position p, int n, std::vector<Position>& next_positions) {
  Field backup = f;
  int score = 0;
  int dy[] = {-1, 0, 0, 1},
      dx[] = {0, -1, 1, 0};
  for (auto it = stones[n].fills.begin(); it != stones[n].fills.end(); ++it) {
    // if this stone cannot be put on this position
    if (f.raw[p.y + it->y][p.x + it->x] != empty_val) {
      f = backup;
      next_positions.clear();
      return 0;
    }

    // set put mark
    ++score;
    f.raw[p.y + it->y][p.x + it->x] = n;

    // pick up next candidational positions
    for (int i = 0; i < 4; ++i) {
      int looky = p.y + it->y + dy[i];
      int lookx = p.x + it->x + dx[i];
      if (0 <= looky && looky < field_size && 0 <= lookx && lookx < field_size ){
        if (f.raw[looky][lookx] == empty_val) {
          next_positions.push_back(Position{looky, lookx});
        }
      }
    }
  }
  return score;
}

void print_answer(std::vector<std::string>& ans) {
  for (int i = 0; i < number_of_stones; ++i) {
    printf("%s\r\n", ans[i].c_str());
  }
}

std::string make_answer(Position p, int flip, int rotate) {
  return to_string(p.x) + " " + to_string(p.y) + " " + ((flip == 0) ? "H" : "T") + " " + to_string(rotate);
}

int solve(Field f, Position p, int flip, int rotate, int i, int nowscore) {
  if (i >= number_of_stones) {
    return 0;
  }
  int u = i;

  if (flip == 1) {
    u += fliped;
  }

  u += rot[rotate / 90];

  std::vector<Position> next_positions;
  int score = 0;

  if (score = put_stone(f, p, u, next_positions)) {
    score += nowscore;
    if (score > max_score) {
      max_score = score;
      max_score_field = f;
      fprintf(stderr, "max score %d!\n", score);
    }
    f.answer[i] = make_answer(p, flip, rotate);
    printf("score: %d\n", score);
    dump_field(f);
    print_answer(f.answer);
    int rot_buf[] = {0, 90, 180, 270};
    for (auto && f_p : next_positions) {
      for (int j = i + 1; j < number_of_stones; ++j) {
        for (auto && s_p : stones[j].fills) {
          for (int k = 0; k < 4; ++k) {
            solve(f, Position{f_p.y - s_p.y, f_p.x - s_p.x}, 0, rot_buf[k], j, score);
            solve(f, Position{f_p.y - s_p.y, f_p.x - s_p.x}, 1, rot_buf[k], j, score);
          }
        }
      }
    }
  }

  return 0;
}


int main() {
  initial_field.answer.resize(256);
  get_input();
  dump_stone(stones[0]);
  int rot_buf[] = {0, 90, 180, 270};
  for (int i = 0; i < number_of_stones; ++i) {
    for (auto && s : stones[i].fills) {
      for (auto && f : initial_field.empties) {
        for (int k = 0; k < 4; ++k) {
          solve(initial_field, Position{f.y - s.y, f.x - s.x}, 0, rot_buf[k], i, 0);
          solve(initial_field, Position{f.y - s.y, f.x - s.x}, 1, rot_buf[k], i, 0);
        }
      }
    }
  }

  return 0;
}
