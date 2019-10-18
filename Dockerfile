FROM docker.elastic.co/elasticsearch/elasticsearch-oss:6.6.2

COPY . /tmp/.

RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install -b file:///tmp/target/releases/jwixel-auth-plugin-1.1-SNAPSHOT.zip
