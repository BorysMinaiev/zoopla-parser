#!/usr/bin/env bash

BASEDIR=$(dirname "$0")
cd $BASEDIR
gradle run --args='config-new.txt'
#  -check-properties-in-db -- in case you need it
