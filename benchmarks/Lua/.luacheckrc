--
-- usage:   luacheck *.lua
-- doc:     https://luacheck.readthedocs.io/en/stable/index.html
--
codes = true
std = 'min'
ignore = {'212/self'}
globals = {'bit32'}

files['json.lua'].ignore = { '631' }
