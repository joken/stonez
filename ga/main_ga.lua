-- main_ga.lua

ga = require "ga"
util = require "util"
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

    -- 遺伝子の総数(round 刻みに丸まる)
    local count_gene = 10
    local round = 1

    -- 選択する遺伝子の個数
    local count_selection = 4

    -- 世代数
    local count_generation = 4

    -- 突然変異する確率
    local probability_mutation = 0.04

    -- 1世代目の遺伝子をランダム生成
    local genes = { }
    for i = 1, count_gene do
        genes[#genes + 1] = ga.Gene(stones)
    end

    -- 1世代分の結果
    local results = { }

    -- デバッグフラグ(表示系)
    util.debug(false)

    -- 世代数だけ，遺伝子の生成，評価および選択を行う
    for i = 1, count_generation do
        -- 遺伝子を生成して評価
        for i = 1, count_gene // round do
            io.write "#"
            for j = 1, round do
                local index = (i - 1) * round + j
                local gene = genes[index]

                -- おいてみる
                local score, count = gene:score(field)

                results[#results + 1] = {
                    gene = gene,
                    score = score,
                    count = count,
                }
            end
        end

        -- 結果をソート(石の個数)
        table.sort(
            results,
            function (op1, op2)
                return op1.count > op2.count
            end
        )
        -- 結果をソート(空きマス数)
        table.sort(
            results,
            function (op1, op2)
                return op1.score < op2.score
            end
        )

        -- 上位の遺伝子を選択
        local selected = { }
        table.move(results, 1, count_selection, 1, selected)

        -- 選択した遺伝子の評価値を表示
        print ""
        for _, result in ipairs(selected) do
            print(result.score, result.count)
            print(result.gene)
        end

        -- 遺伝子を初期化
        genes = { }

        -- 選択した遺伝子をコピー
        for _, result in pairs(selected) do
            genes[#genes + 1] = result.gene
        end

        -- 残数分の遺伝子を生成
        for i = 1, count_gene - count_selection do

            -- 交叉する遺伝子の選択
            local index1 = math.random(count_selection)
            local index2 = (
                math.random(count_selection - 1) + index1 - 1
            ) % count_selection + 1
            local gene1 = selected[index1].gene
            local gene2 = selected[index2].gene

            -- 交叉
            local new_gene = gene1:crossover(gene2)

            -- 突然変異
            if math.random() < probability_mutation then
                new_gene = new_gene:mutate()
            end

            genes[#genes + 1] = new_gene
        end
    end

end

main()
