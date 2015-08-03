-- stonez.lua

local stonez = { }

-- 石
function stonez.Stone()
    local Stone = {
        class_name = "Stone",
        class = stonez.Stone,
    }

    --- メソッド ---

    -- 石操作済みの石を返す
    function Stone:manipulate()
        -- TODO Auto-generated method stub
    end

    -- 生フィールドに対して正規化位置で配置する
    function Stone:deploy_normalized(raw_field, position)
        util.check_argument(raw_field, "table", "Stone:deploy_normalized", 1)
        util.check_argument(position, "table", "Stone:deploy_normalized", 2)

    end

    --- メタテーブル ---

    local meta = { }

    function meta:__tostring()
        -- TODO Auto-generated method stub
        return "[Stone]"
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
    local function count_1()
        local bits = raw_line
        bits = (bits & 0x55555555) + (bits >> 1 & 0x55555555)
        bits = (bits & 0x33333333) + (bits >> 2 & 0x33333333)
        bits = (bits & 0x0f0f0f0f) + (bits >> 4 & 0x0f0f0f0f)
        bits = (bits & 0x00ff00ff) + (bits >> 8 & 0x00ff00ff)
        return (bits & 0x0000ffff) + (bits >> 16 & 0x0000ffff)
    end

    -- 1 か 0 の数を返す
    function Line:count(bit)
        if bit == 1 then
            return count_1()
        end
        if bit == 0 then
            return width - count_1()
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

    -- 複製を返す
    function Line:clone()
        return stonez.Line(raw_line)
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

    -- 輪郭追跡で輪郭線指定を位置に変換
    local function trace_contour(edge_position)
        util.check_argument(edge_position, "number", "trace_contour", 1)

        
    end

    -- 石を位置指定で配置
    local function deploy_stone_freely(stone, manipulation, position)
        util.check_argument(stone, "table", "deploy_stone", 1, 2)
        util.check_argument(manipulation, "number", "deploy_stone", 2, 2)
        util.check_argument(position, "number", "deploy_stone", 3, 2)

        print(("%s: %s"):format("stone", stone))
        print(("%s: %s"):format("manipulation", manipulation))
        print(("%s: %s\n"):format("position", position))

        -- 石操作を行う
        local stone_ready = stone:manipulate(manipulation)

        -- 位置の変換
        local position = {
            x = position % width,
            y = position // width,
        }

        -- 石を正規化位置で配置
        stone:deploy_normalized(filling_field, position)
        stone:deploy_normalized(stone_field, position)

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

        -- 輪郭
        local position, direction = trace_contour(edge)


        -- 実際の配置情報を返す
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
        -- TODO Auto-generated method stub
        return "[Field]"
    end

    return setmetatable(Field, meta)
end

return stonez
