package com.webarity.autocom;

import java.io.IOException;
import java.util.Optional;

import javax.faces.component.UIComponent;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandler;

public class AutoComDefaultTag extends TagHandler {

    public AutoComDefaultTag(TagConfig conf) {
        super(conf);
    }

	@Override
	public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (!(parent instanceof ClientBehaviorHolder)) return;
        ClientBehaviorHolder p = (ClientBehaviorHolder)parent;
        
        AutoCom b = (AutoCom)ctx.getFacesContext().getApplication().createBehavior("autocom");

        Optional.ofNullable(getAttribute("delimiter")).ifPresent(attr -> b.setDelimiter((String)attr.getObject(ctx, String.class)));
        Optional.ofNullable(getAttribute("callbackFunc")).ifPresent(attr -> b.setCallbackFunc((String)attr.getObject(ctx, String.class)));
        Optional.ofNullable(getAttribute("maxResults")).ifPresent(attr -> b.setMaxResults((Integer)attr.getObject(ctx, Integer.class)));
        Optional.ofNullable(getAttribute("source")).ifPresent(attr -> b.setSource(attr.getObject(ctx)));

        p.addClientBehavior("focus", b);
        p.addClientBehavior("keyup", b);
	}

}