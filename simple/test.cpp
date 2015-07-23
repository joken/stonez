#include "test.h"
#include "stone.h"
#include "field.h"

#include <cstdio>
void DumpField(const Field& f) {
  for (int i = 0; i < 32; ++i) {
    for (int j = 0; j < 32; ++j) {
      putc(f.raw[i][j], stdout);
    }
    puts("");
  }
}
void DumpStone(const Stone& s) {
  for (int j = 0; j < 8; ++j) {
    for (int k = 0; k < 8; ++k) {
      putc(s.raw[j][k], stdout);
    }
    puts("");
  }
}
