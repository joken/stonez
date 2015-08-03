-- quest_parser.lua

util = require "util"
stonez = require "stonez"

local quest_parser = { }

function quest_parser.Parser(filename)
    util.check_argument(filename, "string", "quest_parser.Parser", 1)

    local Parser = {
        class_name = "Parser",
        class = quest_parser.Parser,
    }

    -- 切り出した文字列のインデックス
    local INDEX_FIELD = 1
    local INDEX_NUMBER_STONES = 2
    local INDEX_STONE_BASE = 3

    -- 空行区切りで切り出した文字列
    local blocks
    
    --- メソッド ---

    local function create_line(digits)
        local line = stonez.Line()
        local key = 1
        for digit in digits:gmatch "%d" do
            if digit == "1" then
                line:set(key)
            end
            key = key + 1
        end
        return line
    end

    -- 生フィールドを返す
    local function create_raw_field(block_field)
        local raw_field = { }
        for digits in block_field:gmatch "%d+" do
            raw_field[#raw_field + 1] = create_line(digits)
        end
        return raw_field
    end

    -- フィールドを返す
    function Parser:field()
        return stonez.Field(create_raw_field(blocks[INDEX_FIELD]))
    end

    -- 石を返す
    function Parser:stones()
        local stones = { }
        for index_stone = INDEX_STONE_BASE, blocks[INDEX_NUMBER_STONES] do
            local block_stone = blocks[index_stone]
            stones[#stones + 1] = stonez.Stone(create_raw_field(block_stone))
        end
        return stones
    end


    -- ファイルを読んで切り出し
    local function split(filename)
        util.check_argument(filename, "string", "split", 1)
        local file, message = io.open(filename)
        if not file then
            error(message)
        end
        local blocks = { "" }
        for line in file:lines() do
            if line == "" then
                blocks[#blocks + 1] = ""
            elseif #line < 8 then
                blocks[#blocks] = line
                blocks[#blocks + 1] = ""
            else
                blocks[#blocks] = ("%s%s\n"):format(
                    blocks[#blocks],
                    line
                )
            end
        end
        return blocks
    end

    --- 初期化処理 ---

    -- 分割
    blocks = split(filename)

    --- メタテーブル ---

    local meta = { }

    function meta:__tostring()
        -- TODO Auto-generated method stub
        return "[Parser]"
    end

    return setmetatable(Parser, meta)
end

return quest_parser
