-- stonez.lua

local stonez = { }

-- 石
function stonez.Stone()
    local Stone = {
        class_name = "Stone",
        class = stonez.Stone,
    }
    

    function Stone:do_something()
        -- TODO Auto-generated method stub
    end

    local meta = { }

    function meta:__tostring()
        -- TODO Auto-generated method stub
        return "[Stone]"
    end

    return setmetatable(Stone, meta)
end

-- フィールド
function stonez.Field(raw_field)
    local Field = {
        class_name = "Field",
        class = stonez.Field,
    }

    -- 生のフィールド
    local raw_field = { }

    -- クローンとか
    if field_to_clone then
        util.check_argument(field_to_clone, "table", "stonez.Field", 1)
        -- TODO ここにクローン処理
    end

    -- 石を位置指定で配置
    local function locate_stone_freely(stone, manipulation, position)
        print(("%s: %s"):format("stone", stone))
        print(("%s: %s"):format("manipulation", manipulation))
        print(("%s: %s\n"):format("position", position))
    end

    -- 石を輪郭線指定で配置
    local function locate_stone_adjacently(stone, manipulation, edge, phase)
        print(("%s: %s"):format("stone", stone))
        print(("%s: %s"):format("manipulation", manipulation))
        print(("%s: %s"):format("edge", edge))
        print(("%s: %s\n"):format("phase", phase))
    end

    -- 石を配置
    function Field:locate_stone( ... )
        local args = { ... }
        if #args == 4 then
            locate_stone_adjacently( ... )
            return
        end
        if #args == 3 then
            locate_stone_freely( ... )
            return
        end
        error (
            ("bad argument to '%s' (%s)"):format(
                "Field:locate_stone",
                "got too few or too many arguments"
            ),
            2
        )
    end

    -- 得点
    function Field:score()
        -- TODO ちゃんと得点を計算する
        return 123
    end

    -- クローン
    function Field:clone()
        return self.class(self.raw_field)
    end
    
    local meta = { }

    function meta:__tostring()
        -- TODO Auto-generated method stub
        return "[Field]"
    end

    return setmetatable(Field, meta)
end

return stonez
