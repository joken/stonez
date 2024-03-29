--[[
参考文献
https://github.com/vladvasiliu/polyominoes/blob/master/polyominoes/polyomino.py
]]--

-- システム上の最大サイズ(正方形の辺長)．意味は忘れたけど特にいじらなくてもいいはず
local MAX_SIZE
-- サイズの制限(石なら8，フィールドなら32とか)
local LIMIT_SIZE

-- これを呼ぶとGCガッシャンガッシャンしてメモリを規定値以上使っていたら自害する
function check_memory()
    collectgarbage()
    local count = collectgarbage "count"
    -- print(count)
    if count > 12 * 1024 * 1024 then
        os.exit()
    end
    return count
end

function output_filename(i)
    return "polyominoes/" .. ("%04d"):format(i)
end

-- 固定されたn-オミノを生成する
function generate(n)
    -- モノオミノから始める
    local current_minos = PolyominoSet()
    current_minos:add(Polyomino { Vector(1, 2), Vector(1, 3), Vector(2, 2), Vector(3, 2) })
    -- それの前に集合のメンバの子を反復的に追加する
    for i = 2, n do
        print(i, os.clock() * 1000 - _G.t0, check_memory())
        current_minos = childSet(current_minos)
        -- minos:add_range(current_minos)
        -- n-ominoesごとにファイルに出力
        -- current_minos:dump(output_filename(i))
    end
end

-- ポリオミノの集合の子の集合を返す
function childSet(minos)
    local visited_children = Set()
    local children_file = PolyominoSet()
    for mino in minos:values() do
        for child in mino:children():values() do
            local hash = child:hash()
            if child.size.x <= LIMIT_SIZE
                and child.size.y <= LIMIT_SIZE
                and not visited_children:contains(hash) then
                visited_children:add(hash)
                children_file:add(child)
            end
        end
    end
    minos:close()
    return children_file
end

-- ミノの集合から回転を取り除く(ユーティリティ的な)
function one_sided(minos)
    -- 訪問済みの回転したミノの集合
    local vis = Set()
    -- 結果
    local result = Set()
    for mino in minos:values() do
        -- もし初めてこのミノの回転を見たなら，この回転を訪問済み集合に追加する
        if not vis:contains(mino) then
            local mino_rots = mino:rotations()
            vis:add_range(mino_rots)
            -- ミノを追加する
            result:add(mino)
        end
    end
    return result
end

-- ミノの集合から回転と反射を取り除く(ユーティリティ的な)
function free(minos)
    -- 訪問済みの変換したミノの集合
    local vis = Set()
    -- 結果
    local result = Set()
    for mino in minos:values() do
        if not minos:contains(mino) then
            local mino_trans = mino:transforms()
            vis:add_range(mino_trans)
            -- ミノを追加する
            result:add(mino)
        end
    end 
    return result
end

-- 重複のないハッシュ集合
function Set(initial_set)
    local new_Set = {
        class = "Set"
    }
    local raw_set = { }

    function new_Set:add_range(values)
        if values.class == "Set" then
            for value in values:values() do
                self:add(value)
            end
            return self
        end
        for key, value in pairs(values) do
            self:add(value)
        end
        return self
    end

    function new_Set:add(value, value2)
        if self:contains(value) then
            return false
        end
        raw_set[value] = value2 or true
        return true
    end

    function new_Set:remove(value)
        if not self:contains(value) then
            return false
        end
        raw_set[value] = nil
        return true
    end

    function new_Set:contains(value)
        return raw_set[value] ~= nil
    end

    function new_Set:element_at(value)
        return raw_set[value]
    end

    -- 反復子を返す
    function new_Set:values()
        local current_value = nil
        return function ()
            current_value = next(raw_set, current_value)
            return current_value
        end
    end

    function new_Set:dump(filename)
        local file = io.open(filename, "w+")
        if not file then
            error("could not open file: " .. tostring(filename))
        end
        for value in self:values() do
            file:write(tostring(value) .. "\n")
        end
        file:flush()
        file:close()
    end

    -- 初期集合が与えられていればその要素を追加する
    if type(initial_set) == "table" then
        new_Set:add_range(initial_set)
    end

    return new_Set
end

-- 内部にファイルを用いたポリオミノ集合
function PolyominoSet(filename)
    local new_PolyominoSet = {
        class = "PolyominoSet"
    }
    -- ファイル名の指定がないならデフォルトファイル名を用いる
    local filename = filename or "tmp" .. os.tmpname()
    -- ファイルハンドラ置き場
    local flie_opened

    -- オプションを指定して file_opened に開く
    local function open(option)
        local file, message = io.open(filename, option)
        if not file then
            error(message .. (" (mode %s)"):format(option))
        end
        flie_opened = file
    end

    -- ファイルを作っておく
    -- open "w"
    -- flie_opened:close()

    function new_PolyominoSet:add(omino)
        open "a"
        flie_opened:write(tostring(omino:hash()))
        flie_opened:write "\n"
        flie_opened:flush()
        flie_opened:close()
    end

    function new_PolyominoSet:values()
        open "r"
        local lines = flie_opened:lines()
        return function ()
            line = lines()
            return (line and #line ~= 0) and Polyomino(line) or nil
        end
    end

    function new_PolyominoSet:close()
        if flie_opened then
            flie_opened:close()
        end
        flie_opened = nil
    end

    return new_PolyominoSet
end

function Polyomino(raw_shape)
    local new_Polyomino = {
        class = "Polyomino"
    }
    local position_set

    -- ハッシュからposition_setを生成する
    local function unhash(hash)
        local positions = Set()
        local bits_hex = 4
        local y = 0
        for line in hash:gmatch(("."):rep(MAX_SIZE / bits_hex)) do
            local hex = tonumber(line, 16)
            for x = MAX_SIZE - 1, 0, -1 do
                if hex & 1 == 1 then
                    positions:add(Vector(x, y))
                end
                hex = hex >> 1
            end
            y = y + 1
        end
        return positions
    end

    -- raw_shapeがstringならハッシュと見なしてハッシュから復元
    if type(raw_shape) == "string" then
        position_set = unhash(raw_shape)
    else
        position_set = Set(raw_shape)
    end

    -- 境界を計算する
    local function calculate_boundary()
        local raw_top_left = { x = math.huge, y = math.huge }
        local raw_bottom_right = { x = 0, y = 0 }
        for position in position_set:values() do
            raw_top_left.x = math.min(raw_top_left.x, position.x)
            raw_top_left.y = math.min(raw_top_left.y, position.y)
            raw_bottom_right.x = math.max(raw_bottom_right.x, position.x)
            raw_bottom_right.y = math.max(raw_bottom_right.y, position.y)
        end
        return Vector(raw_top_left.x, raw_top_left.y),
            Vector(raw_bottom_right.x, raw_bottom_right.y)
    end

    -- 境界
    local top_left, bottom_right = calculate_boundary()

    -- 大きさ
    new_Polyomino.size = bottom_right:translate(- top_left):translate(Vector(1, 1))

    -- 変換する
    local function transform(mapping)
        local new_raw_shape = { }
        for position in position_set:values() do
            new_raw_shape[#new_raw_shape + 1] = mapping(position)
        end
        return Polyomino(new_raw_shape)
    end

    -- 並行移動する
    function new_Polyomino:translate(phase)
        return transform(
            function (position)
                return position:translate(phase)
            end
        )
    end

    -- 正規化する
    function new_Polyomino:normalize()
        return self:translate(- top_left)
    end

    -- 決め打ちして回す
    function new_Polyomino:rotate_left()
        return transform(
            function (position)
                return Vector(position.y, - position.x)
            end
        ):normalize()
    end

    function new_Polyomino:rotate_half()
        return transform(
            function (position)
                return Vector(- position.x, - position.y)
            end
        ):normalize()
    end

    function new_Polyomino:rotate_right()
        return transform(
            function (position)
                return Vector(- position.y, position.x)
            end
        ):normalize()
    end

    -- 決め打ちして反射させる
    function new_Polyomino:reflect_vert()
        return transform(
            function (position)
                return Vector(position.x, - position.y)
            end
        ):normalize()
    end

    function new_Polyomino:reflect_horiz()
        return transform(
            function (position)
                return Vector(- position.x, position.y)
            end
        ):normalize()
    end

    function new_Polyomino:reflect_diag()
        return transform(
            function (position)
                return Vector(- position.y, - position.x)
            end
        ):normalize()
    end

    function new_Polyomino:reflect_skew()
        return transform(
            function (position)
                return Vector(position.y, position.x)
            end
        ):normalize()
    end

    -- 回転してできるポリオミノを返す
    function new_Polyomino:rotations()
        return {
            self,
            self:rotate_right(),
            self:rotate_left(),
            self:rotate_half(),
        }
    end

    -- 変換してできるポリオミノを返す
    function new_Polyomino:transforms()
        return {
            self:reflect_horiz(),
            self:reflect_vert(),
            self:reflect_diag(),
            self:reflect_skew(),
            table.unpack(self:rotations()),  
        }
    end

    -- 子を返す
    function new_Polyomino:children()
        local child_set = Set()
        -- 全てのブロックに対する隣接ブロックを得る
        local nbrs = Set()
        for position in position_set:values() do
            nbrs:add_range(position:neighbors())
        end
        for position in position_set:values() do
            nbrs:remove(position)
        end
        for nbr in nbrs:values() do
            local child = Polyomino(Set {nbr}:add_range(position_set))
            -- 必要なときだけ正規化する
            child = child:translate(
                Vector(nbr.x == -1 and 1 or 0, nbr.y == -1 and 1 or 0)
            )
            child_set:add(child)
        end
        return child_set
    end

    -- ハッシュ
    function new_Polyomino:hash()
        local s = ""
        local bits_hex = 4
        for y = 0, MAX_SIZE - 1 do
            for x = 0, MAX_SIZE - 1, bits_hex do
                local hex = 0
                for digit = 0, bits_hex - 1 do
                    hex = hex + (
                        position_set:contains(Vector(x + digit, y))
                        and 2 ^ (bits_hex - digit - 1)
                        or 0
                    )
                end
                s = s .. ("%X"):format(hex)
            end
        end

        return s
    end

    local meta = { }

    function meta:__tostring()
        -- local block = "[]"
        -- local none  = "  "
        local block = "1"
        local none  = "0"
        local s = "" -- "   "
        -- for x = 0, bottom_right.x do
        --     s = s .. ("%2X"):format(x)
        -- end
        -- s = s .. "\n"
        for y = 0, bottom_right.y do
            -- s = s .. ("%2X"):format(y) .. ' '
            for x = 0, bottom_right.x do
                s = s .. (position_set:contains(Vector(x, y)) and block or none)
            end
            s = s .. "\n"
        end
        return s
    end

    return setmetatable(new_Polyomino, meta)
end

-- 位置など
do
    local function issue_vector(x, y)
        local new_Vector = {
            x = x,
            y = y,
        }

        function new_Vector:translate(vector)
            return Vector(x + vector.x, y + vector.y)
        end

        function new_Vector:neighbors()
            return Set { 
                self:translate(Vector( 1,  0)),
                self:translate(Vector(-1,  0)),
                self:translate(Vector( 0, -1)),
                self:translate(Vector( 0,  1)),
            }
        end

        local meta = { }

        function meta:__eq(vector)
            return x == vector.x and y == vector.y
        end

        function meta:__tostring()
            return ("(%d, %d)"):format(x, y)
        end

        function meta:__unm()
            return Vector(- x, - y)
        end

        return setmetatable(new_Vector, meta)
    end

    local issued_vectors = { }

    local function add(vector)
        if not issued_vectors[vector.x] then
            issued_vectors[vector.x] = { }
        end
        issued_vectors[vector.x][vector.y] = vector
    end

    local function contains(x, y)
        for x_value, y_set in pairs(issued_vectors) do
            if x_value == x then
                for y_value in pairs(issued_vectors[x_value]) do
                    if y_value == y then
                        return true
                    end
                end
            end
        end
        return false
    end

    function Vector(x, y)
        if contains(x, y) then
            return issued_vectors[x][y]
        end
        local vector = issue_vector(x, y)
        add(vector)
        return vector
    end
end

----------------------------------------------

local argparse = require "lib/argparse"
local parser = argparse()
    :description "A polyominoes generator."
parser:argument "limit"
    :description "Size limit."
parser:argument "num"
    :description "Number of blocks."
local args = parser:parse()

-- システム上の最大サイズ(正方形の辺長)．意味は忘れたけど特にいじらなくてもいいはず
MAX_SIZE = 32
-- サイズの制限(石なら8，フィールドなら32とか)
LIMIT_SIZE = tonumber(args.limit)

-- positions = Set()

-- for i = 1, 128 do
--     positions:add(Vector(math.random(32) - 1, math.random(32) - 1))
-- end

-- p = Polyomino(positions)

-- -- p = Polyomino { Vector(1, 2), Vector(1, 3), Vector(2, 2), Vector(3, 2) }
-- hash_p = p:hash()
-- q = Polyomino(hash_p)
-- hash_q = q:hash()

-- print(hash_p)
-- print(hash_q)
-- print(hash_p == hash_q)

-- s = Set(p:transforms())
-- for omino in s:values() do
--     print(omino)
-- end
-- print(p)

-- for omino in p:children():values() do
--     print(omino)
-- end

t0 = os.clock() * 1000

ominos = generate(tonumber(args.num))

t1 = os.clock() * 1000

-- for omino in ominos:values() do
--     print(omino)
-- end

print("TIME: " .. (t1 - t0))
