FROM golang:1.14-alpine AS builder

ENV CGO_ENABLED=0
ENV GOOS=linux 
ENV GOARCH=amd64

COPY main.go go.mod /confirmation/
COPY proto /confirmation/proto/
WORKDIR /confirmation
RUN go build -o main .


FROM alpine

WORKDIR /usr/confirmation
COPY --from=builder /confirmation/main /usr/confirmation

CMD ["/usr/confirmation/main"]