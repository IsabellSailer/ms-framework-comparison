"use strict";

//const { ServiceBroker } = require("moleculer");
const DbService = require("moleculer-db");
const MongoDBAdapter = require("moleculer-db-adapter-mongo");
const { MoleculerClientError } = require("moleculer").Errors;

/**
 * @typedef {import('moleculer').Context} Context Moleculer's Context
 */

module.exports = {
	name: "treatments",

	/**
	 * Settings
	 */
	settings: {
        fields: ["id", "name", "price", "minduration", "maxduration"],

        entityValidator: {
			id: { type: "number"},
            name: { type: "string", min: 1 },
            price: { type: "number", min: 0},
			minduration: { type: "number", min: 1 },
			maxduration: { type: "number", min: 1}
		}

	},

	/**
	 * Dependencies
	 */
    dependencies: [],
    
	mixins: [DbService],
	adapter: new MongoDBAdapter("mongodb://localhost:27017/beautysalon"),
	collection: "treatments",

	/**
	 * Actions
	 */
	actions: {

		/**
		 * disable actions from mongo mixin
		 */
		list: false,
		get: false,
		update: false,
		create: false,
		remove: false,

		/**
		 * Returns all treatments
		 *
		 * @returns
		 */
		getAllTreatments: {
			rest: {
				method: "GET",
				path: "/"
			},
			async handler(ctx) {
				let params = {
					limit: 0,
					offset: 0,
					sort: ["id"],
					query: {}
				};
				const result = await this.adapter.find(params);
				console.log(result);
				//let json = await this.transformDocuments(ctx, result);
				return result;
			}
		},

		findTreatmentById: {
			rest: {
				method: "GET",
				path: "/:id"
			},
			params: {
				id: { type: "string" }
			},
			async handler(ctx) {
				let searchId = parseInt(ctx.params.id, 10);
				if (isNaN(searchId)) {
					throw new MoleculerClientError("Not found", 404, "NOT_FOUND", { description: ctx.params.id + " is not valid id" });
				}
				let params = {
					limit: 0,
					offset: 0,
					sort: ["id"],
					query: {id: searchId}
				};
				const result = await this.adapter.find(params);
				if (result.length > 0) {
					return result[0];
				} else {
					throw new MoleculerClientError("Not found", 404, "NOT_FOUND", { id: searchId });
				}
			}
		},

		/**
		 * Create a new treatment from the treatment passed in the body
		 * 
		 */
		createTreatment: {
            rest: {
                method: "POST",
                path: "/"
            },
            /*params: {
				id: { type: "number"},
            	name: { type: "string" },
            	price: { type: "number"},
				minduration: { type: "number"},
				maxduration: { type: "number"}
            },*/
            async handler(ctx) {
				let entity = ctx.params;
                await this.validateEntity(entity);

				const doc = await this.adapter.insert(entity);
				this.logger.info("Created treatment", doc);
                return doc;
            }
        }
	},

	/**
	 * Events
	 */
	events: {

	},

	/**
	 * Methods
	 */
	methods: {

	},

	/**
	 * Service created lifecycle event handler
	 */
	created() {

	},

	/**
	 * Service started lifecycle event handler
	 */
	async started() {

	},

	/**
	 * Service stopped lifecycle event handler
	 */
	async stopped() {

	}
};
