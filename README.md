# Social Networking Serverless Kata solution (POC) #

Serverless-based Social Setworking Application satisfying [these requirements](https://github.com/petecocoon/Social-Networking-Serverless-Kata).

## What's Here ##

This repository includes:

* README.md - this file
* buildspec.yml - this file is used by ```AWS CodeBuild``` to build the web
  service
* pom.xml - this file is the Maven Project Object Model for the web service
* src - this directory contains your Java source files
* template.yml - this file contains the *AWS Serverless Application Model (AWS SAM)* used
  by ```AWS CloudFormation``` to deploy your application to AWS Lambda and Amazon API
  Gateway.
* template-configuration.json - this file contains the project ARN with placeholders used for tagging resources with the project ID
* /docs folder - this directory contains the images included in this *readem.MD* file and any other useful document
* /test-requests folder - this directory contains the HTTP requests in the shape of *Postman* collection that can be used to test the documentation

## Application Build & Deploy ##

Any push on the *master* branch of this repository will automatically trigger the ```AWS CodePipeline``` [CI/CD pipeline](https://github.com/Hiskrtapps/Social-Networking-Serverless-Kata/blob/master/docs/pipeline.PNG?raw=true) that will automatically:
 * download the latest version of the software from the master branch of this repository
 * build the software of which the ```AWS Lambda``` logic is written
 * deploy all the resources as described in the *template.yaml*


## Application Overview ## 

The application is implementing the scenarios described in the [requirements](https://github.com/petecocoon/Social-Networking-Serverless-Kata).

### Public APIs ###

#### Application APIs ####
The application exposes 2 REST APIs:
 * POST https://q5un72u80j.execute-api.us-west-1.amazonaws.com/Prod/messages
     * request:
         * headers:
           * ```Content-Type```: application/json
           * ```Authorization```: *id-token*
         * body:
            ```
            {
               "message": "this is the message I am posting" 
            }
            ```
     * response (example)
         * body:
           ```
           {
               "createdAt": "2021-01-18T12:55:07.837117",
               "id": "3cc01f97-642b-4560-bfd5-388a1aea9c77",
               "message": "this is the message I am posting",
               "userId": "giampaolo.grieco+user2@gmail.com"
           }
           ```
     > **_AUTHORIZATION:_** the ```Authorization``` header value is retrieved by a login call to Cognito login endpoint (see later)
     
     > **_USER INFORMATION:_** the *userId* is not passed as an input but it is retained from the *id-token* in the ```Authorization``` header
----
 * GET https://q5un72u80j.execute-api.us-west-1.amazonaws.com/Prod/messages
     * request:
         * headers:
             * ```Content-Type```: application/json
             * ```Authorization```: *id-token*
             * ```x-snsk-page-Limit```: *number*
             * ```x-snsk-pagination-LastEvaluatedKey```: *string*
     * response *(example)*:
         * headers:
             * ```x-snsk-pagination-LastEvaluatedKey```: *string*
         * body (example):
             ```
             [
               {
                   "createdAt": "2021-01-18T12:55:07.837117",
                   "id": "3cc01f97-642b-4560-bfd5-388a1aea9c77",
                   "message": "this is the message I am posting",
                   "userId": "giampaolo.grieco+user2@gmail.com"
               },
               {
                   "createdAt": "2021-01-18T12:55:07.837117",
                   "id": "3cc01f97-642b-4560-bfd5-388a1aea9c77",
                   "message": "this is the message I am posting",
                   "userId": "giampaolo.grieco+user2@gmail.com"
               },
               {
                   "createdAt": "2021-01-18T12:55:07.837117",
                   "id": "3cc01f97-642b-4560-bfd5-388a1aea9c77",
                   "message": "this is the message I am posting",
                   "userId": "giampaolo.grieco+user2@gmail.com"
               }
             ]
             ```
     > **_AUTHORIZATION:_** the ```Authorization``` header value is retrieved by a login call to Cognito login endpoint (see later)
     
     > **_PAGINATION:_** to be scalable this endpoint offers pagination capabilities. They are controlled by the *optional* headers ```x-snsk-page-Limit``` and ```x-snsk-pagination-LastEvaluatedKey```.
     * ```x-snsk-page-Limit```: it is the maximum number of values per page that will be returned (the last page will containing only the remainng elements);
       * if this header value is not passed the default value is used (10)
       * if the value 0 is passed the pagination will be automatically disabled and all the elements are returned (DANGER!)
     * ```x-snsk-pagination-LastEvaluatedKey```: it is the key of the last element returned in a previous endpoint call in which the pagination was enabled
       * if this header is not passed the selection start form the first element (the more recently inserted)
       * if the value from a previous call is passed the selection start form the next element starting from the one referenced by the key
----
#### Cognito APIs ####
In addition to the *Application APIs*, the following Cognito API should be call to perform the login and retrieve the needed *id_token*:
 * POST https://cognito-idp.us-west-1.amazonaws.com/
     * request:
         * headers:
           * ```Content-Type```: application/x-amz-json-1.1
           * ```x-amz-target```: AWSCognitoIdentityProviderService.InitiateAuth
         * body:
            ```
            {
               "AuthParameters" : {
                  "USERNAME" : *string*,
                  "PASSWORD" : *string*
               },
               "AuthFlow" : "USER_PASSWORD_AUTH",
               "ClientId" : "5di40vkm51np6oea341c9emag3"
            }
            ```
     * response
         * body:
           ```
           {
              {
                  "AuthenticationResult": {
                      "AccessToken": *string*,
                      "ExpiresIn": 3600,
                      "IdToken": *string*,
                      "RefreshToken": *string*,
                      "TokenType": "Bearer"
                  },
                  "ChallengeParameters": {}
           ```
     > **_TEST REQUESTS:_** you can download the following [Postman Collection](https://raw.githubusercontent.com/Hiskrtapps/Social-Networking-Serverless-Kata/master/test-requests/SNSK.postman_collection.json) already containing the definition of the 3 request described above. It will be sufficient *send* the *InitiateAuth* request in the collection to execute the login; Any subsequent *GetMessages*/*PostMessage* request *send* will result to be automatically authorized.
     
     > **_TEST USERS:_** to perform the login call to Cognito login endpoint you can use one of the following test users already signed up in the User Pool:
     * "USERNAME" : *giampaolo.grieco+user1@gmail.com*; "PASSWORD" : *definitive*
     * "USERNAME" : *giampaolo.grieco+user2@gmail.com*; "PASSWORD" : *definitive*
     * "USERNAME" : *giampaolo.grieco+user3@gmail.com*; "PASSWORD" : *definitive*

----

### Internal Architecture ###
Following the diagram describing the solution architecture.
![alt text](https://github.com/Hiskrtapps/Social-Networking-Serverless-Kata/blob/master/docs/SNSK%20Cloud%20Architecture.jpg?raw=true)

#### Services&Flows Description ####
1. Authentication call to ```AWS Cognito```
2. REST call to endpoint exposed by ```AWS API Gateway```
3. The proper ```AWS Lambda Function``` (*PostComment* or *GetComments*) is invoked
4. Querying/Storing data in ```AWS DynamoDB```. The result is returned to the caller.  
   **Asynchronously**, in case a new record is inserted a ```DynamoDB Stream``` is generated.
5. **Asynchronously** the ```AWS Lambda Function``` responsible to handle messages backup (*BackupComments*) is invoked.
6. The *BackupComments* ```AWS Lambda Function``` business logic _has not been implemented!_. It represent an **Architectural Plugin Point** in which it will be possible in the future to trigger other different service (for example to store the information of all inserted messages for _analitycs' purposes_).
