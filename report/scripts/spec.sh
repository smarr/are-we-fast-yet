#!/bin/bash

report() {
  echo "\`\$ $1\`"
  echo
  echo "\`\`\`"
  eval "$1"
  echo "\`\`\`"
}

echo "Basic Information on the Benchmarking System"
echo "============================================"

echo

echo "Hardware"
echo "--------"
echo

report "cat /proc/cpuinfo | grep \"model name\" | uniq"
echo
report lscpu
echo
report "cat /proc/meminfo | grep MemTotal"
echo

echo "Software"
echo "--------"
echo

report "uname -a"
echo

report "cat /etc/lsb-release"
echo

report "cc --version"
echo

report "java -version"
echo
