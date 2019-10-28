FROM docker.elastic.co/elasticsearch/elasticsearch-oss:6.6.2

COPY . /tmp/.

RUN /usr/share/elasticsearch/bin/elasticsearch-keystore create
RUN echo "jwixeladmin" | /usr/share/elasticsearch/bin/elasticsearch-keystore add --stdin jwixel.admin_user
RUN echo "let-me-pass" | /usr/share/elasticsearch/bin/elasticsearch-keystore add --stdin jwixel.admin_pass
RUN echo "144c7672-f4d6-4011-b70d-408ce0a3358a" | /usr/share/elasticsearch/bin/elasticsearch-keystore add --stdin jwixel.auth_token

RUN /usr/share/elasticsearch/bin/elasticsearch-plugin install -b file:///tmp/target/releases/jwixel-auth-plugin-1.1-SNAPSHOT.zip
