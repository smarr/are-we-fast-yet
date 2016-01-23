#!/bin/bash

set -e

for f in bounce.cr json.cr list.cr mandelbrot.cr nbody.cr permute.cr queens.cr sieve.cr towers.cr # *.cr deltablue.cr richards.cr storage.cr
do
  crystal build --release $f
done
