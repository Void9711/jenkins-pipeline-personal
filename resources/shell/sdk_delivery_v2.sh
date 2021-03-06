#!/bin/bash
#更新SDK相关项目

sdk_out=("pmall.war" "pout.war" "pserver.war" "scenter.war" "umall.war" "uout.war" "pchannel.war" "uchannel.war")
sdk_inner=("email.war" "pinner.war" "psgop.war" "uinner.war" "usgop.war")
sdk_job=("pjob.zip")
sdk_anti=("anti-addiction-service-0.0.1-SNAPSHOT.jar")
update_path="/data/ServerUpdate/"
deploy_dir_out='/data/java/tomcat/apache-tomcat-out-v2/webapps/'
deploy_dir_inner='/data/java/tomcat/apache-tomcat-inner-v2/webapps/'
deploy_dir_job="/data/java/job/"
deploy_dir_lib_channels="/data/java/tomcat/lib/"
deploy_dir_anti='/data/java/tomcat/apache-tomcat-anti/'
LOG_PATH="/data/java/ac_log/"
LOG_NAME="delivery.log"


param_cnt=$#
update_ver="deploy"

function REPORT
{
  echo -e "[`date "+%F %T"`] $1" | tee -a ${LOG_PATH}${LOG_NAME}
}

function REPORTINFO
{
  REPORT "\033[40;32m[INFO]\033[0m $1"
}


function REPORTERROR
{
  REPORT "\033[40;31m[ERROR]\033[0m $1"
  exit 1
}

function REPORTALL
{
  if [ $? -ne 0 ]; then
    REPORTERROR "$2"
  else
    REPORTINFO "$1"
  fi
}

function check_path()
{
  if [[ ! -d "${update_path}${update_path}${update_ver}/channels" ]]; then
    mkdir -pv ${update_path}${update_ver}/channels
  fi
  if [[ ! -d "${deploy_dir_out}" ]]; then
    mkdir -pv ${deploy_dir_out}
    REPORTINFO "Cant find path ${deploy_dir_out}, created."
  fi
  if [[ ! -d "${deploy_dir_inner}" ]]; then
    mkdir -pv ${deploy_dir_inner}
    REPORTINFO "Cant find path ${deploy_dir_inner}, created"
  fi
  if [[ ! -d "${deploy_dir_lib_channels}/channels" ]]; then
    mkdir -pv ${deploy_dir_lib_channels}/channels
    REPORTINFO "Cant find path ${deploy_dir_lib_channels}/channels, created"
  fi
  if [[ ! -d "${deploy_dir_job}" ]]; then
    mkdir -pv ${deploy_dir_job}
    REPORTINFO "Cant find path ${deploy_dir_job}, created"
  fi
  if [[ ! -d "${deploy_dir_anti}" ]]; then
    mkdir -pv ${deploy_dir_anti}
    REPORTINFO "Cant find path ${deploy_dir_anti}, created"
  fi
  if [[ ! -d "${LOG_PATH}" ]]; then
    mkdir -pv ${LOG_PATH}
    REPORTINFO "Cant find path ${LOG_PATH}, created"
  fi
}

function check_param()
{
  if [[ ! -d "${update_path}${update_ver}" ]]; then
    REPORTERROR "Cant find update path ${update_path}${update_ver}."
  fi
  REPORTINFO "Param check OK.${update_path}${update_ver}"
}

function extract_archive()
{
  cd ${update_path}
  tar xzf *.tar.gz -C ${update_path}${update_ver} && tar xzf ${update_path}${update_ver}/channels.tar.gz -C ${update_path}${update_ver}/channels
}

function delivery_war()
{
  /bin/cp -f ${update_path}${update_ver}/${1} ${2}${1}
  REPORTALL "Delivery ${2}${1} OK." "Delivery ${2}${1} failed, please check."
}

function delivery_pjob()
{
  /bin/cp -f ${update_path}${update_ver}/${1} ${2}${1}
  REPORTALL "Delivery ${2}${1} OK." "Delivery ${2}${1} failed, please check."
  unzip -o -q ${2}${1} -d ${2}
  REPORTALL "Unzip ${2}${1} OK." "Unzip ${2}${1} failed, please check."
}

function delivery_anti
{
  /bin/cp -f ${update_path}${update_ver}/${1} ${2}${1}
  REPORTALL "Delivery ${2}${1} OK." "Delivery ${2}${1} failed, please check."
  cd ${2} && /bin/bash ${2}restart.sh
  REPORTALL "Restart ${2}${1} OK." "Restart ${2}${1} failed, please check."
}

function delivery()
{
  cd ${update_path}${update_ver}
  for i in `find *`; do
    if [[ $i =~ channels/(.+) ]]; then
      REPORTINFO "\033[40;33m[$i in lib/channels]\033[0m"
      delivery_war $i ${deploy_dir_lib_channels}
    elif [[ "${sdk_out[*]}" =~ $i ]]; then
      REPORTINFO "\033[40;33m[$i in sdk_out]\033[0m"
      delivery_war $i ${deploy_dir_out}
    elif [[ "${sdk_inner[*]}" =~ $i ]]; then
      REPORTINFO "\033[40;33m[$i in sdk_inner]\033[0m"
      delivery_war $i ${deploy_dir_inner}
    elif [[ "${sdk_job[*]}" =~ $i ]]; then
      REPORTINFO "\033[40;33m[$i in sdk_job]\033[0m"
      delivery_pjob $i ${deploy_dir_job}
    elif [[ "${sdk_anti[*]}" =~ $i ]]; then
      REPORTINFO "\033[40;33m[$i in sdk_anti]\033[0m"
      delivery_anti $i ${deploy_dir_anti}
    else
      REPORTINFO "$i is unknown."
    fi
  done
}

function main()
{
  check_path
  REPORTINFO "Start delivery ----------------------------------------------------------------"
  extract_archive
  check_param
  delivery
  REPORTINFO "Delivery done  ----------------------------------------------------------------"
}

main
