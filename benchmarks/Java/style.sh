#!/bin/bash
pushd `dirname $0`
ant checkstyle-jar && ant checkstyle
