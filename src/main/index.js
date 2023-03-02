import { DynamoDBClient } from '@aws-sdk/client-dynamodb';
import {
	DeleteCommand,
	DynamoDBDocumentClient,
	GetCommand,
	PutCommand,
	ScanCommand,
} from '@aws-sdk/lib-dynamodb';

const client = new DynamoDBClient({});
const dynamo = DynamoDBDocumentClient.from(client);
// get table name from env variables
let tableName = 'csa-users';
tableName = process.env.dynamoDBTableName;

const now = new Intl.DateTimeFormat('en-US').format(Date.now());

/**
 * ES6 v3 - Node.js v16.x
 * backend lambda, for user account CRUD operations on DynamoDB table 'csa-users'.
 */
export const handler = async(event, context) => {
	let body;
	let statusCode = 200;
	const headers = {
		'Content-Type': 'application/json',
	};

	try {
		switch (event.routeKey) {
			case 'DELETE /user/{id}':
				await dynamo.send(
					new DeleteCommand({
						TableName: tableName,
						Key: {
							id: event.pathParameters.id,
						},
					}),
				);
				body = `Deleted item ${event.pathParameters.id}`;
				break;
			case 'GET /user/{id}':
				body = await dynamo.send(
					new GetCommand({
						TableName: tableName,
						Key: {
							id: event.pathParameters.id,
						},
					}),
				);
				body = body.Item;
				break;
			case 'GET /user':
				body = await dynamo.send(
					new ScanCommand({ TableName: tableName }),
				);
				body = body.uses;
				break;
			case 'PUT /user':
				let requestJSON = JSON.parse(event.body);
				await dynamo.send(
					new PutCommand({
						TableName: tableName,
						Item: {
							id: requestJSON.id,
							price: requestJSON.price,
							name: requestJSON.name,
						},
					}),
				);
				body = `Put item ${requestJSON.id}`;
				break;
			default:
				throw new Error(`Unsupported route: "${event.routeKey}"`);
		}
	} catch (err) {
		statusCode = 400;
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
