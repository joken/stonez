#include <cstdio>
#include <vector>
#include <queue>
#include <deque>

struct Position{
  int y, x;
};
struct Stone {
  int raw[8][8];
  std::vector<Position> candidates;
};
struct Field {
  int raw[40][40];
  std::vector<Position> candidates;
  // フィールドの評価とか、石をおいた記録とかも変数で持つつもり
};

struct Argument {
  int i;
  Position pos;
  Field f;
  int score;
};

Stone stones[256];
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
  for (int i = 0; i < 8; ++i) {
    for (int j = 0; j < 40; ++j) {
      initial_field.raw[i][j] = 256;
    }
  }
  for (int i = 8; i < 40; ++i) {
    for (int j = 0; j < 40; ++j) {
      if (j < 8) {
        initial_field.raw[i][j] = 257;
      } else {
        if (get() == 0) {
          initial_field.raw[i][j] = empty_val;
          initial_field.candidates.push_back(Position{i, j});
        } else {
          initial_field.raw[i][j] = 257;
        }
      }
    }
    read_br();
  }
}

void get_stone(int index) {
  for (int i = 0; i < 8; ++i) {
    for (int j = 0; j < 8; ++j) {
      if (get() == 0) {
        stones[index].raw[i][j] = empty_val;
      } else {
        stones[index].raw[i][j] = index;
        stones[index].candidates.push_back(Position{i, j});
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
  for (int i = 0; i < 40; ++i) {
    for (int j = 0; j < 40; ++j) {
      printf("%d", f.raw[i][j] != empty_val);
    }
    printf("\n");
  }
}

void dump_stone(Stone &s) {
  for (int i = 0; i < 8; ++i) {
    for (int j = 0; j < 8; ++j) {
      printf("%d", s.raw[i][j] != empty_val);
    }
    printf("\n");
  }
}

int put_stone(Field& f, std::deque<Position>next_candidates, int n, Position pos) {
  Field backup = f;
  int score = 0;
  int dy[] = {-1, 0, 0, 1},
      dx[] = {0, -1, 1, 0};
  for (auto it = stones[n].candidates.begin(); it != stones[n].candidates.end(); ++it) {
    // if this stone cannot be put on this position
    if (f.raw[pos.y + it->y][pos.x + it->x] != empty_val) {
      f = backup;
      next_candidates.clear();
      return 0;
    }

    // set put mark
    ++score;
    f.raw[pos.y + it->y][pos.x + it->x] = n;

    // pick up next candidational positions
    for (int i = 0; i < 4; ++i) {
      int looky = pos.y + it->y + dy[i];
      int lookx = pos.x + it->x + dx[i];
      if (0 <= looky && looky < 40 && 0 <= lookx && lookx < 40 ){
        if (f.raw[looky][lookx] == empty_val) {
          next_candidates.push_back(Position{looky, lookx});
        }
      }
    }
  }
  return score;
}

void make_arguments(std::queue<Argument> &args, Field& f, int n, std::deque<Position>& next_candidates, int current_score) {
  for (auto stone_pos = stones[n].candidates.begin(); stone_pos != stones[n].candidates.end(); ++stone_pos) {
    for (auto field_pos = next_candidates.begin(); field_pos != next_candidates.end(); ++field_pos) {
      args.push(Argument{n, {field_pos->y - stone_pos->y, field_pos->x - stone_pos->x}, f, current_score});
    }
  }
}

int solve() {
  int max_score = -1;
  Field max_score_field;

  std::queue<Argument> args;
  std::deque<Position> next_candidates(initial_field.candidates.begin(), initial_field.candidates.end());

  make_arguments(args, initial_field, 0, next_candidates, 0);
  initial_field.candidates.clear();
  while (! args.empty()) {
    int score;
    if ((score = put_stone(args.front().f, next_candidates, args.front().i, args.front().pos)) != 0) {
      // 石をおいたあとの処理。
      // 次の候補を生成する
      //
      // 終了の条件と、最高のフィールドであるかどうかを調べる方法を考えること
      args.front().score += score;
      if (args.front().score > max_score) {
        max_score = args.front().score;
        max_score_field = args.front().f;
      }
      make_arguments(args, args.front().f, args.front().i + 1, next_candidates, args.front().score);
      fprintf(stderr, "score: %d\n", args.front().score);
      fprintf(stdout, "score: %d\n", args.front().score);
      dump_field(args.front().f);
      fprintf(stdout, "\n");
    }
    args.pop();
  }
  printf("max score is %d\n", max_score);
  dump_field(max_score_field);

  return 0;
}

int main() {
  get_input();
  dump_stone(stones[0]);
  solve();

  return 0;
}
