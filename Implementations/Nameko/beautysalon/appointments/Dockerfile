FROM python:3.6.10

ENV NAMEKO_VERSION=3.0.0-rc8

COPY . /appointments

WORKDIR /appointments

RUN pip3 install -r requirements.txt

RUN wget https://github.com/nameko/nameko/archive/v${NAMEKO_VERSION}.zip && unzip v${NAMEKO_VERSION}.zip && rm v${NAMEKO_VERSION}.zip
RUN cd nameko-${NAMEKO_VERSION} && python3 setup.py install

RUN pip3 install -r required-nameko-extensions.txt

RUN cd appointments && alembic init alembic

# replace url with url to database container
RUN cd appointments && sed -i 's/^sqlalchemy\.url.*/sqlalchemy.url = postgres:\/\/postgres:docker@appointments-db\/appointments/' alembic.ini

# reference models.py in env.py so that alembic autogenerate works
RUN cd appointments && sed -i 's/^target_metadata = None/from models import DeclarativeBase\ntarget_metadata = DeclarativeBase.metadata/' alembic/env.py

ENTRYPOINT ["/bin/bash"]

CMD ["./startservice.sh"]