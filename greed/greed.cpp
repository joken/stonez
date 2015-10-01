#include <cstdio>
#include <vector>
#include <string>
#include <sstream>

/* definitions */
const int stone_size = 8;
const int field_size = 32;
const int empty_val = -1;
const int filled_val = 256;
FILE* dumpout = stdout;

struct Position{
  int y, x;
  bool operator==(const Position&obj) const {
    return y == obj.y && x == obj.x;
  }
};

struct Stone {
  int raw[stone_size][stone_size];
  std::vector<Position> fills;
};

struct Field {
  int raw[field_size][field_size];
  std::vector<Position> empties;
  std::vector<std::string> answer;
};

int number_of_stones;
Stone stones[256 * 8];
Field initial_field;

int rotated(int n, int deg) {
  return n + number_of_stones * (deg/90 + 1);
}
int fliped(int n) {
  return n + number_of_stones * 4;
}

/* util */
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

/* dump */
void dump_stone(int n) {
  for (int i = 0; i < 8; ++i) {
    for (int j = 0; j < 8; ++j) {
      if (stones[n].raw[i][j] == empty_val) {
        putc('.', dumpout);
      } else {
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
        putc('.', dumpout);
      } else if (f.raw[i][j] == filled_val) {
        putc('#', dumpout);
      } else {
        putc('@', dumpout);
      }
    }
    putc('\n', dumpout);
  }
}

/* stone manipurates */
void rotate_stone(int n, int deg) {
  for (int i = 0; i < stones[n].fills.size(); ++i) {
    stones[rotated(n, deg)].fills.push_back(Position{stones[n].fills[i].x, 7 - stones[n].fills[i].y});
  }

  if (deg != 270) {
    rotate_stone(n, deg+90);
  }
}

void flip_stone(int n) {
  for (int i = 0; i < stones[n].fills.size(); ++i) {
    stones[fliped(n)].fills.push_back(Position{stones[n].fills[i].y, 7 - stones[n].fills[i].x});
  }
}

/* parsing */
void get_field() {
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
    rotate_stone(fliped(i), 90);
  }
}
void get_input() {
  get_field(); read_br();
  scanf("%d\n", &number_of_stones);
  get_stones();
}



/* main */
int main() {
  get_input();
  dump_field(initial_field);
}
