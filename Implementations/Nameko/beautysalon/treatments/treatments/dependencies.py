from nameko import config
from nameko.extensions import DependencyProvider
import redis


REDIS_URI_KEY = 'REDIS_URI'


class StorageWrapper:

    def __init__(self, client):
        self.client = client

    def _format_key(self, treatment_id):
        return 'treatments:{}'.format(treatment_id)

    def _from_hash(self, document):
        return {
            'id': int(document[b'id']),
            'name': document[b'name'].decode('utf-8'),
            'minduration': int(document[b'minduration']),
            'maxduration': int(document[b'maxduration']),
            'price': int(document[b'price'])
        }

    def get(self, treatment_id):
        treatment = self.client.hgetall(self._format_key(treatment_id))
        return self._from_hash(treatment)

    def list(self):
        keys = self.client.keys(self._format_key('*'))
        for key in keys:
            yield self._from_hash(self.client.hgetall(key))

    def create(self, treatment):
        self.client.hmset(
            self._format_key(treatment['id']),
            treatment)


class Storage(DependencyProvider):

    def setup(self):
        self.client = redis.StrictRedis.from_url(config.get(REDIS_URI_KEY))

    def get_dependency(self, worker_ctx):
        return StorageWrapper(self.client)
