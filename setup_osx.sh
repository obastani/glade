#!/bin/bash

# Copyright 2015-2016 Stanford University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# set up folders
DIR="$(pwd)"
mkdir prog-query
cd prog-query

# sed
mkdir sed
cd sed
curl ftp://ftp.gnu.org/gnu/sed/sed-4.2.2.tar.gz > sed-4.2.2.tar.gz
tar xf sed-4.2.2.tar.gz
cd sed-4.2.2
./configure
make
cd $DIR/prog-query

# grep
mkdir grep
cd grep
curl ftp://ftp.gnu.org/gnu/grep/grep-2.23.tar.xz > grep-2.23.tar.xz
tar xf grep-2.23.tar.xz
cd grep-2.23
./configure
make
cd $DIR/prog-query

# flex
mkdir flex
cd flex
# curl doesn't work here
wget https://sourceforge.net/projects/flex/files/flex-2.6.0.tar.gz
tar xf flex-2.6.0.tar.gz
cd flex-2.6.0
./configure
make
cd $DIR/prog-query

# xml
mkdir xml
cd xml
curl ftp://xmlsoft.org/libxml2/libxml2-2.9.2.tar.gz > libxml2-2.9.2.tar.gz
tar xf libxml2-2.9.2.tar.gz
cd libxml2-2.9.2
./configure
make
cd $DIR/prog-query

# python
mkdir python
cd python
curl https://www.python.org/ftp/python/2.7.10/Python-2.7.10.tgz > Python-2.7.10.tgz
tar xf Python-2.7.10.tgz
cd Python-2.7.10
./configure
make
# hack for Mac OS X
mv python python_
mv python.exe python
# end hack
cd $DIR/prog-query

# end in root directory
cd $DIR
