#include <cstdio>
#include <vector>
#include <queue>
#include <deque>

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
  // フィールドの評価とか、石をおいた記録とかも変数で持つつもり
};

struct Argument {
  int i;
  Position pos;
  Field f;
  int score;
};

Stone stones[256 + 1];
Field initial_field;
int number_of_stones;
const int empty_val = -1;

int get() {
  return getc(stdin) - '0';
}

void read_br() {
  get();
  get();
};

void get_field() {
  for (int i = 0; i < stone_size; ++i) {
    for (int j = 0; j < field_size; ++j) {
      initial_field.raw[i][j] = 256;
    }
  }
  for (int i = 0; i < field_size; ++i) {
    for (int j = 0; j < field_size; ++j) {
      if (get() == 0) {
        initial_field.raw[i][j] = empty_val;
        initial_field.empties.push_back(Position{i, j});
      } else {
        initial_field.raw[i][j] = 257;
      }
    }
    read_br();
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
      printf("%d", f.raw[i][j] != empty_val);
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

int solve(Field f, Position p, int i, int nowscore) {
  if (i >= number_of_stones) {
    return 0;
  }

  std::vector<Position> next_positions;
  int score = 0;

  if (score = put_stone(f, p, i, next_positions)) {
    score += nowscore;
    if (score > max_score) {
      max_score = score;
      max_score_field = f;
      fprintf(stderr, "max score %d!\n,", score);
    }
    printf("score: %d\n", score);
    dump_field(f);
    for (auto && f_p : next_positions) {
      for (int j = i + 1; j < number_of_stones; ++j) {
        for (auto && s_p : stones[j].fills) {
          solve(f, Position{f_p.y - s_p.y, f_p.x - s_p.x}, j, score);
        }
      }
    }
  }

  return 0;
}


int main() {
  get_input();
  dump_stone(stones[0]);
  for (auto && s : stones[0].fills) {
    for (auto && f : initial_field.empties) {
      solve(initial_field, Position{f.y - s.y, f.x - s.x}, 0, 0);
    }
  }

  return 0;
}
