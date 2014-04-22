#!/bin/bash

if [ -x /usr/libexec/java_home ]   # usually on OS X, this tool is available
then
  export JAVA7_HOME=`/usr/libexec/java_home -v 1.7`
  export JAVA8_HOME=`/usr/libexec/java_home -v 1.8`
elif [ -x /usr/sbin/update-java-alternatives ]
then
  jvm_info=(`/usr/sbin/update-java-alternatives -l | grep java-1.7`)
  export JAVA7_HOME=${jvm_info[2]}
  jvm_info=(`/usr/sbin/update-java-alternatives -l | grep java-8`)
  export JAVA8_HOME=${jvm_info[2]}
else
  WARN Java directories could not be guessed, please check config.inc
fi

