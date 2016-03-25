# Copyright 2011 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# Point to Java compiler (change path according to your setting):
#
COMPILER=./javac

#
# Point to Java binary (change path according to your setting):
#
JAVA=./java

OPTS= -server -Xss15500k

LoopTesterApp.jar: LoopTesterApp.class
	jar -cvf LoopTesterApp.jar `find . -name \*.class`

LoopTesterApp.class: LoopTesterApp.java
	$(COMPILER) LoopTesterApp.java

run: 
	$(JAVA)  \
	  $(OPTS) -cp .:$(LIB) LoopTesterApp

clean:
	find . -name \*.class | xargs rm -f
	find . -name \*.jar | xargs rm -f
	find . -name \*~ | xargs rm -f
