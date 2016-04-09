#!/bin/bash

ant > log 2>&1 &

echo $! > pid

tailf log
