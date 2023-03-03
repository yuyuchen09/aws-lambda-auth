# REST API for login and account creation

AWS services used,
- AWS API Gateway for REST API
- authentication with lambda Custom Authorizer
- account creation with backend lambda function
- AWS DynamoDB for persistent storage

## Repo
https://github.com/yuyuchen09/aws-lambda-auth

## System Elements

![aws_api_lambda_auth.png](aws_api_lambda_auth.png) 

## Java
Not available in the Lambda console. Use the AuthPolicy object to generate and serialize IAM policies for your custom authorizer. See javadoc comments for more details.

## Lambda Custom Authorizer

## DynamoDB for persistent storage
User info is persistent into DynamoDB, table 'csa-users'.


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
- In transit, Lambda API is only supported on HTTPS according to AWS Regions and Endpoints documentation.
For Lambda Proxy integration, all requests are proxied "as is" to the endpoint Lambda.
- At rest, All user data stored in Amazon DynamoDB is fully encrypted at rest by default using AWS KMS.
See in test event, header "X-Forwarded-Port": "443"
- Spring security for password encryption, <a href= https://docs.spring.io/spring-security/site/docs/5.0.0.RELEASE/api/>BCryptPasswordEncoder</a>

## TODOs
- Doc with an overall LucidChart diagram
- ~~CRUD support: POST, GET, and DELETE~~
- ~~Secure password: BCrypt encrypted pswd~~
- ~~UserItem VO: to and from JSON object~~
- ~~CICD CloudFormation draft~~
- ~~Java to Kotlin conversion~~
- ~~Testing~~:
  1. Unit tests
  2. Local docker lambda end-to-end testing: Using AWT Toolkit
  3. Test from Lambda console
  4. Test from API Gateway
  5. Postman collection
- AuthPolicy based on principalId
- JWTUtil, handle token properly

## Docs and Refs ##
For more details, see public documentation for:
- [Use API Gateway Lambda authorizers](https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-use-lambda-authorizer.html)
  [Blog Post](https://aws.amazon.com/blogs/compute/introducing-custom-authorizers-in-amazon-api-gateway/) -- [Developer Guide](http://docs.aws.amazon.com/apigateway/latest/developerguide/use-custom-authorizer.html)
- https://jwt.io/
- [Tutorial: Build a CRUD API with Lambda and DynamoDB](https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-dynamo-db.html#http-api-dynamo-db-create-routes)
  