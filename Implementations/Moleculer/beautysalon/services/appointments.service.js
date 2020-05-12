"use strict";

//const DbMixin = require("../mixins/db.mixin");
const DbService = require("moleculer-db");
const SqlAdapter = require("moleculer-db-adapter-sequelize");
const Sequelize = require("sequelize");
const { MoleculerError } = require("moleculer").Errors;

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
	adapter: new SqlAdapter('appointments', 'postgres', 'docker', {
		host: 'localhost',
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
		 * Increase the quantity of the product item.
		 */
		createAppointment: {
			rest: "POST /",
			/*params: {
				id: "string",
				value: "number|integer|positive"
			},*/
			async handler(ctx) {
				console.log("called create");
				let entity = ctx.params;
				await this.validateEntity(entity);

				if (entity.endTime <= entity.startTime) {
					throw new MoleculerError("Conflict", 409, "CONFLICT", { description: "Start time has to be before end time" });
				}
				if (entity.startTime < 8 || entity.endTime < 9 || entity.startTime > 18 || entity.endTime > 18) {
					throw new MoleculerError("Conflict", 409, "CONFLICT", { description: "Appointments are only available from 8 - 18. Please choose another timeslot." });
				}

				entity.duration = entity.endTime - entity.startTime;

				// check for referenced treatment
				let treatment;
				try {
					treatment = await ctx.call("treatments.findTreatmentById", {id: entity.treatmentId.toString()});
					entity.treatmentName = treatment.name;
				} catch (e) {
				    throw new MoleculerError("Conflict", 409, "CONFLICT", { description: "No treatment found for id: " + entity.treatmentId });
				} 

				if (entity.duration < treatment.minduration) {
				    throw new MoleculerError("Conflict", 409, "CONFLICT", { description: "An appointment for " + treatment.name + " takes at least "
					+ treatment.minduration + " hour(s). Please choose another timeslot."});
				}
				if (entity.duration > treatment.maxduration) {
					throw new MoleculerError("Conflict", 409, "CONFLICT", { description: "An appointment for " + treatment.name + " takes maximum "
					+ treatment.maxduration + " hour(s). Please choose another timeslot."});
				}

				let params = {
					limit: 0,
					offset: 0,
					sort: ["id"],
					populate: ["id", "startTime", "customerName"],
					query: {}
				};
				let bookedAppointments = await this.adapter.find(params).map(this.adapter.entityToObject);

				let conflicts = 0;
				for (let booked of bookedAppointments) {
					if (booked.treatmentId == entity.treatmentId && booked.date == entity.date) {
						if (!(entity.startTime <= booked.startTime && entity.endTime <= booked.endTime) && !(booked.endTime <= entity.endTime && booked.endTime <= entity.startTime)) {
							conflicts++;
						}
					}
				}

				if (conflicts > 0) {
					throw new MoleculerError("Conflict", 409, "CONFLICT", { description: "There were " + conflicts
					+ " conflicts with other appointments. Please choose a free timeslot."});
				} else {

					const doc = await this.adapter.insert(entity);
					//console.log(doc);
                	let json = this.adapter.entityToObject(doc);
					return json;
				}
			}
		},

		/**
		 * Decrease the quantity of the product item.
		
		decreaseQuantity: {
			rest: "PUT /:id/quantity/decrease",
			params: {
				id: "string",
				value: "number|integer|positive"
			},
			 @param {Context} ctx 
			async handler(ctx) {
				const doc = await this.adapter.updateById(ctx.params.id, { $inc: { quantity: -ctx.params.value } });
				const json = await this.transformDocuments(ctx, ctx.params, doc);
				await this.entityChanged("updated", json, ctx);

				return json;
			}
		}
		*/
	},

	/**
	 * Methods
	 */
	methods: {
		/**
		 * Loading sample data to the collection.
		 * It is called in the DB.mixin after the database
		 * connection establishing & the collection is empty.
		 
		async seedDB() {
			await this.adapter.insertMany([
				{ name: "Samsung Galaxy S10 Plus", quantity: 10, price: 704 },
				{ name: "iPhone 11 Pro", quantity: 25, price: 999 },
				{ name: "Huawei P30 Pro", quantity: 15, price: 679 },
			]);
		}
		*/
	},

	/**
	 * Fired after database connection establishing.
	 */
	async afterConnected() {
		// await this.adapter.collection.createIndex({ name: 1 });
	}
};
