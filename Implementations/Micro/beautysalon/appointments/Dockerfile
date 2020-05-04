FROM golang:1.13-alpine AS builder

ENV CGO_ENABLED=0
ENV GOOS=linux 
ENV GOARCH=amd64

COPY main.go go.mod /appointments/
COPY proto /appointments/proto/
WORKDIR /appointments
RUN go build -o main .


FROM alpine

WORKDIR /usr/appointments
COPY --from=builder /appointments/main /usr/appointments
COPY config-docker.json /usr/appointments/config.json

EXPOSE 8081
CMD ["/usr/appointments/main"]