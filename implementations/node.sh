#/bin/sh
if [ -x "`which nvm`" ]
then
  NODE_BIN=`nvm which 6`
  if [ ! -x "$NODE_BIN" ] ; then
    NODE_BIN=node
  fi
else
  NODE_BIN=node
fi
exec $NODE_BIN "$@"
