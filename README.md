# Ginger [![Build Status](https://travis-ci.org/kongchen/ginger.png)](https://travis-ci.org/kongchen/ginger)

> Enrich your generated **API document with samples**, to describe your API more precise, vivid and easy to be understood.




## Tutorials

#### I. Swagger your document
The precondition for using Ginger is that you have followed [Swagger's Tutorials](https://github.com/wordnik/swagger-core/wiki/java-jax-rs) and made your service works happily with Swagger -- At least pass the [Test](https://github.com/wordnik/swagger-core/wiki/java-jax-rs#testing) in the tutorial.

Thus, start your service and you can hit the swagger resource declaration url:
```
http://www.example.com:8080/your/api/path/api-docs.json
```
This is your ```swaggerBaseURL```, keep it in mind.


#### II. Prepare your Sample Package

A **sample package** is a directory in which there're **several** ```Request File```s and **one** ```Sequence File```. For example:
> The sample package `/foo/samples/` has 3 request file and 1 sequence file.
```bash
root@/foo/samples/> ls
gettoken.req    post.req    postGroup.req    sample.seq
```



##### 1. Request File
```Request File``` is a UTF-8 encoded text file which describes your HTTP request intuitively, one request file should only represent one request.

Here's an example:

> A **POST** request with ```Accept``` and ```Content-Type``` headers, as well as a JSON body and goes to ```/shopping-cart```
```
POST /shopping-cart
> Accept: application/json
> Content-Type: application/json
{ "items": [
  {
    "url": "/shopping-cart/1",
    "product":"2ZY48XPZ",
    "quantity": 1,
    "name": "New socks",
    "price": 1.25
  } ]
}
```

****
For more details and usages of the `Request File`, please see the [wiki](https://github.com/kongchen/ginger/wiki/Request-File)
****

##### 2. Sequence File

```Sequence File``` is a UTF-8 encoded text file used to control the flow of requests we descibed in ```Request File```s. 

Its name is a constant called ***sample.seq***

Here'e an example:
```java
// add some itmes in the shopping cart
items << PostItems.req

// get the item which id is the first one in the previous items 
selectedItemA << GetItem.req $items[0].id

selectedItemB == GetItem.req $items[1].id

// update the item's count to 0
updateItem << PutItem.req $selectedItemA:ETag $selectedItemA.id 0

//delete the item
deleteItem << DeleteItem.req $updateItem:ETag $updateItem.id

```
****
For more details and usages of the `Sequence File`, please see the [wiki](https://github.com/kongchen/ginger/wiki/Sequence-File)
****

#### III. Launch *Ginger*

`Ginger` is an executable all-in-one jar package `ginger-0.1-jar-with-dependencies.jar`, there're 2 ways to get it:

1. Download the source and launch `mvn package`
2. Download the binary directly from [Bintray](http://dl.bintray.com/kongchen/generic/ginger-0.1-jar-with-dependencies.jar?direct)

Assume you've done the above steps:

1. Your `swaggerBaseURL` is `http://www.example.com:8080/api/api-docs.json` ,
2. You've prepared your `Request File`s and `Sequence File` in a sample package located at `/foo/samples/`

Now, prepare a json configuration file for *Ginger*:
```json
{
   "swaggerBaseURL":"http://www.example.com:8080/api/api-docs.json",
   "apiBasePath": "/api",
   "samplePackage":"/foo/samples",
   "outputTemplatePath":"https://raw.github.com/kongchen/api-doc-template/master/v1.1/markdown.mustache",
   "outputPath":"doc.md",
   "withFormatSuffix":"false"
}

```
1. `swaggerBaseURL` must be end with `api-docs.json`, as defined in Swagger.
2. `outputTemplatePath` is the document output template's URI, see more in [api-doc-template](https://github.com/kongchen/api-doc-template)
3. `apiBasePath` is the api's path based on the server's root endpoint. This configuration is *very important* for populating the samples in output document.
4. `outputPath` is the final document's ouput path.
5. `withFormatSuffix` indicates if you wannt swagger's `.{format}` suffix in your document.

Assume the configuration file is `/bar/test.json`, now you can launch *Ginger*:
```
java -cp ginger-SNAPSHOT-jar-with-dependencies.jar /bar/test.json
```
to let *Ginger* help you:

- Generate API document, 
- Run samples you described in the `samplePackage`, 
- Automatically populate the samples' results in the document according to the output template.

Finally, check out the `/bar/doc.md` to see the final document when the launch finished successfully.
