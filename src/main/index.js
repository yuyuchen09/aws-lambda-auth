const AWS = require('aws-sdk');
const dynamo = new AWS.DynamoDB.DocumentClient();
const { v4: uuidv4 } = require('uuid');
const {
	PutCommand,
	DeleteCommand,
} = require('@aws-sdk/lib-dynamodb');
const http = require('http');

/**
 * Node.js 14.x
 * Backend lambda, HelloWorld-lambda-DDB
 * TODO  Have a list of things for enhancements
 * - sanitize password: encrypt SHA-256 password to DDB, sanitize password.
 *
 * Demonstrates a simple HTTP endpoint using API Gateway. You have full
 * access to the request and response payload, including headers and
 * status code.
 *
 * To scan a DynamoDB table, make a GET request with the TableName as a
 * query string parameter. To put, update, or delete an item, make a POST,
 * PUT, or DELETE request respectively, passing in the payload to the
 * DynamoDB API as a JSON body.
 */
exports.handler = async(event, context) => {
	console.log('Received event:', JSON.stringify(event, null, 2));
	// TODO get table name from env variables
	let ddb_tableName = 'csa-users';
	//ddb_tableName = process.env.dynamoDBTableName;

	let body;
	let statusCode = '200';
	const headers = {
		'Content-Type': 'application/json',
	};

	try {
		switch (event.httpMethod) {
			case 'DELETE /user/{id}':
				await dynamo.send(
					new DeleteCommand({
						TableName: ddb_tableName,
						Key: {
							id: event.pathParameters.id,
						},
					}),
				);
				body = `Deleted item ${event.pathParameters.id}`;
				break;
			case 'POST /user':
				// TODO sanitize password: encrypt SHA-256 password to DDB,
				let requestJSON = JSON.parse(event.body);
				await dynamo.send(
					new PutCommand({
						TableName: ddb_tableName,
						Item: {
							id: requestJSON.id + uuidv4(),
							name: requestJSON.name,
							email: requestJSON.email,
							password: requestJSON.password
						},
					}),
				);
				body = `Put item ${requestJSON.id}`;
				break;
			default:
				throw new Error(`Unsupported method "${event.httpMethod}"`);
		}
	} catch (err) {
		statusCode = '400';
		body = err.message;
	} finally {
		body = JSON.stringify(body);
	}

	return {
		statusCode,
		body,
		headers,
	};
};
