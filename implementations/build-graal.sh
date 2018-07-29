#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

INFO Build Graal
cd $SCRIPT_PATH
load_submodule graal
cd $SCRIPT_PATH/graal

## We got issues with the version check when building from git, so, disable it
export JVMCI_VERSION_CHECK=ignore

cd graal-jvmci-8
../mx/mx --java-home ${JAVA8_HOME} clean
../mx/mx --java-home ${JAVA8_HOME} build

export JVMCI_HOME=`../mx/mx --java-home ${JAVA8_HOME} jdkhome`

cd ..

echo ""
echo %% Build Graal Compiler
echo ""
cd truffle/compiler
../../mx/mx --java-home ${JVMCI_HOME} clean
../../mx/mx --java-home ${JVMCI_HOME} build


echo ""
echo %% Deploy Graal JDK

if [ -d $SCRIPT_PATH/graal-core ]; then
  rm -Rf $SCRIPT_PATH/graal-core
fi
../../mx/mx --java-home ${JVMCI_HOME} makegraaljdk $SCRIPT_PATH/graal-core
rm $SCRIPT_PATH/graal-core/jre/lib/jvmci/compiler-name

OK Graal Build Completed.
