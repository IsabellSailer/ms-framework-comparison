"use strict";

const DbService = require("moleculer-db");
const SqlAdapter = require("moleculer-db-adapter-sequelize");
const Sequelize = require("sequelize");
const { MoleculerClientError } = require("moleculer").Errors;

/**
 * @typedef {import('moleculer').Context} Context Moleculer's Context
 */

module.exports = {
	name: "appointments",
	// version: 1

	/**
	 * Mixins
	 */
	mixins: [DbService],
	adapter: new SqlAdapter('appointments', process.env.POSTGRES_USER, process.env.POSTGRES_PASSWORD, {
		host: process.env.POSTGRES_HOST,
		port: 5432,
		dialect: 'postgres' /* one of 'mysql' | 'mariadb' | 'postgres' | 'mssql' */,
	
		pool: {
			max: 5,
			min: 0,
			idle: 10000
		},
	}),
	model: {
        name: "appointments",
        define: {
            id: { type: Sequelize.INTEGER, primaryKey: true },
			customerName: Sequelize.TEXT,
			date: Sequelize.TEXT,
			startTime: Sequelize.INTEGER,
			endTime: Sequelize.INTEGER,
			duration: Sequelize.INTEGER,
			treatmentName: Sequelize.TEXT,
			treatmentId: Sequelize.INTEGER
		},
        options: {
            // Options from http://docs.sequelizejs.com/manual/tutorial/models-definition.html
        }
    },

	/**
	 * Settings
	 */
	settings: {
		// Available fields in the responses
		fields: [
			"id",
			"customerName",
			"date",
			"startTime",
			"endTime",
			"duration",
			"treatmentName",
			"treatmentId"
		],

		// Validator for the `create` & `insert` actions.
		entityValidator: {
			id: "number",
			customerName: "string",
			date: "string",
			startTime: "number|min:8|max:18",
			endTime: "number|min:8|max:18",
			/* duration: "number|positive",*/
			/*treatmentName: "string",*/
			treatmentId: "number"
		}
	},

	/**
	 * Action Hooks
	 */
	hooks: {
		before: {
			/**
			 * Register a before hook for the `create` action.
			 * It sets a default value for the quantity field.
			 *
			 * @param {Context} ctx
			 
			create(ctx) {
				ctx.params.quantity = 0;
			}
			*/
		}
	},

	/**
	 * Actions
	 */
	actions: {
		/**
		 * The "moleculer-db" mixin registers the following actions:
		 *  - list
		 *  - find
		 *  - count
		 *  - create
		 *  - insert
		 *  - update
		 *  - remove
		 */

		 /**
		 * disable actions from "moleculer-db" mixin
		 */
		list: false,
		get: false,
		update: false,
		create: false,
		remove: false,


		// --- ADDITIONAL ACTIONS ---

		/**
 		 * Get all appointments 
 		 */
		getAllAppointments: {
			rest: "GET /",
			async handler(ctx) {
				let params = {
					limit: 0,
					offset: 0,
					sort: ["id"],
					query: {}
				};
				const allAppointments = await this.adapter.find(params).map(this.adapter.entityToObject);
				return allAppointments;
			}
		},

		/**
		 * Gets an appointment by id
		 */
		getAppointmentById: {
			rest: "GET /:id",
			params: {
				id: { type: "string" }
			},
			async handler(ctx) {
				let searchId = parseInt(ctx.params.id, 10);
				if (isNaN(searchId))  {
					throw new MoleculerClientError("Not found", 404, "NOT_FOUND", { description: ctx.params.id + " is not valid id" });
				}
				let params = {
					limit: 0,
					offset: 0,
					sort: ["id"],
					query: {id: searchId}
				};
				const appointment = await this.adapter.find(params).map(this.adapter.entityToObject);;
				if (appointment.length > 0) {
					return appointment[0];
				} else {
					throw new MoleculerClientError("Not found", 404, "NOT_FOUND", { id: searchId });
				}
			}
		},

		/**
		 * Create an appointment
		 */
		createAppointment: {
			rest: "POST /",
			/*params: {
				id: "string",
				value: "number|integer|positive"
			},*/
			async handler(ctx) {
				let entity = ctx.params;
				await this.validateEntity(entity);

				if (entity.endTime <= entity.startTime) {
					throw new MoleculerClientError("Conflict", 409, "CONFLICT", { description: "Start time has to be before end time" });
				}
				if (entity.startTime < 8 || entity.endTime < 9 || entity.startTime > 18 || entity.endTime > 18) {
					throw new MoleculerClientError("Conflict", 409, "CONFLICT", { description: "Appointments are only available from 8 - 18. Please choose another timeslot." });
				}

				entity.duration = entity.endTime - entity.startTime;

				// check for referenced treatment
				let treatment;
				try {
					treatment = await ctx.call("treatments.findTreatmentById", {id: entity.treatmentId.toString()});
					entity.treatmentName = treatment.name;
				} catch (e) {
				    throw new MoleculerClientError("Conflict", 409, "CONFLICT", { description: "No treatment found for id: " + entity.treatmentId });
				} 

				if (entity.duration < treatment.minduration) {
				    throw new MoleculerClientError("Conflict", 409, "CONFLICT", { description: "An appointment for " + treatment.name + " takes at least "
					+ treatment.minduration + " hour(s). Please choose another timeslot."});
				}
				if (entity.duration > treatment.maxduration) {
					throw new MoleculerClientError("Conflict", 409, "CONFLICT", { description: "An appointment for " + treatment.name + " takes maximum "
					+ treatment.maxduration + " hour(s). Please choose another timeslot."});
				}

				let params = {
					limit: 0,
					offset: 0,
					sort: ["id"],
					query: {}
				};
				let bookedAppointments = await this.adapter.find(params).map(this.adapter.entityToObject);

				let conflicts = 0;
				for (let booked of bookedAppointments) {
					if (booked.treatmentId === entity.treatmentId && booked.date === entity.date) {
						if (!(entity.startTime < booked.startTime && entity.endTime < booked.endTime) && !(booked.endTime < entity.endTime && booked.endTime <= entity.startTime)) {
							conflicts++;
						}
					}
				}

				if (conflicts > 0) {
					throw new MoleculerClientError("Conflict", 409, "CONFLICT", { description: "There were " + conflicts
					+ " conflicts with other appointments. Please choose a free timeslot."});
				}
				let doc;
				try {
				   doc = await this.adapter.insert(entity);
				} catch (e) {
					throw new MoleculerClientError("Conflict", 409, "CONFLICT", { description: "Could not save appointment"});
				}
				let jsonAppointment = this.adapter.entityToObject(doc);
				this.logger.info("Created appointment: ", jsonAppointment);

				//TODO notify confirmation service
				ctx.emit("appointment.created", jsonAppointment, ["confirmation"])


				return jsonAppointment;
			}
		},
	
	},

	/**
	 * Methods
	 */
	methods: {

	},

	/**
	 * Fired after database connection establishing.
	 */
	async afterConnected() {
		// await this.adapter.collection.createIndex({ name: 1 });
	}
};
