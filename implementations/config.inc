#!/bin/bash

if [ -x /usr/libexec/java_home ]   # usually on OS X, this tool is available
then
  export JAVA7_HOME=`/usr/libexec/java_home -v 1.7`
  export JAVA8_HOME=`/usr/libexec/java_home -v 1.8`
  export GRAAL_GRAAL_BASIC_CMD=TODO
  export GRAAL_GRAAL_ENTER_CMD=TODO
  export GRAAL_BASIC_CMD=/Users/smarr/Projects/SOM/graal/jvmci/jdk1.8.0_65/product/bin/java
  export GRAAL_ENTER_CMD=TODO
  
elif [ -x /usr/sbin/update-java-alternatives ]
then
  jvm_info=(`/usr/sbin/update-java-alternatives -l | grep java-1.7`)
  export JAVA7_HOME=${jvm_info[2]}
  jvm_info=(`/usr/sbin/update-java-alternatives -l | grep java-8`)
  export JAVA8_HOME=${jvm_info[2]}

  if [ -x /home/smarr/Projects/graal-graal/jvmci/jdk1.8.0_66/product/bin/java ]; then
    export GRAAL_GRAAL_BASIC_CMD=/home/smarr/Projects/graal-graal/jvmci/jdk1.8.0_66/product/bin/java
    export GRAAL_GRAAL_ENTER_CMD=/home/smarr/Projects/graal-e-graal/jvmci/jdk1.8.0_66/product/bin/java
    export GRAAL_BASIC_CMD=/home/smarr/Projects/graal/jvmci/jdk1.8.0_66/product/bin/java
    export GRAAL_ENTER_CMD=/home/smarr/Projects/graal-e/jvmci/jdk1.8.0_66/product/bin/java
  else
    WARN Graal-enabled VMs not configured, please check config.inc.
  fi
else
  WARN Java directories could not be guessed, please check config.inc
fi

