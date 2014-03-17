#!/bin/sh

# if first time
mkdir -p data

#if not first time
rm --f data/enwikt*

wget -P data http://toolserver.org/~enwikt/definitions/enwikt-defs-latest-en.tsv.gz
cd data && gunzip enwikt-defs-latest-en.tsv.gz
