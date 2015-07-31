-- ga.lua

util = require "util"

local ga = { }

-- 石の遺伝子
local function GeneSegment()
    local GeneSegment = {
        class_name = "GeneSegment",
        class = GeneSegment,
    }
    
    -- 生の値
    local raw_gene = (math.random(0x200) - 1) + ((math.random(0x200) - 1) << 9)

    -- 生の値の意味づけ
    local properties = {
        { key = "selection",    mask = 0x20000, shift = 17 },
        { key = "manipulation", mask = 0x1C000, shift = 14 },
        { key = "position",     mask = 0x03FFF, shift =  0 },
        { key = "edge",         mask = 0x03FF8, shift =  3 },
        { key = "phase",        mask = 0x00007, shift =  0 },
    }

    --- メソッド ---

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
local function Slots(stones_given)
    -- 引数の型を確認
    util.check_argument(stones_given, "table", "Slots", 1)

    local Slots = {
        class_name = "Slots",
        class = Slots,
    }

    -- 実際の配列
    local pairs_stone_segment = { }

    --- メソッド ---

    -- 敷き詰められる石の個数
    function Slots:count_located()
        return #self:pairs_located()
    end

    -- 敷き詰められる石
    function Slots:pairs_located()
        local pairs_located = { }
        for _, pair in pairs(pairs_stone_segment) do
            if pair.gene_segment.selection == 1 then
                pairs_located[#pairs_located + 1] = pair
            end
        end
        return pairs_located
    end

    --- 初期化処理 ---

    -- 遺伝子を生成
    for _, stone in pairs(stones_given) do
        pairs_stone_segment[#pairs_stone_segment + 1] = {
            stone = stone,
            gene_segment = GeneSegment(),
        }
    end

    --- メタテーブル ---

    local meta = { }

    function meta:__index(key)
        if type(key) == "number" and (key > 0 and key <= number_slots) then
            return pairs_stone_segment[key]
        end
    end

    function meta:__tostring()
        local s = ("[%s] %d\n"):format(self.class_name, number_slots)
        for index, pair in ipairs(pairs_stone_segment) do
            s = s .. ("%d:\n%s\n"):format(index, tostring(pair.gene_segment))
        end
        return s
    end

    return setmetatable(Slots, meta)
end

-- 解の遺伝子
function ga.Gene(stones_given)
    util.check_argument(stones_given, "table", "ge.Gene", 1)

    local Gene = {
        class_name = "Gene",
        clsss = ga.Gene,
    }

    -- 石の遺伝子
    local slots = Slots(stones_given)

    --- メソッド ---

    -- この遺伝子の得点
    function Gene:score(field_given)
        -- 引数の型を確認
        util.check_argument(field_given, "table", "Gene:score", 1)

        -- フィールドを複製する
        -- local field = field_given.class(field_given)
        -- local field = field_given:class()
        local field = field_given:clone()

        -- 配置される石の遺伝子のiterator
        local next_pair = util.Set(slots:pairs_located()):values()

        -- フィールドに対してシミュレートする.
        -- 1つめ
        local pair = next_pair()
        -- 配置される石がない
        if pair == nil then
            return field:score()
        end
        -- 1つめを配置
        field:locate_stone(
            pair.stone,
            pair.gene_segment.manipulation,
            pair.gene_segment.position
        )
        -- 2つめ以降
        while true do
            pair = next_pair()
            -- 配置される石がなくなった
            if pair == nil then
                return field:score()
            end
            -- 配置
            field:locate_stone(
                pair.stone,
                pair.gene_segment.manipulation,
                pair.gene_segment.edge,
                pair.gene_segment.phase
            )
        end
    end

    -- この遺伝子で敷き詰めた石の個数
    function Gene:count_located()
        return slots:count_located()
    end

    return Gene
end

return ga
