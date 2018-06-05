#!/bin/bash

SERVICE="saga-create-client"

if [ "$1" == "" ]; then
    docker run --rm -dit --name $SERVICE sbonacho/$SERVICE
else
    docker stop $SERVICE
fi