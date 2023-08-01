#!/bin/bash


# 获取当前脚本所在的工作目录
SCRIPT_PATH=$(cd $(dirname $0) && pwd)

APP_NAME=$SCRIPT_PATH/aliDDns-1.0-SNAPSHOT.jar
Out_log=$2

#使用说明，用来提示输入参数
usage() {
    echo "Usage: sh 脚本名.sh [start|stop|restart|status]"
    exit 1
}

#检查程序是否在运行
is_exist(){
  pid=`ps -ef|grep $APP_NAME|grep -v grep|awk '{print $2}' `
  #如果不存在返回1，存在返回0
  if [ -z "${pid}" ]; then
    return 1
  else
    return 0
  fi
}

#启动方法
start(){
  is_exist
  if [ $? -eq "0" ]; then
    echo "${APP_NAME} is already running. pid=${pid} ."
  else
    echo $Out_log
    if [ "$Out_log" == "log" ]; then
    #启动时设置并发垃圾收集器
      nohup java -Xms64m -Xmx64m -XX:MetaspaceSize=16m -XX:MaxMetaspaceSize=32m -cp  $APP_NAME DDNS >> $SCRIPT_PATH/output.log 2>&1 &
      echo "${APP_NAME} log  start success"
      # 在子进程启动后，父进程退出，实现Type=forking的效果
      exit 0
    else
      nohup java -Xms64m -Xmx64m -XX:MetaspaceSize=16m -XX:MaxMetaspaceSize=32m -cp  $APP_NAME DDNS > /dev/null &
      echo "${APP_NAME} start success"
      # 在子进程启动后，父进程退出，实现Type=forking的效果
      exit 0
    fi
  fi
}

#停止方法
# shellcheck disable=SC2120
stop(){
  is_exist
  if [ $? -eq "0" ]; then
    PROCESS=`ps -ef|grep $APP_NAME|grep -v grep|grep -v PPID|awk '{ print $2}'`
    for i in $PROCESS
    do
      echo "Kill the $1 process [ $i ]"
      kill -9 $i
    done
  else
    echo "${APP_NAME} is not running"
  fi
}

#输出运行状态
status(){
  is_exist
  if [ $? -eq "0" ]; then
    echo "${APP_NAME} is running. Pid is ${pid}"
  else
    echo "${APP_NAME} is NOT running."
  fi
}

#重启
restart(){
  stop
  start
}

#根据输入参数，选择执行对应方法，不输入则执行使用说明
case "$1" in
  "start")
    start
    ;;
  "stop")
    stop
    ;;
  "status")
    status
    ;;
  "restart")
    restart
    ;;
  *)
    usage
    ;;
esac

