#!/bin/bash
pushd `dirname $0`
exec npx eslint .
