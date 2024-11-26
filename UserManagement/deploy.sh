#!/bin/bash

if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <image_name>"
    exit 1
fi

IMAGE_NAME=$1

sbt clean compile stage

sudo docker build -t "$IMAGE_NAME" .

echo "Docker image '$IMAGE_NAME' has been built."
