#!/bin/bash
set -e
mkdir /opt/vexpress-pricing
chown $1 /opt/vexpress-pricing
cd /opt/vexpress-pricing
mv /tmp/application.properties .
wget --auth-no-challenge --user=$2 --password=$3 $4/artifact/build/libs/vexpress-pricing-$5.jar -O vexpress-pricing.jar
mv /tmp/vexpress-pricing.service /etc/systemd/system
chmod 664 /etc/systemd/system/vexpress-pricing.service

# Wait until Java is installed
until [ -f /usr/bin/java ]; do
  sleep 10
done

systemctl enable vexpress-pricing
systemctl start vexpress-pricing
