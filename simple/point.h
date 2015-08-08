#ifndef POINT_H_
#define POINT_H_

struct Point { // 石を置く座標
  int x, y;
  Point(int y, int x):
    y(y), x(x) {}
  bool operator<(const Point&obj)const{
    if (this->y == obj.y) {
      return this->y < obj.y;
    }
    return this->x < obj.x;
  }
};

#endif
