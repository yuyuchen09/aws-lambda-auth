# REST API for login and account creation

## Repo
https://github.com/yuyuchen09/aws-lambda-auth

## System Elements

![AWS API Auth with Lambda Authorizer.png](AWS%20API%20Auth%20with%20Lambda%20Authorizer.png)

## Build 
Java 11 & Maven 3.8
see POM.xml for details
$ mvn clean install -DskipTest
$ mvn test

Use the AuthPolicy object to generate IAM policies for your custom authorizer. 

## Lambda Custom Authorizer

## DynamoDB for persistent storage
User account info is persistent into DynamoDB (DDB), table 'csa-users'.

Here is a sample JSON view of a user item stored in DDB.

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
- In transit, Lambda API is only supported on HTTPS according to AWS Regions and Endpoints documentation. All of the APIs created with Amazon API Gateway expose HTTPS endpoints only. Amazon API Gateway does not support unencrypted (HTTP) endpoints.
  For Lambda Proxy integration, all requests are proxied "as is" to the endpoint Lambda. 
- At rest, All user data stored in Amazon DynamoDB is fully encrypted at rest by default using AWS KMS.
See in test event, header "X-Forwarded-Port": "443"
- Spring security for password encryption, <a href= https://docs.spring.io/spring-security/site/docs/5.0.0.RELEASE/api/>BCryptPasswordEncoder</a>

## TODOs
1. [x] ~~Doc with an overall LucidChart diagram~~
2. [x] ~~DynamoDB CRUD operations support: POST, GET, and DELETE~~
   * DDB table setup: partitiion/hash key: email
   * Global second index on field 'fullName': search by _fullName=dev_
3. [x] ~~Setup the pipeline:~~
   * Connect with Lambda custom authorizer
   * Proxy request to backend lambda DynamoDB CRUD function
   * new Stage 'demo'
4. [x] ~~Secure password: BCrypt encrypted pswd~~
5. [x] ~~UserItem VO: to and from JSON object~~
6. [x] ~~CICD CloudFormation draft only~~
7. [x] ~~Java to Kotlin conversion~~
8. [x] ~~Testings~~:
   * ~~Unit tests~~
   * ~~Local docker lambda end-to-end testing: with AWT Toolkit~~
   * ~~Test from AWS Lambda console~~
   * ~~Test from API Gateway test interface~~
   * Postman collection: ??
9. [ ] AuthPolicy based on principalId
10. [ ] JWTUtil, handle token properly

## Docs and Refs ##
For more details, see public documentation for:
- [Use API Gateway Lambda authorizers](https://docs.aws.amazon.com/apigateway/latest/developerguide/apigateway-use-lambda-authorizer.html)
  [Blog Post](https://aws.amazon.com/blogs/compute/introducing-custom-authorizers-in-amazon-api-gateway/) -- [Developer Guide](http://docs.aws.amazon.com/apigateway/latest/developerguide/use-custom-authorizer.html)
- https://jwt.io/
  