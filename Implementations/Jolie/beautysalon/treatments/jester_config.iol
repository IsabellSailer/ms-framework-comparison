type GetTreatmentRequest:void {
  .treatmentId[1,1]:int
}

type Treatment:void {
  .maxduration[1,1]:int
  .price[1,1]:int
  .name[1,1]:string
  .id[1,1]:int
  .minduration[1,1]:int
}

type GetTreatmentsRequest:void

type GetTreatmentsResponse:void {
  .treatments[0,*]:undefined
}

type CreateTreatmentResponse:void {
  .msg[0,*]:undefined
}

interface TREATMENTSInterface {
RequestResponse:
  getTreatment( GetTreatmentRequest )( Treatment ),
  getTreatments( GetTreatmentsRequest )( GetTreatmentsResponse ),
  createTreatment( Treatment )( CreateTreatmentResponse )
}



outputPort TREATMENTS {
  Protocol:http
  Location:"local"
  Interfaces:TREATMENTSInterface
}


embedded { Jolie: "treatment.ol" in TREATMENTS }
