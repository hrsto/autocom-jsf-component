package com.webarity.autocom;

import static javax.faces.application.ResourceHandler.JSF_SCRIPT_LIBRARY_NAME;
import static javax.faces.application.ResourceHandler.JSF_SCRIPT_RESOURCE_NAME;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
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
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;

@ResourceDependencies({
    @ResourceDependency(library = "autocom", name = "autocom.js", target = "head"),
    @ResourceDependency(library = JSF_SCRIPT_LIBRARY_NAME, name = JSF_SCRIPT_RESOURCE_NAME, target = "head")
})
public class AutoComRenderer extends ClientBehaviorRenderer {

    @Override
    public void decode(FacesContext ctx, UIComponent comp, ClientBehavior behavior) {
        if (!(behavior instanceof AutoCom)) return;
        AutoCom autoComBehavior = (AutoCom) behavior;

        UIInput affectedInput = (UIInput) comp;
        String id = affectedInput.getClientId();
        String incomingVal = sanitize(ctx.getExternalContext().getRequestParameterMap().get(id));

        JsonArray searchResult = null;

        if (autoComBehavior.getSource() instanceof String stringSource) {
            String[] tempVals = stringSource.split(autoComBehavior.getDelimiter());
            searchResult = constructSearchResult(Stream.of(tempVals), incomingVal, autoComBehavior.getMaxResults());
        } else if (autoComBehavior.getSource() instanceof Map<?, ?> mapSource) {
            Map<String, String> tempVals = mapSource.entrySet().stream()
                .filter(entry -> entry.getKey() instanceof String && entry.getValue() instanceof String)
                .collect(Collectors.toMap(key -> (String) key.getKey(), val -> (String) val.getValue()));
            searchResult = constructMappedSearchResult(tempVals.entrySet().stream(), incomingVal, autoComBehavior.getMaxResults());
        } else if (autoComBehavior.getSource() instanceof List<?> listSource) {
            List<String> tempVals = listSource.stream()
                .filter(item -> item instanceof String)
                .map(item -> (String) item)
                .collect(Collectors.toList());
            searchResult = constructSearchResult(tempVals.stream(), incomingVal, autoComBehavior.getMaxResults());
        } else if (autoComBehavior.getSource() instanceof Set<?> setSource) {
            Set<String> tempVals = setSource.stream()
                .filter(item -> item instanceof String)
                .map(item -> (String) item)
                .collect(Collectors.toSet());
            searchResult = constructSearchResult(tempVals.stream(), incomingVal, autoComBehavior.getMaxResults());
        } else if (autoComBehavior.getSource() instanceof String[] arraySource) {
            searchResult = constructSearchResult(Stream.of(arraySource), incomingVal, autoComBehavior.getMaxResults());
        }

        HashMap<String, String> extAttrs = new HashMap<>();
        extAttrs.put("for", id);
        extAttrs.put("parent", affectedInput.getParent().getId());
        extAttrs.put("totalEntries", searchResult == null ? "-1" : Integer.toString(searchResult.size()));

        try (
            PartialResponseWriter prw = ctx.getPartialViewContext().getPartialResponseWriter();
        ) {
            prw.startDocument();
            prw.startExtension(extAttrs);
            prw.write(searchResult == null ? null : searchResult.toString());
            prw.endExtension();
            prw.startEval();
            if (searchResult == null) {
                prw.write("console.error('From server: Supported type for suggestion values are: Map (keys and values), List, Set, String[], String of values delimited some by delimiter.');");
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
        String id = q.getClientId();
        AutoCom b = (AutoCom)behavior;

        return String.format("WebarityAutoCom.makeRQ('%s', '%s', '%s');", id, bCtx.getEventName(), b.getCallbackFunc());
    }
    
    private JsonArray constructSearchResult(Stream<String> str, String searchVal, Integer maxResults) {
        JsonArrayBuilder  searchResults = Json.createArrayBuilder();

        str.map(entry -> sanitize(entry.trim()))
        .filter(entry -> entry.toLowerCase().contains(searchVal == null ? "" : searchVal.trim().toLowerCase()))
        .sorted(Comparator.comparing(String::toLowerCase))
        .limit(maxResults)
        .forEach(entry -> searchResults.add(entry));
        return searchResults.build().asJsonArray();
    }
    
    private JsonArray constructMappedSearchResult(Stream<Entry<String, String>> str, String searchVal, Integer maxResults) {
        JsonArrayBuilder  searchResults = Json.createArrayBuilder();

        str.map(entry -> Map.entry(sanitize(entry.getKey().trim()), sanitize(entry.getValue().trim())))
        .filter(entry -> entry.getValue().toLowerCase().contains(searchVal == null ? "" : searchVal.trim().toLowerCase()))
        .sorted(Comparator.comparing(key -> key.getValue().toLowerCase()))
        .limit(maxResults)
        .forEach(entry -> searchResults.add(Json.createObjectBuilder().add(entry.getKey(), entry.getValue())));
        return searchResults.build().asJsonArray();
    }

    private String sanitize(String rawText) {
        return rawText.replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&apos;");
    }
}