-- stonez.lua

local stonez = { }

-- 石
function stonez.Stone(given_raw_stone)
    if given_raw_stone then
        util.check_argument(given_raw_stone, "table", "stonez.Stone", 1)
    end

    local Stone = {
        class_name = "Stone",
        class = stonez.Stone,
    }

    local width = 8

    --- メソッド ---

    -- 位相から正規化位置を出す
    function decode_phase(contur, phase)
        util.check_argument(contur, "table", "decode_phase", 1)
        util.check_argument(phase, "number", "decode_phase", 2)

        util.not_implemented()

        return { x = 0, y = 0 }
    end

    -- 石操作済みの石を返す
    local function create_manipulated(manipulation)
        local t = (manipulation & 0x4) >> 2
        local r180 = (manipulation & 0x2) >> 1
        local r90 = (manipulation & 0x1) >> 0
        local rev_x = t ~ r180 == 1
        local rev_y = r180 ~ r90 == 1
        local rev_xy = r90 == 1
        local manipulated = { }
        print(manipulation, t, r180, r90, rev_x, rev_y, rev_xy)
        for j = 1, width do
            local y = rev_y and width - j + 1 or j
            manipulated[j] = stonez.Line()
            for i = 1, width do
                local x = rev_x and width - i + 1 or i
                if rev_xy then
                    x, y = y, x
                end
                if raw_stone[y][x] == 1 then
                    io.write "[]"
                    manipulated[j]:set(i)
                else
                    io.write ". "
                end
            end
            print ""
        end
        return stonez.Stone(manipulated)
    end

    -- 生フィールドに対して正規化位置で配置する
    function Stone:deploy_normalized(raw_field, manipulation, position)
        util.check_argument(raw_field, "table", "Stone:deploy_normalized", 1)
        util.check_argument(manipulation, "number", "Stone:deploy_normalized", 2)
        util.check_argument(position, "table", "Stone:deploy_normalized", 3)

        local manipulated_stone = create_manipulated(manipulation)

        print(manipulated_stone)

        util.not_implemented()
    end

    -- 生フィールドに対して輪郭素片と位相で配置
    function Stone:deploy_edge(raw_field, manipulation, contur, phase)
        util.check_argument(raw_field, "table", "Stone:deploy_edge", 1)
        util.check_argument(manipulation, "number", "Stone:deploy_edge", 2)
        util.check_argument(contur, "table", "Stone:deploy_edge", 3)
        util.check_argument(phase, "number", "Stone:deploy_edge", 4)

        -- 正規化位置を出す
        local position = decode_phase(contur, phase)

        -- 正規化位置で配置
        self:deploy_normalized(raw_field, manipulation, position)
    end

    -- 空の生の石を作る
    

    --- 初期化処理 ---

    raw_stone = { }
    if given_raw_stone then
        for _, line in pairs(given_raw_stone) do
            raw_stone[#raw_stone + 1] = line:clone()
        end
    else
        for i = 1, width do
            raw_stone[#raw_stone + 1] = stonez.Line()
        end
    end

    --- メタテーブル ---

    local meta = { }

    function meta:__tostring()
        local s = "[Stone]\n"
        for _, line in ipairs(raw_stone) do
            s = s .. line:binary_string(width, ". ", "[]") .. "\n"
        end
        return s
    end

    return setmetatable(Stone, meta)
end

-- フィールドの列
function stonez.Line(given_line)
    if given_line then
        util.check_argument(given_line, "number", "stonez.Line", 1)
    end

    local Line = {
        class_name = "Line",
        class = stonez.Line,
    }

    -- ビット幅
    local width = 32

    -- 生の値
    local raw_line = given_line or 0x00000000

    --- メソッド ---

    -- key の範囲確認
    local function is_valid(key)
        return type(key) == "number" and key <= width and key > 0
    end

    -- key の位置をセット
    function Line:set(key)
        if is_valid(key) then
            raw_line = raw_line | (0x00000001 << (width - key))
        end
    end

    -- key の位置をリセット
    function Line:reset(key)
        if is_valid(key) then
            raw_line = raw_line & (0xFFFFFFFF ~ (0x00000001 << (width - key)))
        end
    end

    -- 1 の数を返す
    local function count_1(bits)
        bits = (bits & 0x55555555) + (bits >> 1 & 0x55555555)
        bits = (bits & 0x33333333) + (bits >> 2 & 0x33333333)
        bits = (bits & 0x0f0f0f0f) + (bits >> 4 & 0x0f0f0f0f)
        bits = (bits & 0x00ff00ff) + (bits >> 8 & 0x00ff00ff)
        return (bits & 0x0000ffff) + (bits >> 16 & 0x0000ffff)
    end

    -- 1 か 0 の数を返す
    function Line:count(bit)
        if bit == 1 then
            return count_1(raw_line)
        end
        if bit == 0 then
            return width - count_1(raw_line)
        end
        error(
            ("bad argument #%d to '%s' (%s expected, got %s)"):format(
                1,
                "Line:count",
                "0 or 1",
                bit
            ),
            2
        )
    end

    -- 一番左の1の位置を返す
    function Line:nlz()
        local x = raw_line
        x = x | ( x >>  1 );
        x = x | ( x >>  2 );
        x = x | ( x >>  4 );
        x = x | ( x >>  8 );
        x = x | ( x >> 16 );
        return count_1( ~x );
    end

    -- 複製を返す
    function Line:clone()
        return stonez.Line(raw_line)
    end

    -- バイナリ表現を返す
    function Line:binary_string(length, str_0, str_1)
        util.check_argument(length, "number", "Line:binary_string", 1)
        if str_0 then
            util.check_argument(str_0, "string", "Line:binary_string", 2)
        end
        if str_1 then
            util.check_argument(str_1, "string", "Line:binary_string", 3)
        end

        s = ""
        for i = 1, length do
            s = s .. (self[i] == 0 and (str_0 or "0") or (str_1 or "1"))
        end
        return s
    end

    --- メタテーブル ---

    local meta = { }

    function meta:__index(key)
        if is_valid(key) then
            return (raw_line >> (width - key)) & 0x00000001
        end
    end

    function meta:__tostring()
        -- TODO Auto-generated method stub
        return "[Line] " + ("%8X"):format(raw_line)
    end

    return setmetatable(Line, meta)
end

-- フィールド
function stonez.Field(given_filling_field, given_stone_field)
    util.check_argument(given_filling_field, "table", "stonez.Field", 2)
    if given_stone_field then
        util.check_argument(given_stone_field, "table", "stonez.Field", 1)
    end

    local Field = {
        class_name = "Field",
        class = stonez.Field,
    }

    -- フィールド幅
    local width = 32

    -- 石と障害物が区別されないフィールド
    local filling_field

    -- 石だけを配置するフィールド
    local stone_field

    -- 配置された石の数
    local count_deployed = 0

    --- メソッド ---

    -- ラスタスキャン的な左上座標を求める
    local function raster_top_left()
        for y, line in ipairs(stone_field) do
            local nlz = line:nlz()
            if nlz < width then
                return { x = nlz, y = y }
            end
        end
        error("The method call is invalid for this field's current state.", 2)
    end

    -- 輪郭追跡で輪郭線指定を位置に変換
    local function trace_contour(edge_position)
        util.check_argument(edge_position, "number", "trace_contour", 1)

        local position_edge = raster_top_left()
        local direction = 0



        return { position_edge = position_edge, direction = direction }
    end

    -- 石を位置指定で配置
    local function deploy_stone_freely(stone, manipulation, position)
        util.check_argument(stone, "table", "deploy_stone", 1, 2)
        util.check_argument(manipulation, "number", "deploy_stone", 2, 2)
        util.check_argument(position, "number", "deploy_stone", 3, 2)

        print(("%s: %s"):format("stone", stone))
        print(("%s: %s"):format("manipulation", manipulation))
        print(("%s: %s\n"):format("position", position))

        print(Field)

        -- 位置の変換
        local position = {
            x = position % width,
            y = position // width,
        }

        -- 石を正規化位置で配置
        stone:deploy_normalized(filling_field, manipulation, position)
        stone:deploy_normalized(stone_field, manipulation, position)

        util.not_implemented()

        -- 実際の配置情報を返す
        return { }
    end

    -- 石を輪郭線指定で配置
    local function deploy_stone_adjacently(stone, manipulation, edge, phase)
        util.check_argument(stone, "table", "deploy_stone", 1, 2)
        util.check_argument(manipulation, "number", "deploy_stone", 2, 2)
        util.check_argument(edge, "number", "deploy_stone", 3, 2)
        util.check_argument(phase, "number", "deploy_stone", 4, 2)

        print(("%s: %s"):format("stone", stone))
        print(("%s: %s"):format("manipulation", manipulation))
        print(("%s: %s"):format("edge", edge))
        print(("%s: %s\n"):format("phase", phase))

        print(Field)

        -- 輪郭素片デコード
        local contur = trace_contour(edge)

        -- 石を輪郭素片と位相で配置
        stone:deploy_edge(filling_field, manipulation, contur, phase)
        stone:deploy_edge(stone_field, manipulation, contur, phase)

        -- 実際の配置情報を返す

        util.not_implemented()

        return { }
    end

    -- 石を配置
    function Field:deploy_stone( ... )
        local args = { ... }
        if #args == 4 then
            -- 輪郭線指定でおいてみて結果を返す
            return deploy_stone_adjacently( ... )
        end
        if #args == 3 then
            -- 位置指定でおいてみて結果を返す
            return deploy_stone_freely( ... )
        end
        error (
            ("bad argument to '%s' (%s)"):format(
                "Field:deploy_stone",
                "got too few or too many arguments"
            ),
            2
        )
    end

    -- 得点
    function Field:score()
        local score = 0
        for _, line in pairs(filling_field) do
            score = score + line:count(0)
        end
        return score
    end

    -- 配置された石の数
    function Field:count_deployed()
        return count_deployed
    end

    -- クローン
    function Field:clone()
        return self.class(filling_field, stone_field)
    end

    --- 初期化処理 ---

    filling_field = { }
    for _, line in pairs(given_filling_field) do
        filling_field[#filling_field + 1] = line:clone()
    end

    stone_field = { }
    if given_stone_field then
        for _, line in pairs(given_stone_field) do
            stone_field[#stone_field + 1] = line:clone()
        end
    else
        for i = 1, width do
            stone_field[#stone_field + 1] = stonez.Line()
        end
    end

    --- メタテーブル ---

    local meta = { }

    function meta:__tostring()
        local s = "[Field]\n"
        for _, line in ipairs(filling_field) do
            s = s .. line:binary_string(width, ". ", "[]") .. "\n"
        end
        return s
    end

    return setmetatable(Field, meta)
end

return stonez
