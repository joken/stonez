#ifndef DUMP_H_
#define DUMP_H_

#include "stone.h"
#include "field.h"

/*
 * Dump*関数は、標準出力に内容を書く
 */

void DumpField(const Field& f);
void DumpStone(const Stone& s);
#endif
