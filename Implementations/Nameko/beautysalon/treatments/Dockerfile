FROM python:3.6.10

ENV NAMEKO_VERSION=3.0.0-rc8

COPY . /treatments

WORKDIR /treatments

RUN pip3 install -r requirements.txt

RUN wget https://github.com/nameko/nameko/archive/v${NAMEKO_VERSION}.zip && unzip v${NAMEKO_VERSION}.zip && rm v${NAMEKO_VERSION}.zip
RUN cd nameko-${NAMEKO_VERSION} && python3 setup.py install

RUN pip3 install -r required-nameko-extensions.txt

ENTRYPOINT ["/bin/bash"]

CMD ["./startservice.sh"]