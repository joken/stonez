-- util.lua

local util = { }

-- 重複のないハッシュ集合
function util.Set(initial_set)
    local Set = {
        class = "Set",
    }
    local raw_set = { }

    -- 集合かテーブルごと追加
    function Set:add_range(values)
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

    -- 追加．value2 を指定するとその値も保持する．value2 は element_at(value) で取れる
    function Set:add(value, value2)
        if self:contains(value) then
            return false
        end
        raw_set[value] = value2 or true
        return true
    end

    -- value を消す
    function Set:remove(value)
        if not self:contains(value) then
            return false
        end
        raw_set[value] = nil
        return true
    end

    -- value が集合の要素なら true
    function Set:contains(value)
        return raw_set[value] ~= nil
    end

    -- add(value, [value2]) で指定した value2 を返す
    function Set:element_at(value)
        return raw_set[value]
    end

    -- 反復子を返す
    function Set:values()
        local current_value = nil
        return function ()
            current_value = next(raw_set, current_value)
            return current_value
        end
    end

    -- ファイルに吐く
    function Set:dump(filename)
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

    -- 要素数を返す
    function Set:count()
        local count = 0
        for k, v in pairs(raw_set) do
            count = count + 1
        end
        return count
    end

    local meta = { }

    function meta:__tostring()
        return ("[%s] count: %d"):format(self.class, self.count())
    end

    -- 初期集合が与えられていればその要素を追加する
    if type(initial_set) == "table" then
        Set:add_range(initial_set)
    end

    return setmetatable(Set, meta)
end

function util.check_argument(obj, typing, method_name, order, add_error_level)
    local error_level = 2 + (add_error_level or 0)
    if type(typing) == "string" then
        if type(obj) == typing then
            return
        end
        local type_got = type(obj)
        error(
            ("bad argument #%d to '%s' (%s expected, got %s)"):format(
                order,
                method_name,
                typing,
                type_got
            ),
            error_level
        )
    end
end

function util.not_implemented()
    -- print(debug.traceback("*** Not implemented ***", 2))
    -- print ""
end

return util
