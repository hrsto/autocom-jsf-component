# Autocomplete JSF Component

Lightweight Autocomplete jsf component implemented as a jsf behavior. Can be attached to any component of type `UIInput`.

### Features:

* Lightweight
* Unopinionated - it just hands you the results as a JSON array and some metadata, and leaves the rest to you.
* Supports:
    * `java.util.Map<String, ?>` - search is based on the keys of the `Map`
    * `java.util.List<String>`
    * `java.util.Set<String>`
    * `java.lang.String[]`
    * `java.lang.String` - as a simple String that will internally be split into an array of Strings with whitespace trimmed. For ex: `<cb:AutoComDefault source="a,b,c, d, e" />`... The delimiter is customizable. 

### Usage:

In your facelets page import the namespace `https://www.webarity.com/custom-behaviors/autocom`. For ex:
```xml
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html lang="#{siteLocales.getCurrentLocale().getLanguage()}" xmlns="http://www.w3.org/1999/xhtml" xmlns:h="http://xmlns.jcp.org/jsf/html" xmlns:f="http://xmlns.jcp.org/jsf/core" xmlns:ui="http://java.sun.com/jsf/facelets" xmlns:h5a="http://xmlns.jcp.org/jsf/passthrough" xmlns:h5e="http://xmlns.jcp.org/jsf"
xmlns:ac="https://www.webarity.com/custom-behaviors/autocom">
...
</html>
```

Above we use the `ac` namespace, but that can be arbitrary. Now add the component:

```html
<h:form>
    <h:inputText id="thingie">
        <cb:AutoComDefault source="#{SomeBeanOfTypeSet}" callbackFunc="someFunction" />
    </h:inputText>
</h:form>
```

The `AutoComDefault` tag will automatically listen for `onfocus` and `onkeyup` html evens. The `source` attribute is mandatory and should point to one of the above mentioned types.

Another ex:

```html
<h:form>
    <h:inputText id="thingie">
        <cb:AutoCom source="apple, orange, car" event="keyup" callbackFunc="someFunction" />
    </h:inputText>
</h:form>
```

### `callbackFunc` attribute:

`someFunction` is a JavaScript function. For ex:

```javascript
function someFunction(for, parent, results, length) {
    console.log(for); //id of DOM element that the behavior is attached to. I.e. the <input /> element
    console.log(parent); //id of its parent as in terms of the .xhtml faceles page source - most probably the <form ...> element
    console.log(results); //an array of the results as strings
    console.log(length); //the total size of the collection as it's on the server, before being limited by the maxResult attibute of the tag

    //do logic to render some UI element to display those result and enable user to select them; anyway you wish.
}
```

### All attributes:

* source - `java.util.Map<String, ?>`, `java.util.List<String>`, `java.util.Set<String>`, `java.lang.String[]`, `java.lang.String`
* calbackFunc - `java.lang.String` of the JavaScript function to call
* maxResults - `java.lang.Integer` to limit how many results are returned. Defaults to `50`. Note: the `length` argument that is returned and available in the JavaScript callback is for the size of the collection before being limited to `maxResults`.
* delimiter - `java.lang.String` - a java regular expression. Defaults to `" "` (a single white space).
* event - JSF specific. For `<cb:AutoComDefault/>` implicitly two events will be registered - `keyup` and `focus`. For `<cb:AutoCom/>` if not specified, JSF will use the Component's default event.



---

## Prerequisites:

* Java EE8
* JDK >= 10
* Maven >= 3.5.x

## Running

* mvn clean package
* copy jar file to WEB-INF/lib/ for .war deployments
* copy jar file to /lib/ for .ear deployments

## Tested on

* WildFly 15 with jre v10.0.2

---
https://www.webarity.com