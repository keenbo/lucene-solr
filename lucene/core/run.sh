#!/bin/bash

ant -Dversion=6.4.1

dirs=(7200 6200)
for dir in ${dirs[@]}
do
	cp ../build/core/lucene* /cygdrive/d/es_instance/new/es-5.2.2-${dir}/lib/
done


