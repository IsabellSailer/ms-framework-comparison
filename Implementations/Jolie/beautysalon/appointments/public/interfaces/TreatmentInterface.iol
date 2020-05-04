type Treatment: void {
    .name: string
    .id: int
    .price: int
    .minduration: int
    .maxduration: int
}

type GetTreatmentRequest: void {
    .treatmentId: int
}

interface TreatmentInterface {
  RequestResponse:
    getTreatment ( GetTreatmentRequest )( Treatment ),
}