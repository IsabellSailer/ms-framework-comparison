FROM golang:1.13-alpine AS builder

ENV CGO_ENABLED=0
ENV GOOS=linux 
ENV GOARCH=amd64

COPY main.go go.mod /treatments/
WORKDIR /treatments
RUN go build -o main .


FROM alpine

WORKDIR /usr/treatments
COPY --from=builder /treatments/main /usr/treatments
COPY config-docker.json /usr/treatments/config.json

EXPOSE 8080
CMD ["/usr/treatments/main"]