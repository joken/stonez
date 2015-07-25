#include "input.h"
#include <cstdio>

int Parse(Field* f, Stone* reserved) { // 今後、標準入力以外の場所から入力を受け付けるのかしら
  int number_of_stone;
  for (int i = 0; i < 32; ++i) {
    fread(f->raw[i].data(), sizeof(char[32]), 1, stdin);
    getchar(); //CR
    getchar(); //LF
  }
  scanf("\n%d\n", &number_of_stone);
  for (int i = 0; i < number_of_stone; ++i) {
    for (int j = 0; j < 8; ++j) {
      fread(reserved[i].raw[j].data(), sizeof(char[8]), 1, stdin);
      getchar(); //CR
      getchar(); //LF
    }
    getchar(); //CR
    getchar(); //LF
  }

  return number_of_stone;
}
