#!/bin/sh
updatebot push-regex -r "\s+tag: (.*)" -v $(cat VERSION) --previous-line "\s+repository: activiti/ttc-query-campaign" **/values.yaml