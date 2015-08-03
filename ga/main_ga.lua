-- main_ga.lua

ga = require "ga"
quest_parser = require "quest_parser"

local function main()
    -- 乱数シード値設定（テーブルのアドレス）
    math.randomseed(tonumber(tostring { }:match("%s(%x+)"), 16))

    -- パーサ生成
    local parser = quest_parser.Parser(arg[1])

    -- フィールド生成
    local field = parser:field()

    -- 石を用意
    local stones = parser:stones()
    
    -- 遺伝子を生成
    local gene = ga.Gene(stones)

    -- おいてみる
    local score, count = gene:score(field)

    print(score, count)
end

main()
