#!/bin/bash
BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
LOG_FOLDER=$BASE_DIR/../data/
mkdir $LOG_FOLDER/rpython
mkdir $LOG_FOLDER/hotspot

source $BASE_DIR/../implementations/script.inc

INFO Start Collecting Traces from RTruffleSOM-OMOP

cd $BASE_DIR/../implementations/RTruffleSOM/

for bench in DirectAdd DnuAdd DnuPerformAdd PerformAdd \
            ProxyAdd IndirectAdd
do
 PYPYLOG=jit-log-opt,jit-backend:$LOG_FOLDER/rpython/$bench.log ./RTruffleSOM-jit -cp Smalltalk:Examples/Benchmarks/DoesNotUnderstand Examples/Benchmarks/BenchmarkHarness.som $bench 100 0 1000
done

cd $BASE_DIR/../implementations/RTruffleSOM-OMOP/

for bench in AddDispatch AddFieldWrite DispatchEnforcedStd FieldRead GlobalRead ReqPrim \
            AddDispatchEnforced AddFieldWriteEnforced DispatchEnforced FieldReadEnforced GlobalReadEnforced ReqPrimEnforced
do
 PYPYLOG=jit-log-opt,jit-backend:$LOG_FOLDER/rpython/$bench.log ./RTruffleSOM-jit -cp Smalltalk:Examples/Benchmarks/OMOP Examples/Benchmarks/BenchmarkHarness.som $bench 100 0 1000
done

INFO Start Collecting Hotspot Compilation Results

cd $BASE_DIR/../implementations/TruffleSOM

export GRAAL_FLAGS="-XX:+UnlockDiagnosticVMOptions -XX:CompileCommand=print,*::callRoot -G:-TraceTruffleInlining -G:-TraceTruffleCompilation "

for bench in DirectAdd DnuAdd DnuPerformAdd PerformAdd \
             ProxyAdd IndirectAdd
do
  ../graal.sh -G:+TruffleSplitting $GRAAL_FLAGS \
             -Xbootclasspath/a:build/classes:../graal/truffle.jar som.vm.Universe \
             -cp Smalltalk:Examples/Benchmarks/DoesNotUnderstand Examples/Benchmarks/BenchmarkHarness.som \
             $bench 100 0 1000 > $LOG_FOLDER/hotspot/$bench.log
done



cd $BASE_DIR/../implementations/TruffleSOM-OMOP

for bench in AddDispatch AddFieldWrite DispatchEnforcedStd FieldRead GlobalRead ReqPrim \
             AddDispatchEnforced AddFieldWriteEnforced DispatchEnforced FieldReadEnforced GlobalReadEnforced ReqPrimEnforced
do
  ../graal.sh -G:+TruffleSplittingNew $GRAAL_FLAGS \
             -Xbootclasspath/a:build/classes:../graal/truffle.jar som.vm.Universe \
             -cp Smalltalk:Examples/Benchmarks/OMOP Examples/Benchmarks/BenchmarkHarness.som \
             $bench 100 0 1000 > $LOG_FOLDER/hotspot/$bench.log
done

