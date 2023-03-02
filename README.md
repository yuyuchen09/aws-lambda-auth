# REST API for login and account creation

AWS services used,
- AWS API Gateway for REST API
- authentication with lambda Custom Authorizer
- account creation with backend lambda function
- AWS DynamoDB for persistent storage

## Repo
https://github.com/yuyuchen09/aws-lambda-auth

## TODOs
- Doc with an overall Lucidchart diagram
- AuthPolicy based on principalId
- JWTUtil, handle token properly

## Java
Not available in the Lambda console. Use the AuthPolicy object to generate and serialize IAM policies for your custom authorizer. See javadoc comments for more details.

## Lambda Custom Authorizer
![img.png](img.png)


## DynamoDB for persistent storage
User info is stored in DynamoDB

![img_1.png](img_1.png)

Here is a sample JSON view of a user info.

`
{
{
"email": {
"S": "dev@gmail.com"
},
"fullName": {
"S": "dev"
},
"password": {
"S": "$2a$10$NAeW4GtjeQX4jiZeopUl/OqIWOgBctkcmBp/EVPa7BGQ8OwqmsIO6"
}
}
`
## Secure Password
TODO

## Docs and References ##
For more details, see public documentation for:
- [Use API Gateway Lambda authorizers](https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-use-lambda-authorizer.html)
  [Blog Post](https://aws.amazon.com/blogs/compute/introducing-custom-authorizers-in-amazon-api-gateway/) -- [Developer Guide](http://docs.aws.amazon.com/apigateway/latest/developerguide/use-custom-authorizer.html)
- https://jwt.io/
- [Tutorial: Build a CRUD API with Lambda and DynamoDB](https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-dynamo-db.html#http-api-dynamo-db-create-routes)
  