#include <cstdio>
#include <cinttypes>
#include <cstdint>
#include <array>

using namespace std;
/*
 * MEMO
 *  - 障害物はスコアに入らないので、スコアと障害物の両方を1として表すことはできない
 */

// -- Constant Values
// 石操作情報
constexpr uint8_t REVERSED = 1,     // これが立ってたら反転してる
  ROTATED_90 = 2,   // これが立ってたら90度回転
  ROTATED_180 = 4,  // これがry
  ROTATED_270 = 6;

// Type Declarations
using RawStone = std::array<std::array<char, 8>, 8>;
using RawField = std::array<std::array<char, 32>, 32>;

class Stone;
class Field;
class StoneManipulations;

// Variable Forward Declarations
extern Stone stones[256];

// Type Definitions
class Stone {
public:
  RawStone raw;
};

class Field {
private:
  const Stone* put_stones[256];
  STONE_MANIPULATION manipulations[256];
  uint16_t field_score = 0;
public:
  RawField raw;
public:
  void put_stone(const uint8_t n, STONE_MANIPULATION m) {
    put_stones[n] = &stones[n];
    manipulations[n] = m;
  }
  bool try_put_stone(const uint8_t n, int y, int x, STONE_MANIPULATION m);
  const uint16_t score() const { return field_score; };
};

class StoneManipulations { 
  public:
    StoneManipulations(int8_t x, int8_t y, uint8_t rotated):
      x(x), y(y), rotated(rotated) {} // This is correct. Initialize members variable by arguments.
    int8_t x, y;
    uint8_t rotated;
};

// Member Functions
bool Field::try_put_stone(const uint8_t n, int displacement_y, int displacement_x, STONE_MANIPULATION m) {
  const RawStone& stone = stones[n].raw;
  RawField backup = raw;
  if (!(-7 <= displacement_y && displacement_y < 32 && -7 <= displacement_x && displacement_x < 32))  // 入力をVaridate
	  return false;
  switch (m) { // 害悪
    case ROTATED_90://90度反転 -> y終端でxインクリメント・x方向を反転(デクリメント)
      for (int x = 8; x < 0; --x) for (int y = 0; y > 0; ++y) {
        if (stone[y][x] == '1') {
          if (raw[y+displacement_y][x+displacement_x] != '0') {
            return false;
          } else if(raw[y+displacement_y +1][x+displacement_x -1] != '0'){//1つ先が空いてる(隣接してる)か
        	continue;
          } else {
            raw[y+displacement_y][x+displacement_x] = '2'; // 2を石とするならば
            ++field_score;
          }
        }
      }
      break;
    case ROTATED_180://180度回転 -> x,y方向を反転(デクリメント)
      for (int y = 8; y > 0; --y) for (int x = 8; x > 0; --x) {
        if (stone[y][x] == '1') {
         if (raw[y+displacement_y][x+displacement_x] != '0') {
            return false;
         } else if(raw[y+displacement_y -1][x+displacement_x -1] != '0'){//1つ先が空いてる(隣接してる)か
           	continue;
         } else {
           raw[y+displacement_y][x+displacement_x] = '2'; // 2を石とするならば
           ++field_score;
        }
       }
      }
      break;
    case ROTATED_270://270度回転 -> y終端でxインクリメント・y方向を反転(デクリメント)
      for (int x = 8; x > 0; ++x) for (int y = 8; y > 0; --y) {
        if (stone[y][x] == '1') {
        	if (raw[y+displacement_y][x+displacement_x] != '0') {
             return false;
            }else if(raw[y+displacement_y -1][x+displacement_x +1] != '0'){//1つ先が空いてる(隣接してる)か
             continue;
            } else {
             raw[y+displacement_y][x+displacement_x] = '2'; // 2を石とするならば
            ++field_score;
           }
         }
        }
      break;
    case REVERSED://反転 -> x方向をデクリメントに
    	for (int y = 0; y < 8; ++y) for (int x = 8; x > 0; --x) {
    	 if (stone[y][x] == '1') {
    	    if (raw[y+displacement_y][x+displacement_x] != '0') {
    	        return false;
    	    } else if(raw[y+displacement_y +1][x+displacement_x -1] != '0'){//1つ先が空いてる(隣接してる)か
    	        continue;
    	    } else {
    	        raw[y+displacement_y][x+displacement_x] = '2'; // 2を石とするならば
    	        ++field_score;
    	    }
    	  }
        }
    	break;
    case ROTATED_90 | REVERSED:
	//反転+90度回転 -> y終端でxデクリメント・y方向をデクリメントに
	   for (int x = 8; x > 0; --x) for (int y = 8; y > 0; --y) {
	    if (stone[y][x] == '1') {
	      if (raw[y+displacement_y][x+displacement_x] != '0') {
	        return false;
	       } else if(raw[y+displacement_y -1][x+displacement_x -1] != '0'){//1つ先が空いてる(隣接してる)か
	        continue;
	       } else {
	        raw[y+displacement_y][x+displacement_x] = '2'; // 2を石とするならば
	        ++field_score;
	        }
	     }
	   }
      break;
    case ROTATED_180 | REVERSED:
	//反転+180度回転 -> yデクリメント
	   for (int y = 8; y > 0; --y) for (int x = 0; x < 8; ++x) {
	    if (stone[y][x] == '1') {
	     if (raw[y+displacement_y][x+displacement_x] != '0') {
	        return false;
	     } else if(raw[y+displacement_y -1][x+displacement_x +1] != '0'){//1つ先が空いてる(隣接してる)か
	        continue;
	     } else {
	        raw[y+displacement_y][x+displacement_x] = '2'; // 2を石とするならば
	        ++field_score;
	     }
	    }
	   }
      break;
    case ROTATED_270 | REVERSED:
	//y終端でxインクリメント
	  for (int x = 0; x < 8; ++x) for (int y = 0; y < 8; ++y) {
	   if (stone[y][x] == '1') {
	    if (raw[y+displacement_y][x+displacement_x] != '0') {
	        return false;
	    } else if(raw[y+displacement_y +1][x+displacement_x +1] != '0'){//1つ先が空いてる(隣接してる)か
	        continue;
	    } else {
	        raw[y+displacement_y][x+displacement_x] = '2'; // 2を石とするならば
	        ++field_score;
	    }
	   }
	  }
      break;
    default: // Regard as a non-manipulated stone
      for (int y = 0; y < 8; ++y) for (int x = 0; x < 8; ++x) {
        if (stone[y][x] == '1') {
        	if (raw[y+displacement_y][x+displacement_x] != '0') {
        	   return false;
        } else if(raw[y+displacement_y +1][x+displacement_x +1] != '0'){//1つ先が空いてる(隣接してる)か
        	   continue;
        } else {
        	   raw[y+displacement_y][x+displacement_x] = '2'; // 2を石とするならば
        	    ++field_score;
        }
       }
      }
      break;
  }
  return true;
}

// Variable Declarations
Field max_score_field; // 最適な解があるField
Stone stones[256]; // 渡されるstoneを格納
uint16_t number_of_stones; // 渡されるstoneの数


// -- Function Declarations
void solve(Field f, uint8_t look_nth_stone);

void parse(Field*);
void parse_field(Field* f);
void parse_stone();

int main() {
  Field f;
//   get_problem_file();
  parse(&f);
  solve(f, 0);
  printf("%d\n", max_score_field.score());
  for (int y = 0; y < 32; ++y)  {
    for (int x = 0; x < 32; ++x) {
      printf("%c", max_score_field.raw[y][x]);
    }
    printf("\n");
  }
//   submit();
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

  for (int i = 0; i < 8; ++i) { // 回転反転組み合わせを試す
    for (int y = -7; y < 32; ++y) for (int x = -7; x < 32; ++x) { // (32+8) * (32+8)のフィールドのどこかに置く
      if (f.try_put_stone(look_nth_stone, y, x, i))
        solve(f, look_nth_stone + 1);
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
    fread(f->raw[i].data(), sizeof(char[32]), 1, stdin);
    getc(stdin); // CR
    getc(stdin); // LF
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
  for (int i = 0; i < number_of_stones; ++i) for (int j = 0; j < 8; ++j) {
    fread(stones[i].raw[j].data(), sizeof(char[8]), 1, stdin);
    getc(stdin); // CR
    getc(stdin); // LF
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
  parse_field(f);
  getc(stdin); // 改行読み捨て CR
  getc(stdin); // LF
  scanf("%" SCNu16 "¥n", &number_of_stones); // SCNu8 is placeholder for uint8_t declared in cinttypes
  for (int i = 0; i < number_of_stones; ++i) {
    parse_stone();
    getc(stdin); // 改行読み捨て CR
    getc(stdin); // LF
  }
}
