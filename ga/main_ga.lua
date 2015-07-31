-- main_ga.lua

math.randomseed(tonumber(tostring { }:match("%s(%x+)"), 16))

ga = require "ga"
stonez = require "stonez"

print(ga.Gene({ { }, { }, { }, { }, { }, { }, { }, }):score(stonez.Field()))
