#!/bin/bash
pushd `dirname $0`
exec luacheck *.lua
