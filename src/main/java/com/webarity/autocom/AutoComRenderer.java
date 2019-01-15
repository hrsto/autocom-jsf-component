package com.webarity.autocom;

import static javax.faces.application.ResourceHandler.JSF_SCRIPT_LIBRARY_NAME;
import static javax.faces.application.ResourceHandler.JSF_SCRIPT_RESOURCE_NAME;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialResponseWriter;
import javax.faces.render.ClientBehaviorRenderer;
import javax.json.Json;
import javax.json.JsonArrayBuilder;

@ResourceDependencies({
    @ResourceDependency(library = "autocom", name = "autocom.js", target = "head"),
    @ResourceDependency(library = JSF_SCRIPT_LIBRARY_NAME, name = JSF_SCRIPT_RESOURCE_NAME, target = "head")
})
public class AutoComRenderer extends ClientBehaviorRenderer {

    @Override
    public void decode(FacesContext ctx, UIComponent comp, ClientBehavior behavior) {
        if (!(behavior instanceof AutoCom)) return;
        AutoCom b = (AutoCom)behavior;

        UIInput q = (UIInput)comp;
        String id = String.format("%s:%s", q.getParent().getId(), q.getId());
        String incomingVal = ctx.getExternalContext().getRequestParameterMap().get(id);

        String searchResult = "[]";
        int totalEntries = 0;

        if (b.getSource() instanceof String) {
            String[] tempVals = ((String)b.getSource()).split(b.getDelimiter());
            totalEntries = tempVals.length;
            searchResult = constructSearchResult(Stream.of(tempVals), incomingVal, b.getMaxResults());
        } else if (b.getSource() instanceof Map) {
            @SuppressWarnings("unchecked") Map<String, ?> tempVals = (Map<String, ?>)b.getSource();
            totalEntries = tempVals.size();
            searchResult = constructSearchResult(tempVals.keySet().stream(), incomingVal, b.getMaxResults());
        } else if (b.getSource() instanceof List) {
            @SuppressWarnings("unchecked") List<String> tempVals = (List<String>)b.getSource();
            totalEntries = tempVals.size();
            searchResult = constructSearchResult(tempVals.stream(), incomingVal, b.getMaxResults());
        } else if (b.getSource() instanceof Set) {
            @SuppressWarnings("unchecked") Set<String> tempVals = (Set<String>)b.getSource();
            totalEntries = tempVals.size();
            searchResult = constructSearchResult(tempVals.stream(), incomingVal, b.getMaxResults());
        } else if (b.getSource() instanceof String[]) {
            String[] tempVals = (String[])b.getSource();
            totalEntries = tempVals.length;
            searchResult = constructSearchResult(Stream.of(tempVals), incomingVal, b.getMaxResults());
        } else {
            totalEntries = -1;
        }

        HashMap<String, String> extAttrs = new HashMap<>();
        extAttrs.put("for", id);
        extAttrs.put("parent", q.getParent().getId());
        extAttrs.put("totalEntries", Integer.toString(totalEntries));

        try (
            PartialResponseWriter prw = ctx.getPartialViewContext().getPartialResponseWriter();
        ) {
            prw.startDocument();
            prw.startExtension(extAttrs);
            prw.write(searchResult);
            prw.endExtension();
            prw.startEval();
            if (totalEntries == -1) {
                prw.write("console.error('From server: Supported type for suggestion values are: Map (its keys), List, Set, String[], String of values delimited some delimiter.');");
            }
            prw.endEval();
            prw.endDocument();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getScript(ClientBehaviorContext bCtx, ClientBehavior behavior) {
        if (!(bCtx.getComponent() instanceof UIInput))
            return String.format("console.error('Unsupported component for component id %s.);'",
                bCtx.getComponent().getId());

        UIInput q = (UIInput) bCtx.getComponent();
        String id = String.format("%s:%s", q.getParent().getId(), q.getId());
        AutoCom b = (AutoCom)behavior;

        return String.format("WebarityAutoCom.makeRQ('%s', '%s', '%s');", id, bCtx.getEventName(), b.getCallbackFunc());
    }
    
    private String constructSearchResult(Stream<String> str, String searchVal, Integer maxResults) {
        JsonArrayBuilder  searchResults = Json.createArrayBuilder();
        str.map(entry -> entry.trim().toLowerCase())
        .filter(entry -> entry.contains(searchVal.trim().toLowerCase()))
        .sorted()
        .limit(maxResults)
        .forEach(entry -> searchResults.add(entry));
        return searchResults.build().toString();
    }
}