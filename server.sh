#!/bin/bash

javac -cp ./src/My_Jar/*:./src:./out -d ./out/ ./src/*.java

java -cp "./src/My_Jar/*:./out" ServerMain
