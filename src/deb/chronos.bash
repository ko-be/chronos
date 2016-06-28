#!/bin/bash
###############################################################################
# Chronos run script
###############################################################################

me=$(readlink -n -f $0)
bin=$(dirname $me)
#process=$(basename $me)

args=()
  [[ ! -f /etc/default/chronos ]]        || . /etc/default/chronos
  [[ ! ${ULIMIT:-} ]]    || ulimit $ULIMIT
  [[ ! ${ZK_PATH:-} ]]   || args+=( --zk_path $ZK_PATH )
  [[ ! ${ZK_HOSTS:-} ]]  || args+=( --zk_hosts $ZK_HOSTS )
  [[ ! ${MASTER:-} ]]    || args+=( --master $MASTER )
  [[ ! ${HOSTNAME:-} ]]  || args+=( --hostname $HOSTNAME )
  [[ ! ${PORT:-} ]]      || args+=( --http_port $PORT )
  [[ ! ${USER-} ]]       || args+=( --user $USER )
  [[ ! ${CPU-} ]]        || args+=( --mesos_task_cpu $CPU )
  [[ ! ${MEM-} ]]        || args+=( --mesos_task_mem $MEM )
  [[ ! ${DISK-} ]]       || args+=( --mesos_task_disk $DISK )
  [[ ! ${ROLE-} ]]       || args+=( --mesos_role $ROLE )
  [[ ! ${FW_NAME-} ]]    || args+=( --mesos_framework_name $FW_NAME )
  [[ ! ${OPTS-} ]]       || args+=( ${OPTS} )
EXTRA_OPTS="${args[@]}"

echo "chronos home: ${CHRONOS_HOME}"
echo "mesos home: ${MESOS_HOME}"
chronos_jar=$(find ${CHRONOS_HOME} -name "chronos-*.jar" | grep -v sources | head -n1)
mesos_jar=$(find ${MESOS_HOME} -name "mesos-*.jar" | grep -v sources | head -n1)

#libmesos_file=$(find ${MESOS_HOME} -name "libmesos.dylib" -or -name "libmesos.so" | head -n1)
#echo "mesos lib: ${libmesos_file}"
export MESOS_NATIVE_JAVA_LIBRARY="${MESOS_LIB}"

##- JAVA OPTIONS ==============================================================
HEAP=${HEAP:-1024M}
PERMGEN=${HEAP:-256M}

#- Heap space -----------------------------------------------------------------
echo "==> HEAP memory used: $HEAP"
JAVA_OPTS="$JAVA_OPTS -Xmx$HEAP -Xms$HEAP"
#
#- PermGen space --------------------------------------------------------------
echo "==> PERMGEN memory used: $PERMGEN"
JAVA_OPTS="$JAVA_OPTS -XX:PermSize=$PERMGEN -XX:MaxPermSize=$PERMGEN"
#
#- CLASSPATH ------------------------------------------------------------------
JAVA_OPTS="$JAVA_OPTS -Djava.library.path=${JAVA_LIBPATH:-/usr/lib/} -cp $chronos_jar:$mesos_jar"
#
#- SpyMemcached logging implementation ----------------------------------------
#JAVA_OPTS="$JAVA_OPTS -Dnet.spy.log.LoggerImpl=net.spy.memcached.compat.log.Log4JLogger"
#
#- Java Management eXtensions -------------------------------------------------
#JMX_PORT=${JMX_PORT:-9004}
#JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$JMX_PORT"
#JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.authenticate=false"
#JAVA_OPTS="$JAVA_OPTS -Dcom.sun.management.jmxremote.ssl=false"
#
##-============================================================================

echo -e "Launch Chronos"
CMD="java $JAVA_OPTS org.apache.mesos.chronos.scheduler.Main $EXTRA_OPTS $@"
echo -e "cmd: $CMD"
exec $CMD

