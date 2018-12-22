# Simple User CRUD using Scala Lagom Framework

Sample application performing CRUD operation using Lagom Framework. Application uses Elasticsearch as a Database.

*NOTE: This application does not use Event Sourcing or CQRS mechanism.*

Below are list of tools and techs used:

* scala - 2.12.4
* sbt - 1.2.1
* Lagom - 1.4.9
* Elasticsearch - 6.4.3


### Prerequisites
* docker
* sbt


### Running the application
Jump to the project root directory and execute below commands:
```
docker-compose up -d elasticseach
```
Above command will start elasticsearch docker container. You can check the elasticsearch status hitting: `http://localhost:9200`

And then run the application using:
```
sbt runAll
```
This will start the application on port **9000**. So if you want to check any of the available GET request, then you can hit: `http://localhost:9000/API_URL` from your browser.

### Available APIs in this application:

#### Create
- **url:** POST - */api/user/create*
- **Headers:** `Content-Type: application/json`
- **RequestBody**:

```
{
     "firstName": "Jane",
     "lastName": "Doe",
     "email": "janedoe@hotmail.com"
}
```

- **Response:**
```
{
    "id": "e3b25351-0dd0-4679-90d6-fce9148b631b"
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "janedoe@hotmail.com"
}
```

- **Error Codes:**
    - returns HTTP **400 - Bad Request** if a user with email is already present


#### Get
- **url:** GET */api/users/{userId}*
- **Headers:** `Content-Type: application/json`
- **RequestBody**: NA

- **Response:**
```
{
    "id": "e3b25351-0dd0-4679-90d6-fce9148b631b"
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "janedoe@hotmail.com"
}
```
- **Error Codes:**
    - returns HTTP **404 - Not Found** if a user for given ID does not exists

#### Update
- **url:** POST */api/user/{userId}/update*
- **Headers:** `Content-Type: application/json`
- **RequestBody**:
```
{
    "id": "e3b25351-0dd0-4679-90d6-fce9148b631b"
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "janedoe@hotmail.com"
}
```

- **Response:**
```
{
    "id": "e3b25351-0dd0-4679-90d6-fce9148b631b"
    "firstName": "Jane",
    "lastName": "Doe",
    "email": "janedoe@hotmail.com"
}
```
- **Error Codes:**
    - returns HTTP **404 - Not Found** if a user for given ID does not exists

#### DELETE
- **url:** GET */api/user/{userId}/delete*
- **Headers:** `Content-Type: application/json`
- **RequestBody**: NA

- **Response:** HTTP **200 OK**
- **Error Codes:**
    - returns HTTP **404 - Not Found** if a user for given ID does not exists

#### Search
- **url:** POST */api/users?pageNumber=???&pageSize=???*
- **Headers:** `Content-Type: application/json`
- **RequestBody**:
```
{
    "keyword": "jane"
}
```

- **Response:**
```
{
    "result" : [
        {
            "id": "e3b25351-0dd0-4679-90d6-fce9148b631b"
            "firstName": "Jane",
            "lastName": "Doe",
            "email": "janedoe@hotmail.com"
         }
    ],
    "pageSize": ???,
    "pageNumber": ???,
    "totalRecords": ???
}
```

#### References:
* https://www.lagomframework.com
* https://www.playframework.com
* https://stackoverflow.com/questions/tagged/lagom
* https://docs.microsoft.com/en-us/previous-versions/msp-n-p/jj554200(v=pandp.10)


#### Contributors:
**Tarang Bhalodia**

[LinkedIn](https://www.linkedin.com/in/tarangbhalodia/)

[Stack Overflow](https://stackoverflow.com/users/6335075/tarang-bhalodia?tab=profile)
