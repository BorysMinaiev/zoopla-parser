#!/usr/bin/env bash

BASEDIR=$(dirname "$0")
cd $BASEDIR
gradle run --args "test config.txt"
