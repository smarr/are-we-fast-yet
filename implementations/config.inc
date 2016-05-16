#!/bin/bash

SCRIPT_PATH=`dirname $0`

if [ -x /usr/libexec/java_home ]   # usually on OS X, this tool is available
then
  export JAVA7_HOME=`/usr/libexec/java_home -v 1.7`
  export JAVA8_HOME=`/usr/libexec/java_home -v 1.8`
  export GRAAL_GRAAL_BASIC_CMD=TODO
  export GRAAL_GRAAL_ENTER_CMD=TODO
  export GRAAL_BASIC_CMD=/Users/smarr/Projects/SOM/graal/jvmci/jdk1.8.0_65/product/bin/java
  export GRAAL_ENTER_CMD=TODO

  export MX=/Users/smarr/Projects/SOM/graal/mx/mx
  export GRAAL_JS=/Users/smarr/Projects/PostDoc/Truffle/graal-js-ws/graal-js
  
elif [ -x /usr/sbin/update-java-alternatives ]
then
  jvm_info=(`/usr/sbin/update-java-alternatives -l | grep java-1.7`)
  export JAVA7_HOME=${jvm_info[2]}
  jvm_info=(`/usr/sbin/update-java-alternatives -l | grep java-8`)
  export JAVA8_HOME=${jvm_info[2]}

  if [ -x /home/smarr/Projects/graal-graal/jvmci/jdk1.8.0_66/product/bin/java ]; then
    export GRAAL_GRAAL_BASIC_CMD=/home/smarr/Projects/graal-graal/jvmci/jdk1.8.0_91/product/bin/java
    export GRAAL_GRAAL_ENTER_CMD=/home/smarr/Projects/graalvm-0.11-dk/jre/bin/java
    export GRAAL_BASIC_CMD=/home/smarr/Projects/RoarVM-buildslave/GraalBasic/build/jvmci/jdk1.8.0_77/product/bin/java
    export GRAAL_VM_BIN=/home/smarr/Projects/graalvm-0.11-dk/jre/bin/java

    export MX=/home/smarr/Projects/mx/mx
    export GRAAL_JS=/home/smarr/Projects/graalvm-0.11-dk/bin/node
  else
    WARN Graal-enabled VMs not configured, please check config.inc.
  fi
else
  WARN Java directories could not be guessed, please check config.inc
fi

export JRUBY_CMD=$SCRIPT_PATH/JRuby/bin/jruby
export RBX_314_CMD=/home/smarr/Projects/rubinius/3.14/bin/rbx

function home_from_command() {
  HOME=`dirname $1`
  HOME=`dirname $HOME`
  HOME=`dirname $HOME`
  HOME=`dirname $HOME`
}
