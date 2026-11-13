#!/bin/bash
# usage: kc.sh outdir classpath sources...
J=/home/claude/kj/run_kotlin_kernel/jars
ALL=$J/kotlin-jupyter-kernel-0.19.0-944-all.jar
out=$1; cp=$2; shift 2
java -Xmx2200m -XX:+UseSerialGC -cp $ALL org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -no-reflect -jvm-target 17 -Xplugin=/home/claude/kt/serialization-plugin.jar \
  -cp "$J/kotlin-stdlib-2.3.10-RC.jar:$J/kotlinx-serialization-core-jvm-1.9.0.jar:$J/kotlinx-serialization-json-jvm-1.9.0.jar:$J/annotations-13.0.jar:$cp" -d $out "$@"
