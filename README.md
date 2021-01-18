Social Networking Serverless Kata (POC)
==============================================

Serverless-based Social Setworking Spplication satisfying the requirements expressed here:
https://github.com/petecocoon/Social-Networking-Serverless-Kata

What's Here
-----------

This sample includes:

* README.md - this file
* buildspec.yml - this file is used by AWS CodeBuild to build the web
  service
* pom.xml - this file is the Maven Project Object Model for the web service
* src/main - this directory contains your Java service source files
* src/test - this directory contains your Java service unit test files
* template.yml - this file contains the AWS Serverless Application Model (AWS SAM) used
  by AWS CloudFormation to deploy your application to AWS Lambda and Amazon API
  Gateway.
* template-configuration.json - this file contains the project ARN with placeholders used for tagging resources with the project ID

Application Build & Deploy
------------------

Any push on the master branch of this repository will trigger the CI/CD pipeline that will automatically:
 * download the latest version of the software from the master branch of this repository
 * build the software of which the AWS Lambda logic is written
 * deploy all the resources as described in the template.yaml


Application Overview
------------------

The application is implementing the Scenarios described in the [requirements](https://github.com/petecocoon/Social-Networking-Serverless-Kata).

It exposes 2 REST APIs (through the AWS API Gateway service) that permits a user to post a new message and to read all the messages of all the users.

AWS Cognito is used to authorize the 2 endpoints so that it is possible to use the application functionalities only after a signup in the created ser Pool.

The api redirects the calls to 2 AWS Lambdas written in java (one to post a message and one to read the messages).

The java logic implement the storage of the messages into a DynamoDB table.

Once a message is stored in the table a new DynamoDB stream is activated and it triggers a third AWS Lambda. This Lambda represent a plugin point in which it will be possible in the future to trigger other different service (for example to store the information of all inserted messages for analitycs' purposes).

