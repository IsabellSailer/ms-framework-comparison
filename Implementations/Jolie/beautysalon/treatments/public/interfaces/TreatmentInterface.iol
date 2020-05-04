type Treatment: void {
    .name: string //< Name of the Treatment
    .id: int //< ID of the Treatment
    .price: int //< Price
    .minduration: int //< Minimum Duration
    .maxduration: int //< Maximum Duration
}

/**! Requests one Treatment */
type GetTreatmentRequest: void {
    .treatmentId: int
}

/**! Requests all Treatments */
type GetTreatmentsRequest: void 

/**! Returns all Treatments */
type GetTreatmentsResponse: void{
   .treatments*: undefined
}

/**! Response for a newly created  Treatment */
type CreateTreatmentResponse: void{
   .msg*: undefined
}

/**! This Service is responsible for the management of treatments. */
interface TreatmentInterface {
  RequestResponse:
    /**! Returns all Information for the requested Treatment */
    getTreatment ( GetTreatmentRequest )( Treatment ),
    /**! Shows a list of all offered Treatments */
    getTreatments ( GetTreatmentsRequest )( GetTreatmentsResponse ),
    /**! Saves a new Treatment */
    createTreatment( Treatment )( CreateTreatmentResponse )
}