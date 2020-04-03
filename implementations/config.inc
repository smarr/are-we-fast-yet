#!/bin/bash

export JVMCI_VERSION_CHECK=ignore
GRAAL_CORE_CMD=$SCRIPT_PATH/graal-core/bin/java

GRAAL_HOSTED_FLAGS="-server -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -d64 "
GRAAL_JIT_FLAGS="-server -XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler -d64 "

export RBX_314_CMD=$RUBINIUS_BIN




if [ -x /usr/libexec/java_home ]   # usually on OS X, this tool is available
then
  export JAVA8_HOME=`/usr/libexec/java_home -v 1.8`  
elif [ -x /usr/sbin/update-java-alternatives ]
then
  jvm_info=(`/usr/sbin/update-java-alternatives -l | grep java-8`) || true
  if [ -z "$jvm_info" ]
  then
    jvm_info=(`/usr/sbin/update-java-alternatives -l | grep java-1.8`) || true
  fi
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
