# ./keycloak/Dockerfile
FROM quay.io/keycloak/keycloak:21.1.1

# Включаем доступ к репозиториям и устанавливаем curl
USER root
RUN dnf install -y curl --setopt=tsflags=nodocs --enablerepo=ubi-9-baseos && \
    dnf clean all
USER 1000