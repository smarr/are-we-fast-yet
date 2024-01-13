#!/bin/bash
BENCHMARKS=("NBody      10 250000"
            "Richards   10 100"
            "DeltaBlue  10 1200"
            "Mandelbrot 10 500"
            "Queens     10 1000"
            "Towers     10 600"
            "Bounce     10 1500"
            "CD         10 250"
            "Json       10 100"
            "List       10 1500"
            "Storage    10 1000"
            "Sieve      10 3000"
            "Mandelbrot 10 500"
            "Permute    10 1000"
            "Bounce     10 1500"
            "Mandelbrot 10 500"
            "Havlak     10 1500")

ant clean
ant jar


if [ "$1" = "pgo" ]; then
  echo Building with PGO
  native-image --pgo-instrument -cp benchmarks.jar Harness benchmarks-instr

  PROFILES=""

  for b in "${BENCHMARKS[@]}"; do
    read -ra BENCH_SPEC <<< "$b"
    BENCH_NAME="${BENCH_SPEC[0]}"
    PROFILES="${PROFILES},$BENCH_NAME.iprof"
    ./benchmarks-instr -XX:ProfilesDumpFile=${BENCH_NAME}.iprof $b
  done

  # skip the leading comma
  PROFILES="${PROFILES:1}"

  echo Building Final Image
  echo native-image --pgo="$PROFILES" -cp benchmarks.jar Harness benchmarks-pgo
  exec native-image --pgo="$PROFILES" -cp benchmarks.jar Harness benchmarks-pgo
else
  echo Building without PGO
  exec native-image -cp benchmarks.jar Harness benchmarks
fi
