#include <iostream>
#include <cstdio>
#include <map>
#include <vector>
#include <deque>
#include <list>
#include <array>
#include <string>
#include <sstream>
#include <algorithm>
#include <random>

const int field_size = 32;
const int stone_size = 8;
const int empty_val = 256;
const int filled_val = 257;
const int normal = 0,
      rot90 = 1,
      rot180 = 2,
      rot270 = 3,
      fliped = 4;
std::string host;
std::string cur;
std::stringstream out;
std::ostream& dump_out = std::cerr;
std::istream& in = std::cin;
// std::ostream& out = buf; //std::cout;
std::ostream& err = std::cerr;
bool shuffle_flag = true;
bool dump_flag = false;

struct Position {
  int y, x;
  bool operator==(const Position& obj) const {
    return y == obj.y && x == obj.x;
  }
  bool operator<(const Position& obj) const {
    if (y == obj.y) return x < obj.x;
    return y < obj.y;
  }
};
struct OperatedStone {
  int i, j;
  Position p;
};
struct Field {
  std::array<std::array<int, field_size>, field_size> raw;
  std::map<Position, std::deque<OperatedStone>> candidates;
  std::vector<std::string> answer;
  void print_answer(int, int);
  void dump();
  void dump(std::deque<Position>&);
  void dump(std::deque<Position>&, Position p);
};
struct Stone {
  std::array<std::array<int, stone_size>, stone_size> raw;
  std::deque<Position> zks;
  void dump();
};

int get();
void br();

void parse_input();
void parse_field();
void parse_stones();
void parse_stone(int);

void get_candidates();
void solve();
int dfs(int, int, int, int, Position, Field, std::deque<Position>);
int put_stone(Field&, int, int, Position, std::deque<Position>&);
bool check_pos(int y, int x);

std::string answer_format(Position, int);
std::string to_s(int);
void operate(int);

int stone_number;
Field initial_field;
std::deque<Position> initial_empties;
std::array<std::array<Stone, 8>, 256> stones;
int max_score;

void solve() {
  if (shuffle_flag) {
    std::shuffle(initial_empties.begin(), initial_empties.end(), std::mt19937{std::random_device{}()});
  }
  std::deque<Position> empty_list; empty_list.clear();
  for (auto p : initial_empties) {
    for (auto s : initial_field.candidates[p]) {
      if (dfs(1, 0, s.i, s.j, s.p, initial_field, empty_list) != 0) {
        break;
      }
    }
  }
}
int bfs(int c, int nowscore, int n, int m, Position p, Field f, std::deque<Position> candidates) {
}
int dfs(int c, int nowscore, int n, int m, Position p, Field f, std::deque<Position> candidates) {
  int score = 0;

  if ((score = put_stone(f, n, m, p, candidates)) == 0) {
    return 1;
  }
  if (dump_flag) {
    f.dump(candidates);
  }

  f.answer[n] = answer_format(p, m);
  score += nowscore;
  if (max_score < score) {
    max_score = score;
    f.print_answer(score, c);
  }
  err << "score:" << score << ", ";
  for (auto np : candidates) {
    for (auto s : initial_field.candidates[np]) {
      if (s.i > n) {
        if (dfs(c+1, score, s.i, s.j, s.p, f, candidates) != 0) {
          break;
        }
      }
    }
  }
  return 0;
}
int put_stone(Field& f, int n, int m, Position p1, std::deque<Position>& next) {
  Field backup = f;
  int score = 0;

  for (auto p2 : stones[n][m].zks) {
    int y = p1.y + p2.y, x = p1.x + p2.x;

    if (f.raw[y][x] != empty_val) {
      f = backup;
      next.clear();
      return 0;
    }
    f.raw[y][x] = n;
    score += 1;
    auto exist = std::find(next.begin(), next.end(), Position{y, x});
    if (exist != next.end()) {
      next.erase(exist);
    }

    int dy[] = {-1, 0, 0, 1}, dx[] = {0, -1, 1, 0};
    for (int i = 0; i < 4; ++i) {
      int ny = y+dy[i], nx = x+dx[i];
      if (check_pos(ny, nx) &&
          f.raw[ny][nx] == empty_val) {
        next.emplace_back(Position{ny, nx});
      }
    }
  }

  return score;
}
bool check_pos(int y, int x) {
  return (0<= y && y < field_size) && (0 <= x && x < field_size);
}

int main(int argc, char**argv) {
  if (argc < 2) {
    err << "Usage ipv4\n";
    return 1;
  }
  cur = argv[0];
  host = argv[1];
  parse_input();
  get_candidates();
  solve();

  return 0;
}

// -------

void Field::dump() {
  for (int i = 0; i < field_size; ++i) {
    for (int j = 0; j < field_size; ++j) {
      if (raw[i][j] == empty_val) dump_out.put('.');
      else if (raw[i][j] < stone_number) dump_out.put('@');
      else                        dump_out.put('#');
    }
    dump_out.put('\n');
  }
}

void Field::dump(std::deque<Position>& ps) {
  for (int i = 0; i < field_size; ++i) {
    for (int j = 0; j < field_size; ++j) {
      if (raw[i][j] < stone_number) dump_out.put('@');
      else if (std::find(ps.begin(), ps.end(), Position{i, j}) != ps.end())
        dump_out.put('_');
      else if (raw[i][j] == empty_val) dump_out.put('.');
      else                        dump_out.put('#');
    }
    dump_out.put('\n');
  }
}
void Field::dump(std::deque<Position>& ps, Position p) {
  for (int i = 0; i < field_size; ++i) {
    for (int j = 0; j < field_size; ++j) {
      if (Position{i, j} == p) dump_out.put('*');
      else if (raw[i][j] < stone_number) dump_out.put('@');
      else if (std::find(ps.begin(), ps.end(), Position{i, j}) != ps.end())
        dump_out.put('_');
      else if (raw[i][j] == empty_val) dump_out.put('.');
      else                        dump_out.put('#');
    }
    dump_out.put('\n');
  }
}

void Stone::dump() {
  for (int i = 0; i < stone_size; ++i) {
    for (int j = 0; j < stone_size; ++j) {
      if (raw[i][j] == empty_val) dump_out.put('.');
      else                        dump_out.put('@');
    }
    dump_out.put('\n');
  }
}

int get() {
  int c = in.get() - '0';
  if (c == 0) return empty_val;
  return filled_val;
}

void br() {
  in.get(), in.get();
}

void parse_input() {
  parse_field();
  in >> stone_number;
  br();
  parse_stones();
  initial_field.answer.resize(stone_number);
}

void parse_field() {
  for (int i = 0; i < field_size; ++i) {
    for (int j = 0; j < field_size; ++j) {
      if ((initial_field.raw[i][j] = get()) == empty_val)
        initial_empties.emplace_back(Position{i, j});
    }
    br();
  }
}

void parse_stones() {
  for (int i = 0; i < stone_number; ++i) {
    parse_stone(i);
    br();
    operate(i);
  }
}

void parse_stone(int n) {
  for (int i = 0; i < stone_size; ++i) {
    for (int j = 0; j < stone_size; ++j) {
      if ((stones[n][normal].raw[i][j] = get()) != empty_val)
        stones[n][normal].zks.emplace_back(Position{i, j});
    }
    br();
  }
}

void operate(int n) {
  auto rotate_impl = [](int n, int m) {
    for (int i = 0; i < stone_size; ++i) {
      for (int j = 0; j < stone_size; ++j) {
        stones[n][m+1].raw[j][7-i] = stones[n][m].raw[i][j];
      }
    }
    for (auto p : stones[n][m].zks) {
      stones[n][m+1].zks.push_back(Position{p.x, 7-p.y});
    }
  };
  auto flip_impl = [](int n, int m) {
    for (int i = 0; i < stone_size; ++i) {
      for (int j = 0; j < stone_size; ++j) {
        stones[n][m+4].raw[i][7-j] = stones[n][m].raw[i][j];
      }
    }
    for (auto p : stones[n][m].zks) {
      stones[n][m+4].zks.push_back(Position{p.y, 7-p.x});
    }
  };

  rotate_impl(n, 0);
  rotate_impl(n, 1);
  rotate_impl(n, 2);
  flip_impl(n, 0);
  flip_impl(n, 1);
  flip_impl(n, 2);
  flip_impl(n, 3);
}

void get_candidates() {
  for (auto f_p : initial_empties) {
    for (int i = 0; i < stone_number; ++i) {
      for (int j = 0; j < 8; ++j) {
        int y = f_p.y - stones[i][j].zks[0].y,
            x = f_p.x - stones[i][j].zks[0].x;
        bool flag = true;
        for (auto s_p : stones[i][j].zks) {
          if (initial_field.raw[y + s_p.y][x + s_p.x] != empty_val) {
            flag = false;
            break;
          }
        }
        if (flag) {
        for (auto s_p : stones[i][j].zks) {
            initial_field.candidates[Position{f_p.y, f_p.x}].push_back(OperatedStone{i, j, Position{f_p.y - s_p.y, f_p.x - s_p.x}});
          }
        }
      }
    }
  }
}

std::string to_s(int n) {
  std::stringstream ss;
  ss << n;
  return ss.str();
}
std::string answer_format(Position p, int n) {
  std::string base = to_s(p.x) + " " + to_s(p.y)+ " ";
  base += (n & fliped) ? "H " : "T ";
  base += to_s((3 & fliped)*90);
  return base;
}

#if defined(_WIN32) || defined(WIN32) || defined(__CYGWIN__) || defined(__MINGW32__) || defined(WIN64) || defined(_WIN64)

#include <windows.h>

void WinSubmit();
void Field::print_answer(int score, int c) {

  out << initial_empties.size() - score << " " << c << " " << stone_number << "\r\n";
  for (int i = 0; i < stone_number; ++i) {
    out << answer[i] << "\r\n";
  }
  WinSubmit();
}
std::string getPath(std::string s) {
  auto p = s.rfind('\\');
  return s.substr(0, p);
}
void WinSubmit() {
  HANDLE readPipe = NULL, writePipe = NULL;
  HANDLE readTemp;
  HANDLE childProcess = NULL;

  CreatePipe(&readTemp, &writePipe, NULL, 0);
  DuplicateHandle(GetCurrentProcess(), readTemp, GetCurrentProcess(), &readPipe, 0, true, DUPLICATE_SAME_ACCESS);
  CloseHandle(readTemp);

  bool bInheritHandles = true;
  DWORD creationFlags = 0;

  STARTUPINFO si = {};
  si.cb = sizeof(STARTUPINFO);
  si.dwFlags = STARTF_USESTDHANDLES;
  si.hStdInput = readPipe;
  si.hStdOutput = GetStdHandle(STD_OUTPUT_HANDLE);
  si.hStdError = GetStdHandle(STD_ERROR_HANDLE);

  setCurrentDirectory(getPath(cur).c_str());

  PROCESS_INFORMATION pi = {};
  CreateProcess(NULL, LPTSTR(("java SubmitClient " + host).c_str()), NULL, NULL,bInheritHandles, creationFlags, NULL, NULL, &si, &pi);
  childProcess = pi.hProcess;
  CloseHandle(pi.hThread);
  CloseHandle(readPipe);
  readPipe = NULL;

  WriteFile(writePipe, out.str().c_str(), out.str().size(), NULL, NULL);
  CloseHandle(writePipe);
  writePipe = NULL;
  WaitForSingleObject(childProcess, INFINITE);
}
#else
void Field::print_answer(int score, int c) {
  FILE* client = popen(("java SubmitClient " + host).c_str(), "w");
  if (client == NULL) return;

  out << initial_empties.size() - score << " " << c << " " << stone_number << "\r\n";
  for (int i = 0; i < stone_number; ++i) {
    out << answer[i] << "\r\n";
  }
  fprintf(client, "%s\r\n", out.str().c_str());
  pclose(client);
}

#endif
