#!/bin/bash

mvn clean package -Dmaven.test.skip=true -Pdeploy
