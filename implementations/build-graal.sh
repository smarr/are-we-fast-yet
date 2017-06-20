#!/bin/bash
set -e # make script fail on first error
SCRIPT_PATH=`dirname $0`
source $SCRIPT_PATH/script.inc
source $SCRIPT_PATH/config.inc

INFO Build Graal
cd $SCRIPT_PATH
load_submodule graal
cd $SCRIPT_PATH/graal

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
echo %% Deploy Graal Into JVMCI Built
cp mxbuild/dists/graal.jar ${JVMCI_HOME}/jre/lib/jvmci/

if [ -d $SCRIPT_PATH/graal-core ]; then
  rm -Rf $SCRIPT_PATH/graal-core
fi
mv $JVMCI_HOME $SCRIPT_PATH/graal-core

OK Graal Build Completed.
