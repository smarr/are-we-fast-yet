#!/bin/bash
RUBY_BIN=$(which ruby2.3)
if [ ! -x "$RUBY_BIN" ] ; then
  RUBY_BIN=ruby
fi
exec $RUBY_BIN "$@"
