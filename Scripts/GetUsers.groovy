@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.2' )

import groovyx.net.http.RESTClient
import static groovyx.net.http.ContentType.*


RESTClient client = new RESTClient('https://api.victorops.com/api-public/v1/user')
client.defaultRequestHeaders.'X-VO-Api-Id' = ""
client.defaultRequestHeaders.'X-VO-Api-Key' = ""
client.defaultRequestHeaders.'Accept' = "application/json"
def response = client.get()
System.out.println(response)