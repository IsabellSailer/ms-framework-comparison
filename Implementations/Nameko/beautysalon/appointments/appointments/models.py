import datetime

from sqlalchemy import (
    Column, Date, DateTime, ForeignKey, Integer, String
)
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship


class Base(object):
    created_at = Column(
        DateTime,
        default=datetime.datetime.utcnow,
        nullable=False
    )


DeclarativeBase = declarative_base(cls=Base)


class Appointment(DeclarativeBase):
    __tablename__ = "appointment"

    id = Column(Integer, primary_key=True, autoincrement=True)
    customer_name = Column(String, nullable=False)
    treatment_id = Column(Integer, nullable=False)
    treatment_name = Column(String, nullable=False)
    date = Column(Date, nullable=False)
    start_time = Column(Integer, nullable=False)
    end_time = Column(Integer, nullable=False)
    duration= Column(Integer, nullable=False)

