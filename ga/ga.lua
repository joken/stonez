-- ga.lua

util = require "util"
stonez = require "stonez"

local ga = { }

-- 石の遺伝子
local function GeneSegment(given_raw_gene)
    if given_raw_gene then
        util.check_argument(given_raw_gene, "number", "GeneSegment", 1)
    end

    local GeneSegment = {
        class_name = "GeneSegment",
        class = GeneSegment,
    }
    
    -- 生の値
    local raw_gene = given_raw_gene or 
        (math.random(0x200) - 1) + ((math.random(0x200) - 1) << 9)

    -- 生の値の意味づけ
    local properties = {
        { key = "selection",    mask = 0x20000, shift = 17 },
        { key = "manipulation", mask = 0x1C000, shift = 14 },
        { key = "position",     mask = 0x03FFF, shift =  0 },
        { key = "edge",         mask = 0x03FF8, shift =  3 },
        { key = "phase",        mask = 0x00007, shift =  0 },
    }

    --- メソッド ---

    -- 交叉
    function GeneSegment:crossover()

    end

    -- 全プロパティの文字列表現
    function GeneSegment:dump_properties()
        local s = ""
        for _, property in pairs(properties) do
            s = s .. ("%s: %s\n"):format(property.key, self[property.key])
        end
        return s
    end

    -- 生の値の2進表現
    function GeneSegment:dump_raw()
        local s = ""
        for i = 0, 17 do
            s = ((raw_gene >> i) & 1) .. s
        end
        return s
    end

    -- 複製
    function GeneSegment:clone()
        return self.class(raw_gene)
    end

    --- メタテーブル ---

    local meta = { }

    function meta:__index(key)
        for _, property in pairs(properties) do
            if key == property.key then
                return (raw_gene & property.mask) >> property.shift
            end
        end
    end

    function meta:__tostring( ... )
        return ("[%s] %s\n%s"):format(
            self.class_name,
            self:dump_raw(),
            self:dump_properties()
        )
    end

    return setmetatable(GeneSegment, meta)
end

-- 複数の石と遺伝子を紐付けするところ
-- 引数は，Stone の配列か ペアの配列
local function Slots( ... )
    local args = { ... }
    util.check_argument(args[1], "table", "Slots", 1)

    local Slots = {
        class_name = "Slots",
        class = Slots,
    }

    -- 実際の配列
    local pairs_stone_segment

    --- 初期化処理 ---

    if args[1][1] and args[1][1].class_name == "stonez.Stone" then
        -- 遺伝子を生成
        pairs_stone_segment = { }
        for _, stone in pairs(args[1]) do
            pairs_stone_segment[#pairs_stone_segment + 1] = {
                stone = stone,
                gene_segment = GeneSegment(),
            }
        end
    else
        -- セグメントから Slots を生成
        pairs_stone_segment = args[1]
    end

    --- メソッド ---

    -- 交叉
    local function crossover_slots(given_slots)
        return given_slots:crossover(pairs_stone_segment)
    end

    -- 交叉
    local function crossover_pairs(given_pairs)
        local new_pairs = { }

        -- 複製
        for _, pair in pairs(pairs_stone_segment) do
            new_pairs[#new_pairs + 1] = {
                stone = pair.stone,
                gene_segment = pair.gene_segment:clone(),
            }
        end

        -- 交叉
        for key, pair in pairs(pairs_stone_segment) do
            pair.gene_segment:crossover(given_pairs[key])
        end

        return Slots.class(new_pairs)
    end

    -- 交叉
    -- 引数は交叉するお相手の Slot か ペアの集合
    function Slots:crossover( ... )
        local args = { ... }
        util.check_argument(args[1], "table", "Slots:crossover", 1)
        if args[1].class_name == "Slots" then
            return crossover_slots(args[1])
        end
        return crossover_pairs(args[1])
    end

    -- 敷き詰められる石の個数
    function Slots:count_deployed()
        return #self:pairs_deployed()
    end

    -- 敷き詰められる石
    function Slots:pairs_deployed()
        local pairs_deployed = { }
        for _, pair in pairs(pairs_stone_segment) do
            if pair.gene_segment.selection == 1 then
                pairs_deployed[#pairs_deployed + 1] = pair
            end
        end
        return pairs_deployed
    end

    -- 2進表現
    local function string_binary()
        local s = ""
        for _, pair in ipairs(pairs_stone_segment) do
            s = s .. pair.gene_segment:dump_raw()
        end
        return s
    end

    -- 範囲内に納める
    local function clamp(x, max, min)
        return math.min(math.max(x, min), max)
    end

    -- 指紋
    -- Value     | 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16
    -- Character |   . o + = * B O X @  %  &  #  /  ^  S  E

    function Slots:fingerprint()
        -- フィールド
        local field = { }
        -- 文字
        local symbols = {
            ".", "o", "+", "=", "*", "B", "O", "X",
            "@", "%", "&", "#", "/", "^",
            [0] = " ",
            [-1] = "S",
            [-2] = "E",
        }
        -- 最大値
        local size = math.floor(math.log(#pairs_stone_segment, 2))
        local max_x = 8 * size
        local max_y = 4 * size
        -- 初期化
        for j = 0, max_y do
            field[j] = { }
            for i = 0, max_x do
                field[j][i] = 0
            end
        end
        -- 初期位置
        local home_x = (max_x + 1) // 2
        local home_y = (max_y + 1) // 2
        x, y = home_x, home_y
        -- 生成
        for a, b in string_binary():gmatch "(.)(.)" do
            local delta_x = b == "0" and -1 or 1
            local delta_y = a == "0" and -1 or 1
            x = clamp(x + delta_x, max_x, 0)
            y = clamp(y + delta_y, max_y, 0)
            field[y][x] = field[y][x] + 1
        end
        -- 初期位置と終了位置をマーク
        field[home_y][home_x] = -1
        field[y][x] = -2
        -- 文字列に変換
        local s = "+" .. ("-"):rep(max_x + 1) .. "+\n"
        for j = 0, max_y do
            s = s .. "|"
            for i = 0, max_x do
                s = s .. symbols[clamp(field[j][i], 14, -2)]
            end
            s = s .. "|\n"
        end
        s = s .. "+" .. ("-"):rep(max_x + 1) .. "+"
        return s
    end

    --- メタテーブル ---

    local meta = { }

    function meta:__index(key)
        util.check_argument(key, "number", "meta:__index", 1)
        if key > 0 and key <= #pairs_stone_segment then
            return pairs_stone_segment[key]
        end
    end

    function meta:__tostring()
        -- local s = ("[%s] %d\n"):format(self.class_name, #pairs_stone_segment)
        -- for index, pair in ipairs(pairs_stone_segment) do
        --     -- s = s .. ("%d:\n%s\n"):format(index, tostring(pair.gene_segment))
        --     s = s .. pair.gene_segment:dump_raw() .. "\n"
        -- end
        -- return s
        return self:fingerprint()
    end

    return setmetatable(Slots, meta)
end

-- 解の遺伝子
-- 引数: 石のテーブルか Slot
function ga.Gene( ... )
    local args = { ... }
    util.check_argument(args[1], "table", "ga.Gene", 1)

    local Gene = {
        class_name = "ga.Gene",
        clsss = ga.Gene,
    }

    -- 石の遺伝子
    local slots = args[1].class_name == "Slots" and args[1] or Slots(args[1])

    --- メソッド ---

    -- 突然変異
    function Gene:mutate()
        return self
    end

    -- 交叉
    local function crossover_gene(given_gene)
        return given_gene:crossover(slots)
    end

    -- 交叉
    local function crossover_slots(given_slots)
        return ga.Gene(slots:crossover(given_slots))
    end

    -- 交叉
    -- 引数は交叉するお相手の Gene か Slot
    function Gene:crossover( ... )
        local args = { ... }
        util.check_argument(args[1], "table", "Gene:crossover", 1)
        if args[1].class_name == "ga.Gene" then
            return crossover_gene(args[1])
        end
        if args[1].class_name == "Slots" then
            return crossover_slots(args[1])
        end
        print(args[1].class, ga.Gene)
        error (("bad argument to '%s'"):format("Gene:crossover"), 2)
    end

    -- フィールドを与えて石を配置し，この遺伝子の得点を返す
    function Gene:score(given_field)
        -- 引数の型を確認
        util.check_argument(given_field, "table", "Gene:score", 1)

        -- フィールドを複製する
        -- local field = given_field.class(given_field)
        -- local field = given_field:class()
        local field = given_field:clone()

        -- 配置される石の遺伝子のiterator
        if slots.pairs_deployed == nil then
            print(slots[1])
        end
        local pairs_deployed = slots:pairs_deployed()

        -- フィールドに対してシミュレートする.
        -- 1つめ
        local pair = pairs_deployed[1]
        -- 配置される石がない
        if pair == nil then
            return field:score(), field:count_deployed()
        end
        -- 1つめを位置指定で配置
        local success = field:deploy_stone(
            pair.stone,
            pair.gene_segment.manipulation,
            pair.gene_segment.position
        )
        -- 配置できなかった
        if not success then
            return field:score(), field:count_deployed()
        end
        -- 2つめ以降
        for i = 2, #pairs_deployed do
            pair = pairs_deployed[i]
            -- 輪郭線指定で配置
            local success = field:deploy_stone(
                pair.stone,
                pair.gene_segment.manipulation,
                pair.gene_segment.edge,
                pair.gene_segment.phase
            )
            -- 途中で配置できなくなった
            if not success then
                return field:score(), field:count_deployed()
            end
        end
        -- 全部を配置した
        -- フィールドに対する得点を求めて返す
        return field:score(), field:count_deployed()
    end

    -- メタテーブル --

    local meta = { }

    function meta:__tostring()
        return tostring(slots)
    end

    return setmetatable(Gene, meta)
end

return ga
