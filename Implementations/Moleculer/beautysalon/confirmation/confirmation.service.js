"use strict";
/**
 * @typedef {import('moleculer').Context} Context Moleculer's Context
 */

module.exports = {
	name: "confirmation",

	/**
	 * Settings
	 */
	settings: {

	},

	/**
	 * Dependencies
	 */
    dependencies: [],

	/**
	 * Actions
	 */
	actions: {

	},

	/**
	 * Events
	 */
	events: {
		"appointment.created": {
			handler(ctx) {
				this.logger.info("Thank you " + ctx.params.customerName + "! Your Appointment for " + ctx.params.treatmentName + " was confirmed. It will be on " + ctx.params.date + " from " + ctx.params.startTime  + ":00 to " + ctx.params.endTime  +":00.");
			}
		}
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
