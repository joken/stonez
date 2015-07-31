-- ga.lua

util = require "util"

local ga = { }

local function GeneSegment()
    local GeneSegment = {
        class = "GeneSegment",
    }
    
    local raw_gene = (math.random(0x200) - 1) + ((math.random(0x200) - 1) << 9)

    local properties = {
        { key = "raw",          mask = 0x3FFFF, shift =  0 },
        { key = "selection",    mask = 0x20000, shift = 17 },
        { key = "manipulation", mask = 0x1C000, shift = 14 },
        { key = "position",     mask = 0x03FFF, shift =  0 },
        { key = "edge",         mask = 0x03FF8, shift =  3 },
        { key = "phase",        mask = 0x00007, shift =  0 },
    }

    function GeneSegment:dump_properties()
        local s = ""
        for _, property in pairs(properties) do
            s = s .. ("%s: %s\n"):format(property.key, self[property.key])
        end
        return s
    end

    function GeneSegment:dump_raw()
        local s = ""
        for i = 0, 17 do
            s = ((raw_gene >> i) & 1) .. s
        end
        return s
    end

    local meta = { }

    function meta:__index(key)
        for _, property in pairs(properties) do
            if key == property.key then
                return (raw_gene & property.mask) >> property.shift
            end
        end
    end

    function meta:__tostring( ... )
        return ("[%s]\n%s"):format(self.class, self:dump_properties())
            .. self:dump_raw()
    end

    return setmetatable(GeneSegment, meta)
end

local segment = GeneSegment()




local function Slots(number_slots)
    local Slots = { }



    return Slots
end

function ga.Gene()
    local Gene = {
        slots = { },
    }


    return Gene
end

return ga
