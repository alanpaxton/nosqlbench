#!/bin/bash

# Paths and finding nosqlbench
NOSQLBENCH_DIR=/Users/alan/swProjects/evolvedBinary/nosqlbench
NB5_JAR=${NOSQLBENCH_DIR}/nb5/target/nb5.jar

# 
XMLGEN_SCRIPT=existdb_xmlgen_auction.yaml
XMLGEN_PATH=adapter-xmlgen/target/classes/activities
XMLGEN_JAR=${NOSQLBENCH_DIR}/${XMLGEN_PATH}/${XMLGEN_SCRIPT}
echo ${XMLGEN_JAR}

# Scaling and configuration of auction site
AUCTION_SCALE=10
OPEN_AUCTION_COUNT=$((AUCTION_SCALE * 12000))
CLOSED_AUCTION_COUNT=$((AUCTION_SCALE * 9750))
PERSON_COUNT=$((AUCTION_SCALE * 25500))
ITEM_COUNT=$((OPEN_AUCTION_COUNT + CLOSED_AUCTION_COUNT))
CATEGORY_COUNT=$((AUCTION_SCALE * 1000))

echo java -jar ${NB5_JAR} ${XMLGEN_JAR} xmlgen \
    openauctionscount=${OPEN_AUCTION_COUNT} \
    closedauctionscount=${CLOSED_AUCTION_COUNT} \
    personcount=${PERSON_COUNT} \
    itemcount=${ITEM_COUNT} \
    categorycount=${CATEGORY_COUNT}

java -jar ${NB5_JAR} ${XMLGEN_JAR} xmlgen \
    openauctionscount=${OPEN_AUCTION_COUNT} \
    closedauctionscount=${CLOSED_AUCTION_COUNT} \
    personcount=${PERSON_COUNT} \
    itemcount=${ITEM_COUNT} \
    categorycount=${CATEGORY_COUNT}
