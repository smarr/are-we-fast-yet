#!/bin/bash

# make SCRIPT_PATH absolute
pushd `dirname $0` > /dev/null
SCRIPT_PATH=`pwd`
popd > /dev/null

export JVMCI_VERSION_CHECK=ignore
GRAAL_CORE_CMD=$SCRIPT_PATH/graal-core/bin/java

GRAAL_HOSTED_FLAGS="-server -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -d64 "
GRAAL_JIT_FLAGS="-server -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler -d64 "

## GraalVM Binary
export GRAAL_VM_CMD=$SCRIPT_PATH/graalvm/bin/java
export GRAAL_JS=$SCRIPT_PATH/graalvm/bin/node

export RBX_314_CMD=$RUBINIUS_BIN




if [ -x /usr/libexec/java_home ]   # usually on OS X, this tool is available
then
  export JAVA8_HOME=`/usr/libexec/java_home -v 1.8`  
elif [ -x /usr/sbin/update-java-alternatives ]
then
  jvm_info=(`/usr/sbin/update-java-alternatives -l | grep java-8`)
  export JAVA8_HOME=${jvm_info[2]}
else
  WARN Java directories could not be guessed, please check config.inc
fi

export JRUBY_CMD=$SCRIPT_PATH/JRuby/bin/jruby

function home_from_command() {
  HOME=`dirname $1`
  HOME=`dirname $HOME`
  HOME=`dirname $HOME`
  HOME=`dirname $HOME`
}
