# Swagger your API Document with **<font color="green">Examples</font>**

Swagger lets you write your API document near your source code to keep it up-to-date, and provides a pretty ui (swagger-ui) to display the document. The document shows the url, parameters, error codes and descriptions for each of your APIs, and you can even ```try it out!```

![](https://helloreverb.com/img/swagger-hero.png)

It's good enough for most cases.

However, if you wanna to put examples in your document to describe your API more precise, vivid and clear, it seems impossible for swagger.

# This tool makes this **<font color="darkgray">impossible</font>** **possible!**

## Tutorials

#### I. Swagger your document
The precondition for using this tool is that you have followed [Swagger's Tutorials](https://github.com/wordnik/swagger-core/wiki/java-jax-rs) and made your service worked happily with Swagger - Pass the [Test](https://github.com/wordnik/swagger-core/wiki/java-jax-rs#testing) in the tutorial.

Thus, you can hit the swagger resource declaration url:
```
http://www.example.com:8080/your/api/path/api-docs.json
```
We will name this URL as ```swaggerBaseURL```

#### II. Start your service
Obviously, it's a must otherwise you even cannot get Swagger documents.

The purpose of emphasize this is to introduce another url ```sampleBaseURL``` which your samples runs against:
```
http://www.example.com:8080/your/api/path
```

#### III. Prepare your Sample Package

```Sample Package``` is a directory in which there're **several** ```Request file```s and **one** ```Sequence File```. For example:
> It's a sample package with 3 request file and 1 sequence file.
```bash
root@/samplepackage/> ls
gettoken.req    post.req    postGroup.req    sample.seq
```



##### 1. Request File
```Request File``` is a UTF-8 encoded text file which describes your HTTP request intuitively, one request one file and its name is arbitrary.

Here's an example:

> A **```POST```** Request File with ```Accept``` and ```Content-Type``` headers, as well as a json body and calls to ```/shopping-cart```
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
For more details and usages of the `Request File`, please refer to the [wiki]()
****

##### 2. Sequence File

```Sequence File``` is a UTF-8 encoded text file with several lines, a line can be:

1. an empty line,
2. a comment line or
3. a command line. 

The command line controls the flow of requests we descibed in ```Request File```s. It's name is a constant called ***sample.seq***

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

For more details and usages of the `Sequence File`, please refer to the [wiki]()

#### IV. Launch the tool

If you have done the first 3 stpes, you can prepare a json configuration file:
```json
{
   "sampleBaseURL":"http://www.example.com:8080/your/api/path",
   "swaggerBaseURL":"http://www.example.com:8080/your/api/path/api-docs.json",
   "samplePackage":"samples",
   "outputTemplatePath":"template/strapdown.html.mustache",
   "outputPath":"target/doc.html"
}
```
1. `sampleBaseURL` and `swaggerBaseURL` are introduced in the step **I** and **II**, **Note** that the _path_ in the `Request File` is based on the `sampleBaseURL`
2. `samplePackage` is the path to your `Sample Package`;
3. `outputTemplatePath` is the document output template file's path.
4. `outputPath` is the final document's ouput path.

With this configuration file, you can launch this tool:
```

```
to let the tool helps you:

- generate API document, 
- run samples you described in the `samplePackage`, 
- automatically populate the samples' results in the document according to the output template.

Finally, check out the `outputPath` to see the final document when the launch finished successfully.
