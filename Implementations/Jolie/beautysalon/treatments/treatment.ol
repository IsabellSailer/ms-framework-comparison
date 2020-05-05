include "console.iol"
include "string_utils.iol"
include "file.iol"


include "./public/interfaces/TreatmentInterface.iol"
include "./public/interfaces/MongoDBConnector.iol"

execution{ concurrent }

inputPort TREATMENTS {
  Location: "local"
  Protocol: http
  Interfaces: TreatmentInterface
}

inputPort GETTREATMENT {
  Location: "socket://localhost:8090/"
  Protocol: http
  Interfaces: TreatmentInterface
}


init {
readFile@File( {
    filename = "config.json"
    format = "json"
})(config)

scope (InsertMongoDB){
  install (default => valueToPrettyString@StringUtils(InsertMongoDB)(s);
           println@Console(s)());
           connectValue.host = config.database.redis.host;
           connectValue.dbname ="admin";
           connectValue.username = "admin";
           connectValue.password = "docker";
           connectValue.port = 27017;
           connectValue.jsonStringDebug = true;
           connectValue.timeZone = "Europe/Berlin";
           connectValue.logStreamDebug = true;
           valueToPrettyString@StringUtils(connectValue)(s);
           connect@MongoDB(connectValue)()  
    }

    println@Console("Server is up and running...")()
}


main {
    [ createTreatment( request )( response ) {   
        
        q.collection = "treatments";
        with (q.document){
            .name = request.name;
            .id = request.id;
            .price = request.price;
            .minduration = request.minduration;
            .maxduration = request.maxduration
        };
        insert@MongoDB(q)()
        response.msg << q.document

    }]


    [ getTreatment( request )( response ) {
        q.collection = "treatments";
        q.filter = "{ id: '$id'}";
        q.filter.id = request.treatmentId;
        query@MongoDB(q)(result);

        response.id = int(result.document.id)
        response.name = result.document.name
        response.price = int(result.document.price)
        response.minduration = int(result.document.minduration)
        response.maxduration = int(result.document.maxduration)
        
    }]


    [ getTreatments( request )( response ) {
        for( i = 1, i < 21, i++ ){
            q.collection = "treatments";
            q.filter = "{ id: '$id'}";
            q.filter.id = i;
            query@MongoDB(q)(result);
            if (result.document.id == i) {
                response.treatments += result.document.name +
                " - ID: " + result.document.id + 
                ". Price: " + result.document.price + 
                " Euro. Minimum Duration: " + result.document.minduration + 
                " hour(s). Maximum Duration: " + result.document.maxduration + " hour(s). ------------------ "
            }   
        }     
    }]

}