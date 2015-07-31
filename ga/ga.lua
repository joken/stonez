-- ga.lua

local ga = { }

local function GeneSegment()
    local GeneSegment = {
        raw = math.random(0x40000) - 1
    }

    return GeneSegment
end

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