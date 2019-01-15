var WebarityAutoCom = (() => {
    const delay = 350;
    const errMsg = "No AutoCom callback function set. Specify a callback attribute for the AutoCom tag with name of function without parentheses. Or override WebarityAutoCom.defaultCallbackFunc with a custom function that takes 4 attributes: String#targetElement, String#parentElement, JSON#foundItems, Number#length";
    var clientCallback;

    var setClientCallback = (cb) => {
        if (typeof eval(cb) !== 'function') throw `clientCallback couldn't be resolved to 'function'`;
        clientCallback = eval(cb);
    };
    
    return {
        ajaxCallback: (evt) => {
            if (evt.status !== "success") return;
            var foundItems = JSON.parse(evt.responseXML.getElementsByTagName('extension')[0].textContent);
            var length = Number.parseInt(evt.responseXML.getElementsByTagName('extension')[0].attributes.totalEntries.value);
            var targetElement = evt.responseXML.getElementsByTagName('extension')[0].attributes.for;
            var parentElement = evt.responseXML.getElementsByTagName('extension')[0].attributes.parent;
            
            clientCallback(targetElement, parentElement, foundItems, length);
        },
        makeRQ: (id, evt, clientCallback) => {
            try {
                setClientCallback(clientCallback);

                jsf.ajax.request(id, null, {
                    onevent: WebarityAutoCom.ajaxCallback, 
                    'javax.faces.partial.execute': id, 
                    'javax.faces.partial.render': id, 
                    'javax.faces.behavior.event': evt, 
                    delay: delay
                });
            } catch (ex) {
                console.error(ex);
            }
        },
        defaultCallbackFunc() {
            console.error(errMsg);
        }
    };
})();