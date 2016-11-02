#!/usr/bin/env bash

cd `dirname $0`/../
app_home=`pwd`
lib_dir=${app_home}/lib
app_mainclass=("org.wex.Bootstrap")
app_classpath="$lib_dir/data-collect.data-collect-1.0-alpha-10.jar:$lib_dir/org.scala-lang.scala-library-2.11.8.jar:$lib_dir/com.typesafe.akka.akka-stream_2.11-2.4.12.jar:$lib_dir/com.typesafe.akka.akka-actor_2.11-2.4.12.jar:$lib_dir/com.typesafe.config-1.3.0.jar:$lib_dir/org.scala-lang.modules.scala-java8-compat_2.11-0.7.0.jar:$lib_dir/org.reactivestreams.reactive-streams-1.0.0.jar:$lib_dir/com.typesafe.ssl-config-core_2.11-0.2.1.jar:$lib_dir/org.scala-lang.modules.scala-parser-combinators_2.11-1.0.4.jar:$lib_dir/com.typesafe.akka.akka-slf4j_2.11-2.4.12.jar:$lib_dir/org.slf4j.slf4j-api-1.7.16.jar:$lib_dir/com.typesafe.akka.akka-http-core_2.11-2.4.11.jar:$lib_dir/com.typesafe.akka.akka-parsing_2.11-2.4.11.jar:$lib_dir/com.typesafe.akka.akka-http-spray-json-experimental_2.11-2.4.11.jar:$lib_dir/com.typesafe.akka.akka-http-experimental_2.11-2.4.11.jar:$lib_dir/io.spray.spray-json_2.11-1.3.2.jar:$lib_dir/com.wingtech.ojdbc-7.jar:$lib_dir/org.apache.logging.log4j.log4j-core-2.7.jar:$lib_dir/org.apache.logging.log4j.log4j-api-2.7.jar:$lib_dir/org.scalactic.scalactic_2.11-3.0.0.jar:$lib_dir/org.scala-lang.scala-reflect-2.11.8.jar:$lib_dir/org.quartz-scheduler.quartz-2.2.3.jar:$lib_dir/c3p0.c3p0-0.9.1.1.jar"
java_cmd=java

check_java_command(){
    type java >/dev/null 2>&1 || { echo >&2 "I require foo but it's not installed.  Aborting."; exit 1; }
}

check_java_version(){
    if [ `java -version 2>&1 | head -1 | grep "1.8" | wc -l` != 1 ]; then
        echo "java version require 1.8."
        exit 1;
    fi
}

process_args(){
  check_java_command
  check_java_version

  case "$1" in
    -h|-help) help; exit 1 ;;
    -start) start ;;
    -stop) stop ;;
    *) help; exit 1;;
    esac
}

start(){
  nohup $java_cmd -cp ${app_classpath} ${app_mainclass} &
}

stop(){
  app_pid_file=$app_home/RUNNING_PID
  if [ -f $app_pid_file ]
  then
    app_pid=`cat $app_pid_file`
    kill -9 $app_pid
    rm -f $app_pid_file
  else
    echo "pid file not exist."
    exit 1
  fi
}

help(){
  echo -e "
    -h | -help         print help
    -start      start data-collect
    -stop        stop data-collect
  "
}

process_args $1